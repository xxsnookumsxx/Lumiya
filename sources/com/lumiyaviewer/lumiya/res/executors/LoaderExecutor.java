package com.lumiyaviewer.lumiya.res.executors;

/* JADX INFO: loaded from: classes.dex */
public class LoaderExecutor extends WeakExecutor {

    private static class InstanceHolder {
        private static final LoaderExecutor Instance = new LoaderExecutor(null);

        private InstanceHolder() {
        }
    }

    private LoaderExecutor() {
        super("ResourceLoader", 4);
    }

    /* synthetic */ LoaderExecutor(LoaderExecutor loaderExecutor) {
        this();
    }

    public static LoaderExecutor getInstance() {
        return InstanceHolder.Instance;
    }
}
