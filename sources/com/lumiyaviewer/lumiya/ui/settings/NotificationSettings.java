package com.lumiyaviewer.lumiya.ui.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.view.ViewCompat;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.lumiyaviewer.lumiya.R;
import com.lumiyaviewer.lumiya.ui.media.NotificationSounds;
import com.lumiyaviewer.lumiya.utils.LEDAction;

/* JADX INFO: loaded from: classes.dex */
public class NotificationSettings {
    private NotificationType type;
    private boolean notificationEnabled = false;
    private boolean soundEnabled = false;
    private String ringtone = "";
    private LEDAction blinkAction = LEDAction.None;
    private String blinkColor = "red";

    public NotificationSettings(NotificationType notificationType) {
        this.type = notificationType;
    }

    private int getPrefColor(String str) {
        if (str.length() != 6) {
            return 0;
        }
        try {
            return Integer.parseInt(str, 16) | ViewCompat.MEASURED_STATE_MASK;
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private String getPreferenceValueName(Context context, String str, int i, int i2) {
        String[] stringArray = context.getResources().getStringArray(i);
        String[] stringArray2 = context.getResources().getStringArray(i2);
        for (int i3 = 0; i3 < stringArray.length; i3++) {
            if (stringArray[i3].equals(str)) {
                return stringArray2[i3];
            }
        }
        return "";
    }

    public void Load(SharedPreferences sharedPreferences) {
        this.notificationEnabled = sharedPreferences.getBoolean(this.type.getEnableKey(), true);
        this.soundEnabled = sharedPreferences.getBoolean(this.type.getPlaySoundKey(), true);
        NotificationSounds notificationSounds = NotificationSounds.defaultSounds.get(this.type);
        this.ringtone = sharedPreferences.getString(this.type.getRingtoneKey(), notificationSounds != null ? notificationSounds.getUri().toString() : null);
        this.blinkAction = LEDAction.getByPreferenceString(sharedPreferences.getString(this.type.getBlinkKey(), "none"));
        this.blinkColor = sharedPreferences.getString(this.type.getBlinkColorKey(), "FF0000");
    }

    public LEDAction getLEDAction() {
        return this.blinkAction;
    }

    public int getLEDColor() {
        return getPrefColor(this.blinkColor);
    }

    public String getRingtone() {
        if (this.soundEnabled) {
            return this.ringtone;
        }
        return null;
    }

    String getSummary(Context context) {
        String title;
        if (this.ringtone != null) {
            Uri uri = Uri.parse(this.ringtone);
            NotificationSounds notificationSounds = NotificationSounds.defaultSounds.get(this.type);
            if (Objects.equal(notificationSounds != null ? notificationSounds.getUri() : null, uri)) {
                title = "Default";
            } else if (this.ringtone.isEmpty()) {
                title = "Silent";
            } else {
                Ringtone ringtone = RingtoneManager.getRingtone(context, uri);
                title = ringtone != null ? ringtone.getTitle(context) : "No sound selected";
            }
        } else {
            title = "Default";
        }
        String preferenceValueName = getPreferenceValueName(context, this.blinkColor, R.array.pref_led_color_values, R.array.pref_led_color);
        if (!this.notificationEnabled) {
            return "Do nothing";
        }
        String str = this.soundEnabled ? "Notify, play sound (" + title + ")" : "Notify";
        if (this.blinkAction != LEDAction.None) {
            return str + ", blink " + (!Strings.isNullOrEmpty(preferenceValueName) ? preferenceValueName.toLowerCase() + " " : "") + "LED";
        }
        return str;
    }

    public boolean isEnabled() {
        return this.notificationEnabled;
    }
}
