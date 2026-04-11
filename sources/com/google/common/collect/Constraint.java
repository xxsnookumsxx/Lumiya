package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;

/* JADX INFO: loaded from: classes.dex */
@GwtCompatible
interface Constraint<E> {
    E checkElement(E e);

    String toString();
}
