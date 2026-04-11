package okio;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/* JADX INFO: loaded from: classes.dex */
public final class HashingSource extends ForwardingSource {
    private final Mac mac;
    private final MessageDigest messageDigest;

    private HashingSource(Source source, String str) {
        super(source);
        try {
            this.messageDigest = MessageDigest.getInstance(str);
            this.mac = null;
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError();
        }
    }

    private HashingSource(Source source, ByteString byteString, String str) {
        super(source);
        try {
            this.mac = Mac.getInstance(str);
            this.mac.init(new SecretKeySpec(byteString.toByteArray(), str));
            this.messageDigest = null;
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException(e);
        } catch (NoSuchAlgorithmException e2) {
            throw new AssertionError();
        }
    }

    public static HashingSource hmacSha1(Source source, ByteString byteString) {
        return new HashingSource(source, byteString, "HmacSHA1");
    }

    public static HashingSource hmacSha256(Source source, ByteString byteString) {
        return new HashingSource(source, byteString, "HmacSHA256");
    }

    public static HashingSource md5(Source source) {
        return new HashingSource(source, "MD5");
    }

    public static HashingSource sha1(Source source) {
        return new HashingSource(source, "SHA-1");
    }

    public static HashingSource sha256(Source source) {
        return new HashingSource(source, "SHA-256");
    }

    public ByteString hash() {
        return ByteString.of(this.messageDigest == null ? this.mac.doFinal() : this.messageDigest.digest());
    }

    @Override // okio.ForwardingSource, okio.Source
    public long read(Buffer buffer, long j) throws IOException {
        long j2 = super.read(buffer, j);
        if (j2 != -1) {
            long j3 = buffer.size - j2;
            long j4 = buffer.size;
            Segment segment = buffer.head;
            while (true) {
                if (j4 <= j3) {
                    break;
                }
                segment = segment.prev;
                j4 -= (long) (segment.limit - segment.pos);
            }
            while (true) {
                if (j4 >= buffer.size) {
                    break;
                }
                int i = (int) ((j3 + ((long) segment.pos)) - j4);
                if (this.messageDigest == null) {
                    this.mac.update(segment.data, i, segment.limit - i);
                } else {
                    this.messageDigest.update(segment.data, i, segment.limit - i);
                }
                j4 += (long) (segment.limit - segment.pos);
                segment = segment.next;
                j3 = j4;
            }
        }
        return j2;
    }
}
