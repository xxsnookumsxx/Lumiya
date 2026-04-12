package com.lumiyaviewer.lumiya.slproto.avatar;

/* JADX INFO: loaded from: classes.dex */
// Updated for modern Second Life Bento and PBR support
// Now supports 38 baked texture entries (was 6)
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
    BAKED_LOWER_SPECULAR(AvatarTextureFaceIndex.TEX_LOWER_SPECULAR),
    // Bento mesh bakes - hands
    BAKED_LEFT_HAND(AvatarTextureFaceIndex.TEX_LEFT_HAND_BAKE),
    BAKED_RIGHT_HAND(AvatarTextureFaceIndex.TEX_RIGHT_HAND_BAKE),
    BAKED_LEFT_HAND_NORMAL(AvatarTextureFaceIndex.TEX_LEFT_HAND_NORMAL),
    BAKED_RIGHT_HAND_NORMAL(AvatarTextureFaceIndex.TEX_RIGHT_HAND_NORMAL),
    // Bento mesh bakes - wings
    BAKED_LEFT_WING(AvatarTextureFaceIndex.TEX_LEFT_WING_BAKE),
    BAKED_RIGHT_WING(AvatarTextureFaceIndex.TEX_RIGHT_WING_BAKE),
    BAKED_LEFT_WING_NORMAL(AvatarTextureFaceIndex.TEX_LEFT_WING_NORMAL),
    BAKED_RIGHT_WING_NORMAL(AvatarTextureFaceIndex.TEX_RIGHT_WING_NORMAL),
    // Bento mesh bakes - tail
    BAKED_TAIL(AvatarTextureFaceIndex.TEX_TAIL_BAKE),
    BAKED_TAIL_NORMAL(AvatarTextureFaceIndex.TEX_TAIL_NORMAL),
    // Bento mesh bakes - hindlimbs
    BAKED_LEFT_HINDLIMB(AvatarTextureFaceIndex.TEX_LEFT_HINDLIMB_BAKE),
    BAKED_RIGHT_HINDLIMB(AvatarTextureFaceIndex.TEX_RIGHT_HINDLIMB_BAKE),
    BAKED_LEFT_HINDLIMB_NORMAL(AvatarTextureFaceIndex.TEX_LEFT_HINDLIMB_NORMAL),
    BAKED_RIGHT_HINDLIMB_NORMAL(AvatarTextureFaceIndex.TEX_RIGHT_HINDLIMB_NORMAL),
    // Additional PBR specular for limbs
    BAKED_LEFT_ARM_SPECULAR(AvatarTextureFaceIndex.TEX_LEFT_ARM_SPECULAR),
    BAKED_RIGHT_ARM_SPECULAR(AvatarTextureFaceIndex.TEX_RIGHT_ARM_SPECULAR),
    BAKED_LEFT_LEG_SPECULAR(AvatarTextureFaceIndex.TEX_LEFT_LEG_SPECULAR),
    BAKED_RIGHT_LEG_SPECULAR(AvatarTextureFaceIndex.TEX_RIGHT_LEG_SPECULAR),
    // PBR shiny (new SL material system)
    BAKED_HEAD_SHINY(AvatarTextureFaceIndex.TEX_HEAD_SHINY),
    BAKED_UPPER_SHINY(AvatarTextureFaceIndex.TEX_UPPER_SHINY),
    BAKED_LOWER_SHINY(AvatarTextureFaceIndex.TEX_LOWER_SHINY);

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
