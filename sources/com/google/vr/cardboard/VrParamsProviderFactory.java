package com.google.vr.cardboard;

import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.os.Build;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/* JADX INFO: loaded from: classes.dex */
public final class VrParamsProviderFactory {
    private static final boolean DEBUG = false;
    private static final String TAG = VrParamsProviderFactory.class.getSimpleName();
    private static VrParamsProvider providerForTesting;

    public static class ContentProviderClientHandle {
        public final String authority;
        public final ContentProviderClient client;

        ContentProviderClientHandle(ContentProviderClient contentProviderClient, String str) {
            this.client = contentProviderClient;
            this.authority = str;
        }
    }

    public static VrParamsProvider create(Context context) {
        if (providerForTesting != null) {
            return providerForTesting;
        }
        ContentProviderClientHandle contentProviderClientHandleTryToGetContentProviderClientHandle = tryToGetContentProviderClientHandle(context);
        return contentProviderClientHandleTryToGetContentProviderClientHandle == null ? new LegacyVrParamsProvider() : new ContentProviderVrParamsProvider(contentProviderClientHandleTryToGetContentProviderClientHandle.client, contentProviderClientHandleTryToGetContentProviderClientHandle.authority);
    }

    private static List<String> getValidContentProviderAuthorities(Context context) {
        List<ResolveInfo> listQueryIntentContentProviders;
        if (Build.VERSION.SDK_INT < 19 || (listQueryIntentContentProviders = context.getPackageManager().queryIntentContentProviders(new Intent(VrSettingsProviderContract.PROVIDER_INTENT_ACTION), 0)) == null || listQueryIntentContentProviders.isEmpty()) {
            return null;
        }
        ArrayList arrayList = new ArrayList();
        Iterator<ResolveInfo> it = listQueryIntentContentProviders.iterator();
        while (it.hasNext()) {
            ProviderInfo providerInfo = it.next().providerInfo;
            if (PackageUtils.isGooglePackage(providerInfo.packageName)) {
                arrayList.add(providerInfo.authority);
            }
        }
        return arrayList;
    }

    public static boolean isContentProviderAvailable(Context context) {
        if (providerForTesting != null && (providerForTesting instanceof ContentProviderVrParamsProvider)) {
            return true;
        }
        List<String> validContentProviderAuthorities = getValidContentProviderAuthorities(context);
        return (validContentProviderAuthorities == null || validContentProviderAuthorities.isEmpty()) ? false : true;
    }

    public static void setProviderForTesting(VrParamsProvider vrParamsProvider) {
        providerForTesting = vrParamsProvider;
    }

    public static ContentProviderClientHandle tryToGetContentProviderClientHandle(Context context) {
        List<String> validContentProviderAuthorities = getValidContentProviderAuthorities(context);
        if (validContentProviderAuthorities != null) {
            for (String str : validContentProviderAuthorities) {
                ContentProviderClient contentProviderClientAcquireContentProviderClient = context.getContentResolver().acquireContentProviderClient(str);
                if (contentProviderClientAcquireContentProviderClient != null) {
                    return new ContentProviderClientHandle(contentProviderClientAcquireContentProviderClient, str);
                }
            }
        }
        return null;
    }
}
