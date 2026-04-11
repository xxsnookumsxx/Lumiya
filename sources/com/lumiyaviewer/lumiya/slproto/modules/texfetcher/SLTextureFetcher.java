package com.lumiyaviewer.lumiya.slproto.modules.texfetcher;

import com.lumiyaviewer.lumiya.Debug;
import com.lumiyaviewer.lumiya.render.tex.TexturePriority;
import com.lumiyaviewer.lumiya.slproto.SLAgentCircuit;
import com.lumiyaviewer.lumiya.slproto.caps.SLCaps;
import com.lumiyaviewer.lumiya.slproto.handler.SLMessageHandler;
import com.lumiyaviewer.lumiya.slproto.messages.ImageNotInDatabase;
import com.lumiyaviewer.lumiya.slproto.modules.SLIdleHandler;
import com.lumiyaviewer.lumiya.slproto.modules.SLModule;
import com.lumiyaviewer.lumiya.utils.PriorityBinQueue;
import java.io.File;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/* JADX INFO: loaded from: classes.dex */
public class SLTextureFetcher extends SLModule implements SLIdleHandler {
    private static final int MAX_UDP_TRANSFERS = 2;
    private String agentAppearanceService;
    private String capURL;
    private long lastCheckForStalls;
    private PriorityBinQueue<SLTextureFetchRequest> udpQueue;
    private Map<UUID, TextureUDPTransfer> udpTransfers;

    public SLTextureFetcher(SLAgentCircuit sLAgentCircuit, SLCaps sLCaps, String str) {
        super(sLAgentCircuit);
        this.capURL = null;
        this.agentAppearanceService = null;
        this.udpTransfers = new ConcurrentHashMap();
        this.udpQueue = new PriorityBinQueue<>(TexturePriority.valuesCustom().length);
        this.lastCheckForStalls = 0L;
        this.agentAppearanceService = str;
        this.capURL = sLCaps.getCapability(SLCaps.SLCapability.GetTexture);
        Debug.Log("TextureFetcher: capURL = " + this.capURL);
    }

    private synchronized void RunUDPQueue() {
        SLTextureFetchRequest sLTextureFetchRequestPoll;
        if (this.udpTransfers.size() < 2 && (sLTextureFetchRequestPoll = this.udpQueue.poll()) != null) {
            TextureUDPTransfer textureUDPTransfer = new TextureUDPTransfer(sLTextureFetchRequestPoll.destFile, sLTextureFetchRequestPoll);
            this.udpTransfers.put(sLTextureFetchRequestPoll.textureID, textureUDPTransfer);
            textureUDPTransfer.StartTransfer(this.agentCircuit, this.circuitInfo);
        }
    }

    public void BeginFetch(SLTextureFetchRequest sLTextureFetchRequest) {
        SLTextureFetchRequest sLTextureFetchRequest2 = null;
        synchronized (this) {
            File file = sLTextureFetchRequest.destFile;
            if (file.exists()) {
                sLTextureFetchRequest.outputFile = file;
                sLTextureFetchRequest2 = sLTextureFetchRequest;
            } else {
                this.udpQueue.add(sLTextureFetchRequest);
                RunUDPQueue();
            }
        }
        if (sLTextureFetchRequest2 == null || sLTextureFetchRequest2.onFetchComplete == null) {
            return;
        }
        sLTextureFetchRequest2.onFetchComplete.OnTextureFetchComplete(sLTextureFetchRequest);
    }

    public synchronized void CancelFetch(SLTextureFetchRequest sLTextureFetchRequest) {
        this.udpQueue.remove(sLTextureFetchRequest);
        this.udpTransfers.remove(sLTextureFetchRequest.textureID);
        RunUDPQueue();
    }

    @Override // com.lumiyaviewer.lumiya.slproto.modules.SLModule
    public void HandleCloseCircuit() {
        StopFetching();
        super.HandleCloseCircuit();
    }

    /* JADX WARN: Removed duplicated region for block: B:18:0x0037  */
    @com.lumiyaviewer.lumiya.slproto.handler.SLMessageHandler
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public void HandleImageData(com.lumiyaviewer.lumiya.slproto.messages.ImageData r4) {
        /*
            r3 = this;
            r1 = 0
            monitor-enter(r3)
            java.util.Map<java.util.UUID, com.lumiyaviewer.lumiya.slproto.modules.texfetcher.TextureUDPTransfer> r0 = r3.udpTransfers     // Catch: java.lang.Throwable -> L34
            com.lumiyaviewer.lumiya.slproto.messages.ImageData$ImageID r2 = r4.ImageID_Field     // Catch: java.lang.Throwable -> L34
            java.util.UUID r2 = r2.ID     // Catch: java.lang.Throwable -> L34
            java.lang.Object r0 = r0.get(r2)     // Catch: java.lang.Throwable -> L34
            com.lumiyaviewer.lumiya.slproto.modules.texfetcher.TextureUDPTransfer r0 = (com.lumiyaviewer.lumiya.slproto.modules.texfetcher.TextureUDPTransfer) r0     // Catch: java.lang.Throwable -> L34
            if (r0 == 0) goto L37
            r0.HandleImageData(r4)     // Catch: java.lang.Throwable -> L34
            boolean r2 = r0.isCompleted()     // Catch: java.lang.Throwable -> L34
            if (r2 == 0) goto L37
            java.util.Map<java.util.UUID, com.lumiyaviewer.lumiya.slproto.modules.texfetcher.TextureUDPTransfer> r1 = r3.udpTransfers     // Catch: java.lang.Throwable -> L34
            com.lumiyaviewer.lumiya.slproto.messages.ImageData$ImageID r2 = r4.ImageID_Field     // Catch: java.lang.Throwable -> L34
            java.util.UUID r2 = r2.ID     // Catch: java.lang.Throwable -> L34
            r1.remove(r2)     // Catch: java.lang.Throwable -> L34
            com.lumiyaviewer.lumiya.slproto.modules.texfetcher.SLTextureFetchRequest r0 = r0.fetchReq     // Catch: java.lang.Throwable -> L34
            r3.RunUDPQueue()     // Catch: java.lang.Throwable -> L34
        L27:
            monitor-exit(r3)
            if (r0 == 0) goto L33
            com.lumiyaviewer.lumiya.slproto.modules.texfetcher.SLTextureFetchRequest$TextureFetchCompleteListener r1 = r0.onFetchComplete
            if (r1 == 0) goto L33
            com.lumiyaviewer.lumiya.slproto.modules.texfetcher.SLTextureFetchRequest$TextureFetchCompleteListener r1 = r0.onFetchComplete
            r1.OnTextureFetchComplete(r0)
        L33:
            return
        L34:
            r0 = move-exception
            monitor-exit(r3)
            throw r0
        L37:
            r0 = r1
            goto L27
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lumiyaviewer.lumiya.slproto.modules.texfetcher.SLTextureFetcher.HandleImageData(com.lumiyaviewer.lumiya.slproto.messages.ImageData):void");
    }

    @SLMessageHandler
    public void HandleImageNotInDatabase(ImageNotInDatabase imageNotInDatabase) {
        SLTextureFetchRequest sLTextureFetchRequest;
        synchronized (this) {
            Debug.Log("TextureUDP: Image not in database: " + imageNotInDatabase.ImageID_Field.ID);
            TextureUDPTransfer textureUDPTransferRemove = this.udpTransfers.remove(imageNotInDatabase.ImageID_Field.ID);
            sLTextureFetchRequest = textureUDPTransferRemove != null ? textureUDPTransferRemove.fetchReq : null;
        }
        if (sLTextureFetchRequest != null && sLTextureFetchRequest.onFetchComplete != null) {
            sLTextureFetchRequest.onFetchComplete.OnTextureFetchComplete(sLTextureFetchRequest);
        }
        RunUDPQueue();
    }

    /* JADX WARN: Removed duplicated region for block: B:19:0x003e  */
    @com.lumiyaviewer.lumiya.slproto.handler.SLMessageHandler
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public void HandleImagePacket(com.lumiyaviewer.lumiya.slproto.messages.ImagePacket r4) {
        /*
            r3 = this;
            r1 = 0
            monitor-enter(r3)
            java.util.Map<java.util.UUID, com.lumiyaviewer.lumiya.slproto.modules.texfetcher.TextureUDPTransfer> r0 = r3.udpTransfers     // Catch: java.lang.Throwable -> L3b
            com.lumiyaviewer.lumiya.slproto.messages.ImagePacket$ImageID r2 = r4.ImageID_Field     // Catch: java.lang.Throwable -> L3b
            java.util.UUID r2 = r2.ID     // Catch: java.lang.Throwable -> L3b
            java.lang.Object r0 = r0.get(r2)     // Catch: java.lang.Throwable -> L3b
            com.lumiyaviewer.lumiya.slproto.modules.texfetcher.TextureUDPTransfer r0 = (com.lumiyaviewer.lumiya.slproto.modules.texfetcher.TextureUDPTransfer) r0     // Catch: java.lang.Throwable -> L3b
            if (r0 == 0) goto L3e
            r0.HandleImagePacket(r4)     // Catch: java.lang.Throwable -> L3b
            boolean r2 = r0.isCompleted()     // Catch: java.lang.Throwable -> L3b
            if (r2 == 0) goto L3e
            java.util.Map<java.util.UUID, com.lumiyaviewer.lumiya.slproto.modules.texfetcher.TextureUDPTransfer> r1 = r3.udpTransfers     // Catch: java.lang.Throwable -> L3b
            com.lumiyaviewer.lumiya.slproto.messages.ImagePacket$ImageID r2 = r4.ImageID_Field     // Catch: java.lang.Throwable -> L3b
            java.util.UUID r2 = r2.ID     // Catch: java.lang.Throwable -> L3b
            r1.remove(r2)     // Catch: java.lang.Throwable -> L3b
            com.lumiyaviewer.lumiya.slproto.modules.texfetcher.SLTextureFetchRequest r1 = r0.fetchReq     // Catch: java.lang.Throwable -> L3b
            java.io.File r0 = r0.getOutputFile()     // Catch: java.lang.Throwable -> L3b
            r1.outputFile = r0     // Catch: java.lang.Throwable -> L3b
            r3.RunUDPQueue()     // Catch: java.lang.Throwable -> L3b
            r0 = r1
        L2e:
            monitor-exit(r3)
            if (r0 == 0) goto L3a
            com.lumiyaviewer.lumiya.slproto.modules.texfetcher.SLTextureFetchRequest$TextureFetchCompleteListener r1 = r0.onFetchComplete
            if (r1 == 0) goto L3a
            com.lumiyaviewer.lumiya.slproto.modules.texfetcher.SLTextureFetchRequest$TextureFetchCompleteListener r1 = r0.onFetchComplete
            r1.OnTextureFetchComplete(r0)
        L3a:
            return
        L3b:
            r0 = move-exception
            monitor-exit(r3)
            throw r0
        L3e:
            r0 = r1
            goto L2e
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lumiyaviewer.lumiya.slproto.modules.texfetcher.SLTextureFetcher.HandleImagePacket(com.lumiyaviewer.lumiya.slproto.messages.ImagePacket):void");
    }

    @Override // com.lumiyaviewer.lumiya.slproto.modules.SLIdleHandler
    public void ProcessIdle() {
        HashSet hashSet;
        HashSet hashSet2 = null;
        long jCurrentTimeMillis = System.currentTimeMillis();
        if (jCurrentTimeMillis >= this.lastCheckForStalls + 1000) {
            this.lastCheckForStalls = jCurrentTimeMillis;
            try {
                Iterator<T> it = this.udpTransfers.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry entry = (Map.Entry) it.next();
                    if (!((TextureUDPTransfer) entry.getValue()).hasStalled() || ((TextureUDPTransfer) entry.getValue()).RetryTransfer(this.agentCircuit, this.circuitInfo)) {
                        hashSet = hashSet2;
                    } else {
                        Debug.Printf("Cannot retry texture %s", ((UUID) entry.getKey()).toString());
                        HashSet hashSet3 = hashSet2 == null ? new HashSet() : hashSet2;
                        hashSet3.add((UUID) entry.getKey());
                        hashSet = hashSet3;
                    }
                    hashSet2 = hashSet;
                }
                if (hashSet2 != null) {
                    Iterator it2 = hashSet2.iterator();
                    while (it2.hasNext()) {
                        TextureUDPTransfer textureUDPTransferRemove = this.udpTransfers.remove((UUID) it2.next());
                        if (textureUDPTransferRemove != null) {
                            SLTextureFetchRequest sLTextureFetchRequest = textureUDPTransferRemove.fetchReq;
                            sLTextureFetchRequest.outputFile = null;
                            if (sLTextureFetchRequest.onFetchComplete != null) {
                                sLTextureFetchRequest.onFetchComplete.OnTextureFetchComplete(sLTextureFetchRequest);
                            }
                        }
                    }
                    RunUDPQueue();
                }
            } catch (ConcurrentModificationException e) {
                Debug.Warning(e);
            }
        }
    }

    public void StopFetching() {
        this.udpQueue.clear();
    }

    public void UpdatePriority(SLTextureFetchRequest sLTextureFetchRequest) {
        this.udpQueue.updatePriority(sLTextureFetchRequest);
    }

    public String getAgentAppearanceService() {
        return this.agentAppearanceService;
    }

    public String getCapURL() {
        return this.capURL;
    }
}
