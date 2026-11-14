package com.lumiyaviewer.lumiya.res.mesh;

import com.google.common.io.ByteStreams;
import com.google.common.net.HttpHeaders;
import com.lumiyaviewer.lumiya.Debug;
import com.lumiyaviewer.lumiya.GlobalOptions;
import com.lumiyaviewer.lumiya.res.ResourceFileCache;
import com.lumiyaviewer.lumiya.res.ResourceManager;
import com.lumiyaviewer.lumiya.res.ResourceRequest;
import com.lumiyaviewer.lumiya.res.executors.MeshFetchExecutor;
import com.lumiyaviewer.lumiya.slproto.https.SLHTTPSConnection;
import com.lumiyaviewer.lumiya.slproto.mesh.MeshData;
import com.lumiyaviewer.lumiya.utils.HasPriority;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.Future;
import okhttp3.Request;
import okhttp3.Response;

/* JADX INFO: loaded from: classes.dex */
public class MeshCache extends ResourceFileCache<UUID, MeshData> {
    private static final int MAX_ATTEMPTS = 3;
    private static volatile File baseDir;
    private final Object capURLlock = new Object();
    private volatile String capURL = null;

    private class MeshDownloadRequest extends ResourceRequest<UUID, MeshData> implements Runnable, HasPriority {
        private volatile Future<?> downloadTask;
        private final File outputFile;

        public MeshDownloadRequest(UUID uuid, ResourceManager<UUID, MeshData> resourceManager, File file) {
            super(uuid, resourceManager);
            this.outputFile = file;
        }

        @Override // com.lumiyaviewer.lumiya.res.ResourceRequest
        public void cancelRequest() {
            Future<?> future = this.downloadTask;
            if (future != null) {
                future.cancel(true);
            }
            super.cancelRequest();
        }

        @Override // com.lumiyaviewer.lumiya.res.ResourceRequest
        public void execute() {
            this.downloadTask = MeshFetchExecutor.getInstance().submit(this);
        }

        @Override // com.lumiyaviewer.lumiya.utils.HasPriority
        public int getPriority() {
            return 0;
        }

        @Override // java.lang.Runnable
        public void run() {
            String str;
            synchronized (MeshCache.this.capURLlock) {
                str = MeshCache.this.capURL;
                if (str == null) {
                    try {
                        MeshCache.this.capURLlock.wait(15000L);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    str = MeshCache.this.capURL;
                }
            }
            if (str == null || (this.downloadTask != null && this.downloadTask.isCancelled())) {
                if (str == null) {
                    Debug.AlwaysPrintf("MeshFetch: aborted mesh %s (capURL null after wait)", getParams());
                }
                completeRequest(null);
                return;
            }
            File file = new File(this.outputFile.getAbsolutePath() + ".tmp");
            String str2 = str + (str.contains("?") ? "&" : (str.endsWith("/") ? "?" : "/?")) + "mesh_id=" + getParams().toString();
            Debug.Printf("Fetching mesh: %s", str2);
            for (int i = 0; i < MAX_ATTEMPTS; i++) {
                try {
                    Response responseExecute = SLHTTPSConnection.getOkHttpClient().newCall(new Request.Builder().url(str2).header(HttpHeaders.ACCEPT, "application/octet-stream").build()).execute();
                    if (responseExecute == null) {
                        throw new IOException("Null response");
                    }
                    try {
                        if (!responseExecute.isSuccessful()) {
                            throw new IOException("Error response code " + responseExecute.code());
                        }
                        File parentFile = file.getParentFile();
                        if (parentFile != null) {
                            parentFile.mkdirs();
                        }
                        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file));
                        long jCopy = ByteStreams.copy(responseExecute.body().byteStream(), bufferedOutputStream);
                        bufferedOutputStream.flush();
                        bufferedOutputStream.close();
                        File parentFile2 = this.outputFile.getParentFile();
                        if (parentFile2 != null) {
                            parentFile2.mkdirs();
                        }
                        file.renameTo(this.outputFile);
                        Debug.Printf("MeshFetch: Saved %d bytes to %s", Long.valueOf(jCopy), this.outputFile.toString());
                        completeRequest(new MeshData(this.outputFile));
                        return;
                    } finally {
                        responseExecute.close();
                    }
                } catch (IOException e2) {
                    Debug.Warning(e2);
                    Debug.AlwaysPrintf("MeshFetch: attempt %d/%d failed for %s: %s", Integer.valueOf(i + 1), Integer.valueOf(MAX_ATTEMPTS), getParams(), e2.getMessage());
                }
            }
            if (this.downloadTask != null && this.downloadTask.isCancelled()) {
                return;
            }
            Debug.AlwaysPrintf("MeshFetch: exhausted retries for mesh %s", getParams());
            completeRequest(null);
        }
    }

    private static File getBaseDir() {
        if (baseDir == null) {
            baseDir = GlobalOptions.getInstance().getCacheDir("mesh");
        }
        return baseDir;
    }

    public static void onCacheDirChanged() {
        baseDir = null;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.lumiyaviewer.lumiya.res.ResourceFileCache
    public MeshData createResourceFromFile(UUID uuid, File file) {
        try {
            return new MeshData(file);
        } catch (IOException e) {
            Debug.Warning(e);
            Debug.AlwaysPrintf("MeshCache: failed to parse cached mesh %s: %s", uuid, e.getMessage());
            return null;
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.lumiyaviewer.lumiya.res.ResourceFileCache
    public ResourceRequest<UUID, MeshData> createResourceGenRequest(UUID uuid, ResourceManager<UUID, MeshData> resourceManager, File file) {
        return new MeshDownloadRequest(uuid, resourceManager, file);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.lumiyaviewer.lumiya.res.ResourceFileCache
    public File getResourceFile(UUID uuid) {
        int iHashCode = uuid.hashCode();
        return new File(getBaseDir(), String.format("%02x/%s.mesh", Integer.valueOf(((iHashCode >> 24) ^ (((iHashCode >> 8) ^ iHashCode) ^ (iHashCode >> 16))) & 255), uuid.toString()));
    }

    public void setCapURL(String str) {
        synchronized (this.capURLlock) {
            this.capURL = str;
            this.capURLlock.notifyAll();
        }
    }
}
