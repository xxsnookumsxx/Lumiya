package com.lumiyaviewer.lumiya.slproto.events;

/* JADX INFO: loaded from: classes.dex */
public class SLDisconnectEvent {
    public final String message;
    public final boolean normalDisconnect;

    public SLDisconnectEvent(boolean z, String str) {
        this.normalDisconnect = z;
        this.message = str;
    }
}
