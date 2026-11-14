package com.lumiyaviewer.lumiya.slproto.prims;

import com.lumiyaviewer.lumiya.slproto.avatar.AvatarTextureFaceIndex;
import com.lumiyaviewer.lumiya.slproto.textures.SLTextureEntry;
import com.lumiyaviewer.lumiya.slproto.textures.SLTextureEntryFace;
import java.util.Arrays;
import java.util.UUID;
import javax.annotation.Nullable;

/* JADX INFO: loaded from: classes.dex */
public class PrimDrawParams {
    @Nullable
    private final UUID bomAvatarUUID;
    @Nullable
    private final AvatarTextureFaceIndex[] bomFaces;
    @Nullable
    private final AvatarTextureFaceIndex defaultBomFace;
    private final SLTextureEntry textures;
    private final PrimVolumeParams volumeParams;

    public PrimDrawParams(PrimVolumeParams primVolumeParams, SLTextureEntry sLTextureEntry) {
        this(primVolumeParams, sLTextureEntry, null, null, null);
    }

    public PrimDrawParams(PrimVolumeParams primVolumeParams, SLTextureEntry sLTextureEntry, @Nullable UUID bomAvatarUUID, @Nullable AvatarTextureFaceIndex defaultBomFace, @Nullable AvatarTextureFaceIndex[] bomFaces) {
        this.volumeParams = primVolumeParams;
        this.textures = sLTextureEntry;
        this.bomAvatarUUID = bomAvatarUUID;
        this.defaultBomFace = defaultBomFace;
        this.bomFaces = bomFaces;
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || !(obj instanceof PrimDrawParams)) {
            return false;
        }
        PrimDrawParams primDrawParams = (PrimDrawParams) obj;
        if ((this.volumeParams == null) != (primDrawParams.volumeParams == null)) {
            return false;
        }
        if (this.volumeParams != null && !this.volumeParams.equals(primDrawParams.volumeParams)) {
            return false;
        }
        if ((this.textures == null) != (primDrawParams.textures == null)) {
            return false;
        }
        if (this.textures != null && !this.textures.equals(primDrawParams.textures)) {
            return false;
        }
        if ((this.bomAvatarUUID == null) != (primDrawParams.bomAvatarUUID == null)) {
            return false;
        }
        if (this.bomAvatarUUID != null && !this.bomAvatarUUID.equals(primDrawParams.bomAvatarUUID)) {
            return false;
        }
        if (this.defaultBomFace != primDrawParams.defaultBomFace) {
            return false;
        }
        return Arrays.equals(this.bomFaces, primDrawParams.bomFaces);
    }

    @Nullable
    public final UUID getBomAvatarUUID() {
        return this.bomAvatarUUID;
    }

    @Nullable
    public final AvatarTextureFaceIndex getBomFace(@Nullable SLTextureEntryFace face, int faceIndex) {
        if (this.bomAvatarUUID == null || face == null || this.textures == null) {
            return null;
        }
        if (face != this.textures.GetDefaultTexture() && (face.hasAttribute() & 1) != 0) {
            if (this.bomFaces != null && faceIndex >= 0 && faceIndex < this.bomFaces.length) {
                return this.bomFaces[faceIndex];
            }
            return null;
        }
        return this.defaultBomFace;
    }

    public final SLTextureEntry getTextures() {
        return this.textures;
    }

    public final PrimVolumeParams getVolumeParams() {
        return this.volumeParams;
    }

    public int hashCode() {
        int iHashCode = this.volumeParams != null ? this.volumeParams.hashCode() + 0 : 0;
        if (this.textures != null) {
            iHashCode = (iHashCode * 31) + this.textures.hashCode();
        }
        if (this.bomAvatarUUID != null) {
            iHashCode = (iHashCode * 31) + this.bomAvatarUUID.hashCode();
        }
        if (this.defaultBomFace != null) {
            iHashCode = (iHashCode * 31) + this.defaultBomFace.hashCode();
        }
        if (this.bomFaces != null) {
            iHashCode = (iHashCode * 31) + Arrays.hashCode(this.bomFaces);
        }
        return iHashCode;
    }
}
