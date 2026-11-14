package com.lumiyaviewer.lumiya.ui.notify;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import com.google.common.base.Objects;
import com.lumiyaviewer.lumiya.R;
import com.lumiyaviewer.lumiya.slproto.SLGridConnection;
import com.lumiyaviewer.lumiya.slproto.avatar.SLMoveEvents;
import com.lumiyaviewer.lumiya.slproto.users.ChatterNameRetriever;
import com.lumiyaviewer.lumiya.slproto.users.ParcelData;
import com.lumiyaviewer.lumiya.slproto.users.manager.CurrentLocationInfo;
import com.lumiyaviewer.lumiya.ui.login.LoginActivity;
import com.lumiyaviewer.lumiya.ui.notify.NotificationChannels;

/* loaded from: classes.dex */
public class OnlineNotificationInfo {

    /* renamed from: -com-lumiyaviewer-lumiya-slproto-SLGridConnection$ConnectionStateSwitchesValues, reason: not valid java name */
    private static /* synthetic */ int[] f465x8f75539 = null;
    private final String contentText;
    private final boolean hasProgress;
    private final String titleText;
    private final boolean visible;

    /* renamed from: -getcom-lumiyaviewer-lumiya-slproto-SLGridConnection$ConnectionStateSwitchesValues, reason: not valid java name */
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

    /* JADX WARN: Removed duplicated region for block: B:28:0x00be  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public OnlineNotificationInfo(boolean z, Context context, String str, SLGridConnection sLGridConnection, ChatterNameRetriever chatterNameRetriever, @Nullable CurrentLocationInfo currentLocationInfo) {
        String str2;
        if (!z || sLGridConnection == null) {
            this.visible = false;
            this.titleText = null;
            this.contentText = null;
            this.hasProgress = false;
            return;
        }
        switch (m678x1c568815()[sLGridConnection.getConnectionState().ordinal()]) {
            case 1:
                this.visible = true;
                String resolvedName = chatterNameRetriever.getResolvedName();
                this.titleText = resolvedName != null ? str + ": " + resolvedName : str;
                if (currentLocationInfo != null) {
                    ParcelData parcelData = currentLocationInfo.parcelData();
                    String name = parcelData != null ? parcelData.getName() : null;
                    if (name != null && !name.equals("(loading)")) {
                        str2 = String.format(context.getResources().getString(R.string.grid_status_connected_details), name, Integer.valueOf(currentLocationInfo.nearbyUsers()));
                        this.contentText = str2 == null ? String.format(context.getResources().getString(R.string.grid_status_connected), str) : str2;
                        this.hasProgress = false;
                        break;
                    }
                }
                str2 = null;
                this.contentText = str2 == null ? String.format(context.getResources().getString(R.string.grid_status_connected), str) : str2;
                this.hasProgress = false;
                break;
            case 2:
                this.visible = true;
                this.titleText = str;
                if (sLGridConnection.getIsReconnecting()) {
                    this.contentText = String.format(context.getResources().getString(R.string.grid_status_reconnecting), str);
                } else {
                    this.contentText = String.format(context.getResources().getString(R.string.grid_status_connecting), str);
                }
                this.hasProgress = true;
                break;
            default:
                this.visible = false;
                this.titleText = null;
                this.contentText = null;
                this.hasProgress = false;
                break;
        }
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
