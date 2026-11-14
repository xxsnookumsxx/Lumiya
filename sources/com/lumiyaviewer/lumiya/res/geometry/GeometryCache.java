package com.lumiyaviewer.lumiya.res.geometry;

import com.lumiyaviewer.lumiya.Debug;
import com.lumiyaviewer.lumiya.openjpeg.OpenJPEG;
import com.lumiyaviewer.lumiya.render.drawable.DrawableGeometry;
import com.lumiyaviewer.lumiya.render.tex.DrawableTextureParams;
import com.lumiyaviewer.lumiya.render.tex.TextureClass;
import com.lumiyaviewer.lumiya.res.ResourceConsumer;
import com.lumiyaviewer.lumiya.res.ResourceManager;
import com.lumiyaviewer.lumiya.res.ResourceMemoryCache;
import com.lumiyaviewer.lumiya.res.ResourceRequest;
import com.lumiyaviewer.lumiya.res.executors.GeometryComputeExecutor;
import com.lumiyaviewer.lumiya.res.mesh.MeshCache;
import com.lumiyaviewer.lumiya.res.textures.TextureCache;
import com.lumiyaviewer.lumiya.slproto.mesh.MeshData;
import com.lumiyaviewer.lumiya.slproto.prims.PrimVolumeParams;
import com.lumiyaviewer.lumiya.utils.HasPriority;
import java.util.concurrent.Future;

/* JADX INFO: loaded from: classes.dex */
public class GeometryCache extends ResourceMemoryCache<PrimVolumeParams, DrawableGeometry> {
    private final MeshCache meshCache;

    private static class MeshGeometryRequest extends ResourceRequest<PrimVolumeParams, DrawableGeometry> implements Runnable, ResourceConsumer, HasPriority {
        private volatile Future<?> computeFuture;
        private final MeshCache meshCache;
        private volatile MeshData meshData;

        public MeshGeometryRequest(MeshCache meshCache, PrimVolumeParams primVolumeParams, ResourceManager<PrimVolumeParams, DrawableGeometry> resourceManager) {
            super(primVolumeParams, resourceManager);
            this.meshCache = meshCache;
        }

        @Override // com.lumiyaviewer.lumiya.res.ResourceConsumer
        public void OnResourceReady(Object obj, boolean z) {
            if (!(obj instanceof MeshData)) {
                completeRequest(null);
            } else {
                this.meshData = (MeshData) obj;
                this.computeFuture = GeometryComputeExecutor.getInstance().submit(this);
            }
        }

        @Override // com.lumiyaviewer.lumiya.res.ResourceRequest
        public void cancelRequest() {
            Future<?> future = this.computeFuture;
            if (future != null) {
                future.cancel(false);
            }
            this.meshCache.CancelRequest(this);
            super.cancelRequest();
        }

        @Override // com.lumiyaviewer.lumiya.res.ResourceRequest
        public void execute() {
            this.meshCache.RequestResource(getParams().SculptID, this);
        }

        @Override // com.lumiyaviewer.lumiya.utils.HasPriority
        public int getPriority() {
            return -1;
        }

        @Override // java.lang.Runnable
        public void run() {
            try {
                completeRequest(new DrawableGeometry(this.meshData));
            } catch (Exception e) {
                Debug.Warning(e);
                completeRequest(null);
            }
        }
    }

    private static class SculptGeometryRequest extends ResourceRequest<PrimVolumeParams, DrawableGeometry> implements Runnable, ResourceConsumer, HasPriority {
        private volatile Future<?> computeFuture;
        private volatile OpenJPEG sculptData;
        private final DrawableTextureParams sculptTextureParams;

        public SculptGeometryRequest(PrimVolumeParams primVolumeParams, ResourceManager<PrimVolumeParams, DrawableGeometry> resourceManager) {
            super(primVolumeParams, resourceManager);
            this.sculptTextureParams = DrawableTextureParams.create(getParams().SculptID, TextureClass.Sculpt);
        }

        @Override // com.lumiyaviewer.lumiya.res.ResourceConsumer
        public void OnResourceReady(Object obj, boolean z) {
            if (!(obj instanceof OpenJPEG)) {
                completeRequest(null);
            } else {
                this.sculptData = (OpenJPEG) obj;
                this.computeFuture = GeometryComputeExecutor.getInstance().submit(this);
            }
        }

        @Override // com.lumiyaviewer.lumiya.res.ResourceRequest
        public void cancelRequest() {
            Future<?> future = this.computeFuture;
            if (future != null) {
                future.cancel(false);
            }
            TextureCache.getInstance().CancelRequest(this);
            super.cancelRequest();
        }

        @Override // com.lumiyaviewer.lumiya.res.ResourceRequest
        public void execute() {
            TextureCache.getInstance().RequestResource(this.sculptTextureParams, this);
        }

        @Override // com.lumiyaviewer.lumiya.utils.HasPriority
        public int getPriority() {
            return 0;
        }

        @Override // java.lang.Runnable
        public void run() {
            try {
                completeRequest(new DrawableGeometry(getParams(), this.sculptData));
            } catch (Exception e) {
                Debug.Warning(e);
                completeRequest(null);
            }
        }
    }

    private static class SimpleGeometryRequest extends ResourceRequest<PrimVolumeParams, DrawableGeometry> implements Runnable, HasPriority {
        private volatile Future<?> computeFuture;

        public SimpleGeometryRequest(PrimVolumeParams primVolumeParams, ResourceManager<PrimVolumeParams, DrawableGeometry> resourceManager) {
            super(primVolumeParams, resourceManager);
        }

        @Override // com.lumiyaviewer.lumiya.res.ResourceRequest
        public void cancelRequest() {
            Future<?> future = this.computeFuture;
            if (future != null) {
                future.cancel(false);
            }
            super.cancelRequest();
        }

        @Override // com.lumiyaviewer.lumiya.res.ResourceRequest
        public void execute() {
            this.computeFuture = GeometryComputeExecutor.getInstance().submit(this);
        }

        @Override // com.lumiyaviewer.lumiya.utils.HasPriority
        public int getPriority() {
            return 1;
        }

        @Override // java.lang.Runnable
        public void run() {
            try {
                completeRequest(new DrawableGeometry(getParams(), null));
            } catch (Exception e) {
                Debug.Warning(e);
                completeRequest(null);
            }
        }
    }

    public GeometryCache(MeshCache meshCache) {
        this.meshCache = meshCache;
    }

    protected ResourceRequest<PrimVolumeParams, DrawableGeometry> CreateNewRequest(PrimVolumeParams primVolumeParams, ResourceManager<PrimVolumeParams, DrawableGeometry> resourceManager) {
        return primVolumeParams.isMesh() ? new MeshGeometryRequest(this.meshCache, primVolumeParams, resourceManager) : primVolumeParams.isSculpt() ? new SculptGeometryRequest(primVolumeParams, resourceManager) : new SimpleGeometryRequest(primVolumeParams, resourceManager);
    }

    
}
