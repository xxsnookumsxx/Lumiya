package com.lumiyaviewer.lumiya.utils;

import com.google.common.base.Ascii;
import com.google.common.primitives.UnsignedBytes;
import com.lumiyaviewer.lumiya.slproto.types.LLVector3;
import java.io.DataInput;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/* JADX INFO: loaded from: classes.dex */
public class LittleEndianDataInputStream implements DataInput {
    private byte[] buf = new byte[8];
    private InputStream inputStream;

    public LittleEndianDataInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override // java.io.DataInput
    public boolean readBoolean() throws IOException {
        int i = this.inputStream.read();
        if (i == -1) {
            throw new EOFException("End of stream");
        }
        return i != 0;
    }

    @Override // java.io.DataInput
    public byte readByte() throws IOException {
        int i = this.inputStream.read();
        if (i == -1) {
            throw new EOFException("End of stream");
        }
        return (byte) (i & 255);
    }

    @Override // java.io.DataInput
    public char readChar() throws IOException {
        int i = this.inputStream.read();
        if (i == -1) {
            throw new EOFException("End of stream");
        }
        return (char) (i & 255);
    }

    @Override // java.io.DataInput
    public double readDouble() throws IOException {
        return Double.longBitsToDouble(readLong());
    }

    @Override // java.io.DataInput
    public float readFloat() throws IOException {
        return Float.intBitsToFloat(readInt());
    }

    @Override // java.io.DataInput
    public void readFully(byte[] bArr) throws IOException {
        if (this.inputStream.read(bArr, 0, bArr.length) != bArr.length) {
            throw new EOFException("End of stream");
        }
    }

    @Override // java.io.DataInput
    public void readFully(byte[] bArr, int i, int i2) throws IOException {
        if (this.inputStream.read(bArr, i, i2) != i2) {
            throw new EOFException("End of stream");
        }
    }

    @Override // java.io.DataInput
    public int readInt() throws IOException {
        if (this.inputStream.read(this.buf, 0, 4) != 4) {
            throw new EOFException("End of stream");
        }
        return (this.buf[3] << Ascii.CAN) | ((this.buf[2] & UnsignedBytes.MAX_VALUE) << 16) | ((this.buf[1] & UnsignedBytes.MAX_VALUE) << 8) | (this.buf[0] & UnsignedBytes.MAX_VALUE);
    }

    @Override // java.io.DataInput
    public String readLine() throws IOException {
        StringBuilder sb = new StringBuilder();
        while (true) {
            char c = readChar();
            if (c == '\n' || c == '\r') {
                break;
            }
            sb.append(c);
        }
        return sb.toString();
    }

    @Override // java.io.DataInput
    public long readLong() throws IOException {
        if (this.inputStream.read(this.buf, 0, 8) != 8) {
            throw new EOFException("End of stream");
        }
        return (((long) this.buf[7]) << 56) | (((long) (this.buf[6] & UnsignedBytes.MAX_VALUE)) << 48) | (((long) (this.buf[5] & UnsignedBytes.MAX_VALUE)) << 40) | (((long) (this.buf[4] & UnsignedBytes.MAX_VALUE)) << 32) | (((long) (this.buf[3] & UnsignedBytes.MAX_VALUE)) << 24) | (((long) (this.buf[2] & UnsignedBytes.MAX_VALUE)) << 16) | (((long) (this.buf[1] & UnsignedBytes.MAX_VALUE)) << 8) | ((long) (this.buf[0] & UnsignedBytes.MAX_VALUE));
    }

    @Override // java.io.DataInput
    public short readShort() throws IOException {
        if (this.inputStream.read(this.buf, 0, 2) != 2) {
            throw new EOFException("End of stream");
        }
        return (short) (((this.buf[1] & UnsignedBytes.MAX_VALUE) << 8) | (this.buf[0] & UnsignedBytes.MAX_VALUE));
    }

    @Override // java.io.DataInput
    public String readUTF() throws IOException {
        return readLine();
    }

    @Override // java.io.DataInput
    public int readUnsignedByte() throws IOException {
        int i = this.inputStream.read();
        if (i == -1) {
            throw new EOFException("End of stream");
        }
        return i & 255;
    }

    @Override // java.io.DataInput
    public int readUnsignedShort() throws IOException {
        if (this.inputStream.read(this.buf, 0, 2) != 2) {
            throw new EOFException("End of stream");
        }
        return ((this.buf[1] & UnsignedBytes.MAX_VALUE) << 8) | (this.buf[0] & UnsignedBytes.MAX_VALUE);
    }

    public LLVector3 readVector3() throws IOException {
        return new LLVector3(readFloat(), readFloat(), readFloat());
    }

    public String readZeroTerminatedString() throws IOException {
        StringBuilder sb = new StringBuilder();
        while (true) {
            char c = readChar();
            if (c == 0) {
                return sb.toString();
            }
            sb.append(c);
        }
    }

    @Override // java.io.DataInput
    public int skipBytes(int i) throws IOException {
        return (int) this.inputStream.skip(i);
    }
}
