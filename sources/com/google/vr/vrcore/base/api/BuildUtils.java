package com.google.vr.vrcore.base.api;

import android.content.Context;
import android.content.pm.PackageManager;

/* JADX INFO: loaded from: classes.dex */
public class BuildUtils {
    private static volatile Boolean isDebug;

    private static synchronized boolean computeIsDebugBuild(Context context) {
        if (isDebug == null) {
            try {
                isDebug = Boolean.valueOf(SignatureUtils.verifySignature(context.getPackageManager().getPackageInfo(context.getPackageName(), 64), SignatureUtils.BLAZE_DEBUG_SIGNATURE, SignatureUtils.ANDROID_DEBUG_SIGNATURE, SignatureUtils.VRCORE_DEBUG_SIGNATURE));
            } catch (PackageManager.NameNotFoundException e) {
                throw new IllegalStateException("Unable to find self package info", e);
            }
        }
        return isDebug.booleanValue();
    }

    public static boolean isDebugBuild(Context context) {
        return isDebug == null ? computeIsDebugBuild(context) : isDebug.booleanValue();
    }

    public static synchronized void setIsDebugBuild(boolean z) {
        isDebug = Boolean.valueOf(z);
    }
}
