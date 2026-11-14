package com.lumiyaviewer.lumiya;

import android.util.Log;
import java.nio.ByteBuffer;

/* JADX INFO: loaded from: classes.dex */
public class Debug {
    private static final String LOG_TAG = "Lumiya";

    public static void AlwaysPrintf(String str, Object... objArr) {
        if (str != null) {
            Log.d(LOG_TAG, objArr.length == 0 ? str : String.format(str, objArr));
        }
    }

    public static void DumpBuffer(String str, ByteBuffer byteBuffer) {
    }

    public static void DumpBuffer(String str, byte[] bArr) {
    }

    public static void DumpBuffer(String str, byte[] bArr, int i) {
    }

    public static void Log(String str) {
        if (BuildConfig.DEBUG && str != null) {
            Log.d(LOG_TAG, str);
        }
    }

    public static void Printf(String str, Object... objArr) {
        if (BuildConfig.DEBUG && str != null) {
            Log.d(LOG_TAG, objArr.length == 0 ? str : String.format(str, objArr));
        }
    }

    public static void Warning(Throwable th) {
        if (BuildConfig.DEBUG && th != null) {
            Log.w(LOG_TAG, th.toString(), th);
        }
    }

    public static boolean isDebugBuild() {
        return BuildConfig.DEBUG;
    }
}
