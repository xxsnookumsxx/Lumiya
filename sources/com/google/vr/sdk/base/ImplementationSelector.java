package com.google.vr.sdk.base;

import android.content.Context;

/* JADX INFO: loaded from: classes.dex */
public class ImplementationSelector {
    public static CardboardViewApi createCardboardViewApi(Context context) {
        return new CardboardViewNativeImpl(context);
    }
}
