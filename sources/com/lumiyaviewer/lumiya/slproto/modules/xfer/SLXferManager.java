package com.lumiyaviewer.lumiya.slproto.modules.xfer;

import com.lumiyaviewer.lumiya.slproto.SLAgentCircuit;
import com.lumiyaviewer.lumiya.slproto.handler.SLMessageHandler;
import com.lumiyaviewer.lumiya.slproto.messages.SendXferPacket;
import com.lumiyaviewer.lumiya.slproto.modules.SLModule;
import com.lumiyaviewer.lumiya.slproto.modules.xfer.SLXfer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/* JADX INFO: loaded from: classes.dex */
public class SLXferManager extends SLModule {
    private Map<String, Long> activeTransferIDs;
    private Map<Long, SLXfer> activeTransfers;
    private AtomicLong nextID;

    public SLXferManager(SLAgentCircuit sLAgentCircuit) {
        super(sLAgentCircuit);
        this.activeTransfers = Collections.synchronizedMap(new HashMap());
        this.activeTransferIDs = Collections.synchronizedMap(new HashMap());
        this.nextID = new AtomicLong(1L);
    }

    @SLMessageHandler
    public synchronized void HandleSendXferPacket(SendXferPacket sendXferPacket) {
        Long lValueOf = Long.valueOf(sendXferPacket.XferID_Field.ID);
        SLXfer sLXfer = this.activeTransfers.get(lValueOf);
        if (sLXfer != null) {
            sLXfer.HandleDataPacket(this, sendXferPacket);
            if (sLXfer.isCompleted()) {
                this.activeTransfers.remove(lValueOf);
                this.activeTransferIDs.remove(sLXfer.getFilename());
                sLXfer.invokeListeners();
            }
        }
    }

    public synchronized void RequestXfer(String str, ELLPath eLLPath, boolean z, SLXfer.SLXferCompletionListener sLXferCompletionListener, Object obj) {
        SLXfer sLXfer;
        Long l = this.activeTransferIDs.get(str);
        if (l != null && (sLXfer = this.activeTransfers.get(l)) != null) {
            sLXfer.addListener(sLXferCompletionListener, obj);
            return;
        }
        Long lValueOf = Long.valueOf(this.nextID.incrementAndGet());
        this.activeTransferIDs.put(str, lValueOf);
        SLXfer sLXfer2 = new SLXfer(lValueOf.longValue(), str, eLLPath, z);
        sLXfer2.addListener(sLXferCompletionListener, obj);
        this.activeTransfers.put(lValueOf, sLXfer2);
        sLXfer2.StartTransfer(this);
    }
}
