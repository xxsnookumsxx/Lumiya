package com.lumiyaviewer.lumiya.slproto.avatar;

import com.google.common.primitives.UnsignedBytes;
import com.lumiyaviewer.lumiya.Debug;
import com.lumiyaviewer.lumiya.openjpeg.OpenJPEG;
import com.lumiyaviewer.lumiya.render.GLTexture;
import com.lumiyaviewer.rawbuffers.DirectByteBuffer;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/* JADX INFO: loaded from: classes.dex */
public class SLPolyMorphData {
    private DirectByteBuffer indexBuffer;
    private boolean isMasked;
    private SLPolyMesh mesh;
    private SLVisualParamID morphID;
    private int numVertices;
    private DirectByteBuffer texCoordsBuffer;
    private DirectByteBuffer vertexBuffer;

    public SLPolyMorphData(SLVisualParamID sLVisualParamID, SLPolyMesh sLPolyMesh, DataInputStream dataInputStream) throws IOException {
        this.morphID = sLVisualParamID;
        this.mesh = sLPolyMesh;
        this.isMasked = dataInputStream.readByte() != 0;
        this.numVertices = dataInputStream.readInt();
        this.vertexBuffer = new DirectByteBuffer(this.numVertices * 24);
        this.texCoordsBuffer = new DirectByteBuffer(this.numVertices * 8);
        this.indexBuffer = new DirectByteBuffer(this.numVertices * 4);
        this.vertexBuffer.read(dataInputStream);
        this.texCoordsBuffer.read(dataInputStream);
        this.indexBuffer.read(dataInputStream);
        Debug.Log("SLPolyMorphData: Loaded morph '" + sLVisualParamID + "', vertices = " + this.numVertices);
    }

    public void applyMorphData(SLMeshData sLMeshData, float f, GLTexture gLTexture) {
        int height;
        int width;
        ByteBuffer extraComponentsBuffer = null;
        int iPosition = 0;
        if (this.isMasked && gLTexture != null) {
            width = gLTexture.getWidth();
            height = gLTexture.getHeight();
            extraComponentsBuffer = gLTexture.getExtraComponentsBuffer();
            if (extraComponentsBuffer != null) {
                iPosition = extraComponentsBuffer.position();
            }
        } else {
            height = 0;
            width = 0;
        }
        OpenJPEG.applyMeshMorph(f, sLMeshData.vertexBuffer.asByteBuffer(), sLMeshData.texCoordsBuffer.asByteBuffer(), this.numVertices, this.indexBuffer.asByteBuffer(), this.vertexBuffer.asByteBuffer(), this.texCoordsBuffer.asByteBuffer(), width, height, iPosition, extraComponentsBuffer);
    }

    public void applyMorphDataSlow(SLMeshData sLMeshData, float f, GLTexture gLTexture) {
        FloatBuffer floatBufferAsFloatBuffer = this.vertexBuffer.asFloatBuffer();
        FloatBuffer floatBufferAsFloatBuffer2 = this.texCoordsBuffer.asFloatBuffer();
        IntBuffer intBufferAsIntBuffer = this.indexBuffer.asIntBuffer();
        FloatBuffer floatBufferAsFloatBuffer3 = sLMeshData.vertexBuffer.asFloatBuffer();
        FloatBuffer floatBufferAsFloatBuffer4 = sLMeshData.texCoordsBuffer.asFloatBuffer();
        boolean z = this.isMasked && gLTexture != null;
        int width = 0;
        int height = 0;
        ByteBuffer extraComponentsBuffer = null;
        int iPosition = 0;
        if (z) {
            width = gLTexture.getWidth();
            height = gLTexture.getHeight();
            extraComponentsBuffer = gLTexture.getExtraComponentsBuffer();
            if (extraComponentsBuffer != null) {
                iPosition = extraComponentsBuffer.position();
            } else {
                z = false;
            }
        }
        for (int i = 0; i < this.numVertices; i++) {
            int i2 = intBufferAsIntBuffer.get(i);
            float f2 = z ? ((extraComponentsBuffer.get(((int) Math.floor(floatBufferAsFloatBuffer4.get((i2 * 2) + 0) * width)) + ((((int) Math.floor(floatBufferAsFloatBuffer4.get((i2 * 2) + 1) * height)) * width) + iPosition)) & 255) / 255.0f) * f : f;
            for (int i3 = 0; i3 < 6; i3++) {
                floatBufferAsFloatBuffer3.put((i2 * 6) + i3, floatBufferAsFloatBuffer3.get((i2 * 6) + i3) + (floatBufferAsFloatBuffer.get((i * 6) + i3) * f2));
            }
            for (int i4 = 0; i4 < 2; i4++) {
                floatBufferAsFloatBuffer4.put((i2 * 2) + i4, floatBufferAsFloatBuffer4.get((i2 * 2) + i4) + (floatBufferAsFloatBuffer2.get((i * 2) + i4) * f2));
            }
        }
    }
}
