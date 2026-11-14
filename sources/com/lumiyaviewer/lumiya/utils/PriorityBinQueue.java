package com.lumiyaviewer.lumiya.utils;

import com.lumiyaviewer.lumiya.Debug;
import java.util.Collection;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/* loaded from: classes.dex */
public class PriorityBinQueue<T> implements BlockingQueue<T> {
    private final Map<T, Integer> allItems = new IdentityHashMap();
    private final Lock lock = new ReentrantLock();
    private final Condition notEmpty = this.lock.newCondition();
    private final int numBins;
    private final Set<T>[] queues;

    public PriorityBinQueue(int i) {
        this.numBins = i;
        this.queues = new Set[i];
        for (int i2 = 0; i2 < i; i2++) {
            this.queues[i2] = new HashSet();
        }
    }

    @Override // java.util.concurrent.BlockingQueue, java.util.Queue, java.util.Collection
    public boolean add(T t) {
        int i;
        this.lock.lock();
        try {
            if (t instanceof HasPriority) {
                i = ((HasPriority) t).getPriority();
                if (i < 0) {
                    i = 0;
                } else if (i > this.numBins - 1) {
                    i = this.numBins - 1;
                }
            } else {
                Debug.Printf("Thread %s added item %s without a priority", Thread.currentThread().getName(), t.toString());
                i = 0;
            }
            boolean add = this.queues[i].add(t);
            this.allItems.put(t, Integer.valueOf(i));
            Debug.Printf("Thread %s added item to the queue, bin %d/%d", Thread.currentThread().getName(), Integer.valueOf(i), Integer.valueOf(this.numBins));
            this.notEmpty.signalAll();
            return add;
        } finally {
            this.lock.unlock();
        }
    }

    @Override // java.util.Collection
    public boolean addAll(Collection<? extends T> collection) {
        boolean z = false;
        Iterator it = collection.iterator();
        while (it.hasNext()) {
            z |= add((T) it.next());
        }
        return z;
    }

    @Override // java.util.Collection
    public void clear() {
        this.lock.lock();
        try {
            for (int i = 0; i < this.numBins; i++) {
                this.queues[i].clear();
            }
            this.allItems.clear();
        } finally {
            this.lock.unlock();
        }
    }

    @Override // java.util.concurrent.BlockingQueue, java.util.Collection
    public boolean contains(Object obj) {
        this.lock.lock();
        try {
            return this.allItems.containsKey(obj);
        } finally {
            this.lock.unlock();
        }
    }

    @Override // java.util.Collection
    public boolean containsAll(Collection<?> collection) {
        this.lock.lock();
        boolean z = true;
        try {
            Iterator it = collection.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                if (!contains(it.next())) {
                    z = false;
                    break;
                }
            }
            return z;
        } finally {
            this.lock.unlock();
        }
    }

    @Override // java.util.concurrent.BlockingQueue
    public int drainTo(Collection<? super T> collection) {
        this.lock.lock();
        try {
            int i = 0;
            for (int i2 = 0; i2 < this.numBins; i2++) {
                i += this.queues[i2].size();
                collection.addAll(this.queues[i2]);
                this.queues[i2].clear();
            }
            this.allItems.clear();
            return i;
        } finally {
            this.lock.unlock();
        }
    }

    @Override // java.util.concurrent.BlockingQueue
    public int drainTo(Collection<? super T> collection, int i) {
        throw new UnsupportedOperationException();
    }

    @Override // java.util.Queue
    public T element() {
        T peek = peek();
        if (peek == null) {
            throw new NoSuchElementException();
        }
        return peek;
    }

    @Override // java.util.Collection
    public boolean isEmpty() {
        this.lock.lock();
        try {
            return this.allItems.isEmpty();
        } finally {
            this.lock.unlock();
        }
    }

    @Override // java.util.Collection, java.lang.Iterable
    public Iterator<T> iterator() {
        throw new UnsupportedOperationException("Iterator not supported");
    }

    @Override // java.util.concurrent.BlockingQueue, java.util.Queue
    public boolean offer(T t) {
        return add(t);
    }

    @Override // java.util.concurrent.BlockingQueue
    public boolean offer(T t, long j, TimeUnit timeUnit) throws InterruptedException {
        return add(t);
    }

    @Override // java.util.Queue
    public T peek() {
        T next;
        this.lock.lock();
        try {
            for (int i = 0; i < this.numBins; i++) {
                if (!this.queues[i].isEmpty() && (next = this.queues[i].iterator().next()) != null) {
                    return next;
                }
            }
            return null;
        } finally {
            this.lock.unlock();
        }
    }

    @Override // java.util.Queue
    public T poll() {
        Iterator<T> it;
        T next;
        this.lock.lock();
        try {
            for (int i = 0; i < this.numBins; i++) {
                if (!this.queues[i].isEmpty() && (next = (it = this.queues[i].iterator()).next()) != null) {
                    it.remove();
                    this.allItems.remove(next);
                    return next;
                }
            }
            return null;
        } finally {
            this.lock.unlock();
        }
    }

    /* JADX WARN: Code restructure failed: missing block: B:12:0x002b, code lost:
    
        r2.remove();
        r7.allItems.remove(r0);
        com.lumiyaviewer.lumiya.Debug.Printf("Thread %s got item with priority %d", java.lang.Thread.currentThread().getName(), java.lang.Integer.valueOf(r1));
     */
    @Override // java.util.concurrent.BlockingQueue
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public T poll(long j, TimeUnit timeUnit) throws InterruptedException {
        Iterator<T> r2;
        T t = null;
        int i = 0;
        this.lock.lock();
        try {
            if (this.notEmpty.await(j, timeUnit)) {
                while (true) {
                    if (i < this.numBins) {
                        if (!this.queues[i].isEmpty() && (t = (r2 = this.queues[i].iterator()).next()) != null) {
                            r2.remove();
                            this.allItems.remove(t);
                            break;
                        }
                        i++;
                    } else {
                        break;
                    }
                }
            }
            return t;
        } finally {
            this.lock.unlock();
        }
    }

    @Override // java.util.concurrent.BlockingQueue
    public void put(T t) throws InterruptedException {
        add(t);
    }

    @Override // java.util.concurrent.BlockingQueue
    public int remainingCapacity() {
        return Integer.MAX_VALUE;
    }

    @Override // java.util.Queue
    public T remove() {
        T poll = poll();
        if (poll == null) {
            throw new NoSuchElementException();
        }
        return poll;
    }

    @Override // java.util.concurrent.BlockingQueue, java.util.Collection
    public boolean remove(Object obj) {
        this.lock.lock();
        try {
            boolean z = false;
            for (int i = 0; i < this.numBins; i++) {
                z |= this.queues[i].remove(obj);
            }
            this.allItems.remove(obj);
            return z;
        } finally {
            this.lock.unlock();
        }
    }

    @Override // java.util.Collection
    public boolean removeAll(Collection<?> collection) {
        boolean z = false;
        Iterator it = collection.iterator();
        while (it.hasNext()) {
            z |= remove(it.next());
        }
        return z;
    }

    @Override // java.util.Collection
    public boolean retainAll(Collection<?> collection) {
        this.lock.lock();
        try {
            boolean z = false;
            for (int i = 0; i < this.numBins; i++) {
                z |= this.queues[i].retainAll(collection);
            }
            this.allItems.keySet().retainAll(collection);
            return z;
        } finally {
            this.lock.unlock();
        }
    }

    @Override // java.util.Collection
    public int size() {
        this.lock.lock();
        try {
            int i = 0;
            for (int i2 = 0; i2 < this.numBins; i2++) {
                i += this.queues[i2].size();
            }
            return i;
        } finally {
            this.lock.unlock();
        }
    }

    @Override // java.util.concurrent.BlockingQueue
    public T take() throws InterruptedException {
        Iterator<T> it;
        T next;
        this.lock.lock();
        try {
            while (true) {
                for (int i = 0; i < this.numBins; i++) {
                    if (!this.queues[i].isEmpty() && (next = (it = this.queues[i].iterator()).next()) != null) {
                        it.remove();
                        this.allItems.remove(next);
                        Debug.Printf("Thread %s got item with priority %d", Thread.currentThread().getName(), Integer.valueOf(i));
                        return next;
                    }
                }
                Debug.Printf("Thread %s waiting on the queue", Thread.currentThread().getName());
                this.notEmpty.await();
            }
        } finally {
            this.lock.unlock();
        }
    }

    @Override // java.util.Collection
    public Object[] toArray() {
        throw new UnsupportedOperationException();
    }

    @Override // java.util.Collection
    public <T> T[] toArray(T[] tArr) {
        throw new UnsupportedOperationException();
    }

    public void updatePriority(T t) {
        int intValue;
        this.lock.lock();
        try {
            if (t instanceof HasPriority) {
                int priority = ((HasPriority) t).getPriority();
                int i = priority >= 0 ? priority > this.numBins + (-1) ? this.numBins - 1 : priority : 0;
                Integer num = this.allItems.get(t);
                if (num != null && (intValue = num.intValue()) != i && this.queues[intValue].remove(t)) {
                    this.queues[i].add(t);
                    this.allItems.put(t, Integer.valueOf(i));
                }
            }
        } finally {
            this.lock.unlock();
        }
    }
}
