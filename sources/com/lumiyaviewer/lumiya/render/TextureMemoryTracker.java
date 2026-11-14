package com.lumiyaviewer.lumiya.render;

import android.app.ActivityManager;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.os.SystemClock;
import com.lumiyaviewer.lumiya.Debug;
import com.lumiyaviewer.lumiya.LumiyaApp;
import com.lumiyaviewer.lumiya.res.textures.TextureCache;
import com.lumiyaviewer.lumiya.slproto.avatar.SLMoveEvents;
import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.Nullable;

/* JADX INFO: loaded from: classes.dex */
public class TextureMemoryTracker {
    private static final int PAGE_SIZE = 4096;
    private static final int RELEASE_DELAY_FRAMES = 4;
    private static final long TEXTURE_MAX_RESERVED_MEMORY = 33554432L;
    private static final long DYNAMIC_MIN_LIMIT = 67108864L;
    private static final long DYNAMIC_MAX_LIMIT = 1073741824L;
    private static final long DYNAMIC_STEP_BYTES = 16777216L;
    private static final long DYNAMIC_UPDATE_INTERVAL_MS = 5000L;
    private static volatile long textureMemoryReserved = 33554432L;
    private static AtomicLong textureMemoryUsed = new AtomicLong(0L);
    private static AtomicLong openJpegMemoryUsed = new AtomicLong(0L);
    private static AtomicLong openJpegMemoryMmapped = new AtomicLong(0L);
    private static AtomicLong textureMemoryLimit = new AtomicLong(SLMoveEvents.AGENT_CONTROL_TURN_RIGHT);
    private static AtomicBoolean dynamicLimitEnabled = new AtomicBoolean(false);
    private static volatile long lastDynamicUpdateMs = 0L;
    private static AtomicBoolean inflightLowMemory = new AtomicBoolean(false);
    private static AtomicLong bufMemory = new AtomicLong(0L);
    private static final AtomicLong[] delayedRelease = new AtomicLong[4];
    private static final AtomicLong[] delayedReleaseBuf = new AtomicLong[4];
    private static final AtomicInteger delayedReleaseIndex = new AtomicInteger(0);
    private static AtomicBoolean stalled = new AtomicBoolean(false);
    private static final Object rendererLock = new Object();

    @Nullable
    private static WeakReference<Object> activeRendererRef = null;

    static {
        for (int i = 0; i < 4; i++) {
            delayedRelease[i] = new AtomicLong(0L);
            delayedReleaseBuf[i] = new AtomicLong(0L);
        }
    }

    public static long actualSize(int i) {
        return ((((long) i + 4096L) - 1L) / 4096L) * 4096L;
    }

    public static void allocBufferMemory(int i) {
        bufMemory.addAndGet(actualSize(i));
        printStats();
    }

    public static void allocOpenJpegMemory(int i, boolean z) {
        long jActualSize = actualSize(i);
        if (z) {
            openJpegMemoryMmapped.addAndGet(jActualSize);
        }
        openJpegMemoryUsed.addAndGet(jActualSize);
        updateInflightMemoryLow();
        printStats();
    }

    public static void allocTextureMemory(int i) {
        textureMemoryUsed.addAndGet(actualSize(i));
        updateInflightMemoryLow();
        printStats();
    }

    public static boolean canAllocateMemory(int i) {
        return !stalled.get() && ((textureMemoryUsed.get() + bufMemory.get()) + actualSize(i)) + textureMemoryReserved < textureMemoryLimit.get();
    }

    public static void clearActiveRenderer(Object obj) {
        synchronized (rendererLock) {
            Object obj2 = activeRendererRef != null ? activeRendererRef.get() : null;
            if (obj2 == obj || obj2 == null) {
                activeRendererRef = null;
            }
        }
    }

    public static boolean hasActiveRenderer() {
        boolean z;
        synchronized (rendererLock) {
            z = (activeRendererRef != null ? activeRendererRef.get() : null) != null;
        }
        return z;
    }

    private static volatile long lastStatsPrintMs = 0L;

    private static void printStats() {
        if (Debug.isDebugBuild()) {
            long now = android.os.SystemClock.uptimeMillis();
            if (now - lastStatsPrintMs >= 5000L) {
                lastStatsPrintMs = now;
                Debug.Printf("Texture mem used: %d Mb oj %d mmap %d tot %d limit %d Mb%s buf %dk",
                        Long.valueOf(textureMemoryUsed.get() / 1048576L),
                        Long.valueOf(openJpegMemoryUsed.get() / 1048576L),
                        Long.valueOf(openJpegMemoryMmapped.get() / 1048576L),
                        Long.valueOf((textureMemoryUsed.get() + openJpegMemoryUsed.get()) / 1048576L),
                        Long.valueOf(textureMemoryLimit.get() / 1048576L),
                        dynamicLimitEnabled.get() ? " (auto)" : "",
                        Long.valueOf(bufMemory.get() / 1024L));
            }
        }
    }

    public static void releaseAllFrameMemory() {
        for (int i = 0; i < 4; i++) {
            delayedRelease[i].set(0);
            delayedReleaseBuf[i].set(0);
        }
        delayedReleaseIndex.set(0);
    }

    public static void releaseAllGLMemory() {
        textureMemoryUsed.set(0L);
        bufMemory.set(0L);
        stalled.set(false);
        printStats();
    }

    public static void releaseBufferMemory(int i) {
        delayedReleaseBuf[delayedReleaseIndex.get() & 3].addAndGet(actualSize(i));
    }

    public static void releaseFrameMemory() {
        updateDynamicMemoryLimitIfNeeded();
        boolean z;
        int nextIndex = (delayedReleaseIndex.get() + 1) & 3;
        delayedReleaseIndex.set(nextIndex);
        long andSet = delayedRelease[nextIndex].getAndSet(0L);
        long andSet2 = delayedReleaseBuf[nextIndex].getAndSet(0L);
        if (andSet != 0L) {
            textureMemoryUsed.addAndGet(-andSet);
            z = true;
        } else {
            z = false;
        }
        if (andSet2 != 0L) {
            bufMemory.addAndGet(-andSet2);
            z = true;
        }
        if (z) {
            updateInflightMemoryLow();
            printStats();
            stalled.set(false);
        }
    }

    public static void releaseOpenJpegMemory(int i, boolean z) {
        long jActualSize = actualSize(i);
        if (z) {
            openJpegMemoryMmapped.addAndGet(-jActualSize);
        }
        openJpegMemoryUsed.addAndGet(-jActualSize);
        updateInflightMemoryLow();
    }

    public static void releaseTextureMemory(int i) {
        delayedRelease[delayedReleaseIndex.get() & 3].addAndGet(actualSize(i));
    }

    public static void setActiveRenderer(Object obj) {
        synchronized (rendererLock) {
            activeRendererRef = new WeakReference<>(obj);
        }
    }

    private static void setInflightMemoryLow(boolean z) {
        if (inflightLowMemory.getAndSet(z) != z) {
            TextureCache.getInstance().setTextureMemoryState(z);
        }
    }

    public static void setMemoryLimit(int i) {
        setMemoryLimit((long) i);
    }

    public static void setMemoryLimit(long j) {
        long clamped = Math.max(16777216L, j);
        textureMemoryLimit.set(clamped);
        long reserved = clamped / 4L;
        textureMemoryReserved = reserved <= TEXTURE_MAX_RESERVED_MEMORY ? reserved : TEXTURE_MAX_RESERVED_MEMORY;
        updateInflightMemoryLow();
    }

    public static void setDynamicMemoryLimit(boolean z) {
        dynamicLimitEnabled.set(z);
        if (z) {
            lastDynamicUpdateMs = 0L;
            refreshDynamicMemoryLimit(false);
        }
    }

    public static void updateDynamicMemoryLimitIfNeeded() {
        if (!dynamicLimitEnabled.get()) {
            return;
        }
        long now = SystemClock.uptimeMillis();
        if (now - lastDynamicUpdateMs >= DYNAMIC_UPDATE_INTERVAL_MS) {
            lastDynamicUpdateMs = now;
            refreshDynamicMemoryLimit(false);
        }
    }

    public static void onMemoryPressure(int level) {
        boolean severe = level == ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL || level >= ComponentCallbacks2.TRIM_MEMORY_COMPLETE;
        boolean moderate = level == ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW || level >= ComponentCallbacks2.TRIM_MEMORY_MODERATE;
        if (!severe && !moderate) {
            return;
        }
        if (dynamicLimitEnabled.get()) {
            lastDynamicUpdateMs = SystemClock.uptimeMillis();
            refreshDynamicMemoryLimit(severe);
        } else if (severe) {
            setInflightMemoryLow(true);
        }
    }

    private static void refreshDynamicMemoryLimit(boolean forceShrink) {
        Context context = LumiyaApp.getContext();
        if (context == null) {
            return;
        }
        try {
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            if (am == null) {
                return;
            }
            ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
            am.getMemoryInfo(mi);
            long totalMem = mi.totalMem > 0L ? mi.totalMem : 536870912L;
            long baseline = Math.max(DYNAMIC_MIN_LIMIT, Math.min(DYNAMIC_MAX_LIMIT, totalMem / 4L));
            long target;
            if (forceShrink || mi.lowMemory || mi.availMem <= mi.threshold * 2L) {
                long used = textureMemoryUsed.get() + bufMemory.get();
                long cap = forceShrink ? DYNAMIC_MIN_LIMIT : 268435456L;
                target = Math.max(DYNAMIC_MIN_LIMIT, Math.min(cap, Math.min(used + 33554432L, baseline / 2L)));
            } else {
                long headroom = Math.max(DYNAMIC_MIN_LIMIT, (mi.availMem - mi.threshold) / 2L);
                target = Math.max(DYNAMIC_MIN_LIMIT, Math.min(DYNAMIC_MAX_LIMIT, Math.min(baseline, headroom)));
            }
            target = (target / DYNAMIC_STEP_BYTES) * DYNAMIC_STEP_BYTES;
            if (target < DYNAMIC_MIN_LIMIT) {
                target = DYNAMIC_MIN_LIMIT;
            }
            long current = textureMemoryLimit.get();
            if (forceShrink || Math.abs(target - current) >= DYNAMIC_STEP_BYTES) {
                setMemoryLimit(target);
                Debug.Printf("TextureMemoryTracker: dynamic limit %d Mb (avail %d Mb / total %d Mb, low=%b)",
                        Long.valueOf(target / 1048576L),
                        Long.valueOf(mi.availMem / 1048576L),
                        Long.valueOf(totalMem / 1048576L),
                        Boolean.valueOf(mi.lowMemory));
            }
        } catch (Exception e) {
            Debug.Warning(e);
        }
    }

    public static void stall() {
        stalled.set(true);
    }

    private static void updateInflightMemoryLow() {
        setInflightMemoryLow(textureMemoryUsed.get() + openJpegMemoryUsed.get() >= (textureMemoryLimit.get() * 3L) / 2L);
    }
}
