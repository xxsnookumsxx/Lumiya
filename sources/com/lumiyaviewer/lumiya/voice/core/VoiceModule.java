package com.lumiyaviewer.lumiya.voice.core;

import android.content.Context;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.lumiyaviewer.lumiya.Debug;
import com.lumiyaviewer.lumiya.GlobalOptions;
import com.lumiyaviewer.lumiya.LumiyaApp;
import com.lumiyaviewer.lumiya.R;
import com.lumiyaviewer.lumiya.eventbus.EventBus;
import com.lumiyaviewer.lumiya.eventbus.EventHandler;
import com.lumiyaviewer.lumiya.react.Subscription;
import com.lumiyaviewer.lumiya.react.SubscriptionData;
import com.lumiyaviewer.lumiya.react.SubscriptionSingleKey;
import com.lumiyaviewer.lumiya.react.UIThreadExecutor;
import com.lumiyaviewer.lumiya.slproto.SLAgentCircuit;
import com.lumiyaviewer.lumiya.slproto.auth.SLAuthReply;
import com.lumiyaviewer.lumiya.slproto.caps.SLCaps;
import com.lumiyaviewer.lumiya.slproto.chat.SLChatSystemMessageEvent;
import com.lumiyaviewer.lumiya.slproto.https.LLSDXMLAsyncRequest;
import com.lumiyaviewer.lumiya.slproto.llsd.LLSDException;
import com.lumiyaviewer.lumiya.slproto.llsd.LLSDNode;
import com.lumiyaviewer.lumiya.slproto.llsd.types.LLSDMap;
import com.lumiyaviewer.lumiya.slproto.llsd.types.LLSDString;
import com.lumiyaviewer.lumiya.slproto.llsd.types.LLSDUUID;
import com.lumiyaviewer.lumiya.slproto.llsd.types.LLSDUndefined;
import com.lumiyaviewer.lumiya.slproto.modules.SLModule;
import com.lumiyaviewer.lumiya.slproto.modules.SLModules;
import com.lumiyaviewer.lumiya.slproto.modules.mutelist.MuteType;
import com.lumiyaviewer.lumiya.slproto.types.LLVector3d;
import com.lumiyaviewer.lumiya.slproto.users.ChatterID;
import com.lumiyaviewer.lumiya.slproto.users.chatsrc.ChatMessageSourceUnknown;
import com.lumiyaviewer.lumiya.slproto.users.manager.UserManager;
import com.lumiyaviewer.lumiya.voice.model.VoiceAudioDevice;
import com.lumiyaviewer.lumiya.voice.model.VoiceChannelInfo;
import com.lumiyaviewer.lumiya.voice.model.VoiceChatInfo;
import com.lumiyaviewer.lumiya.voice.model.VoiceLoginCredentials;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Second Life Voice Module integrating built-in WebRTC audio directly with SLAgentCircuit.
 */
public class VoiceModule extends SLModule implements WebRTCVoiceClient.SessionListener {
    private static final int INVALID_PARCEL_ID = -1;

    @Nullable
    private final String provisionURL;
    @Nullable
    private final String parcelVoiceInfoURL;
    @Nullable
    private final String chatSessionRequestURL;
    @Nullable
    private final String voiceSignalingURL;

    @Nullable
    private final UserManager userManager;
    private final VoiceAudioRouter audioRouter;
    private final WebRTCVoiceClient webRtcClient;

    private final Object parcelVoiceChannelLock = new Object();
    private int requestedParcelID = INVALID_PARCEL_ID;
    private int currentParcelID = INVALID_PARCEL_ID;
    @Nullable
    private VoiceChannelInfo currentParcelVoiceChannel = null;

    private final Set<UUID> requestedGroupChats = Collections.synchronizedSet(new HashSet<UUID>());
    private final Map<ChatterID, VoiceChannelInfo> knownChannels = Collections.synchronizedMap(new HashMap<ChatterID, VoiceChannelInfo>());

    private final SubscriptionData<SubscriptionSingleKey, Boolean> voiceLoggedInSub;
    @Nullable
    private volatile VoiceLoginCredentials loginCredentials;
    private volatile boolean voiceEnabled = false;
    private volatile boolean voiceLoggedIn = false;
    private volatile boolean shutdown = false;

    public VoiceModule(@Nonnull SLAgentCircuit circuit, @Nonnull SLCaps caps) {
        super(circuit);

        this.provisionURL = caps.getCapability(SLCaps.SLCapability.ProvisionVoiceAccountRequest);
        this.parcelVoiceInfoURL = caps.getCapability(SLCaps.SLCapability.ParcelVoiceInfoRequest);
        this.chatSessionRequestURL = caps.getCapability(SLCaps.SLCapability.ChatSessionRequest);
        this.voiceSignalingURL = caps.getCapability(SLCaps.SLCapability.VoiceSignalingRequest);

        this.userManager = UserManager.getUserManager(circuit.getAgentUUID());
        Context appContext = LumiyaApp.getContext();
        this.audioRouter = new VoiceAudioRouter(appContext, this.userManager);
        this.webRtcClient = new WebRTCVoiceClient(appContext);

        this.voiceLoggedInSub = new SubscriptionData<>(UIThreadExecutor.getInstance(), new Subscription.OnData<Boolean>() {
            @Override
            public void onData(Boolean loggedIn) {
                VoiceModule.this.voiceLoggedIn = loggedIn != null && loggedIn;
            }
        });

        if (this.userManager != null) {
            this.voiceLoggedInSub.subscribe(this.userManager.getVoiceLoggedIn(), SubscriptionSingleKey.Value);
        }

        Debug.Printf("VoiceModule: provision=%s, parcel=%s, chatSession=%s, signaling=%s",
                provisionURL, parcelVoiceInfoURL, chatSessionRequestURL, voiceSignalingURL);

        EventBus.getInstance().subscribe(this);
        updateVoiceEnabledStatus();
    }

    @Override
    public void HandleCloseCircuit() {
        shutdownVoice();
        super.HandleCloseCircuit();
    }

    /**
     * Enables voice readiness when ProvisionVoiceAccountRequest is available and voice is enabled.
     */
    public boolean requestProvisioning() {
        if (shutdown) {
            return false;
        }
        updateVoiceEnabledStatus();
        return voiceEnabled && provisionURL != null;
    }

    /**
     * Request parcel voice info for current avatar's parcel.
     */
    public boolean requestParcelVoiceInfo() {
        if (parcelVoiceInfoURL == null || shutdown) {
            return false;
        }
        final int targetParcelId = currentParcelID;
        new LLSDXMLAsyncRequest(parcelVoiceInfoURL, new LLSDUndefined(), new LLSDXMLAsyncRequest.LLSDXMLResultListener() {
            @Override
            public void onLLSDXMLResult(LLSDNode node) {
                handleParcelVoiceInfoResult(targetParcelId, node);
            }
        });
        return true;
    }

    public void setCurrentParcel(final int parcelID) {
        boolean shouldRequest = false;
        synchronized (parcelVoiceChannelLock) {
            if (parcelVoiceInfoURL != null && provisionURL != null && requestedParcelID != parcelID) {
                requestedParcelID = parcelID;
                shouldRequest = true;
            }
        }
        if (shouldRequest) {
            new LLSDXMLAsyncRequest(parcelVoiceInfoURL, new LLSDUndefined(), new LLSDXMLAsyncRequest.LLSDXMLResultListener() {
                @Override
                public void onLLSDXMLResult(LLSDNode node) {
                    handleParcelVoiceInfoResult(parcelID, node);
                }
            });
        }
    }

    private void handleParcelVoiceInfoResult(int parcelID, @Nullable LLSDNode node) {
        if (shutdown) {
            return;
        }
        currentParcelID = parcelID;
        boolean changed = false;
        VoiceChannelInfo channelInfo = null;
        if (node != null) {
            try {
                LLSDNode credsNode = node.keyExists("voice_credentials")
                        ? node.byKey("voice_credentials")
                        : (node.keyExists("credentials") ? node.byKey("credentials") : null);
                if (credsNode != null && credsNode.keyExists("channel_uri")) {
                    String channelUri = credsNode.byKey("channel_uri").asString();
                    String channelCreds = credsNode.keyExists("channel_credentials")
                            ? credsNode.byKey("channel_credentials").asString()
                            : null;
                    if (!Strings.isNullOrEmpty(channelUri)) {
                        channelInfo = new VoiceChannelInfo(channelUri, channelCreds, true, true);
                    }
                }
            } catch (LLSDException e) {
                Debug.Printf("VoiceModule: error parsing parcel voice info for %d (%s)", parcelID, e.getMessage());
            }
        }
        synchronized (parcelVoiceChannelLock) {
            if (!Objects.equal(currentParcelVoiceChannel, channelInfo)) {
                currentParcelVoiceChannel = channelInfo;
                changed = true;
            }
        }
        if (changed) {
            SLModules modules = agentCircuit.getModules();
            if (modules != null) {
                modules.minimap.requestUpdateAvatarParcelData();
            }
        }
    }

    @Nullable
    public VoiceChannelInfo getCurrentParcelVoiceChannel() {
        synchronized (parcelVoiceChannelLock) {
            return currentParcelVoiceChannel;
        }
    }

    public void nearbyVoiceChatRequest(@Nullable VoiceChannelInfo voiceChannelInfo) {
        if (!voiceEnabled || !voiceLoggedIn || voiceChannelInfo == null || userManager == null) {
            return;
        }
        ChatterID localChatter = ChatterID.getLocalChatterID(userManager.getUserID());
        connectChannel(localChatter, voiceChannelInfo);
    }

    public boolean groupVoiceChatRequest(@Nullable UUID groupId) {
        if (!voiceEnabled || !voiceLoggedIn || chatSessionRequestURL == null || groupId == null) {
            return false;
        }
        requestedGroupChats.add(groupId);
        agentCircuit.StartGroupSessionForVoice(groupId);
        return true;
    }

    public void onGroupSessionReady(@Nonnull final UUID groupId) {
        if (!requestedGroupChats.remove(groupId) || chatSessionRequestURL == null || shutdown) {
            return;
        }
        LLSDMap body = new LLSDMap(
                new LLSDMap.LLSDMapEntry("method", new LLSDString("call")),
                new LLSDMap.LLSDMapEntry("session-id", new LLSDUUID(groupId))
        );
        new LLSDXMLAsyncRequest(chatSessionRequestURL, body, new LLSDXMLAsyncRequest.LLSDXMLResultListener() {
            @Override
            public void onLLSDXMLResult(LLSDNode node) {
                if (shutdown || userManager == null) {
                    return;
                }
                ChatterID groupChatter = ChatterID.getGroupChatterID(userManager.getUserID(), groupId);
                try {
                    if (node == null) {
                        throw new LLSDException("Null ChatSessionRequest response");
                    }
                    LLSDNode credsNode = node.keyExists("voice_credentials")
                            ? node.byKey("voice_credentials")
                            : (node.keyExists("credentials") ? node.byKey("credentials") : null);
                    if (credsNode == null) {
                        throw new LLSDException("Missing voice_credentials");
                    }
                    String channelUri = credsNode.byKey("channel_uri").asString();
                    String channelCreds = credsNode.keyExists("channel_credentials")
                            ? credsNode.byKey("channel_credentials").asString()
                            : null;
                    if (voiceEnabled && voiceLoggedIn) {
                        VoiceChannelInfo channelInfo = new VoiceChannelInfo(channelUri, channelCreds, false, true);
                        connectChannel(groupChatter, channelInfo);
                    }
                } catch (LLSDException e) {
                    Debug.Warning(e);
                    agentCircuit.HandleChatEvent(
                            groupChatter,
                            new SLChatSystemMessageEvent(
                                    ChatMessageSourceUnknown.getInstance(),
                                    userManager.getUserID(),
                                    LumiyaApp.getContext().getString(R.string.failed_to_connect_group_voice)),
                            false);
                }
            }
        });
    }

    public boolean userVoiceChatRequest(@Nullable final UUID targetUserId) {
        if (!voiceEnabled || !voiceLoggedIn || targetUserId == null || chatSessionRequestURL == null || userManager == null) {
            return false;
        }
        final ChatterID userChatter = ChatterID.getUserChatterID(userManager.getUserID(), targetUserId);
        LLSDMap body = new LLSDMap(
                new LLSDMap.LLSDMapEntry("method", new LLSDString("call")),
                new LLSDMap.LLSDMapEntry("session-id", new LLSDUUID(targetUserId))
        );
        new LLSDXMLAsyncRequest(chatSessionRequestURL, body, new LLSDXMLAsyncRequest.LLSDXMLResultListener() {
            @Override
            public void onLLSDXMLResult(LLSDNode node) {
                if (shutdown || userManager == null) {
                    return;
                }
                try {
                    if (node == null) {
                        throw new LLSDException("Null ChatSessionRequest response");
                    }
                    LLSDNode credsNode = node.keyExists("voice_credentials")
                            ? node.byKey("voice_credentials")
                            : (node.keyExists("credentials") ? node.byKey("credentials") : null);
                    if (credsNode == null || !credsNode.keyExists("channel_uri")) {
                        throw new LLSDException("Missing voice_credentials for P2P call");
                    }
                    String channelUri = credsNode.byKey("channel_uri").asString();
                    String channelCreds = credsNode.keyExists("channel_credentials")
                            ? credsNode.byKey("channel_credentials").asString()
                            : null;
                    if (voiceEnabled && voiceLoggedIn) {
                        VoiceChannelInfo channelInfo = new VoiceChannelInfo(channelUri, channelCreds, false, false);
                        connectChannel(userChatter, channelInfo);
                    }
                } catch (LLSDException e) {
                    Debug.Warning(e);
                    agentCircuit.HandleChatEvent(
                            userChatter,
                            new SLChatSystemMessageEvent(
                                    ChatMessageSourceUnknown.getInstance(),
                                    userManager.getUserID(),
                                    LumiyaApp.getContext().getString(R.string.failed_to_connect_group_voice)),
                            false);
                }
            }
        });
        return true;
    }

    public void acceptVoiceCall(@Nullable ChatterID chatterID) {
        if (chatterID == null || !voiceEnabled || !voiceLoggedIn) {
            return;
        }
        VoiceChannelInfo info = knownChannels.get(chatterID);
        if (info == null && chatterID instanceof ChatterID.ChatterIDLocal) {
            info = getCurrentParcelVoiceChannel();
        }
        if (info != null) {
            connectChannel(chatterID, info);
        }
    }

    public void terminateVoiceCall(@Nullable ChatterID chatterID) {
        ChatterID active = webRtcClient.getActiveChatterID();
        if (chatterID == null || Objects.equal(active, chatterID)) {
            terminateSession();
        }
    }

    private void connectChannel(@Nonnull ChatterID chatterID, @Nonnull VoiceChannelInfo channelInfo) {
        if (shutdown || provisionURL == null || userManager == null) {
            return;
        }
        knownChannels.put(chatterID, channelInfo);
        userManager.setVoiceActiveChatter(chatterID);
        audioRouter.activate();

        WebRTCVoiceClient.SessionParams params = new WebRTCVoiceClient.SessionParams(
                chatterID,
                channelInfo,
                channelInfo.isSpatial ? currentParcelID : INVALID_PARCEL_ID,
                provisionURL,
                voiceSignalingURL,
                resolveStunServers(),
                agentCircuit.getAgentUUID(),
                new WebRTCVoiceClient.MuteChecker() {
                    @Override
                    public boolean isVoiceMuted(@Nonnull UUID agentUUID) {
                        SLModules modules = agentCircuit.getModules();
                        return modules != null && modules.muteList.isMuted(agentUUID, MuteType.AGENT);
                    }
                },
                this
        );
        webRtcClient.connect(params);
    }

    @Nonnull
    private List<String> resolveStunServers() {
        List<String> servers = new ArrayList<>();
        SLAuthReply authReply = agentCircuit.getAuthReply();
        String loginUrl = authReply != null ? Strings.nullToEmpty(authReply.loginURL).toLowerCase() : "";
        if (loginUrl.contains("aditi")) {
            servers.add("stun:stun1.aditi.secondlife.io:3478");
            servers.add("stun:stun2.aditi.secondlife.io:3478");
        } else if (loginUrl.contains("agni") || loginUrl.contains("lindenlab.com") || loginUrl.contains("secondlife.com")) {
            servers.add("stun:stun1.agni.secondlife.io:3478");
            servers.add("stun:stun2.agni.secondlife.io:3478");
            servers.add("stun:stun3.agni.secondlife.io:3478");
        } else {
            servers.add("stun:stun.l.google.com:19302");
            servers.add("stun:stun1.l.google.com:19302");
        }
        return servers;
    }

    public void setMicEnabled(boolean enabled) {
        if (shutdown) {
            return;
        }
        webRtcClient.setMicEnabled(enabled);
    }

    public void setAudioDevice(@Nonnull VoiceAudioDevice device) {
        audioRouter.setAudioDevice(device);
    }

    public void setSpeakerVolume(float volume) {
        audioRouter.setSpeakerVolume(volume);
    }

    public void updateSpatialVoicePosition() {
        VoiceChannelInfo activeChannel = webRtcClient.getActiveChannelInfo();
        if (shutdown || !voiceLoggedIn || activeChannel == null || !activeChannel.isSpatial) {
            return;
        }
        LLVector3d pos = agentCircuit.getAgentGlobalPosition();
        SLModules modules = agentCircuit.getModules();
        if (pos == null || modules == null) {
            return;
        }
        float headingRad = modules.avatarControl.getAgentHeading() * 0.017453292f;
        float halfAngle = headingRad * 0.5f;
        float qz = (float) Math.sin(halfAngle);
        float qw = (float) Math.cos(halfAngle);

        webRtcClient.sendSpatialPosition(
                (float) pos.x, (float) pos.y, (float) pos.z,
                0.0f, 0.0f, qz, qw,
                (float) pos.x, (float) pos.y, (float) pos.z,
                0.0f, 0.0f, qz, qw);
    }

    public void terminateSession() {
        ChatterID active = webRtcClient.getActiveChatterID();
        webRtcClient.setMicEnabled(false);
        webRtcClient.disconnect();
        audioRouter.deactivate();
        if (userManager != null) {
            if (active != null) {
                userManager.setVoiceChatInfo(active, VoiceChatInfo.empty());
            }
            userManager.setVoiceActiveChatter(null);
        }
    }

    @EventHandler
    public void onGlobalOptionsChanged(GlobalOptions.GlobalOptionsChangedEvent event) {
        updateVoiceEnabledStatus();
    }

    public void updateVoiceEnabledStatus() {
        if (shutdown) {
            return;
        }
        voiceEnabled = GlobalOptions.getInstance().getVoiceEnabled();
        if (!voiceEnabled) {
            terminateSession();
            if (userManager != null) {
                userManager.setVoiceLoggedIn(false);
            }
        } else if (provisionURL != null) {
            if (userManager != null) {
                userManager.setVoiceLoggedIn(true);
            }
            audioRouter.publishProperties();
        } else {
            if (userManager != null) {
                userManager.setVoiceLoggedIn(false);
            }
        }
    }

    public void shutdownVoice() {
        if (shutdown) {
            return;
        }
        shutdown = true;
        Debug.Printf("VoiceModule: initiating shutdown");
        EventBus.getInstance().unsubscribe(this);
        voiceLoggedInSub.unsubscribe();
        webRtcClient.shutdown();
        audioRouter.release();
        if (userManager != null) {
            userManager.setVoiceLoggedIn(false);
            userManager.setVoiceActiveChatter(null);
        }
        loginCredentials = null;
    }

    @Override
    public void onChatStateChanged(@Nonnull ChatterID chatterID, @Nonnull VoiceChatInfo info) {
        if (userManager != null) {
            userManager.setVoiceChatInfo(chatterID, info);
            if (info.state == VoiceChatInfo.VoiceChatState.None) {
                audioRouter.deactivate();
                userManager.setVoiceActiveChatter(null);
            } else {
                userManager.setVoiceActiveChatter(chatterID);
            }
        }
    }

    @Override
    public void onSessionError(@Nonnull ChatterID chatterID, @Nonnull String errorMessage) {
        Debug.Printf("VoiceModule: session error for %s: %s", chatterID, errorMessage);
        if (userManager != null) {
            agentCircuit.HandleChatEvent(
                    chatterID,
                    new SLChatSystemMessageEvent(
                            ChatMessageSourceUnknown.getInstance(),
                            userManager.getUserID(),
                            errorMessage),
                    false);
        }
    }

    public boolean isVoiceEnabled() {
        return voiceEnabled;
    }

    public boolean isVoiceLoggedIn() {
        return voiceLoggedIn;
    }

    @Nullable
    public VoiceLoginCredentials getLoginCredentials() {
        return loginCredentials;
    }
}
