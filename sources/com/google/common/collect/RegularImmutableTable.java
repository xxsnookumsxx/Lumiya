package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Table;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import javax.annotation.Nullable;

/* JADX INFO: loaded from: classes.dex */
@GwtCompatible
abstract class RegularImmutableTable<R, C, V> extends ImmutableTable<R, C, V> {

    private final class CellSet extends ImmutableSet.Indexed<Table.Cell<R, C, V>> {
        private CellSet() {
        }

        @Override // com.google.common.collect.ImmutableCollection, java.util.AbstractCollection, java.util.Collection, java.util.Set
        public boolean contains(@Nullable Object obj) {
            if (!(obj instanceof Table.Cell)) {
                return false;
            }
            Table.Cell cell = (Table.Cell) obj;
            Object obj2 = RegularImmutableTable.this.get(cell.getRowKey(), cell.getColumnKey());
            return obj2 != null && obj2.equals(cell.getValue());
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        @Override // com.google.common.collect.ImmutableSet.Indexed
        public Table.Cell<R, C, V> get(int i) {
            return RegularImmutableTable.this.getCell(i);
        }

        @Override // com.google.common.collect.ImmutableCollection
        boolean isPartialView() {
            return false;
        }

        @Override // java.util.AbstractCollection, java.util.Collection, java.util.Set
        public int size() {
            return RegularImmutableTable.this.size();
        }
    }

    private final class Values extends ImmutableList<V> {
        private Values() {
        }

        @Override // java.util.List
        public V get(int i) {
            return (V) RegularImmutableTable.this.getValue(i);
        }

        @Override // com.google.common.collect.ImmutableCollection
        boolean isPartialView() {
            return true;
        }

        @Override // java.util.AbstractCollection, java.util.Collection, java.util.List
        public int size() {
            return RegularImmutableTable.this.size();
        }
    }

    RegularImmutableTable() {
    }

    static <R, C, V> RegularImmutableTable<R, C, V> forCells(Iterable<Table.Cell<R, C, V>> iterable) {
        return forCellsInternal(iterable, null, null);
    }

    static <R, C, V> RegularImmutableTable<R, C, V> forCells(List<Table.Cell<R, C, V>> list, @Nullable final Comparator<? super R> comparator, @Nullable final Comparator<? super C> comparator2) {
        Preconditions.checkNotNull(list);
        if (comparator != null || comparator2 != null) {
            Collections.sort(list, new Comparator<Table.Cell<R, C, V>>() { // from class: com.google.common.collect.RegularImmutableTable.1
                @Override // java.util.Comparator
                public int compare(Table.Cell<R, C, V> cell, Table.Cell<R, C, V> cell2) {
                    int iCompare = comparator != null ? comparator.compare(cell.getRowKey(), cell2.getRowKey()) : 0;
                    if (iCompare != 0) {
                        return iCompare;
                    }
                    if (comparator2 != null) {
                        return comparator2.compare(cell.getColumnKey(), cell2.getColumnKey());
                    }
                    return 0;
                }
            });
        }
        return forCellsInternal(list, comparator, comparator2);
    }

    private static final <R, C, V> RegularImmutableTable<R, C, V> forCellsInternal(Iterable<Table.Cell<R, C, V>> iterable, @Nullable Comparator<? super R> comparator, @Nullable Comparator<? super C> comparator2) {
        LinkedHashSet linkedHashSet = new LinkedHashSet();
        LinkedHashSet linkedHashSet2 = new LinkedHashSet();
        ImmutableList immutableListCopyOf = ImmutableList.copyOf(iterable);
        for (Table.Cell<R, C, V> cell : iterable) {
            linkedHashSet.add(cell.getRowKey());
            linkedHashSet2.add(cell.getColumnKey());
        }
        ImmutableSet immutableSetCopyOf = comparator != null ? ImmutableSet.copyOf((Collection) Ordering.from(comparator).immutableSortedCopy(linkedHashSet)) : ImmutableSet.copyOf((Collection) linkedHashSet);
        ImmutableSet immutableSetCopyOf2 = comparator2 != null ? ImmutableSet.copyOf((Collection) Ordering.from(comparator2).immutableSortedCopy(linkedHashSet2)) : ImmutableSet.copyOf((Collection) linkedHashSet2);
        return !(((long) immutableListCopyOf.size()) <= (((long) immutableSetCopyOf.size()) * ((long) immutableSetCopyOf2.size())) / 2) ? new DenseImmutableTable<>(immutableListCopyOf, immutableSetCopyOf, immutableSetCopyOf2) : new SparseImmutableTable<>(immutableListCopyOf, immutableSetCopyOf, immutableSetCopyOf2);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Override // com.google.common.collect.ImmutableTable, com.google.common.collect.AbstractTable
    public final ImmutableSet<Table.Cell<R, C, V>> createCellSet() {
        return !isEmpty() ? new CellSet() : ImmutableSet.of();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Override // com.google.common.collect.ImmutableTable, com.google.common.collect.AbstractTable
    public final ImmutableCollection<V> createValues() {
        return !isEmpty() ? new Values() : ImmutableList.of();
    }

    abstract Table.Cell<R, C, V> getCell(int i);

    abstract V getValue(int i);
}
