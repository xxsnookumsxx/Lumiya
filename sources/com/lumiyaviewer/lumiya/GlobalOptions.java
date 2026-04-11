package com.lumiyaviewer.lumiya;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.lumiyaviewer.lumiya.eventbus.EventBus;
import com.lumiyaviewer.lumiya.render.TextureMemoryTracker;
import com.lumiyaviewer.lumiya.res.textures.TextureCache;
import com.lumiyaviewer.lumiya.ui.media.NotificationSounds;
import com.lumiyaviewer.lumiya.ui.settings.NotificationType;
import com.lumiyaviewer.lumiya.ui.settings.ThemeChangedEvent;
import com.lumiyaviewer.lumiya.voiceintf.VoicePluginServiceConnection;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/* JADX INFO: loaded from: classes.dex */
public class GlobalOptions implements SharedPreferences.OnSharedPreferenceChangeListener {
    private int themeResourceId = R.style.Theme_Lumiya_Light;
    private boolean legacyUserNames = false;
    private boolean showTimestamps = true;
    private boolean highQualityTextures = false;
    private boolean compressedTextures = true;
    private int maxTextureDownloads = 2;
    private boolean terrainTextures = true;
    private MeshRendering meshRendering = MeshRendering.medium;
    private boolean RLVEnabled = false;
    private boolean keepWifiOn = false;
    private final AtomicReference<File> baseCacheDir = new AtomicReference<>();
    private final AtomicReference<ImmutableList<File>> availableCacheDirs = new AtomicReference<>(ImmutableList.of());
    private final AtomicBoolean cacheDirUsed = new AtomicBoolean(false);
    private boolean autoReconnect = true;
    private int maxReconnectAttempts = 10;
    private boolean hoverTextEnableHUDs = true;
    private boolean hoverTextEnableObjects = false;
    private boolean advancedRendering = true;
    private boolean useFXAA = false;
    private boolean renderClouds = true;
    private boolean forceDaylightTime = false;
    private float forceDaylightHour = 0.5f;
    private boolean cloudSyncEnabled = false;
    private boolean voiceEnabled = false;

    public static class GlobalOptionsChangedEvent {
        public final SharedPreferences preferences;

        public GlobalOptionsChangedEvent(SharedPreferences sharedPreferences) {
            this.preferences = sharedPreferences;
        }
    }

    private static class InstanceHolder {
        private static final GlobalOptions Instance = new GlobalOptions();

        private InstanceHolder() {
        }
    }

    public enum MeshRendering {
        high("high_lod"),
        medium("medium_lod"),
        low("low_lod"),
        lowest("lowest_lod"),
        disabled(null);

        private String lodName;

        MeshRendering(String str) {
            this.lodName = str;
        }

        /* JADX INFO: renamed from: values, reason: to resolve conflict with enum method */
        public static MeshRendering[] valuesCustom() {
            return values();
        }

        public String getLODName() {
            return this.lodName;
        }
    }

    public static GlobalOptions getInstance() {
        return InstanceHolder.Instance;
    }

    private static long getTotalMemory() {
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader("/proc/meminfo"), 8192);
            while (true) {
                String line = bufferedReader.readLine();
                if (line == null) {
                    break;
                }
                if (line.startsWith("MemTotal:")) {
                    String[] strArrSplit = line.split("\\s+");
                    j = strArrSplit.length >= 2 ? Long.parseLong(strArrSplit[1]) : 0L;
                    for (int i = 0; i < strArrSplit.length; i++) {
                        Debug.Log("Memory " + i + ":" + strArrSplit[i]);
                    }
                }
            }
            bufferedReader.close();
        } catch (Exception e) {
        }
        return j;
    }

    private boolean isCacheDirectoryWriteable(@Nullable File file) {
        if (file == null) {
            return false;
        }
        try {
            file.mkdirs();
            if (!file.exists()) {
                return false;
            }
            File file2 = new File(file, ".tmp");
            if (file2.exists()) {
                file2.delete();
                if (file2.exists()) {
                    return false;
                }
            }
            file2.createNewFile();
            if (!file2.exists()) {
                return false;
            }
            file2.delete();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Removed duplicated region for block: B:46:0x00d7  */
    /* JADX WARN: Type inference failed for: r0v2, types: [java.io.File] */
    /* JADX WARN: Type inference failed for: r10v0, types: [com.lumiyaviewer.lumiya.GlobalOptions] */
    /* JADX WARN: Type inference failed for: r1v0 */
    /* JADX WARN: Type inference failed for: r1v1 */
    /* JADX WARN: Type inference failed for: r1v12, types: [java.io.File, java.lang.Object] */
    /* JADX WARN: Type inference failed for: r1v13 */
    /* JADX WARN: Type inference failed for: r1v14 */
    /* JADX WARN: Type inference failed for: r1v15 */
    /* JADX WARN: Type inference failed for: r1v16 */
    /* JADX WARN: Type inference failed for: r1v17 */
    /* JADX WARN: Type inference failed for: r1v18 */
    /* JADX WARN: Type inference failed for: r1v19 */
    /* JADX WARN: Type inference failed for: r1v2 */
    /* JADX WARN: Type inference failed for: r1v20 */
    /* JADX WARN: Type inference failed for: r2v2 */
    /* JADX WARN: Type inference failed for: r2v3, types: [java.io.File, java.lang.Object] */
    /* JADX WARN: Type inference failed for: r2v4, types: [java.io.File] */
    /* JADX WARN: Type inference failed for: r8v0, types: [java.lang.Object[]] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    private void updateCacheDir(android.content.Context r11, android.content.SharedPreferences r12) {
        /*
            Method dump skipped, instruction units count: 217
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lumiyaviewer.lumiya.GlobalOptions.updateCacheDir(android.content.Context, android.content.SharedPreferences):void");
    }

    private void updateNotificationSoundDefault(SharedPreferences sharedPreferences, NotificationType notificationType) {
        NotificationSounds notificationSounds;
        if (sharedPreferences.contains(notificationType.getRingtoneKey()) || (notificationSounds = NotificationSounds.defaultSounds.get(notificationType)) == null) {
            return;
        }
        Uri uri = notificationSounds.getUri();
        SharedPreferences.Editor editorEdit = sharedPreferences.edit();
        editorEdit.putString(notificationType.getRingtoneKey(), uri.toString());
        editorEdit.apply();
        Debug.Printf("NotificationSounds: Updated %s preference to %s", notificationType.getRingtoneKey(), uri);
    }

    public void enableVoice() {
        SharedPreferences.Editor editorEdit = LumiyaApp.getDefaultSharedPreferences().edit();
        editorEdit.putBoolean("enableVoice", true);
        editorEdit.apply();
    }

    public boolean getAdvancedRendering() {
        return this.advancedRendering;
    }

    public final boolean getAutoReconnect() {
        return this.autoReconnect;
    }

    public ImmutableList<File> getAvailableCacheDirs() {
        return this.availableCacheDirs.get();
    }

    public File getBaseCacheDir() {
        return this.baseCacheDir.get();
    }

    public final File getCacheDir(@Nonnull String str) {
        this.cacheDirUsed.set(true);
        File cacheDir = this.baseCacheDir.get();
        if (cacheDir == null) {
            cacheDir = LumiyaApp.getContext().getCacheDir();
        }
        File file = new File(cacheDir, str);
        try {
            file.mkdirs();
        } catch (Exception e) {
        }
        return file;
    }

    public final boolean getCompressedTextures() {
        return this.compressedTextures;
    }

    public float getForceDaylightHour() {
        return this.forceDaylightHour;
    }

    public boolean getForceDaylightTime() {
        return this.forceDaylightTime;
    }

    public final boolean getHighQualityTextures() {
        return this.highQualityTextures;
    }

    public final boolean getHoverTextEnableHUDs() {
        return this.hoverTextEnableHUDs;
    }

    public final boolean getHoverTextEnableObjects() {
        return this.hoverTextEnableObjects;
    }

    public boolean getKeepWifiOn() {
        return this.keepWifiOn;
    }

    public final int getMaxReconnectAttempts() {
        return this.maxReconnectAttempts;
    }

    public final int getMaxTextureDownloads() {
        return this.maxTextureDownloads;
    }

    public final MeshRendering getMeshRendering() {
        return this.meshRendering;
    }

    public final boolean getRLVEnabled() {
        return this.RLVEnabled;
    }

    public boolean getRenderClouds() {
        return this.renderClouds;
    }

    public boolean getShowTimestamps() {
        return this.showTimestamps;
    }

    public final boolean getTerrainTextures() {
        return this.terrainTextures;
    }

    public int getThemeResourceId() {
        return this.themeResourceId;
    }

    public boolean getUseFXAA() {
        return this.useFXAA;
    }

    public boolean getVoiceEnabled() {
        return this.voiceEnabled;
    }

    public void initialize() {
        SharedPreferences defaultSharedPreferences = LumiyaApp.getDefaultSharedPreferences();
        updateFromPreferences(LumiyaApp.getContext(), defaultSharedPreferences);
        defaultSharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    public boolean isCacheDirUsed() {
        return this.cacheDirUsed.get();
    }

    public boolean isLegacyUserNames() {
        return this.legacyUserNames;
    }

    @Override // android.content.SharedPreferences.OnSharedPreferenceChangeListener
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String str) {
        updateFromPreferences(LumiyaApp.getContext(), sharedPreferences);
        EventBus.getInstance().publish(new GlobalOptionsChangedEvent(sharedPreferences));
    }

    public void updateFromPreferences(Context context, SharedPreferences sharedPreferences) {
        int i;
        Debug.Printf("Updating options from preferences.", new Object[0]);
        updateNotificationSoundDefault(sharedPreferences, NotificationType.Private);
        updateNotificationSoundDefault(sharedPreferences, NotificationType.Group);
        updateNotificationSoundDefault(sharedPreferences, NotificationType.LocalChat);
        if (!sharedPreferences.getBoolean("system_defaults_set", false)) {
            SharedPreferences.Editor editorEdit = sharedPreferences.edit();
            long totalMemory = getTotalMemory();
            int iAvailableProcessors = Runtime.getRuntime().availableProcessors();
            if (iAvailableProcessors < 2 || totalMemory <= 524288) {
                editorEdit.putBoolean("high_quality_textures", false);
            } else {
                editorEdit.putBoolean("high_quality_textures", true);
            }
            editorEdit.putString("texture_memory_limit", Integer.toString(totalMemory != 0 ? totalMemory <= 262144 ? 32 : totalMemory <= 524288 ? 64 : 128 : 64));
            int i2 = 2;
            if (iAvailableProcessors >= 4 && totalMemory > 524288) {
                i2 = 8;
            } else if (iAvailableProcessors >= 2) {
                i2 = 4;
            }
            editorEdit.putString("max_texture_downloads", Integer.toString(i2));
            editorEdit.putBoolean("system_defaults_set", true);
            editorEdit.commit();
        }
        int i3 = this.themeResourceId;
        String strNullToEmpty = Strings.nullToEmpty(sharedPreferences.getString("theme", "light"));
        if (strNullToEmpty.equals("dark")) {
            this.themeResourceId = R.style.Theme_Lumiya;
        } else if (strNullToEmpty.equals("pink")) {
            this.themeResourceId = R.style.Theme_Lumiya_Pink;
        } else {
            this.themeResourceId = R.style.Theme_Lumiya_Light;
        }
        this.legacyUserNames = sharedPreferences.getBoolean("legacyUserNames", false);
        this.showTimestamps = sharedPreferences.getBoolean("chatTimestamps", true);
        this.highQualityTextures = sharedPreferences.getBoolean("high_quality_textures", false);
        this.compressedTextures = sharedPreferences.getBoolean("compressed_textures", true);
        this.keepWifiOn = sharedPreferences.getBoolean("keep_wifi_on", true);
        this.cloudSyncEnabled = sharedPreferences.getBoolean("sync_to_gdrive", false);
        this.voiceEnabled = sharedPreferences.getBoolean("enableVoice", false) ? VoicePluginServiceConnection.isPluginSupported() : false;
        try {
            i = Integer.parseInt(sharedPreferences.getString("max_texture_downloads", "2"));
            if (i < 1) {
                i = 1;
            }
        } catch (Exception e) {
            i = 2;
        }
        if (i != this.maxTextureDownloads) {
            this.maxTextureDownloads = i;
            TextureCache.getInstance().setMaxTextureDownloads(this.maxTextureDownloads);
        }
        this.terrainTextures = sharedPreferences.getBoolean("terrain_textures", true);
        int i4 = 64;
        try {
            i4 = Integer.parseInt(sharedPreferences.getString("texture_memory_limit", "64"));
        } catch (Exception e2) {
        }
        try {
            this.meshRendering = MeshRendering.valueOf(sharedPreferences.getString("mesh_rendering", "high"));
        } catch (Exception e3) {
        }
        TextureMemoryTracker.setMemoryLimit(i4 * 1024 * 1024);
        this.RLVEnabled = sharedPreferences.getBoolean("rlv_enabled", false);
        this.autoReconnect = sharedPreferences.getBoolean("auto_reconnect", true);
        try {
            this.maxReconnectAttempts = Integer.parseInt(sharedPreferences.getString("reconnect_attempts", "10"));
        } catch (Exception e4) {
        }
        updateCacheDir(context, sharedPreferences);
        String string = sharedPreferences.getString("hover_text", "huds");
        if (string.equals("all")) {
            this.hoverTextEnableHUDs = true;
            this.hoverTextEnableObjects = true;
        } else if (string.equals("none")) {
            this.hoverTextEnableHUDs = false;
            this.hoverTextEnableObjects = false;
        } else {
            this.hoverTextEnableHUDs = true;
            this.hoverTextEnableObjects = false;
        }
        this.advancedRendering = sharedPreferences.getBoolean("advanced_rendering", true);
        this.useFXAA = sharedPreferences.getBoolean("fxaa_enable", false);
        this.renderClouds = sharedPreferences.getBoolean("clouds_enable", true);
        String string2 = sharedPreferences.getString("render_time_of_day", "sim");
        if (string2.equalsIgnoreCase("sim")) {
            this.forceDaylightTime = false;
            this.forceDaylightHour = 0.5f;
        } else {
            try {
                this.forceDaylightTime = true;
                this.forceDaylightHour = Float.parseFloat(string2);
            } catch (Exception e5) {
                this.forceDaylightTime = false;
                this.forceDaylightHour = 0.5f;
            }
        }
        if (i3 != this.themeResourceId) {
            EventBus.getInstance().publish(new ThemeChangedEvent(this.themeResourceId));
        }
    }
}
