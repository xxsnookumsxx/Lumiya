package com.lumiyaviewer.lumiya.voice.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import com.lumiyaviewer.lumiya.Debug;
import com.lumiyaviewer.lumiya.slproto.users.manager.UserManager;
import com.lumiyaviewer.lumiya.voice.model.VoiceAudioDevice;
import com.lumiyaviewer.lumiya.voice.model.VoiceAudioProperties;
import com.lumiyaviewer.lumiya.voice.model.VoiceBluetoothState;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Routes voice audio between earpiece, loudspeaker, and Bluetooth SCO,
 * and publishes {@link VoiceAudioProperties} updates to {@link UserManager}.
 */
public class VoiceAudioRouter {
    private final Context context;
    @Nullable
    private final UserManager userManager;
    @Nullable
    private final AudioManager audioManager;

    private boolean active = false;
    private boolean receiverRegistered = false;
    private int savedAudioMode = AudioManager.MODE_NORMAL;
    private boolean savedSpeakerphoneOn = false;
    private boolean speakerphoneOn = false;
    private float speakerVolume = 1.0f;
    @Nonnull
    private VoiceBluetoothState bluetoothState = VoiceBluetoothState.Disconnected;

    private final BroadcastReceiver scoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context ctx, Intent intent) {
            if (intent == null || !AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED.equals(intent.getAction())) {
                return;
            }
            int state = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, AudioManager.SCO_AUDIO_STATE_ERROR);
            switch (state) {
                case AudioManager.SCO_AUDIO_STATE_CONNECTED:
                    bluetoothState = VoiceBluetoothState.Active;
                    break;
                case AudioManager.SCO_AUDIO_STATE_CONNECTING:
                    bluetoothState = VoiceBluetoothState.Connecting;
                    break;
                case AudioManager.SCO_AUDIO_STATE_DISCONNECTED:
                    bluetoothState = VoiceBluetoothState.Disconnected;
                    break;
                case AudioManager.SCO_AUDIO_STATE_ERROR:
                default:
                    bluetoothState = VoiceBluetoothState.Error;
                    break;
            }
            publishProperties();
        }
    };

    public VoiceAudioRouter(@Nonnull Context context, @Nullable UserManager userManager) {
        this.context = context.getApplicationContext();
        this.userManager = userManager;
        this.audioManager = (AudioManager) this.context.getSystemService(Context.AUDIO_SERVICE);
        readInitialVolume();
        publishProperties();
    }

    private void readInitialVolume() {
        if (audioManager == null) {
            return;
        }
        int max = audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
        int cur = audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
        if (max > 0) {
            speakerVolume = Math.max(0.0f, Math.min(1.0f, (float) cur / (float) max));
        }
    }

    public synchronized void activate() {
        if (active || audioManager == null) {
            return;
        }
        try {
            savedAudioMode = audioManager.getMode();
            savedSpeakerphoneOn = audioManager.isSpeakerphoneOn();
            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
            audioManager.setSpeakerphoneOn(speakerphoneOn);
            if (!receiverRegistered) {
                context.registerReceiver(scoReceiver, new IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED));
                receiverRegistered = true;
            }
            active = true;
            readInitialVolume();
            publishProperties();
        } catch (Exception e) {
            Debug.Warning(e);
        }
    }

    public synchronized void deactivate() {
        if (!active) {
            return;
        }
        active = false;
        if (audioManager != null) {
            try {
                if (audioManager.isBluetoothScoOn()) {
                    audioManager.setBluetoothScoOn(false);
                    audioManager.stopBluetoothSco();
                }
                audioManager.setSpeakerphoneOn(savedSpeakerphoneOn);
                audioManager.setMode(savedAudioMode);
            } catch (Exception e) {
                Debug.Warning(e);
            }
        }
        speakerphoneOn = false;
        bluetoothState = VoiceBluetoothState.Disconnected;
        unregisterReceiverSafe();
        publishProperties();
    }

    public synchronized void release() {
        deactivate();
        unregisterReceiverSafe();
    }

    private void unregisterReceiverSafe() {
        if (receiverRegistered) {
            try {
                context.unregisterReceiver(scoReceiver);
            } catch (IllegalArgumentException ignored) {
            }
            receiverRegistered = false;
        }
    }

    public synchronized void setAudioDevice(@Nonnull VoiceAudioDevice device) {
        if (audioManager == null) {
            return;
        }
        try {
            switch (device) {
                case Loudspeaker:
                    stopBluetoothScoSafe();
                    speakerphoneOn = true;
                    audioManager.setSpeakerphoneOn(true);
                    break;
                case Bluetooth:
                    speakerphoneOn = false;
                    audioManager.setSpeakerphoneOn(false);
                    if (audioManager.isBluetoothScoAvailableOffCall()) {
                        bluetoothState = VoiceBluetoothState.Connecting;
                        audioManager.startBluetoothSco();
                        audioManager.setBluetoothScoOn(true);
                    } else {
                        bluetoothState = VoiceBluetoothState.Error;
                    }
                    break;
                case Default:
                default:
                    stopBluetoothScoSafe();
                    speakerphoneOn = false;
                    audioManager.setSpeakerphoneOn(false);
                    break;
            }
        } catch (Exception e) {
            Debug.Warning(e);
            bluetoothState = VoiceBluetoothState.Error;
        }
        publishProperties();
    }

    private void stopBluetoothScoSafe() {
        if (audioManager == null) {
            return;
        }
        if (audioManager.isBluetoothScoOn() || bluetoothState == VoiceBluetoothState.Active || bluetoothState == VoiceBluetoothState.Connecting) {
            try {
                audioManager.setBluetoothScoOn(false);
                audioManager.stopBluetoothSco();
            } catch (Exception e) {
                Debug.Warning(e);
            }
        }
        bluetoothState = VoiceBluetoothState.Disconnected;
    }

    public synchronized void setSpeakerVolume(float volume) {
        float clamped = Math.max(0.0f, Math.min(1.0f, volume));
        speakerVolume = clamped;
        if (audioManager != null) {
            try {
                int max = audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
                if (max > 0) {
                    int target = Math.round(clamped * max);
                    audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, target, 0);
                }
            } catch (Exception e) {
                Debug.Warning(e);
            }
        }
        publishProperties();
    }

    public void publishProperties() {
        if (userManager != null) {
            userManager.setVoiceAudioProperties(new VoiceAudioProperties(speakerVolume, speakerphoneOn, bluetoothState));
        }
    }
}
