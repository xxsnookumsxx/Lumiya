package com.lumiyaviewer.lumiya.render.spatial;

import com.lumiyaviewer.lumiya.render.avatar.DrawableAvatar;
import com.lumiyaviewer.lumiya.render.avatar.DrawableAvatarStub;
import com.lumiyaviewer.lumiya.slproto.objects.SLObjectAvatarInfo;
import java.lang.ref.WeakReference;
import javax.annotation.Nonnull;

/* JADX INFO: loaded from: classes.dex */
public class DrawListAvatarEntry extends DrawListObjectEntry {
    private WeakReference<DrawableAvatar> drawableAvatar;
    private WeakReference<DrawableAvatarStub> drawableAvatarStub;

    @Nonnull
    private final SLObjectAvatarInfo objectAvatarInfo;

    public DrawListAvatarEntry(@Nonnull SLObjectAvatarInfo sLObjectAvatarInfo) {
        super(sLObjectAvatarInfo);
        this.drawableAvatar = null;
        this.drawableAvatarStub = null;
        this.objectAvatarInfo = sLObjectAvatarInfo;
    }

    @Override // com.lumiyaviewer.lumiya.render.spatial.DrawListEntry
    public void addToDrawList(@Nonnull DrawList drawList) {
        if (drawList.avatars.size() >= drawList.avatarCountLimit && !this.objectAvatarInfo.isMyAvatar()) {
            WeakReference<DrawableAvatarStub> weakReference = this.drawableAvatarStub;
            DrawableAvatarStub unchecked = weakReference != null ? weakReference.get() : null;
            if (unchecked == null) {
                unchecked = drawList.drawableStore.drawableAvatarStubCache.getUnchecked(this.objectAvatarInfo);
                this.drawableAvatarStub = new WeakReference<>(unchecked);
            }
            drawList.avatarStubs.add(unchecked);
            return;
        }
        WeakReference<DrawableAvatar> weakReference2 = this.drawableAvatar;
        DrawableAvatar unchecked2 = weakReference2 != null ? weakReference2.get() : null;
        if (unchecked2 == null) {
            unchecked2 = drawList.drawableStore.drawableAvatarCache.getUnchecked(this.objectAvatarInfo);
            this.drawableAvatar = new WeakReference<>(unchecked2);
        }
        drawList.avatars.add(unchecked2);
        if (this.objectAvatarInfo.isMyAvatar()) {
            drawList.myAvatar = unchecked2;
        }
    }

    @Nonnull
    public SLObjectAvatarInfo getObjectAvatarInfo() {
        return this.objectAvatarInfo;
    }
}
