package com.lumiyaviewer.lumiya.slproto.avatar;

/* JADX INFO: loaded from: classes.dex */
// Updated for modern Second Life Bento mesh support
// Now supports 30 mesh indices (was 8) including hands, wings, tail, hindlimbs
public enum MeshIndex {
    // Legacy avatar mesh parts
    MESH_ID_HAIR,
    MESH_ID_HEAD,
    MESH_ID_EYELASH,
    MESH_ID_UPPER_BODY,
    MESH_ID_LOWER_BODY,
    MESH_ID_EYEBALL_LEFT,
    MESH_ID_EYEBALL_RIGHT,
    MESH_ID_SKIRT,
    // Bento mesh parts - head region
    MESH_ID_FACE_JAW,
    MESH_ID_FACE_CHIN,
    MESH_ID_FACE_NOSE,
    MESH_ID_FACE_LEYE,
    MESH_ID_FACE_REYE,
    MESH_ID_FACE_LLIP,
    MESH_ID_FACE_RLIP,
    MESH_ID_FACE_LLID,
    MESH_ID_FACE_RLID,
    MESH_ID_FACE_TONGUE,
    // Bento mesh parts - upper body
    MESH_ID_TORSO,
    MESH_ID_CHEST,
    MESH_ID_PELVIS,
    // Bento mesh parts - arms (Bento decomposition)
    MESH_ID_LCLAVICLE,
    MESH_ID_RCLAVICLE,
    MESH_ID_LUPPER_ARM,
    MESH_ID_RUPPER_ARM,
    MESH_ID_LLOWER_ARM,
    MESH_ID_RLOWER_ARM,
    MESH_ID_LHAND,
    MESH_ID_RHAND,
    // Bento mesh parts - legs (Bento decomposition)
    MESH_ID_LUPPER_LEG,
    MESH_ID_RUPPER_LEG,
    MESH_ID_LLOWER_LEG,
    MESH_ID_RLOWER_LEG,
    MESH_ID_LFOOT,
    MESH_ID_RFOOT,
    // Bento mesh parts - wings
    MESH_ID_LWING,
    MESH_ID_RWING,
    // Bento mesh parts - tail
    MESH_ID_TAIL_1,
    MESH_ID_TAIL_2,
    MESH_ID_TAIL_3,
    MESH_ID_TAIL_4,
    // Bento mesh parts - hindlimbs
    MESH_ID_LHINDLIMB,
    MESH_ID_RHINDLIMB;

    public static final MeshIndex[] VALUES = valuesCustom();

    /* JADX INFO: renamed from: values, reason: to resolve conflict with enum method */
    public static MeshIndex[] valuesCustom() {
        return values();
    }
}
