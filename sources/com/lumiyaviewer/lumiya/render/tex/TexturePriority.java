package com.lumiyaviewer.lumiya.render.tex;

/* JADX INFO: loaded from: classes.dex */
public enum TexturePriority {
    Asset,
    Sculpt,
    PrimVisibleClose,
    PrimVisibleMedium,
    Terrain,
    PrimVisibleFar,
    PrimInvisible,
    Lowest;

    /* JADX INFO: renamed from: values, reason: to resolve conflict with enum method */
    public static TexturePriority[] valuesCustom() {
        return values();
    }
}
