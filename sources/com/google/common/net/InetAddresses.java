package com.google.common.net;

import android.support.v4.internal.view.SupportMenu;
import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.hash.Hashing;
import com.google.common.io.ByteStreams;
import com.google.common.primitives.Ints;
import com.google.common.primitives.UnsignedBytes;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Locale;
import javax.annotation.Nullable;

/* JADX INFO: loaded from: classes.dex */
@Beta
public final class InetAddresses {
    private static final int IPV4_PART_COUNT = 4;
    private static final int IPV6_PART_COUNT = 8;
    private static final Inet4Address LOOPBACK4 = (Inet4Address) forString("127.0.0.1");
    private static final Inet4Address ANY4 = (Inet4Address) forString("0.0.0.0");

    @Beta
    public static final class TeredoInfo {
        private final Inet4Address client;
        private final int flags;
        private final int port;
        private final Inet4Address server;

        public TeredoInfo(@Nullable Inet4Address inet4Address, @Nullable Inet4Address inet4Address2, int i, int i2) {
            Preconditions.checkArgument(i >= 0 && i <= 65535, "port '%s' is out of range (0 <= port <= 0xffff)", Integer.valueOf(i));
            Preconditions.checkArgument(i2 >= 0 && i2 <= 65535, "flags '%s' is out of range (0 <= flags <= 0xffff)", Integer.valueOf(i2));
            this.server = (Inet4Address) MoreObjects.firstNonNull(inet4Address, InetAddresses.ANY4);
            this.client = (Inet4Address) MoreObjects.firstNonNull(inet4Address2, InetAddresses.ANY4);
            this.port = i;
            this.flags = i2;
        }

        public Inet4Address getClient() {
            return this.client;
        }

        public int getFlags() {
            return this.flags;
        }

        public int getPort() {
            return this.port;
        }

        public Inet4Address getServer() {
            return this.server;
        }
    }

    private InetAddresses() {
    }

    private static InetAddress bytesToInetAddress(byte[] bArr) {
        try {
            return InetAddress.getByAddress(bArr);
        } catch (UnknownHostException e) {
            throw new AssertionError(e);
        }
    }

    public static int coerceToInteger(InetAddress inetAddress) {
        return ByteStreams.newDataInput(getCoercedIPv4Address(inetAddress).getAddress()).readInt();
    }

    private static void compressLongestRunOfZeroes(int[] iArr) {
        int i = -1;
        int i2 = -1;
        int i3 = -1;
        for (int i4 = 0; i4 < iArr.length + 1; i4++) {
            if (i4 < iArr.length && iArr[i4] == 0) {
                if (i < 0) {
                    i = i4;
                }
            } else if (i >= 0) {
                int i5 = i4 - i;
                if (i5 > i2) {
                    i2 = i5;
                    i3 = i;
                }
                i = -1;
            }
        }
        if (i2 < 2) {
            return;
        }
        Arrays.fill(iArr, i3, i3 + i2, -1);
    }

    private static String convertDottedQuadToHex(String str) {
        int iLastIndexOf = str.lastIndexOf(58);
        String strSubstring = str.substring(0, iLastIndexOf + 1);
        byte[] bArrTextToNumericFormatV4 = textToNumericFormatV4(str.substring(iLastIndexOf + 1));
        if (bArrTextToNumericFormatV4 == null) {
            return null;
        }
        return strSubstring + Integer.toHexString(((bArrTextToNumericFormatV4[0] & UnsignedBytes.MAX_VALUE) << 8) | (bArrTextToNumericFormatV4[1] & UnsignedBytes.MAX_VALUE)) + ":" + Integer.toHexString((bArrTextToNumericFormatV4[3] & UnsignedBytes.MAX_VALUE) | ((bArrTextToNumericFormatV4[2] & UnsignedBytes.MAX_VALUE) << 8));
    }

    public static InetAddress decrement(InetAddress inetAddress) {
        byte[] address = inetAddress.getAddress();
        int length = address.length - 1;
        while (length >= 0 && address[length] == 0) {
            address[length] = -1;
            length--;
        }
        Preconditions.checkArgument(length >= 0, "Decrementing %s would wrap.", inetAddress);
        address[length] = (byte) (address[length] - 1);
        return bytesToInetAddress(address);
    }

    public static InetAddress forString(String str) {
        byte[] bArrIpStringToBytes = ipStringToBytes(str);
        if (bArrIpStringToBytes != null) {
            return bytesToInetAddress(bArrIpStringToBytes);
        }
        throw formatIllegalArgumentException("'%s' is not an IP string literal.", str);
    }

    public static InetAddress forUriString(String str) {
        String strSubstring;
        int i;
        Preconditions.checkNotNull(str);
        if (str.startsWith("[") && str.endsWith("]")) {
            strSubstring = str.substring(1, str.length() - 1);
            i = 16;
        } else {
            i = 4;
            strSubstring = str;
        }
        byte[] bArrIpStringToBytes = ipStringToBytes(strSubstring);
        if (bArrIpStringToBytes != null && bArrIpStringToBytes.length == i) {
            return bytesToInetAddress(bArrIpStringToBytes);
        }
        throw formatIllegalArgumentException("Not a valid URI IP literal: '%s'", str);
    }

    private static IllegalArgumentException formatIllegalArgumentException(String str, Object... objArr) {
        return new IllegalArgumentException(String.format(Locale.ROOT, str, objArr));
    }

    public static Inet4Address fromInteger(int i) {
        return getInet4Address(Ints.toByteArray(i));
    }

    public static InetAddress fromLittleEndianByteArray(byte[] bArr) throws UnknownHostException {
        byte[] bArr2 = new byte[bArr.length];
        for (int i = 0; i < bArr.length; i++) {
            bArr2[i] = bArr[(bArr.length - i) - 1];
        }
        return InetAddress.getByAddress(bArr2);
    }

    public static Inet4Address get6to4IPv4Address(Inet6Address inet6Address) {
        Preconditions.checkArgument(is6to4Address(inet6Address), "Address '%s' is not a 6to4 address.", toAddrString(inet6Address));
        return getInet4Address(Arrays.copyOfRange(inet6Address.getAddress(), 2, 6));
    }

    public static Inet4Address getCoercedIPv4Address(InetAddress inetAddress) {
        boolean z;
        if (inetAddress instanceof Inet4Address) {
            return (Inet4Address) inetAddress;
        }
        byte[] address = inetAddress.getAddress();
        int i = 0;
        while (true) {
            if (i < 15) {
                if (address[i] != 0) {
                    z = false;
                    break;
                }
                i++;
            } else {
                z = true;
                break;
            }
        }
        if (z && address[15] == 1) {
            return LOOPBACK4;
        }
        if (z && address[15] == 0) {
            return ANY4;
        }
        Inet6Address inet6Address = (Inet6Address) inetAddress;
        int iAsInt = Hashing.murmur3_32().hashLong(!hasEmbeddedIPv4ClientAddress(inet6Address) ? ByteBuffer.wrap(inet6Address.getAddress(), 0, 8).getLong() : getEmbeddedIPv4ClientAddress(inet6Address).hashCode()).asInt() | (-536870912);
        if (iAsInt == -1) {
            iAsInt = -2;
        }
        return getInet4Address(Ints.toByteArray(iAsInt));
    }

    public static Inet4Address getCompatIPv4Address(Inet6Address inet6Address) {
        Preconditions.checkArgument(isCompatIPv4Address(inet6Address), "Address '%s' is not IPv4-compatible.", toAddrString(inet6Address));
        return getInet4Address(Arrays.copyOfRange(inet6Address.getAddress(), 12, 16));
    }

    public static Inet4Address getEmbeddedIPv4ClientAddress(Inet6Address inet6Address) {
        if (isCompatIPv4Address(inet6Address)) {
            return getCompatIPv4Address(inet6Address);
        }
        if (is6to4Address(inet6Address)) {
            return get6to4IPv4Address(inet6Address);
        }
        if (isTeredoAddress(inet6Address)) {
            return getTeredoInfo(inet6Address).getClient();
        }
        throw formatIllegalArgumentException("'%s' has no embedded IPv4 address.", toAddrString(inet6Address));
    }

    private static Inet4Address getInet4Address(byte[] bArr) {
        Preconditions.checkArgument(bArr.length == 4, "Byte array has invalid length for an IPv4 address: %s != 4.", Integer.valueOf(bArr.length));
        return (Inet4Address) bytesToInetAddress(bArr);
    }

    public static Inet4Address getIsatapIPv4Address(Inet6Address inet6Address) {
        Preconditions.checkArgument(isIsatapAddress(inet6Address), "Address '%s' is not an ISATAP address.", toAddrString(inet6Address));
        return getInet4Address(Arrays.copyOfRange(inet6Address.getAddress(), 12, 16));
    }

    public static TeredoInfo getTeredoInfo(Inet6Address inet6Address) {
        Preconditions.checkArgument(isTeredoAddress(inet6Address), "Address '%s' is not a Teredo address.", toAddrString(inet6Address));
        byte[] address = inet6Address.getAddress();
        Inet4Address inet4Address = getInet4Address(Arrays.copyOfRange(address, 4, 8));
        int i = ByteStreams.newDataInput(address, 8).readShort() & 65535;
        int i2 = (ByteStreams.newDataInput(address, 10).readShort() ^ (-1)) & SupportMenu.USER_MASK;
        byte[] bArrCopyOfRange = Arrays.copyOfRange(address, 12, 16);
        for (int i3 = 0; i3 < bArrCopyOfRange.length; i3++) {
            bArrCopyOfRange[i3] = (byte) (bArrCopyOfRange[i3] ^ (-1));
        }
        return new TeredoInfo(inet4Address, getInet4Address(bArrCopyOfRange), i2, i);
    }

    public static boolean hasEmbeddedIPv4ClientAddress(Inet6Address inet6Address) {
        return isCompatIPv4Address(inet6Address) || is6to4Address(inet6Address) || isTeredoAddress(inet6Address);
    }

    private static String hextetsToIPv6String(int[] iArr) {
        StringBuilder sb = new StringBuilder(39);
        int i = 0;
        boolean z = false;
        while (i < iArr.length) {
            boolean z2 = iArr[i] >= 0;
            if (z2) {
                if (z) {
                    sb.append(':');
                }
                sb.append(Integer.toHexString(iArr[i]));
            } else if (i == 0 || z) {
                sb.append("::");
            }
            i++;
            z = z2;
        }
        return sb.toString();
    }

    public static InetAddress increment(InetAddress inetAddress) {
        byte[] address = inetAddress.getAddress();
        int length = address.length - 1;
        while (length >= 0 && address[length] == -1) {
            address[length] = 0;
            length--;
        }
        Preconditions.checkArgument(length >= 0, "Incrementing %s would wrap.", inetAddress);
        address[length] = (byte) (address[length] + 1);
        return bytesToInetAddress(address);
    }

    private static byte[] ipStringToBytes(String str) {
        boolean z = false;
        boolean z2 = false;
        for (int i = 0; i < str.length(); i++) {
            char cCharAt = str.charAt(i);
            if (cCharAt == '.') {
                z = true;
            } else if (cCharAt != ':') {
                if (Character.digit(cCharAt, 16) == -1) {
                    return null;
                }
            } else {
                if (z) {
                    return null;
                }
                z2 = true;
            }
        }
        if (!z2) {
            if (z) {
                return textToNumericFormatV4(str);
            }
            return null;
        }
        if (z && (str = convertDottedQuadToHex(str)) == null) {
            return null;
        }
        return textToNumericFormatV6(str);
    }

    public static boolean is6to4Address(Inet6Address inet6Address) {
        byte[] address = inet6Address.getAddress();
        return address[0] == 32 && address[1] == 2;
    }

    public static boolean isCompatIPv4Address(Inet6Address inet6Address) {
        if (!inet6Address.isIPv4CompatibleAddress()) {
            return false;
        }
        byte[] address = inet6Address.getAddress();
        return (address[12] == 0 && address[13] == 0 && address[14] == 0 && (address[15] == 0 || address[15] == 1)) ? false : true;
    }

    public static boolean isInetAddress(String str) {
        return ipStringToBytes(str) != null;
    }

    public static boolean isIsatapAddress(Inet6Address inet6Address) {
        if (isTeredoAddress(inet6Address)) {
            return false;
        }
        byte[] address = inet6Address.getAddress();
        return (address[8] | 3) == 3 && address[9] == 0 && address[10] == 94 && address[11] == -2;
    }

    public static boolean isMappedIPv4Address(String str) {
        byte[] bArrIpStringToBytes = ipStringToBytes(str);
        if (bArrIpStringToBytes == null || bArrIpStringToBytes.length != 16) {
            return false;
        }
        for (int i = 0; i < 10; i++) {
            if (bArrIpStringToBytes[i] != 0) {
                return false;
            }
        }
        for (int i2 = 10; i2 < 12; i2++) {
            if (bArrIpStringToBytes[i2] != -1) {
                return false;
            }
        }
        return true;
    }

    public static boolean isMaximum(InetAddress inetAddress) {
        for (byte b : inetAddress.getAddress()) {
            if (b != -1) {
                return false;
            }
        }
        return true;
    }

    public static boolean isTeredoAddress(Inet6Address inet6Address) {
        byte[] address = inet6Address.getAddress();
        return address[0] == 32 && address[1] == 1 && address[2] == 0 && address[3] == 0;
    }

    public static boolean isUriInetAddress(String str) {
        try {
            forUriString(str);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static short parseHextet(String str) {
        int i = Integer.parseInt(str, 16);
        if (i <= 65535) {
            return (short) i;
        }
        throw new NumberFormatException();
    }

    private static byte parseOctet(String str) {
        int i = Integer.parseInt(str);
        if (i <= 255 && (!str.startsWith("0") || str.length() <= 1)) {
            return (byte) i;
        }
        throw new NumberFormatException();
    }

    private static byte[] textToNumericFormatV4(String str) {
        if (str.split("\\.", 5).length != 4) {
            return null;
        }
        byte[] bArr = new byte[4];
        for (int i = 0; i < bArr.length; i++) {
            try {
                bArr[i] = parseOctet(r1[i]);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return bArr;
    }

    private static byte[] textToNumericFormatV6(String str) {
        int i;
        int length;
        int i2;
        String[] strArrSplit = str.split(":", 10);
        if (strArrSplit.length < 3 || strArrSplit.length > 9) {
            return null;
        }
        int i3 = -1;
        for (int i4 = 1; i4 < strArrSplit.length - 1; i4++) {
            if (strArrSplit[i4].length() == 0) {
                if (i3 >= 0) {
                    return null;
                }
                i3 = i4;
            }
        }
        if (i3 < 0) {
            length = strArrSplit.length;
            i2 = 0;
        } else {
            int length2 = (strArrSplit.length - i3) - 1;
            if (strArrSplit[0].length() != 0) {
                i = i3;
            } else {
                i = i3 - 1;
                if (i != 0) {
                    return null;
                }
            }
            if (strArrSplit[strArrSplit.length - 1].length() == 0 && length2 - 1 != 0) {
                return null;
            }
            int i5 = length2;
            length = i;
            i2 = i5;
        }
        int i6 = 8 - (length + i2);
        if (i3 >= 0 ? i6 < 1 : i6 != 0) {
            return null;
        }
        ByteBuffer byteBufferAllocate = ByteBuffer.allocate(16);
        for (int i7 = 0; i7 < length; i7++) {
            try {
                byteBufferAllocate.putShort(parseHextet(strArrSplit[i7]));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        for (int i8 = 0; i8 < i6; i8++) {
            byteBufferAllocate.putShort((short) 0);
        }
        while (i2 > 0) {
            byteBufferAllocate.putShort(parseHextet(strArrSplit[strArrSplit.length - i2]));
            i2--;
        }
        return byteBufferAllocate.array();
    }

    public static String toAddrString(InetAddress inetAddress) {
        Preconditions.checkNotNull(inetAddress);
        if (inetAddress instanceof Inet4Address) {
            return inetAddress.getHostAddress();
        }
        Preconditions.checkArgument(inetAddress instanceof Inet6Address);
        byte[] address = inetAddress.getAddress();
        int[] iArr = new int[8];
        for (int i = 0; i < iArr.length; i++) {
            iArr[i] = Ints.fromBytes((byte) 0, (byte) 0, address[i * 2], address[(i * 2) + 1]);
        }
        compressLongestRunOfZeroes(iArr);
        return hextetsToIPv6String(iArr);
    }

    public static String toUriString(InetAddress inetAddress) {
        return !(inetAddress instanceof Inet6Address) ? toAddrString(inetAddress) : "[" + toAddrString(inetAddress) + "]";
    }
}
