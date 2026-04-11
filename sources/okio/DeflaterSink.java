package okio;

import java.io.IOException;
import java.util.zip.Deflater;
import org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement;

/* JADX INFO: loaded from: classes.dex */
public final class DeflaterSink implements Sink {
    private boolean closed;
    private final Deflater deflater;
    private final BufferedSink sink;

    DeflaterSink(BufferedSink bufferedSink, Deflater deflater) {
        if (bufferedSink == null) {
            throw new IllegalArgumentException("source == null");
        }
        if (deflater == null) {
            throw new IllegalArgumentException("inflater == null");
        }
        this.sink = bufferedSink;
        this.deflater = deflater;
    }

    public DeflaterSink(Sink sink, Deflater deflater) {
        this(Okio.buffer(sink), deflater);
    }

    @IgnoreJRERequirement
    private void deflate(boolean z) throws IOException {
        Segment segmentWritableSegment;
        Buffer buffer = this.sink.buffer();
        while (true) {
            segmentWritableSegment = buffer.writableSegment(1);
            int iDeflate = !z ? this.deflater.deflate(segmentWritableSegment.data, segmentWritableSegment.limit, 8192 - segmentWritableSegment.limit) : this.deflater.deflate(segmentWritableSegment.data, segmentWritableSegment.limit, 8192 - segmentWritableSegment.limit, 2);
            if (iDeflate > 0) {
                segmentWritableSegment.limit += iDeflate;
                buffer.size += (long) iDeflate;
                this.sink.emitCompleteSegments();
            } else if (this.deflater.needsInput()) {
                break;
            }
        }
        if (segmentWritableSegment.pos != segmentWritableSegment.limit) {
            return;
        }
        buffer.head = segmentWritableSegment.pop();
        SegmentPool.recycle(segmentWritableSegment);
    }

    @Override // okio.Sink, java.io.Closeable, java.lang.AutoCloseable
    public void close() throws Throwable {
        Throwable th = null;
        if (this.closed) {
            return;
        }
        try {
            finishDeflate();
        } catch (Throwable th2) {
            th = th2;
        }
        try {
            this.deflater.end();
        } catch (Throwable th3) {
            if (th == null) {
                th = th3;
            }
        }
        try {
            this.sink.close();
            th = th;
        } catch (Throwable th4) {
            th = th4;
            if (th != null) {
                th = th;
            }
        }
        this.closed = true;
        if (th == null) {
            return;
        }
        Util.sneakyRethrow(th);
    }

    void finishDeflate() throws IOException {
        this.deflater.finish();
        deflate(false);
    }

    @Override // okio.Sink, java.io.Flushable
    public void flush() throws IOException {
        deflate(true);
        this.sink.flush();
    }

    @Override // okio.Sink
    public Timeout timeout() {
        return this.sink.timeout();
    }

    public String toString() {
        return "DeflaterSink(" + this.sink + ")";
    }

    @Override // okio.Sink
    public void write(Buffer buffer, long j) throws IOException {
        Util.checkOffsetAndCount(buffer.size, 0L, j);
        while (true) {
            if (j <= 0) {
                return;
            }
            Segment segment = buffer.head;
            int iMin = (int) Math.min(j, segment.limit - segment.pos);
            this.deflater.setInput(segment.data, segment.pos, iMin);
            deflate(false);
            buffer.size -= (long) iMin;
            segment.pos += iMin;
            if (segment.pos == segment.limit) {
                buffer.head = segment.pop();
                SegmentPool.recycle(segment);
            }
            j -= (long) iMin;
        }
    }
}
