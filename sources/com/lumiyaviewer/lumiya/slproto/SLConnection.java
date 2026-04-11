package com.lumiyaviewer.lumiya.slproto;

import java.io.IOException;
import java.nio.channels.Selector;
import java.util.Timer;

/* JADX INFO: loaded from: classes.dex */
public class SLConnection implements Runnable {
    private static final int DEFAULT_IDLE_INTERVAL = 1000;
    private Selector selector;
    private volatile Timer timer;
    private Thread workingThread;

    public SLConnection() {
        System.setProperty("java.net.preferIPv4Stack", "true");
        System.setProperty("java.net.preferIPv6Addresses", "false");
        try {
            this.selector = Selector.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.workingThread = null;
        this.timer = null;
    }

    public void AddCircuit(SLCircuit sLCircuit) {
        synchronized (this) {
            if (this.workingThread == null) {
                this.workingThread = new Thread(this, "SLConnection");
                this.workingThread.start();
            }
        }
    }

    public Selector getSelector() {
        return this.selector;
    }

    public Timer getTimer() {
        Timer timer;
        synchronized (this) {
            if (this.timer == null) {
                this.timer = new Timer("SLConnectionTimer", true);
            }
            timer = this.timer;
        }
        return timer;
    }

    /* JADX WARN: Removed duplicated region for block: B:76:0x00f2  */
    @Override // java.lang.Runnable
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public void run() {
        /*
            Method dump skipped, instruction units count: 245
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lumiyaviewer.lumiya.slproto.SLConnection.run():void");
    }
}
