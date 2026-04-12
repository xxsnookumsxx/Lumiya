package com.lumiyaviewer.lumiya.slproto.avatar;

import android.content.res.AssetManager;
import com.lumiyaviewer.lumiya.Debug;
import com.lumiyaviewer.lumiya.LumiyaApp;
import java.io.DataInputStream;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.Map;

/* JADX INFO: loaded from: classes.dex */
public class SLBaseAvatar {
    private Map<MeshIndex, MeshEntry> meshes;

    private static class InstanceHolder {
        private static final SLBaseAvatar Instance = new SLBaseAvatar(null);

        private InstanceHolder() {
        }
    }

    public static class MeshEntry {
        public String meshName;
        public SLPolyMesh polyMesh;
        public AvatarTextureFaceIndex textureFaceIndex;
        public BakedTextureIndex textureIndex;

        public MeshEntry(BakedTextureIndex bakedTextureIndex, AvatarTextureFaceIndex avatarTextureFaceIndex, String str, SLPolyMesh sLPolyMesh) {
            this.textureIndex = bakedTextureIndex;
            this.textureFaceIndex = avatarTextureFaceIndex;
            this.meshName = str;
            this.polyMesh = sLPolyMesh;
        }
    }

    private SLBaseAvatar() {
        this.meshes = new EnumMap(MeshIndex.class);
        this.meshes.put(MeshIndex.MESH_ID_HAIR, new MeshEntry(BakedTextureIndex.BAKED_HAIR, AvatarTextureFaceIndex.TEX_HAIR_BAKED, "hairMesh", loadMesh("avatar_hair")));
        this.meshes.put(MeshIndex.MESH_ID_HEAD, new MeshEntry(BakedTextureIndex.BAKED_HEAD, AvatarTextureFaceIndex.TEX_HEAD_BAKED, "headMesh", loadMesh("avatar_head")));
        this.meshes.put(MeshIndex.MESH_ID_EYELASH, new MeshEntry(BakedTextureIndex.BAKED_HEAD, AvatarTextureFaceIndex.TEX_HEAD_BAKED, "eyelashMesh", loadMesh("avatar_eyelashes")));
        this.meshes.put(MeshIndex.MESH_ID_UPPER_BODY, new MeshEntry(BakedTextureIndex.BAKED_UPPER, AvatarTextureFaceIndex.TEX_UPPER_BAKED, "upperBodyMesh", loadMesh("avatar_upper_body")));
        this.meshes.put(MeshIndex.MESH_ID_LOWER_BODY, new MeshEntry(BakedTextureIndex.BAKED_LOWER, AvatarTextureFaceIndex.TEX_LOWER_BAKED, "lowerBodyMesh", loadMesh("avatar_lower_body")));
        this.meshes.put(MeshIndex.MESH_ID_EYEBALL_LEFT, new MeshEntry(BakedTextureIndex.BAKED_EYES, AvatarTextureFaceIndex.TEX_EYES_BAKED, "eyeBallLeftMesh", loadMesh("avatar_eye")));
        this.meshes.put(MeshIndex.MESH_ID_EYEBALL_RIGHT, new MeshEntry(BakedTextureIndex.BAKED_EYES, AvatarTextureFaceIndex.TEX_EYES_BAKED, "eyeBallRightMesh", loadMesh("avatar_eye")));
        this.meshes.put(MeshIndex.MESH_ID_SKIRT, new MeshEntry(BakedTextureIndex.BAKED_SKIRT, AvatarTextureFaceIndex.TEX_SKIRT_BAKED, "skirtMesh", loadMesh("avatar_skirt")));
        
        // Bento mesh parts - head region (loaded from assets if available, null otherwise)
        this.meshes.put(MeshIndex.MESH_ID_FACE_JAW, new MeshEntry(BakedTextureIndex.BAKED_HEAD, AvatarTextureFaceIndex.TEX_HEAD_BAKED, "jawMesh", loadMesh("avatar_jaw")));
        this.meshes.put(MeshIndex.MESH_ID_FACE_CHIN, new MeshEntry(BakedTextureIndex.BAKED_HEAD, AvatarTextureFaceIndex.TEX_HEAD_BAKED, "chinMesh", loadMesh("avatar_chin")));
        this.meshes.put(MeshIndex.MESH_ID_FACE_NOSE, new MeshEntry(BakedTextureIndex.BAKED_HEAD, AvatarTextureFaceIndex.TEX_HEAD_BAKED, "noseMesh", loadMesh("avatar_nose")));
        this.meshes.put(MeshIndex.MESH_ID_FACE_LEYE, new MeshEntry(BakedTextureIndex.BAKED_EYES, AvatarTextureFaceIndex.TEX_EYES_BAKED, "leftEyeMesh", loadMesh("avatar_eye_left")));
        this.meshes.put(MeshIndex.MESH_ID_FACE_REYE, new MeshEntry(BakedTextureIndex.BAKED_EYES, AvatarTextureFaceIndex.TEX_EYES_BAKED, "rightEyeMesh", loadMesh("avatar_eye_right")));
        this.meshes.put(MeshIndex.MESH_ID_FACE_TONGUE, new MeshEntry(BakedTextureIndex.BAKED_HEAD, AvatarTextureFaceIndex.TEX_HEAD_BAKED, "tongueMesh", loadMesh("avatar_tongue")));
        
        // Bento mesh parts - torso
        this.meshes.put(MeshIndex.MESH_ID_TORSO, new MeshEntry(BakedTextureIndex.BAKED_UPPER, AvatarTextureFaceIndex.TEX_UPPER_BAKED, "torsoMesh", loadMesh("avatar_torso")));
        this.meshes.put(MeshIndex.MESH_ID_CHEST, new MeshEntry(BakedTextureIndex.BAKED_UPPER, AvatarTextureFaceIndex.TEX_UPPER_BAKED, "chestMesh", loadMesh("avatar_chest")));
        this.meshes.put(MeshIndex.MESH_ID_PELVIS, new MeshEntry(BakedTextureIndex.BAKED_LOWER, AvatarTextureFaceIndex.TEX_LOWER_BAKED, "pelvisMesh", loadMesh("avatar_pelvis")));
        
        // Bento mesh parts - arms (Bento decomposition)
        this.meshes.put(MeshIndex.MESH_ID_LCLAVICLE, new MeshEntry(BakedTextureIndex.BAKED_LEFT_ARM, AvatarTextureFaceIndex.TEX_LEFT_ARM_BAKE, "leftClavicleMesh", loadMesh("avatar_lclavicle")));
        this.meshes.put(MeshIndex.MESH_ID_RCLAVICLE, new MeshEntry(BakedTextureIndex.BAKED_RIGHT_ARM, AvatarTextureFaceIndex.TEX_RIGHT_ARM_BAKE, "rightClavicleMesh", loadMesh("avatar_rclavicle")));
        this.meshes.put(MeshIndex.MESH_ID_LUPPER_ARM, new MeshEntry(BakedTextureIndex.BAKED_LEFT_ARM, AvatarTextureFaceIndex.TEX_LEFT_ARM_BAKE, "leftUpperArmMesh", loadMesh("avatar_lupper_arm")));
        this.meshes.put(MeshIndex.MESH_ID_RUPPER_ARM, new MeshEntry(BakedTextureIndex.BAKED_RIGHT_ARM, AvatarTextureFaceIndex.TEX_RIGHT_ARM_BAKE, "rightUpperArmMesh", loadMesh("avatar_rupper_arm")));
        this.meshes.put(MeshIndex.MESH_ID_LLOWER_ARM, new MeshEntry(BakedTextureIndex.BAKED_LEFT_ARM, AvatarTextureFaceIndex.TEX_LEFT_ARM_BAKE, "leftLowerArmMesh", loadMesh("avatar_llower_arm")));
        this.meshes.put(MeshIndex.MESH_ID_RLOWER_ARM, new MeshEntry(BakedTextureIndex.BAKED_RIGHT_ARM, AvatarTextureFaceIndex.TEX_RIGHT_ARM_BAKE, "rightLowerArmMesh", loadMesh("avatar_rlower_arm")));
        this.meshes.put(MeshIndex.MESH_ID_LHAND, new MeshEntry(BakedTextureIndex.BAKED_LEFT_HAND, AvatarTextureFaceIndex.TEX_LEFT_HAND_BAKE, "leftHandMesh", loadMesh("avatar_lhand")));
        this.meshes.put(MeshIndex.MESH_ID_RHAND, new MeshEntry(BakedTextureIndex.BAKED_RIGHT_HAND, AvatarTextureFaceIndex.TEX_RIGHT_HAND_BAKE, "rightHandMesh", loadMesh("avatar_rhand")));
        
        // Bento mesh parts - legs (Bento decomposition)
        this.meshes.put(MeshIndex.MESH_ID_LUPPER_LEG, new MeshEntry(BakedTextureIndex.BAKED_LEFT_LEG, AvatarTextureFaceIndex.TEX_LEFT_LEG_BAKE, "leftUpperLegMesh", loadMesh("avatar_lupper_leg")));
        this.meshes.put(MeshIndex.MESH_ID_RUPPER_LEG, new MeshEntry(BakedTextureIndex.BAKED_RIGHT_LEG, AvatarTextureFaceIndex.TEX_RIGHT_LEG_BAKE, "rightUpperLegMesh", loadMesh("avatar_rupper_leg")));
        this.meshes.put(MeshIndex.MESH_ID_LLOWER_LEG, new MeshEntry(BakedTextureIndex.BAKED_LEFT_LEG, AvatarTextureFaceIndex.TEX_LEFT_LEG_BAKE, "leftLowerLegMesh", loadMesh("avatar_llower_leg")));
        this.meshes.put(MeshIndex.MESH_ID_RLOWER_LEG, new MeshEntry(BakedTextureIndex.BAKED_RIGHT_LEG, AvatarTextureFaceIndex.TEX_RIGHT_LEG_BAKE, "rightLowerLegMesh", loadMesh("avatar_rlower_leg")));
        this.meshes.put(MeshIndex.MESH_ID_LFOOT, new MeshEntry(BakedTextureIndex.BAKED_LEFT_LEG, AvatarTextureFaceIndex.TEX_LEFT_LEG_BAKE, "leftFootMesh", loadMesh("avatar_lfoot")));
        this.meshes.put(MeshIndex.MESH_ID_RFOOT, new MeshEntry(BakedTextureIndex.BAKED_RIGHT_LEG, AvatarTextureFaceIndex.TEX_RIGHT_LEG_BAKE, "rightFootMesh", loadMesh("avatar_rfoot")));
        
        // Bento mesh parts - wings
        this.meshes.put(MeshIndex.MESH_ID_LWING, new MeshEntry(BakedTextureIndex.BAKED_LEFT_WING, AvatarTextureFaceIndex.TEX_LEFT_WING_BAKE, "leftWingMesh", loadMesh("avatar_lwing")));
        this.meshes.put(MeshIndex.MESH_ID_RWING, new MeshEntry(BakedTextureIndex.BAKED_RIGHT_WING, AvatarTextureFaceIndex.TEX_RIGHT_WING_BAKE, "rightWingMesh", loadMesh("avatar_rwing")));
        
        // Bento mesh parts - tail
        this.meshes.put(MeshIndex.MESH_ID_TAIL_1, new MeshEntry(BakedTextureIndex.BAKED_TAIL, AvatarTextureFaceIndex.TEX_TAIL_BAKE, "tail1Mesh", loadMesh("avatar_tail1")));
        this.meshes.put(MeshIndex.MESH_ID_TAIL_2, new MeshEntry(BakedTextureIndex.BAKED_TAIL, AvatarTextureFaceIndex.TEX_TAIL_BAKE, "tail2Mesh", loadMesh("avatar_tail2")));
        this.meshes.put(MeshIndex.MESH_ID_TAIL_3, new MeshEntry(BakedTextureIndex.BAKED_TAIL, AvatarTextureFaceIndex.TEX_TAIL_BAKE, "tail3Mesh", loadMesh("avatar_tail3")));
        this.meshes.put(MeshIndex.MESH_ID_TAIL_4, new MeshEntry(BakedTextureIndex.BAKED_TAIL, AvatarTextureFaceIndex.TEX_TAIL_BAKE, "tail4Mesh", loadMesh("avatar_tail4")));
        
        // Bento mesh parts - hindlimbs
        this.meshes.put(MeshIndex.MESH_ID_LHINDLIMB, new MeshEntry(BakedTextureIndex.BAKED_LEFT_HINDLIMB, AvatarTextureFaceIndex.TEX_LEFT_HINDLIMB_BAKE, "leftHindlimbMesh", loadMesh("avatar_lhindlimb")));
        this.meshes.put(MeshIndex.MESH_ID_RHINDLIMB, new MeshEntry(BakedTextureIndex.BAKED_RIGHT_HINDLIMB, AvatarTextureFaceIndex.TEX_RIGHT_HINDLIMB_BAKE, "rightHindlimbMesh", loadMesh("avatar_rhindlimb")));
    }

    /* synthetic */ SLBaseAvatar(SLBaseAvatar sLBaseAvatar) {
        this();
    }

    public static SLBaseAvatar getInstance() {
        return InstanceHolder.Instance;
    }

    private SLPolyMesh loadMesh(String str) {
        DataInputStream dataInputStream;
        InputStream inputStreamOpen;
        Debug.Printf("BaseAvatar: loading mesh for " + str, new Object[0]);
        try {
            AssetManager assetManager = LumiyaApp.getAssetManager();
            InputStream inputStreamOpen2 = assetManager.open("character/" + str + ".lbm");
            if (str.equals("avatar_head")) {
                inputStreamOpen = assetManager.open("character/" + str + ".lbm_0");
                dataInputStream = new DataInputStream(inputStreamOpen);
            } else {
                dataInputStream = null;
                inputStreamOpen = null;
            }
            SLPolyMesh sLPolyMesh = new SLPolyMesh(new DataInputStream(inputStreamOpen2), dataInputStream);
            inputStreamOpen2.close();
            if (inputStreamOpen != null) {
                inputStreamOpen.close();
            }
            return sLPolyMesh;
        } catch (Exception e) {
            Debug.Warning(e);
            return null;
        }
    }

    public MeshEntry getMeshEntry(MeshIndex meshIndex) {
        return this.meshes.get(meshIndex);
    }
}
