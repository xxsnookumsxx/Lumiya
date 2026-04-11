package com.google.vr.ndk.base;

import android.content.Context;
import android.util.Log;
import com.google.common.logging.nano.Vr;
import com.google.vr.cardboard.VrParamsProvider;
import com.google.vr.cardboard.VrParamsProviderFactory;
import com.google.vr.vrcore.nano.SdkConfiguration;

/* JADX INFO: loaded from: classes.dex */
public class SdkConfigurationReader {
    public static final Vr.VREvent.SdkConfigurationParams DEFAULT_PARAMS;
    static final Vr.VREvent.SdkConfigurationParams REQUESTED_PARAMS;
    private static final String TAG = SdkConfigurationReader.class.getSimpleName();
    static Vr.VREvent.SdkConfigurationParams sParams;

    static {
        Vr.VREvent.SdkConfigurationParams sdkConfigurationParams = new Vr.VREvent.SdkConfigurationParams();
        REQUESTED_PARAMS = sdkConfigurationParams;
        sdkConfigurationParams.useSystemClockForSensorTimestamps = true;
        REQUESTED_PARAMS.useMagnetometerInSensorFusion = true;
        REQUESTED_PARAMS.allowDynamicLibraryLoading = true;
        REQUESTED_PARAMS.cpuLateLatchingEnabled = true;
        REQUESTED_PARAMS.daydreamImageAlignment = 1;
        REQUESTED_PARAMS.asyncReprojectionConfig = new Vr.VREvent.SdkConfigurationParams.AsyncReprojectionConfig();
        REQUESTED_PARAMS.useOnlineMagnetometerCalibration = true;
        Vr.VREvent.SdkConfigurationParams sdkConfigurationParams2 = new Vr.VREvent.SdkConfigurationParams();
        DEFAULT_PARAMS = sdkConfigurationParams2;
        sdkConfigurationParams2.useSystemClockForSensorTimestamps = false;
        DEFAULT_PARAMS.useMagnetometerInSensorFusion = false;
        DEFAULT_PARAMS.allowDynamicLibraryLoading = false;
        DEFAULT_PARAMS.cpuLateLatchingEnabled = false;
        DEFAULT_PARAMS.daydreamImageAlignment = 3;
        DEFAULT_PARAMS.asyncReprojectionConfig = null;
        DEFAULT_PARAMS.useOnlineMagnetometerCalibration = false;
    }

    public static Vr.VREvent.SdkConfigurationParams getParams(Context context) {
        synchronized (SdkConfigurationReader.class) {
            if (sParams != null) {
                return sParams;
            }
            VrParamsProvider vrParamsProviderCreate = VrParamsProviderFactory.create(context);
            Vr.VREvent.SdkConfigurationParams paramsFromProvider = readParamsFromProvider(vrParamsProviderCreate);
            synchronized (SdkConfigurationReader.class) {
                sParams = paramsFromProvider;
            }
            vrParamsProviderCreate.close();
            return sParams;
        }
    }

    private static Vr.VREvent.SdkConfigurationParams readParamsFromProvider(VrParamsProvider vrParamsProvider) {
        SdkConfiguration.SdkConfigurationRequest sdkConfigurationRequest = new SdkConfiguration.SdkConfigurationRequest();
        sdkConfigurationRequest.requestedParams = REQUESTED_PARAMS;
        sdkConfigurationRequest.sdkVersion = "1.10.0";
        Vr.VREvent.SdkConfigurationParams sdkConfigurationParams = vrParamsProvider.readSdkConfigurationParams(sdkConfigurationRequest);
        if (sdkConfigurationParams == null) {
            Log.w(TAG, "VrParamsProvider returned null params, using defaults.");
            return DEFAULT_PARAMS;
        }
        String str = TAG;
        String strValueOf = String.valueOf(sdkConfigurationParams);
        Log.d(str, new StringBuilder(String.valueOf(strValueOf).length() + 38).append("Fetched params from VrParamsProvider: ").append(strValueOf).toString());
        return sdkConfigurationParams;
    }
}
