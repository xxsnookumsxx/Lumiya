package com.lumiyaviewer.lumiya.slproto.modules.transfer;

import com.lumiyaviewer.lumiya.slproto.messages.TransferInfo;
import com.lumiyaviewer.lumiya.slproto.messages.TransferPacket;
import com.lumiyaviewer.lumiya.slproto.messages.TransferRequest;
import com.lumiyaviewer.lumiya.slproto.users.manager.assets.AssetKey;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/* JADX INFO: loaded from: classes.dex */
public class SLTransfer {
    public static final int AT_ANIMATION = 20;
    public static final int AT_BODYPART = 13;
    public static final int AT_CALLINGCARD = 2;
    public static final int AT_CATEGORY = 8;
    public static final int AT_CLOTHING = 5;
    public static final int AT_GESTURE = 21;
    public static final int AT_IMAGE_JPEG = 19;
    public static final int AT_IMAGE_TGA = 18;
    public static final int AT_LANDMARK = 3;
    public static final int AT_LOST_AND_FOUND = 16;
    public static final int AT_LSL_BYTECODE = 11;
    public static final int AT_LSL_TEXT = 10;
    public static final int AT_NOTECARD = 7;
    public static final int AT_OBJECT = 6;
    public static final int AT_ROOT_CATEGORY = 9;
    public static final int AT_SCRIPT = 4;
    public static final int AT_SIMSTATE = 22;
    public static final int AT_SNAPSHOT_CATEGORY = 15;
    public static final int AT_SOUND = 1;
    public static final int AT_SOUND_WAV = 17;
    public static final int AT_TEXTURE = 0;
    public static final int AT_TEXTURE_TGA = 12;
    public static final int AT_TRASH = 14;
    public static final int LLTCT_ASSET = 2;
    public static final int LLTCT_MISC = 1;
    public static final int LLTCT_UNKNOWN = 0;
    public static final int LLTST_ASSET = 2;
    public static final int LLTST_FILE = 1;
    public static final int LLTST_SIM_ESTATE = 4;
    public static final int LLTST_SIM_INV_ITEM = 3;
    public static final int LLTST_UNKNOWN = 0;
    public static final int LLTS_ABORT = 3;
    public static final int LLTS_DONE = 1;
    public static final int LLTS_ERROR = -1;
    public static final int LLTS_INSUFFICIENT_PERMISSIONS = -3;
    public static final int LLTS_OK = 0;
    public static final int LLTS_SKIP = 2;
    public static final int LLTS_UNKNOWN_SOURCE = -2;
    public static final int LLTTT_FILE = 1;
    public static final int LLTTT_UNKNOWN = 0;
    public static final int LLTTT_VFILE = 2;
    private final UUID agentID;
    private final int assetType;
    private final UUID assetUUID;
    private final int channelType;
    private byte[] data;
    private final UUID itemUUID;
    private final UUID ownerUUID;
    private final float priority;
    private final UUID sessionID;
    private final int sourceType;
    private final UUID taskUUID;
    private final Map<Integer, TransferPacket> queuedPackets = new ConcurrentHashMap();
    private final UUID transferUUID = UUID.randomUUID();
    private boolean statusKnown = false;
    private int status = -1;
    private int size = 0;
    private int nextPacket = 0;
    private int currentSize = 0;

    SLTransfer(UUID uuid, UUID uuid2, AssetKey assetKey, float f) {
        this.agentID = uuid;
        this.sessionID = uuid2;
        this.channelType = assetKey.channelType();
        this.sourceType = assetKey.sourceType();
        this.priority = f;
        this.assetUUID = assetKey.assetUUID();
        this.assetType = assetKey.assetType();
        this.ownerUUID = assetKey.ownerUUID();
        this.itemUUID = assetKey.itemUUID();
        this.taskUUID = assetKey.taskUUID();
    }

    private void RunQueuedPackets(SLTransferManager sLTransferManager) {
        TransferPacket transferPacket;
        if (this.statusKnown && this.status == 0) {
            while (!this.queuedPackets.isEmpty() && (transferPacket = this.queuedPackets.get(Integer.valueOf(this.nextPacket))) != null) {
                this.queuedPackets.remove(Integer.valueOf(this.nextPacket));
                this.nextPacket++;
                int length = transferPacket.TransferData_Field.Data.length;
                System.arraycopy(transferPacket.TransferData_Field.Data, 0, this.data, this.currentSize, length);
                this.currentSize = length + this.currentSize;
                if (transferPacket.TransferData_Field.Status != 0) {
                    this.status = transferPacket.TransferData_Field.Status;
                }
            }
        }
        if (!this.statusKnown || this.status == 0) {
            return;
        }
        sLTransferManager.EndTransfer(this);
    }

    void HandleTransferInfo(SLTransferManager sLTransferManager, TransferInfo transferInfo) {
        this.statusKnown = true;
        this.status = transferInfo.TransferInfoData_Field.Status;
        this.size = transferInfo.TransferInfoData_Field.Size;
        if (this.status == 0) {
            this.data = new byte[this.size];
        }
        RunQueuedPackets(sLTransferManager);
    }

    void HandleTransferPacket(SLTransferManager sLTransferManager, TransferPacket transferPacket) {
        this.queuedPackets.put(Integer.valueOf(transferPacket.TransferData_Field.Packet), transferPacket);
        RunQueuedPackets(sLTransferManager);
    }

    int getAssetType() {
        return this.assetType;
    }

    UUID getAssetUUID() {
        return this.assetUUID;
    }

    int getChannelType() {
        return this.channelType;
    }

    byte[] getData() {
        return this.data;
    }

    float getPriority() {
        return this.priority;
    }

    int getStatus() {
        return this.status;
    }

    UUID getTransferUUID() {
        return this.transferUUID;
    }

    TransferRequest makeTransferRequest() {
        TransferRequest transferRequest = new TransferRequest();
        transferRequest.TransferInfo_Field.TransferID = this.transferUUID;
        transferRequest.TransferInfo_Field.ChannelType = this.channelType;
        transferRequest.TransferInfo_Field.SourceType = this.sourceType;
        transferRequest.TransferInfo_Field.Priority = this.priority;
        ByteBuffer byteBufferAllocate = ByteBuffer.allocate(1024);
        byteBufferAllocate.order(ByteOrder.BIG_ENDIAN);
        if (this.sourceType == 3) {
            byteBufferAllocate.putLong(this.agentID.getMostSignificantBits());
            byteBufferAllocate.putLong(this.agentID.getLeastSignificantBits());
            byteBufferAllocate.putLong(this.sessionID.getMostSignificantBits());
            byteBufferAllocate.putLong(this.sessionID.getLeastSignificantBits());
            byteBufferAllocate.putLong(this.ownerUUID.getMostSignificantBits());
            byteBufferAllocate.putLong(this.ownerUUID.getLeastSignificantBits());
            if (this.taskUUID != null) {
                byteBufferAllocate.putLong(this.taskUUID.getMostSignificantBits());
                byteBufferAllocate.putLong(this.taskUUID.getLeastSignificantBits());
            } else {
                byteBufferAllocate.putLong(0L);
                byteBufferAllocate.putLong(0L);
            }
            byteBufferAllocate.putLong(this.itemUUID.getMostSignificantBits());
            byteBufferAllocate.putLong(this.itemUUID.getLeastSignificantBits());
        }
        byteBufferAllocate.putLong(this.assetUUID.getMostSignificantBits());
        byteBufferAllocate.putLong(this.assetUUID.getLeastSignificantBits());
        byteBufferAllocate.order(ByteOrder.LITTLE_ENDIAN);
        byteBufferAllocate.putInt(this.assetType);
        byteBufferAllocate.flip();
        transferRequest.TransferInfo_Field.Params = new byte[byteBufferAllocate.limit()];
        byteBufferAllocate.get(transferRequest.TransferInfo_Field.Params, 0, byteBufferAllocate.limit());
        transferRequest.isReliable = true;
        return transferRequest;
    }
}
