package com.lumiyaviewer.lumiya.res.geometry;

import com.lumiyaviewer.lumiya.render.drawable.DrawableGeometry;
import com.lumiyaviewer.lumiya.render.drawable.DrawablePrim;
import com.lumiyaviewer.lumiya.render.glres.textures.GLTextureCache;
import com.lumiyaviewer.lumiya.res.ResourceConsumer;
import com.lumiyaviewer.lumiya.res.ResourceManager;
import com.lumiyaviewer.lumiya.res.ResourceMemoryCache;
import com.lumiyaviewer.lumiya.res.ResourceRequest;
import com.lumiyaviewer.lumiya.res.executors.GeometryComputeExecutor;
import com.lumiyaviewer.lumiya.slproto.prims.PrimDrawParams;
import com.lumiyaviewer.lumiya.utils.HasPriority;
import java.util.concurrent.Future;

/* JADX INFO: loaded from: classes.dex */
public class PrimCache extends ResourceMemoryCache<PrimDrawParams, DrawablePrim> {
    private final GeometryCache geometryCache;
    private final GLTextureCache textureCache;

    private static class PrimRequest extends ResourceRequest<PrimDrawParams, DrawablePrim> implements Runnable, ResourceConsumer, HasPriority {
        private volatile Future<?> computeFuture;
        private volatile DrawableGeometry geometry;
        private final GeometryCache geometryCache;
        private final GLTextureCache glTextureCache;

        public PrimRequest(GLTextureCache gLTextureCache, GeometryCache geometryCache, PrimDrawParams primDrawParams, ResourceManager<PrimDrawParams, DrawablePrim> resourceManager) {
            super(primDrawParams, resourceManager);
            this.glTextureCache = gLTextureCache;
            this.geometryCache = geometryCache;
        }

        @Override // com.lumiyaviewer.lumiya.res.ResourceConsumer
        public void OnResourceReady(Object obj, boolean z) {
            if (!(obj instanceof DrawableGeometry)) {
                completeRequest(null);
            } else {
                this.geometry = (DrawableGeometry) obj;
                this.computeFuture = GeometryComputeExecutor.getInstance().submit(this);
            }
        }

        @Override // com.lumiyaviewer.lumiya.res.ResourceRequest
        public void cancelRequest() {
            Future<?> future = this.computeFuture;
            if (future != null) {
                future.cancel(false);
            }
            this.geometryCache.CancelRequest(this);
            super.cancelRequest();
        }

        @Override // com.lumiyaviewer.lumiya.res.ResourceRequest
        public void execute() {
            this.geometryCache.RequestResource(getParams().getVolumeParams(), this);
        }

        @Override // com.lumiyaviewer.lumiya.utils.HasPriority
        public int getPriority() {
            return getParams().getVolumeParams().isMesh() ? 0 : 1;
        }

        @Override // java.lang.Runnable
        public void run() {
            try {
                completeRequest(new DrawablePrim(getParams(), this.geometry));
            } catch (Exception e) {
                completeRequest(null);
            }
        }
    }

    public PrimCache(GLTextureCache gLTextureCache, GeometryCache geometryCache) {
        this.textureCache = gLTextureCache;
        this.geometryCache = geometryCache;
    }

    protected ResourceRequest<PrimDrawParams, DrawablePrim> CreateNewRequest(PrimDrawParams primDrawParams, ResourceManager<PrimDrawParams, DrawablePrim> resourceManager) {
        return new PrimRequest(this.textureCache, this.geometryCache, primDrawParams, resourceManager);
    }

    
}
