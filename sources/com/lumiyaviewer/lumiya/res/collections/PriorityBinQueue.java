package com.lumiyaviewer.lumiya.res.collections;

import android.Manifest;
import com.lumiyaviewer.lumiya.Debug;
import com.lumiyaviewer.lumiya.utils.HasPriority;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/* JADX INFO: loaded from: classes.dex */
public class PriorityBinQueue<T> implements BlockingQueue<T> {
    private final QueueFactory<T> queueFactory;
    private final SortedMap<Integer, Queue<T>> queues = new TreeMap();
    private final Lock lock = new ReentrantLock();
    private final Condition notEmpty = this.lock.newCondition();

    public interface QueueFactory<T> {
        Queue<T> getQueue();
    }

    public PriorityBinQueue(QueueFactory<T> queueFactory) {
        this.queueFactory = queueFactory;
    }

    private int getPriority(Object obj) {
        if (obj instanceof HasPriority) {
            return ((HasPriority) obj).getPriority();
        }
        return 0;
    }

    @Override // java.util.concurrent.BlockingQueue, java.util.Queue, java.util.Collection
    public boolean add(T t) {
        this.lock.lock();
        try {
            int priority = getPriority(t);
            Debug.Printf("PriorityBinQueue: added %s with prio %d", t.toString(), Integer.valueOf(priority));
            Queue<T> queue = this.queues.get(Integer.valueOf(priority));
            if (queue == null) {
                queue = this.queueFactory.getQueue();
                this.queues.put(Integer.valueOf(priority), queue);
            }
            boolean zAdd = queue.add(t);
            this.notEmpty.signalAll();
            return zAdd;
        } finally {
            this.lock.unlock();
        }
    }

    @Override // java.util.Collection
    public boolean addAll(Collection<? extends T> collection) {
        this.lock.lock();
        boolean zAdd = false;
        try {
            Iterator<T> it = collection.iterator();
            while (true) {
                boolean z = zAdd;
                if (!it.hasNext()) {
                    return z;
                }
                T next = it.next();
                int priority = getPriority(next);
                Queue<T> queue = this.queues.get(Integer.valueOf(priority));
                if (queue == null) {
                    queue = this.queueFactory.getQueue();
                    this.queues.put(Integer.valueOf(priority), queue);
                }
                zAdd = queue.add(next) | z;
                this.notEmpty.signalAll();
            }
        } finally {
            this.lock.unlock();
        }
    }

    @Override // java.util.Collection
    public void clear() {
        this.lock.lock();
        try {
            this.queues.clear();
        } finally {
            this.lock.unlock();
        }
    }

    @Override // java.util.concurrent.BlockingQueue, java.util.Collection
    public boolean contains(Object obj) {
        this.lock.lock();
        try {
            Queue<T> queue = this.queues.get(Integer.valueOf(getPriority(obj)));
            if (queue != null) {
                return queue.contains(obj);
            }
            return false;
        } finally {
            this.lock.unlock();
        }
    }

    @Override // java.util.Collection
    public boolean containsAll(Collection<?> collection) {
        boolean z;
        this.lock.lock();
        try {
            Iterator<T> it = collection.iterator();
            while (true) {
                if (!it.hasNext()) {
                    z = true;
                    break;
                }
                T next = it.next();
                Queue<T> queue = this.queues.get(Integer.valueOf(getPriority(next)));
                if (queue != null && !queue.contains(next)) {
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
        int i = 0;
        try {
            Iterator<T> it = this.queues.values().iterator();
            while (it.hasNext()) {
                Queue queue = (Queue) it.next();
                while (true) {
                    Manifest manifest = (Object) queue.poll();
                    if (manifest != null) {
                        collection.add(manifest);
                        i++;
                    }
                }
            }
            this.queues.clear();
            return i;
        } finally {
            this.lock.unlock();
        }
    }

    @Override // java.util.concurrent.BlockingQueue
    public int drainTo(Collection<? super T> collection, int i) {
        int i2;
        this.lock.lock();
        int i3 = 0;
        try {
            Iterator<T> it = this.queues.values().iterator();
            while (true) {
                if (!it.hasNext()) {
                    i2 = i3;
                    break;
                }
                Queue queue = (Queue) it.next();
                while (true) {
                    Manifest manifest = (Object) queue.poll();
                    if (manifest == null || i3 >= i) {
                        break;
                    }
                    collection.add(manifest);
                    i3++;
                }
                if (i3 >= i) {
                    i2 = i3;
                    break;
                }
            }
            return i2;
        } finally {
            this.lock.unlock();
        }
    }

    @Override // java.util.Queue
    public T element() {
        T tPeek = peek();
        if (tPeek == null) {
            throw new NoSuchElementException();
        }
        return tPeek;
    }

    @Override // java.util.Collection
    public boolean isEmpty() {
        boolean z;
        this.lock.lock();
        try {
            Iterator<T> it = this.queues.values().iterator();
            while (true) {
                if (!it.hasNext()) {
                    z = true;
                    break;
                }
                if (!((Queue) it.next()).isEmpty()) {
                    z = false;
                    break;
                }
            }
            return z;
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
        this.lock.lock();
        try {
            Iterator<T> it = this.queues.values().iterator();
            while (it.hasNext()) {
                Queue<T> queue = (Queue) it.next();
                if (!queue.isEmpty()) {
                    for (T t : queue) {
                        if (t != null) {
                            return t;
                        }
                    }
                }
            }
            return null;
        } finally {
            this.lock.unlock();
        }
    }

    @Override // java.util.Queue
    public T poll() {
        this.lock.lock();
        try {
            Iterator<T> it = this.queues.values().iterator();
            while (it.hasNext()) {
                Iterator it2 = ((Queue) it.next()).iterator();
                while (it2.hasNext()) {
                    T t = (T) it2.next();
                    if (t != null) {
                        it2.remove();
                        return t;
                    }
                }
            }
            return null;
        } finally {
            this.lock.unlock();
        }
    }

    @Override // java.util.concurrent.BlockingQueue
    public T poll(long j, TimeUnit timeUnit) throws InterruptedException {
        this.lock.lock();
        do {
            try {
                T tPoll = poll();
                if (tPoll != null) {
                    return tPoll;
                }
            } finally {
                this.lock.unlock();
            }
        } while (this.notEmpty.await(j, timeUnit));
        return null;
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
        T tPoll = poll();
        if (tPoll == null) {
            throw new NoSuchElementException();
        }
        return tPoll;
    }

    @Override // java.util.concurrent.BlockingQueue, java.util.Collection
    public boolean remove(Object obj) {
        this.lock.lock();
        try {
            Queue<T> queue = this.queues.get(Integer.valueOf(getPriority(obj)));
            if (queue != null) {
                return queue.remove(obj);
            }
            return false;
        } finally {
            this.lock.unlock();
        }
    }

    @Override // java.util.Collection
    public boolean removeAll(Collection<?> collection) {
        this.lock.lock();
        boolean zRemove = false;
        try {
            for (T t : collection) {
                Queue<T> queue = this.queues.get(Integer.valueOf(getPriority(t)));
                zRemove = queue != null ? queue.remove(t) | zRemove : zRemove;
            }
            return zRemove;
        } finally {
            this.lock.unlock();
        }
    }

    @Override // java.util.Collection
    public boolean retainAll(Collection<?> collection) {
        this.lock.lock();
        boolean zRetainAll = false;
        try {
            Iterator<T> it = this.queues.values().iterator();
            while (true) {
                boolean z = zRetainAll;
                if (!it.hasNext()) {
                    return z;
                }
                zRetainAll = ((Queue) it.next()).retainAll(collection) | z;
            }
        } finally {
            this.lock.unlock();
        }
    }

    @Override // java.util.Collection
    public int size() {
        this.lock.lock();
        int size = 0;
        try {
            Iterator<T> it = this.queues.values().iterator();
            while (true) {
                int i = size;
                if (!it.hasNext()) {
                    return i;
                }
                size = ((Queue) it.next()).size() + i;
            }
        } finally {
            this.lock.unlock();
        }
    }

    @Override // java.util.concurrent.BlockingQueue
    public T take() throws InterruptedException {
        this.lock.lock();
        while (true) {
            try {
                T tPoll = poll();
                if (tPoll != null) {
                    return tPoll;
                }
                this.notEmpty.await();
            } finally {
                this.lock.unlock();
            }
        }
    }

    @Override // java.util.Collection
    public Object[] toArray() {
        int length = 0;
        this.lock.lock();
        try {
            ArrayList<Object[]> arrayList = new ArrayList();
            Iterator<T> it = this.queues.values().iterator();
            int i = 0;
            while (it.hasNext()) {
                Object[] array = ((Queue) it.next()).toArray();
                int length2 = array.length + i;
                arrayList.add(array);
                i = length2;
            }
            Object[] objArr = new Object[i];
            for (Object[] objArr2 : arrayList) {
                System.arraycopy(objArr2, 0, objArr, length, objArr2.length);
                length = objArr2.length + length;
            }
            arrayList.clear();
            return objArr;
        } finally {
            this.lock.unlock();
        }
    }

    @Override // java.util.Collection
    public <T1> T1[] toArray(T1[] t1Arr) {
        int length = 0;
        this.lock.lock();
        try {
            ArrayList<Object[]> arrayList = new ArrayList();
            Iterator<T> it = this.queues.values().iterator();
            int i = 0;
            while (it.hasNext()) {
                Object[] array = ((Queue) it.next()).toArray();
                int length2 = array.length + i;
                arrayList.add(array);
                i = length2;
            }
            if (t1Arr.length >= i) {
                Arrays.fill(t1Arr, (Object) null);
            } else {
                t1Arr = (T1[]) new Object[i];
            }
            for (Object[] objArr : arrayList) {
                System.arraycopy(objArr, 0, t1Arr, length, objArr.length);
                length = objArr.length + length;
            }
            arrayList.clear();
            return t1Arr;
        } finally {
            this.lock.unlock();
        }
    }
}
