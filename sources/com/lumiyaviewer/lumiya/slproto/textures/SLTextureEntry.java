package com.lumiyaviewer.lumiya.slproto.textures;

import android.support.v4.internal.view.SupportMenu;
import com.google.common.base.Ascii;
import com.google.common.logging.nano.Vr;
import com.lumiyaviewer.lumiya.Debug;
import com.lumiyaviewer.lumiya.render.avatar.AvatarTextures;
import com.lumiyaviewer.lumiya.slproto.avatar.BakedTextureIndex;
import com.lumiyaviewer.lumiya.utils.InternPool;
import com.lumiyaviewer.lumiya.utils.UUIDPool;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Set;
import java.util.UUID;

/* JADX INFO: loaded from: classes.dex */
public class SLTextureEntry {
    public static final int MAX_FACES = 64;
    private static final SLTextureEntryFace[] emptyFaces = new SLTextureEntryFace[0];
    private static final InternPool<SLTextureEntry> pool = new InternPool<>();
    private final SLTextureEntryFace DefaultTexture;
    private final SLTextureEntryFace[] FaceTextures;
    private final long faceMask;
    private final int hashValue;

    private SLTextureEntry(SLTextureEntryFace sLTextureEntryFace, SLTextureEntryFace[] sLTextureEntryFaceArr) {
        this.DefaultTexture = sLTextureEntryFace;
        this.FaceTextures = sLTextureEntryFaceArr;
        long j = 0;
        for (int i2 = 0; i2 < sLTextureEntryFaceArr.length && i2 < MAX_FACES; i2++) {
            if (sLTextureEntryFaceArr[i2] != null) {
                j |= 1L << i2;
            }
        }
        this.faceMask = j;
        this.hashValue = getHashValue();
    }

    private SLTextureEntry(ByteBuffer byteBuffer, int i) {
        MutableSLTextureEntryFace mutableSLTextureEntryFace = new MutableSLTextureEntryFace(-1);
        if (byteBuffer.limit() - byteBuffer.position() < 16) {
            this.DefaultTexture = SLTextureEntryFace.create(mutableSLTextureEntryFace);
            this.FaceTextures = emptyFaces;
            this.faceMask = 0L;
            this.hashValue = getHashValue();
            return;
        }
        MutableSLTextureEntryFace[] mutableSLTextureEntryFaceArr = new MutableSLTextureEntryFace[MAX_FACES];
        long[] jArr = new long[1];
        int[] iArr2 = new int[1];
        mutableSLTextureEntryFace.setTextureID(UUIDPool.getUUID(getUUID(byteBuffer)));
        while (true) {
            long jReadFaceBitfield = ReadFaceBitfield(byteBuffer, iArr2);
            if (jReadFaceBitfield == 0) {
                break;
            }
            UUID uuid = UUIDPool.getUUID(getUUID(byteBuffer));
            long j = 1;
            int i3 = 0;
            while (i3 < iArr2[0] && i3 < MAX_FACES) {
                if ((jReadFaceBitfield & j) != 0) {
                    MutableSLTextureEntryFace face = CreateFace(mutableSLTextureEntryFaceArr, i3, jArr);
                    if (face != null) {
                        face.setTextureID(uuid);
                    }
                }
                i3++;
                j <<= 1;
            }
        }
        mutableSLTextureEntryFace.setRGBA(byteBuffer.getInt());
        while (true) {
            long jReadFaceBitfield2 = ReadFaceBitfield(byteBuffer, iArr2);
            if (jReadFaceBitfield2 == 0) {
                break;
            }
            int i4 = byteBuffer.getInt();
            long j2 = 1;
            int i6 = 0;
            while (i6 < iArr2[0] && i6 < MAX_FACES) {
                if ((jReadFaceBitfield2 & j2) != 0) {
                    MutableSLTextureEntryFace face2 = CreateFace(mutableSLTextureEntryFaceArr, i6, jArr);
                    if (face2 != null) {
                        face2.setRGBA(i4);
                    }
                }
                i6++;
                j2 <<= 1;
            }
        }
        mutableSLTextureEntryFace.setRepeatU(byteBuffer.getFloat());
        while (true) {
            long jReadFaceBitfield3 = ReadFaceBitfield(byteBuffer, iArr2);
            if (jReadFaceBitfield3 == 0) {
                break;
            }
            float f = byteBuffer.getFloat();
            long j3 = 1;
            int i8 = 0;
            while (i8 < iArr2[0] && i8 < MAX_FACES) {
                if ((jReadFaceBitfield3 & j3) != 0) {
                    MutableSLTextureEntryFace face3 = CreateFace(mutableSLTextureEntryFaceArr, i8, jArr);
                    if (face3 != null) {
                        face3.setRepeatU(f);
                    }
                }
                i8++;
                j3 <<= 1;
            }
        }
        mutableSLTextureEntryFace.setRepeatV(byteBuffer.getFloat());
        while (true) {
            long jReadFaceBitfield4 = ReadFaceBitfield(byteBuffer, iArr2);
            if (jReadFaceBitfield4 == 0) {
                break;
            }
            float f2 = byteBuffer.getFloat();
            long j4 = 1;
            int i10 = 0;
            while (i10 < iArr2[0] && i10 < MAX_FACES) {
                if ((jReadFaceBitfield4 & j4) != 0) {
                    MutableSLTextureEntryFace face4 = CreateFace(mutableSLTextureEntryFaceArr, i10, jArr);
                    if (face4 != null) {
                        face4.setRepeatV(f2);
                    }
                }
                i10++;
                j4 <<= 1;
            }
        }
        mutableSLTextureEntryFace.setOffsetU(getOffset(byteBuffer));
        while (true) {
            long jReadFaceBitfield5 = ReadFaceBitfield(byteBuffer, iArr2);
            if (jReadFaceBitfield5 == 0) {
                break;
            }
            float offset = getOffset(byteBuffer);
            long j5 = 1;
            int i12 = 0;
            while (i12 < iArr2[0] && i12 < MAX_FACES) {
                if ((jReadFaceBitfield5 & j5) != 0) {
                    MutableSLTextureEntryFace face5 = CreateFace(mutableSLTextureEntryFaceArr, i12, jArr);
                    if (face5 != null) {
                        face5.setOffsetU(offset);
                    }
                }
                i12++;
                j5 <<= 1;
            }
        }
        mutableSLTextureEntryFace.setOffsetV(getOffset(byteBuffer));
        while (true) {
            long jReadFaceBitfield6 = ReadFaceBitfield(byteBuffer, iArr2);
            if (jReadFaceBitfield6 == 0) {
                break;
            }
            float offset2 = getOffset(byteBuffer);
            long j6 = 1;
            int i14 = 0;
            while (i14 < iArr2[0] && i14 < MAX_FACES) {
                if ((jReadFaceBitfield6 & j6) != 0) {
                    MutableSLTextureEntryFace face6 = CreateFace(mutableSLTextureEntryFaceArr, i14, jArr);
                    if (face6 != null) {
                        face6.setOffsetV(offset2);
                    }
                }
                i14++;
                j6 <<= 1;
            }
        }
        mutableSLTextureEntryFace.setRotation(getRotation(byteBuffer));
        while (true) {
            long jReadFaceBitfield7 = ReadFaceBitfield(byteBuffer, iArr2);
            if (jReadFaceBitfield7 == 0) {
                break;
            }
            float rotation = getRotation(byteBuffer);
            long j7 = 1;
            int i16 = 0;
            while (i16 < iArr2[0] && i16 < MAX_FACES) {
                if ((jReadFaceBitfield7 & j7) != 0) {
                    MutableSLTextureEntryFace face7 = CreateFace(mutableSLTextureEntryFaceArr, i16, jArr);
                    if (face7 != null) {
                        face7.setRotation(rotation);
                    }
                }
                i16++;
                j7 <<= 1;
            }
        }
        mutableSLTextureEntryFace.setMaterial(byteBuffer.get());
        while (true) {
            long jReadFaceBitfield8 = ReadFaceBitfield(byteBuffer, iArr2);
            if (jReadFaceBitfield8 == 0) {
                break;
            }
            byte b = byteBuffer.get();
            long j8 = 1;
            int i18 = 0;
            while (i18 < iArr2[0] && i18 < MAX_FACES) {
                if ((jReadFaceBitfield8 & j8) != 0) {
                    MutableSLTextureEntryFace face8 = CreateFace(mutableSLTextureEntryFaceArr, i18, jArr);
                    if (face8 != null) {
                        face8.setMaterial(b);
                    }
                }
                i18++;
                j8 <<= 1;
            }
        }
        mutableSLTextureEntryFace.setMedia(byteBuffer.get());
        while (true) {
            long jReadFaceBitfield9 = ReadFaceBitfield(byteBuffer, iArr2);
            if (jReadFaceBitfield9 == 0) {
                break;
            }
            byte b2 = byteBuffer.get();
            long j9 = 1;
            int i20 = 0;
            while (i20 < iArr2[0] && i20 < MAX_FACES) {
                if ((jReadFaceBitfield9 & j9) != 0) {
                    MutableSLTextureEntryFace face9 = CreateFace(mutableSLTextureEntryFaceArr, i20, jArr);
                    if (face9 != null) {
                        face9.setMedia(b2);
                    }
                }
                i20++;
                j9 <<= 1;
            }
        }
        mutableSLTextureEntryFace.setGlow(getGlow(byteBuffer));
        while (true) {
            long jReadFaceBitfield10 = ReadFaceBitfield(byteBuffer, iArr2);
            if (jReadFaceBitfield10 == 0) {
                break;
            }
            float glow = getGlow(byteBuffer);
            long j10 = 1;
            int i22 = 0;
            while (i22 < iArr2[0] && i22 < MAX_FACES) {
                if ((jReadFaceBitfield10 & j10) != 0) {
                    MutableSLTextureEntryFace face10 = CreateFace(mutableSLTextureEntryFaceArr, i22, jArr);
                    if (face10 != null) {
                        face10.setGlow(glow);
                    }
                }
                i22++;
                j10 <<= 1;
            }
        }
        this.faceMask = jArr[0];
        int i23 = 64 - Long.numberOfLeadingZeros(this.faceMask);
        this.DefaultTexture = SLTextureEntryFace.create(mutableSLTextureEntryFace);
        if (i23 == 0) {
            this.FaceTextures = emptyFaces;
        } else {
            this.FaceTextures = new SLTextureEntryFace[i23];
            for (int i25 = 0; i25 < i23; i25++) {
                this.FaceTextures[i25] = SLTextureEntryFace.create(mutableSLTextureEntryFaceArr[i25]);
            }
        }
        this.hashValue = getHashValue();
    }

    private static MutableSLTextureEntryFace CreateFace(MutableSLTextureEntryFace[] mutableSLTextureEntryFaceArr, int i, long[] jArr) {
        if (i < 0 || i >= MAX_FACES) {
            return null;
        }
        if (mutableSLTextureEntryFaceArr[i] != null) {
            return mutableSLTextureEntryFaceArr[i];
        }
        jArr[0] = jArr[0] | (1L << i);
        mutableSLTextureEntryFaceArr[i] = new MutableSLTextureEntryFace(0);
        return mutableSLTextureEntryFaceArr[i];
    }

    private long ReadFaceBitfield(ByteBuffer byteBuffer, int[] iArr) {
        byte b;
        iArr[0] = 0;
        if (byteBuffer.position() >= byteBuffer.limit()) {
            return 0L;
        }
        long j = 0;
        do {
            b = byteBuffer.get();
            j = (j << 7) | ((long) (b & Ascii.DEL));
            iArr[0] = iArr[0] + 7;
        } while ((b & 128) != 0 && byteBuffer.position() < byteBuffer.limit());
        return j;
    }

    private void WriteFaceBitfield(ByteBuffer byteBuffer, long j) {
        int i2 = 0;
        long j2 = j;
        for (int i3 = 0; i3 < 10; i3++) {
            if ((j2 & (-128L)) == 0) {
                i2 = i3 + 1;
                break;
            }
            j2 >>>= 7;
        }
        Debug.Log(String.format("WriteFaceBitfield: faceBits = 0x%016x, count %d", Long.valueOf(j), Integer.valueOf(i2)));
        int i5 = (i2 - 1) * 7;
        for (int i6 = 0; i6 < i2; i6++) {
            byte b = (byte) ((j >>> i5) & ((long) Vr.VREvent.VrCore.ErrorCode.CONTROLLER_UNSTUCK));
            if (i6 != i2 - 1) {
                b = (byte) (b | 128);
            }
            Debug.Log(String.format("WriteFaceBitfield: i = %d, shift = %d, byte 0x%02x", Integer.valueOf(i6), Integer.valueOf(i5), Byte.valueOf(b)));
            byteBuffer.put(b);
            i5 -= 7;
        }
    }

    public static SLTextureEntry create(SLTextureEntryFace sLTextureEntryFace, SLTextureEntryFace[] sLTextureEntryFaceArr) {
        return pool.intern(new SLTextureEntry(sLTextureEntryFace, sLTextureEntryFaceArr));
    }

    public static SLTextureEntry create(ByteBuffer byteBuffer, int i) {
        return pool.intern(new SLTextureEntry(byteBuffer, i));
    }

    private static float getGlow(ByteBuffer byteBuffer) {
        return byteBuffer.get() / 255.0f;
    }

    private int getHashValue() {
        int length = this.FaceTextures.length + ((int) (this.faceMask ^ (this.faceMask >>> 32))) + this.DefaultTexture.hashCode();
        long j = 1;
        for (int i2 = 0; i2 < this.FaceTextures.length; i2++) {
            if ((this.faceMask & j) != 0 && this.FaceTextures[i2] != null) {
                length += this.FaceTextures[i2].hashCode();
            }
            j <<= 1;
        }
        return length;
    }

    private static float getOffset(ByteBuffer byteBuffer) {
        return byteBuffer.getShort() / 32767.0f;
    }

    private static float getRotation(ByteBuffer byteBuffer) {
        return (byteBuffer.getShort() / 32767.0f) * 3.1415927f * 2.0f;
    }

    private static UUID getUUID(ByteBuffer byteBuffer) {
        byteBuffer.order(ByteOrder.BIG_ENDIAN);
        UUID uuid = new UUID(byteBuffer.getLong(), byteBuffer.getLong());
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return uuid;
    }

    private static void putGlow(ByteBuffer byteBuffer, float f) {
        byteBuffer.put((byte) (255.0f * f));
    }

    private static void putOffset(ByteBuffer byteBuffer, float f) {
        byteBuffer.putShort((short) (32767.0f * f));
    }

    private static void putRotation(ByteBuffer byteBuffer, float f) {
        byteBuffer.putShort((short) ((f / 6.2831855f) * 32767.0f));
    }

    private static void putUUID(ByteBuffer byteBuffer, UUID uuid) {
        long mostSignificantBits;
        long leastSignificantBits = 0;
        byteBuffer.order(ByteOrder.BIG_ENDIAN);
        if (uuid != null) {
            mostSignificantBits = uuid.getMostSignificantBits();
            leastSignificantBits = uuid.getLeastSignificantBits();
        } else {
            mostSignificantBits = 0;
        }
        byteBuffer.putLong(mostSignificantBits);
        byteBuffer.putLong(leastSignificantBits);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
    }

    public final SLTextureEntryFace GetDefaultTexture() {
        return this.DefaultTexture;
    }

    public final SLTextureEntryFace GetFace(int i) {
        if (i < 0 || i >= MAX_FACES) {
            return null;
        }
        if (i < this.FaceTextures.length && this.FaceTextures[i] != null) {
            return this.FaceTextures[i];
        }
        return this.DefaultTexture;
    }

    public void collectBoMBakes(Set<BakedTextureIndex> set) {
        BakedTextureIndex byBakedUUID = BakedTextureIndex.getByBakedUUID(this.DefaultTexture.textureID());
        if (byBakedUUID != null) {
            set.add(byBakedUUID);
        }
        for (SLTextureEntryFace sLTextureEntryFace : this.FaceTextures) {
            if (sLTextureEntryFace != null && (sLTextureEntryFace.hasAttribute() & 1) != 0) {
                BakedTextureIndex byBakedUUID2 = BakedTextureIndex.getByBakedUUID(sLTextureEntryFace.textureID());
                if (byBakedUUID2 != null) {
                    set.add(byBakedUUID2);
                }
            }
        }
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || !(obj instanceof SLTextureEntry)) {
            return false;
        }
        SLTextureEntry sLTextureEntry = (SLTextureEntry) obj;
        if (this.faceMask != sLTextureEntry.faceMask || this.FaceTextures.length != sLTextureEntry.FaceTextures.length || !this.DefaultTexture.equals(sLTextureEntry.DefaultTexture)) {
            return false;
        }
        long j = 1;
        for (int i2 = 0; i2 < this.FaceTextures.length; i2++) {
            if ((this.faceMask & j) != 0 && !this.FaceTextures[i2].equals(sLTextureEntry.FaceTextures[i2])) {
                return false;
            }
            j <<= 1;
        }
        return true;
    }

    public long getFaceMask() {
        return this.faceMask;
    }

    public boolean hasBoMTextures() {
        if (BakedTextureIndex.isBakedMagicUUID(this.DefaultTexture.textureID())) {
            return true;
        }
        for (SLTextureEntryFace sLTextureEntryFace : this.FaceTextures) {
            if (sLTextureEntryFace != null && (sLTextureEntryFace.hasAttribute() & 1) != 0 && BakedTextureIndex.isBakedMagicUUID(sLTextureEntryFace.textureID())) {
                return true;
            }
        }
        return false;
    }

    public final int hashCode() {
        return this.hashValue;
    }

    public boolean isSingleFace() {
        return this.faceMask == 0;
    }

    public byte[] packByteArray() {
        ByteBuffer byteBufferAllocate = ByteBuffer.allocate(SupportMenu.USER_MASK);
        putUUID(byteBufferAllocate, this.DefaultTexture.textureID());
        for (int i = 0; i < this.FaceTextures.length; i++) {
            if (this.FaceTextures[i] != null) {
                boolean z = this.DefaultTexture.textureID() == null || !this.FaceTextures[i].getTextureID(this.DefaultTexture).equals(this.DefaultTexture.textureID());
                if (z) {
                    WriteFaceBitfield(byteBufferAllocate, 1L << i);
                    putUUID(byteBufferAllocate, this.FaceTextures[i].getTextureID(this.DefaultTexture));
                }
            }
        }
        WriteFaceBitfield(byteBufferAllocate, 0L);
        byteBufferAllocate.putInt(this.DefaultTexture.rgba());
        for (int i2 = 0; i2 < this.FaceTextures.length; i2++) {
            if (this.FaceTextures[i2] != null && this.FaceTextures[i2].getRGBA(this.DefaultTexture) != this.DefaultTexture.rgba()) {
                WriteFaceBitfield(byteBufferAllocate, 1L << i2);
                byteBufferAllocate.putInt(this.FaceTextures[i2].getRGBA(this.DefaultTexture));
            }
        }
        WriteFaceBitfield(byteBufferAllocate, 0L);
        byteBufferAllocate.putFloat(this.DefaultTexture.repeatU());
        for (int i3 = 0; i3 < this.FaceTextures.length; i3++) {
            if (this.FaceTextures[i3] != null && this.FaceTextures[i3].getRepeatU(this.DefaultTexture) != this.DefaultTexture.repeatU()) {
                WriteFaceBitfield(byteBufferAllocate, 1L << i3);
                byteBufferAllocate.putFloat(this.FaceTextures[i3].getRepeatU(this.DefaultTexture));
            }
        }
        WriteFaceBitfield(byteBufferAllocate, 0L);
        byteBufferAllocate.putFloat(this.DefaultTexture.repeatV());
        for (int i4 = 0; i4 < this.FaceTextures.length; i4++) {
            if (this.FaceTextures[i4] != null && this.FaceTextures[i4].getRepeatV(this.DefaultTexture) != this.DefaultTexture.repeatV()) {
                WriteFaceBitfield(byteBufferAllocate, 1L << i4);
                byteBufferAllocate.putFloat(this.FaceTextures[i4].getRepeatV(this.DefaultTexture));
            }
        }
        WriteFaceBitfield(byteBufferAllocate, 0L);
        putOffset(byteBufferAllocate, this.DefaultTexture.offsetU());
        for (int i5 = 0; i5 < this.FaceTextures.length; i5++) {
            if (this.FaceTextures[i5] != null && this.FaceTextures[i5].getOffsetU(this.DefaultTexture) != this.DefaultTexture.offsetU()) {
                WriteFaceBitfield(byteBufferAllocate, 1L << i5);
                putOffset(byteBufferAllocate, this.FaceTextures[i5].getOffsetU(this.DefaultTexture));
            }
        }
        WriteFaceBitfield(byteBufferAllocate, 0L);
        putOffset(byteBufferAllocate, this.DefaultTexture.offsetV());
        for (int i6 = 0; i6 < this.FaceTextures.length; i6++) {
            if (this.FaceTextures[i6] != null && this.FaceTextures[i6].getOffsetV(this.DefaultTexture) != this.DefaultTexture.offsetV()) {
                WriteFaceBitfield(byteBufferAllocate, 1L << i6);
                putOffset(byteBufferAllocate, this.FaceTextures[i6].getOffsetV(this.DefaultTexture));
            }
        }
        WriteFaceBitfield(byteBufferAllocate, 0L);
        putRotation(byteBufferAllocate, this.DefaultTexture.rotation());
        for (int i7 = 0; i7 < this.FaceTextures.length; i7++) {
            if (this.FaceTextures[i7] != null && this.FaceTextures[i7].getRotation(this.DefaultTexture) != this.DefaultTexture.rotation()) {
                WriteFaceBitfield(byteBufferAllocate, 1L << i7);
                putRotation(byteBufferAllocate, this.FaceTextures[i7].getRotation(this.DefaultTexture));
            }
        }
        WriteFaceBitfield(byteBufferAllocate, 0L);
        byteBufferAllocate.put(this.DefaultTexture.materialb());
        for (int i8 = 0; i8 < this.FaceTextures.length; i8++) {
            if (this.FaceTextures[i8] != null && this.FaceTextures[i8].getMaterial(this.DefaultTexture) != this.DefaultTexture.materialb()) {
                WriteFaceBitfield(byteBufferAllocate, 1L << i8);
                byteBufferAllocate.put(this.FaceTextures[i8].getMaterial(this.DefaultTexture));
            }
        }
        WriteFaceBitfield(byteBufferAllocate, 0L);
        byteBufferAllocate.put(this.DefaultTexture.mediab());
        for (int i9 = 0; i9 < this.FaceTextures.length; i9++) {
            if (this.FaceTextures[i9] != null && this.FaceTextures[i9].getMedia(this.DefaultTexture) != this.DefaultTexture.mediab()) {
                WriteFaceBitfield(byteBufferAllocate, 1L << i9);
                byteBufferAllocate.put(this.FaceTextures[i9].getMedia(this.DefaultTexture));
            }
        }
        WriteFaceBitfield(byteBufferAllocate, 0L);
        putGlow(byteBufferAllocate, this.DefaultTexture.glow());
        for (int i10 = 0; i10 < this.FaceTextures.length; i10++) {
            if (this.FaceTextures[i10] != null && this.FaceTextures[i10].getGlow(this.DefaultTexture) != this.DefaultTexture.glow()) {
                WriteFaceBitfield(byteBufferAllocate, 1L << i10);
                putGlow(byteBufferAllocate, this.FaceTextures[i10].getGlow(this.DefaultTexture));
            }
        }
        WriteFaceBitfield(byteBufferAllocate, 0L);
        byte[] bArr = new byte[byteBufferAllocate.position()];
        byteBufferAllocate.position(0);
        byteBufferAllocate.get(bArr);
        Debug.DumpBuffer("Baking: TEpacked: ", bArr);
        return bArr;
    }

    public SLTextureEntry resolveBoMTextures(AvatarTextures avatarTextures) {
        if (avatarTextures == null || !hasBoMTextures()) {
            return this;
        }
        SLTextureEntryFace sLTextureEntryFaceWithTextureID = this.DefaultTexture;
        BakedTextureIndex byBakedUUID = BakedTextureIndex.getByBakedUUID(this.DefaultTexture.textureID());
        if (byBakedUUID != null) {
            UUID texture = avatarTextures.getTexture(byBakedUUID.getFaceIndex());
            if (texture == null || texture.equals(UUIDPool.ZeroUUID)) {
                texture = AvatarTextures.DEFAULT_AVATAR_TEXTURE;
            }
            sLTextureEntryFaceWithTextureID = SLTextureEntryFace.withTextureID(this.DefaultTexture, texture);
        }
        SLTextureEntryFace[] sLTextureEntryFaceArr = new SLTextureEntryFace[this.FaceTextures.length];
        for (int i = 0; i < this.FaceTextures.length; i++) {
            SLTextureEntryFace sLTextureEntryFace = this.FaceTextures[i];
            if (sLTextureEntryFace != null && (sLTextureEntryFace.hasAttribute() & 1) != 0) {
                BakedTextureIndex byBakedUUID2 = BakedTextureIndex.getByBakedUUID(sLTextureEntryFace.textureID());
                if (byBakedUUID2 != null) {
                    UUID texture2 = avatarTextures.getTexture(byBakedUUID2.getFaceIndex());
                    if (texture2 == null || texture2.equals(UUIDPool.ZeroUUID)) {
                        texture2 = AvatarTextures.DEFAULT_AVATAR_TEXTURE;
                    }
                    sLTextureEntryFace = SLTextureEntryFace.withTextureID(sLTextureEntryFace, texture2);
                }
            }
            sLTextureEntryFaceArr[i] = sLTextureEntryFace;
        }
        return create(sLTextureEntryFaceWithTextureID, sLTextureEntryFaceArr);
    }
}
