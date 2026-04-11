package com.google.vr.sdk.base;

import android.opengl.Matrix;
import com.google.vr.cardboard.UsedByNative;

/* JADX INFO: loaded from: classes.dex */
@UsedByNative
public class HeadTransform {
    private static final float GIMBAL_LOCK_EPSILON = 0.01f;
    private final float[] headView = new float[16];

    public HeadTransform() {
        Matrix.setIdentityM(this.headView, 0);
    }

    public void getEulerAngles(float[] fArr, int i) {
        float fAtan2;
        float fAtan22;
        if (i + 3 > fArr.length) {
            throw new IllegalArgumentException("Not enough space to write the result");
        }
        float fAsin = (float) Math.asin(this.headView[6]);
        if (Math.sqrt(1.0f - (this.headView[6] * this.headView[6])) >= 0.009999999776482582d) {
            fAtan2 = (float) Math.atan2(-this.headView[2], this.headView[10]);
            fAtan22 = (float) Math.atan2(-this.headView[4], this.headView[5]);
        } else {
            fAtan2 = 0.0f;
            fAtan22 = (float) Math.atan2(this.headView[1], this.headView[0]);
        }
        fArr[i + 0] = -fAsin;
        fArr[i + 1] = -fAtan2;
        fArr[i + 2] = -fAtan22;
    }

    public void getForwardVector(float[] fArr, int i) {
        if (i + 3 > fArr.length) {
            throw new IllegalArgumentException("Not enough space to write the result");
        }
        for (int i2 = 0; i2 < 3; i2++) {
            fArr[i2 + i] = -this.headView[(i2 * 4) + 2];
        }
    }

    public void getHeadView(float[] fArr, int i) {
        if (i + 16 > fArr.length) {
            throw new IllegalArgumentException("Not enough space to write the result");
        }
        System.arraycopy(this.headView, 0, fArr, i, 16);
    }

    @UsedByNative
    public float[] getHeadView() {
        return this.headView;
    }

    public void getQuaternion(float[] fArr, int i) {
        float f;
        float f2;
        float f3;
        float f4;
        if (i + 4 > fArr.length) {
            throw new IllegalArgumentException("Not enough space to write the result");
        }
        float[] fArr2 = this.headView;
        if (fArr2[0] + fArr2[5] + fArr2[10] >= 0.0f) {
            float fSqrt = (float) Math.sqrt(r0 + 1.0f);
            f4 = 0.5f * fSqrt;
            float f5 = 0.5f / fSqrt;
            f2 = (fArr2[9] - fArr2[6]) * f5;
            f3 = (fArr2[2] - fArr2[8]) * f5;
            f = f5 * (fArr2[4] - fArr2[1]);
        } else if (fArr2[0] > fArr2[5] && fArr2[0] > fArr2[10]) {
            float fSqrt2 = (float) Math.sqrt(((fArr2[0] + 1.0f) - fArr2[5]) - fArr2[10]);
            f2 = fSqrt2 * 0.5f;
            float f6 = 0.5f / fSqrt2;
            f3 = (fArr2[4] + fArr2[1]) * f6;
            f = (fArr2[2] + fArr2[8]) * f6;
            f4 = f6 * (fArr2[9] - fArr2[6]);
        } else if (fArr2[5] > fArr2[10]) {
            float fSqrt3 = (float) Math.sqrt(((fArr2[5] + 1.0f) - fArr2[0]) - fArr2[10]);
            f3 = fSqrt3 * 0.5f;
            float f7 = 0.5f / fSqrt3;
            f2 = (fArr2[4] + fArr2[1]) * f7;
            f = (fArr2[9] + fArr2[6]) * f7;
            f4 = f7 * (fArr2[2] - fArr2[8]);
        } else {
            float fSqrt4 = (float) Math.sqrt(((fArr2[10] + 1.0f) - fArr2[0]) - fArr2[5]);
            f = fSqrt4 * 0.5f;
            float f8 = 0.5f / fSqrt4;
            f2 = (fArr2[2] + fArr2[8]) * f8;
            f3 = (fArr2[9] + fArr2[6]) * f8;
            f4 = f8 * (fArr2[4] - fArr2[1]);
        }
        fArr[i + 0] = f2;
        fArr[i + 1] = f3;
        fArr[i + 2] = f;
        fArr[i + 3] = f4;
    }

    public void getRightVector(float[] fArr, int i) {
        if (i + 3 > fArr.length) {
            throw new IllegalArgumentException("Not enough space to write the result");
        }
        for (int i2 = 0; i2 < 3; i2++) {
            fArr[i2 + i] = this.headView[i2 * 4];
        }
    }

    public void getTranslation(float[] fArr, int i) {
        if (i + 3 > fArr.length) {
            throw new IllegalArgumentException("Not enough space to write the result");
        }
        for (int i2 = 0; i2 < 3; i2++) {
            fArr[i2 + i] = this.headView[i2 + 12];
        }
    }

    public void getUpVector(float[] fArr, int i) {
        if (i + 3 > fArr.length) {
            throw new IllegalArgumentException("Not enough space to write the result");
        }
        for (int i2 = 0; i2 < 3; i2++) {
            fArr[i2 + i] = this.headView[(i2 * 4) + 1];
        }
    }
}
