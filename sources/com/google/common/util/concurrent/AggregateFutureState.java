package com.google.common.util.concurrent;

import com.google.common.annotations.GwtCompatible;
import com.google.common.collect.Sets;
import java.util.Set;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/* JADX INFO: loaded from: classes.dex */
@GwtCompatible(emulated = true)
abstract class AggregateFutureState {
    private volatile int remaining;
    private volatile Set<Throwable> seenExceptions = null;
    private static final AtomicReferenceFieldUpdater<AggregateFutureState, Set<Throwable>> SEEN_EXCEPTIONS_UDPATER = AtomicReferenceFieldUpdater.newUpdater(AggregateFutureState.class, Set.class, "seenExceptions");
    private static final AtomicIntegerFieldUpdater<AggregateFutureState> REMAINING_COUNT_UPDATER = AtomicIntegerFieldUpdater.newUpdater(AggregateFutureState.class, "remaining");

    AggregateFutureState(int i) {
        this.remaining = i;
    }

    abstract void addInitialException(Set<Throwable> set);

    final int decrementRemainingAndGet() {
        return REMAINING_COUNT_UPDATER.decrementAndGet(this);
    }

    final Set<Throwable> getOrInitSeenExceptions() {
        Set<Throwable> set = this.seenExceptions;
        if (set != null) {
            return set;
        }
        Set<Throwable> setNewConcurrentHashSet = Sets.newConcurrentHashSet();
        addInitialException(setNewConcurrentHashSet);
        SEEN_EXCEPTIONS_UDPATER.compareAndSet(this, null, setNewConcurrentHashSet);
        return this.seenExceptions;
    }
}
