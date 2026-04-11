package com.google.vr.cardboard;

import android.content.Context;
import android.os.RemoteException;
import android.util.Log;
import com.google.vr.ndk.base.Version;
import com.google.vr.vrcore.base.api.VrCoreNotAvailableException;
import com.google.vr.vrcore.base.api.VrCoreUtils;
import com.google.vr.vrcore.library.api.IVrNativeLibraryLoader;
import com.google.vr.vrcore.library.api.ObjectWrapper;
import com.google.vr.vrcore.library.api.VrCoreLoader;

/* JADX INFO: loaded from: classes.dex */
@UsedByNative
public class VrCoreLibraryLoader {
    private static final String TAG = VrCoreLibraryLoader.class.getSimpleName();

    public static void checkVrCoreGvrLibraryAvailable(Context context) throws VrCoreNotAvailableException {
        checkVrCoreGvrLibraryAvailable(context, Version.CURRENT);
    }

    private static void checkVrCoreGvrLibraryAvailable(Context context, Version version) throws VrCoreNotAvailableException {
        String vrCoreSdkLibraryVersion = VrCoreUtils.getVrCoreSdkLibraryVersion(context);
        Version version2 = Version.parse(vrCoreSdkLibraryVersion);
        if (version2 == null) {
            Log.i(TAG, "VrCore version does not support library loading.");
            throw new VrCoreNotAvailableException(4);
        }
        if (version2.isAtLeast(version)) {
            return;
        }
        Log.w(TAG, String.format("VrCore GVR library version obsolete; VrCore supports %s but target version is %s", vrCoreSdkLibraryVersion, version.toString()));
        throw new VrCoreNotAvailableException(4);
    }

    @UsedByNative
    public static long loadNativeGvrLibrary(Context context, int i, int i2, int i3) {
        try {
            Version version = new Version(i, i2, i3);
            if (!Version.CURRENT.equals(version)) {
                Log.w(TAG, String.format("Native SDK version does not match Java; expected %s but received %s", Version.CURRENT, version.toString()));
            }
            checkVrCoreGvrLibraryAvailable(context, version);
            IVrNativeLibraryLoader iVrNativeLibraryLoaderNewNativeLibraryLoader = VrCoreLoader.getRemoteCreator(context).newNativeLibraryLoader(ObjectWrapper.wrap(VrCoreLoader.getRemoteContext(context)), ObjectWrapper.wrap(context));
            if (iVrNativeLibraryLoaderNewNativeLibraryLoader != null) {
                return iVrNativeLibraryLoaderNewNativeLibraryLoader.loadNativeGvrLibrary(version.majorVersion, version.minorVersion, version.patchVersion);
            }
            Log.e(TAG, "Failed to load native GVR library from VrCore: no library loader available.");
            return 0L;
        } catch (RemoteException | VrCoreNotAvailableException | IllegalArgumentException | IllegalStateException | SecurityException | UnsatisfiedLinkError e) {
            String str = TAG;
            String strValueOf = String.valueOf(e);
            Log.e(str, new StringBuilder(String.valueOf(strValueOf).length() + 49).append("Failed to load native GVR library from VrCore:\n  ").append(strValueOf).toString());
            return 0L;
        }
    }
}
