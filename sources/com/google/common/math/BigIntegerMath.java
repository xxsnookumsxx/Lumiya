package com.google.common.math;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/* JADX INFO: loaded from: classes.dex */
@GwtCompatible(emulated = true)
public final class BigIntegerMath {

    @VisibleForTesting
    static final int SQRT2_PRECOMPUTE_THRESHOLD = 256;

    @VisibleForTesting
    static final BigInteger SQRT2_PRECOMPUTED_BITS = new BigInteger("16a09e667f3bcc908b2fb1366ea957d3e3adec17512775099da2f590b0667322a", 16);
    private static final double LN_10 = Math.log(10.0d);
    private static final double LN_2 = Math.log(2.0d);

    /* JADX INFO: renamed from: com.google.common.math.BigIntegerMath$1, reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$java$math$RoundingMode = new int[RoundingMode.values().length];

        static {
            try {
                $SwitchMap$java$math$RoundingMode[RoundingMode.UNNECESSARY.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$java$math$RoundingMode[RoundingMode.DOWN.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$java$math$RoundingMode[RoundingMode.FLOOR.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$java$math$RoundingMode[RoundingMode.UP.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$java$math$RoundingMode[RoundingMode.CEILING.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$java$math$RoundingMode[RoundingMode.HALF_DOWN.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$java$math$RoundingMode[RoundingMode.HALF_UP.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$java$math$RoundingMode[RoundingMode.HALF_EVEN.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
        }
    }

    private BigIntegerMath() {
    }

    public static BigInteger binomial(int i, int i2) {
        MathPreconditions.checkNonNegative("n", i);
        MathPreconditions.checkNonNegative("k", i2);
        Preconditions.checkArgument(i2 <= i, "k (%s) > n (%s)", Integer.valueOf(i2), Integer.valueOf(i));
        if (i2 > (i >> 1)) {
            i2 = i - i2;
        }
        if (i2 < LongMath.biggestBinomials.length && i <= LongMath.biggestBinomials[i2]) {
            return BigInteger.valueOf(LongMath.binomial(i, i2));
        }
        BigInteger bigInteger = BigInteger.ONE;
        int iLog2 = LongMath.log2(i, RoundingMode.CEILING);
        long j = 1;
        long j2 = i;
        BigInteger bigInteger2 = bigInteger;
        int i3 = iLog2;
        for (int i4 = 1; i4 < i2; i4++) {
            int i5 = i - i4;
            int i6 = i4 + 1;
            if (i3 + iLog2 < 63) {
                j2 *= (long) i5;
                j *= (long) i6;
                i3 += iLog2;
            } else {
                BigInteger bigIntegerDivide = bigInteger2.multiply(BigInteger.valueOf(j2)).divide(BigInteger.valueOf(j));
                j2 = i5;
                j = i6;
                bigInteger2 = bigIntegerDivide;
                i3 = iLog2;
            }
        }
        return bigInteger2.multiply(BigInteger.valueOf(j2)).divide(BigInteger.valueOf(j));
    }

    @GwtIncompatible("TODO")
    public static BigInteger divide(BigInteger bigInteger, BigInteger bigInteger2, RoundingMode roundingMode) {
        return new BigDecimal(bigInteger).divide(new BigDecimal(bigInteger2), 0, roundingMode).toBigIntegerExact();
    }

    public static BigInteger factorial(int i) {
        MathPreconditions.checkNonNegative("n", i);
        if (i < LongMath.factorials.length) {
            return BigInteger.valueOf(LongMath.factorials[i]);
        }
        ArrayList arrayList = new ArrayList(IntMath.divide(IntMath.log2(i, RoundingMode.CEILING) * i, 64, RoundingMode.CEILING));
        int length = LongMath.factorials.length;
        long j = LongMath.factorials[length - 1];
        int iNumberOfTrailingZeros = Long.numberOfTrailingZeros(j);
        long j2 = j >> iNumberOfTrailingZeros;
        int iLog2 = LongMath.log2(j2, RoundingMode.FLOOR) + 1;
        int iLog22 = LongMath.log2(length, RoundingMode.FLOOR) + 1;
        int i2 = iNumberOfTrailingZeros;
        int iLog23 = iLog2;
        long j3 = j2;
        long j4 = length;
        int i3 = iLog22;
        int i4 = 1 << (iLog22 - 1);
        while (true) {
            if (j4 > ((long) i)) {
                break;
            }
            if ((((long) i4) & j4) != 0) {
                i4 <<= 1;
                i3++;
            }
            int iNumberOfTrailingZeros2 = Long.numberOfTrailingZeros(j4);
            long j5 = j4 >> iNumberOfTrailingZeros2;
            i2 += iNumberOfTrailingZeros2;
            if ((i3 - iNumberOfTrailingZeros2) + iLog23 >= 64) {
                arrayList.add(BigInteger.valueOf(j3));
                j3 = 1;
            }
            long j6 = j5 * j3;
            j4 = 1 + j4;
            iLog23 = LongMath.log2(j6, RoundingMode.FLOOR) + 1;
            j3 = j6;
        }
        if (!(j3 <= 1)) {
            arrayList.add(BigInteger.valueOf(j3));
        }
        return listProduct(arrayList).shiftLeft(i2);
    }

    @GwtIncompatible("TODO")
    static boolean fitsInLong(BigInteger bigInteger) {
        return bigInteger.bitLength() <= 63;
    }

    public static boolean isPowerOfTwo(BigInteger bigInteger) {
        Preconditions.checkNotNull(bigInteger);
        return bigInteger.signum() > 0 && bigInteger.getLowestSetBit() == bigInteger.bitLength() + (-1);
    }

    static BigInteger listProduct(List<BigInteger> list) {
        return listProduct(list, 0, list.size());
    }

    static BigInteger listProduct(List<BigInteger> list, int i, int i2) {
        switch (i2 - i) {
            case 0:
                return BigInteger.ONE;
            case 1:
                return list.get(i);
            case 2:
                return list.get(i).multiply(list.get(i + 1));
            case 3:
                return list.get(i).multiply(list.get(i + 1)).multiply(list.get(i + 2));
            default:
                int i3 = (i2 + i) >>> 1;
                return listProduct(list, i, i3).multiply(listProduct(list, i3, i2));
        }
    }

    @GwtIncompatible("TODO")
    public static int log10(BigInteger bigInteger, RoundingMode roundingMode) {
        int iCompareTo;
        int i;
        BigInteger bigInteger2;
        int i2;
        MathPreconditions.checkPositive("x", bigInteger);
        if (fitsInLong(bigInteger)) {
            return LongMath.log10(bigInteger.longValue(), roundingMode);
        }
        int iLog2 = (int) ((((double) log2(bigInteger, RoundingMode.FLOOR)) * LN_2) / LN_10);
        BigInteger bigIntegerPow = BigInteger.TEN.pow(iLog2);
        int iCompareTo2 = bigIntegerPow.compareTo(bigInteger);
        if (iCompareTo2 <= 0) {
            BigInteger bigIntegerMultiply = BigInteger.TEN.multiply(bigIntegerPow);
            i = iLog2;
            bigInteger2 = bigIntegerPow;
            i2 = iCompareTo2;
            BigInteger bigInteger3 = bigIntegerMultiply;
            int iCompareTo3 = bigIntegerMultiply.compareTo(bigInteger);
            while (iCompareTo3 <= 0) {
                i++;
                BigInteger bigIntegerMultiply2 = BigInteger.TEN.multiply(bigInteger3);
                i2 = iCompareTo3;
                iCompareTo3 = bigIntegerMultiply2.compareTo(bigInteger);
                bigInteger2 = bigInteger3;
                bigInteger3 = bigIntegerMultiply2;
            }
        } else {
            do {
                iLog2--;
                bigIntegerPow = bigIntegerPow.divide(BigInteger.TEN);
                iCompareTo = bigIntegerPow.compareTo(bigInteger);
            } while (iCompareTo > 0);
            i = iLog2;
            bigInteger2 = bigIntegerPow;
            i2 = iCompareTo;
        }
        switch (AnonymousClass1.$SwitchMap$java$math$RoundingMode[roundingMode.ordinal()]) {
            case 1:
                MathPreconditions.checkRoundingUnnecessary(i2 == 0);
            case 2:
            case 3:
                return i;
            case 4:
            case 5:
                return !bigInteger2.equals(bigInteger) ? i + 1 : i;
            case 6:
            case 7:
            case 8:
                return bigInteger.pow(2).compareTo(bigInteger2.pow(2).multiply(BigInteger.TEN)) > 0 ? i + 1 : i;
            default:
                throw new AssertionError();
        }
    }

    public static int log2(BigInteger bigInteger, RoundingMode roundingMode) {
        MathPreconditions.checkPositive("x", (BigInteger) Preconditions.checkNotNull(bigInteger));
        int iBitLength = bigInteger.bitLength() - 1;
        switch (AnonymousClass1.$SwitchMap$java$math$RoundingMode[roundingMode.ordinal()]) {
            case 1:
                MathPreconditions.checkRoundingUnnecessary(isPowerOfTwo(bigInteger));
            case 2:
            case 3:
                return iBitLength;
            case 4:
            case 5:
                return !isPowerOfTwo(bigInteger) ? iBitLength + 1 : iBitLength;
            case 6:
            case 7:
            case 8:
                return iBitLength >= 256 ? bigInteger.pow(2).bitLength() + (-1) >= (iBitLength * 2) + 1 ? iBitLength + 1 : iBitLength : bigInteger.compareTo(SQRT2_PRECOMPUTED_BITS.shiftRight(256 - iBitLength)) > 0 ? iBitLength + 1 : iBitLength;
            default:
                throw new AssertionError();
        }
    }

    @GwtIncompatible("TODO")
    public static BigInteger sqrt(BigInteger bigInteger, RoundingMode roundingMode) {
        boolean z = false;
        MathPreconditions.checkNonNegative("x", bigInteger);
        if (fitsInLong(bigInteger)) {
            return BigInteger.valueOf(LongMath.sqrt(bigInteger.longValue(), roundingMode));
        }
        BigInteger bigIntegerSqrtFloor = sqrtFloor(bigInteger);
        switch (AnonymousClass1.$SwitchMap$java$math$RoundingMode[roundingMode.ordinal()]) {
            case 1:
                MathPreconditions.checkRoundingUnnecessary(bigIntegerSqrtFloor.pow(2).equals(bigInteger));
            case 2:
            case 3:
                return bigIntegerSqrtFloor;
            case 4:
            case 5:
                int iIntValue = bigIntegerSqrtFloor.intValue();
                if (iIntValue * iIntValue == bigInteger.intValue() && bigIntegerSqrtFloor.pow(2).equals(bigInteger)) {
                    z = true;
                }
                return !z ? bigIntegerSqrtFloor.add(BigInteger.ONE) : bigIntegerSqrtFloor;
            case 6:
            case 7:
            case 8:
                return bigIntegerSqrtFloor.pow(2).add(bigIntegerSqrtFloor).compareTo(bigInteger) < 0 ? bigIntegerSqrtFloor.add(BigInteger.ONE) : bigIntegerSqrtFloor;
            default:
                throw new AssertionError();
        }
    }

    @GwtIncompatible("TODO")
    private static BigInteger sqrtApproxWithDoubles(BigInteger bigInteger) {
        return DoubleMath.roundToBigInteger(Math.sqrt(DoubleUtils.bigToDouble(bigInteger)), RoundingMode.HALF_EVEN);
    }

    @GwtIncompatible("TODO")
    private static BigInteger sqrtFloor(BigInteger bigInteger) {
        BigInteger bigIntegerSqrtApproxWithDoubles;
        BigInteger bigInteger2;
        int iLog2 = log2(bigInteger, RoundingMode.FLOOR);
        if (iLog2 >= 1023) {
            int i = (iLog2 - 52) & (-2);
            bigIntegerSqrtApproxWithDoubles = sqrtApproxWithDoubles(bigInteger.shiftRight(i)).shiftLeft(i >> 1);
        } else {
            bigIntegerSqrtApproxWithDoubles = sqrtApproxWithDoubles(bigInteger);
        }
        BigInteger bigIntegerShiftRight = bigIntegerSqrtApproxWithDoubles.add(bigInteger.divide(bigIntegerSqrtApproxWithDoubles)).shiftRight(1);
        if (bigIntegerSqrtApproxWithDoubles.equals(bigIntegerShiftRight)) {
            return bigIntegerSqrtApproxWithDoubles;
        }
        do {
            bigInteger2 = bigIntegerShiftRight;
            bigIntegerShiftRight = bigInteger2.add(bigInteger.divide(bigInteger2)).shiftRight(1);
        } while (bigIntegerShiftRight.compareTo(bigInteger2) < 0);
        return bigInteger2;
    }
}
