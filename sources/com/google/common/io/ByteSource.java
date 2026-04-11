package com.google.common.io;

import com.google.common.annotations.Beta;
import com.google.common.base.Ascii;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.hash.Funnels;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Iterator;

/* JADX INFO: loaded from: classes.dex */
public abstract class ByteSource {

    private final class AsCharSource extends CharSource {
        private final Charset charset;

        private AsCharSource(Charset charset) {
            this.charset = (Charset) Preconditions.checkNotNull(charset);
        }

        @Override // com.google.common.io.CharSource
        public Reader openStream() throws IOException {
            return new InputStreamReader(ByteSource.this.openStream(), this.charset);
        }

        public String toString() {
            return ByteSource.this.toString() + ".asCharSource(" + this.charset + ")";
        }
    }

    private static class ByteArrayByteSource extends ByteSource {
        final byte[] bytes;
        final int length;
        final int offset;

        ByteArrayByteSource(byte[] bArr) {
            this(bArr, 0, bArr.length);
        }

        ByteArrayByteSource(byte[] bArr, int i, int i2) {
            this.bytes = bArr;
            this.offset = i;
            this.length = i2;
        }

        @Override // com.google.common.io.ByteSource
        public long copyTo(OutputStream outputStream) throws IOException {
            outputStream.write(this.bytes, this.offset, this.length);
            return this.length;
        }

        @Override // com.google.common.io.ByteSource
        public HashCode hash(HashFunction hashFunction) throws IOException {
            return hashFunction.hashBytes(this.bytes, this.offset, this.length);
        }

        @Override // com.google.common.io.ByteSource
        public boolean isEmpty() {
            return this.length == 0;
        }

        @Override // com.google.common.io.ByteSource
        public InputStream openBufferedStream() throws IOException {
            return openStream();
        }

        @Override // com.google.common.io.ByteSource
        public InputStream openStream() {
            return new ByteArrayInputStream(this.bytes, this.offset, this.length);
        }

        @Override // com.google.common.io.ByteSource
        public <T> T read(ByteProcessor<T> byteProcessor) throws IOException {
            byteProcessor.processBytes(this.bytes, this.offset, this.length);
            return byteProcessor.getResult();
        }

        @Override // com.google.common.io.ByteSource
        public byte[] read() {
            return Arrays.copyOfRange(this.bytes, this.offset, this.offset + this.length);
        }

        @Override // com.google.common.io.ByteSource
        public long size() {
            return this.length;
        }

        @Override // com.google.common.io.ByteSource
        public Optional<Long> sizeIfKnown() {
            return Optional.of(Long.valueOf(this.length));
        }

        @Override // com.google.common.io.ByteSource
        public ByteSource slice(long j, long j2) {
            Preconditions.checkArgument(!((j > 0L ? 1 : (j == 0L ? 0 : -1)) < 0), "offset (%s) may not be negative", Long.valueOf(j));
            Preconditions.checkArgument(!((j2 > 0L ? 1 : (j2 == 0L ? 0 : -1)) < 0), "length (%s) may not be negative", Long.valueOf(j2));
            long jMin = Math.min(j, this.length);
            return new ByteArrayByteSource(this.bytes, ((int) jMin) + this.offset, (int) Math.min(j2, ((long) this.length) - jMin));
        }

        public String toString() {
            return "ByteSource.wrap(" + Ascii.truncate(BaseEncoding.base16().encode(this.bytes, this.offset, this.length), 30, "...") + ")";
        }
    }

    private static final class ConcatenatedByteSource extends ByteSource {
        final Iterable<? extends ByteSource> sources;

        ConcatenatedByteSource(Iterable<? extends ByteSource> iterable) {
            this.sources = (Iterable) Preconditions.checkNotNull(iterable);
        }

        @Override // com.google.common.io.ByteSource
        public boolean isEmpty() throws IOException {
            Iterator<? extends ByteSource> it = this.sources.iterator();
            while (it.hasNext()) {
                if (!it.next().isEmpty()) {
                    return false;
                }
            }
            return true;
        }

        @Override // com.google.common.io.ByteSource
        public InputStream openStream() throws IOException {
            return new MultiInputStream(this.sources.iterator());
        }

        @Override // com.google.common.io.ByteSource
        public long size() throws IOException {
            long size = 0;
            Iterator<? extends ByteSource> it = this.sources.iterator();
            while (true) {
                long j = size;
                if (!it.hasNext()) {
                    return j;
                }
                size = it.next().size() + j;
            }
        }

        @Override // com.google.common.io.ByteSource
        public Optional<Long> sizeIfKnown() {
            long jLongValue = 0;
            Iterator<? extends ByteSource> it = this.sources.iterator();
            while (true) {
                long j = jLongValue;
                if (!it.hasNext()) {
                    return Optional.of(Long.valueOf(j));
                }
                Optional<Long> optionalSizeIfKnown = it.next().sizeIfKnown();
                if (!optionalSizeIfKnown.isPresent()) {
                    return Optional.absent();
                }
                jLongValue = optionalSizeIfKnown.get().longValue() + j;
            }
        }

        public String toString() {
            return "ByteSource.concat(" + this.sources + ")";
        }
    }

    private static final class EmptyByteSource extends ByteArrayByteSource {
        static final EmptyByteSource INSTANCE = new EmptyByteSource();

        EmptyByteSource() {
            super(new byte[0]);
        }

        @Override // com.google.common.io.ByteSource
        public CharSource asCharSource(Charset charset) {
            Preconditions.checkNotNull(charset);
            return CharSource.empty();
        }

        @Override // com.google.common.io.ByteSource.ByteArrayByteSource, com.google.common.io.ByteSource
        public byte[] read() {
            return this.bytes;
        }

        @Override // com.google.common.io.ByteSource.ByteArrayByteSource
        public String toString() {
            return "ByteSource.empty()";
        }
    }

    private final class SlicedByteSource extends ByteSource {
        final long length;
        final long offset;

        SlicedByteSource(long j, long j2) {
            Preconditions.checkArgument(!((j > 0L ? 1 : (j == 0L ? 0 : -1)) < 0), "offset (%s) may not be negative", Long.valueOf(j));
            Preconditions.checkArgument(!((j2 > 0L ? 1 : (j2 == 0L ? 0 : -1)) < 0), "length (%s) may not be negative", Long.valueOf(j2));
            this.offset = j;
            this.length = j2;
        }

        private InputStream sliceStream(InputStream inputStream) throws Throwable {
            if (!(this.offset <= 0)) {
                try {
                    if (!(ByteStreams.skipUpTo(inputStream, this.offset) >= this.offset)) {
                        inputStream.close();
                        return new ByteArrayInputStream(new byte[0]);
                    }
                } finally {
                }
            }
            return ByteStreams.limit(inputStream, this.length);
        }

        @Override // com.google.common.io.ByteSource
        public boolean isEmpty() throws IOException {
            return this.length == 0 || super.isEmpty();
        }

        @Override // com.google.common.io.ByteSource
        public InputStream openBufferedStream() throws IOException {
            return sliceStream(ByteSource.this.openBufferedStream());
        }

        @Override // com.google.common.io.ByteSource
        public InputStream openStream() throws IOException {
            return sliceStream(ByteSource.this.openStream());
        }

        @Override // com.google.common.io.ByteSource
        public Optional<Long> sizeIfKnown() {
            Optional<Long> optionalSizeIfKnown = ByteSource.this.sizeIfKnown();
            if (!optionalSizeIfKnown.isPresent()) {
                return Optional.absent();
            }
            long jLongValue = optionalSizeIfKnown.get().longValue();
            return Optional.of(Long.valueOf(Math.min(this.length, jLongValue - Math.min(this.offset, jLongValue))));
        }

        @Override // com.google.common.io.ByteSource
        public ByteSource slice(long j, long j2) {
            Preconditions.checkArgument(!((j > 0L ? 1 : (j == 0L ? 0 : -1)) < 0), "offset (%s) may not be negative", Long.valueOf(j));
            Preconditions.checkArgument(!((j2 > 0L ? 1 : (j2 == 0L ? 0 : -1)) < 0), "length (%s) may not be negative", Long.valueOf(j2));
            return ByteSource.this.slice(this.offset + j, Math.min(j2, this.length - j));
        }

        public String toString() {
            return ByteSource.this.toString() + ".slice(" + this.offset + ", " + this.length + ")";
        }
    }

    protected ByteSource() {
    }

    public static ByteSource concat(Iterable<? extends ByteSource> iterable) {
        return new ConcatenatedByteSource(iterable);
    }

    public static ByteSource concat(Iterator<? extends ByteSource> it) {
        return concat(ImmutableList.copyOf(it));
    }

    public static ByteSource concat(ByteSource... byteSourceArr) {
        return concat(ImmutableList.copyOf(byteSourceArr));
    }

    private long countByReading(InputStream inputStream) throws IOException {
        long j = 0;
        while (true) {
            long j2 = inputStream.read(ByteStreams.skipBuffer);
            if (j2 == -1) {
                return j;
            }
            j += j2;
        }
    }

    private long countBySkipping(InputStream inputStream) throws IOException {
        long j = 0;
        while (true) {
            long jSkipUpTo = ByteStreams.skipUpTo(inputStream, 2147483647L);
            if (jSkipUpTo <= 0) {
                return j;
            }
            j += jSkipUpTo;
        }
    }

    public static ByteSource empty() {
        return EmptyByteSource.INSTANCE;
    }

    public static ByteSource wrap(byte[] bArr) {
        return new ByteArrayByteSource(bArr);
    }

    public CharSource asCharSource(Charset charset) {
        return new AsCharSource(charset);
    }

    public boolean contentEquals(ByteSource byteSource) throws Throwable {
        RuntimeException runtimeExceptionRethrow;
        int i;
        Preconditions.checkNotNull(byteSource);
        byte[] bArr = new byte[8192];
        byte[] bArr2 = new byte[8192];
        Closer closerCreate = Closer.create();
        try {
            try {
                InputStream inputStream = (InputStream) closerCreate.register(openStream());
                InputStream inputStream2 = (InputStream) closerCreate.register(byteSource.openStream());
                do {
                    i = ByteStreams.read(inputStream, bArr, 0, 8192);
                    if (i != ByteStreams.read(inputStream2, bArr2, 0, 8192) || !Arrays.equals(bArr, bArr2)) {
                        return false;
                    }
                } while (i == 8192);
                return true;
            } finally {
            }
        } finally {
            closerCreate.close();
        }
    }

    public long copyTo(ByteSink byteSink) throws Throwable {
        RuntimeException runtimeExceptionRethrow;
        Preconditions.checkNotNull(byteSink);
        Closer closerCreate = Closer.create();
        try {
            try {
                return ByteStreams.copy((InputStream) closerCreate.register(openStream()), (OutputStream) closerCreate.register(byteSink.openStream()));
            } finally {
            }
        } finally {
            closerCreate.close();
        }
    }

    public long copyTo(OutputStream outputStream) throws Throwable {
        RuntimeException runtimeExceptionRethrow;
        Preconditions.checkNotNull(outputStream);
        Closer closerCreate = Closer.create();
        try {
            try {
                return ByteStreams.copy((InputStream) closerCreate.register(openStream()), outputStream);
            } finally {
            }
        } finally {
            closerCreate.close();
        }
    }

    public HashCode hash(HashFunction hashFunction) throws Throwable {
        Hasher hasherNewHasher = hashFunction.newHasher();
        copyTo(Funnels.asOutputStream(hasherNewHasher));
        return hasherNewHasher.hash();
    }

    public boolean isEmpty() throws Throwable {
        RuntimeException runtimeExceptionRethrow;
        Optional<Long> optionalSizeIfKnown = sizeIfKnown();
        if (optionalSizeIfKnown.isPresent() && optionalSizeIfKnown.get().longValue() == 0) {
            return true;
        }
        Closer closerCreate = Closer.create();
        try {
            try {
                return ((InputStream) closerCreate.register(openStream())).read() == -1;
            } finally {
            }
        } finally {
            closerCreate.close();
        }
    }

    public InputStream openBufferedStream() throws IOException {
        InputStream inputStreamOpenStream = openStream();
        return !(inputStreamOpenStream instanceof BufferedInputStream) ? new BufferedInputStream(inputStreamOpenStream) : (BufferedInputStream) inputStreamOpenStream;
    }

    public abstract InputStream openStream() throws IOException;

    @Beta
    public <T> T read(ByteProcessor<T> byteProcessor) throws Throwable {
        RuntimeException runtimeExceptionRethrow;
        Preconditions.checkNotNull(byteProcessor);
        Closer closerCreate = Closer.create();
        try {
            try {
                return (T) ByteStreams.readBytes((InputStream) closerCreate.register(openStream()), byteProcessor);
            } finally {
            }
        } finally {
            closerCreate.close();
        }
    }

    public byte[] read() throws Throwable {
        Closer closerCreate = Closer.create();
        try {
            try {
                return ByteStreams.toByteArray((InputStream) closerCreate.register(openStream()));
            } catch (Throwable th) {
                throw closerCreate.rethrow(th);
            }
        } finally {
            closerCreate.close();
        }
    }

    public long size() throws Throwable {
        Optional<Long> optionalSizeIfKnown = sizeIfKnown();
        if (optionalSizeIfKnown.isPresent()) {
            return optionalSizeIfKnown.get().longValue();
        }
        Closer closerCreate = Closer.create();
        try {
            return countBySkipping((InputStream) closerCreate.register(openStream()));
        } catch (IOException e) {
            closerCreate.close();
            closerCreate = Closer.create();
            try {
                try {
                    return countByReading((InputStream) closerCreate.register(openStream()));
                } catch (Throwable th) {
                    throw closerCreate.rethrow(th);
                }
            } finally {
            }
        } finally {
        }
    }

    @Beta
    public Optional<Long> sizeIfKnown() {
        return Optional.absent();
    }

    public ByteSource slice(long j, long j2) {
        return new SlicedByteSource(j, j2);
    }
}
