package com.lumiyaviewer.lumiya.slproto.assets;

import com.google.common.collect.ImmutableList;
import com.lumiyaviewer.lumiya.Debug;
import java.io.UnsupportedEncodingException;
import java.util.UUID;

/* JADX INFO: loaded from: classes.dex */
public class SLWearableData {
    public final String name;
    public final ImmutableList<WearableParam> params;
    public final ImmutableList<WearableTexture> textures;

    static class WearableFormatException extends AssetFormatException {
        WearableFormatException() {
            super("Unsupported wearable format");
        }

        WearableFormatException(Throwable th) {
            super("Unsupported wearable format", th);
        }
    }

    public static class WearableParam {
        public final int paramIndex;
        public final float paramValue;

        WearableParam(int i, float f) {
            this.paramIndex = i;
            this.paramValue = f;
        }
    }

    public static class WearableTexture {
        public final int layer;
        public final UUID textureID;

        WearableTexture(int i, UUID uuid) {
            this.layer = i;
            this.textureID = uuid;
        }
    }

    SLWearableData(byte[] bArr) throws WearableFormatException {
        int i;
        try {
            String[] strArrSplit = new String(bArr, "ISO-8859-1").trim().split("\n+");
            if (strArrSplit.length < 2) {
                throw new WearableFormatException();
            }
            if (!strArrSplit[0].trim().startsWith("LLWearable")) {
                throw new WearableFormatException();
            }
            try {
                this.name = strArrSplit[1];
                ImmutableList.Builder builder = ImmutableList.builder();
                ImmutableList.Builder builder2 = ImmutableList.builder();
                int i2 = 2;
                while (i2 < strArrSplit.length) {
                    String[] strArrSplit2 = strArrSplit[i2].trim().split("\\s+");
                    if (strArrSplit2.length < 1) {
                        i2++;
                    } else if (strArrSplit2[0].equalsIgnoreCase("permissions") || strArrSplit2[0].equalsIgnoreCase("sale_info")) {
                        i2++;
                        if (i2 >= strArrSplit.length) {
                            throw new WearableFormatException();
                        }
                        if (!strArrSplit[i2].trim().equalsIgnoreCase("{")) {
                            throw new WearableFormatException();
                        }
                        while (true) {
                            if (i2 >= strArrSplit.length) {
                                break;
                            }
                            if (strArrSplit[i2].trim().equalsIgnoreCase("}")) {
                                i2++;
                                break;
                            }
                            i2++;
                        }
                    } else {
                        if (strArrSplit2[0].equalsIgnoreCase("parameters")) {
                            int i3 = Integer.parseInt(strArrSplit2[1]);
                            i = i2 + 1;
                            for (int i4 = 0; i4 < i3; i4++) {
                                if (i >= strArrSplit.length) {
                                    throw new WearableFormatException();
                                }
                                try {
                                    try {
                                        String[] strArrSplit3 = strArrSplit[i].trim().split("\\s+");
                                        if (strArrSplit3.length < 2) {
                                            throw new WearableFormatException();
                                        }
                                        builder.add(new WearableParam(Integer.parseInt(strArrSplit3[0]), Float.parseFloat(strArrSplit3[1])));
                                    } catch (NumberFormatException e) {
                                        Debug.Warning(e);
                                    }
                                } catch (WearableFormatException e2) {
                                    Debug.Warning(e2);
                                }
                                i++;
                            }
                        } else if (strArrSplit2[0].equalsIgnoreCase("textures")) {
                            int i5 = Integer.parseInt(strArrSplit2[1]);
                            i = i2 + 1;
                            for (int i6 = 0; i6 < i5; i6++) {
                                if (i >= strArrSplit.length) {
                                    throw new WearableFormatException();
                                }
                                try {
                                    try {
                                        String[] strArrSplit4 = strArrSplit[i].trim().split("\\s+");
                                        if (strArrSplit4.length < 2) {
                                            throw new WearableFormatException();
                                        }
                                        builder2.add(new WearableTexture(Integer.parseInt(strArrSplit4[0]), UUID.fromString(strArrSplit4[1])));
                                    } catch (NumberFormatException e3) {
                                        Debug.Warning(e3);
                                    }
                                } catch (WearableFormatException e4) {
                                    Debug.Warning(e4);
                                }
                                i++;
                            }
                        } else {
                            i2++;
                        }
                        i2 = i;
                    }
                }
                this.params = builder.build();
                this.textures = builder2.build();
            } catch (NumberFormatException e5) {
                throw new WearableFormatException(e5);
            }
        } catch (UnsupportedEncodingException e6) {
            throw new WearableFormatException(e6);
        }
    }
}
