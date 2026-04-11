package com.google.common.escape;

import com.google.common.annotations.Beta;
import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Preconditions;

/* JADX INFO: loaded from: classes.dex */
@Beta
@GwtCompatible
public abstract class UnicodeEscaper extends Escaper {
    private static final int DEST_PAD = 32;

    protected UnicodeEscaper() {
    }

    protected static int codePointAt(CharSequence charSequence, int i, int i2) {
        Preconditions.checkNotNull(charSequence);
        if (i >= i2) {
            throw new IndexOutOfBoundsException("Index exceeds specified range");
        }
        int i3 = i + 1;
        char cCharAt = charSequence.charAt(i);
        if (cCharAt < 55296 || cCharAt > 57343) {
            return cCharAt;
        }
        if (cCharAt > 56319) {
            throw new IllegalArgumentException("Unexpected low surrogate character '" + cCharAt + "' with value " + ((int) cCharAt) + " at index " + (i3 - 1) + " in '" + ((Object) charSequence) + "'");
        }
        if (i3 == i2) {
            return -cCharAt;
        }
        char cCharAt2 = charSequence.charAt(i3);
        if (Character.isLowSurrogate(cCharAt2)) {
            return Character.toCodePoint(cCharAt, cCharAt2);
        }
        throw new IllegalArgumentException("Expected low surrogate but got char '" + cCharAt2 + "' with value " + ((int) cCharAt2) + " at index " + i3 + " in '" + ((Object) charSequence) + "'");
    }

    private static char[] growBuffer(char[] cArr, int i, int i2) {
        char[] cArr2 = new char[i2];
        if (i > 0) {
            System.arraycopy(cArr, 0, cArr2, 0, i);
        }
        return cArr2;
    }

    @Override // com.google.common.escape.Escaper
    public String escape(String str) {
        Preconditions.checkNotNull(str);
        int length = str.length();
        int iNextEscapeIndex = nextEscapeIndex(str, 0, length);
        return iNextEscapeIndex != length ? escapeSlow(str, iNextEscapeIndex) : str;
    }

    protected abstract char[] escape(int i);

    protected final String escapeSlow(String str, int i) {
        int length;
        char[] cArr;
        int i2;
        int length2 = str.length();
        char[] cArrCharBufferFromThreadLocal = Platform.charBufferFromThreadLocal();
        int i3 = 0;
        int i4 = 0;
        while (i < length2) {
            int iCodePointAt = codePointAt(str, i, length2);
            if (iCodePointAt < 0) {
                throw new IllegalArgumentException("Trailing high surrogate at end of input");
            }
            char[] cArrEscape = escape(iCodePointAt);
            int i5 = i + (!Character.isSupplementaryCodePoint(iCodePointAt) ? 1 : 2);
            if (cArrEscape == null) {
                i2 = i3;
                cArr = cArrCharBufferFromThreadLocal;
            } else {
                int i6 = i - i3;
                int length3 = i4 + i6 + cArrEscape.length;
                if (cArrCharBufferFromThreadLocal.length < length3) {
                    cArrCharBufferFromThreadLocal = growBuffer(cArrCharBufferFromThreadLocal, i4, length3 + (length2 - i) + 32);
                }
                if (i6 <= 0) {
                    length = i4;
                } else {
                    str.getChars(i3, i, cArrCharBufferFromThreadLocal, i4);
                    length = i6 + i4;
                }
                if (cArrEscape.length > 0) {
                    System.arraycopy(cArrEscape, 0, cArrCharBufferFromThreadLocal, length, cArrEscape.length);
                    length += cArrEscape.length;
                }
                i4 = length;
                cArr = cArrCharBufferFromThreadLocal;
                i2 = i5;
            }
            i = nextEscapeIndex(str, i5, length2);
            cArrCharBufferFromThreadLocal = cArr;
            i3 = i2;
        }
        int i7 = length2 - i3;
        if (i7 > 0) {
            int i8 = i7 + i4;
            if (cArrCharBufferFromThreadLocal.length < i8) {
                cArrCharBufferFromThreadLocal = growBuffer(cArrCharBufferFromThreadLocal, i4, i8);
            }
            str.getChars(i3, length2, cArrCharBufferFromThreadLocal, i4);
            i4 = i8;
        }
        return new String(cArrCharBufferFromThreadLocal, 0, i4);
    }

    protected int nextEscapeIndex(CharSequence charSequence, int i, int i2) {
        while (i < i2) {
            int iCodePointAt = codePointAt(charSequence, i, i2);
            if (iCodePointAt < 0 || escape(iCodePointAt) != null) {
                break;
            }
            i += !Character.isSupplementaryCodePoint(iCodePointAt) ? 1 : 2;
        }
        return i;
    }
}
