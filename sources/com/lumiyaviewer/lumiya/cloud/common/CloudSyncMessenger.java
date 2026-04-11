package com.lumiyaviewer.lumiya.cloud.common;

import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

/* JADX INFO: loaded from: classes.dex */
public class CloudSyncMessenger {
    public static boolean sendMessage(Messenger messenger, MessageType messageType, Bundleable bundleable, Messenger messenger2) {
        if (messenger == null) {
            return false;
        }
        Bundle bundle = new Bundle();
        bundle.putString("messageType", messageType.toString());
        bundle.putBundle("message", bundleable.toBundle());
        Message messageObtain = Message.obtain(null, 100, bundle);
        messageObtain.replyTo = messenger2;
        try {
            messenger.send(messageObtain);
            return true;
        } catch (RemoteException e) {
            return false;
        }
    }
}
