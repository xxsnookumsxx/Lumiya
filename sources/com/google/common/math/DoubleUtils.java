package com.google.common.math;

import com.google.common.base.Preconditions;
import java.math.BigInteger;

/* JADX INFO: loaded from: classes.dex */
final class DoubleUtils {
    static final int EXPONENT_BIAS = 1023;
    static final long EXPONENT_MASK = 9218868437227405312L;
    static final long IMPLICIT_BIT = 4503599627370496L;
    private static final long ONE_BITS = Double.doubleToRawLongBits(1.0d);
    static final int SIGNIFICAND_BITS = 52;
    static final long SIGNIFICAND_MASK = 4503599627370495L;
    static final long SIGN_MASK = Long.MIN_VALUE;

    private DoubleUtils() {
    }

    static double bigToDouble(BigInteger bigInteger) {
        BigInteger bigIntegerAbs = bigInteger.abs();
        int iBitLength = bigIntegerAbs.bitLength() - 1;
        if (iBitLength < 63) {
            return bigInteger.longValue();
        }
        if (iBitLength > EXPONENT_BIAS) {
            return ((double) bigInteger.signum()) * Double.POSITIVE_INFINITY;
        }
        int i = (iBitLength - 52) - 1;
        long jLongValue = bigIntegerAbs.shiftRight(i).longValue();
        long j = (jLongValue >> 1) & SIGNIFICAND_MASK;
        return Double.longBitsToDouble(((!(((jLongValue & 1) > 0L ? 1 : ((jLongValue & 1) == 0L ? 0 : -1)) != 0 && (((1 & j) > 0L ? 1 : ((1 & j) == 0L ? 0 : -1)) != 0 || bigIntegerAbs.getLowestSetBit() < i)) ? j : 1 + j) + (((long) (iBitLength + EXPONENT_BIAS)) << 52)) | (((long) bigInteger.signum()) & SIGN_MASK));
    }

    static double ensureNonNegative(double d) {
        Preconditions.checkArgument(Double.isNaN(d) ? false : true);
        if (d > 0.0d) {
            return d;
        }
        return 0.0d;
    }

    static long getSignificand(double d) {
        Preconditions.checkArgument(isFinite(d), "not a normal value");
        int exponent = Math.getExponent(d);
        long jDoubleToRawLongBits = Double.doubleToRawLongBits(d) & SIGNIFICAND_MASK;
        return exponent != -1023 ? IMPLICIT_BIT | jDoubleToRawLongBits : jDoubleToRawLongBits << 1;
    }

    static boolean isFinite(double d) {
        return Math.getExponent(d) <= EXPONENT_BIAS;
    }

    static boolean isNormal(double d) {
        return Math.getExponent(d) >= -1022;
    }

    static double nextDown(double d) {
        return -Math.nextUp(-d);
    }

    static double scaleNormalize(double d) {
        return Double.longBitsToDouble((Double.doubleToRawLongBits(d) & SIGNIFICAND_MASK) | ONE_BITS);
    }
}
