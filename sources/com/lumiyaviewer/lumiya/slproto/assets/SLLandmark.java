package com.lumiyaviewer.lumiya.slproto.assets;

import com.lumiyaviewer.lumiya.slproto.types.LLVector3;
import java.io.UnsupportedEncodingException;
import java.util.UUID;

/* JADX INFO: loaded from: classes.dex */
public class SLLandmark {
    public LLVector3 localPos;
    public UUID regionUUID;

    public static class LandmarkFormatException extends AssetFormatException {
        private static final long serialVersionUID = -1927623876075592027L;

        public LandmarkFormatException() {
            super("Unsupported landmark format");
        }
    }

    public SLLandmark(byte[] bArr) throws LandmarkFormatException {
        try {
            String[] strArrSplit = new String(bArr, "ISO-8859-1").trim().split("\n+");
            if (strArrSplit.length < 1) {
                throw new LandmarkFormatException();
            }
            if (!strArrSplit[0].trim().equalsIgnoreCase("Landmark version 2")) {
                throw new LandmarkFormatException();
            }
            for (int i = 1; i < strArrSplit.length; i++) {
                String[] strArrSplit2 = strArrSplit[i].trim().split("\\s+");
                if (strArrSplit2.length >= 1) {
                    if (strArrSplit2[0].equalsIgnoreCase("region_id")) {
                        this.regionUUID = UUID.fromString(strArrSplit2[1]);
                    } else if (strArrSplit2[0].equalsIgnoreCase("local_pos")) {
                        this.localPos = new LLVector3();
                        this.localPos.x = Float.parseFloat(strArrSplit2[1]);
                        this.localPos.y = Float.parseFloat(strArrSplit2[2]);
                        this.localPos.z = Float.parseFloat(strArrSplit2[3]);
                    }
                }
            }
        } catch (UnsupportedEncodingException e) {
            throw new LandmarkFormatException();
        }
    }
}
