package com.lumiyaviewer.lumiya.voice.core;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import com.lumiyaviewer.lumiya.Debug;
import com.lumiyaviewer.lumiya.slproto.https.LLSDXMLRequest;
import com.lumiyaviewer.lumiya.slproto.llsd.LLSDNode;
import com.lumiyaviewer.lumiya.slproto.llsd.types.LLSDArray;
import com.lumiyaviewer.lumiya.slproto.llsd.types.LLSDBoolean;
import com.lumiyaviewer.lumiya.slproto.llsd.types.LLSDInt;
import com.lumiyaviewer.lumiya.slproto.llsd.types.LLSDMap;
import com.lumiyaviewer.lumiya.slproto.llsd.types.LLSDString;
import com.lumiyaviewer.lumiya.slproto.users.ChatterID;
import com.lumiyaviewer.lumiya.voice.model.VoiceChannelInfo;
import com.lumiyaviewer.lumiya.voice.model.VoiceChatInfo;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.MediaStreamTrack;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RtpReceiver;
import org.webrtc.RtpTransceiver;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.audio.AudioDeviceModule;
import org.webrtc.audio.JavaAudioDeviceModule;

/**
 * Native Android WebRTC client implementing Second Life's WebRTC voice signaling
 * (ProvisionVoiceAccountRequest + VoiceSignalingRequest) and SLData JSON channel.
 */
public class WebRTCVoiceClient {
    private static final String WEBRTC_VOICE_SERVER_TYPE = "webrtc";
    private static final String DATA_CHANNEL_LABEL = "SLData";
    private static final long POSITION_UPDATE_MIN_INTERVAL_MS = 100L;

    private static volatile boolean factoryInitialized = false;
    private static PeerConnectionFactory sharedFactory;
    private static AudioDeviceModule sharedAudioDeviceModule;
    private static final Object factoryLock = new Object();

    public interface MuteChecker {
        boolean isVoiceMuted(@Nonnull UUID agentUUID);
    }

    public interface SessionListener {
        void onChatStateChanged(@Nonnull ChatterID chatterID, @Nonnull VoiceChatInfo info);
        void onSessionError(@Nonnull ChatterID chatterID, @Nonnull String errorMessage);
    }

    public static class SessionParams {
        public final ChatterID chatterID;
        public final VoiceChannelInfo channelInfo;
        public final int parcelLocalID;
        public final String provisionURL;
        @Nullable
        public final String signalingURL;
        public final List<String> stunServers;
        public final UUID agentUUID;
        public final MuteChecker muteChecker;
        public final SessionListener listener;

        public SessionParams(@Nonnull ChatterID chatterID,
                             @Nonnull VoiceChannelInfo channelInfo,
                             int parcelLocalID,
                             @Nonnull String provisionURL,
                             @Nullable String signalingURL,
                             @Nonnull List<String> stunServers,
                             @Nonnull UUID agentUUID,
                             @Nonnull MuteChecker muteChecker,
                             @Nonnull SessionListener listener) {
            this.chatterID = chatterID;
            this.channelInfo = channelInfo;
            this.parcelLocalID = parcelLocalID;
            this.provisionURL = provisionURL;
            this.signalingURL = signalingURL;
            this.stunServers = stunServers;
            this.agentUUID = agentUUID;
            this.muteChecker = muteChecker;
            this.listener = listener;
        }
    }

    private static class ParticipantState {
        boolean isSpeaking = false;
        boolean isModeratorMuted = false;
        float level = 0.0f;
    }

    private final Context appContext;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Object sessionLock = new Object();

    @Nullable
    private ActiveSession activeSession;
    private volatile boolean micEnabled = false;
    private volatile boolean disposed = false;

    public WebRTCVoiceClient(@Nonnull Context context) {
        this.appContext = context.getApplicationContext();
    }

    private static PeerConnectionFactory getOrCreateFactory(Context context) {
        synchronized (factoryLock) {
            if (!factoryInitialized) {
                PeerConnectionFactory.initialize(
                        PeerConnectionFactory.InitializationOptions.builder(context)
                                .setEnableInternalTracer(false)
                                .createInitializationOptions());
                sharedAudioDeviceModule = JavaAudioDeviceModule.builder(context)
                        .setUseHardwareAcousticEchoCanceler(true)
                        .setUseHardwareNoiseSuppressor(true)
                        .createAudioDeviceModule();
                sharedFactory = PeerConnectionFactory.builder()
                        .setAudioDeviceModule(sharedAudioDeviceModule)
                        .createPeerConnectionFactory();
                factoryInitialized = true;
            }
            return sharedFactory;
        }
    }

    public boolean isMicEnabled() {
        return micEnabled;
    }

    public boolean isConnectedOrConnecting() {
        synchronized (sessionLock) {
            return activeSession != null && !activeSession.closed;
        }
    }

    @Nullable
    public ChatterID getActiveChatterID() {
        synchronized (sessionLock) {
            return activeSession != null && !activeSession.closed ? activeSession.params.chatterID : null;
        }
    }

    @Nullable
    public VoiceChannelInfo getActiveChannelInfo() {
        synchronized (sessionLock) {
            return activeSession != null && !activeSession.closed ? activeSession.params.channelInfo : null;
        }
    }

    public void connect(@Nonnull SessionParams params) {
        if (disposed) {
            return;
        }
        executor.execute(() -> {
            disconnectInternal();
            ActiveSession session = new ActiveSession(params);
            synchronized (sessionLock) {
                if (disposed) {
                    session.close(false);
                    return;
                }
                activeSession = session;
            }
            session.start();
        });
    }

    public void disconnect() {
        executor.execute(this::disconnectInternal);
    }

    public void shutdown() {
        disposed = true;
        disconnect();
        executor.shutdown();
    }

    public void setMicEnabled(boolean enabled) {
        this.micEnabled = enabled;
        executor.execute(() -> {
            ActiveSession session;
            synchronized (sessionLock) {
                session = activeSession;
            }
            if (session != null && !session.closed) {
                session.applyMicEnabled(enabled);
            }
        });
    }

    public void sendSpatialPosition(float spX, float spY, float spZ,
                                    float shX, float shY, float shZ, float shW,
                                    float lpX, float lpY, float lpZ,
                                    float lhX, float lhY, float lhZ, float lhW) {
        ActiveSession session;
        synchronized (sessionLock) {
            session = activeSession;
        }
        if (session == null || session.closed || !session.params.channelInfo.isSpatial) {
            return;
        }
        session.maybeSendPosition(spX, spY, spZ, shX, shY, shZ, shW, lpX, lpY, lpZ, lhX, lhY, lhZ, lhW);
    }

    private void disconnectInternal() {
        ActiveSession prev;
        synchronized (sessionLock) {
            prev = activeSession;
            activeSession = null;
        }
        if (prev != null) {
            prev.close(true);
        }
    }

    private class ActiveSession {
        final SessionParams params;
        final Map<UUID, ParticipantState> participants = new ConcurrentHashMap<>();
        final List<IceCandidate> pendingCandidates = new ArrayList<>();

        @Nullable
        PeerConnection peerConnection;
        @Nullable
        AudioSource audioSource;
        @Nullable
        AudioTrack localAudioTrack;
        @Nullable
        DataChannel dataChannel;
        @Nullable
        LLSDNode viewerSessionNode;

        volatile boolean closed = false;
        boolean iceCompletedPending = false;
        VoiceChatInfo.VoiceChatState currentState = VoiceChatInfo.VoiceChatState.Connecting;
        long lastPositionSentMs = 0L;
        String lastPositionPayload = null;

        ActiveSession(SessionParams params) {
            this.params = params;
        }

        void start() {
            publishState(VoiceChatInfo.VoiceChatState.Connecting, VoiceChatInfo.VoiceChatState.None);
            try {
                PeerConnectionFactory factory = getOrCreateFactory(appContext);

                List<PeerConnection.IceServer> iceServers = new ArrayList<>();
                for (String url : params.stunServers) {
                    iceServers.add(PeerConnection.IceServer.builder(url).createIceServer());
                }
                PeerConnection.RTCConfiguration rtcConfig = new PeerConnection.RTCConfiguration(iceServers);
                rtcConfig.sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN;
                rtcConfig.continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_ONCE;

                peerConnection = factory.createPeerConnection(rtcConfig, new PeerConnectionObserver());
                if (peerConnection == null) {
                    fail("Failed to create WebRTC PeerConnection");
                    return;
                }

                boolean hasRecordPermission = ContextCompat.checkSelfPermission(
                        appContext, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;

                if (hasRecordPermission) {
                    MediaConstraints audioConstraints = new MediaConstraints();
                    audioSource = factory.createAudioSource(audioConstraints);
                    localAudioTrack = factory.createAudioTrack("ARDAMSa0", audioSource);
                    localAudioTrack.setEnabled(micEnabled);
                    peerConnection.addTrack(localAudioTrack, Collections.singletonList("ARDAMS"));
                } else {
                    Debug.Printf("WebRTCVoiceClient: RECORD_AUDIO not granted, joining receive-only");
                    peerConnection.addTransceiver(
                            MediaStreamTrack.MediaType.MEDIA_TYPE_AUDIO,
                            new RtpTransceiver.RtpTransceiverInit(RtpTransceiver.RtpTransceiverDirection.RECV_ONLY));
                }

                DataChannel.Init dcInit = new DataChannel.Init();
                dcInit.ordered = true;
                dataChannel = peerConnection.createDataChannel(DATA_CHANNEL_LABEL, dcInit);
                if (dataChannel != null) {
                    dataChannel.registerObserver(new DataChannelObserver(dataChannel));
                }

                MediaConstraints offerConstraints = new MediaConstraints();
                offerConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
                peerConnection.createOffer(new SimpleSdpObserver() {
                    @Override
                    public void onCreateSuccess(SessionDescription sdp) {
                        if (closed || peerConnection == null) {
                            return;
                        }
                        peerConnection.setLocalDescription(new SimpleSdpObserver() {
                            @Override
                            public void onSetSuccess() {
                                if (!closed) {
                                    executor.execute(() -> sendProvisionOffer(sdp.description));
                                }
                            }

                            @Override
                            public void onSetFailure(String error) {
                                fail("setLocalDescription failed: " + error);
                            }
                        }, sdp);
                    }

                    @Override
                    public void onCreateFailure(String error) {
                        fail("createOffer failed: " + error);
                    }
                }, offerConstraints);
            } catch (Throwable t) {
                Debug.Warning(t);
                fail("WebRTC initialization failed: " + t.getMessage());
            }
        }

        void applyMicEnabled(boolean enabled) {
            if (localAudioTrack != null) {
                try {
                    localAudioTrack.setEnabled(enabled);
                } catch (Exception e) {
                    Debug.Warning(e);
                }
            }
            if (currentState == VoiceChatInfo.VoiceChatState.Active) {
                publishState(VoiceChatInfo.VoiceChatState.Active, VoiceChatInfo.VoiceChatState.Active);
            }
        }

        private void sendProvisionOffer(String offerSdp) {
            if (closed) {
                return;
            }
            try {
                LLSDMap jsepMap = new LLSDMap(
                        new LLSDMap.LLSDMapEntry("type", new LLSDString("offer")),
                        new LLSDMap.LLSDMapEntry("sdp", new LLSDString(offerSdp))
                );

                List<LLSDMap.LLSDMapEntry> entries = new ArrayList<>();
                entries.add(new LLSDMap.LLSDMapEntry("jsep", jsepMap));
                entries.add(new LLSDMap.LLSDMapEntry("voice_server_type", new LLSDString(WEBRTC_VOICE_SERVER_TYPE)));

                if (params.channelInfo.isSpatial) {
                    entries.add(new LLSDMap.LLSDMapEntry("channel_type", new LLSDString("local")));
                    if (params.parcelLocalID >= 0) {
                        entries.add(new LLSDMap.LLSDMapEntry("parcel_local_id", new LLSDInt(params.parcelLocalID)));
                    }
                } else {
                    entries.add(new LLSDMap.LLSDMapEntry("channel_type", new LLSDString("multiagent")));
                    if (params.channelInfo.voiceChannelURI != null) {
                        entries.add(new LLSDMap.LLSDMapEntry("channel", new LLSDString(params.channelInfo.voiceChannelURI)));
                    }
                    if (params.channelInfo.channelCredentials != null) {
                        entries.add(new LLSDMap.LLSDMapEntry("credentials", new LLSDString(params.channelInfo.channelCredentials)));
                    }
                }

                LLSDMap requestBody = new LLSDMap(entries.toArray(new LLSDMap.LLSDMapEntry[0]));
                LLSDNode result = new LLSDXMLRequest().PerformRequest(params.provisionURL, requestBody);
                if (closed) {
                    return;
                }
                if (result == null || !result.keyExists("viewer_session") || !result.keyExists("jsep")) {
                    fail("Invalid ProvisionVoiceAccountRequest response");
                    return;
                }

                LLSDNode jsepResp = result.byKey("jsep");
                String answerType = jsepResp.keyExists("type") ? jsepResp.byKey("type").asString() : "";
                String answerSdp = jsepResp.keyExists("sdp") ? jsepResp.byKey("sdp").asString() : "";
                if (!"answer".equals(answerType) || answerSdp.isEmpty()) {
                    fail("Missing SDP answer in ProvisionVoiceAccountRequest response");
                    return;
                }

                viewerSessionNode = result.byKey("viewer_session");
                Debug.Printf("WebRTCVoiceClient: received SDP answer, setting remote description");

                if (peerConnection != null) {
                    peerConnection.setRemoteDescription(new SimpleSdpObserver() {
                        @Override
                        public void onSetSuccess() {
                            executor.execute(() -> flushIceUpdates());
                        }

                        @Override
                        public void onSetFailure(String error) {
                            fail("setRemoteDescription failed: " + error);
                        }
                    }, new SessionDescription(SessionDescription.Type.ANSWER, answerSdp));
                }
            } catch (Exception e) {
                Debug.Warning(e);
                fail("Provisioning request error: " + e.getMessage());
            }
        }

        private void flushIceUpdates() {
            if (closed || viewerSessionNode == null || params.signalingURL == null) {
                return;
            }
            List<IceCandidate> toSend;
            boolean sendCompleted;
            synchronized (pendingCandidates) {
                toSend = new ArrayList<>(pendingCandidates);
                pendingCandidates.clear();
                sendCompleted = iceCompletedPending;
                iceCompletedPending = false;
            }
            if (toSend.isEmpty() && !sendCompleted) {
                return;
            }

            try {
                if (!toSend.isEmpty()) {
                    LLSDArray candidatesArray = new LLSDArray();
                    for (IceCandidate c : toSend) {
                        candidatesArray.add(new LLSDMap(
                                new LLSDMap.LLSDMapEntry("sdpMid", new LLSDString(c.sdpMid != null ? c.sdpMid : "")),
                                new LLSDMap.LLSDMapEntry("sdpMLineIndex", new LLSDInt(c.sdpMLineIndex)),
                                new LLSDMap.LLSDMapEntry("candidate", new LLSDString(c.sdp != null ? c.sdp : ""))
                        ));
                    }
                    LLSDMap body = new LLSDMap(
                            new LLSDMap.LLSDMapEntry("candidates", candidatesArray),
                            new LLSDMap.LLSDMapEntry("viewer_session", viewerSessionNode),
                            new LLSDMap.LLSDMapEntry("voice_server_type", new LLSDString(WEBRTC_VOICE_SERVER_TYPE))
                    );
                    new LLSDXMLRequest().PerformRequest(params.signalingURL, body);
                }

                if (sendCompleted && !closed) {
                    LLSDMap completedMap = new LLSDMap(
                            new LLSDMap.LLSDMapEntry("completed", new LLSDBoolean(true))
                    );
                    LLSDMap body = new LLSDMap(
                            new LLSDMap.LLSDMapEntry("candidate", completedMap),
                            new LLSDMap.LLSDMapEntry("viewer_session", viewerSessionNode),
                            new LLSDMap.LLSDMapEntry("voice_server_type", new LLSDString(WEBRTC_VOICE_SERVER_TYPE))
                    );
                    new LLSDXMLRequest().PerformRequest(params.signalingURL, body);
                }
            } catch (Exception e) {
                Debug.Warning(e);
            }
        }

        private void sendJoin() {
            JSONObject root = new JSONObject();
            JSONObject joinObj = new JSONObject();
            try {
                joinObj.put("p", true);
                root.put("j", joinObj);
                sendDataChannelJson(root.toString());
            } catch (JSONException e) {
                Debug.Warning(e);
            }
        }

        void maybeSendPosition(float spX, float spY, float spZ,
                               float shX, float shY, float shZ, float shW,
                               float lpX, float lpY, float lpZ,
                               float lhX, float lhY, float lhZ, float lhW) {
            long now = System.currentTimeMillis();
            if (now - lastPositionSentMs < POSITION_UPDATE_MIN_INTERVAL_MS) {
                return;
            }
            try {
                JSONObject sp = new JSONObject();
                sp.put("x", (int) (spX * 100.0f));
                sp.put("y", (int) (spY * 100.0f));
                sp.put("z", (int) (spZ * 100.0f));

                JSONObject sh = new JSONObject();
                sh.put("x", (int) (shX * 100.0f));
                sh.put("y", (int) (shY * 100.0f));
                sh.put("z", (int) (shZ * 100.0f));
                sh.put("w", (int) (shW * 100.0f));

                JSONObject lp = new JSONObject();
                lp.put("x", (int) (lpX * 100.0f));
                lp.put("y", (int) (lpY * 100.0f));
                lp.put("z", (int) (lpZ * 100.0f));

                JSONObject lh = new JSONObject();
                lh.put("x", (int) (lhX * 100.0f));
                lh.put("y", (int) (lhY * 100.0f));
                lh.put("z", (int) (lhZ * 100.0f));
                lh.put("w", (int) (lhW * 100.0f));

                JSONObject root = new JSONObject();
                root.put("sp", sp);
                root.put("sh", sh);
                root.put("lp", lp);
                root.put("lh", lh);

                String payload = root.toString();
                if (payload.equals(lastPositionPayload)) {
                    return;
                }
                lastPositionSentMs = now;
                lastPositionPayload = payload;
                sendDataChannelJson(payload);
            } catch (JSONException e) {
                Debug.Warning(e);
            }
        }

        private void sendDataChannelJson(String json) {
            DataChannel dc = this.dataChannel;
            if (closed || dc == null || dc.state() != DataChannel.State.OPEN) {
                return;
            }
            byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
            dc.send(new DataChannel.Buffer(ByteBuffer.wrap(bytes), false));
        }

        private void handleIncomingData(String jsonText) {
            if (closed) {
                return;
            }
            try {
                JSONObject root = new JSONObject(jsonText);
                JSONObject mutePayload = null;

                Iterator<String> keys = root.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    UUID agentId;
                    try {
                        agentId = UUID.fromString(key);
                    } catch (IllegalArgumentException ignored) {
                        continue;
                    }
                    JSONObject obj = root.optJSONObject(key);
                    if (obj == null) {
                        continue;
                    }

                    boolean leaving = obj.optBoolean("l", false);
                    if (leaving) {
                        if (!agentId.equals(params.agentUUID)) {
                            participants.remove(agentId);
                        }
                        continue;
                    }

                    ParticipantState state = participants.get(agentId);
                    if (state == null && obj.has("j")) {
                        state = new ParticipantState();
                        participants.put(agentId, state);
                        if (!agentId.equals(params.agentUUID) && params.muteChecker.isVoiceMuted(agentId)) {
                            if (mutePayload == null) {
                                mutePayload = new JSONObject();
                            }
                            mutePayload.put(key, true);
                        }
                    }

                    if (state != null) {
                        if (obj.has("p")) {
                            state.level = (float) obj.optDouble("p", 0.0) / 128.0f;
                        }
                        if (obj.has("v")) {
                            state.isSpeaking = obj.optBoolean("v", false);
                        }
                        if (obj.has("m")) {
                            state.isModeratorMuted = obj.optBoolean("m", false);
                        }
                    }
                }

                if (mutePayload != null) {
                    JSONObject msg = new JSONObject();
                    msg.put("m", mutePayload);
                    sendDataChannelJson(msg.toString());
                }

                publishState(currentState, currentState);
            } catch (JSONException e) {
                Debug.Warning(e);
            }
        }

        private void publishState(VoiceChatInfo.VoiceChatState nextState, VoiceChatInfo.VoiceChatState prevState) {
            currentState = nextState;
            int speakingCount = 0;
            UUID topSpeaker = null;
            float topLevel = -1.0f;
            for (Map.Entry<UUID, ParticipantState> entry : participants.entrySet()) {
                ParticipantState ps = entry.getValue();
                if (ps.isSpeaking) {
                    speakingCount++;
                    if (ps.level >= topLevel) {
                        topLevel = ps.level;
                        topSpeaker = entry.getKey();
                    }
                }
            }
            VoiceChatInfo info = VoiceChatInfo.create(
                    nextState,
                    prevState,
                    speakingCount,
                    topSpeaker,
                    params.channelInfo.isConference,
                    micEnabled);
            params.listener.onChatStateChanged(params.chatterID, info);
        }

        private void fail(String message) {
            if (closed) {
                return;
            }
            Debug.Printf("WebRTCVoiceClient error: %s", message);
            params.listener.onSessionError(params.chatterID, message);
            close(true);
        }

        void close(boolean notifyNone) {
            if (closed) {
                return;
            }
            closed = true;
            VoiceChatInfo.VoiceChatState prev = currentState;

            if (viewerSessionNode != null && params.provisionURL != null) {
                final LLSDNode sessionNode = viewerSessionNode;
                final String url = params.provisionURL;
                new Thread(() -> {
                    try {
                        LLSDMap logoutBody = new LLSDMap(
                                new LLSDMap.LLSDMapEntry("logout", new LLSDBoolean(true)),
                                new LLSDMap.LLSDMapEntry("viewer_session", sessionNode),
                                new LLSDMap.LLSDMapEntry("voice_server_type", new LLSDString(WEBRTC_VOICE_SERVER_TYPE))
                        );
                        new LLSDXMLRequest().PerformRequest(url, logoutBody);
                    } catch (Exception e) {
                        Debug.Warning(e);
                    }
                }).start();
            }

            if (dataChannel != null) {
                try {
                    dataChannel.unregisterObserver();
                    dataChannel.close();
                    dataChannel.dispose();
                } catch (Exception ignored) {
                }
                dataChannel = null;
            }
            if (peerConnection != null) {
                try {
                    peerConnection.close();
                    peerConnection.dispose();
                } catch (Exception ignored) {
                }
                peerConnection = null;
            }
            if (localAudioTrack != null) {
                try {
                    localAudioTrack.dispose();
                } catch (Exception ignored) {
                }
                localAudioTrack = null;
            }
            if (audioSource != null) {
                try {
                    audioSource.dispose();
                } catch (Exception ignored) {
                }
                audioSource = null;
            }
            participants.clear();

            if (notifyNone) {
                currentState = VoiceChatInfo.VoiceChatState.None;
                params.listener.onChatStateChanged(
                        params.chatterID,
                        VoiceChatInfo.create(
                                VoiceChatInfo.VoiceChatState.None,
                                prev,
                                0,
                                null,
                                params.channelInfo.isConference,
                                false));
            }
        }

        private class PeerConnectionObserver implements PeerConnection.Observer {
            @Override
            public void onSignalingChange(PeerConnection.SignalingState signalingState) {
            }

            @Override
            public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
                Debug.Printf("WebRTCVoiceClient: ICE connection state = %s", iceConnectionState);
                if (iceConnectionState == PeerConnection.IceConnectionState.CONNECTED
                        || iceConnectionState == PeerConnection.IceConnectionState.COMPLETED) {
                    if (currentState != VoiceChatInfo.VoiceChatState.Active) {
                        publishState(VoiceChatInfo.VoiceChatState.Active, currentState);
                    }
                } else if (iceConnectionState == PeerConnection.IceConnectionState.FAILED) {
                    fail("ICE connection failed");
                }
            }

            @Override
            public void onIceConnectionReceivingChange(boolean b) {
            }

            @Override
            public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
                if (iceGatheringState == PeerConnection.IceGatheringState.COMPLETE) {
                    synchronized (pendingCandidates) {
                        iceCompletedPending = true;
                    }
                    executor.execute(() -> flushIceUpdates());
                }
            }

            @Override
            public void onIceCandidate(IceCandidate iceCandidate) {
                if (iceCandidate == null || closed) {
                    return;
                }
                synchronized (pendingCandidates) {
                    pendingCandidates.add(iceCandidate);
                }
                executor.execute(() -> flushIceUpdates());
            }

            @Override
            public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {
            }

            @Override
            public void onAddStream(MediaStream mediaStream) {
            }

            @Override
            public void onRemoveStream(MediaStream mediaStream) {
            }

            @Override
            public void onDataChannel(DataChannel dc) {
                if (closed || dc == null) {
                    return;
                }
                if (DATA_CHANNEL_LABEL.equals(dc.label()) && dataChannel == null) {
                    dataChannel = dc;
                    dc.registerObserver(new DataChannelObserver(dc));
                }
            }

            @Override
            public void onRenegotiationNeeded() {
            }

            @Override
            public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {
            }
        }

        private class DataChannelObserver implements DataChannel.Observer {
            private final DataChannel channel;

            DataChannelObserver(DataChannel channel) {
                this.channel = channel;
            }

            @Override
            public void onBufferedAmountChange(long l) {
            }

            @Override
            public void onStateChange() {
                if (closed) {
                    return;
                }
                DataChannel.State state = channel.state();
                Debug.Printf("WebRTCVoiceClient: SLData state = %s", state);
                if (state == DataChannel.State.OPEN) {
                    sendJoin();
                    publishState(VoiceChatInfo.VoiceChatState.Active, currentState);
                }
            }

            @Override
            public void onMessage(DataChannel.Buffer buffer) {
                if (closed || buffer == null || buffer.binary) {
                    return;
                }
                ByteBuffer data = buffer.data;
                byte[] bytes = new byte[data.remaining()];
                data.get(bytes);
                String text = new String(bytes, StandardCharsets.UTF_8);
                handleIncomingData(text);
            }
        }
    }

    private static class SimpleSdpObserver implements SdpObserver {
        @Override
        public void onCreateSuccess(SessionDescription sessionDescription) {
        }

        @Override
        public void onSetSuccess() {
        }

        @Override
        public void onCreateFailure(String s) {
        }

        @Override
        public void onSetFailure(String s) {
        }
    }
}
