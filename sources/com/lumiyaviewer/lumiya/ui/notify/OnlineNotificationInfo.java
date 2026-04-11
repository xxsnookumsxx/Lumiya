package com.lumiyaviewer.lumiya.ui.notify;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import com.google.common.base.Objects;
import com.lumiyaviewer.lumiya.R;
import com.lumiyaviewer.lumiya.slproto.SLGridConnection;
import com.lumiyaviewer.lumiya.slproto.avatar.SLMoveEvents;
import com.lumiyaviewer.lumiya.ui.login.LoginActivity;
import com.lumiyaviewer.lumiya.ui.notify.NotificationChannels;

/* JADX INFO: loaded from: classes.dex */
public class OnlineNotificationInfo {

    /* JADX INFO: renamed from: -com-lumiyaviewer-lumiya-slproto-SLGridConnection$ConnectionStateSwitchesValues, reason: not valid java name */
    private static final /* synthetic */ int[] f465x8f75539 = null;
    private final String contentText;
    private final boolean hasProgress;
    private final String titleText;
    private final boolean visible;

    /* JADX INFO: renamed from: -getcom-lumiyaviewer-lumiya-slproto-SLGridConnection$ConnectionStateSwitchesValues, reason: not valid java name */
    private static /* synthetic */ int[] m678x1c568815() {
        if (f465x8f75539 != null) {
            return f465x8f75539;
        }
        int[] iArr = new int[SLGridConnection.ConnectionState.valuesCustom().length];
        try {
            iArr[SLGridConnection.ConnectionState.Connected.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[SLGridConnection.ConnectionState.Connecting.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[SLGridConnection.ConnectionState.Idle.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        f465x8f75539 = iArr;
        return iArr;
    }

    /* JADX WARN: Removed duplicated region for block: B:31:0x00d9  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public OnlineNotificationInfo(boolean r7, android.content.Context r8, java.lang.String r9, com.lumiyaviewer.lumiya.slproto.SLGridConnection r10, com.lumiyaviewer.lumiya.slproto.users.ChatterNameRetriever r11, @android.support.annotation.Nullable com.lumiyaviewer.lumiya.slproto.users.manager.CurrentLocationInfo r12) {
        /*
            Method dump skipped, instruction units count: 230
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lumiyaviewer.lumiya.ui.notify.OnlineNotificationInfo.<init>(boolean, android.content.Context, java.lang.String, com.lumiyaviewer.lumiya.slproto.SLGridConnection, com.lumiyaviewer.lumiya.slproto.users.ChatterNameRetriever, com.lumiyaviewer.lumiya.slproto.users.manager.CurrentLocationInfo):void");
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof OnlineNotificationInfo)) {
            return false;
        }
        OnlineNotificationInfo onlineNotificationInfo = (OnlineNotificationInfo) obj;
        return Objects.equal(this.titleText, onlineNotificationInfo.titleText) && Objects.equal(this.contentText, onlineNotificationInfo.contentText) && this.visible == onlineNotificationInfo.visible && this.hasProgress == onlineNotificationInfo.hasProgress;
    }

    public Notification getNotification(Context context) {
        if (!this.visible) {
            return null;
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NotificationChannels.getInstance().getChannelName(NotificationChannels.Channel.OnlineStatus));
        builder.setSmallIcon(R.drawable.ic_online_notify).setContentTitle(this.titleText).setContentText(this.contentText).setDefaults(0).setOngoing(true).setContentIntent(PendingIntent.getActivity(context, 0, new Intent(context, (Class<?>) LoginActivity.class), SLMoveEvents.AGENT_CONTROL_AWAY)).setOnlyAlertOnce(true);
        if (this.hasProgress) {
            builder.setProgress(0, 0, true);
        }
        return builder.build();
    }
}
