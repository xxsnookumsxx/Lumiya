package com.lumiyaviewer.lumiya.slproto.messages;

import com.google.common.primitives.UnsignedBytes;
import com.lumiyaviewer.lumiya.slproto.SLMessage;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;

/* JADX INFO: loaded from: classes.dex */
public class ScriptDataReply extends SLMessage {
    public ArrayList<DataBlock> DataBlock_Fields = new ArrayList<>();

    public static class DataBlock {
        public long Hash;
        public byte[] Reply;
    }

    public ScriptDataReply() {
        this.zeroCoded = false;
    }

    @Override // com.lumiyaviewer.lumiya.slproto.SLMessage
    public int CalcPayloadSize() {
        int length = 5;
        Iterator it = this.DataBlock_Fields.iterator();
        while (true) {
            int i = length;
            if (!it.hasNext()) {
                return i;
            }
            length = ((DataBlock) it.next()).Reply.length + 10 + i;
        }
    }

    @Override // com.lumiyaviewer.lumiya.slproto.SLMessage
    public void Handle(SLMessageHandler sLMessageHandler) {
        sLMessageHandler.HandleScriptDataReply(this);
    }

    @Override // com.lumiyaviewer.lumiya.slproto.SLMessage
    public void PackPayload(ByteBuffer byteBuffer) {
        byteBuffer.putShort((short) -1);
        byteBuffer.put((byte) 1);
        byteBuffer.put((byte) 82);
        byteBuffer.put((byte) this.DataBlock_Fields.size());
        for (DataBlock dataBlock : this.DataBlock_Fields) {
            packLong(byteBuffer, dataBlock.Hash);
            packVariable(byteBuffer, dataBlock.Reply, 2);
        }
    }

    @Override // com.lumiyaviewer.lumiya.slproto.SLMessage
    public void UnpackPayload(ByteBuffer byteBuffer) {
        int i = byteBuffer.get() & UnsignedBytes.MAX_VALUE;
        for (int i2 = 0; i2 < i; i2++) {
            DataBlock dataBlock = new DataBlock();
            dataBlock.Hash = unpackLong(byteBuffer);
            dataBlock.Reply = unpackVariable(byteBuffer, 2);
            this.DataBlock_Fields.add(dataBlock);
        }
    }
}
