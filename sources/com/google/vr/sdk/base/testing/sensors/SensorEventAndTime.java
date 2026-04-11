package com.google.vr.sdk.base.testing.sensors;

import android.hardware.SensorEvent;

/* JADX INFO: loaded from: classes.dex */
public class SensorEventAndTime {
    public SensorEvent event;
    public long timeNs;

    public SensorEventAndTime(SensorEvent sensorEvent, long j) {
        this.event = sensorEvent;
        this.timeNs = j;
    }
}
