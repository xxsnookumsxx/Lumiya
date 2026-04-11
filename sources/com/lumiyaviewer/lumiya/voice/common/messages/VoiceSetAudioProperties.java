package com.lumiyaviewer.lumiya.voice.common.messages;

import android.os.Bundle;
import com.lumiyaviewer.lumiya.voice.common.VoicePluginMessage;
import com.lumiyaviewer.lumiya.voice.common.model.VoiceAudioDevice;
import javax.annotation.Nullable;

/* JADX INFO: loaded from: classes.dex */
public class VoiceSetAudioProperties implements VoicePluginMessage {

    @Nullable
    public final VoiceAudioDevice audioDevice;
    public final float speakerVolume;
    public final boolean speakerVolumeValid;

    public VoiceSetAudioProperties(float f, boolean z, @Nullable VoiceAudioDevice voiceAudioDevice) {
        this.speakerVolume = f;
        this.speakerVolumeValid = z;
        this.audioDevice = voiceAudioDevice;
    }

    public VoiceSetAudioProperties(Bundle bundle) {
        VoiceAudioDevice voiceAudioDeviceValueOf;
        this.speakerVolumeValid = bundle.containsKey("speakerVolume");
        this.speakerVolume = !this.speakerVolumeValid ? Float.NaN : bundle.getFloat("speakerVolume");
        if (bundle.containsKey("audioDevice")) {
            try {
                voiceAudioDeviceValueOf = VoiceAudioDevice.valueOf(bundle.getString("audioDevice"));
            } catch (IllegalArgumentException e) {
                voiceAudioDeviceValueOf = null;
            }
        } else {
            voiceAudioDeviceValueOf = null;
        }
        this.audioDevice = voiceAudioDeviceValueOf;
    }

    @Override // com.lumiyaviewer.lumiya.voice.common.VoicePluginMessage
    public Bundle toBundle() {
        Bundle bundle = new Bundle();
        if (this.speakerVolumeValid) {
            bundle.putFloat("speakerVolume", this.speakerVolume);
        }
        if (this.audioDevice != null) {
            bundle.putString("audioDevice", this.audioDevice.name());
        }
        return bundle;
    }
}
