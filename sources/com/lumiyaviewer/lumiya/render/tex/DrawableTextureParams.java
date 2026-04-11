package com.lumiyaviewer.lumiya.render.tex;

import com.lumiyaviewer.lumiya.slproto.avatar.AvatarTextureFaceIndex;
import java.io.File;
import java.util.UUID;
import javax.annotation.Nullable;

/* JADX INFO: loaded from: classes.dex */
public abstract class DrawableTextureParams {
    public static DrawableTextureParams create(UUID uuid, TextureClass textureClass) {
        return new AutoValue_DrawableTextureParams(uuid, textureClass, null, null);
    }

    public static DrawableTextureParams create(UUID uuid, @Nullable AvatarTextureFaceIndex avatarTextureFaceIndex, @Nullable UUID uuid2) {
        return new AutoValue_DrawableTextureParams(uuid, TextureClass.Baked, avatarTextureFaceIndex, uuid2);
    }

    @Nullable
    public abstract AvatarTextureFaceIndex avatarFaceIndex();

    @Nullable
    public abstract UUID avatarUUID();

    public final File getTextureRawPath(File file, boolean z) {
        int iHashCode = uuid().hashCode();
        int i = ((iHashCode >> 24) ^ (((iHashCode >> 8) ^ iHashCode) ^ (iHashCode >> 16))) & 255;
        String storePath = textureClass().getStorePath();
        if (textureClass() == TextureClass.Prim && z) {
            storePath = storePath + "-hq";
        }
        return new File(file, String.format("%s-raw/%02x/%s.raw", storePath, Integer.valueOf(i), uuid().toString()));
    }

    public abstract TextureClass textureClass();

    public abstract UUID uuid();
}
