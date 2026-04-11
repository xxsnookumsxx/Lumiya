package com.lumiyaviewer.lumiya.slproto.users.manager;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.lumiyaviewer.lumiya.slproto.users.manager.UnreadNotificationInfo;
import com.lumiyaviewer.lumiya.ui.settings.NotificationType;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nonnull;

/* JADX INFO: loaded from: classes.dex */
public abstract class UnreadNotifications {
    public static UnreadNotifications create(@Nonnull UUID uuid, @Nonnull ImmutableMap<NotificationType, UnreadNotificationInfo> immutableMap) {
        return new AutoValue_UnreadNotifications(uuid, immutableMap);
    }

    @Nonnull
    public abstract UUID agentUUID();

    public UnreadNotifications filter(@Nonnull ImmutableSet<NotificationType> immutableSet) {
        if (immutableSet.containsAll(notificationGroups().keySet())) {
            return this;
        }
        ImmutableMap.Builder builder = ImmutableMap.builder();
        for (Map.Entry<NotificationType, UnreadNotificationInfo> entry : notificationGroups().entrySet()) {
            if (immutableSet.contains(entry.getKey())) {
                builder.put(entry);
            }
        }
        return create(agentUUID(), builder.build());
    }

    public UnreadNotificationInfo merge() {
        UnreadNotificationInfo.ObjectPopupNotification objectPopupNotification;
        ImmutableList.Builder builder;
        int i;
        ImmutableMap<NotificationType, UnreadNotificationInfo> immutableMapNotificationGroups = notificationGroups();
        if (immutableMapNotificationGroups.isEmpty()) {
            return UnreadNotificationInfo.create(agentUUID(), 0, null, null, 0, null, null, UnreadNotificationInfo.ObjectPopupNotification.create(0, 0, null));
        }
        if (immutableMapNotificationGroups.size() == 1) {
            return immutableMapNotificationGroups.entrySet().iterator().next().getValue();
        }
        UnreadNotificationInfo.ObjectPopupNotification objectPopupNotification2 = null;
        NotificationType notificationType = null;
        NotificationType notificationType2 = null;
        UnreadNotificationInfo.UnreadMessageSource unreadMessageSource = null;
        int i2 = 0;
        int iFreshMessagesCount = 0;
        ImmutableList.Builder builder2 = null;
        boolean z = false;
        Iterator<NotificationType> it = NotificationType.VALUES_BY_DESCENDING_PRIORITY.iterator();
        while (true) {
            objectPopupNotification = objectPopupNotification2;
            builder = builder2;
            i = i2;
            boolean z2 = z;
            if (!it.hasNext()) {
                break;
            }
            UnreadNotificationInfo unreadNotificationInfo = immutableMapNotificationGroups.get(it.next());
            if (unreadNotificationInfo != null) {
                int i3 = unreadNotificationInfo.totalUnreadCount() + i;
                if (!unreadNotificationInfo.unreadSources().isEmpty()) {
                    if (builder == null) {
                        builder = ImmutableList.builder();
                    }
                    builder.addAll((Iterable) unreadNotificationInfo.unreadSources());
                }
                NotificationType notificationTypeOrNull = unreadNotificationInfo.mostImportantType().orNull();
                if (notificationTypeOrNull != null && (notificationType == null || notificationTypeOrNull.compareTo(notificationType) > 0)) {
                    notificationType = notificationTypeOrNull;
                }
                NotificationType notificationTypeOrNull2 = unreadNotificationInfo.mostImportantFreshType().orNull();
                if (notificationTypeOrNull2 != null && (notificationType2 == null || notificationTypeOrNull2.compareTo(notificationType2) > 0)) {
                    notificationType2 = notificationTypeOrNull2;
                }
                iFreshMessagesCount += unreadNotificationInfo.freshMessagesCount();
                UnreadNotificationInfo.UnreadMessageSource unreadMessageSourceOrNull = unreadNotificationInfo.singleFreshSource().orNull();
                if (unreadMessageSourceOrNull != null) {
                    if (unreadMessageSource != null || !(!z2)) {
                        unreadMessageSourceOrNull = null;
                    }
                    z2 = true;
                } else {
                    unreadMessageSourceOrNull = unreadMessageSource;
                }
                UnreadNotificationInfo.ObjectPopupNotification objectPopupNotificationObjectPopupInfo = unreadNotificationInfo.objectPopupInfo();
                if (objectPopupNotificationObjectPopupInfo.isEmpty()) {
                    z = z2;
                    unreadMessageSource = unreadMessageSourceOrNull;
                    i2 = i3;
                    builder2 = builder;
                    objectPopupNotification2 = objectPopupNotification;
                } else {
                    unreadMessageSource = unreadMessageSourceOrNull;
                    builder2 = builder;
                    objectPopupNotification2 = objectPopupNotificationObjectPopupInfo;
                    z = z2;
                    i2 = i3;
                }
            } else {
                z = z2;
                i2 = i;
                builder2 = builder;
                objectPopupNotification2 = objectPopupNotification;
            }
        }
        return UnreadNotificationInfo.create(agentUUID(), i, builder != null ? builder.build() : null, notificationType, iFreshMessagesCount, notificationType2, unreadMessageSource, objectPopupNotification != null ? objectPopupNotification : UnreadNotificationInfo.ObjectPopupNotification.create(0, 0, null));
    }

    @Nonnull
    public abstract ImmutableMap<NotificationType, UnreadNotificationInfo> notificationGroups();
}
