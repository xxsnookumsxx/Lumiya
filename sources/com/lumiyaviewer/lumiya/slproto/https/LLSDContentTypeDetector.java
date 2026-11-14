package com.lumiyaviewer.lumiya.slproto.https;

import com.lumiyaviewer.lumiya.Debug;
import java.io.BufferedInputStream;
import java.io.IOException;

/* loaded from: classes.dex */
public class LLSDContentTypeDetector {

    public enum LLSDContentType {
        llsdXML,
        llsdBinary;

        /* renamed from: values, reason: to resolve conflict with enum method */
        public static LLSDContentType[] valuesCustom() {
            return values();
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:27:0x0058  */
    /* JADX WARN: Removed duplicated region for block: B:30:0x005f  */
    /* JADX WARN: Removed duplicated region for block: B:38:0x0081  */
    /* JADX WARN: Removed duplicated region for block: B:41:0x00b9  */
    /* JADX WARN: Removed duplicated region for block: B:43:0x00b7  */
    /* JADX WARN: Removed duplicated region for block: B:44:0x00b3  */
    /* JADX WARN: Removed duplicated region for block: B:45:0x00af  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public static LLSDContentType DetectContentType(BufferedInputStream bufferedInputStream, String str) throws IOException {
        bufferedInputStream.mark(64);
        byte[] bArr = {-17, -69, -65};
        byte[] bArr2 = new byte[32];
        int read = bufferedInputStream.read(bArr2, 0, bArr2.length);
        boolean z3 = false;
        if (read >= bArr.length) {
            z3 = true;
            for (int i2 = 0; i2 < read && i2 < bArr.length; i2++) {
                if (bArr2[i2] != bArr[i2]) {
                    z3 = false;
                    break;
                }
            }
        }
        int i = z3 ? bArr.length : 0;
        String str2 = read > i ? new String(bArr2, i, read - i, "UTF-8") : "";
        bufferedInputStream.reset();
        bufferedInputStream.skip(i);
        boolean z;
        boolean z2;
        if (str2.startsWith("<llsd>") || str2.startsWith("<?xml")) {
            z = true;
            z2 = false;
        } else if (str2.startsWith("<? LLSD/Binary ?>") || str2.startsWith("{") || str2.startsWith("<?llsd/binary")) {
            z = false;
            z2 = true;
        } else {
            z = false;
            z2 = false;
        }
        Object[] objArr = new Object[5];
        objArr[0] = str;
        objArr[1] = z2 ? "true" : "false";
        objArr[2] = z ? "true" : "false";
        objArr[3] = Integer.valueOf(i);
        objArr[4] = str2;
        Debug.Printf("LLSD: contentType '%s', detected binary %s, xml %s, skipBytes %d, firstString '%s'", objArr);
        if (z2) {
            z = true;
        }
        if (!z && str.equalsIgnoreCase("application/llsd+binary")) {
            z2 = true;
        }
        if (!z2) {
            Debug.Printf("LLSD: using XML parser", new Object[0]);
            return LLSDContentType.llsdXML;
        }
        Debug.Printf("LLSD: using binary parser", new Object[0]);
        return LLSDContentType.llsdBinary;
    }
}
