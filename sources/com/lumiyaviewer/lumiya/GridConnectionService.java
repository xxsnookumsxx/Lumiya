package com.lumiyaviewer.lumiya;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import com.google.common.base.Strings;
import com.google.vr.cardboard.TransitionView;
import com.lumiyaviewer.lumiya.GlobalOptions;
import com.lumiyaviewer.lumiya.eventbus.EventBus;
import com.lumiyaviewer.lumiya.eventbus.EventHandler;
import com.lumiyaviewer.lumiya.licensing.LicenseChecker;
import com.lumiyaviewer.lumiya.react.Subscription;
import com.lumiyaviewer.lumiya.react.SubscriptionData;
import com.lumiyaviewer.lumiya.react.SubscriptionSingleKey;
import com.lumiyaviewer.lumiya.react.UIThreadExecutor;
import com.lumiyaviewer.lumiya.slproto.SLAgentCircuit;
import com.lumiyaviewer.lumiya.slproto.SLGridConnection;
import com.lumiyaviewer.lumiya.slproto.auth.SLAuthParams;
import com.lumiyaviewer.lumiya.slproto.avatar.SLMoveEvents;
import com.lumiyaviewer.lumiya.slproto.chat.SLVoiceUpgradeEvent;
import com.lumiyaviewer.lumiya.slproto.chat.generic.SLChatEvent;
import com.lumiyaviewer.lumiya.slproto.events.SLConnectionStateChangedEvent;
import com.lumiyaviewer.lumiya.slproto.events.SLDisconnectEvent;
import com.lumiyaviewer.lumiya.slproto.events.SLLoginResultEvent;
import com.lumiyaviewer.lumiya.slproto.modules.SLModules;
import com.lumiyaviewer.lumiya.slproto.users.ChatterID;
import com.lumiyaviewer.lumiya.slproto.users.ChatterNameRetriever;
import com.lumiyaviewer.lumiya.slproto.users.manager.CurrentLocationInfo;
import com.lumiyaviewer.lumiya.slproto.users.manager.UnreadNotificationInfo;
import com.lumiyaviewer.lumiya.slproto.users.manager.UnreadNotificationManager;
import com.lumiyaviewer.lumiya.slproto.users.manager.UnreadNotifications;
import com.lumiyaviewer.lumiya.slproto.users.manager.UserManager;
import com.lumiyaviewer.lumiya.sync.CloudSyncServiceConnection;
import com.lumiyaviewer.lumiya.ui.chat.ChatFragment;
import com.lumiyaviewer.lumiya.ui.chat.contacts.ChatFragmentActivityFactory;
import com.lumiyaviewer.lumiya.ui.common.ActivityUtils;
import com.lumiyaviewer.lumiya.ui.common.ConnectedActivity;
import com.lumiyaviewer.lumiya.ui.common.MasterDetailsActivity;
import com.lumiyaviewer.lumiya.ui.notify.DummyNotificationChannelManager;
import com.lumiyaviewer.lumiya.ui.notify.NotificationChannels;
import com.lumiyaviewer.lumiya.ui.notify.OnlineNotificationInfo;
import com.lumiyaviewer.lumiya.ui.settings.NotificationSettings;
import com.lumiyaviewer.lumiya.ui.settings.NotificationType;
import com.lumiyaviewer.lumiya.utils.LEDAction;
import com.lumiyaviewer.lumiya.voice.common.messages.VoiceRinging;
import com.lumiyaviewer.lumiya.voice.common.model.VoiceLoginInfo;
import com.lumiyaviewer.lumiya.voiceintf.VoicePluginServiceConnection;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;

/* JADX INFO: loaded from: classes.dex */
public class GridConnectionService extends Service implements SharedPreferences.OnSharedPreferenceChangeListener {

    /* JADX INFO: renamed from: -com-lumiyaviewer-lumiya-ui-settings-NotificationTypeSwitchesValues, reason: not valid java name */
    private static /* synthetic */ int[] f3x3582025 = null;

    /* JADX INFO: renamed from: -com-lumiyaviewer-lumiya-utils-LEDActionSwitchesValues, reason: not valid java name */
    private static /* synthetic */ int[] f4comlumiyaviewerlumiyautilsLEDActionSwitchesValues = null;
    public static final String LOGIN_ACTION = "com.lumiyaviewer.lumiya.ACTION_LOGIN";
    private static final int REQUEST_CODE_UNREAD_NOTIFY = 2131755072;
    private Subscription<Boolean, UnreadNotifications> unreadNotifySubscription;
    private static WeakReference<GridConnectionService> serviceInstance = null;
    private static boolean onlineNotify = false;
    private static boolean soundOnNotify = true;
    private static NotificationSettings notifyLocalChat = new NotificationSettings(NotificationType.LocalChat);
    private static NotificationSettings notifyPrivate = new NotificationSettings(NotificationType.Private);
    private static NotificationSettings notifyGroup = new NotificationSettings(NotificationType.Group);
    private static String gridName = "Second Life";
    private static SLGridConnection gridConnection = null;
    private static Set<Activity> visibleActivities = Collections.synchronizedSet(new HashSet());
    private final EventBus eventBus = EventBus.getInstance();
    private final Handler mHandler = new Handler();
    private SharedPreferences prefs = null;
    private boolean cloudSyncEnabled = false;

    @Nullable
    private UserManager cloudSyncUserManager = null;
    private boolean cloudPluginReceiverRegistered = false;
    private boolean voicePluginReceiverRegistered = false;
    private final SubscriptionData<SubscriptionSingleKey, CurrentLocationInfo> currentLocationInfo = new SubscriptionData<>(UIThreadExecutor.getInstance(), new Subscription.OnData() { // from class: com.lumiyaviewer.lumiya.-$Lambda$3DowF6pLKgVjVrTY9aZKQ2J3cf0
        private final /* synthetic */ void $m$0(Object obj) {
            GridConnectionService.this.m17com_lumiyaviewer_lumiya_GridConnectionServicemthref0((CurrentLocationInfo) obj);
        }

        @Override // com.lumiyaviewer.lumiya.react.Subscription.OnData
        public final void onData(Object obj) {
            $m$0(obj);
        }
    });
    private ChatterNameRetriever connectedAgentNameRetriever = null;
    private final Set<Integer> shownNotificationIds = Collections.newSetFromMap(Collections.synchronizedMap(new HashMap()));
    private OnlineNotificationInfo onlineNotificationInfo = new OnlineNotificationInfo(onlineNotify, this, gridName, gridConnection, this.connectedAgentNameRetriever, null);
    private WifiManager.WifiLock wifiLock = null;
    private final IBinder mBinder = new GridServiceBinder();
    private Handler licenseCheckHandler = new Handler() { // from class: com.lumiyaviewer.lumiya.GridConnectionService.1
        @Override // android.os.Handler
        public void handleMessage(Message message) {
            switch (message.what) {
                case R.id.msg_licensing_allow /* 2131755033 */:
                    Debug.Printf("License: License check ok.", new Object[0]);
                    if (message.obj instanceof SLAuthParams) {
                        GridConnectionService.this.performLogin((SLAuthParams) message.obj);
                    }
                    break;
                case R.id.msg_licensing_app_error /* 2131755034 */:
                    String str = message.obj instanceof String ? (String) message.obj : "Internal application error";
                    Debug.Printf("License: License check app error: %s", str);
                    GridConnectionService.this.eventBus.publish(new SLLoginResultEvent(false, "License check failed: " + str + ".", null));
                    break;
                case R.id.msg_licensing_dont_allow /* 2131755035 */:
                    Debug.Printf("License: License check failed.", new Object[0]);
                    GridConnectionService.this.eventBus.publish(new SLLoginResultEvent(false, "You don't have valid license to use this application.", null));
                    break;
            }
        }
    };
    private final BroadcastReceiver cloudPluginInstalledReceiver = new BroadcastReceiver() { // from class: com.lumiyaviewer.lumiya.GridConnectionService.2
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            UUID activeAgentUUID;
            UserManager userManager;
            if (!Strings.nullToEmpty(intent.getData().getSchemeSpecificPart()).equals("com.lumiyaviewer.lumiya.cloud") || GridConnectionService.gridConnection == null || GridConnectionService.gridConnection.getConnectionState() != SLGridConnection.ConnectionState.Connected || (activeAgentUUID = GridConnectionService.gridConnection.getActiveAgentUUID()) == null || (userManager = UserManager.getUserManager(activeAgentUUID)) == null) {
                return;
            }
            GridConnectionService.this.startCloudSync(userManager);
        }
    };
    private final BroadcastReceiver voicePluginInstalledReceiver = new BroadcastReceiver() { // from class: com.lumiyaviewer.lumiya.GridConnectionService.3
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            UUID activeAgentUUID;
            UserManager userManager;
            SLAgentCircuit activeAgentCircuit;
            SLModules modules;
            if (!Strings.nullToEmpty(intent.getData().getSchemeSpecificPart()).equals("com.lumiyaviewer.lumiya.voice") || GridConnectionService.gridConnection == null || GridConnectionService.gridConnection.getConnectionState() != SLGridConnection.ConnectionState.Connected || (activeAgentUUID = GridConnectionService.gridConnection.getActiveAgentUUID()) == null || (userManager = UserManager.getUserManager(activeAgentUUID)) == null || (activeAgentCircuit = userManager.getActiveAgentCircuit()) == null || (modules = activeAgentCircuit.getModules()) == null) {
                return;
            }
            modules.voice.updateVoiceEnabledStatus();
        }
    };
    private CloudSyncServiceConnection cloudSyncServiceConnection = null;
    private VoicePluginServiceConnection voicePluginServiceConnection = null;
    private final ChatterNameRetriever.OnChatterNameUpdated onActiveAgentNameUpdated = new ChatterNameRetriever.OnChatterNameUpdated() { // from class: com.lumiyaviewer.lumiya.-$Lambda$3DowF6pLKgVjVrTY9aZKQ2J3cf0.2
        private final /* synthetic */ void $m$0(ChatterNameRetriever chatterNameRetriever) {
            GridConnectionService.this.m19lambda$com_lumiyaviewer_lumiya_GridConnectionService_20777(chatterNameRetriever);
        }

        @Override // com.lumiyaviewer.lumiya.slproto.users.ChatterNameRetriever.OnChatterNameUpdated
        public final void onChatterNameUpdated(ChatterNameRetriever chatterNameRetriever) {
            $m$0(chatterNameRetriever);
        }
    };

    public class GridServiceBinder extends Binder {
        public GridServiceBinder() {
        }

        public EventBus getEventBus() {
            return GridConnectionService.this.eventBus;
        }

        public SLGridConnection getGridConn() {
            return GridConnectionService.gridConnection;
        }
    }

    /* JADX INFO: renamed from: -getcom-lumiyaviewer-lumiya-ui-settings-NotificationTypeSwitchesValues, reason: not valid java name */
    private static /* synthetic */ int[] m13xc4a7bd01() {
        if (f3x3582025 != null) {
            return f3x3582025;
        }
        int[] iArr = new int[NotificationType.valuesCustom().length];
        try {
            iArr[NotificationType.Group.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[NotificationType.LocalChat.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[NotificationType.Private.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        f3x3582025 = iArr;
        return iArr;
    }

    /* JADX INFO: renamed from: -getcom-lumiyaviewer-lumiya-utils-LEDActionSwitchesValues, reason: not valid java name */
    private static /* synthetic */ int[] m14getcomlumiyaviewerlumiyautilsLEDActionSwitchesValues() {
        if (f4comlumiyaviewerlumiyautilsLEDActionSwitchesValues != null) {
            return f4comlumiyaviewerlumiyautilsLEDActionSwitchesValues;
        }
        int[] iArr = new int[LEDAction.valuesCustom().length];
        try {
            iArr[LEDAction.Always.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[LEDAction.Fast.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[LEDAction.None.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[LEDAction.Slow.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        f4comlumiyaviewerlumiyautilsLEDActionSwitchesValues = iArr;
        return iArr;
    }

    public GridConnectionService() {
        getGridConnection();
        this.eventBus.subscribe(this, null, this.mHandler);
    }

    private void connectToVoicePlugin(VoiceLoginInfo voiceLoginInfo, UserManager userManager) {
        boolean zBindService;
        if (this.voicePluginServiceConnection == null) {
            this.voicePluginServiceConnection = new VoicePluginServiceConnection(this);
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.lumiyaviewer.lumiya.voice", "com.lumiyaviewer.lumiya.voice.VoiceService"));
            try {
                zBindService = bindService(intent, this.voicePluginServiceConnection, 1);
            } catch (SecurityException e) {
                Debug.Warning(e);
                zBindService = false;
            }
            Debug.Printf("LumiyaVoice: bindService = %b", Boolean.valueOf(zBindService));
            if (!zBindService) {
                this.voicePluginServiceConnection = null;
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction("android.intent.action.PACKAGE_ADDED");
                intentFilter.addDataScheme("package");
                if (Build.VERSION.SDK_INT >= 19) {
                    intentFilter.addDataSchemeSpecificPart("com.lumiyaviewer.lumiya.voice", 0);
                }
                registerReceiver(this.voicePluginInstalledReceiver, intentFilter);
                this.voicePluginReceiverRegistered = true;
                if (userManager != null && VoicePluginServiceConnection.shouldDisplayInstallOffer()) {
                    userManager.getChatterList().getActiveChattersManager().HandleChatEvent(ChatterID.getLocalChatterID(userManager.getUserID()), new SLVoiceUpgradeEvent(userManager.getUserID(), LumiyaApp.getContext().getString(R.string.plugin_install_for_voice_needed), true, LicenseChecker.VOICE_PLUGIN_URL), false);
                }
            }
        }
        if (this.voicePluginServiceConnection != null) {
            this.voicePluginServiceConnection.setVoiceLoginInfo(voiceLoginInfo, userManager);
        }
    }

    public static SLGridConnection getGridConnection() {
        if (gridConnection == null) {
            gridConnection = new SLGridConnection();
        }
        return gridConnection;
    }

    @Nullable
    public static GridConnectionService getServiceInstance() {
        if (serviceInstance != null) {
            return serviceInstance.get();
        }
        return null;
    }

    private void handleStartService(Intent intent) {
        Object[] objArr = new Object[1];
        objArr[0] = intent != null ? "not null" : "null";
        Debug.Printf("GridConnectionService: service is now started, intent is %s", objArr);
        updateOnlineNotification();
        if (intent != null) {
            String strNullToEmpty = Strings.nullToEmpty(intent.getAction());
            if (strNullToEmpty.equals(LOGIN_ACTION)) {
                new LicenseChecker(getApplicationContext(), this.licenseCheckHandler, new SLAuthParams(intent));
                return;
            }
            if (strNullToEmpty.equals(VoicePluginServiceConnection.ACTION_VOICE_REJECT)) {
                if (this.voicePluginServiceConnection != null) {
                    this.voicePluginServiceConnection.rejectCall(intent);
                }
            } else {
                if (!strNullToEmpty.equals(VoicePluginServiceConnection.ACTION_VOICE_ACCEPT) || this.voicePluginServiceConnection == null) {
                    return;
                }
                this.voicePluginServiceConnection.acceptCall(intent);
            }
        }
    }

    public static boolean hasVisibleActivities() {
        return !visibleActivities.isEmpty();
    }

    private void hideUnreadNotificationSingle(int i) {
        if (this.shownNotificationIds.contains(Integer.valueOf(i))) {
            this.shownNotificationIds.remove(Integer.valueOf(i));
            ((NotificationManager) getSystemService("notification")).cancel(i);
        }
    }

    private static NotificationSettings notifySettingsByType(NotificationType notificationType) {
        switch (m13xc4a7bd01()[notificationType.ordinal()]) {
        }
        return notifyLocalChat;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX INFO: renamed from: onCurrentLocationInfo, reason: merged with bridge method [inline-methods] */
    public void m17com_lumiyaviewer_lumiya_GridConnectionServicemthref0(CurrentLocationInfo currentLocationInfo) {
        updateOnlineNotification();
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX INFO: renamed from: onUnreadNotification, reason: merged with bridge method [inline-methods] */
    public void m18com_lumiyaviewer_lumiya_GridConnectionServicemthref1(UnreadNotifications unreadNotifications) {
        showUnreadNotification(unreadNotifications);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void performLogin(SLAuthParams sLAuthParams) {
        gridName = sLAuthParams.gridName;
        gridConnection.Connect(sLAuthParams);
    }

    private void readPreferences(SharedPreferences sharedPreferences) {
        onlineNotify = sharedPreferences.getBoolean("onlineNotify", true);
        soundOnNotify = sharedPreferences.getBoolean("soundOnNotify", true);
        this.cloudSyncEnabled = sharedPreferences.getBoolean("sync_to_gdrive", false);
        notifyLocalChat.Load(sharedPreferences);
        notifyPrivate.Load(sharedPreferences);
        notifyGroup.Load(sharedPreferences);
        SLGridConnection.setAutoresponseInfo(sharedPreferences.getBoolean("autoresponse", false), sharedPreferences.getString("autoresponseText", "(Autoresponse) I have auto-response feature enabled. I will respond shortly."));
        Debug.Log("GridConnectionService: prefs: onlineNotify = " + onlineNotify);
        Debug.Log("GridConnectionService: prefs: soundOnNotify = " + soundOnNotify);
        if (gridConnection != null) {
            try {
                gridConnection.getModules().HandleGlobalOptionsChange();
            } catch (SLGridConnection.NotConnectedException e) {
            }
        }
        updateOnlineNotification();
        updateCloudSyncStatus();
    }

    public static void setActivityVisible(Activity activity, boolean z) {
        if (z) {
            visibleActivities.add(activity);
        } else {
            visibleActivities.remove(activity);
        }
        if (visibleActivities.isEmpty() || gridConnection == null) {
            return;
        }
        try {
            if (gridConnection.getConnectionState() == SLGridConnection.ConnectionState.Connected) {
                gridConnection.getAgentCircuit().UnpauseAgent();
            }
        } catch (SLGridConnection.NotConnectedException e) {
            e.printStackTrace();
        }
    }

    private void showUnreadNotification(UnreadNotifications unreadNotifications) {
        NotificationChannels notificationChannels = NotificationChannels.getInstance();
        if (!notificationChannels.useNotificationGroups()) {
            showUnreadNotificationSingle(unreadNotifications.merge(), R.id.unread_notify_id, DummyNotificationChannelManager.DEFAULT_NOTIFICATION_CHANNEL, null, true, null);
            return;
        }
        if (notificationChannels.areNotificationsSystemControlled()) {
            unreadNotifications = unreadNotifications.filter(notificationChannels.getEnabledTypes(this));
        }
        if (unreadNotifications.notificationGroups().isEmpty()) {
            if (this.shownNotificationIds.isEmpty()) {
                return;
            }
            NotificationManager notificationManager = (NotificationManager) getSystemService("notification");
            Iterator it = this.shownNotificationIds.iterator();
            while (it.hasNext()) {
                notificationManager.cancel(((Integer) it.next()).intValue());
            }
            this.shownNotificationIds.clear();
            return;
        }
        UnreadNotificationInfo unreadNotificationInfoMerge = unreadNotifications.merge();
        showUnreadNotificationSingle(unreadNotificationInfoMerge, R.id.unread_notify_id, notificationChannels.getChannelName(notificationChannels.getChannelByType(unreadNotificationInfoMerge.mostImportantFreshType().or(unreadNotificationInfoMerge.mostImportantType().or(NotificationType.LocalChat)))), NotificationChannels.MESSAGE_NOTIFICATION_GROUP, true, null);
        for (NotificationType notificationType : NotificationType.VALUES) {
            NotificationChannels.Channel channelByType = notificationChannels.getChannelByType(notificationType);
            if (channelByType != null) {
                showUnreadNotificationSingle(unreadNotifications.notificationGroups().get(notificationType), channelByType.notificationId, notificationChannels.getChannelName(channelByType), NotificationChannels.MESSAGE_NOTIFICATION_GROUP, false, Integer.toString(9 - notificationType.getPriority()));
            }
        }
    }

    private void showUnreadNotificationSingle(UnreadNotificationInfo unreadNotificationInfo, int i, @Nonnull String str, @Nullable String str2, boolean z, @Nullable String str3) {
        UnreadNotificationInfo.UnreadMessageSource unreadMessageSource;
        UnreadNotificationInfo.UnreadMessageSource unreadMessageSource2;
        boolean z2;
        String strOrNull;
        Intent intentCaptureNotify;
        NotificationManager notificationManager = (NotificationManager) getSystemService("notification");
        boolean z3 = false;
        if (unreadNotificationInfo != null && (unreadNotificationInfo.totalUnreadCount() != 0 || unreadNotificationInfo.objectPopupInfo().objectPopupsCount() != 0)) {
            z3 = true;
        }
        if (!z3) {
            hideUnreadNotificationSingle(i);
            return;
        }
        Debug.Log("GridConnectionService: updateUnreadNotification: notification state changed.");
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, str);
        builder.setSmallIcon(R.drawable.ic_unread_icon);
        if (str2 != null) {
            builder.setGroup(str2);
            builder.setGroupSummary(z);
            builder.setGroupAlertBehavior(2);
            if (str3 != null) {
                builder.setSortKey(str3);
            }
        }
        UserManager userManager = UserManager.getUserManager(unreadNotificationInfo.agentUUID());
        UnreadNotificationInfo.UnreadMessageSource unreadMessageSource3 = unreadNotificationInfo.unreadSources().size() == 1 ? unreadNotificationInfo.unreadSources().get(0) : null;
        Intent intentCreateIntent = ChatFragmentActivityFactory.getInstance().createIntent(this, unreadMessageSource3 != null ? ChatFragment.makeSelection(unreadMessageSource3.chatterID()) : null);
        intentCreateIntent.addFlags(536870912);
        ActivityUtils.setActiveAgentID(intentCreateIntent, unreadNotificationInfo.agentUUID());
        UnreadNotificationInfo.UnreadMessageSource next = null;
        if (unreadNotificationInfo.singleFreshSource().isPresent()) {
            unreadMessageSource2 = unreadNotificationInfo.singleFreshSource().get();
        } else {
            Iterator<UnreadNotificationInfo.UnreadMessageSource> it = unreadNotificationInfo.unreadSources().iterator();
            while (true) {
                unreadMessageSource = next;
                if (!it.hasNext()) {
                    break;
                }
                next = it.next();
                if (unreadMessageSource != null && next.unreadMessagesCount() <= unreadMessageSource.unreadMessagesCount()) {
                    next = unreadMessageSource;
                }
            }
            unreadMessageSource2 = unreadMessageSource;
        }
        if (unreadMessageSource2 != null) {
            intentCreateIntent.putExtra(MasterDetailsActivity.WEAK_SELECTION_KEY, ChatFragment.makeSelection(unreadMessageSource2.chatterID()));
        }
        Intent intent = (userManager == null || (intentCaptureNotify = userManager.getUnreadNotificationManager().captureNotify(unreadNotificationInfo, intentCreateIntent)) == null) ? intentCreateIntent : intentCaptureNotify;
        if (unreadMessageSource3 != null && unreadNotificationInfo.objectPopupInfo().objectPopupsCount() == 0) {
            String strOr = unreadMessageSource3.chatterName().or(gridName);
            builder.setContentTitle(strOr);
            if (unreadMessageSource3.unreadMessagesCount() == 1 && unreadMessageSource3.unreadMessages().size() == 1) {
                builder.setContentText(unreadMessageSource3.unreadMessages().get(0).getPlainTextMessage(this, unreadMessageSource3.chatterID().getUserManager(), true));
            } else {
                builder.setContentText(String.format(getString(R.string.unread_messages), Integer.valueOf(unreadMessageSource3.unreadMessagesCount())));
            }
            if (unreadMessageSource3.unreadMessages().size() > 1) {
                NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
                Iterator<SLChatEvent> it2 = unreadMessageSource3.unreadMessages().iterator();
                while (it2.hasNext()) {
                    inboxStyle.addLine(it2.next().getPlainTextMessage(this, unreadMessageSource3.chatterID().getUserManager(), unreadMessageSource3.chatterID().getChatterType() == ChatterID.ChatterType.User, "  ", " "));
                }
                inboxStyle.setBigContentTitle(strOr);
                inboxStyle.setSummaryText(String.format(getString(R.string.unread_messages), Integer.valueOf(unreadNotificationInfo.totalUnreadCount())));
                builder.setStyle(inboxStyle);
            }
            z2 = z3;
        } else if (unreadNotificationInfo.totalUnreadCount() != 0) {
            String quantityString = getResources().getQuantityString(R.plurals.new_messages, unreadNotificationInfo.totalUnreadCount(), Integer.valueOf(unreadNotificationInfo.totalUnreadCount()));
            if (unreadNotificationInfo.objectPopupInfo().objectPopupsCount() != 0) {
                quantityString = quantityString + getResources().getQuantityString(R.plurals.and_questions, unreadNotificationInfo.objectPopupInfo().objectPopupsCount(), Integer.valueOf(unreadNotificationInfo.objectPopupInfo().objectPopupsCount()));
            }
            builder.setContentTitle(quantityString);
            if (unreadNotificationInfo.totalUnreadCount() != 0 || unreadNotificationInfo.objectPopupInfo().objectPopupsCount() == 0) {
                builder.setContentTitle(getResources().getQuantityString(R.plurals.new_messages, unreadNotificationInfo.totalUnreadCount(), Integer.valueOf(unreadNotificationInfo.totalUnreadCount())));
            } else {
                builder.setContentTitle(getResources().getQuantityString(R.plurals.new_questions, unreadNotificationInfo.objectPopupInfo().objectPopupsCount(), Integer.valueOf(unreadNotificationInfo.objectPopupInfo().objectPopupsCount())));
            }
            Iterator<UnreadNotificationInfo.UnreadMessageSource> it3 = unreadNotificationInfo.unreadSources().iterator();
            while (true) {
                if (!it3.hasNext()) {
                    strOrNull = null;
                    break;
                }
                UnreadNotificationInfo.UnreadMessageSource next2 = it3.next();
                if (next2.chatterName().isPresent()) {
                    strOrNull = next2.chatterName().orNull();
                    break;
                }
            }
            int size = unreadNotificationInfo.unreadSources().size();
            String str4 = strOrNull != null ? size > 1 ? strOrNull + " " + String.format(getString(R.string.other_chatters), Integer.valueOf(size - 1)) : strOrNull : String.format(getString(R.string.chat_sessions), Integer.valueOf(size));
            builder.setContentText(str4);
            NotificationCompat.InboxStyle inboxStyle2 = new NotificationCompat.InboxStyle();
            for (UnreadNotificationInfo.UnreadMessageSource unreadMessageSource4 : unreadNotificationInfo.unreadSources()) {
                Iterator<SLChatEvent> it4 = unreadMessageSource4.unreadMessages().iterator();
                while (it4.hasNext()) {
                    inboxStyle2.addLine(it4.next().getPlainTextMessage(this, unreadMessageSource4.chatterID().getUserManager(), false, "  ", " "));
                }
            }
            inboxStyle2.setBigContentTitle(String.format(getString(R.string.unread_messages), Integer.valueOf(unreadNotificationInfo.totalUnreadCount())));
            inboxStyle2.setSummaryText(str4);
            builder.setStyle(inboxStyle2);
            z2 = z3;
        } else if (unreadNotificationInfo.objectPopupInfo().objectPopupsCount() != 0) {
            intent.putExtra(ConnectedActivity.OBJECT_POPUP_NOTIFICATION, true);
            if (intent.getBundleExtra(MasterDetailsActivity.INTENT_SELECTION_KEY) == null && intent.getBundleExtra(MasterDetailsActivity.WEAK_SELECTION_KEY) == null) {
                intent.putExtra(MasterDetailsActivity.INTENT_SELECTION_KEY, ChatFragment.makeSelection(ChatterID.getLocalChatterID(unreadNotificationInfo.agentUUID())));
            }
            UnreadNotificationInfo.ObjectPopupMessage objectPopupMessageOrNull = unreadNotificationInfo.objectPopupInfo().lastObjectPopup().orNull();
            if (unreadNotificationInfo.objectPopupInfo().freshObjectPopupsCount() != 1 || objectPopupMessageOrNull == null) {
                builder.setContentTitle(getResources().getQuantityString(R.plurals.new_questions, unreadNotificationInfo.objectPopupInfo().objectPopupsCount(), Integer.valueOf(unreadNotificationInfo.objectPopupInfo().objectPopupsCount())));
                if (objectPopupMessageOrNull != null) {
                    builder.setContentText(objectPopupMessageOrNull.objectName() + ": " + objectPopupMessageOrNull.message());
                    z2 = z3;
                } else {
                    builder.setContentText(getResources().getQuantityString(R.plurals.new_questions, unreadNotificationInfo.objectPopupInfo().objectPopupsCount(), Integer.valueOf(unreadNotificationInfo.objectPopupInfo().objectPopupsCount())));
                    z2 = z3;
                }
            } else {
                builder.setContentTitle(objectPopupMessageOrNull.objectName());
                builder.setContentText(objectPopupMessageOrNull.message());
                z2 = z3;
            }
        } else {
            z2 = false;
        }
        if (!z2) {
            hideUnreadNotificationSingle(i);
            return;
        }
        String quantityString2 = null;
        if (unreadNotificationInfo.freshMessagesCount() > 0) {
            UnreadNotificationInfo.UnreadMessageSource unreadMessageSourceOrNull = unreadNotificationInfo.singleFreshSource().orNull();
            if (unreadMessageSourceOrNull == null) {
                quantityString2 = String.format(getString(R.string.unread_messages), Integer.valueOf(unreadNotificationInfo.freshMessagesCount()));
            } else if (unreadNotificationInfo.freshMessagesCount() != 1 || unreadMessageSourceOrNull.unreadMessages().size() <= 0) {
                String str5 = String.format(getString(R.string.unread_messages), Integer.valueOf(unreadNotificationInfo.freshMessagesCount()));
                String string = unreadMessageSourceOrNull.chatterID().getChatterType() == ChatterID.ChatterType.Local ? getString(R.string.messages_in_where, new Object[]{unreadMessageSourceOrNull.chatterName().or(getString(R.string.local_chat_title))}) : unreadMessageSourceOrNull.chatterID().getChatterType() == ChatterID.ChatterType.Group ? getString(R.string.messages_in_where, new Object[]{unreadMessageSourceOrNull.chatterName().or(getString(R.string.default_group_chat_title))}) : unreadMessageSourceOrNull.chatterName().isPresent() ? String.format(getString(R.string.from_chatter), unreadMessageSourceOrNull.chatterName().orNull()) : null;
                quantityString2 = string != null ? str5 + " " + string : str5;
            } else {
                quantityString2 = "[" + (unreadMessageSourceOrNull.chatterID().getChatterType() == ChatterID.ChatterType.Local ? getString(R.string.chat_type_ticker_local) : unreadMessageSourceOrNull.chatterID().getChatterType() == ChatterID.ChatterType.Group ? getString(R.string.chat_type_ticker_group) : getString(R.string.chat_type_ticker_im)) + "] " + unreadMessageSourceOrNull.unreadMessages().get(unreadMessageSourceOrNull.unreadMessages().size() - 1).getPlainTextMessage(this, unreadMessageSourceOrNull.chatterID().getUserManager(), false).toString().trim();
                int iIndexOf = quantityString2.indexOf("\n");
                if (iIndexOf >= 0) {
                    quantityString2 = quantityString2.substring(0, iIndexOf);
                }
                if (quantityString2.length() > 30) {
                    quantityString2 = quantityString2.substring(0, 30) + "...";
                }
            }
        } else if (unreadNotificationInfo.objectPopupInfo().freshObjectPopupsCount() != 0) {
            UnreadNotificationInfo.ObjectPopupMessage objectPopupMessageOrNull2 = unreadNotificationInfo.objectPopupInfo().lastObjectPopup().orNull();
            if (unreadNotificationInfo.objectPopupInfo().freshObjectPopupsCount() != 1 || objectPopupMessageOrNull2 == null) {
                quantityString2 = getResources().getQuantityString(R.plurals.new_questions, unreadNotificationInfo.objectPopupInfo().objectPopupsCount(), Integer.valueOf(unreadNotificationInfo.objectPopupInfo().objectPopupsCount()));
            } else {
                quantityString2 = objectPopupMessageOrNull2.objectName() + ": " + objectPopupMessageOrNull2.message();
                int iIndexOf2 = quantityString2.indexOf("\n");
                if (iIndexOf2 >= 0) {
                    quantityString2 = quantityString2.substring(0, iIndexOf2);
                }
                if (quantityString2.length() > 30) {
                    quantityString2 = quantityString2.substring(0, 30) + "...";
                }
            }
        }
        builder.setOngoing(false);
        builder.setNumber(unreadNotificationInfo.totalUnreadCount());
        builder.setContentIntent(PendingIntent.getActivity(this, R.id.unread_notify_request_code, intent, SLMoveEvents.AGENT_CONTROL_AWAY));
        builder.setDefaults(0);
        if (quantityString2 != null) {
            builder.setTicker(quantityString2);
        }
        builder.setOnlyAlertOnce(true);
        if (str2 == null || (!z)) {
            LEDAction lEDAction = LEDAction.None;
            int lEDColor = 0;
            NotificationType notificationTypeOrNull = unreadNotificationInfo.totalUnreadCount() > 0 ? unreadNotificationInfo.mostImportantType().orNull() : null;
            if (notificationTypeOrNull == null && unreadNotificationInfo.objectPopupInfo().objectPopupsCount() != 0) {
                notificationTypeOrNull = NotificationType.LocalChat;
            }
            if (notificationTypeOrNull != null) {
                NotificationSettings notificationSettingsNotifySettingsByType = notifySettingsByType(notificationTypeOrNull);
                lEDAction = notificationSettingsNotifySettingsByType.getLEDAction();
                lEDColor = notificationSettingsNotifySettingsByType.getLEDColor();
            }
            NotificationType notificationTypeOrNull2 = unreadNotificationInfo.freshMessagesCount() != 0 ? unreadNotificationInfo.mostImportantFreshType().orNull() : null;
            if (notificationTypeOrNull2 == null && unreadNotificationInfo.objectPopupInfo().freshObjectPopupsCount() != 0) {
                notificationTypeOrNull2 = NotificationType.LocalChat;
            }
            NotificationSettings notificationSettingsNotifySettingsByType2 = notificationTypeOrNull2 != null ? notifySettingsByType(notificationTypeOrNull2) : null;
            Debug.Printf("GridConnectionService: updateUnreadNotification: ledAction = %s, color = %08x", lEDAction.toString(), Integer.valueOf(lEDColor));
            if (lEDAction != LEDAction.None) {
                switch (m14getcomlumiyaviewerlumiyautilsLEDActionSwitchesValues()[lEDAction.ordinal()]) {
                    case 1:
                        builder.setLights(lEDColor, 1, 0);
                        break;
                    case 2:
                        builder.setLights(lEDColor, 300, 100);
                        break;
                    case 3:
                        builder.setLights(lEDColor, 0, 0);
                        break;
                    case 4:
                        builder.setLights(lEDColor, 1000, TransitionView.TRANSITION_ANIMATION_DURATION_MS);
                        break;
                }
            }
            if (!soundOnNotify || notificationSettingsNotifySettingsByType2 == null) {
                Debug.Printf("GridConnectionService: will not emit sound.", new Object[0]);
            } else {
                String ringtone = notificationSettingsNotifySettingsByType2.getRingtone();
                if (ringtone != null) {
                    builder.setSound(Uri.parse(ringtone));
                    builder.setOnlyAlertOnce(false);
                }
            }
        }
        this.shownNotificationIds.add(Integer.valueOf(i));
        notificationManager.notify(i, builder.build());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void startCloudSync(UserManager userManager) {
        this.cloudSyncUserManager = userManager;
        updateCloudSyncStatus();
    }

    private void stopCloudSync() {
        this.cloudSyncUserManager = null;
        updateCloudSyncStatus();
    }

    private void updateCloudSyncStatus() {
        boolean zBindService;
        if (!this.cloudSyncEnabled || this.cloudSyncUserManager == null) {
            if (this.cloudSyncServiceConnection != null) {
                this.cloudSyncServiceConnection.stopSyncing();
                this.cloudSyncServiceConnection = null;
            }
            if (this.cloudPluginReceiverRegistered) {
                try {
                    unregisterReceiver(this.cloudPluginInstalledReceiver);
                } catch (IllegalArgumentException e) {
                    Debug.Warning(e);
                }
            }
            this.cloudPluginReceiverRegistered = false;
            return;
        }
        if (this.cloudSyncServiceConnection == null) {
            this.cloudSyncServiceConnection = new CloudSyncServiceConnection(this, this.cloudSyncUserManager);
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.lumiyaviewer.lumiya.cloud", "com.lumiyaviewer.lumiya.cloud.DriveSyncService"));
            try {
                zBindService = bindService(intent, this.cloudSyncServiceConnection, 1);
            } catch (SecurityException e2) {
                Debug.Warning(e2);
                zBindService = false;
            }
            Debug.Printf("LumiyaCloud: bindService = %b", Boolean.valueOf(zBindService));
            if (zBindService) {
                return;
            }
            this.cloudSyncServiceConnection.stopSyncing();
            Intent intent2 = new Intent("android.intent.action.VIEW");
            intent2.setData(Uri.parse(LicenseChecker.CLOUD_PLUGIN_URL));
            this.cloudSyncServiceConnection.showSyncingError(getString(R.string.cloud_sync_not_installed), getString(R.string.cloud_sync_not_installed_long), intent2);
            this.cloudSyncServiceConnection = null;
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.intent.action.PACKAGE_ADDED");
            intentFilter.addDataScheme("package");
            if (Build.VERSION.SDK_INT >= 19) {
                intentFilter.addDataSchemeSpecificPart("com.lumiyaviewer.lumiya.cloud", 0);
            }
            registerReceiver(this.cloudPluginInstalledReceiver, intentFilter);
            this.cloudPluginReceiverRegistered = true;
        }
    }

    private void updateOnlineNotification() {
        boolean z;
        if (gridConnection != null) {
            SLGridConnection.ConnectionState connectionState = gridConnection.getConnectionState();
            z = connectionState != SLGridConnection.ConnectionState.Idle && GlobalOptions.getInstance().getKeepWifiOn();
            UUID activeAgentUUID = gridConnection.getActiveAgentUUID();
            if (activeAgentUUID == null || connectionState == SLGridConnection.ConnectionState.Idle) {
                if (this.connectedAgentNameRetriever != null) {
                    this.connectedAgentNameRetriever.dispose();
                }
                this.connectedAgentNameRetriever = null;
            } else if (this.connectedAgentNameRetriever == null) {
                this.connectedAgentNameRetriever = new ChatterNameRetriever(ChatterID.getUserChatterID(activeAgentUUID, activeAgentUUID), this.onActiveAgentNameUpdated, UIThreadExecutor.getSerialInstance());
            }
        } else {
            z = false;
        }
        if (z && this.wifiLock == null) {
            Context applicationContext = getApplicationContext();
            if (applicationContext != null) {
                this.wifiLock = ((WifiManager) applicationContext.getSystemService("wifi")).createWifiLock(1, "Lumiya");
                this.wifiLock.acquire();
                Debug.Printf("WiFi lock acquired", new Object[0]);
            }
        } else if (this.wifiLock != null && (!z)) {
            this.wifiLock.release();
            this.wifiLock = null;
            Debug.Printf("WiFi lock released", new Object[0]);
        }
        OnlineNotificationInfo onlineNotificationInfo = new OnlineNotificationInfo(onlineNotify, this, gridName, gridConnection, this.connectedAgentNameRetriever, this.currentLocationInfo.getData());
        if (onlineNotificationInfo.equals(this.onlineNotificationInfo)) {
            return;
        }
        this.onlineNotificationInfo = onlineNotificationInfo;
        Notification notification = onlineNotificationInfo.getNotification(this);
        if (notification != null) {
            startForeground(R.id.online_notify_id, notification);
        } else {
            stopForeground(true);
        }
    }

    public void acceptVoiceCall(ChatterID chatterID) {
        if (this.voicePluginServiceConnection != null) {
            this.voicePluginServiceConnection.acceptVoiceCall(chatterID);
        }
    }

    public void acceptVoiceCall(VoiceRinging voiceRinging) {
        if (this.voicePluginServiceConnection != null) {
            this.voicePluginServiceConnection.acceptVoiceCall(voiceRinging);
        }
    }

    public void enableVoiceMic(boolean z) {
        if (this.voicePluginServiceConnection != null) {
            this.voicePluginServiceConnection.enableVoiceMic(z);
        }
    }

    @javax.annotation.Nullable
    public VoicePluginServiceConnection getVoicePluginServiceConnection() {
        return this.voicePluginServiceConnection;
    }

    @EventHandler
    public void handleConnectEvent(SLLoginResultEvent sLLoginResultEvent) {
        UserManager userManager = UserManager.getUserManager(sLLoginResultEvent.activeAgentUUID);
        if (userManager != null) {
            this.unreadNotifySubscription = userManager.getUnreadNotificationManager().getUnreadNotifications().subscribe(UnreadNotificationManager.unreadNotificationKey, UIThreadExecutor.getSerialInstance(), new Subscription.OnData() { // from class: com.lumiyaviewer.lumiya.-$Lambda$3DowF6pLKgVjVrTY9aZKQ2J3cf0.1
                private final /* synthetic */ void $m$0(Object obj) {
                    GridConnectionService.this.m18com_lumiyaviewer_lumiya_GridConnectionServicemthref1((UnreadNotifications) obj);
                }

                @Override // com.lumiyaviewer.lumiya.react.Subscription.OnData
                public final void onData(Object obj) {
                    $m$0(obj);
                }
            });
            this.currentLocationInfo.subscribe(userManager.getCurrentLocationInfo(), SubscriptionSingleKey.Value);
        }
        updateOnlineNotification();
        VoicePluginServiceConnection.setInstallOfferDisplayed(false);
        if (sLLoginResultEvent.success) {
            startCloudSync(userManager);
            return;
        }
        Debug.Log("GridConnectionService: stopping self because of connect failed.");
        stopCloudSync();
        stopSelf();
    }

    @EventHandler
    public void handleConnectionStateChangedEvent(SLConnectionStateChangedEvent sLConnectionStateChangedEvent) {
        updateOnlineNotification();
    }

    @EventHandler
    public void handleDisconnectEvent(SLDisconnectEvent sLDisconnectEvent) {
        updateOnlineNotification();
        Debug.Log("GridConnectionService: stopping self because of disconnect.");
        stopCloudSync();
        stopVoice();
        stopSelf();
        this.currentLocationInfo.unsubscribe();
        VoicePluginServiceConnection.setInstallOfferDisplayed(false);
    }

    /* JADX INFO: renamed from: lambda$-com_lumiyaviewer_lumiya_GridConnectionService_20777, reason: not valid java name */
    /* synthetic */ void m19lambda$com_lumiyaviewer_lumiya_GridConnectionService_20777(ChatterNameRetriever chatterNameRetriever) {
        updateOnlineNotification();
    }

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        return this.mBinder;
    }

    @Override // android.app.Service
    public void onCreate() {
        super.onCreate();
        serviceInstance = new WeakReference<>(this);
        this.prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        this.prefs.registerOnSharedPreferenceChangeListener(this);
        readPreferences(this.prefs);
        updateOnlineNotification();
    }

    @Override // android.app.Service
    public void onDestroy() {
        if (this.prefs != null) {
            this.prefs.unregisterOnSharedPreferenceChangeListener(this);
            this.prefs = null;
        }
        this.onlineNotificationInfo = new OnlineNotificationInfo(onlineNotify, this, gridName, gridConnection, this.connectedAgentNameRetriever, null);
        stopForeground(true);
        this.shownNotificationIds.clear();
        if (this.eventBus != null) {
            this.eventBus.unsubscribe(this);
        }
        serviceInstance = null;
        super.onDestroy();
    }

    @EventHandler
    public void onGlobalPreferencesChanged(GlobalOptions.GlobalOptionsChangedEvent globalOptionsChangedEvent) {
        readPreferences(globalOptionsChangedEvent.preferences);
    }

    @Override // android.content.SharedPreferences.OnSharedPreferenceChangeListener
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String str) {
        readPreferences(sharedPreferences);
        updateOnlineNotification();
    }

    @Override // android.app.Service
    public int onStartCommand(Intent intent, int i, int i2) {
        Object[] objArr = new Object[2];
        objArr[0] = intent != null ? "not null" : "null";
        objArr[1] = Integer.valueOf(i);
        Debug.Printf("onStartCommand: intent is %s, flags %08x", objArr);
        if ((i & 1) != 0) {
            intent = null;
        }
        handleStartService(intent);
        return 2;
    }

    @Override // android.app.Service
    public boolean onUnbind(Intent intent) {
        Debug.Log("GridConnectionService: onUnbind called, connection state = " + gridConnection.getConnectionState());
        if (gridConnection.getConnectionState() == SLGridConnection.ConnectionState.Idle) {
            Debug.Log("GridConnectionService: Stopping self because unbind and no clients are bound");
            stopCloudSync();
            stopSelf();
        }
        return super.onUnbind(intent);
    }

    public void startVoice(VoiceLoginInfo voiceLoginInfo, UserManager userManager) {
        connectToVoicePlugin(voiceLoginInfo, userManager);
    }

    public void stopVoice() {
        if (this.voicePluginServiceConnection != null) {
            this.voicePluginServiceConnection.disconnect();
            this.voicePluginServiceConnection = null;
        }
    }

    public void terminateVoiceCall(ChatterID chatterID) {
        if (this.voicePluginServiceConnection != null) {
            this.voicePluginServiceConnection.terminateVoiceCall(chatterID);
        }
    }
}
