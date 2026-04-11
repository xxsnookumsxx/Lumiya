package okhttp3.internal.cache2;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import okio.Buffer;

/* JADX INFO: loaded from: classes.dex */
final class FileOperator {
    private static final int BUFFER_SIZE = 8192;
    private final byte[] byteArray = new byte[8192];
    private final ByteBuffer byteBuffer = ByteBuffer.wrap(this.byteArray);
    private final FileChannel fileChannel;

    public FileOperator(FileChannel fileChannel) {
        this.fileChannel = fileChannel;
    }

    public void read(long j, Buffer buffer, long j2) throws IOException {
        if (!(j2 >= 0)) {
            throw new IndexOutOfBoundsException();
        }
        while (true) {
            if (j2 <= 0) {
                return;
            }
            try {
                this.byteBuffer.limit((int) Math.min(8192L, j2));
                if (this.fileChannel.read(this.byteBuffer, j) == -1) {
                    throw new EOFException();
                }
                int iPosition = this.byteBuffer.position();
                buffer.write(this.byteArray, 0, iPosition);
                j += (long) iPosition;
                j2 -= (long) iPosition;
            } finally {
                this.byteBuffer.clear();
            }
        }
    }

    public void write(long j, Buffer buffer, long j2) throws IOException {
        if (!(j2 < 0)) {
            if (j2 <= buffer.size()) {
                long jWrite = j;
                while (true) {
                    if (j2 <= 0) {
                        return;
                    }
                    try {
                        int iMin = (int) Math.min(8192L, j2);
                        buffer.read(this.byteArray, 0, iMin);
                        this.byteBuffer.limit(iMin);
                        do {
                            jWrite += (long) this.fileChannel.write(this.byteBuffer, jWrite);
                        } while (this.byteBuffer.hasRemaining());
                        j2 -= (long) iMin;
                    } finally {
                        this.byteBuffer.clear();
                    }
                }
            }
        }
        throw new IndexOutOfBoundsException();
    }
}
