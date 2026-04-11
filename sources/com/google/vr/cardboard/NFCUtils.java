package com.google.vr.cardboard;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.util.Log;

/* JADX INFO: loaded from: classes.dex */
public class NFCUtils {
    private static final String TAG = NFCUtils.class.getSimpleName();
    Context context;
    NfcAdapter nfcAdapter;
    BroadcastReceiver nfcBroadcastReceiver;
    IntentFilter[] nfcIntentFilters;

    private IntentFilter createNfcIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.nfc.action.NDEF_DISCOVERED");
        intentFilter.addAction("android.nfc.action.TECH_DISCOVERED");
        intentFilter.addAction("android.nfc.action.TAG_DISCOVERED");
        return intentFilter;
    }

    protected boolean isNFCEnabled() {
        return this.nfcAdapter != null && this.nfcAdapter.isEnabled();
    }

    public void onCreate(Activity activity) {
        this.context = activity.getApplicationContext();
        this.nfcAdapter = NfcAdapter.getDefaultAdapter(this.context);
        this.nfcBroadcastReceiver = new BroadcastReceiver() { // from class: com.google.vr.cardboard.NFCUtils.1
            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                Log.i(NFCUtils.TAG, "Got an NFC tag!");
                NFCUtils.this.onNFCTagDetected((Tag) intent.getParcelableExtra("android.nfc.extra.TAG"));
            }
        };
        IntentFilter intentFilterCreateNfcIntentFilter = createNfcIntentFilter();
        intentFilterCreateNfcIntentFilter.addDataScheme("cardboard");
        IntentFilter intentFilterCreateNfcIntentFilter2 = createNfcIntentFilter();
        intentFilterCreateNfcIntentFilter2.addDataScheme("http");
        intentFilterCreateNfcIntentFilter2.addDataAuthority("goo.gl", null);
        IntentFilter intentFilterCreateNfcIntentFilter3 = createNfcIntentFilter();
        intentFilterCreateNfcIntentFilter3.addDataScheme("http");
        intentFilterCreateNfcIntentFilter3.addDataAuthority("google.com", null);
        intentFilterCreateNfcIntentFilter3.addDataPath("/cardboard/cfg.*", 2);
        this.nfcIntentFilters = new IntentFilter[]{intentFilterCreateNfcIntentFilter, intentFilterCreateNfcIntentFilter2, intentFilterCreateNfcIntentFilter3};
    }

    protected void onNFCTagDetected(Tag tag) {
    }

    public void onPause(Activity activity) {
        if (isNFCEnabled()) {
            this.nfcAdapter.disableForegroundDispatch(activity);
        }
        activity.unregisterReceiver(this.nfcBroadcastReceiver);
    }

    public void onResume(Activity activity) {
        activity.registerReceiver(this.nfcBroadcastReceiver, createNfcIntentFilter());
        Intent intent = new Intent("android.nfc.action.NDEF_DISCOVERED");
        intent.setPackage(activity.getPackageName());
        PendingIntent broadcast = PendingIntent.getBroadcast(this.context, 0, intent, 0);
        if (isNFCEnabled()) {
            this.nfcAdapter.enableForegroundDispatch(activity, broadcast, this.nfcIntentFilters, null);
        }
    }
}
