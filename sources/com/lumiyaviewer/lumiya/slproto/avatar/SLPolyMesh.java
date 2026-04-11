package com.lumiyaviewer.lumiya.slproto.avatar;

import android.opengl.Matrix;
import com.lumiyaviewer.lumiya.Debug;
import com.lumiyaviewer.lumiya.openjpeg.OpenJPEG;
import com.lumiyaviewer.lumiya.render.GLTexture;
import com.lumiyaviewer.lumiya.slproto.types.LLQuaternion;
import com.lumiyaviewer.lumiya.slproto.types.LLVector3;
import com.lumiyaviewer.rawbuffers.DirectByteBuffer;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.EnumMap;
import java.util.Map;

/* JADX INFO: loaded from: classes.dex */
public class SLPolyMesh extends SLMeshData {
    protected boolean hasWeights;
    public int[] jointMap;
    private Map<SLVisualParamID, Integer> morphIndices = new EnumMap(SLVisualParamID.class);
    private SLPolyMorphData[] morphs;
    protected DirectByteBuffer weightsBuffer;

    public SLPolyMesh(DataInputStream dataInputStream, DataInputStream dataInputStream2) throws IOException {
        this.position = new LLVector3(dataInputStream.readFloat(), dataInputStream.readFloat(), dataInputStream.readFloat());
        this.scale = new LLVector3(dataInputStream.readFloat(), dataInputStream.readFloat(), dataInputStream.readFloat());
        this.rotation = new LLQuaternion(dataInputStream.readFloat(), dataInputStream.readFloat(), dataInputStream.readFloat(), dataInputStream.readFloat());
        this.hasWeights = dataInputStream.readByte() != 0;
        this.numVertices = dataInputStream.readInt();
        this.vertexBuffer = new DirectByteBuffer(this.numVertices * 24);
        this.texCoordsBuffer = new DirectByteBuffer(this.numVertices * 8);
        this.vertexBuffer.read(dataInputStream);
        this.texCoordsBuffer.read(dataInputStream);
        if (this.hasWeights) {
            this.weightsBuffer = new DirectByteBuffer(this.numVertices * 4);
            this.weightsBuffer.read(dataInputStream);
        }
        this.numFaces = dataInputStream.readInt();
        this.indexBuffer = new DirectByteBuffer(this.numFaces * 2 * 3);
        this.indexBuffer.read(dataInputStream);
        int i = dataInputStream.readInt();
        this.morphs = new SLPolyMorphData[i];
        int i2 = 0;
        int i3 = 0;
        DataInputStream dataInputStream3 = dataInputStream;
        while (i2 < i) {
            if (i3 >= 50 && dataInputStream2 != null) {
                i3 = 0;
                dataInputStream3 = dataInputStream2;
            }
            SLVisualParamID sLVisualParamID = SLVisualParamID.valuesCustom()[dataInputStream3.readInt()];
            this.morphs[i2] = new SLPolyMorphData(sLVisualParamID, this, dataInputStream3);
            this.morphIndices.put(sLVisualParamID, Integer.valueOf(i2));
            i2++;
            i3++;
        }
        int i4 = dataInputStream3.readInt();
        this.jointMap = new int[i4];
        for (int i5 = 0; i5 < i4; i5++) {
            this.jointMap[i5] = dataInputStream3.readInt();
        }
        Debug.Log("SLPolyMesh: Loaded, numVerts = " + this.numVertices + ", faces = " + this.numFaces + ", morphs = " + this.morphs.length);
    }

    public void applyMorphData(SLMeshData sLMeshData, float[] fArr, GLTexture gLTexture) {
        for (int i = 0; i < fArr.length; i++) {
            this.morphs[i].applyMorphData(sLMeshData, fArr[i], gLTexture);
        }
    }

    public void applySkeleton(SLAnimatedMeshData sLAnimatedMeshData, float[] fArr) {
        DirectByteBuffer animatedVertexData;
        if (!this.hasWeights || this.jointMap == null || (animatedVertexData = sLAnimatedMeshData.getAnimatedVertexData()) == null) {
            return;
        }
        OpenJPEG.applyMorphingTransform(this.numVertices, sLAnimatedMeshData.vertexBuffer.asByteBuffer(), animatedVertexData.asByteBuffer(), this.weightsBuffer.asByteBuffer(), this.jointMap, fArr);
    }

    public void applySkeletonSlow(SLAnimatedMeshData sLAnimatedMeshData, float[] fArr) {
        DirectByteBuffer animatedVertexData;
        double d;
        if (!this.hasWeights || this.jointMap == null || (animatedVertexData = sLAnimatedMeshData.getAnimatedVertexData()) == null) {
            return;
        }
        FloatBuffer floatBufferAsFloatBuffer = this.weightsBuffer.asFloatBuffer();
        FloatBuffer floatBufferAsFloatBuffer2 = sLAnimatedMeshData.vertexBuffer.asFloatBuffer();
        FloatBuffer floatBufferAsFloatBuffer3 = animatedVertexData.asFloatBuffer();
        float[] fArr2 = new float[16];
        float[] fArr3 = new float[16];
        double d2 = -1.0d;
        int i = 0;
        while (i < this.numVertices) {
            float f = floatBufferAsFloatBuffer.get(i);
            if (f != d2) {
                float fFloor = (float) Math.floor(f);
                float f2 = f - fFloor;
                int i2 = ((int) fFloor) - 1;
                int i3 = 0;
                if (i2 >= 0 && i2 < this.jointMap.length) {
                    i3 = this.jointMap[i2];
                }
                int i4 = (i2 + 1 < 0 || i2 + 1 >= this.jointMap.length) ? i3 : this.jointMap[i2 + 1];
                d = f;
                int i5 = i3 * 16;
                int i6 = i4 * 16;
                if (i5 == i6) {
                    System.arraycopy(fArr, i5, fArr3, 0, 16);
                } else {
                    for (int i7 = 0; i7 < 16; i7++) {
                        fArr3[i7] = (fArr[i5 + i7] * (1.0f - f2)) + (fArr[i6 + i7] * f2);
                    }
                }
            } else {
                d = d2;
            }
            fArr2[0] = floatBufferAsFloatBuffer2.get((i * 6) + 0);
            fArr2[1] = floatBufferAsFloatBuffer2.get((i * 6) + 1);
            fArr2[2] = floatBufferAsFloatBuffer2.get((i * 6) + 2);
            fArr2[3] = 1.0f;
            Matrix.multiplyMV(fArr2, 4, fArr3, 0, fArr2, 0);
            floatBufferAsFloatBuffer3.put((i * 6) + 0, fArr2[4]);
            floatBufferAsFloatBuffer3.put((i * 6) + 1, fArr2[5]);
            floatBufferAsFloatBuffer3.put((i * 6) + 2, fArr2[6]);
            i++;
            d2 = d;
        }
    }

    public int getMorphIndex(SLVisualParamID sLVisualParamID) {
        Integer num = this.morphIndices.get(sLVisualParamID);
        if (num == null) {
            return -1;
        }
        return num.intValue();
    }

    public int getNumMorphs() {
        return this.morphs.length;
    }
}
