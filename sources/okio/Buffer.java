package okio;

import com.google.common.base.Ascii;
import com.google.common.primitives.UnsignedBytes;
import com.lumiyaviewer.lumiya.slproto.prims.PrimProfileParams;
import com.lumiyaviewer.lumiya.slproto.textures.MutableSLTextureEntryFace;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/* JADX INFO: loaded from: classes.dex */
public final class Buffer implements BufferedSource, BufferedSink, Cloneable {
    private static final byte[] DIGITS = {48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 97, 98, 99, 100, 101, 102};
    static final int REPLACEMENT_CHARACTER = 65533;
    Segment head;
    long size;

    private ByteString digest(String str) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(str);
            if (this.head != null) {
                messageDigest.update(this.head.data, this.head.pos, this.head.limit - this.head.pos);
                for (Segment segment = this.head.next; segment != this.head; segment = segment.next) {
                    messageDigest.update(segment.data, segment.pos, segment.limit - segment.pos);
                }
            }
            return ByteString.of(messageDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError();
        }
    }

    private ByteString hmac(String str, ByteString byteString) {
        try {
            Mac mac = Mac.getInstance(str);
            mac.init(new SecretKeySpec(byteString.toByteArray(), str));
            if (this.head != null) {
                mac.update(this.head.data, this.head.pos, this.head.limit - this.head.pos);
                for (Segment segment = this.head.next; segment != this.head; segment = segment.next) {
                    mac.update(segment.data, segment.pos, segment.limit - segment.pos);
                }
            }
            return ByteString.of(mac.doFinal());
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException(e);
        } catch (NoSuchAlgorithmException e2) {
            throw new AssertionError();
        }
    }

    private boolean rangeEquals(Segment segment, int i, ByteString byteString, int i2, int i3) {
        int i4 = segment.limit;
        byte[] bArr = segment.data;
        int i5 = i;
        Segment segment2 = segment;
        while (i2 < i3) {
            if (i5 == i4) {
                segment2 = segment2.next;
                bArr = segment2.data;
                i5 = segment2.pos;
                i4 = segment2.limit;
            }
            if (bArr[i5] != byteString.getByte(i2)) {
                return false;
            }
            i5++;
            i2++;
        }
        return true;
    }

    private void readFrom(InputStream inputStream, long j, boolean z) throws IOException {
        if (inputStream == null) {
            throw new IllegalArgumentException("in == null");
        }
        while (true) {
            if (!(j > 0) && !z) {
                return;
            }
            Segment segmentWritableSegment = writableSegment(1);
            int i = inputStream.read(segmentWritableSegment.data, segmentWritableSegment.limit, (int) Math.min(j, 8192 - segmentWritableSegment.limit));
            if (i == -1) {
                if (!z) {
                    throw new EOFException();
                }
                return;
            } else {
                segmentWritableSegment.limit += i;
                this.size += (long) i;
                j -= (long) i;
            }
        }
    }

    @Override // okio.BufferedSource, okio.BufferedSink
    public Buffer buffer() {
        return this;
    }

    public void clear() {
        try {
            skip(this.size);
        } catch (EOFException e) {
            throw new AssertionError(e);
        }
    }

    public Buffer clone() {
        Buffer buffer = new Buffer();
        if (this.size == 0) {
            return buffer;
        }
        buffer.head = new Segment(this.head);
        Segment segment = buffer.head;
        Segment segment2 = buffer.head;
        Segment segment3 = buffer.head;
        segment2.prev = segment3;
        segment.next = segment3;
        for (Segment segment4 = this.head.next; segment4 != this.head; segment4 = segment4.next) {
            buffer.head.prev.push(new Segment(segment4));
        }
        buffer.size = this.size;
        return buffer;
    }

    @Override // okio.Source, java.io.Closeable, java.lang.AutoCloseable
    public void close() {
    }

    public long completeSegmentByteCount() {
        long j = this.size;
        if (j == 0) {
            return 0L;
        }
        Segment segment = this.head.prev;
        return (segment.limit < 8192 && segment.owner) ? j - ((long) (segment.limit - segment.pos)) : j;
    }

    public Buffer copyTo(OutputStream outputStream) throws IOException {
        return copyTo(outputStream, 0L, this.size);
    }

    public Buffer copyTo(OutputStream outputStream, long j, long j2) throws IOException {
        if (outputStream == null) {
            throw new IllegalArgumentException("out == null");
        }
        Util.checkOffsetAndCount(this.size, j, j2);
        if (j2 == 0) {
            return this;
        }
        Segment segment = this.head;
        while (true) {
            if (j < ((long) (segment.limit - segment.pos))) {
                break;
            }
            j -= (long) (segment.limit - segment.pos);
            segment = segment.next;
        }
        Segment segment2 = segment;
        while (true) {
            if (j2 <= 0) {
                return this;
            }
            int i = (int) (((long) segment2.pos) + j);
            int iMin = (int) Math.min(segment2.limit - i, j2);
            outputStream.write(segment2.data, i, iMin);
            j2 -= (long) iMin;
            segment2 = segment2.next;
            j = 0;
        }
    }

    public Buffer copyTo(Buffer buffer, long j, long j2) {
        if (buffer == null) {
            throw new IllegalArgumentException("out == null");
        }
        Util.checkOffsetAndCount(this.size, j, j2);
        if (j2 == 0) {
            return this;
        }
        buffer.size += j2;
        Segment segment = this.head;
        while (true) {
            if (j < ((long) (segment.limit - segment.pos))) {
                break;
            }
            j -= (long) (segment.limit - segment.pos);
            segment = segment.next;
        }
        Segment segment2 = segment;
        while (true) {
            if (j2 <= 0) {
                return this;
            }
            Segment segment3 = new Segment(segment2);
            segment3.pos = (int) (((long) segment3.pos) + j);
            segment3.limit = Math.min(segment3.pos + ((int) j2), segment3.limit);
            if (buffer.head != null) {
                buffer.head.prev.push(segment3);
            } else {
                segment3.prev = segment3;
                segment3.next = segment3;
                buffer.head = segment3;
            }
            j2 -= (long) (segment3.limit - segment3.pos);
            segment2 = segment2.next;
            j = 0;
        }
    }

    @Override // okio.BufferedSink
    public BufferedSink emit() {
        return this;
    }

    @Override // okio.BufferedSink
    public Buffer emitCompleteSegments() {
        return this;
    }

    /* JADX WARN: Code restructure failed: missing block: B:31:0x005e, code lost:
    
        if (r3 == r5.limit) goto L35;
     */
    /* JADX WARN: Code restructure failed: missing block: B:33:0x0062, code lost:
    
        if (r2 == r4.limit) goto L36;
     */
    /* JADX WARN: Code restructure failed: missing block: B:35:0x0066, code lost:
    
        r5 = r5.next;
        r3 = r5.pos;
     */
    /* JADX WARN: Code restructure failed: missing block: B:36:0x006b, code lost:
    
        r4 = r4.next;
        r2 = r4.pos;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public boolean equals(java.lang.Object r15) {
        /*
            r14 = this;
            r0 = 0
            r7 = 1
            r8 = 0
            if (r14 == r15) goto L15
            boolean r2 = r15 instanceof okio.Buffer
            if (r2 == 0) goto L16
            okio.Buffer r15 = (okio.Buffer) r15
            long r2 = r14.size
            long r4 = r15.size
            int r2 = (r2 > r4 ? 1 : (r2 == r4 ? 0 : -1))
            if (r2 == 0) goto L17
            return r8
        L15:
            return r7
        L16:
            return r8
        L17:
            long r2 = r14.size
            int r2 = (r2 > r0 ? 1 : (r2 == r0 ? 0 : -1))
            if (r2 != 0) goto L1e
            return r7
        L1e:
            okio.Segment r5 = r14.head
            okio.Segment r4 = r15.head
            int r3 = r5.pos
            int r2 = r4.pos
        L26:
            long r10 = r14.size
            int r6 = (r0 > r10 ? 1 : (r0 == r10 ? 0 : -1))
            if (r6 < 0) goto L57
            r6 = r7
        L2d:
            if (r6 != 0) goto L70
            int r6 = r5.limit
            int r6 = r6 - r3
            int r9 = r4.limit
            int r9 = r9 - r2
            int r6 = java.lang.Math.min(r6, r9)
            long r10 = (long) r6
            r9 = r8
        L3b:
            long r12 = (long) r9
            int r6 = (r12 > r10 ? 1 : (r12 == r10 ? 0 : -1))
            if (r6 < 0) goto L59
            r6 = r7
        L41:
            if (r6 != 0) goto L5c
            byte[] r12 = r5.data
            int r6 = r3 + 1
            r12 = r12[r3]
            byte[] r13 = r4.data
            int r3 = r2 + 1
            r2 = r13[r2]
            if (r12 != r2) goto L5b
            int r2 = r9 + 1
            r9 = r2
            r2 = r3
            r3 = r6
            goto L3b
        L57:
            r6 = r8
            goto L2d
        L59:
            r6 = r8
            goto L41
        L5b:
            return r8
        L5c:
            int r6 = r5.limit
            if (r3 == r6) goto L66
        L60:
            int r6 = r4.limit
            if (r2 == r6) goto L6b
        L64:
            long r0 = r0 + r10
            goto L26
        L66:
            okio.Segment r5 = r5.next
            int r3 = r5.pos
            goto L60
        L6b:
            okio.Segment r4 = r4.next
            int r2 = r4.pos
            goto L64
        L70:
            return r7
        */
        throw new UnsupportedOperationException("Method not decompiled: okio.Buffer.equals(java.lang.Object):boolean");
    }

    @Override // okio.BufferedSource
    public boolean exhausted() {
        return this.size == 0;
    }

    @Override // okio.BufferedSink, okio.Sink, java.io.Flushable
    public void flush() {
    }

    public byte getByte(long j) {
        Util.checkOffsetAndCount(this.size, j, 1L);
        Segment segment = this.head;
        while (true) {
            int i = segment.limit - segment.pos;
            if (!(j >= ((long) i))) {
                return segment.data[segment.pos + ((int) j)];
            }
            j -= (long) i;
            segment = segment.next;
        }
    }

    public int hashCode() {
        Segment segment = this.head;
        if (segment == null) {
            return 0;
        }
        int i = 1;
        do {
            int i2 = segment.pos;
            int i3 = segment.limit;
            while (i2 < i3) {
                int i4 = segment.data[i2] + (i * 31);
                i2++;
                i = i4;
            }
            segment = segment.next;
        } while (segment != this.head);
        return i;
    }

    public ByteString hmacSha1(ByteString byteString) {
        return hmac("HmacSHA1", byteString);
    }

    public ByteString hmacSha256(ByteString byteString) {
        return hmac("HmacSHA256", byteString);
    }

    @Override // okio.BufferedSource
    public long indexOf(byte b) {
        return indexOf(b, 0L);
    }

    /* JADX WARN: Removed duplicated region for block: B:38:0x006d  */
    /* JADX WARN: Removed duplicated region for block: B:40:0x0070  */
    /* JADX WARN: Removed duplicated region for block: B:41:0x007b  */
    /* JADX WARN: Removed duplicated region for block: B:47:0x0083 A[SYNTHETIC] */
    @Override // okio.BufferedSource
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public long indexOf(byte r13, long r14) {
        /*
            r12 = this;
            r0 = 0
            r10 = -1
            r5 = 1
            r6 = 0
            int r2 = (r14 > r0 ? 1 : (r14 == r0 ? 0 : -1))
            if (r2 < 0) goto L16
            r2 = r5
        Lb:
            if (r2 != 0) goto L18
            java.lang.IllegalArgumentException r0 = new java.lang.IllegalArgumentException
            java.lang.String r1 = "fromIndex < 0"
            r0.<init>(r1)
            throw r0
        L16:
            r2 = r6
            goto Lb
        L18:
            okio.Segment r2 = r12.head
            if (r2 == 0) goto L3a
            long r8 = r12.size
            long r8 = r8 - r14
            int r3 = (r8 > r14 ? 1 : (r8 == r14 ? 0 : -1))
            if (r3 < 0) goto L3b
            r3 = r5
        L24:
            if (r3 != 0) goto L3f
            long r0 = r12.size
            r4 = r2
        L29:
            int r2 = (r0 > r14 ? 1 : (r0 == r14 ? 0 : -1))
            if (r2 > 0) goto L3d
            r2 = r5
        L2e:
            if (r2 != 0) goto L67
            okio.Segment r4 = r4.prev
            int r2 = r4.limit
            int r3 = r4.pos
            int r2 = r2 - r3
            long r2 = (long) r2
            long r0 = r0 - r2
            goto L29
        L3a:
            return r10
        L3b:
            r3 = r6
            goto L24
        L3d:
            r2 = r6
            goto L2e
        L3f:
            r4 = r2
        L40:
            int r2 = r4.limit
            int r3 = r4.pos
            int r2 = r2 - r3
            long r2 = (long) r2
            long r2 = r2 + r0
            int r7 = (r2 > r14 ? 1 : (r2 == r14 ? 0 : -1))
            if (r7 < 0) goto L53
            r7 = r5
        L4c:
            if (r7 != 0) goto L67
            okio.Segment r0 = r4.next
            r4 = r0
            r0 = r2
            goto L40
        L53:
            r7 = r6
            goto L4c
        L55:
            r8 = r3[r2]
            if (r8 == r13) goto L7d
            int r2 = r2 + 1
        L5b:
            if (r2 < r7) goto L55
            int r2 = r4.limit
            int r3 = r4.pos
            int r2 = r2 - r3
            long r2 = (long) r2
            long r0 = r0 + r2
            okio.Segment r4 = r4.next
            r14 = r0
        L67:
            long r2 = r12.size
            int r2 = (r0 > r2 ? 1 : (r0 == r2 ? 0 : -1))
            if (r2 < 0) goto L7b
            r2 = r5
        L6e:
            if (r2 != 0) goto L83
            byte[] r3 = r4.data
            int r2 = r4.pos
            long r8 = (long) r2
            long r8 = r8 + r14
            long r8 = r8 - r0
            int r2 = (int) r8
            int r7 = r4.limit
            goto L5b
        L7b:
            r2 = r6
            goto L6e
        L7d:
            int r3 = r4.pos
            int r2 = r2 - r3
            long r2 = (long) r2
            long r0 = r0 + r2
            return r0
        L83:
            return r10
        */
        throw new UnsupportedOperationException("Method not decompiled: okio.Buffer.indexOf(byte, long):long");
    }

    @Override // okio.BufferedSource
    public long indexOf(ByteString byteString) throws IOException {
        return indexOf(byteString, 0L);
    }

    @Override // okio.BufferedSource
    public long indexOf(ByteString byteString, long j) throws IOException {
        long j2;
        Segment segment;
        if (byteString.size() == 0) {
            throw new IllegalArgumentException("bytes is empty");
        }
        if (!(j >= 0)) {
            throw new IllegalArgumentException("fromIndex < 0");
        }
        Segment segment2 = this.head;
        if (segment2 == null) {
            return -1L;
        }
        if (!(this.size - j >= j)) {
            j2 = this.size;
            segment = segment2;
            while (true) {
                if (j2 <= j) {
                    break;
                }
                segment = segment.prev;
                j2 -= (long) (segment.limit - segment.pos);
            }
        } else {
            j2 = 0;
            segment = segment2;
            while (true) {
                long j3 = ((long) (segment.limit - segment.pos)) + j2;
                if (j3 >= j) {
                    break;
                }
                segment = segment.next;
                j2 = j3;
            }
        }
        byte b = byteString.getByte(0);
        int size = byteString.size();
        long j4 = (this.size - ((long) size)) + 1;
        long j5 = j2;
        Segment segment3 = segment;
        while (true) {
            if (j5 >= j4) {
                return -1L;
            }
            byte[] bArr = segment3.data;
            int iMin = (int) Math.min(segment3.limit, (((long) segment3.pos) + j4) - j5);
            for (int i = (int) ((((long) segment3.pos) + j) - j5); i < iMin; i++) {
                if (bArr[i] == b && rangeEquals(segment3, i + 1, byteString, 1, size)) {
                    return ((long) (i - segment3.pos)) + j5;
                }
            }
            long j6 = ((long) (segment3.limit - segment3.pos)) + j5;
            segment3 = segment3.next;
            j5 = j6;
            j = j6;
        }
    }

    @Override // okio.BufferedSource
    public long indexOfElement(ByteString byteString) {
        return indexOfElement(byteString, 0L);
    }

    @Override // okio.BufferedSource
    public long indexOfElement(ByteString byteString, long j) {
        long j2;
        Segment segment;
        int i;
        if (!(j >= 0)) {
            throw new IllegalArgumentException("fromIndex < 0");
        }
        Segment segment2 = this.head;
        if (segment2 == null) {
            return -1L;
        }
        if (!(this.size - j >= j)) {
            j2 = this.size;
            segment = segment2;
            while (true) {
                if (j2 <= j) {
                    break;
                }
                segment = segment.prev;
                j2 -= (long) (segment.limit - segment.pos);
            }
        } else {
            j2 = 0;
            segment = segment2;
            while (true) {
                long j3 = ((long) (segment.limit - segment.pos)) + j2;
                if (j3 >= j) {
                    break;
                }
                segment = segment.next;
                j2 = j3;
            }
        }
        if (byteString.size() == 2) {
            byte b = byteString.getByte(0);
            byte b2 = byteString.getByte(1);
            loop4: while (true) {
                if (j2 >= this.size) {
                    return -1L;
                }
                byte[] bArr = segment.data;
                i = (int) ((((long) segment.pos) + j) - j2);
                int i2 = segment.limit;
                while (i < i2) {
                    byte b3 = bArr[i];
                    if (b3 == b || b3 == b2) {
                        break loop4;
                    }
                    i++;
                }
                j2 += (long) (segment.limit - segment.pos);
                segment = segment.next;
                j = j2;
            }
            return j2 + ((long) (i - segment.pos));
        }
        byte[] bArrInternalArray = byteString.internalArray();
        while (true) {
            if (j2 >= this.size) {
                return -1L;
            }
            byte[] bArr2 = segment.data;
            int i3 = (int) ((((long) segment.pos) + j) - j2);
            int i4 = segment.limit;
            for (int i5 = i3; i5 < i4; i5++) {
                byte b4 = bArr2[i5];
                for (byte b5 : bArrInternalArray) {
                    if (b4 == b5) {
                        return j2 + ((long) (i5 - segment.pos));
                    }
                }
            }
            j2 += (long) (segment.limit - segment.pos);
            segment = segment.next;
            j = j2;
        }
    }

    @Override // okio.BufferedSource
    public InputStream inputStream() {
        return new InputStream() { // from class: okio.Buffer.2
            @Override // java.io.InputStream
            public int available() {
                return (int) Math.min(Buffer.this.size, 2147483647L);
            }

            @Override // java.io.InputStream, java.io.Closeable, java.lang.AutoCloseable
            public void close() {
            }

            @Override // java.io.InputStream
            public int read() {
                if (Buffer.this.size <= 0) {
                    return -1;
                }
                return Buffer.this.readByte() & UnsignedBytes.MAX_VALUE;
            }

            @Override // java.io.InputStream
            public int read(byte[] bArr, int i, int i2) {
                return Buffer.this.read(bArr, i, i2);
            }

            public String toString() {
                return Buffer.this + ".inputStream()";
            }
        };
    }

    public ByteString md5() {
        return digest("MD5");
    }

    @Override // okio.BufferedSink
    public OutputStream outputStream() {
        return new OutputStream() { // from class: okio.Buffer.1
            @Override // java.io.OutputStream, java.io.Closeable, java.lang.AutoCloseable
            public void close() {
            }

            @Override // java.io.OutputStream, java.io.Flushable
            public void flush() {
            }

            public String toString() {
                return Buffer.this + ".outputStream()";
            }

            @Override // java.io.OutputStream
            public void write(int i) {
                Buffer.this.writeByte((int) ((byte) i));
            }

            @Override // java.io.OutputStream
            public void write(byte[] bArr, int i, int i2) {
                Buffer.this.write(bArr, i, i2);
            }
        };
    }

    @Override // okio.BufferedSource
    public boolean rangeEquals(long j, ByteString byteString) {
        return rangeEquals(j, byteString, 0, byteString.size());
    }

    @Override // okio.BufferedSource
    public boolean rangeEquals(long j, ByteString byteString, int i, int i2) {
        if (!(j < 0) && i >= 0 && i2 >= 0) {
            if (!(this.size - j < ((long) i2)) && byteString.size() - i >= i2) {
                for (int i3 = 0; i3 < i2; i3++) {
                    if (getByte(((long) i3) + j) != byteString.getByte(i + i3)) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override // okio.BufferedSource
    public int read(byte[] bArr) {
        return read(bArr, 0, bArr.length);
    }

    @Override // okio.BufferedSource
    public int read(byte[] bArr, int i, int i2) {
        Util.checkOffsetAndCount(bArr.length, i, i2);
        Segment segment = this.head;
        if (segment == null) {
            return -1;
        }
        int iMin = Math.min(i2, segment.limit - segment.pos);
        System.arraycopy(segment.data, segment.pos, bArr, i, iMin);
        segment.pos += iMin;
        this.size -= (long) iMin;
        if (segment.pos == segment.limit) {
            this.head = segment.pop();
            SegmentPool.recycle(segment);
        }
        return iMin;
    }

    @Override // okio.Source
    public long read(Buffer buffer, long j) {
        if (buffer == null) {
            throw new IllegalArgumentException("sink == null");
        }
        if (!(j >= 0)) {
            throw new IllegalArgumentException("byteCount < 0: " + j);
        }
        if (this.size == 0) {
            return -1L;
        }
        if (!(j <= this.size)) {
            j = this.size;
        }
        buffer.write(this, j);
        return j;
    }

    @Override // okio.BufferedSource
    public long readAll(Sink sink) throws IOException {
        long j = this.size;
        if (!(j <= 0)) {
            sink.write(this, j);
        }
        return j;
    }

    @Override // okio.BufferedSource
    public byte readByte() {
        if (this.size == 0) {
            throw new IllegalStateException("size == 0");
        }
        Segment segment = this.head;
        int i = segment.pos;
        int i2 = segment.limit;
        int i3 = i + 1;
        byte b = segment.data[i];
        this.size--;
        if (i3 != i2) {
            segment.pos = i3;
        } else {
            this.head = segment.pop();
            SegmentPool.recycle(segment);
        }
        return b;
    }

    @Override // okio.BufferedSource
    public byte[] readByteArray() {
        try {
            return readByteArray(this.size);
        } catch (EOFException e) {
            throw new AssertionError(e);
        }
    }

    @Override // okio.BufferedSource
    public byte[] readByteArray(long j) throws EOFException {
        Util.checkOffsetAndCount(this.size, 0L, j);
        if (!(j <= 2147483647L)) {
            throw new IllegalArgumentException("byteCount > Integer.MAX_VALUE: " + j);
        }
        byte[] bArr = new byte[(int) j];
        readFully(bArr);
        return bArr;
    }

    @Override // okio.BufferedSource
    public ByteString readByteString() {
        return new ByteString(readByteArray());
    }

    @Override // okio.BufferedSource
    public ByteString readByteString(long j) throws EOFException {
        return new ByteString(readByteArray(j));
    }

    /* JADX WARN: Removed duplicated region for block: B:10:0x0029  */
    /* JADX WARN: Removed duplicated region for block: B:47:0x00d5  */
    /* JADX WARN: Removed duplicated region for block: B:48:0x00e2  */
    /* JADX WARN: Removed duplicated region for block: B:51:0x002d A[EDGE_INSN: B:51:0x002d->B:12:0x002d BREAK  A[LOOP:0: B:7:0x001a->B:56:?], SYNTHETIC] */
    @Override // okio.BufferedSource
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public long readDecimalLong() {
        /*
            Method dump skipped, instruction units count: 234
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: okio.Buffer.readDecimalLong():long");
    }

    public Buffer readFrom(InputStream inputStream) throws IOException {
        readFrom(inputStream, Long.MAX_VALUE, true);
        return this;
    }

    public Buffer readFrom(InputStream inputStream, long j) throws IOException {
        if (!(j >= 0)) {
            throw new IllegalArgumentException("byteCount < 0: " + j);
        }
        readFrom(inputStream, j, false);
        return this;
    }

    @Override // okio.BufferedSource
    public void readFully(Buffer buffer, long j) throws EOFException {
        if (this.size >= j) {
            buffer.write(this, j);
        } else {
            buffer.write(this, this.size);
            throw new EOFException();
        }
    }

    @Override // okio.BufferedSource
    public void readFully(byte[] bArr) throws EOFException {
        int i = 0;
        while (i < bArr.length) {
            int i2 = read(bArr, i, bArr.length - i);
            if (i2 == -1) {
                throw new EOFException();
            }
            i += i2;
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:10:0x0026  */
    /* JADX WARN: Removed duplicated region for block: B:38:0x00be  */
    /* JADX WARN: Removed duplicated region for block: B:39:0x00cb  */
    /* JADX WARN: Removed duplicated region for block: B:42:0x002a A[EDGE_INSN: B:42:0x002a->B:12:0x002a BREAK  A[LOOP:0: B:7:0x0017->B:46:?], SYNTHETIC] */
    @Override // okio.BufferedSource
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public long readHexadecimalUnsignedLong() {
        /*
            Method dump skipped, instruction units count: 211
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: okio.Buffer.readHexadecimalUnsignedLong():long");
    }

    @Override // okio.BufferedSource
    public int readInt() {
        if (!(this.size >= 4)) {
            throw new IllegalStateException("size < 4: " + this.size);
        }
        Segment segment = this.head;
        int i = segment.pos;
        int i2 = segment.limit;
        if (i2 - i < 4) {
            return ((readByte() & UnsignedBytes.MAX_VALUE) << 24) | ((readByte() & UnsignedBytes.MAX_VALUE) << 16) | ((readByte() & UnsignedBytes.MAX_VALUE) << 8) | (readByte() & UnsignedBytes.MAX_VALUE);
        }
        byte[] bArr = segment.data;
        int i3 = i + 1;
        int i4 = i3 + 1;
        int i5 = ((bArr[i] & UnsignedBytes.MAX_VALUE) << 24) | ((bArr[i3] & UnsignedBytes.MAX_VALUE) << 16);
        int i6 = i4 + 1;
        int i7 = i5 | ((bArr[i4] & UnsignedBytes.MAX_VALUE) << 8);
        int i8 = i6 + 1;
        int i9 = i7 | (bArr[i6] & UnsignedBytes.MAX_VALUE);
        this.size -= 4;
        if (i8 != i2) {
            segment.pos = i8;
        } else {
            this.head = segment.pop();
            SegmentPool.recycle(segment);
        }
        return i9;
    }

    @Override // okio.BufferedSource
    public int readIntLe() {
        return Util.reverseBytesInt(readInt());
    }

    @Override // okio.BufferedSource
    public long readLong() {
        if (!(this.size >= 8)) {
            throw new IllegalStateException("size < 8: " + this.size);
        }
        Segment segment = this.head;
        int i = segment.pos;
        int i2 = segment.limit;
        if (i2 - i < 8) {
            return ((((long) readInt()) & 4294967295L) << 32) | (((long) readInt()) & 4294967295L);
        }
        byte[] bArr = segment.data;
        int i3 = i + 1;
        long j = (((long) bArr[i]) & 255) << 56;
        int i4 = i3 + 1;
        long j2 = ((((long) bArr[i3]) & 255) << 48) | j;
        int i5 = i4 + 1;
        long j3 = j2 | ((((long) bArr[i4]) & 255) << 40);
        int i6 = i5 + 1;
        long j4 = j3 | ((((long) bArr[i5]) & 255) << 32);
        int i7 = i6 + 1;
        long j5 = j4 | ((((long) bArr[i6]) & 255) << 24);
        int i8 = i7 + 1;
        long j6 = j5 | ((((long) bArr[i7]) & 255) << 16);
        int i9 = i8 + 1;
        long j7 = j6 | ((((long) bArr[i8]) & 255) << 8);
        int i10 = i9 + 1;
        long j8 = j7 | (((long) bArr[i9]) & 255);
        this.size -= 8;
        if (i10 != i2) {
            segment.pos = i10;
        } else {
            this.head = segment.pop();
            SegmentPool.recycle(segment);
        }
        return j8;
    }

    @Override // okio.BufferedSource
    public long readLongLe() {
        return Util.reverseBytesLong(readLong());
    }

    @Override // okio.BufferedSource
    public short readShort() {
        if (!(this.size >= 2)) {
            throw new IllegalStateException("size < 2: " + this.size);
        }
        Segment segment = this.head;
        int i = segment.pos;
        int i2 = segment.limit;
        if (i2 - i < 2) {
            return (short) (((readByte() & UnsignedBytes.MAX_VALUE) << 8) | (readByte() & UnsignedBytes.MAX_VALUE));
        }
        byte[] bArr = segment.data;
        int i3 = i + 1;
        int i4 = i3 + 1;
        int i5 = ((bArr[i] & UnsignedBytes.MAX_VALUE) << 8) | (bArr[i3] & UnsignedBytes.MAX_VALUE);
        this.size -= 2;
        if (i4 != i2) {
            segment.pos = i4;
        } else {
            this.head = segment.pop();
            SegmentPool.recycle(segment);
        }
        return (short) i5;
    }

    @Override // okio.BufferedSource
    public short readShortLe() {
        return Util.reverseBytesShort(readShort());
    }

    @Override // okio.BufferedSource
    public String readString(long j, Charset charset) throws EOFException {
        Util.checkOffsetAndCount(this.size, 0L, j);
        if (charset == null) {
            throw new IllegalArgumentException("charset == null");
        }
        if (!(j <= 2147483647L)) {
            throw new IllegalArgumentException("byteCount > Integer.MAX_VALUE: " + j);
        }
        if (j == 0) {
            return "";
        }
        Segment segment = this.head;
        if (!(((long) segment.pos) + j <= ((long) segment.limit))) {
            return new String(readByteArray(j), charset);
        }
        String str = new String(segment.data, segment.pos, (int) j, charset);
        segment.pos = (int) (((long) segment.pos) + j);
        this.size -= j;
        if (segment.pos == segment.limit) {
            this.head = segment.pop();
            SegmentPool.recycle(segment);
        }
        return str;
    }

    @Override // okio.BufferedSource
    public String readString(Charset charset) {
        try {
            return readString(this.size, charset);
        } catch (EOFException e) {
            throw new AssertionError(e);
        }
    }

    @Override // okio.BufferedSource
    public String readUtf8() {
        try {
            return readString(this.size, Util.UTF_8);
        } catch (EOFException e) {
            throw new AssertionError(e);
        }
    }

    @Override // okio.BufferedSource
    public String readUtf8(long j) throws EOFException {
        return readString(j, Util.UTF_8);
    }

    @Override // okio.BufferedSource
    public int readUtf8CodePoint() throws EOFException {
        int i;
        int i2;
        int i3;
        if (this.size == 0) {
            throw new EOFException();
        }
        byte b = getByte(0L);
        if ((b & 128) == 0) {
            i = 1;
            i2 = b & Ascii.DEL;
            i3 = 0;
        } else if ((b & 224) == 192) {
            i2 = b & 31;
            i = 2;
            i3 = 128;
        } else if ((b & PrimProfileParams.LL_PCODE_HOLE_MASK) == 224) {
            i2 = b & 15;
            i = 3;
            i3 = 2048;
        } else {
            if ((b & 248) != 240) {
                skip(1L);
                return REPLACEMENT_CHARACTER;
            }
            i2 = b & 7;
            i = 4;
            i3 = 65536;
        }
        if (!(this.size >= ((long) i))) {
            throw new EOFException("size < " + i + ": " + this.size + " (to read code point prefixed 0x" + Integer.toHexString(b) + ")");
        }
        for (int i4 = 1; i4 < i; i4++) {
            byte b2 = getByte(i4);
            if ((b2 & MutableSLTextureEntryFace.SHINY_MASK) != 128) {
                skip(i4);
                return REPLACEMENT_CHARACTER;
            }
            i2 = (i2 << 6) | (b2 & 63);
        }
        skip(i);
        return i2 <= 1114111 ? ((i2 >= 55296 && i2 <= 57343) || i2 < i3) ? REPLACEMENT_CHARACTER : i2 : REPLACEMENT_CHARACTER;
    }

    @Override // okio.BufferedSource
    public String readUtf8Line() throws EOFException {
        long jIndexOf = indexOf((byte) 10);
        if (jIndexOf != -1) {
            return readUtf8Line(jIndexOf);
        }
        if (this.size != 0) {
            return readUtf8(this.size);
        }
        return null;
    }

    String readUtf8Line(long j) throws EOFException {
        if ((j <= 0) || getByte(j - 1) != 13) {
            String utf8 = readUtf8(j);
            skip(1L);
            return utf8;
        }
        String utf82 = readUtf8(j - 1);
        skip(2L);
        return utf82;
    }

    @Override // okio.BufferedSource
    public String readUtf8LineStrict() throws EOFException {
        long jIndexOf = indexOf((byte) 10);
        if (jIndexOf != -1) {
            return readUtf8Line(jIndexOf);
        }
        Buffer buffer = new Buffer();
        copyTo(buffer, 0L, Math.min(32L, this.size));
        throw new EOFException("\\n not found: size=" + size() + " content=" + buffer.readByteString().hex() + "…");
    }

    @Override // okio.BufferedSource
    public boolean request(long j) {
        return !((this.size > j ? 1 : (this.size == j ? 0 : -1)) < 0);
    }

    @Override // okio.BufferedSource
    public void require(long j) throws EOFException {
        if (!(this.size >= j)) {
            throw new EOFException();
        }
    }

    List<Integer> segmentSizes() {
        if (this.head == null) {
            return Collections.emptyList();
        }
        ArrayList arrayList = new ArrayList();
        arrayList.add(Integer.valueOf(this.head.limit - this.head.pos));
        for (Segment segment = this.head.next; segment != this.head; segment = segment.next) {
            arrayList.add(Integer.valueOf(segment.limit - segment.pos));
        }
        return arrayList;
    }

    @Override // okio.BufferedSource
    public int select(Options options) {
        Segment segment = this.head;
        if (segment == null) {
            return options.indexOf(ByteString.EMPTY);
        }
        ByteString[] byteStringArr = options.byteStrings;
        int length = byteStringArr.length;
        for (int i = 0; i < length; i++) {
            ByteString byteString = byteStringArr[i];
            if (!(this.size < ((long) byteString.size())) && rangeEquals(segment, segment.pos, byteString, 0, byteString.size())) {
                try {
                    skip(byteString.size());
                    return i;
                } catch (EOFException e) {
                    throw new AssertionError(e);
                }
            }
        }
        return -1;
    }

    int selectPrefix(Options options) {
        Segment segment = this.head;
        ByteString[] byteStringArr = options.byteStrings;
        int length = byteStringArr.length;
        for (int i = 0; i < length; i++) {
            ByteString byteString = byteStringArr[i];
            int iMin = (int) Math.min(this.size, byteString.size());
            if (iMin == 0 || rangeEquals(segment, segment.pos, byteString, 0, iMin)) {
                return i;
            }
        }
        return -1;
    }

    public ByteString sha1() {
        return digest("SHA-1");
    }

    public ByteString sha256() {
        return digest("SHA-256");
    }

    public long size() {
        return this.size;
    }

    @Override // okio.BufferedSource
    public void skip(long j) throws EOFException {
        while (true) {
            if (j <= 0) {
                return;
            }
            if (this.head == null) {
                throw new EOFException();
            }
            int iMin = (int) Math.min(j, this.head.limit - this.head.pos);
            this.size -= (long) iMin;
            j -= (long) iMin;
            Segment segment = this.head;
            segment.pos = iMin + segment.pos;
            if (this.head.pos == this.head.limit) {
                Segment segment2 = this.head;
                this.head = segment2.pop();
                SegmentPool.recycle(segment2);
            }
        }
    }

    public ByteString snapshot() {
        if (this.size <= 2147483647L) {
            return snapshot((int) this.size);
        }
        throw new IllegalArgumentException("size > Integer.MAX_VALUE: " + this.size);
    }

    public ByteString snapshot(int i) {
        return i != 0 ? new SegmentedByteString(this, i) : ByteString.EMPTY;
    }

    @Override // okio.Source
    public Timeout timeout() {
        return Timeout.NONE;
    }

    public String toString() {
        return snapshot().toString();
    }

    Segment writableSegment(int i) {
        if (i < 1 || i > 8192) {
            throw new IllegalArgumentException();
        }
        if (this.head != null) {
            Segment segment = this.head.prev;
            return (segment.limit + i <= 8192 && segment.owner) ? segment : segment.push(SegmentPool.take());
        }
        this.head = SegmentPool.take();
        Segment segment2 = this.head;
        Segment segment3 = this.head;
        Segment segment4 = this.head;
        segment3.prev = segment4;
        segment2.next = segment4;
        return segment4;
    }

    @Override // okio.BufferedSink
    public Buffer write(ByteString byteString) {
        if (byteString == null) {
            throw new IllegalArgumentException("byteString == null");
        }
        byteString.write(this);
        return this;
    }

    @Override // okio.BufferedSink
    public Buffer write(byte[] bArr) {
        if (bArr != null) {
            return write(bArr, 0, bArr.length);
        }
        throw new IllegalArgumentException("source == null");
    }

    @Override // okio.BufferedSink
    public Buffer write(byte[] bArr, int i, int i2) {
        if (bArr == null) {
            throw new IllegalArgumentException("source == null");
        }
        Util.checkOffsetAndCount(bArr.length, i, i2);
        int i3 = i + i2;
        while (i < i3) {
            Segment segmentWritableSegment = writableSegment(1);
            int iMin = Math.min(i3 - i, 8192 - segmentWritableSegment.limit);
            System.arraycopy(bArr, i, segmentWritableSegment.data, segmentWritableSegment.limit, iMin);
            i += iMin;
            segmentWritableSegment.limit = iMin + segmentWritableSegment.limit;
        }
        this.size += (long) i2;
        return this;
    }

    @Override // okio.BufferedSink
    public BufferedSink write(Source source, long j) throws IOException {
        while (true) {
            if (j <= 0) {
                return this;
            }
            long j2 = source.read(this, j);
            if (j2 == -1) {
                throw new EOFException();
            }
            j -= j2;
        }
    }

    @Override // okio.Sink
    public void write(Buffer buffer, long j) {
        if (buffer == null) {
            throw new IllegalArgumentException("source == null");
        }
        if (buffer == this) {
            throw new IllegalArgumentException("source == this");
        }
        Util.checkOffsetAndCount(buffer.size, 0L, j);
        while (true) {
            if (j <= 0) {
                return;
            }
            if (!(j >= ((long) (buffer.head.limit - buffer.head.pos)))) {
                Segment segment = this.head == null ? null : this.head.prev;
                if (segment != null && segment.owner) {
                    if (!((((long) segment.limit) + j) - ((long) (!segment.shared ? segment.pos : 0)) > 8192)) {
                        buffer.head.writeTo(segment, (int) j);
                        buffer.size -= j;
                        this.size += j;
                        return;
                    }
                }
                buffer.head = buffer.head.split((int) j);
            }
            Segment segment2 = buffer.head;
            long j2 = segment2.limit - segment2.pos;
            buffer.head = segment2.pop();
            if (this.head != null) {
                this.head.prev.push(segment2).compact();
            } else {
                this.head = segment2;
                Segment segment3 = this.head;
                Segment segment4 = this.head;
                Segment segment5 = this.head;
                segment4.prev = segment5;
                segment3.next = segment5;
            }
            buffer.size -= j2;
            this.size += j2;
            j -= j2;
        }
    }

    @Override // okio.BufferedSink
    public long writeAll(Source source) throws IOException {
        if (source == null) {
            throw new IllegalArgumentException("source == null");
        }
        long j = 0;
        while (true) {
            long j2 = source.read(this, 8192L);
            if (j2 == -1) {
                return j;
            }
            j += j2;
        }
    }

    @Override // okio.BufferedSink
    public Buffer writeByte(int i) {
        Segment segmentWritableSegment = writableSegment(1);
        byte[] bArr = segmentWritableSegment.data;
        int i2 = segmentWritableSegment.limit;
        segmentWritableSegment.limit = i2 + 1;
        bArr[i2] = (byte) i;
        this.size++;
        return this;
    }

    @Override // okio.BufferedSink
    public Buffer writeDecimalLong(long j) {
        boolean z;
        long j2;
        int i;
        if (j == 0) {
            return writeByte(48);
        }
        if (j >= 0) {
            z = false;
            j2 = j;
        } else {
            j2 = -j;
            if (!(j2 >= 0)) {
                return writeUtf8("-9223372036854775808");
            }
            z = true;
        }
        if (j2 >= 100000000) {
            if (j2 >= 1000000000000L) {
                if (j2 >= 1000000000000000L) {
                    if (j2 >= 100000000000000000L) {
                        i = !((j2 > 1000000000000000000L ? 1 : (j2 == 1000000000000000000L ? 0 : -1)) >= 0) ? 18 : 19;
                    } else {
                        i = !((j2 > 10000000000000000L ? 1 : (j2 == 10000000000000000L ? 0 : -1)) >= 0) ? 16 : 17;
                    }
                } else {
                    if (j2 >= 10000000000000L) {
                        i = !((j2 > 100000000000000L ? 1 : (j2 == 100000000000000L ? 0 : -1)) >= 0) ? 14 : 15;
                    } else {
                        i = 13;
                    }
                }
            } else {
                if (j2 >= 10000000000L) {
                    i = !((j2 > 100000000000L ? 1 : (j2 == 100000000000L ? 0 : -1)) >= 0) ? 11 : 12;
                } else {
                    i = !((j2 > 1000000000L ? 1 : (j2 == 1000000000L ? 0 : -1)) >= 0) ? 9 : 10;
                }
            }
        } else {
            if (j2 >= 10000) {
                if (j2 >= 1000000) {
                    i = !((j2 > 10000000L ? 1 : (j2 == 10000000L ? 0 : -1)) >= 0) ? 7 : 8;
                } else {
                    i = !((j2 > 100000L ? 1 : (j2 == 100000L ? 0 : -1)) >= 0) ? 5 : 6;
                }
            } else {
                if (j2 >= 100) {
                    i = !((j2 > 1000L ? 1 : (j2 == 1000L ? 0 : -1)) >= 0) ? 3 : 4;
                } else {
                    i = !((j2 > 10L ? 1 : (j2 == 10L ? 0 : -1)) >= 0) ? 1 : 2;
                }
            }
        }
        if (z) {
            i++;
        }
        Segment segmentWritableSegment = writableSegment(i);
        byte[] bArr = segmentWritableSegment.data;
        int i2 = segmentWritableSegment.limit + i;
        while (j2 != 0) {
            i2--;
            bArr[i2] = DIGITS[(int) (j2 % 10)];
            j2 /= 10;
        }
        if (z) {
            bArr[i2 - 1] = 45;
        }
        segmentWritableSegment.limit += i;
        this.size = ((long) i) + this.size;
        return this;
    }

    @Override // okio.BufferedSink
    public Buffer writeHexadecimalUnsignedLong(long j) {
        if (j == 0) {
            return writeByte(48);
        }
        int iNumberOfTrailingZeros = (Long.numberOfTrailingZeros(Long.highestOneBit(j)) / 4) + 1;
        Segment segmentWritableSegment = writableSegment(iNumberOfTrailingZeros);
        byte[] bArr = segmentWritableSegment.data;
        int i = segmentWritableSegment.limit;
        for (int i2 = (segmentWritableSegment.limit + iNumberOfTrailingZeros) - 1; i2 >= i; i2--) {
            bArr[i2] = DIGITS[(int) (15 & j)];
            j >>>= 4;
        }
        segmentWritableSegment.limit += iNumberOfTrailingZeros;
        this.size = ((long) iNumberOfTrailingZeros) + this.size;
        return this;
    }

    @Override // okio.BufferedSink
    public Buffer writeInt(int i) {
        Segment segmentWritableSegment = writableSegment(4);
        byte[] bArr = segmentWritableSegment.data;
        int i2 = segmentWritableSegment.limit;
        int i3 = i2 + 1;
        bArr[i2] = (byte) ((i >>> 24) & 255);
        int i4 = i3 + 1;
        bArr[i3] = (byte) ((i >>> 16) & 255);
        int i5 = i4 + 1;
        bArr[i4] = (byte) ((i >>> 8) & 255);
        bArr[i5] = (byte) (i & 255);
        segmentWritableSegment.limit = i5 + 1;
        this.size += 4;
        return this;
    }

    @Override // okio.BufferedSink
    public Buffer writeIntLe(int i) {
        return writeInt(Util.reverseBytesInt(i));
    }

    @Override // okio.BufferedSink
    public Buffer writeLong(long j) {
        Segment segmentWritableSegment = writableSegment(8);
        byte[] bArr = segmentWritableSegment.data;
        int i = segmentWritableSegment.limit;
        int i2 = i + 1;
        bArr[i] = (byte) ((j >>> 56) & 255);
        int i3 = i2 + 1;
        bArr[i2] = (byte) ((j >>> 48) & 255);
        int i4 = i3 + 1;
        bArr[i3] = (byte) ((j >>> 40) & 255);
        int i5 = i4 + 1;
        bArr[i4] = (byte) ((j >>> 32) & 255);
        int i6 = i5 + 1;
        bArr[i5] = (byte) ((j >>> 24) & 255);
        int i7 = i6 + 1;
        bArr[i6] = (byte) ((j >>> 16) & 255);
        int i8 = i7 + 1;
        bArr[i7] = (byte) ((j >>> 8) & 255);
        bArr[i8] = (byte) (j & 255);
        segmentWritableSegment.limit = i8 + 1;
        this.size += 8;
        return this;
    }

    @Override // okio.BufferedSink
    public Buffer writeLongLe(long j) {
        return writeLong(Util.reverseBytesLong(j));
    }

    @Override // okio.BufferedSink
    public Buffer writeShort(int i) {
        Segment segmentWritableSegment = writableSegment(2);
        byte[] bArr = segmentWritableSegment.data;
        int i2 = segmentWritableSegment.limit;
        int i3 = i2 + 1;
        bArr[i2] = (byte) ((i >>> 8) & 255);
        bArr[i3] = (byte) (i & 255);
        segmentWritableSegment.limit = i3 + 1;
        this.size += 2;
        return this;
    }

    @Override // okio.BufferedSink
    public Buffer writeShortLe(int i) {
        return writeShort((int) Util.reverseBytesShort((short) i));
    }

    @Override // okio.BufferedSink
    public Buffer writeString(String str, int i, int i2, Charset charset) {
        if (str == null) {
            throw new IllegalArgumentException("string == null");
        }
        if (i < 0) {
            throw new IllegalAccessError("beginIndex < 0: " + i);
        }
        if (i2 < i) {
            throw new IllegalArgumentException("endIndex < beginIndex: " + i2 + " < " + i);
        }
        if (i2 > str.length()) {
            throw new IllegalArgumentException("endIndex > string.length: " + i2 + " > " + str.length());
        }
        if (charset == null) {
            throw new IllegalArgumentException("charset == null");
        }
        if (charset.equals(Util.UTF_8)) {
            return writeUtf8(str, i, i2);
        }
        byte[] bytes = str.substring(i, i2).getBytes(charset);
        return write(bytes, 0, bytes.length);
    }

    @Override // okio.BufferedSink
    public Buffer writeString(String str, Charset charset) {
        return writeString(str, 0, str.length(), charset);
    }

    public Buffer writeTo(OutputStream outputStream) throws IOException {
        return writeTo(outputStream, this.size);
    }

    public Buffer writeTo(OutputStream outputStream, long j) throws IOException {
        if (outputStream == null) {
            throw new IllegalArgumentException("out == null");
        }
        Util.checkOffsetAndCount(this.size, 0L, j);
        Segment segment = this.head;
        while (true) {
            if (j <= 0) {
                return this;
            }
            int iMin = (int) Math.min(j, segment.limit - segment.pos);
            outputStream.write(segment.data, segment.pos, iMin);
            segment.pos += iMin;
            this.size -= (long) iMin;
            j -= (long) iMin;
            if (segment.pos == segment.limit) {
                Segment segmentPop = segment.pop();
                this.head = segmentPop;
                SegmentPool.recycle(segment);
                segment = segmentPop;
            }
        }
    }

    @Override // okio.BufferedSink
    public Buffer writeUtf8(String str) {
        return writeUtf8(str, 0, str.length());
    }

    @Override // okio.BufferedSink
    public Buffer writeUtf8(String str, int i, int i2) {
        int i3;
        if (str == null) {
            throw new IllegalArgumentException("string == null");
        }
        if (i < 0) {
            throw new IllegalAccessError("beginIndex < 0: " + i);
        }
        if (i2 < i) {
            throw new IllegalArgumentException("endIndex < beginIndex: " + i2 + " < " + i);
        }
        if (i2 > str.length()) {
            throw new IllegalArgumentException("endIndex > string.length: " + i2 + " > " + str.length());
        }
        while (i < i2) {
            char cCharAt = str.charAt(i);
            if (cCharAt < 128) {
                Segment segmentWritableSegment = writableSegment(1);
                byte[] bArr = segmentWritableSegment.data;
                int i4 = segmentWritableSegment.limit - i;
                int iMin = Math.min(i2, 8192 - i4);
                i3 = i + 1;
                bArr[i4 + i] = (byte) cCharAt;
                while (i3 < iMin) {
                    if (str.charAt(i3) >= 128) {
                        break;
                    }
                    bArr[i3 + i4] = (byte) r7;
                    i3++;
                }
                int i5 = (i3 + i4) - segmentWritableSegment.limit;
                segmentWritableSegment.limit += i5;
                this.size = ((long) i5) + this.size;
            } else if (cCharAt < 2048) {
                writeByte((cCharAt >> 6) | 192);
                writeByte((cCharAt & '?') | 128);
                i3 = i + 1;
            } else if (cCharAt >= 55296 && cCharAt <= 57343) {
                char cCharAt2 = i + 1 >= i2 ? (char) 0 : str.charAt(i + 1);
                if (cCharAt <= 56319 && cCharAt2 >= 56320 && cCharAt2 <= 57343) {
                    int i6 = ((cCharAt2 & 9215) | ((cCharAt & 10239) << 10)) + 65536;
                    writeByte((i6 >> 18) | 240);
                    writeByte(((i6 >> 12) & 63) | 128);
                    writeByte(((i6 >> 6) & 63) | 128);
                    writeByte((i6 & 63) | 128);
                    i3 = i + 2;
                } else {
                    writeByte(63);
                    i++;
                }
            } else {
                writeByte((cCharAt >> '\f') | 224);
                writeByte(((cCharAt >> 6) & 63) | 128);
                writeByte((cCharAt & '?') | 128);
                i3 = i + 1;
            }
            i = i3;
        }
        return this;
    }

    @Override // okio.BufferedSink
    public Buffer writeUtf8CodePoint(int i) {
        if (i < 128) {
            writeByte(i);
        } else if (i < 2048) {
            writeByte((i >> 6) | 192);
            writeByte((i & 63) | 128);
        } else if (i >= 65536) {
            if (i > 1114111) {
                throw new IllegalArgumentException("Unexpected code point: " + Integer.toHexString(i));
            }
            writeByte((i >> 18) | 240);
            writeByte(((i >> 12) & 63) | 128);
            writeByte(((i >> 6) & 63) | 128);
            writeByte((i & 63) | 128);
        } else {
            if (i >= 55296 && i <= 57343) {
                throw new IllegalArgumentException("Unexpected code point: " + Integer.toHexString(i));
            }
            writeByte((i >> 12) | 224);
            writeByte(((i >> 6) & 63) | 128);
            writeByte((i & 63) | 128);
        }
        return this;
    }
}
