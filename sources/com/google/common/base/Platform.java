package com.google.common.base;

import com.google.common.annotations.GwtCompatible;
import java.lang.ref.WeakReference;

/* JADX INFO: loaded from: classes.dex */
@GwtCompatible(emulated = true)
final class Platform {
    private Platform() {
    }

    static <T extends Enum<T>> Optional<T> getEnumIfPresent(Class<T> cls, String str) {
        WeakReference<? extends Enum<?>> weakReference = Enums.getEnumConstants(cls).get(str);
        return weakReference != null ? Optional.of(cls.cast(weakReference.get())) : Optional.absent();
    }

    static CharMatcher precomputeCharMatcher(CharMatcher charMatcher) {
        return charMatcher.precomputedInternal();
    }

    static long systemNanoTime() {
        return System.nanoTime();
    }
}
