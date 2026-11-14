package com.lumiyaviewer.lumiya.res.executors;

import com.lumiyaviewer.lumiya.GlobalOptions;
import java.util.concurrent.PriorityBlockingQueue;

/* JADX INFO: loaded from: classes.dex */
public class HTTPFetchExecutor extends WeakExecutor {

    private static class InstanceHolder {
        private static final HTTPFetchExecutor Instance = new HTTPFetchExecutor(null);

        private InstanceHolder() {
        }
    }

    private HTTPFetchExecutor() {
        super("ResourceHTTPFetch", getBoundedTexturePoolSize(GlobalOptions.getInstance().getMaxTextureDownloads()), new PriorityBlockingQueue());
    }

    public static int getBoundedTexturePoolSize(int requested) {
        return Math.max(8, Math.min(10, requested));
    }

    /* synthetic */ HTTPFetchExecutor(HTTPFetchExecutor hTTPFetchExecutor) {
        this();
    }

    public static HTTPFetchExecutor getInstance() {
        return InstanceHolder.Instance;
    }
}
