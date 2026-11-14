package com.lumiyaviewer.lumiya.slproto.messages;

import com.google.common.primitives.UnsignedBytes;
import com.lumiyaviewer.lumiya.slproto.SLMessage;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

/* JADX INFO: loaded from: classes.dex */
public class DirPeopleReply extends SLMessage {
    public AgentData AgentData_Field;
    public QueryData QueryData_Field;
    public ArrayList<QueryReplies> QueryReplies_Fields = new ArrayList<>();

    public static class AgentData {
        public UUID AgentID;
    }

    public static class QueryData {
        public UUID QueryID;
    }

    public static class QueryReplies {
        public UUID AgentID;
        public byte[] FirstName;
        public byte[] Group;
        public byte[] LastName;
        public boolean Online;
        public int Reputation;
    }

    public DirPeopleReply() {
        this.zeroCoded = true;
        this.AgentData_Field = new AgentData();
        this.QueryData_Field = new QueryData();
    }

    @Override // com.lumiyaviewer.lumiya.slproto.SLMessage
    public int CalcPayloadSize() {
        int length = 37;
        Iterator it = this.QueryReplies_Fields.iterator();
        while (true) {
            int i = length;
            if (!it.hasNext()) {
                return i;
            }
            QueryReplies queryReplies = (QueryReplies) it.next();
            length = queryReplies.Group.length + queryReplies.FirstName.length + 17 + 1 + queryReplies.LastName.length + 1 + 1 + 4 + i;
        }
    }

    @Override // com.lumiyaviewer.lumiya.slproto.SLMessage
    public void Handle(SLMessageHandler sLMessageHandler) {
        sLMessageHandler.HandleDirPeopleReply(this);
    }

    @Override // com.lumiyaviewer.lumiya.slproto.SLMessage
    public void PackPayload(ByteBuffer byteBuffer) {
        byteBuffer.putShort((short) -1);
        byteBuffer.put((byte) 0);
        byteBuffer.put((byte) 36);
        packUUID(byteBuffer, this.AgentData_Field.AgentID);
        packUUID(byteBuffer, this.QueryData_Field.QueryID);
        byteBuffer.put((byte) this.QueryReplies_Fields.size());
        for (QueryReplies queryReplies : this.QueryReplies_Fields) {
            packUUID(byteBuffer, queryReplies.AgentID);
            packVariable(byteBuffer, queryReplies.FirstName, 1);
            packVariable(byteBuffer, queryReplies.LastName, 1);
            packVariable(byteBuffer, queryReplies.Group, 1);
            packBoolean(byteBuffer, queryReplies.Online);
            packInt(byteBuffer, queryReplies.Reputation);
        }
    }

    @Override // com.lumiyaviewer.lumiya.slproto.SLMessage
    public void UnpackPayload(ByteBuffer byteBuffer) {
        this.AgentData_Field.AgentID = unpackUUID(byteBuffer);
        this.QueryData_Field.QueryID = unpackUUID(byteBuffer);
        int i = byteBuffer.get() & 255;
        for (int i2 = 0; i2 < i; i2++) {
            QueryReplies queryReplies = new QueryReplies();
            queryReplies.AgentID = unpackUUID(byteBuffer);
            queryReplies.FirstName = unpackVariable(byteBuffer, 1);
            queryReplies.LastName = unpackVariable(byteBuffer, 1);
            queryReplies.Group = unpackVariable(byteBuffer, 1);
            queryReplies.Online = unpackBoolean(byteBuffer);
            queryReplies.Reputation = unpackInt(byteBuffer);
            this.QueryReplies_Fields.add(queryReplies);
        }
    }
}
