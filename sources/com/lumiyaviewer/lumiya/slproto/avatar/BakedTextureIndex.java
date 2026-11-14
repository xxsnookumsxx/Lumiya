package com.lumiyaviewer.lumiya.slproto.avatar;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/* JADX INFO: loaded from: classes.dex */
public enum BakedTextureIndex {
    BAKED_HEAD(AvatarTextureFaceIndex.TEX_HEAD_BAKED, UUID.fromString("5a9f4a74-30f2-821c-b88d-70499d3e7183")),
    BAKED_UPPER(AvatarTextureFaceIndex.TEX_UPPER_BAKED, UUID.fromString("ae2de45c-d252-50b8-5c6e-19f39ce79317")),
    BAKED_LOWER(AvatarTextureFaceIndex.TEX_LOWER_BAKED, UUID.fromString("24daea5f-0539-cfcf-047f-fbc40b2786ba")),
    BAKED_EYES(AvatarTextureFaceIndex.TEX_EYES_BAKED, UUID.fromString("52cc6bb6-2ee5-e632-d3ad-50197b1dcb8a")),
    BAKED_SKIRT(AvatarTextureFaceIndex.TEX_SKIRT_BAKED, UUID.fromString("43529ce8-7faa-ad92-165a-bc4078371687")),
    BAKED_HAIR(AvatarTextureFaceIndex.TEX_HAIR_BAKED, UUID.fromString("09aac1fb-6bce-0bee-7d44-caac6dbb6c63")),
    BAKED_LEFT_ARM(AvatarTextureFaceIndex.TEX_LEFT_ARM_BAKED, UUID.fromString("ff62763f-d60a-9855-890b-0c96f8f8cd98")),
    BAKED_LEFT_LEG(AvatarTextureFaceIndex.TEX_LEFT_LEG_BAKED, UUID.fromString("8e915e25-31d1-cc95-ae08-d58a47488251")),
    BAKED_AUX1(AvatarTextureFaceIndex.TEX_AUX1_BAKED, UUID.fromString("9742065b-19b5-297c-858a-29711d539043")),
    BAKED_AUX2(AvatarTextureFaceIndex.TEX_AUX2_BAKED, UUID.fromString("03642e83-2bd1-4eb9-34b4-4c47ed586d2d")),
    BAKED_AUX3(AvatarTextureFaceIndex.TEX_AUX3_BAKED, UUID.fromString("edd51b77-fc10-ce7a-4b3d-011dfc349e4f"));

    private static final Map<UUID, BakedTextureIndex> bakedUuidMap = new HashMap();
    private final UUID bakedTextureUUID;
    private final AvatarTextureFaceIndex faceIndex;

    static {
        for (BakedTextureIndex bakedTextureIndex : values()) {
            bakedUuidMap.put(bakedTextureIndex.bakedTextureUUID, bakedTextureIndex);
        }
    }

    BakedTextureIndex(AvatarTextureFaceIndex avatarTextureFaceIndex, UUID uuid) {
        this.faceIndex = avatarTextureFaceIndex;
        this.bakedTextureUUID = uuid;
    }

    public static BakedTextureIndex getByBakedUUID(UUID uuid) {
        if (uuid == null) {
            return null;
        }
        return bakedUuidMap.get(uuid);
    }

    public static boolean isBakedMagicUUID(UUID uuid) {
        return uuid != null && bakedUuidMap.containsKey(uuid);
    }

    /* JADX INFO: renamed from: values, reason: to resolve conflict with enum method */
    public static BakedTextureIndex[] valuesCustom() {
        return values();
    }

    public UUID getBakedTextureUUID() {
        return this.bakedTextureUUID;
    }

    public AvatarTextureFaceIndex getFaceIndex() {
        return this.faceIndex;
    }
}
