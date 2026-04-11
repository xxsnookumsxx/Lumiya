package com.google.common.io;

import com.google.common.base.Preconditions;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

/* JADX INFO: loaded from: classes.dex */
public abstract class ByteSink {

    private final class AsCharSink extends CharSink {
        private final Charset charset;

        private AsCharSink(Charset charset) {
            this.charset = (Charset) Preconditions.checkNotNull(charset);
        }

        @Override // com.google.common.io.CharSink
        public Writer openStream() throws IOException {
            return new OutputStreamWriter(ByteSink.this.openStream(), this.charset);
        }

        public String toString() {
            return ByteSink.this.toString() + ".asCharSink(" + this.charset + ")";
        }
    }

    protected ByteSink() {
    }

    public CharSink asCharSink(Charset charset) {
        return new AsCharSink(charset);
    }

    public OutputStream openBufferedStream() throws IOException {
        OutputStream outputStreamOpenStream = openStream();
        return !(outputStreamOpenStream instanceof BufferedOutputStream) ? new BufferedOutputStream(outputStreamOpenStream) : (BufferedOutputStream) outputStreamOpenStream;
    }

    public abstract OutputStream openStream() throws IOException;

    public void write(byte[] bArr) throws Throwable {
        RuntimeException runtimeExceptionRethrow;
        Preconditions.checkNotNull(bArr);
        Closer closerCreate = Closer.create();
        try {
            try {
                OutputStream outputStream = (OutputStream) closerCreate.register(openStream());
                outputStream.write(bArr);
                outputStream.flush();
            } finally {
            }
        } finally {
            closerCreate.close();
        }
    }

    public long writeFrom(InputStream inputStream) throws Throwable {
        Preconditions.checkNotNull(inputStream);
        Closer closerCreate = Closer.create();
        try {
            try {
                OutputStream outputStream = (OutputStream) closerCreate.register(openStream());
                long jCopy = ByteStreams.copy(inputStream, outputStream);
                outputStream.flush();
                return jCopy;
            } catch (Throwable th) {
                throw closerCreate.rethrow(th);
            }
        } finally {
            closerCreate.close();
        }
    }
}
