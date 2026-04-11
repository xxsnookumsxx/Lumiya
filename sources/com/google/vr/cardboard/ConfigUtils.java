package com.google.vr.cardboard;

import android.net.Uri;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.google.protobuf.nano.MessageNano;
import com.google.vrtoolkit.cardboard.proto.nano.CardboardDevice;
import com.google.vrtoolkit.cardboard.proto.nano.Phone;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/* JADX INFO: loaded from: classes.dex */
public class ConfigUtils {
    public static final String CARDBOARD_CONFIG_FOLDER = "Cardboard";
    private static final String CARDBOARD_DEVICE_PARAMS_FILE = "current_device_params";
    private static final int CARDBOARD_DEVICE_PARAMS_STREAM_SENTINEL = 894990891;
    private static final String CARDBOARD_PHONE_PARAMS_FILE = "phone_params";
    private static final int CARDBOARD_PHONE_PARAMS_STREAM_SENTINEL = 779508118;
    private static final boolean DEBUG = false;
    private static final String TAG = ConfigUtils.class.getSimpleName();
    public static final String URI_KEY_PARAMS = "p";

    private static File getConfigFile(String str) throws IllegalStateException {
        File file = new File(Environment.getExternalStorageDirectory(), CARDBOARD_CONFIG_FOLDER);
        if (!file.exists()) {
            file.mkdirs();
        } else if (!file.isDirectory()) {
            String strValueOf = String.valueOf(file);
            throw new IllegalStateException(new StringBuilder(String.valueOf(strValueOf).length() + 61).append(strValueOf).append(" already exists as a file, but is expected to be a directory.").toString());
        }
        return new File(file, str);
    }

    public static CardboardDevice.DeviceParams readDeviceParamsFromExternalStorage() {
        return (CardboardDevice.DeviceParams) readFromExternalStorage(CardboardDevice.DeviceParams.class, CARDBOARD_DEVICE_PARAMS_FILE, CARDBOARD_DEVICE_PARAMS_STREAM_SENTINEL, true);
    }

    public static CardboardDevice.DeviceParams readDeviceParamsFromUri(Uri uri) {
        String queryParameter = uri.getQueryParameter(URI_KEY_PARAMS);
        if (queryParameter == null) {
            Log.w(TAG, "No Cardboard parameters in URI.");
            return null;
        }
        try {
            return (CardboardDevice.DeviceParams) MessageNano.mergeFrom(new CardboardDevice.DeviceParams(), Base64.decode(queryParameter, 11));
        } catch (Exception e) {
            String str = TAG;
            String strValueOf = String.valueOf(e);
            Log.w(str, new StringBuilder(String.valueOf(strValueOf).length() + 46).append("Parsing cardboard parameters from URI failed: ").append(strValueOf).toString());
            return null;
        }
    }

    private static <T extends MessageNano> T readFromExternalStorage(Class<T> cls, String str, int i, boolean z) throws Throwable {
        BufferedInputStream bufferedInputStream;
        try {
            try {
                bufferedInputStream = new BufferedInputStream(new FileInputStream(getConfigFile(str)));
            } catch (Throwable th) {
                th = th;
                bufferedInputStream = null;
            }
            try {
                T t = (T) readFromInputStream(cls, bufferedInputStream, i);
                try {
                    bufferedInputStream.close();
                } catch (IOException e) {
                }
                return t;
            } catch (Throwable th2) {
                th = th2;
                if (bufferedInputStream != null) {
                    try {
                        bufferedInputStream.close();
                    } catch (IOException e2) {
                    }
                }
                throw th;
            }
        } catch (FileNotFoundException e3) {
            if (z) {
                String str2 = TAG;
                String strValueOf = String.valueOf(e3);
                Log.d(str2, new StringBuilder(String.valueOf(strValueOf).length() + 39).append("Parameters file not found for reading: ").append(strValueOf).toString());
            }
            return null;
        } catch (IllegalStateException e4) {
            String str3 = TAG;
            String strValueOf2 = String.valueOf(e4);
            Log.w(str3, new StringBuilder(String.valueOf(strValueOf2).length() + 26).append("Error reading parameters: ").append(strValueOf2).toString());
            return null;
        }
    }

    private static <T extends MessageNano> T readFromInputStream(Class<T> cls, InputStream inputStream, int i) {
        if (inputStream == null) {
            return null;
        }
        try {
            ByteBuffer byteBufferAllocate = ByteBuffer.allocate(8);
            if (inputStream.read(byteBufferAllocate.array(), 0, byteBufferAllocate.array().length) == -1) {
                Log.e(TAG, "Error parsing param record: end of stream.");
                return null;
            }
            int i2 = byteBufferAllocate.getInt();
            int i3 = byteBufferAllocate.getInt();
            if (i2 != i) {
                Log.e(TAG, "Error parsing param record: incorrect sentinel.");
                return null;
            }
            byte[] bArr = new byte[i3];
            if (inputStream.read(bArr, 0, i3) != -1) {
                return (T) MessageNano.mergeFrom(cls.newInstance(), bArr);
            }
            Log.e(TAG, "Error parsing param record: end of stream.");
            return null;
        } catch (InvalidProtocolBufferNanoException e) {
            String str = TAG;
            String strValueOf = String.valueOf(e.toString());
            Log.w(str, strValueOf.length() == 0 ? new String("Error parsing protocol buffer: ") : "Error parsing protocol buffer: ".concat(strValueOf));
            return null;
        } catch (IOException e2) {
            String str2 = TAG;
            String strValueOf2 = String.valueOf(e2.toString());
            Log.w(str2, strValueOf2.length() == 0 ? new String("Error reading parameters: ") : "Error reading parameters: ".concat(strValueOf2));
            return null;
        } catch (IllegalAccessException e3) {
            String str3 = TAG;
            String strValueOf3 = String.valueOf(e3.toString());
            Log.w(str3, strValueOf3.length() == 0 ? new String("Error accessing parameter type: ") : "Error accessing parameter type: ".concat(strValueOf3));
            return null;
        } catch (InstantiationException e4) {
            String str4 = TAG;
            String strValueOf4 = String.valueOf(e4.toString());
            Log.w(str4, strValueOf4.length() == 0 ? new String("Error creating parameters: ") : "Error creating parameters: ".concat(strValueOf4));
            return null;
        }
    }

    public static Phone.PhoneParams readPhoneParamsFromExternalStorage() {
        return (Phone.PhoneParams) readFromExternalStorage(Phone.PhoneParams.class, "phone_params", CARDBOARD_PHONE_PARAMS_STREAM_SENTINEL, false);
    }

    public static boolean removeDeviceParamsFromExternalStorage() {
        boolean zDelete;
        try {
            File configFile = getConfigFile(CARDBOARD_DEVICE_PARAMS_FILE);
            zDelete = !configFile.exists() ? true : configFile.delete();
        } catch (IllegalStateException e) {
            String str = TAG;
            String strValueOf = String.valueOf(e);
            Log.w(str, new StringBuilder(String.valueOf(strValueOf).length() + 34).append("Error clearing device parameters: ").append(strValueOf).toString());
            zDelete = false;
        }
        if (!zDelete) {
            Log.e(TAG, "Could not clear Cardboard parameters from external storage.");
        }
        return zDelete;
    }

    public static boolean writeDeviceParamsToExternalStorage(CardboardDevice.DeviceParams deviceParams) throws Throwable {
        boolean zWriteToExternalStorage = writeToExternalStorage(deviceParams, CARDBOARD_DEVICE_PARAMS_FILE, CARDBOARD_DEVICE_PARAMS_STREAM_SENTINEL);
        if (!zWriteToExternalStorage) {
            Log.e(TAG, "Could not write Cardboard parameters to external storage.");
        }
        return zWriteToExternalStorage;
    }

    public static boolean writePhoneParamsToExternalStorage(Phone.PhoneParams phoneParams) throws Throwable {
        if (phoneParams.dEPRECATEDGyroBias != null && phoneParams.dEPRECATEDGyroBias.length == 0) {
            phoneParams = phoneParams.mo6clone();
            phoneParams.dEPRECATEDGyroBias = new float[]{0.0f, 0.0f, 0.0f};
        }
        boolean zWriteToExternalStorage = writeToExternalStorage(phoneParams, "phone_params", CARDBOARD_PHONE_PARAMS_STREAM_SENTINEL);
        if (!zWriteToExternalStorage) {
            Log.e(TAG, "Could not write Phone parameters to external storage.");
        }
        return zWriteToExternalStorage;
    }

    /* JADX WARN: Not initialized variable reg: 1, insn: 0x0089: MOVE (r2 I:??[OBJECT, ARRAY]) = (r1 I:??[OBJECT, ARRAY]), block:B:33:0x0089 */
    private static boolean writeToExternalStorage(MessageNano messageNano, String str, int i) throws Throwable {
        BufferedOutputStream bufferedOutputStream;
        BufferedOutputStream bufferedOutputStream2;
        BufferedOutputStream bufferedOutputStream3 = null;
        try {
        } catch (Throwable th) {
            th = th;
        }
        try {
            try {
                bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(getConfigFile(str)));
                try {
                    boolean zWriteToOutputStream = writeToOutputStream(messageNano, bufferedOutputStream, i);
                    try {
                        bufferedOutputStream.close();
                        return zWriteToOutputStream;
                    } catch (IOException e) {
                        return zWriteToOutputStream;
                    }
                } catch (FileNotFoundException e2) {
                    e = e2;
                    String str2 = TAG;
                    String strValueOf = String.valueOf(e);
                    Log.e(str2, new StringBuilder(String.valueOf(strValueOf).length() + 39).append("Parameters file not found for writing: ").append(strValueOf).toString());
                    if (bufferedOutputStream != null) {
                        try {
                            bufferedOutputStream.close();
                        } catch (IOException e3) {
                        }
                        return false;
                    }
                    return false;
                } catch (IllegalStateException e4) {
                    e = e4;
                    bufferedOutputStream3 = bufferedOutputStream;
                    String str3 = TAG;
                    String strValueOf2 = String.valueOf(e);
                    Log.w(str3, new StringBuilder(String.valueOf(strValueOf2).length() + 26).append("Error writing parameters: ").append(strValueOf2).toString());
                    if (bufferedOutputStream3 != null) {
                        try {
                            bufferedOutputStream3.close();
                        } catch (IOException e5) {
                        }
                        return false;
                    }
                    return false;
                }
            } catch (FileNotFoundException e6) {
                e = e6;
                bufferedOutputStream = null;
            } catch (IllegalStateException e7) {
                e = e7;
            }
        } catch (Throwable th2) {
            th = th2;
            bufferedOutputStream3 = bufferedOutputStream2;
            if (bufferedOutputStream3 != null) {
                try {
                    bufferedOutputStream3.close();
                } catch (IOException e8) {
                }
            }
            throw th;
        }
    }

    private static boolean writeToOutputStream(MessageNano messageNano, OutputStream outputStream, int i) {
        try {
            byte[] byteArray = MessageNano.toByteArray(messageNano);
            ByteBuffer byteBufferAllocate = ByteBuffer.allocate(8);
            byteBufferAllocate.putInt(i);
            byteBufferAllocate.putInt(byteArray.length);
            outputStream.write(byteBufferAllocate.array());
            outputStream.write(byteArray);
            return true;
        } catch (IOException e) {
            String str = TAG;
            String strValueOf = String.valueOf(e.toString());
            Log.w(str, strValueOf.length() == 0 ? new String("Error writing parameters: ") : "Error writing parameters: ".concat(strValueOf));
            return false;
        }
    }
}
