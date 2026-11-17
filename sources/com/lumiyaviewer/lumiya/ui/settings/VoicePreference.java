package com.lumiyaviewer.lumiya.ui.settings;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.CheckBoxPreference;
import android.util.AttributeSet;
import com.lumiyaviewer.lumiya.GlobalOptions;
import com.lumiyaviewer.lumiya.R;

/**
 * Preference toggle for built-in WebRTC voice chat.
 */
public class VoicePreference extends CheckBoxPreference {
    private static final int REQUEST_RECORD_AUDIO = 101;

    public VoicePreference(Context context) {
        super(context);
        initVoicePrefCapability();
    }

    public VoicePreference(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        initVoicePrefCapability();
    }

    public VoicePreference(Context context, AttributeSet attributeSet, int defStyleAttr) {
        super(context, attributeSet, defStyleAttr);
        initVoicePrefCapability();
    }

    public VoicePreference(Context context, AttributeSet attributeSet, int defStyleAttr, int defStyleRes) {
        super(context, attributeSet, defStyleAttr, defStyleRes);
        initVoicePrefCapability();
    }

    private void initVoicePrefCapability() {
        setSummaryOn(R.string.voice_enabled_summary);
        setSummaryOff(R.string.voice_summary_off);
    }

    @Override
    protected void onClick() {
        super.onClick();
        boolean enabled = isChecked();
        GlobalOptions.getInstance().setVoiceEnabled(enabled);
        if (enabled) {
            requestRecordAudioIfNeeded(getContext());
        }
    }

    private static void requestRecordAudioIfNeeded(Context context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Activity activity = findActivity(context);
        if (activity != null) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO);
        }
    }

    private static Activity findActivity(Context context) {
        Context current = context;
        while (current instanceof ContextWrapper) {
            if (current instanceof Activity) {
                return (Activity) current;
            }
            current = ((ContextWrapper) current).getBaseContext();
        }
        return null;
    }
}
