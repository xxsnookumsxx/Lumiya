package com.google.common.primitives;

import com.google.common.annotations.Beta;
import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Preconditions;
import java.util.Comparator;
import javax.annotation.CheckReturnValue;

/* JADX INFO: loaded from: classes.dex */
@Beta
@GwtCompatible
public final class UnsignedInts {
    static final long INT_MASK = 4294967295L;

    enum LexicographicalComparator implements Comparator<int[]> {
        INSTANCE;

        @Override // java.util.Comparator
        public int compare(int[] iArr, int[] iArr2) {
            int iMin = Math.min(iArr.length, iArr2.length);
            for (int i = 0; i < iMin; i++) {
                if (iArr[i] != iArr2[i]) {
                    return UnsignedInts.compare(iArr[i], iArr2[i]);
                }
            }
            return iArr.length - iArr2.length;
        }
    }

    private UnsignedInts() {
    }

    @CheckReturnValue
    public static int compare(int i, int i2) {
        return Ints.compare(flip(i), flip(i2));
    }

    public static int decode(String str) {
        ParseRequest parseRequestFromString = ParseRequest.fromString(str);
        try {
            return parseUnsignedInt(parseRequestFromString.rawValue, parseRequestFromString.radix);
        } catch (NumberFormatException e) {
            NumberFormatException numberFormatException = new NumberFormatException("Error parsing value: " + str);
            numberFormatException.initCause(e);
            throw numberFormatException;
        }
    }

    @CheckReturnValue
    public static int divide(int i, int i2) {
        return (int) (toLong(i) / toLong(i2));
    }

    static int flip(int i) {
        return Integer.MIN_VALUE ^ i;
    }

    @CheckReturnValue
    public static String join(String str, int... iArr) {
        Preconditions.checkNotNull(str);
        if (iArr.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder(iArr.length * 5);
        sb.append(toString(iArr[0]));
        for (int i = 1; i < iArr.length; i++) {
            sb.append(str).append(toString(iArr[i]));
        }
        return sb.toString();
    }

    @CheckReturnValue
    public static Comparator<int[]> lexicographicalComparator() {
        return LexicographicalComparator.INSTANCE;
    }

    @CheckReturnValue
    public static int max(int... iArr) {
        Preconditions.checkArgument(iArr.length > 0);
        int iFlip = flip(iArr[0]);
        for (int i = 1; i < iArr.length; i++) {
            int iFlip2 = flip(iArr[i]);
            if (iFlip2 > iFlip) {
                iFlip = iFlip2;
            }
        }
        return flip(iFlip);
    }

    @CheckReturnValue
    public static int min(int... iArr) {
        Preconditions.checkArgument(iArr.length > 0);
        int iFlip = flip(iArr[0]);
        for (int i = 1; i < iArr.length; i++) {
            int iFlip2 = flip(iArr[i]);
            if (iFlip2 < iFlip) {
                iFlip = iFlip2;
            }
        }
        return flip(iFlip);
    }

    public static int parseUnsignedInt(String str) {
        return parseUnsignedInt(str, 10);
    }

    public static int parseUnsignedInt(String str, int i) {
        Preconditions.checkNotNull(str);
        long j = Long.parseLong(str, i);
        if ((INT_MASK & j) != j) {
            throw new NumberFormatException("Input " + str + " in base " + i + " is not in the range of an unsigned integer");
        }
        return (int) j;
    }

    @CheckReturnValue
    public static int remainder(int i, int i2) {
        return (int) (toLong(i) % toLong(i2));
    }

    @CheckReturnValue
    public static long toLong(int i) {
        return ((long) i) & INT_MASK;
    }

    @CheckReturnValue
    public static String toString(int i) {
        return toString(i, 10);
    }

    @CheckReturnValue
    public static String toString(int i, int i2) {
        return Long.toString(((long) i) & INT_MASK, i2);
    }
}
