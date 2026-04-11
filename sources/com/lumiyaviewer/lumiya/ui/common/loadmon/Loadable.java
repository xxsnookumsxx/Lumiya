package com.lumiyaviewer.lumiya.ui.common.loadmon;

import javax.annotation.Nonnull;

/* JADX INFO: loaded from: classes.dex */
public interface Loadable {

    public interface LoadableStatusListener {
        void onLoadableStatusChange(Loadable loadable, Status status);
    }

    public enum Status {
        Idle,
        Loading,
        Loaded,
        Error;

        /* JADX INFO: renamed from: values, reason: to resolve conflict with enum method */
        public static Status[] valuesCustom() {
            return values();
        }
    }

    void addLoadableStatusListener(LoadableStatusListener loadableStatusListener);

    @Nonnull
    Status getLoadableStatus();
}
