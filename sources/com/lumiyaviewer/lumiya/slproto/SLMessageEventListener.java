package com.lumiyaviewer.lumiya.slproto;

/* JADX INFO: loaded from: classes.dex */
public interface SLMessageEventListener {

    public static abstract class SLMessageBaseEventListener implements SLMessageEventListener {
        @Override // com.lumiyaviewer.lumiya.slproto.SLMessageEventListener
        public void onMessageAcknowledged(SLMessage sLMessage) {
        }

        @Override // com.lumiyaviewer.lumiya.slproto.SLMessageEventListener
        public void onMessageTimeout(SLMessage sLMessage) {
        }
    }

    void onMessageAcknowledged(SLMessage sLMessage);

    void onMessageTimeout(SLMessage sLMessage);
}
