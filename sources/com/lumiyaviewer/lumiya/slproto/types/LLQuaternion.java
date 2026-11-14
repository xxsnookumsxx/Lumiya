package com.lumiyaviewer.lumiya.slproto.types;

import com.google.common.primitives.UnsignedBytes;
import java.nio.ByteBuffer;

/* JADX INFO: loaded from: classes.dex */
public class LLQuaternion {

    /* JADX INFO: renamed from: -com-lumiyaviewer-lumiya-slproto-types-LLQuaternion$OrderSwitchesValues, reason: not valid java name */
    private static /* synthetic */ int[] f143xae1ac4c9 = null;
    public static final float FP_MAG_THRESHOLD = 1.0E-7f;
    private float[] inverseMatrix;
    private float[] matrix;
    public float w;
    public float x;
    public float y;
    public float z;

    public enum Order {
        XYZ,
        YZX,
        ZXY,
        XZY,
        YXZ,
        ZYX;

        /* JADX INFO: renamed from: values, reason: to resolve conflict with enum method */
        public static Order[] valuesCustom() {
            return values();
        }
    }

    /* JADX INFO: renamed from: -getcom-lumiyaviewer-lumiya-slproto-types-LLQuaternion$OrderSwitchesValues, reason: not valid java name */
    private static /* synthetic */ int[] m268xd25893a5() {
        if (f143xae1ac4c9 != null) {
            return f143xae1ac4c9;
        }
        int[] iArr = new int[Order.valuesCustom().length];
        try {
            iArr[Order.XYZ.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[Order.XZY.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[Order.YXZ.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[Order.YZX.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[Order.ZXY.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[Order.ZYX.ordinal()] = 6;
        } catch (NoSuchFieldError e6) {
        }
        f143xae1ac4c9 = iArr;
        return iArr;
    }

    public LLQuaternion() {
        this.matrix = null;
        this.inverseMatrix = null;
        this.x = 0.0f;
        this.y = 0.0f;
        this.z = 0.0f;
        this.w = 1.0f;
    }

    public LLQuaternion(float f, float f2, float f3, float f4) {
        this.matrix = null;
        this.inverseMatrix = null;
        this.x = f;
        this.y = f2;
        this.z = f3;
        this.w = f4;
    }

    public LLQuaternion(LLQuaternion lLQuaternion) {
        this.matrix = null;
        this.inverseMatrix = null;
        this.x = lLQuaternion.x;
        this.y = lLQuaternion.y;
        this.z = lLQuaternion.z;
        this.w = lLQuaternion.w;
    }

    public LLQuaternion(float[] fArr) {
        this.matrix = null;
        this.inverseMatrix = null;
        float f = fArr[0] + 1.0f + fArr[5] + fArr[10];
        if (f > 0.5f) {
            float fSqrt = (float) (Math.sqrt(f) * 2.0d);
            this.x = (fArr[9] - fArr[6]) / fSqrt;
            this.y = (fArr[2] - fArr[8]) / fSqrt;
            this.z = (fArr[4] - fArr[1]) / fSqrt;
            this.w = fSqrt * 0.25f;
            return;
        }
        if (fArr[0] > fArr[5] && fArr[0] > fArr[10]) {
            float fSqrt2 = (float) (Math.sqrt(((fArr[0] + 1.0f) - fArr[5]) - fArr[10]) * 2.0d);
            this.x = 0.25f * fSqrt2;
            this.y = (fArr[4] + fArr[1]) / fSqrt2;
            this.z = (fArr[2] + fArr[8]) / fSqrt2;
            this.w = (fArr[9] - fArr[6]) / fSqrt2;
            return;
        }
        if (fArr[5] > fArr[10]) {
            float fSqrt3 = (float) (Math.sqrt(((fArr[5] + 1.0f) - fArr[0]) - fArr[10]) * 2.0d);
            this.x = (fArr[4] + fArr[1]) / fSqrt3;
            this.y = 0.25f * fSqrt3;
            this.z = (fArr[9] + fArr[6]) / fSqrt3;
            this.w = (fArr[2] - fArr[8]) / fSqrt3;
            return;
        }
        float fSqrt4 = (float) (Math.sqrt(((fArr[10] + 1.0f) - fArr[0]) - fArr[5]) * 2.0d);
        this.x = (fArr[2] + fArr[8]) / fSqrt4;
        this.y = (fArr[9] + fArr[6]) / fSqrt4;
        this.z = 0.25f * fSqrt4;
        this.w = (fArr[4] - fArr[1]) / fSqrt4;
    }

    public static LLQuaternion fromEuler(float f, float f2, float f3) {
        double dCos = Math.cos(f / 2.0f);
        double dSin = Math.sin(f / 2.0f);
        double dCos2 = Math.cos(f2 / 2.0f);
        double dSin2 = Math.sin(f2 / 2.0f);
        double dCos3 = Math.cos(f3 / 2.0f);
        double dSin3 = Math.sin(f3 / 2.0f);
        double d = dCos * dCos2;
        double d2 = dSin * dSin2;
        return new LLQuaternion((float) ((dSin * dCos2 * dCos3) + (dCos * dSin2 * dSin3)), (float) (((dCos * dSin2) * dCos3) - ((dSin * dCos2) * dSin3)), (float) ((d * dSin3) + (d2 * dCos3)), (float) ((d * dCos3) - (d2 * dSin3)));
    }

    public static LLQuaternion lerp(LLQuaternion lLQuaternion, LLQuaternion lLQuaternion2, float f) {
        return new LLQuaternion(lLQuaternion.x + ((lLQuaternion2.x - lLQuaternion.x) * f), lLQuaternion.y + ((lLQuaternion2.y - lLQuaternion.y) * f), lLQuaternion.z + ((lLQuaternion2.z - lLQuaternion.z) * f), lLQuaternion.w + ((lLQuaternion2.w - lLQuaternion.w) * f));
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    public static LLQuaternion mayaQ(float f, float f2, float f3, Order order) {
        LLQuaternion lLQuaternion = new LLQuaternion();
        LLQuaternion lLQuaternion2 = new LLQuaternion();
        LLQuaternion lLQuaternion3 = new LLQuaternion();
        lLQuaternion.setQuat(f * 0.017453292f, new LLVector3(1.0f, 0.0f, 0.0f));
        lLQuaternion2.setQuat(f2 * 0.017453292f, new LLVector3(0.0f, 1.0f, 0.0f));
        lLQuaternion3.setQuat(f3 * 0.017453292f, new LLVector3(0.0f, 0.0f, 1.0f));
        LLQuaternion lLQuaternion4 = new LLQuaternion();
        LLQuaternion lLQuaternion5 = new LLQuaternion();
        switch (m268xd25893a5()[order.ordinal()]) {
            case 1:
                lLQuaternion4.setMul(lLQuaternion, lLQuaternion2);
                lLQuaternion5.setMul(lLQuaternion4, lLQuaternion3);
                return lLQuaternion5;
            case 2:
                lLQuaternion4.setMul(lLQuaternion, lLQuaternion3);
                lLQuaternion5.setMul(lLQuaternion4, lLQuaternion2);
                return lLQuaternion5;
            case 3:
                lLQuaternion4.setMul(lLQuaternion2, lLQuaternion);
                lLQuaternion5.setMul(lLQuaternion4, lLQuaternion3);
                return lLQuaternion5;
            case 4:
                lLQuaternion4.setMul(lLQuaternion2, lLQuaternion3);
                lLQuaternion5.setMul(lLQuaternion4, lLQuaternion);
                return lLQuaternion5;
            case 5:
                lLQuaternion4.setMul(lLQuaternion3, lLQuaternion);
                lLQuaternion5.setMul(lLQuaternion4, lLQuaternion2);
                return lLQuaternion5;
            case 6:
                lLQuaternion4.setMul(lLQuaternion3, lLQuaternion2);
                lLQuaternion5.setMul(lLQuaternion4, lLQuaternion);
                return lLQuaternion5;
            default:
                return lLQuaternion5;
        }
    }

    public static LLQuaternion parseFloatVec3(ByteBuffer byteBuffer) {
        float f = byteBuffer.getFloat();
        float f2 = byteBuffer.getFloat();
        float f3 = byteBuffer.getFloat();
        float f4 = 1.0f - (((f * f) + (f2 * f2)) + (f3 * f3));
        return new LLQuaternion(f, f2, f3, f4 > 0.0f ? (float) Math.sqrt(f4) : 0.0f);
    }

    public static LLQuaternion parseU16Vec3(ByteBuffer byteBuffer, float f, float f2) {
        return new LLQuaternion(LLTersePacking.U16_to_float(byteBuffer.getShort() & 65535, f, f2), LLTersePacking.U16_to_float(byteBuffer.getShort() & 65535, f, f2), LLTersePacking.U16_to_float(byteBuffer.getShort() & 65535, f, f2), LLTersePacking.U16_to_float(byteBuffer.getShort() & 65535, f, f2));
    }

    public static LLQuaternion parseU8Vec3(ByteBuffer byteBuffer, float f, float f2) {
        return new LLQuaternion(LLTersePacking.U8_to_float(byteBuffer.get() & UnsignedBytes.MAX_VALUE, f, f2), LLTersePacking.U8_to_float(byteBuffer.get() & UnsignedBytes.MAX_VALUE, f, f2), LLTersePacking.U8_to_float(byteBuffer.get() & UnsignedBytes.MAX_VALUE, f, f2), LLTersePacking.U8_to_float(byteBuffer.get() & UnsignedBytes.MAX_VALUE, f, f2));
    }

    public static LLQuaternion shortestArc(LLVector3 lLVector3, LLVector3 lLVector32) {
        LLVector3 lLVector33 = new LLVector3(lLVector3);
        LLVector3 lLVector34 = new LLVector3(lLVector32);
        float fNormVec = lLVector33.normVec();
        float fNormVec2 = lLVector34.normVec();
        if (fNormVec < 1.0E-7f || fNormVec2 < 1.0E-7f) {
            return new LLQuaternion();
        }
        LLVector3 lLVector3Cross = LLVector3.cross(lLVector33, lLVector34);
        float fDot = lLVector33.dot(lLVector34);
        if (fDot > 0.9999999f) {
            return new LLQuaternion();
        }
        if (fDot >= -0.9999999f) {
            float fAcos = (float) Math.acos(fDot);
            LLQuaternion lLQuaternion = new LLQuaternion();
            lLQuaternion.setQuat(fAcos, lLVector3Cross);
            return lLQuaternion;
        }
        LLVector3 lLVector35 = new LLVector3(lLVector33);
        lLVector35.mul(lLVector33.x / lLVector33.dot(lLVector33));
        LLVector3 lLVector36 = new LLVector3(1.0f, 0.0f, 0.0f);
        lLVector36.sub(lLVector35);
        if (lLVector36.normVec() < 1.0E-7f) {
            lLVector36.set(0.0f, 0.0f, 1.0f);
        }
        return new LLQuaternion(lLVector36.x, lLVector36.y, lLVector36.z, 0.0f);
    }

    public static LLQuaternion unpackFromVector3(LLVector3 lLVector3) {
        float fMagVecSquared = 1.0f - lLVector3.magVecSquared();
        return new LLQuaternion(lLVector3.x, lLVector3.y, lLVector3.z, fMagVecSquared > 0.0f ? (float) Math.sqrt(fMagVecSquared) : 0.0f);
    }

    public void addMul(LLQuaternion lLQuaternion, float f) {
        this.x += lLQuaternion.x * f;
        this.y += lLQuaternion.y * f;
        this.z += lLQuaternion.z * f;
        this.w += lLQuaternion.w * f;
    }

    public LLQuaternion conjQuat() {
        return new LLQuaternion(this.x * (-1.0f), this.y * (-1.0f), this.z * (-1.0f), this.w);
    }

    public float getAngleAxis(LLVector3 lLVector3) {
        float f = this.w;
        if (f > 1.0f) {
            f = 1.0f;
        }
        float f2 = f >= -1.0f ? f : -1.0f;
        float fSqrt = (float) Math.sqrt(1.0f - (f2 * f2));
        float f3 = Math.abs(fSqrt) < 5.0E-4f ? 1.0f : 1.0f / fSqrt;
        float fAcos = ((float) Math.acos(f2)) * 2.0f;
        if (fAcos > 3.1415927f) {
            lLVector3.x = (-this.x) * f3;
            lLVector3.y = (-this.y) * f3;
            lLVector3.z = f3 * (-this.z);
            return 6.2831855f - fAcos;
        }
        lLVector3.x = this.x * f3;
        lLVector3.y = this.y * f3;
        lLVector3.z = f3 * this.z;
        return fAcos;
    }

    public void getInverseMatrix(float[] fArr, int i) {
        float f = this.x * this.x;
        float f2 = this.y * this.y;
        float f3 = this.z * this.z;
        float f4 = (-this.x) * (-this.y);
        float f5 = (-this.x) * (-this.z);
        float f6 = (-this.y) * (-this.z);
        float f7 = this.w * (-this.x);
        float f8 = this.w * (-this.y);
        float f9 = this.w * (-this.z);
        fArr[i + 0] = 1.0f - ((f2 + f3) * 2.0f);
        fArr[i + 1] = (f4 - f9) * 2.0f;
        fArr[i + 2] = (f5 + f8) * 2.0f;
        fArr[i + 3] = 0.0f;
        fArr[i + 4] = (f4 + f9) * 2.0f;
        fArr[i + 5] = 1.0f - ((f3 + f) * 2.0f);
        fArr[i + 6] = (f6 - f7) * 2.0f;
        fArr[i + 7] = 0.0f;
        fArr[i + 8] = (f5 - f8) * 2.0f;
        fArr[i + 9] = (f6 + f7) * 2.0f;
        fArr[i + 10] = 1.0f - ((f + f2) * 2.0f);
        fArr[i + 11] = 0.0f;
        fArr[i + 12] = 0.0f;
        fArr[i + 13] = 0.0f;
        fArr[i + 14] = 0.0f;
        fArr[i + 15] = 1.0f;
    }

    public float[] getInverseMatrix() {
        if (this.inverseMatrix != null) {
            return this.inverseMatrix;
        }
        float f = this.x * this.x;
        float f2 = this.y * this.y;
        float f3 = this.z * this.z;
        float f4 = (-this.x) * (-this.y);
        float f5 = (-this.x) * (-this.z);
        float f6 = (-this.y) * (-this.z);
        float f7 = this.w * (-this.x);
        float f8 = this.w * (-this.y);
        float f9 = this.w * (-this.z);
        this.inverseMatrix = new float[]{1.0f - ((f2 + f3) * 2.0f), (f4 - f9) * 2.0f, (f5 + f8) * 2.0f, 0.0f, (f4 + f9) * 2.0f, 1.0f - ((f3 + f) * 2.0f), (f6 - f7) * 2.0f, 0.0f, (f5 - f8) * 2.0f, (f6 + f7) * 2.0f, 1.0f - ((f + f2) * 2.0f), 0.0f, 0.0f, 0.0f, 0.0f, 1.0f};
        return this.inverseMatrix;
    }

    public float[] getMatrix() {
        if (this.matrix != null) {
            return this.matrix;
        }
        float f = this.x * this.x;
        float f2 = this.y * this.y;
        float f3 = this.z * this.z;
        float f4 = this.x * this.y;
        float f5 = this.x * this.z;
        float f6 = this.y * this.z;
        float f7 = this.w * this.x;
        float f8 = this.w * this.y;
        float f9 = this.w * this.z;
        this.matrix = new float[]{1.0f - ((f2 + f3) * 2.0f), (f4 - f9) * 2.0f, (f5 + f8) * 2.0f, 0.0f, (f4 + f9) * 2.0f, 1.0f - ((f3 + f) * 2.0f), (f6 - f7) * 2.0f, 0.0f, (f5 - f8) * 2.0f, (f6 + f7) * 2.0f, 1.0f - ((f + f2) * 2.0f), 0.0f, 0.0f, 0.0f, 0.0f, 1.0f};
        return this.matrix;
    }

    public float normalize() {
        float fSqrt = (float) Math.sqrt((this.x * this.x) + (this.y * this.y) + (this.z * this.z) + (this.w * this.w));
        if (fSqrt <= 1.0E-7f) {
            this.x = 0.0f;
            this.y = 0.0f;
            this.z = 0.0f;
            this.w = 1.0f;
        } else if (Math.abs(1.0f - fSqrt) > 1.0E-6f) {
            float f = 1.0f / fSqrt;
            this.x *= f;
            this.y *= f;
            this.z *= f;
            this.w = f * this.w;
        }
        this.matrix = null;
        this.inverseMatrix = null;
        return fSqrt;
    }

    public void set(LLQuaternion lLQuaternion) {
        this.x = lLQuaternion.x;
        this.y = lLQuaternion.y;
        this.z = lLQuaternion.z;
        this.w = lLQuaternion.w;
        this.matrix = null;
        this.inverseMatrix = null;
    }

    public void setIdentity() {
        this.x = 0.0f;
        this.y = 0.0f;
        this.z = 0.0f;
        this.w = 1.0f;
    }

    public void setLerp(LLQuaternion lLQuaternion, float f, LLQuaternion lLQuaternion2, float f2) {
        this.x = (lLQuaternion.x * f) + (lLQuaternion2.x * f2);
        this.y = (lLQuaternion.y * f) + (lLQuaternion2.y * f2);
        this.z = (lLQuaternion.z * f) + (lLQuaternion2.z * f2);
        this.w = (lLQuaternion.w * f) + (lLQuaternion2.w * f2);
        this.matrix = null;
        this.inverseMatrix = null;
    }

    public void setMul(LLQuaternion lLQuaternion, LLQuaternion lLQuaternion2) {
        this.x = (((lLQuaternion2.w * lLQuaternion.x) + (lLQuaternion2.x * lLQuaternion.w)) + (lLQuaternion2.y * lLQuaternion.z)) - (lLQuaternion2.z * lLQuaternion.y);
        this.y = (((lLQuaternion2.w * lLQuaternion.y) + (lLQuaternion2.y * lLQuaternion.w)) + (lLQuaternion2.z * lLQuaternion.x)) - (lLQuaternion2.x * lLQuaternion.z);
        this.z = (((lLQuaternion2.w * lLQuaternion.z) + (lLQuaternion2.z * lLQuaternion.w)) + (lLQuaternion2.x * lLQuaternion.y)) - (lLQuaternion2.y * lLQuaternion.x);
        this.w = (((lLQuaternion2.w * lLQuaternion.w) - (lLQuaternion2.x * lLQuaternion.x)) - (lLQuaternion2.y * lLQuaternion.y)) - (lLQuaternion2.z * lLQuaternion.z);
        this.matrix = null;
        this.inverseMatrix = null;
    }

    public void setQuat(float f, float f2, float f3, float f4) {
        LLVector3 lLVector3 = new LLVector3(f2, f3, f4);
        lLVector3.normVec();
        float f5 = 0.5f * f;
        float fCos = (float) Math.cos(f5);
        float fSin = (float) Math.sin(f5);
        this.x = lLVector3.x * fSin;
        this.y = lLVector3.y * fSin;
        this.z = lLVector3.z * fSin;
        this.w = fCos;
        normalize();
        this.matrix = null;
        this.inverseMatrix = null;
    }

    public void setQuat(float f, LLVector3 lLVector3) {
        LLVector3 lLVector32 = new LLVector3(lLVector3);
        lLVector32.normVec();
        float f2 = 0.5f * f;
        float fCos = (float) Math.cos(f2);
        float fSin = (float) Math.sin(f2);
        this.x = lLVector32.x * fSin;
        this.y = lLVector32.y * fSin;
        this.z = lLVector32.z * fSin;
        this.w = fCos;
        normalize();
        this.matrix = null;
        this.inverseMatrix = null;
    }

    public void setRaw(float f, float f2, float f3, float f4) {
        this.x = f;
        this.y = f2;
        this.z = f3;
        this.w = f4;
    }

    public void setZero() {
        this.x = 0.0f;
        this.y = 0.0f;
        this.z = 0.0f;
        this.w = 0.0f;
    }

    public String toString() {
        return String.format("(%.2f, %.2f, %.2f, %.2f)", Float.valueOf(this.x), Float.valueOf(this.y), Float.valueOf(this.z), Float.valueOf(this.w));
    }
}
