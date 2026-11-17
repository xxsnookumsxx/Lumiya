package com.lumiyaviewer.lumiya.ui.settings;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Preference toggle for built-in voice chat referenced by preferences_chat.xml.
 */
public class VoiceEnablePreference extends VoicePreference {
    public VoiceEnablePreference(Context context) {
        super(context);
    }

    public VoiceEnablePreference(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public VoiceEnablePreference(Context context, AttributeSet attributeSet, int defStyleAttr) {
        super(context, attributeSet, defStyleAttr);
    }

    public VoiceEnablePreference(Context context, AttributeSet attributeSet, int defStyleAttr, int defStyleRes) {
        super(context, attributeSet, defStyleAttr, defStyleRes);
    }
}
