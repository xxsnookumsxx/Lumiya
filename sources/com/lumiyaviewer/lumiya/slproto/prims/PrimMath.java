package com.lumiyaviewer.lumiya.slproto.prims;

import com.lumiyaviewer.lumiya.slproto.types.LLVector3;

/* JADX INFO: loaded from: classes.dex */
public class PrimMath {
    public static final float F_DEG_TO_RAD = 0.017453292f;
    public static final float F_PI = 3.1415927f;

    public static float lerp(float f, float f2, float f3) {
        return ((f2 - f) * f3) + f;
    }

    public static float[] lookAt(LLVector3 lLVector3, LLVector3 lLVector32, LLVector3 lLVector33) {
        LLVector3 lLVector3Sub = LLVector3.sub(lLVector32, lLVector3);
        lLVector3Sub.normVec();
        LLVector3 lLVector34 = new LLVector3(lLVector3Sub);
        lLVector34.setCross(lLVector33);
        return new float[]{lLVector34.x, lLVector33.x, -lLVector3Sub.x, 0.0f, lLVector34.y, lLVector33.y, -lLVector3Sub.y, 0.0f, lLVector34.z, lLVector33.z, -lLVector3Sub.z, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f};
    }
}
