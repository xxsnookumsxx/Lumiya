package com.lumiyaviewer.lumiya.slproto.llsd.types;

import com.lumiyaviewer.lumiya.slproto.llsd.LLSDNode;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;
import org.xmlpull.v1.XmlSerializer;

/* JADX INFO: loaded from: classes.dex */
public class LLSDUUID extends LLSDNode {
    private UUID value;

    public LLSDUUID() {
        this.value = null;
    }

    public LLSDUUID(String str) {
        int length = str.length();
        long j = 0;
        int i = 0;
        int i2 = 0;
        long j2 = 0;
        long j3 = 0;
        int i3 = 0;
        while (i3 < length) {
            char cCharAt = str.charAt(i3);
            if (cCharAt != '-') {
                j = (j << 4) | ((long) ((cCharAt < '0' || cCharAt > '9') ? (cCharAt < 'a' || cCharAt > 'f') ? (cCharAt < 'A' || cCharAt > 'F') ? 0 : (cCharAt - 'A') + 10 : (cCharAt - 'a') + 10 : cCharAt - '0'));
                i++;
                if (i >= 16) {
                    if (i2 == 0) {
                        j3 = j;
                    } else {
                        j2 = j;
                    }
                    i2++;
                }
            }
            i3++;
            j2 = j2;
            j3 = j3;
            i = i;
            i2 = i2;
        }
        this.value = new UUID(j3, j2);
    }

    public LLSDUUID(UUID uuid) {
        this.value = uuid;
    }

    @Override // com.lumiyaviewer.lumiya.slproto.llsd.LLSDNode
    public String asString() {
        return this.value.toString();
    }

    @Override // com.lumiyaviewer.lumiya.slproto.llsd.LLSDNode
    public UUID asUUID() {
        return this.value;
    }

    @Override // com.lumiyaviewer.lumiya.slproto.llsd.LLSDNode
    public void toBinary(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeByte(117);
        dataOutputStream.writeLong(this.value.getMostSignificantBits());
        dataOutputStream.writeLong(this.value.getLeastSignificantBits());
    }

    @Override // com.lumiyaviewer.lumiya.slproto.llsd.LLSDNode
    public void toXML(XmlSerializer xmlSerializer) throws IOException {
        xmlSerializer.startTag("", "uuid");
        if (this.value != null) {
            xmlSerializer.text(this.value.toString());
        }
        xmlSerializer.endTag("", "uuid");
    }
}
