package com.lumiyaviewer.lumiya.voice.model;

import javax.annotation.Nonnull;

/**
 * Snapshot of active voice audio routing and speaker volume properties.
 */
public class VoiceAudioProperties {
    @Nonnull
    public final VoiceBluetoothState bluetoothState;
    public final float speakerVolume;
    public final boolean speakerphoneOn;

    public VoiceAudioProperties(float speakerVolume, boolean speakerphoneOn, @Nonnull VoiceBluetoothState bluetoothState) {
        this.speakerVolume = speakerVolume;
        this.speakerphoneOn = speakerphoneOn;
        this.bluetoothState = bluetoothState;
    }
}
