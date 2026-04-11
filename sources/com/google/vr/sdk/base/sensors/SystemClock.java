package com.google.vr.sdk.base.sensors;

/* JADX INFO: loaded from: classes.dex */
public class SystemClock implements Clock {
    @Override // com.google.vr.sdk.base.sensors.Clock
    public long nanoTime() {
        return System.nanoTime();
    }
}
