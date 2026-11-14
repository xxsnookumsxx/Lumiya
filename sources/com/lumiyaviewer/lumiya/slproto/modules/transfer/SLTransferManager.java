package com.lumiyaviewer.lumiya.slproto.modules.transfer;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import com.lumiyaviewer.lumiya.Debug;
import com.lumiyaviewer.lumiya.react.AsyncRequestHandler;
import com.lumiyaviewer.lumiya.react.RequestHandler;
import com.lumiyaviewer.lumiya.react.ResultHandler;
import com.lumiyaviewer.lumiya.res.anim.AnimationCache;
import com.lumiyaviewer.lumiya.res.executors.HTTPFetchExecutor;
import com.lumiyaviewer.lumiya.slproto.SLAgentCircuit;
import com.lumiyaviewer.lumiya.slproto.caps.SLCaps;
import com.lumiyaviewer.lumiya.slproto.handler.SLMessageHandler;
import com.lumiyaviewer.lumiya.slproto.https.SLHTTPSConnection;
import com.lumiyaviewer.lumiya.slproto.inventory.SLAssetType;
import com.lumiyaviewer.lumiya.slproto.messages.TransferAbort;
import com.lumiyaviewer.lumiya.slproto.messages.TransferInfo;
import com.lumiyaviewer.lumiya.slproto.messages.TransferPacket;
import com.lumiyaviewer.lumiya.slproto.modules.SLModule;
import com.lumiyaviewer.lumiya.slproto.users.manager.UserManager;
import com.lumiyaviewer.lumiya.slproto.users.manager.assets.AssetData;
import com.lumiyaviewer.lumiya.slproto.users.manager.assets.AssetKey;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.Nonnull;
import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/* JADX INFO: loaded from: classes.dex */
public class SLTransferManager extends SLModule {
    private static final float DEFAULT_PRIORITY = 10000.0f;
    private final Map<AssetKey, HttpAssetTask> activeHttpTransfers;
    private final BiMap<AssetKey, UUID> activeTransferIds;
    private final Map<UUID, SLTransfer> activeTransfers;
    private final RequestHandler<AssetKey> assetRequestHandler;
    private final ResultHandler<AssetKey, AssetData> assetResultHandler;
    private final UserManager userManager;

    private static final class HttpAssetTask {
        final AtomicBoolean cancelled = new AtomicBoolean(false);
        volatile Call call;
        volatile Future<?> future;

        HttpAssetTask() {
        }

        void cancel() {
            this.cancelled.set(true);
            Call c = this.call;
            if (c != null) {
                c.cancel();
            }
            Future<?> f = this.future;
            if (f != null) {
                f.cancel(true);
            }
        }
    }

    public SLTransferManager(SLAgentCircuit sLAgentCircuit) {
        super(sLAgentCircuit);
        this.activeTransfers = Collections.synchronizedMap(new HashMap());
        this.activeTransferIds = Maps.synchronizedBiMap(HashBiMap.create());
        this.activeHttpTransfers = Collections.synchronizedMap(new HashMap());
        this.assetRequestHandler = new AsyncRequestHandler(this.agentCircuit, new RequestHandler<AssetKey>() { // from class: com.lumiyaviewer.lumiya.slproto.modules.transfer.SLTransferManager.1
            @Override // com.lumiyaviewer.lumiya.react.RequestHandler
            public void onRequest(@Nonnull AssetKey assetKey) {
                Debug.Printf("Transfer: Requested asset download for %s", assetKey);
                if (SLTransferManager.this.tryBeginHttpTransfer(assetKey)) {
                    return;
                }
                SLTransferManager.this.beginUdpTransfer(assetKey);
            }

            /* JADX WARN: Multi-variable type inference failed */
            @Override // com.lumiyaviewer.lumiya.react.RequestHandler
            public void onRequestCancelled(@Nonnull AssetKey assetKey) {
                HttpAssetTask httpTask = SLTransferManager.this.activeHttpTransfers.remove(assetKey);
                if (httpTask != null) {
                    httpTask.cancel();
                }
                SLTransfer sLTransfer;
                UUID uuid = (UUID) SLTransferManager.this.activeTransferIds.remove(assetKey);
                if (uuid == null || (sLTransfer = (SLTransfer) SLTransferManager.this.activeTransfers.get(uuid)) == null) {
                    return;
                }
                SLTransferManager.this.CancelTransfer(sLTransfer);
            }
        });
        this.userManager = UserManager.getUserManager(this.agentCircuit.circuitInfo.agentID);
        if (this.userManager != null) {
            AnimationCache.getInstance().setAssetResponseCacher(this.userManager.getAssetResponseCacher());
        }
        this.assetResultHandler = this.userManager != null ? this.userManager.getAssetResponseCacher().getRequestSource().attachRequestHandler(this.assetRequestHandler) : null;
    }

    private static boolean isViewerAssetFetchable(SLAssetType assetType) {
        switch (assetType) {
            case AT_TEXTURE:
            case AT_SOUND:
            case AT_LANDMARK:
            case AT_CLOTHING:
            case AT_BODYPART:
            case AT_ANIMATION:
            case AT_GESTURE:
            case AT_SETTINGS:
            case AT_MATERIAL:
                return true;
            default:
                return false;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void beginUdpTransfer(AssetKey assetKey) {
        if (this.agentCircuit == null || this.circuitInfo == null) {
            return;
        }
        SLTransfer sLTransfer = new SLTransfer(this.circuitInfo.agentID, this.circuitInfo.sessionID, assetKey, DEFAULT_PRIORITY);
        this.activeTransferIds.forcePut(assetKey, sLTransfer.getTransferUUID());
        BeginTransfer(sLTransfer);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean tryBeginHttpTransfer(final AssetKey assetKey) {
        if (assetKey.sourceType() != 2 || assetKey.assetUUID() == null || this.agentCircuit == null) {
            return false;
        }
        SLCaps caps = this.agentCircuit.getCaps();
        if (caps == null) {
            return false;
        }
        String viewerAssetCap = caps.getCapability(SLCaps.SLCapability.ViewerAsset);
        if (viewerAssetCap == null || viewerAssetCap.isEmpty()) {
            return false;
        }
        SLAssetType assetType = SLAssetType.getByType(assetKey.assetType());
        if (!isViewerAssetFetchable(assetType)) {
            return false;
        }
        String separator = viewerAssetCap.contains("?") ? "&" : (viewerAssetCap.endsWith("/") ? "?" : "/?");
        final String url = viewerAssetCap + separator + assetType.getStringCode() + "_id=" + assetKey.assetUUID().toString();
        final HttpAssetTask task = new HttpAssetTask();
        HttpAssetTask oldTask = this.activeHttpTransfers.put(assetKey, task);
        if (oldTask != null) {
            oldTask.cancel();
        }
        task.future = HTTPFetchExecutor.getInstance().submit(new Runnable() { // from class: com.lumiyaviewer.lumiya.slproto.modules.transfer.SLTransferManager.2
            @Override // java.lang.Runnable
            public void run() {
                if (task.cancelled.get() || Thread.currentThread().isInterrupted()) {
                    SLTransferManager.this.activeHttpTransfers.remove(assetKey);
                    return;
                }
                Response response = null;
                byte[] bytes = null;
                try {
                    Call call = SLHTTPSConnection.getOkHttpClient().newCall(new Request.Builder().url(url).build());
                    task.call = call;
                    response = call.execute();
                    if (response.isSuccessful()) {
                        ResponseBody body = response.body();
                        if (body != null) {
                            bytes = body.bytes();
                        }
                    } else {
                        Debug.Printf("Transfer: ViewerAsset HTTP %d for %s", Integer.valueOf(response.code()), url);
                    }
                } catch (IOException e) {
                    if (!task.cancelled.get()) {
                        Debug.Printf("Transfer: ViewerAsset HTTP error for %s: %s", url, e.getMessage());
                    }
                } finally {
                    if (response != null) {
                        response.close();
                    }
                    SLTransferManager.this.activeHttpTransfers.remove(assetKey);
                }
                if (task.cancelled.get() || Thread.currentThread().isInterrupted()) {
                    return;
                }
                if (bytes != null && bytes.length > 0) {
                    Debug.Printf("Transfer: ViewerAsset HTTP succeeded for %s (%d bytes)", assetKey.assetUUID(), Integer.valueOf(bytes.length));
                    if (SLTransferManager.this.assetResultHandler != null) {
                        SLTransferManager.this.assetResultHandler.onResultData(assetKey, new AssetData(1, bytes));
                    }
                    return;
                }
                Debug.Printf("Transfer: Falling back to UDP for asset %s", assetKey.assetUUID());
                SLTransferManager.this.beginUdpTransfer(assetKey);
            }
        });
        return true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void BeginTransfer(SLTransfer sLTransfer) {
        Debug.Printf("Transfer: Starting transfer: assetUUID %s, assetType %d", sLTransfer.getAssetUUID().toString(), Integer.valueOf(sLTransfer.getAssetType()));
        this.activeTransfers.put(sLTransfer.getTransferUUID(), sLTransfer);
        this.agentCircuit.SendMessage(sLTransfer.makeTransferRequest());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void CancelTransfer(SLTransfer sLTransfer) {
        this.activeTransfers.remove(sLTransfer.getTransferUUID());
        TransferAbort transferAbort = new TransferAbort();
        transferAbort.TransferInfo_Field.TransferID = sLTransfer.getTransferUUID();
        transferAbort.TransferInfo_Field.ChannelType = sLTransfer.getChannelType();
        transferAbort.isReliable = true;
        this.agentCircuit.SendMessage(transferAbort);
    }

    void EndTransfer(SLTransfer sLTransfer) {
        int status;
        this.activeTransfers.remove(sLTransfer.getTransferUUID());
        AssetKey assetKeyRemove = this.activeTransferIds.inverse().remove(sLTransfer.getTransferUUID());
        if (assetKeyRemove == null || this.assetResultHandler == null || (status = sLTransfer.getStatus()) == 3 || status == 0) {
            return;
        }
        this.assetResultHandler.onResultData(assetKeyRemove, new AssetData(status, sLTransfer.getData()));
    }

    @Override // com.lumiyaviewer.lumiya.slproto.modules.SLModule
    public void HandleCloseCircuit() {
        synchronized (this.activeHttpTransfers) {
            for (HttpAssetTask task : this.activeHttpTransfers.values()) {
                task.cancel();
            }
            this.activeHttpTransfers.clear();
        }
        AnimationCache.getInstance().setAssetResponseCacher(null);
        if (this.userManager != null) {
            this.userManager.getAssetResponseCacher().getRequestSource().detachRequestHandler(this.assetRequestHandler);
        }
        super.HandleCloseCircuit();
    }

    @SLMessageHandler
    public void HandleTransferInfo(TransferInfo transferInfo) {
        SLTransfer sLTransfer = this.activeTransfers.get(transferInfo.TransferInfoData_Field.TransferID);
        if (sLTransfer != null) {
            Debug.Log(String.format("Transfer: Info recd, status %d, size %d", Integer.valueOf(transferInfo.TransferInfoData_Field.Status), Integer.valueOf(transferInfo.TransferInfoData_Field.Size)));
            sLTransfer.HandleTransferInfo(this, transferInfo);
        }
    }

    @SLMessageHandler
    public void HandleTransferPacket(TransferPacket transferPacket) {
        SLTransfer sLTransfer = this.activeTransfers.get(transferPacket.TransferData_Field.TransferID);
        if (sLTransfer != null) {
            Debug.Log(String.format("Transfer: data recd, packet %d, status %d, size %d.", Integer.valueOf(transferPacket.TransferData_Field.Packet), Integer.valueOf(transferPacket.TransferData_Field.Status), Integer.valueOf(transferPacket.TransferData_Field.Data.length)));
            sLTransfer.HandleTransferPacket(this, transferPacket);
        }
    }
}
