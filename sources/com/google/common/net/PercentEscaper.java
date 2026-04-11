package com.google.common.net;

import com.google.common.annotations.Beta;
import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Preconditions;
import com.google.common.escape.UnicodeEscaper;

/* JADX INFO: loaded from: classes.dex */
@Beta
@GwtCompatible
public final class PercentEscaper extends UnicodeEscaper {
    private static final char[] PLUS_SIGN = {'+'};
    private static final char[] UPPER_HEX_DIGITS = "0123456789ABCDEF".toCharArray();
    private final boolean plusForSpace;
    private final boolean[] safeOctets;

    public PercentEscaper(String str, boolean z) {
        Preconditions.checkNotNull(str);
        if (str.matches(".*[0-9A-Za-z].*")) {
            throw new IllegalArgumentException("Alphanumeric characters are always 'safe' and should not be explicitly specified");
        }
        String str2 = str + "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        if (z && str2.contains(" ")) {
            throw new IllegalArgumentException("plusForSpace cannot be specified when space is a 'safe' character");
        }
        this.plusForSpace = z;
        this.safeOctets = createSafeOctets(str2);
    }

    private static boolean[] createSafeOctets(String str) {
        char[] charArray = str.toCharArray();
        int iMax = -1;
        for (char c : charArray) {
            iMax = Math.max((int) c, iMax);
        }
        boolean[] zArr = new boolean[iMax + 1];
        for (char c2 : charArray) {
            zArr[c2] = true;
        }
        return zArr;
    }

    @Override // com.google.common.escape.UnicodeEscaper, com.google.common.escape.Escaper
    public String escape(String str) {
        Preconditions.checkNotNull(str);
        int length = str.length();
        for (int i = 0; i < length; i++) {
            char cCharAt = str.charAt(i);
            if (cCharAt >= this.safeOctets.length || !this.safeOctets[cCharAt]) {
                return escapeSlow(str, i);
            }
        }
        return str;
    }

    @Override // com.google.common.escape.UnicodeEscaper
    protected char[] escape(int i) {
        if (i < this.safeOctets.length && this.safeOctets[i]) {
            return null;
        }
        if (i == 32 && this.plusForSpace) {
            return PLUS_SIGN;
        }
        if (i <= 127) {
            return new char[]{'%', UPPER_HEX_DIGITS[i >>> 4], UPPER_HEX_DIGITS[i & 15]};
        }
        if (i <= 2047) {
            char[] cArr = {'%', UPPER_HEX_DIGITS[(i >>> 4) | 12], UPPER_HEX_DIGITS[i & 15], '%', UPPER_HEX_DIGITS[(r1 & 3) | 8], UPPER_HEX_DIGITS[i & 15]};
            int i2 = (i >>> 4) >>> 2;
            return cArr;
        }
        if (i <= 65535) {
            char[] cArr2 = {'%', 'E', cArr[i >>> 2], '%', UPPER_HEX_DIGITS[(i & 3) | 8], UPPER_HEX_DIGITS[r1 & 15], '%', UPPER_HEX_DIGITS[(r1 & 3) | 8], UPPER_HEX_DIGITS[i & 15]};
            int i3 = ((i >>> 4) >>> 2) >>> 4;
            char[] cArr3 = UPPER_HEX_DIGITS;
            return cArr2;
        }
        if (i > 1114111) {
            throw new IllegalArgumentException("Invalid unicode character value " + i);
        }
        char[] cArr4 = {'%', 'F', UPPER_HEX_DIGITS[(i >>> 2) & 7], '%', UPPER_HEX_DIGITS[(i & 3) | 8], UPPER_HEX_DIGITS[r1 & 15], '%', UPPER_HEX_DIGITS[(r1 & 3) | 8], UPPER_HEX_DIGITS[r1 & 15], '%', UPPER_HEX_DIGITS[(r1 & 3) | 8], UPPER_HEX_DIGITS[i & 15]};
        int i4 = ((((i >>> 4) >>> 2) >>> 4) >>> 2) >>> 4;
        return cArr4;
    }

    @Override // com.google.common.escape.UnicodeEscaper
    protected int nextEscapeIndex(CharSequence charSequence, int i, int i2) {
        Preconditions.checkNotNull(charSequence);
        while (i < i2) {
            char cCharAt = charSequence.charAt(i);
            if (cCharAt >= this.safeOctets.length || !this.safeOctets[cCharAt]) {
                break;
            }
            i++;
        }
        return i;
    }
}
