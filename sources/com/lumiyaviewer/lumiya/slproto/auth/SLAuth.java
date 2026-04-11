package com.lumiyaviewer.lumiya.slproto.auth;

import com.lumiyaviewer.lumiya.utils.HashUtils;
import java.io.IOException;

/* JADX INFO: loaded from: classes.dex */
public class SLAuth {

    private static class LoginRequestField {
        public final String name;
        public final String value;

        private LoginRequestField(String str, String str2) {
            this.name = str;
            this.value = str2;
        }

        /* synthetic */ LoginRequestField(String str, String str2, LoginRequestField loginRequestField) {
            this(str, str2);
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:64:0x0376  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    private com.lumiyaviewer.lumiya.slproto.auth.SLAuthReply SendLoginRequest(com.lumiyaviewer.lumiya.slproto.auth.SLAuthParams r12) throws java.io.IOException {
        /*
            Method dump skipped, instruction units count: 889
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lumiyaviewer.lumiya.slproto.auth.SLAuth.SendLoginRequest(com.lumiyaviewer.lumiya.slproto.auth.SLAuthParams):com.lumiyaviewer.lumiya.slproto.auth.SLAuthReply");
    }

    public static String getPasswordHash(String str) {
        String strTrim = str.trim();
        if (strTrim.length() > 16) {
            strTrim = strTrim.substring(0, 16);
        }
        return "$1$" + HashUtils.MD5_Hash(strTrim);
    }

    public SLAuthReply Login(SLAuthParams sLAuthParams) throws IOException {
        try {
            return SendLoginRequest(sLAuthParams);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("Failed to login to simulator");
        }
    }
}
