package com.lumiyaviewer.lumiya.slproto.avatar;

/* JADX INFO: loaded from: classes.dex */
// Updated for modern Second Life Bento and PBR support
// Now supports 21 baked texture entries (was 6)
public enum BakedTextureIndex {
    BAKED_HEAD(AvatarTextureFaceIndex.TEX_HEAD_BAKED),
    BAKED_UPPER(AvatarTextureFaceIndex.TEX_UPPER_BAKED),
    BAKED_LOWER(AvatarTextureFaceIndex.TEX_LOWER_BAKED),
    BAKED_EYES(AvatarTextureFaceIndex.TEX_EYES_BAKED),
    BAKED_SKIRT(AvatarTextureFaceIndex.TEX_SKIRT_BAKED),
    BAKED_HAIR(AvatarTextureFaceIndex.TEX_HAIR_BAKED),
    BAKED_LEFT_ARM(AvatarTextureFaceIndex.TEX_LEFT_ARM_BAKE),
    BAKED_RIGHT_ARM(AvatarTextureFaceIndex.TEX_RIGHT_ARM_BAKE),
    BAKED_LEFT_LEG(AvatarTextureFaceIndex.TEX_LEFT_LEG_BAKE),
    BAKED_RIGHT_LEG(AvatarTextureFaceIndex.TEX_RIGHT_LEG_BAKE),
    BAKED_SPINE(AvatarTextureFaceIndex.TEX_SPINE_BAKE),
    BAKED_PELVIS(AvatarTextureFaceIndex.TEX_PELVIS_BAKE),
    BAKED_HEAD_PHYSICS(AvatarTextureFaceIndex.TEX_HEAD_PHYSICS_BAKE),
    BAKED_UPPER_PHYSICS(AvatarTextureFaceIndex.TEX_UPPER_PHYSICS_BAKE),
    BAKED_LOWER_PHYSICS(AvatarTextureFaceIndex.TEX_LOWER_PHYSICS_BAKE),
    BAKED_HEAD_NORMAL(AvatarTextureFaceIndex.TEX_HEAD_NORMAL),
    BAKED_UPPER_NORMAL(AvatarTextureFaceIndex.TEX_UPPER_NORMAL),
    BAKED_LOWER_NORMAL(AvatarTextureFaceIndex.TEX_LOWER_NORMAL),
    BAKED_HEAD_SPECULAR(AvatarTextureFaceIndex.TEX_HEAD_SPECULAR),
    BAKED_UPPER_SPECULAR(AvatarTextureFaceIndex.TEX_UPPER_SPECULAR),
    BAKED_LOWER_SPECULAR(AvatarTextureFaceIndex.TEX_LOWER_SPECULAR);

    private AvatarTextureFaceIndex faceIndex;

    BakedTextureIndex(AvatarTextureFaceIndex avatarTextureFaceIndex) {
        this.faceIndex = avatarTextureFaceIndex;
    }

    /* JADX INFO: renamed from: values, reason: to resolve conflict with enum method */
    public static BakedTextureIndex[] valuesCustom() {
        return values();
    }

    public AvatarTextureFaceIndex getFaceIndex() {
        return this.faceIndex;
    }
}
