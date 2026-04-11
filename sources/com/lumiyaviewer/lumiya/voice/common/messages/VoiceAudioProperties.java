package com.lumiyaviewer.lumiya.voice.common.messages;

import android.os.Bundle;
import com.lumiyaviewer.lumiya.voice.common.VoicePluginMessage;
import com.lumiyaviewer.lumiya.voice.common.model.VoiceBluetoothState;
import javax.annotation.Nonnull;

/* JADX INFO: loaded from: classes.dex */
public class VoiceAudioProperties implements VoicePluginMessage {

    @Nonnull
    public final VoiceBluetoothState bluetoothState;
    public final float speakerVolume;
    public final boolean speakerphoneOn;

    public VoiceAudioProperties(float f, boolean z, @Nonnull VoiceBluetoothState voiceBluetoothState) {
        this.speakerVolume = f;
        this.speakerphoneOn = z;
        this.bluetoothState = voiceBluetoothState;
    }

    public VoiceAudioProperties(Bundle bundle) {
        VoiceBluetoothState voiceBluetoothStateValueOf;
        this.speakerVolume = bundle.getFloat("speakerVolume");
        this.speakerphoneOn = bundle.getBoolean("speakerphoneOn");
        try {
            voiceBluetoothStateValueOf = VoiceBluetoothState.valueOf(bundle.getString("bluetoothState"));
        } catch (IllegalArgumentException e) {
            voiceBluetoothStateValueOf = VoiceBluetoothState.Error;
        }
        this.bluetoothState = voiceBluetoothStateValueOf;
    }

    @Override // com.lumiyaviewer.lumiya.voice.common.VoicePluginMessage
    public Bundle toBundle() {
        Bundle bundle = new Bundle();
        bundle.putFloat("speakerVolume", this.speakerVolume);
        bundle.putBoolean("speakerphoneOn", this.speakerphoneOn);
        bundle.putString("bluetoothState", this.bluetoothState.name());
        return bundle;
    }
}
