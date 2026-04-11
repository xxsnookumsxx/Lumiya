package com.lumiyaviewer.lumiya.slproto.users.manager;

import android.content.Intent;
import android.content.SharedPreferences;
import com.lumiyaviewer.lumiya.Debug;
import com.lumiyaviewer.lumiya.GlobalOptions;
import com.lumiyaviewer.lumiya.LumiyaApp;
import com.lumiyaviewer.lumiya.dao.ChatMessageDao;
import com.lumiyaviewer.lumiya.dao.Chatter;
import com.lumiyaviewer.lumiya.dao.ChatterDao;
import com.lumiyaviewer.lumiya.dao.DaoSession;
import com.lumiyaviewer.lumiya.eventbus.EventBus;
import com.lumiyaviewer.lumiya.eventbus.EventHandler;
import com.lumiyaviewer.lumiya.react.SimpleRequestHandler;
import com.lumiyaviewer.lumiya.react.Subscribable;
import com.lumiyaviewer.lumiya.react.SubscriptionPool;
import com.lumiyaviewer.lumiya.slproto.users.ChatterID;
import com.lumiyaviewer.lumiya.slproto.users.ChatterNameRetriever;
import com.lumiyaviewer.lumiya.slproto.users.manager.UnreadNotificationInfo;
import com.lumiyaviewer.lumiya.ui.notify.NotificationChannels;
import com.lumiyaviewer.lumiya.ui.settings.NotificationType;
import de.greenrobot.dao.query.QueryBuilder;
import de.greenrobot.dao.query.WhereCondition;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/* JADX INFO: loaded from: classes.dex */
public class UnreadNotificationManager implements ChatterNameRetriever.OnChatterNameUpdated {
    private static final long FRESH_MESSAGES_NOTIFICATION_INTERVAL = 3000;
    private static final int MASK_ENABLED_ALL = 7;
    private static final int MASK_ENABLED_GROUP = 2;
    private static final int MASK_ENABLED_IM = 4;
    private static final int MASK_ENABLED_LOCAL = 1;
    private static final int MAX_CHATTERS_PER_NOTIFICATION = 3;
    private static final int MAX_MESSAGES_PER_NOTIFICATION = 3;
    public static final Boolean unreadNotificationKey = Boolean.FALSE;

    @Nonnull
    private final ChatMessageDao chatMessageDao;

    @Nonnull
    private final ChatterDao chatterDao;
    private final UnreadNotificationInfo emptyNotification;
    private final Executor updateExecutor;

    @Nonnull
    private final UserManager userManager;
    private final Map<Long, ChatterNameRetriever> chatterSources = new ConcurrentHashMap(4, 0.75f, 2);
    private final AtomicInteger maskEnabled = new AtomicInteger(7);
    private final AtomicInteger totalUnreadCount = new AtomicInteger(0);
    private final AtomicInteger totalSourcesCount = new AtomicInteger(0);
    private final AtomicReference<NotificationType> mostImportantNotificationType = new AtomicReference<>();
    private final Map<Long, Integer> freshMessageCounts = new HashMap();
    private final Object freshMessageCountsLock = new Object();
    private final AtomicLong lastFreshMessageNotification = new AtomicLong(0);
    private final Object notifyCaptureLock = new Object();

    @Nullable
    private WeakReference<NotifyCapture> notifyCapture = null;
    private final Runnable updateChatterDataRunnable = new Runnable() { // from class: com.lumiyaviewer.lumiya.slproto.users.manager.UnreadNotificationManager.1
        @Override // java.lang.Runnable
        public void run() {
            UnreadNotificationManager.this.updateUnreadChatterData();
            UnreadNotificationManager.this.updateExecutor.execute(UnreadNotificationManager.this.updateNotificationDataRunnable);
        }
    };
    private final Runnable updateNotificationDataRunnable = new Runnable() { // from class: com.lumiyaviewer.lumiya.slproto.users.manager.UnreadNotificationManager.2
        @Override // java.lang.Runnable
        public void run() {
            UnreadNotificationManager.this.unreadNotificationInfoPool.onResultData(UnreadNotificationManager.unreadNotificationKey, UnreadNotificationManager.this.getUnreadNotification());
        }
    };
    private final SubscriptionPool<Boolean, UnreadNotifications> unreadNotificationInfoPool = new SubscriptionPool<>();

    public interface NotifyCapture {
        @Nullable
        Intent onGetNotifyCaptureIntent(@Nonnull UnreadNotificationInfo unreadNotificationInfo, Intent intent);
    }

    public UnreadNotificationManager(@Nonnull UserManager userManager, @Nonnull DaoSession daoSession) {
        this.userManager = userManager;
        this.chatterDao = daoSession.getChatterDao();
        this.chatMessageDao = daoSession.getChatMessageDao();
        this.updateExecutor = userManager.getDatabaseRunOnceExecutor();
        this.emptyNotification = UnreadNotificationInfo.create(userManager.getUserID(), 0, null, null, 0, null, null, UnreadNotificationInfo.ObjectPopupNotification.create(0, 0, null));
        this.unreadNotificationInfoPool.attachRequestHandler(new SimpleRequestHandler<Boolean>() { // from class: com.lumiyaviewer.lumiya.slproto.users.manager.UnreadNotificationManager.3
            @Override // com.lumiyaviewer.lumiya.react.RequestHandler
            public void onRequest(@Nonnull Boolean bool) {
                UnreadNotificationManager.this.updateExecutor.execute(UnreadNotificationManager.this.updateChatterDataRunnable);
            }
        });
        updateTypesFromPreferences(LumiyaApp.getDefaultSharedPreferences());
        EventBus.getInstance().subscribe(this);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Removed duplicated region for block: B:97:0x0241 A[PHI: r8
  0x0241: PHI (r8v7 int) = (r8v5 int), (r8v8 int) binds: [B:26:0x00bc, B:34:0x00de] A[DONT_GENERATE, DONT_INLINE]] */
    /* JADX WARN: Removed duplicated region for block: B:99:0x024c  */
    @javax.annotation.Nonnull
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public com.lumiyaviewer.lumiya.slproto.users.manager.UnreadNotifications getUnreadNotification() {
        /*
            Method dump skipped, instruction units count: 601
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lumiyaviewer.lumiya.slproto.users.manager.UnreadNotificationManager.getUnreadNotification():com.lumiyaviewer.lumiya.slproto.users.manager.UnreadNotifications");
    }

    private void setEnabledMask(int i) {
        if (this.maskEnabled.getAndSet(i) != i) {
            updateUnreadNotifications();
        }
    }

    private void updateTypesFromPreferences(SharedPreferences sharedPreferences) {
        int i;
        if (NotificationChannels.getInstance().areNotificationsSystemControlled()) {
            i = 7;
        } else {
            i = sharedPreferences.getBoolean(NotificationType.LocalChat.getEnableKey(), true) ? 1 : 0;
            if (sharedPreferences.getBoolean(NotificationType.Group.getEnableKey(), true)) {
                i |= 2;
            }
            if (sharedPreferences.getBoolean(NotificationType.Private.getEnableKey(), true)) {
                i |= 4;
            }
        }
        setEnabledMask(i);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateUnreadChatterData() {
        int i = this.maskEnabled.get();
        if (i == 0) {
            this.totalUnreadCount.set(0);
            this.totalSourcesCount.set(0);
            Iterator<Map.Entry<Long, ChatterNameRetriever>> it = this.chatterSources.entrySet().iterator();
            while (it.hasNext()) {
                it.next().getValue().dispose();
                it.remove();
            }
            this.mostImportantNotificationType.set(null);
            return;
        }
        QueryBuilder<Chatter> queryBuilderWhere = this.chatterDao.queryBuilder().where(ChatterDao.Properties.UnreadCount.gt(0), ChatterDao.Properties.Muted.notEq(true));
        if (i != 7) {
            ArrayList arrayList = new ArrayList(3);
            if ((i & 1) != 0) {
                arrayList.add(Integer.valueOf(ChatterID.ChatterType.Local.ordinal()));
            }
            if ((i & 2) != 0) {
                arrayList.add(Integer.valueOf(ChatterID.ChatterType.Group.ordinal()));
            }
            if ((i & 4) != 0) {
                arrayList.add(Integer.valueOf(ChatterID.ChatterType.User.ordinal()));
            }
            queryBuilderWhere = queryBuilderWhere.where(ChatterDao.Properties.Type.in(arrayList), new WhereCondition[0]);
        }
        HashSet hashSet = null;
        int i2 = 0;
        int unreadCount = 0;
        NotificationType notificationType = null;
        for (Chatter chatter : queryBuilderWhere.orderDesc(ChatterDao.Properties.LastMessageID).listLazy()) {
            ChatterID chatterIDFromDatabaseObject = ChatterID.fromDatabaseObject(this.userManager.getUserID(), chatter);
            if (chatterIDFromDatabaseObject != null) {
                if (hashSet == null) {
                    hashSet = new HashSet();
                }
                if (hashSet.size() < 3) {
                    hashSet.add(chatter.getId());
                    if (!this.chatterSources.containsKey(chatter.getId())) {
                        this.chatterSources.put(chatter.getId(), new ChatterNameRetriever(chatterIDFromDatabaseObject, this, null));
                    }
                }
                NotificationType notificationType2 = chatterIDFromDatabaseObject.getChatterType().getNotificationType();
                if (notificationType == null || notificationType2.compareTo(notificationType) > 0) {
                    notificationType = notificationType2;
                }
                unreadCount += chatter.getUnreadCount();
                i2++;
            }
            unreadCount = unreadCount;
            i2 = i2;
            hashSet = hashSet;
            notificationType = notificationType;
        }
        this.totalUnreadCount.set(unreadCount);
        this.totalSourcesCount.set(i2);
        this.mostImportantNotificationType.set(notificationType);
        Iterator<Map.Entry<Long, ChatterNameRetriever>> it2 = this.chatterSources.entrySet().iterator();
        while (it2.hasNext()) {
            Map.Entry<Long, ChatterNameRetriever> next = it2.next();
            if (hashSet == null || (!hashSet.contains(next.getKey()))) {
                next.getValue().dispose();
                it2.remove();
            }
        }
    }

    public void addFreshMessage(@Nonnull Chatter chatter) {
        ChatterID.ChatterType chatterType;
        boolean z = true;
        Long id = chatter.getId();
        if (id != null) {
            int i = this.maskEnabled.get();
            if (i == 0) {
                z = false;
            } else if (i != 7 && (((chatterType = ChatterID.ChatterType.VALUES[chatter.getType()]) != ChatterID.ChatterType.User || (i & 4) == 0) && ((chatterType != ChatterID.ChatterType.Group || (i & 2) == 0) && (chatterType != ChatterID.ChatterType.Local || (i & 1) == 0)))) {
                z = false;
            }
            if (!z) {
                synchronized (this.freshMessageCountsLock) {
                    this.freshMessageCounts.remove(id);
                }
            } else {
                synchronized (this.freshMessageCountsLock) {
                    Integer num = this.freshMessageCounts.get(id);
                    this.freshMessageCounts.put(id, Integer.valueOf((num != null ? num.intValue() : 0) + 1));
                }
            }
        }
    }

    @Nullable
    public Intent captureNotify(UnreadNotificationInfo unreadNotificationInfo, Intent intent) {
        NotifyCapture notifyCapture;
        synchronized (this.notifyCaptureLock) {
            notifyCapture = this.notifyCapture != null ? this.notifyCapture.get() : null;
        }
        Debug.Printf("NotifyCapture: capture = %s", notifyCapture);
        if (notifyCapture != null) {
            return notifyCapture.onGetNotifyCaptureIntent(unreadNotificationInfo, intent);
        }
        return null;
    }

    public void clearFreshMessages(@Nonnull Chatter chatter) {
        Long id = chatter.getId();
        if (id != null) {
            synchronized (this.freshMessageCountsLock) {
                this.freshMessageCounts.remove(id);
            }
        }
    }

    public void clearNotifyCapture(@Nullable NotifyCapture notifyCapture) {
        synchronized (this.notifyCaptureLock) {
            if (this.notifyCapture != null && this.notifyCapture.get() == notifyCapture) {
                this.notifyCapture = null;
                updateUnreadNotifications();
            }
        }
    }

    public Subscribable<Boolean, UnreadNotifications> getUnreadNotifications() {
        return this.unreadNotificationInfoPool;
    }

    @Override // com.lumiyaviewer.lumiya.slproto.users.ChatterNameRetriever.OnChatterNameUpdated
    public void onChatterNameUpdated(ChatterNameRetriever chatterNameRetriever) {
        this.updateExecutor.execute(this.updateNotificationDataRunnable);
    }

    @EventHandler
    public void onGlobalPreferencesChanged(GlobalOptions.GlobalOptionsChangedEvent globalOptionsChangedEvent) {
        if (globalOptionsChangedEvent.preferences != null) {
            updateTypesFromPreferences(globalOptionsChangedEvent.preferences);
        }
    }

    public void setNotifyCapture(@Nullable NotifyCapture notifyCapture) {
        synchronized (this.notifyCaptureLock) {
            this.notifyCapture = new WeakReference<>(notifyCapture);
            updateUnreadNotifications();
        }
    }

    void updateUnreadNotifications() {
        this.unreadNotificationInfoPool.requestUpdate(unreadNotificationKey);
    }
}
