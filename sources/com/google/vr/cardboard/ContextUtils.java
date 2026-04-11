package com.google.vr.cardboard;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;

/* JADX INFO: loaded from: classes.dex */
public class ContextUtils {
    public static boolean canGetActivity(Context context) {
        return getActivity(context) != null;
    }

    public static Activity getActivity(Context context) {
        for (Context baseContext = context; baseContext != null; baseContext = ((ContextWrapper) baseContext).getBaseContext()) {
            if (baseContext instanceof Activity) {
                return (Activity) baseContext;
            }
            if (!(baseContext instanceof ContextWrapper)) {
                return null;
            }
        }
        return null;
    }
}
