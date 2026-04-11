package com.lumiyaviewer.lumiya.render.picking;

import android.opengl.Matrix;
import com.lumiyaviewer.lumiya.render.RenderContext;
import com.lumiyaviewer.lumiya.slproto.types.LLVector3;
import com.lumiyaviewer.lumiya.slproto.types.LLVector4;

/* JADX INFO: loaded from: classes.dex */
public class GLRayTrace {

    public static class RayIntersectInfo {
        public final LLVector4 intersectPoint;
        public final float s;
        public final float t;

        RayIntersectInfo(LLVector4 lLVector4, float f, float f2) {
            this.intersectPoint = lLVector4;
            this.s = f;
            this.t = f2;
        }

        public String toString() {
            return "RayIntersectInfo{intersectPoint=" + this.intersectPoint + ", s=" + this.s + ", t=" + this.t + '}';
        }
    }

    public static float getIntersectionDepth(RenderContext renderContext, LLVector4 lLVector4, float[] fArr) {
        float[] fArr2 = new float[8];
        Matrix.multiplyMV(fArr2, 0, fArr, 0, new float[]{lLVector4.x, lLVector4.y, lLVector4.z, 1.0f}, 0);
        if (renderContext.hasGL20) {
            Matrix.multiplyMV(fArr2, 4, renderContext.modelViewMatrix.getMatrixData(), renderContext.modelViewMatrix.getMatrixDataOffset(), fArr2, 0);
        } else {
            Matrix.multiplyMV(fArr2, 4, renderContext.projectionMatrix.getMatrixData(), renderContext.projectionMatrix.getMatrixDataOffset(), fArr2, 0);
        }
        return fArr2[6];
    }

    public static RayIntersectInfo intersect_RayTriangle(LLVector3 lLVector3, LLVector3 lLVector32, LLVector3[] lLVector3Arr, int i) {
        LLVector3 lLVector3Sub = LLVector3.sub(lLVector3Arr[i + 1], lLVector3Arr[i + 0]);
        LLVector3 lLVector3Sub2 = LLVector3.sub(lLVector3Arr[i + 2], lLVector3Arr[i + 0]);
        LLVector3 lLVector3Cross = LLVector3.cross(lLVector3Sub, lLVector3Sub2);
        if (lLVector3Cross.isZero()) {
            return null;
        }
        LLVector3 lLVector3Sub3 = LLVector3.sub(lLVector32, lLVector3);
        float f = -lLVector3Cross.dot(LLVector3.sub(lLVector3, lLVector3Arr[i + 0]));
        float fDot = lLVector3Cross.dot(lLVector3Sub3);
        if (Math.abs(fDot) < 1.0E-7f) {
            return null;
        }
        float f2 = f / fDot;
        if (f2 < 0.0d) {
            return null;
        }
        LLVector3 lLVector33 = new LLVector3(lLVector3Sub3);
        lLVector33.mul(f2);
        lLVector33.add(lLVector3);
        float fDot2 = lLVector3Sub.dot(lLVector3Sub);
        float fDot3 = lLVector3Sub.dot(lLVector3Sub2);
        float fDot4 = lLVector3Sub2.dot(lLVector3Sub2);
        LLVector3 lLVector3Sub4 = LLVector3.sub(lLVector33, lLVector3Arr[i + 0]);
        float fDot5 = lLVector3Sub4.dot(lLVector3Sub);
        float fDot6 = lLVector3Sub4.dot(lLVector3Sub2);
        float f3 = (fDot3 * fDot3) - (fDot2 * fDot4);
        if (Math.abs(f3) < 1.0E-7f) {
            return null;
        }
        float f4 = ((fDot3 * fDot6) - (fDot4 * fDot5)) / f3;
        if (f4 < 0.0d || f4 > 1.0d) {
            return null;
        }
        float f5 = ((fDot5 * fDot3) - (fDot6 * fDot2)) / f3;
        if (f5 < 0.0d || f4 + f5 > 1.0d) {
            return null;
        }
        return new RayIntersectInfo(new LLVector4(lLVector33.x, lLVector33.y, lLVector33.z, f2), f4, f5);
    }
}
