package com.lumiyaviewer.lumiya.react;

import javax.annotation.Nonnull;

/* JADX INFO: loaded from: classes.dex */
public interface RequestHandler<K> {
    void onRequest(@Nonnull K k);

    void onRequestCancelled(@Nonnull K k);
}
