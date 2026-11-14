package com.lumiyaviewer.lumiya.utils.reqset;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
/* loaded from: classes.dex */
class WeakRequestSet<T> {
    private final Map<T, Set<WeakReference<Object>>> requests = new HashMap();
    private final Object lock = new Object();

    WeakRequestSet() {
    }

    boolean addRequest(@Nonnull T t, @Nonnull Object obj) {
        boolean z;
        boolean z2 = true;
        synchronized (this.lock) {
            Set<WeakReference<Object>> set = this.requests.get(t);
            if (set == null) {
                HashSet hashSet = new HashSet();
                hashSet.add(new WeakReference(obj));
                this.requests.put(t, hashSet);
            } else {
                Iterator<WeakReference<Object>> it = set.iterator();
                boolean z3 = false;
                while (it.hasNext()) {
                    WeakReference<Object> next = it.next();
                    if (next.get() == null) {
                        it.remove();
                        z = z3;
                    } else {
                        z = next.get() == obj ? true : z3;
                    }
                    z3 = z;
                }
                if (z3) {
                    z2 = false;
                } else {
                    set.add(new WeakReference<>(obj));
                }
            }
        }
        return z2;
    }

    void completeRequest(@Nonnull T t) {
        Set<WeakReference<Object>> remove;
        synchronized (this.lock) {
            remove = this.requests.remove(t);
        }
        Iterator<WeakReference<Object>> it = remove.iterator();
        while (it.hasNext()) {
            Object obj = ((WeakReference) it.next()).get();
            if (obj != null && (obj instanceof RequestCompleteListener)) {
                ((RequestCompleteListener) obj).onRequestComplete(t);
            }
        }
    }

    /* JADX WARN: Code restructure failed: missing block: B:22:0x004d, code lost:
    
        r0 = r0.getKey();
     */
    @Nullable
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    T getRequest() {
        T t;
        synchronized (this.lock) {
            Iterator<Map.Entry<T, Set<WeakReference<Object>>>> it = this.requests.entrySet().iterator();
            while (true) {
                if (!it.hasNext()) {
                    t = null;
                    break;
                }
                Map.Entry<T, Set<WeakReference<Object>>> next = it.next();
                Iterator<WeakReference<Object>> it2 = next.getValue().iterator();
                while (it2.hasNext()) {
                    if (it2.next().get() == null) {
                        it2.remove();
                    }
                }
                if (!next.getValue().isEmpty()) {
                    t = next.getKey();
                    break;
                }
                it.remove();
            }
        }
        return t;
    }
}
