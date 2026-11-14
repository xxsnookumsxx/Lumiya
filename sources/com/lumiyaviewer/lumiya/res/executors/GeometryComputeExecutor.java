package com.lumiyaviewer.lumiya.res.executors;

import java.util.concurrent.PriorityBlockingQueue;

/**
 * Multi-threaded priority executor for thread-safe mesh and prim geometry construction,
 * kept separate from single-threaded PrimComputeExecutor (used by SpatialObjectIndex).
 */
public class GeometryComputeExecutor extends WeakExecutor {
    private static final int COMPUTE_THREADS = 4;

    private static class InstanceHolder {
        private static final GeometryComputeExecutor Instance = new GeometryComputeExecutor();

        private InstanceHolder() {
        }
    }

    private GeometryComputeExecutor() {
        super("GeometryCompute", COMPUTE_THREADS, new PriorityBlockingQueue());
    }

    public static GeometryComputeExecutor getInstance() {
        return InstanceHolder.Instance;
    }
}
