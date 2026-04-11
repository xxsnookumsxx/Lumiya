package com.lumiyaviewer.lumiya.slproto.avatar;

import java.util.Arrays;
import javax.annotation.Nonnull;

/* JADX INFO: loaded from: classes.dex */
public class SLAvatarParamColor {

    @Nonnull
    public final ColorOperation colorOperation;

    @Nonnull
    private final int[] colorValues;

    public enum ColorOperation {
        Default,
        Blend,
        Multiply;

        /* JADX INFO: renamed from: values, reason: to resolve conflict with enum method */
        public static ColorOperation[] valuesCustom() {
            return values();
        }
    }

    SLAvatarParamColor(@Nonnull ColorOperation colorOperation, @Nonnull int[] iArr) {
        this.colorOperation = colorOperation;
        this.colorValues = iArr;
    }

    public static int colorAdd(int i, int i2) {
        int i3 = (i & 255) + (i2 & 255);
        int i4 = ((i >> 8) & 255) + ((i2 >> 8) & 255);
        int i5 = ((i2 >> 16) & 255) + ((i >> 16) & 255);
        int i6 = ((i >> 24) & 255) + ((i2 >> 24) & 255);
        if (i3 > 255) {
            i3 = 255;
        }
        if (i4 > 255) {
            i4 = 255;
        }
        if (i5 > 255) {
            i5 = 255;
        }
        return ((i6 <= 255 ? i6 : 255) << 24) | (i5 << 16) | (i4 << 8) | i3;
    }

    public static int colorLerp(int i, int i2, float f) {
        float f2 = 1.0f - f;
        int iRound = Math.round(((i & 255) * f2) + ((i2 & 255) * f));
        int iRound2 = Math.round((f2 * ((i >> 8) & 255)) + (f * ((i2 >> 8) & 255)));
        int iRound3 = Math.round((f2 * ((i >> 16) & 255)) + (f * ((i2 >> 16) & 255)));
        int iRound4 = Math.round((f2 * ((i >> 24) & 255)) + (f * ((i2 >> 24) & 255)));
        if (iRound < 0) {
            iRound = 0;
        } else if (iRound > 255) {
            iRound = 255;
        }
        if (iRound2 < 0) {
            iRound2 = 0;
        } else if (iRound2 > 255) {
            iRound2 = 255;
        }
        if (iRound3 < 0) {
            iRound3 = 0;
        } else if (iRound3 > 255) {
            iRound3 = 255;
        }
        return ((iRound4 >= 0 ? iRound4 > 255 ? 255 : iRound4 : 0) << 24) | (iRound3 << 16) | (iRound2 << 8) | iRound;
    }

    public static int colorMult(int i, int i2) {
        int i3 = ((i & 255) * (i2 & 255)) / 255;
        int i4 = (((i >> 8) & 255) * ((i2 >> 8) & 255)) / 255;
        int i5 = (((i >> 16) & 255) * ((i2 >> 16) & 255)) / 255;
        int i6 = (((i >> 24) & 255) * ((i2 >> 24) & 255)) / 255;
        if (i3 > 255) {
            i3 = 255;
        }
        if (i4 > 255) {
            i4 = 255;
        }
        if (i5 > 255) {
            i5 = 255;
        }
        return ((i6 <= 255 ? i6 : 255) << 24) | (i5 << 16) | (i4 << 8) | i3;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        SLAvatarParamColor sLAvatarParamColor = (SLAvatarParamColor) obj;
        if (this.colorOperation == sLAvatarParamColor.colorOperation) {
            return Arrays.equals(this.colorValues, sLAvatarParamColor.colorValues);
        }
        return false;
    }

    public int getColor(float f) {
        if (this.colorValues.length == 0) {
            return 0;
        }
        if (this.colorValues.length == 1) {
            return this.colorValues[0];
        }
        int length = this.colorValues.length - 1;
        float f2 = length * f;
        int i = (int) f2;
        int i2 = i + 1;
        if (i >= length) {
            return this.colorValues[length];
        }
        return colorLerp(this.colorValues[i], this.colorValues[i2], f2 - i);
    }

    public int hashCode() {
        return (this.colorOperation.hashCode() * 31) + Arrays.hashCode(this.colorValues);
    }
}
