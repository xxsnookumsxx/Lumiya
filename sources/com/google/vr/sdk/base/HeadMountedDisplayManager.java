package com.google.vr.sdk.base;

import android.content.Context;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import com.google.vr.cardboard.VrParamsProvider;
import com.google.vr.cardboard.VrParamsProviderFactory;
import com.google.vrtoolkit.cardboard.proto.nano.CardboardDevice;
import com.google.vrtoolkit.cardboard.proto.nano.Phone;

/* JADX INFO: loaded from: classes.dex */
public class HeadMountedDisplayManager {
    private static final String TAG = "HeadMountedDisplayManager";
    private final Context context;
    private final HeadMountedDisplay hmd = new HeadMountedDisplay(createScreenParams(), createGvrViewerParams());
    private final VrParamsProvider paramsProvider;

    public HeadMountedDisplayManager(Context context) {
        this.context = context;
        this.paramsProvider = VrParamsProviderFactory.create(context);
    }

    private GvrViewerParams createGvrViewerParams() {
        return new GvrViewerParams(this.paramsProvider.readDeviceParams());
    }

    private ScreenParams createScreenParams() {
        Display display = getDisplay();
        ScreenParams screenParamsFromProto = ScreenParams.fromProto(display, this.paramsProvider.readPhoneParams());
        return screenParamsFromProto == null ? new ScreenParams(display) : screenParamsFromProto;
    }

    private Display getDisplay() {
        return ((WindowManager) this.context.getSystemService("window")).getDefaultDisplay();
    }

    public HeadMountedDisplay getHeadMountedDisplay() {
        return this.hmd;
    }

    public void onPause() {
    }

    public void onResume() {
        CardboardDevice.DeviceParams deviceParams = this.paramsProvider.readDeviceParams();
        GvrViewerParams gvrViewerParams = deviceParams == null ? null : new GvrViewerParams(deviceParams);
        if (gvrViewerParams != null && !gvrViewerParams.equals(this.hmd.getGvrViewerParams())) {
            this.hmd.setGvrViewerParams(gvrViewerParams);
            Log.i(TAG, "Successfully read updated device params from external storage");
        }
        Phone.PhoneParams phoneParams = this.paramsProvider.readPhoneParams();
        ScreenParams screenParamsFromProto = phoneParams != null ? ScreenParams.fromProto(getDisplay(), phoneParams) : null;
        if (screenParamsFromProto == null || screenParamsFromProto.equals(this.hmd.getScreenParams())) {
            return;
        }
        this.hmd.setScreenParams(screenParamsFromProto);
        Log.i(TAG, "Successfully read updated screen params from external storage");
    }

    public boolean updateGvrViewerParams(GvrViewerParams gvrViewerParams) {
        if (gvrViewerParams == null || gvrViewerParams.equals(this.hmd.getGvrViewerParams())) {
            return false;
        }
        this.hmd.setGvrViewerParams(gvrViewerParams);
        this.paramsProvider.writeDeviceParams(gvrViewerParams.toProtobuf());
        return true;
    }

    public boolean updateScreenParams(ScreenParams screenParams) {
        if (screenParams == null || screenParams.equals(this.hmd.getScreenParams())) {
            return false;
        }
        this.hmd.setScreenParams(screenParams);
        return true;
    }
}
