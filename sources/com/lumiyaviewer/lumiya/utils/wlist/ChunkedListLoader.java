package com.lumiyaviewer.lumiya.utils.wlist;

import com.google.common.collect.Lists;
import com.lumiyaviewer.lumiya.Debug;
import com.lumiyaviewer.lumiya.utils.Identifiable;
import com.lumiyaviewer.lumiya.utils.wlist.ChunkedList;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.RandomAccess;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/* JADX INFO: loaded from: classes.dex */
public class ChunkedListLoader<E extends Identifiable<Long>> extends AbstractList<E> implements ChunkedList.ChunkFactory<E>, RandomAccess {

    @Nonnull
    private final Executor executor;

    @Nonnull
    private final EventListener listener;

    @Nonnull
    private final Executor listenerExecutor;
    private long loadAboveTopmostId;
    private long loadBelowLastId;
    private final boolean startFromStart;
    private final int windowSize;
    private final Queue<E> addedElements = new ConcurrentLinkedQueue();
    private final AtomicBoolean updatePosted = new AtomicBoolean();
    private final AtomicBoolean loadRequested = new AtomicBoolean();
    private final AtomicBoolean reloadRequested = new AtomicBoolean();
    private final AtomicBoolean reloadAccepted = new AtomicBoolean();
    private final Object lock = new Object();
    private boolean loadAboveWanted = false;

    @Nullable
    private LoadResult<E> loadAboveResult = null;
    private boolean loadBelowWanted = false;

    @Nullable
    private LoadResult<E> loadBelowResult = null;
    private final Map<Long, E> updatedElements = new HashMap();
    private final Runnable loadMoreData = new Runnable() { // from class: com.lumiyaviewer.lumiya.utils.wlist.ChunkedListLoader.1
        @Override // java.lang.Runnable
        public void run() {
            boolean z;
            long j;
            boolean z2;
            boolean z3;
            long j2;
            boolean z4 = true;
            Debug.Printf("ChatView: processing loadMoreData(), reloadRequested %b", Boolean.valueOf(ChunkedListLoader.this.reloadRequested.get()));
            ChunkedListLoader.this.loadRequested.set(false);
            synchronized (ChunkedListLoader.this.lock) {
                z = (ChunkedListLoader.this.loadAboveWanted && ChunkedListLoader.this.loadAboveResult == null) ? !ChunkedListLoader.this.reloadRequested.get() : false;
                j = ChunkedListLoader.this.loadAboveTopmostId;
            }
            if (z) {
                LoadResult<E> loadResultLoadInBackground = ChunkedListLoader.this.loadInBackground(ChunkedListLoader.this.windowSize, j, false);
                synchronized (ChunkedListLoader.this.lock) {
                    ChunkedListLoader.this.loadAboveResult = loadResultLoadInBackground;
                }
                z2 = true;
            } else {
                z2 = false;
            }
            synchronized (ChunkedListLoader.this.lock) {
                z3 = (ChunkedListLoader.this.loadBelowWanted && ChunkedListLoader.this.loadBelowResult == null) ? !ChunkedListLoader.this.reloadRequested.get() : false;
                j2 = ChunkedListLoader.this.loadBelowLastId;
            }
            if (z3) {
                LoadResult<E> loadResultLoadInBackground2 = ChunkedListLoader.this.loadInBackground(ChunkedListLoader.this.windowSize, j2, true);
                synchronized (ChunkedListLoader.this.lock) {
                    ChunkedListLoader.this.loadBelowResult = loadResultLoadInBackground2;
                }
                z2 = true;
            }
            if (ChunkedListLoader.this.reloadRequested.getAndSet(false)) {
                ChunkedListLoader.this.reloadAccepted.set(true);
            } else {
                z4 = z2;
            }
            if (z4) {
                ChunkedListLoader.this.postUpdate();
            }
        }
    };
    private final Runnable processUpdate = new Runnable() { // from class: com.lumiyaviewer.lumiya.utils.wlist.ChunkedListLoader.2
        @Override // java.lang.Runnable
        public void run() {
            LoadResult loadResult;
            int size;
            LoadResult loadResult2;
            int size2;
            Identifiable identifiable;
            boolean z;
            ChunkedListLoader.this.updatePosted.set(false);
            Debug.Printf("ChatView: processUpdate, reloadAccepted: %b", Boolean.valueOf(ChunkedListLoader.this.reloadAccepted.get()));
            if (ChunkedListLoader.this.reloadAccepted.getAndSet(false)) {
                synchronized (ChunkedListLoader.this.lock) {
                    ChunkedListLoader.this.loadAboveWanted = false;
                    ChunkedListLoader.this.loadAboveResult = null;
                    ChunkedListLoader.this.loadBelowResult = null;
                    ChunkedListLoader.this.loadBelowWanted = false;
                }
                ChunkedListLoader.this.hasAbove = true;
                ChunkedListLoader.this.hasBelow = true;
                ChunkedListLoader.this.addedElements.clear();
                ChunkedListLoader.this.items.clear();
                ChunkedListLoader.this.listener.onListReloaded();
                return;
            }
            synchronized (ChunkedListLoader.this.lock) {
                loadResult = ChunkedListLoader.this.loadAboveResult;
                if (loadResult != null) {
                    ChunkedListLoader.this.loadAboveResult = null;
                    ChunkedListLoader.this.loadAboveWanted = false;
                }
            }
            if (loadResult != null) {
                size = loadResult.entries.size() + 0;
                ChunkedListLoader.this.items.addChunkAtStart(Lists.reverse(loadResult.entries));
                ChunkedListLoader.this.hasAbove = loadResult.hasMore;
                if (loadResult.fromId == Long.MAX_VALUE) {
                    ChunkedListLoader.this.hasBelow = false;
                }
            } else {
                size = 0;
            }
            if (size != 0) {
                ChunkedListLoader.this.listener.onListItemsAdded(0, size);
            }
            int size3 = ChunkedListLoader.this.items.size();
            synchronized (ChunkedListLoader.this.lock) {
                loadResult2 = ChunkedListLoader.this.loadBelowResult;
                if (loadResult2 != null) {
                    ChunkedListLoader.this.loadBelowResult = null;
                    ChunkedListLoader.this.loadBelowWanted = false;
                }
            }
            if (loadResult2 != null) {
                size2 = loadResult2.entries.size();
                ChunkedListLoader.this.items.addChunkAtEnd(loadResult2.entries);
                ChunkedListLoader.this.hasBelow = loadResult2.hasMore;
                if (loadResult2.fromId == 0) {
                    ChunkedListLoader.this.hasAbove = false;
                }
            } else {
                size2 = 0;
            }
            boolean z2 = false;
            int i = size2;
            while (true) {
                Identifiable identifiable2 = (Identifiable) ChunkedListLoader.this.addedElements.poll();
                if (identifiable2 == null) {
                    break;
                }
                long jLongValue = ChunkedListLoader.this.items.size() > 0 ? ((Long) ((Identifiable) ChunkedListLoader.this.items.get(ChunkedListLoader.this.items.size() - 1)).getId()).longValue() : -1L;
                Debug.Printf("ChatView: added element: id %d, lastId %d, hasBelow %b", identifiable2.getId(), Long.valueOf(jLongValue), Boolean.valueOf(ChunkedListLoader.this.hasBelow));
                if (ChunkedListLoader.this.hasBelow || ((Long) identifiable2.getId()).longValue() <= jLongValue) {
                    z = z2;
                } else {
                    ChunkedListLoader.this.items.addElement(identifiable2, ChunkedListLoader.this.windowSize, ChunkedListLoader.this);
                    i++;
                    z = true;
                }
                z2 = z;
                i = i;
            }
            if (i != 0) {
                ChunkedListLoader.this.listener.onListItemsAdded(size3, i);
            }
            if (z2) {
                ChunkedListLoader.this.listener.onListItemAddedAtEnd();
            }
            while (true) {
                synchronized (ChunkedListLoader.this.lock) {
                    Iterator it = ChunkedListLoader.this.updatedElements.entrySet().iterator();
                    if (it.hasNext()) {
                        identifiable = (Identifiable) ((Map.Entry) it.next()).getValue();
                        it.remove();
                    } else {
                        identifiable = null;
                    }
                }
                if (identifiable == null) {
                    return;
                }
                int iReplaceElement = ChunkedListLoader.this.items.replaceElement(identifiable, ChunkedListLoader.this.chatMessageComparator);
                Debug.Printf("ChunkedListLoader: replace: replacedIndex is %d", Integer.valueOf(iReplaceElement));
                if (iReplaceElement >= 0) {
                    ChunkedListLoader.this.listener.onListItemChanged(iReplaceElement);
                }
            }
        }
    };
    private final Comparator<E> chatMessageComparator = new Comparator() { // from class: com.lumiyaviewer.lumiya.utils.wlist.-$Lambda$QDlX9uefQr1Wq8gtt1O6M2wUNME
        private final /* synthetic */ int $m$0(Object obj, Object obj2) {
            return Long.signum(((Long) ((Identifiable) obj).getId()).longValue() - ((Long) ((Identifiable) obj2).getId()).longValue());
        }

        @Override // java.util.Comparator
        public final int compare(Object obj, Object obj2) {
            return $m$0(obj, obj2);
        }
    };

    @Nonnull
    private ChunkedList<E> items = new ChunkedList<>();
    private boolean hasAbove = true;
    private boolean hasBelow = true;

    public interface EventListener {
        @Nonnull
        Executor getListEventsExecutor();

        void onListItemAddedAtEnd();

        void onListItemChanged(int i);

        void onListItemsAdded(int i, int i2);

        void onListItemsRemoved(int i, int i2);

        void onListReloaded();
    }

    protected static class LoadResult<E> {
        public final List<E> entries;
        final long fromId;
        final boolean hasMore;

        public LoadResult(List<E> list, boolean z, long j) {
            this.entries = list;
            this.hasMore = z;
            this.fromId = j;
        }
    }

    public ChunkedListLoader(int i, @Nonnull Executor executor, boolean z, @Nonnull EventListener eventListener) {
        this.windowSize = i;
        this.executor = executor;
        this.startFromStart = z;
        this.listener = eventListener;
        this.listenerExecutor = eventListener.getListEventsExecutor();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void postUpdate() {
        if (!this.updatePosted.compareAndSet(false, true)) {
            Debug.Printf("ChatView: processUpdate () already requested", new Object[0]);
        } else {
            Debug.Printf("ChatView: requesting processUpdate ()", new Object[0]);
            this.listenerExecutor.execute(this.processUpdate);
        }
    }

    public void addElement(E e) {
        Debug.Printf("ChatView: addElement: adding element with id %d", e.getId());
        this.addedElements.add(e);
        postUpdate();
    }

    @Override // com.lumiyaviewer.lumiya.utils.wlist.ChunkedList.ChunkFactory
    public List<E> createEmptyChunk() {
        return new ArrayList(this.windowSize);
    }

    @Override // java.util.AbstractList, java.util.List
    public E get(int i) {
        return this.items.get(i);
    }

    public boolean hasMoreItemsAtBottom() {
        return this.hasBelow;
    }

    protected LoadResult<E> loadInBackground(int i, long j, boolean z) {
        return new LoadResult<>(new ArrayList(0), false, j);
    }

    public void reload() {
        this.reloadRequested.set(true);
        if (this.loadRequested.compareAndSet(false, true)) {
            this.executor.execute(this.loadMoreData);
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:129:0x01dc  */
    /* JADX WARN: Removed duplicated region for block: B:71:0x0126  */
    /* JADX WARN: Removed duplicated region for block: B:93:0x0161  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public void setVisibleRange(int r9, int r10) {
        /*
            Method dump skipped, instruction units count: 479
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lumiyaviewer.lumiya.utils.wlist.ChunkedListLoader.setVisibleRange(int, int):void");
    }

    @Override // java.util.AbstractCollection, java.util.Collection, java.util.List
    public int size() {
        return this.items.size();
    }

    public void updateElement(E e) {
        Debug.Printf("ChatView: addElement: updated element with id %d", e.getId());
        synchronized (this.lock) {
            this.updatedElements.put((Long) e.getId(), e);
        }
        postUpdate();
    }
}
