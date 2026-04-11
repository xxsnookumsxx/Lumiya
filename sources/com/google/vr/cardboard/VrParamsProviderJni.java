package com.google.vr.cardboard;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.google.protobuf.nano.MessageNano;
import com.google.vr.ndk.base.SdkConfigurationReader;
import com.google.vrtoolkit.cardboard.proto.nano.CardboardDevice;
import com.google.vrtoolkit.cardboard.proto.nano.Phone;
import com.google.vrtoolkit.cardboard.proto.nano.Preferences;

/* JADX INFO: loaded from: classes.dex */
@UsedByNative
public class VrParamsProviderJni {
    private static final String TAG = "VrParamsProviderJni";
    private static volatile DisplayMetrics displayMetricsOverride = null;

    private static DisplayMetrics getDisplayMetrics(Context context) {
        DisplayMetrics displayMetrics = displayMetricsOverride;
        return displayMetrics == null ? DisplayUtils.getDisplayMetricsLandscape(((WindowManager) context.getSystemService("window")).getDefaultDisplay()) : displayMetrics;
    }

    private static native void nativeUpdateNativePhoneParamsPointer(long j, int i, int i2, float f, float f2);

    @UsedByNative
    private static byte[] readDeviceParams(Context context) {
        VrParamsProvider vrParamsProviderCreate = VrParamsProviderFactory.create(context);
        CardboardDevice.DeviceParams deviceParams = vrParamsProviderCreate.readDeviceParams();
        vrParamsProviderCreate.close();
        if (deviceParams != null) {
            return MessageNano.toByteArray(deviceParams);
        }
        return null;
    }

    @UsedByNative
    private static void readPhoneParams(Context context, long j) {
        if (context == null) {
            Log.w(TAG, "Missing context for phone params lookup. Results may be invalid.");
            updateNativePhoneParamsPointer(j, Resources.getSystem().getDisplayMetrics());
            return;
        }
        DisplayMetrics displayMetrics = getDisplayMetrics(context);
        VrParamsProvider vrParamsProviderCreate = VrParamsProviderFactory.create(context);
        Phone.PhoneParams phoneParams = vrParamsProviderCreate.readPhoneParams();
        vrParamsProviderCreate.close();
        if (phoneParams != null) {
            if (phoneParams.hasXPpi()) {
                displayMetrics.xdpi = phoneParams.getXPpi();
            }
            if (phoneParams.hasYPpi()) {
                displayMetrics.ydpi = phoneParams.getYPpi();
            }
        }
        updateNativePhoneParamsPointer(j, displayMetrics);
    }

    @UsedByNative
    private static byte[] readSdkConfigurationParams(Context context) {
        return MessageNano.toByteArray(SdkConfigurationReader.getParams(context));
    }

    @UsedByNative
    private static byte[] readUserPrefs(Context context) {
        VrParamsProvider vrParamsProviderCreate = VrParamsProviderFactory.create(context);
        Preferences.UserPrefs userPrefs = vrParamsProviderCreate.readUserPrefs();
        vrParamsProviderCreate.close();
        if (userPrefs != null) {
            return MessageNano.toByteArray(userPrefs);
        }
        return null;
    }

    public static void setDisplayOverride(Display display) {
        displayMetricsOverride = display != null ? DisplayUtils.getDisplayMetricsLandscape(display) : null;
    }

    private static void updateNativePhoneParamsPointer(long j, DisplayMetrics displayMetrics) {
        nativeUpdateNativePhoneParamsPointer(j, displayMetrics.widthPixels, displayMetrics.heightPixels, displayMetrics.xdpi, displayMetrics.ydpi);
    }

    @UsedByNative
    private static boolean writeDeviceParams(Context context, byte[] bArr) {
        CardboardDevice.DeviceParams deviceParams = null;
        VrParamsProvider vrParamsProviderCreate = VrParamsProviderFactory.create(context);
        if (bArr != null) {
            try {
                try {
                    deviceParams = (CardboardDevice.DeviceParams) MessageNano.mergeFrom(new CardboardDevice.DeviceParams(), bArr);
                } catch (InvalidProtocolBufferNanoException e) {
                    String strValueOf = String.valueOf(e);
                    Log.w(TAG, new StringBuilder(String.valueOf(strValueOf).length() + 31).append("Error parsing protocol buffer: ").append(strValueOf).toString());
                    vrParamsProviderCreate.close();
                    return false;
                }
            } finally {
                vrParamsProviderCreate.close();
            }
        }
        return vrParamsProviderCreate.writeDeviceParams(deviceParams);
    }
}
