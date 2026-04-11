package com.lumiyaviewer.lumiya.utils.wlist;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.RandomAccess;
import javax.annotation.Nonnull;

/* JADX INFO: loaded from: classes.dex */
public class ChunkedList<E> extends AbstractList<E> implements RandomAccess {
    private final List<List<E>> chunks = new ArrayList();
    private int count = 0;
    private List<E> lastChunk = null;
    private int lastChunkIndex = 0;
    private int lastChunkStart = 0;
    private int lastChunkSize = 0;

    public interface ChunkFactory<E> {
        List<E> createEmptyChunk();
    }

    private void checkConsistency() {
        Iterator<T> it = this.chunks.iterator();
        int size = 0;
        while (it.hasNext()) {
            size = ((List) it.next()).size() + size;
        }
        if (size != this.count) {
            throw new IllegalStateException(String.format("newCount %d, count %d", Integer.valueOf(size), Integer.valueOf(this.count)));
        }
    }

    private int replaceElementInChunk(List<E> list, @Nonnull E e, @Nonnull Comparator<E> comparator) {
        int iBinarySearch;
        if (list.isEmpty() || (iBinarySearch = Collections.binarySearch(list, e, comparator)) < 0) {
            return -1;
        }
        return replaceFoundElement(list, iBinarySearch, e);
    }

    private int replaceFoundElement(List<E> list, int i, @Nonnull E e) {
        list.set(i, e);
        int size = 0;
        Iterator<T> it = this.chunks.iterator();
        while (true) {
            int i2 = size;
            if (!it.hasNext()) {
                return -1;
            }
            List<E> list2 = (List) it.next();
            if (list2 == list) {
                return i2 + i;
            }
            size = list2.size() + i2;
        }
    }

    private void resetLastPosition() {
        this.lastChunk = null;
        checkConsistency();
    }

    private void setLastChunk(int i) {
        if (i < 0 || i >= this.count) {
            throw new IndexOutOfBoundsException(String.format("index %d, count %d", Integer.valueOf(i), Integer.valueOf(this.count)));
        }
        checkConsistency();
        if (this.lastChunk == null) {
            this.lastChunkIndex = 0;
            this.lastChunkStart = 0;
            this.lastChunk = this.chunks.get(this.lastChunkIndex);
            this.lastChunkSize = this.lastChunk.size();
        }
        while (i < this.lastChunkStart) {
            this.lastChunkIndex--;
            this.lastChunk = this.chunks.get(this.lastChunkIndex);
            this.lastChunkSize = this.lastChunk.size();
            this.lastChunkStart -= this.lastChunkSize;
        }
        while (i >= this.lastChunkStart + this.lastChunkSize) {
            this.lastChunkIndex++;
            this.lastChunkStart += this.lastChunkSize;
            if (this.lastChunkIndex >= this.chunks.size()) {
                throw new IllegalStateException(String.format("lastChunkIndex runaway, position %d, count %d, lastChunkStart %d", Integer.valueOf(i), Integer.valueOf(this.count), Integer.valueOf(this.lastChunkStart)));
            }
            this.lastChunk = this.chunks.get(this.lastChunkIndex);
            this.lastChunkSize = this.lastChunk.size();
        }
    }

    public void addChunkAtEnd(List<E> list) {
        this.chunks.add(list);
        this.count += list.size();
        resetLastPosition();
    }

    public void addChunkAtStart(List<E> list) {
        this.chunks.add(0, list);
        this.count += list.size();
        resetLastPosition();
    }

    public void addElement(E e, int i, ChunkFactory<E> chunkFactory) {
        List<E> list = this.chunks.size() > 0 ? this.chunks.get(this.chunks.size() - 1) : null;
        if (list == null || list.size() >= i) {
            List<E> listCreateEmptyChunk = chunkFactory.createEmptyChunk();
            listCreateEmptyChunk.add(e);
            this.chunks.add(listCreateEmptyChunk);
            this.count++;
        } else {
            list.add(e);
            this.count++;
            if (this.lastChunk == list) {
                this.lastChunkSize++;
            }
        }
        checkConsistency();
    }

    @Override // java.util.AbstractList, java.util.AbstractCollection, java.util.Collection, java.util.List
    public void clear() {
        this.chunks.clear();
        this.count = 0;
        resetLastPosition();
    }

    @Override // java.util.AbstractList, java.util.List
    public E get(int i) {
        setLastChunk(i);
        if (i < this.lastChunkStart || i >= this.lastChunkStart + this.lastChunkSize) {
            throw new IndexOutOfBoundsException(String.format("index %d, count %d", Integer.valueOf(i), Integer.valueOf(this.count)));
        }
        return this.lastChunk.get(i - this.lastChunkStart);
    }

    public int removeChunkAtEnd() {
        if (this.chunks.size() <= 0) {
            return 0;
        }
        List<E> listRemove = this.chunks.remove(this.chunks.size() - 1);
        int size = listRemove != null ? listRemove.size() : 0;
        this.count -= size;
        resetLastPosition();
        return size;
    }

    public int removeChunkAtStart() {
        if (this.chunks.size() <= 0) {
            return 0;
        }
        List<E> listRemove = this.chunks.remove(0);
        int size = listRemove != null ? listRemove.size() : 0;
        this.count -= size;
        resetLastPosition();
        return size;
    }

    public int removeElementsAfter(int i) {
        checkConsistency();
        if (i < 0 || i >= this.count) {
            return 0;
        }
        setLastChunk(i);
        if (i < this.lastChunkStart || i >= this.lastChunkStart + this.lastChunkSize) {
            return 0;
        }
        int i2 = this.lastChunkIndex + 2;
        int size = 0;
        for (int size2 = this.chunks.size() - 1; size2 >= i2; size2--) {
            size += this.chunks.get(size2).size();
            this.chunks.remove(size2);
        }
        this.count -= size;
        checkConsistency();
        return size;
    }

    public int removeElementsBefore(int i) {
        int i2;
        int size;
        checkConsistency();
        if (i < 0 || i >= this.count) {
            return 0;
        }
        setLastChunk(i);
        if (i < this.lastChunkStart || i >= this.lastChunkStart + this.lastChunkSize) {
            return 0;
        }
        int i3 = this.lastChunkIndex - 2;
        if (i3 >= 0) {
            i2 = i3 + 1;
            size = 0;
        } else {
            i2 = 0;
            size = 0;
        }
        while (i2 > 0) {
            size += this.chunks.get(0).size();
            this.chunks.remove(0);
            i2--;
        }
        this.count -= size;
        resetLastPosition();
        return size;
    }

    public int replaceElement(@Nonnull E e, @Nonnull Comparator<E> comparator) {
        int i;
        int i2;
        byte b;
        if (this.chunks.isEmpty()) {
            return -1;
        }
        byte b2 = 0;
        int size = this.chunks.size() / 2;
        while (true) {
            List<E> list = this.chunks.get(size);
            if (!list.isEmpty()) {
                E e2 = list.get(0);
                E e3 = list.get(list.size() - 1);
                int iCompare = comparator.compare(e, e2);
                if (iCompare == 0) {
                    return replaceFoundElement(list, 0, e);
                }
                if (iCompare < 0) {
                    i2 = size - 1;
                    if (i2 < 0) {
                        return -1;
                    }
                    b = -1;
                    size = i2;
                    b2 = b;
                } else {
                    int iCompare2 = comparator.compare(e, e3);
                    if (iCompare2 == 0) {
                        return replaceFoundElement(list, list.size() - 1, e);
                    }
                    if (iCompare2 <= 0) {
                        return replaceElementInChunk(list, e, comparator);
                    }
                    i = size + 1;
                    b2 = 1;
                    if (i >= this.chunks.size()) {
                        return -1;
                    }
                }
            } else if (b2 < 0) {
                i = size - 1;
                if (i < 0) {
                    return -1;
                }
            } else if (b2 > 0) {
                i = size + 1;
                if (i >= this.chunks.size()) {
                    return -1;
                }
            } else {
                int i3 = 0;
                while (true) {
                    if (i3 >= this.chunks.size()) {
                        i = -1;
                        break;
                    }
                    if (!this.chunks.get(i3).isEmpty()) {
                        i = i3;
                        break;
                    }
                    i3++;
                }
                if (i == -1) {
                    return -1;
                }
            }
            byte b3 = b2;
            i2 = i;
            b = b3;
            size = i2;
            b2 = b;
        }
    }

    @Override // java.util.AbstractCollection, java.util.Collection, java.util.List
    public int size() {
        return this.count;
    }
}
