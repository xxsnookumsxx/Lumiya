package com.lumiyaviewer.lumiya.res.executors;

import java.util.concurrent.PriorityBlockingQueue;

/**
 * Dedicated HTTP fetch executor for mesh downloads (~70% of Wi-Fi HTTP bandwidth/slots).
 */
public class MeshFetchExecutor extends WeakExecutor {
    public static final int MESH_FETCH_THREADS = 22;

    private static class InstanceHolder {
        private static final MeshFetchExecutor Instance = new MeshFetchExecutor();

        private InstanceHolder() {
        }
    }

    private MeshFetchExecutor() {
        super("MeshHTTPFetch", MESH_FETCH_THREADS, new PriorityBlockingQueue());
    }

    public static MeshFetchExecutor getInstance() {
        return InstanceHolder.Instance;
    }
}
