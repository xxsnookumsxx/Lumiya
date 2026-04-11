package com.lumiyaviewer.lumiya.slproto.avatar;

/* JADX INFO: loaded from: classes.dex */
// Updated for modern Second Life Bento and PBR support
// Now supports 54 texture face indices (was 32)
public enum AvatarTextureFaceIndex {
    TEX_HEAD_BODYPAINT("head"),
    TEX_UPPER_SHIRT("upper"),
    TEX_LOWER_PANTS("lower"),
    TEX_EYES_IRIS("eyes"),
    TEX_HAIR("hair"),
    TEX_UPPER_BODYPAINT("upper"),
    TEX_LOWER_BODYPAINT("lower"),
    TEX_LOWER_SHOES("lower"),
    TEX_HEAD_BAKED("head"),
    TEX_UPPER_BAKED("upper"),
    TEX_LOWER_BAKED("lower"),
    TEX_EYES_BAKED("eyes"),
    TEX_LOWER_SOCKS("lower"),
    TEX_UPPER_JACKET("upper"),
    TEX_LOWER_JACKET("lower"),
    TEX_UPPER_GLOVES("upper"),
    TEX_UPPER_UNDERSHIRT("upper"),
    TEX_LOWER_UNDERPANTS("lower"),
    TEX_SKIRT("skirt"),
    TEX_SKIRT_BAKED("skirt"),
    TEX_HAIR_BAKED("hair"),
    TEX_LOWER_ALPHA("lower"),
    TEX_UPPER_ALPHA("upper"),
    TEX_HEAD_ALPHA("head"),
    TEX_EYES_ALPHA("eyes"),
    TEX_HAIR_ALPHA("hair"),
    TEX_HEAD_TATTOO("head"),
    TEX_UPPER_TATTOO("upper"),
    TEX_LOWER_TATTOO("lower"),
    TEX_HEAD_PHYSICS_BAKE("head"),
    TEX_UPPER_PHYSICS_BAKE("upper"),
    TEX_LOWER_PHYSICS_BAKE("lower"),
    TEX_LEFT_ARM_BAKE("upper"),
    TEX_RIGHT_ARM_BAKE("upper"),
    TEX_LEFT_LEG_BAKE("lower"),
    TEX_RIGHT_LEG_BAKE("lower"),
    TEX_SPINE_BAKE("lower"),
    TEX_PELVIS_BAKE("lower"),
    TEX_HEAD_NORMAL("head"),
    TEX_UPPER_NORMAL("upper"),
    TEX_LOWER_NORMAL("lower"),
    TEX_LEFT_ARM_NORMAL("upper"),
    TEX_RIGHT_ARM_NORMAL("upper"),
    TEX_LEFT_LEG_NORMAL("lower"),
    TEX_RIGHT_LEG_NORMAL("lower"),
    TEX_SPINE_NORMAL("lower"),
    TEX_PELVIS_NORMAL("lower"),
    TEX_HEAD_SPECULAR("head"),
    TEX_UPPER_SPECULAR("upper"),
    TEX_LOWER_SPECULAR("lower");

    private String bakedTextureName;

    AvatarTextureFaceIndex(String str) {
        this.bakedTextureName = str;
    }

    /* JADX INFO: renamed from: values, reason: to resolve conflict with enum method */
    public static AvatarTextureFaceIndex[] valuesCustom() {
        return values();
    }

    public String getBakedTextureName() {
        return this.bakedTextureName;
    }
}
