package com.lumiyaviewer.lumiya.slproto.mesh;

import com.lumiyaviewer.lumiya.openjpeg.OpenJPEG;
import com.lumiyaviewer.lumiya.slproto.llsd.LLSDException;
import com.lumiyaviewer.lumiya.slproto.llsd.LLSDNode;
import com.lumiyaviewer.lumiya.slproto.types.LLVector2;
import com.lumiyaviewer.lumiya.slproto.types.LLVector3;
import com.lumiyaviewer.rawbuffers.DirectByteBuffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import javax.annotation.Nonnull;

/* JADX INFO: loaded from: classes.dex */
public class MeshFace {
    private final DirectByteBuffer indexBuffer;
    private final int numIndices;
    private final int numVertices;
    private final DirectByteBuffer texCoordsBuffer;
    private final DirectByteBuffer vertexBuffer;
    private final DirectByteBuffer weightBuffer;

    MeshFace(LLSDNode lLSDNode) throws LLSDException {
        if (lLSDNode.keyExists("NoGeometry") || (!lLSDNode.keyExists("Position")) || (!lLSDNode.keyExists("TriangleList"))) {
            this.vertexBuffer = null;
            this.indexBuffer = null;
            this.weightBuffer = null;
            this.texCoordsBuffer = null;
            this.numIndices = 0;
            this.numVertices = 0;
            return;
        }
        byte[] bArrAsBinary = lLSDNode.byKey("Position").asBinary();
        byte[] bArrAsBinary2 = lLSDNode.keyExists("Normal") ? lLSDNode.byKey("Normal").asBinary() : null;
        byte[] bArrAsBinary3 = lLSDNode.keyExists("TexCoord0") ? lLSDNode.byKey("TexCoord0").asBinary() : null;
        this.numVertices = bArrAsBinary.length / 6;
        this.vertexBuffer = new DirectByteBuffer(this.numVertices * 6 * 4);
        LLVector3 lLVector3 = new LLVector3(-0.5f, -0.5f, -0.5f);
        LLVector3 lLVector32 = new LLVector3(0.5f, 0.5f, 0.5f);
        if (lLSDNode.keyExists("PositionDomain")) {
            if (lLSDNode.byKey("PositionDomain").keyExists("Min")) {
                LLSDNode lLSDNodeByKey = lLSDNode.byKey("PositionDomain").byKey("Min");
                lLVector3.set((float) lLSDNodeByKey.byIndex(0).asDouble(), (float) lLSDNodeByKey.byIndex(1).asDouble(), (float) lLSDNodeByKey.byIndex(2).asDouble());
            }
            if (lLSDNode.byKey("PositionDomain").keyExists("Max")) {
                LLSDNode lLSDNodeByKey2 = lLSDNode.byKey("PositionDomain").byKey("Max");
                lLVector32.set((float) lLSDNodeByKey2.byIndex(0).asDouble(), (float) lLSDNodeByKey2.byIndex(1).asDouble(), (float) lLSDNodeByKey2.byIndex(2).asDouble());
            }
        }
        LLVector2 lLVector2 = null;
        LLVector2 lLVector22 = null;
        if (bArrAsBinary3 != null) {
            lLVector2 = new LLVector2(0.0f, 0.0f);
            lLVector22 = new LLVector2(0.0f, 0.0f);
            if (lLSDNode.keyExists("TexCoord0Domain")) {
                if (lLSDNode.byKey("TexCoord0Domain").keyExists("Min")) {
                    LLSDNode lLSDNodeByKey3 = lLSDNode.byKey("TexCoord0Domain").byKey("Min");
                    lLVector2.set((float) lLSDNodeByKey3.byIndex(0).asDouble(), (float) lLSDNodeByKey3.byIndex(1).asDouble());
                }
                if (lLSDNode.byKey("TexCoord0Domain").keyExists("Max")) {
                    LLSDNode lLSDNodeByKey4 = lLSDNode.byKey("TexCoord0Domain").byKey("Max");
                    lLVector22.set((float) lLSDNodeByKey4.byIndex(0).asDouble(), (float) lLSDNodeByKey4.byIndex(1).asDouble());
                }
            }
        }
        ShortBuffer shortBufferAsShortBuffer = ByteBuffer.wrap(bArrAsBinary).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
        ShortBuffer shortBufferAsShortBuffer2 = bArrAsBinary2 != null ? ByteBuffer.wrap(bArrAsBinary2).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer() : null;
        ShortBuffer shortBufferAsShortBuffer3 = bArrAsBinary3 != null ? ByteBuffer.wrap(bArrAsBinary3).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer() : null;
        this.vertexBuffer.position(0);
        for (int i = 0; i < this.numVertices; i++) {
            float f = (((shortBufferAsShortBuffer.get() & 65535) * (lLVector32.x - lLVector3.x)) / 65535.0f) + lLVector3.x;
            float f2 = (((shortBufferAsShortBuffer.get() & 65535) * (lLVector32.y - lLVector3.y)) / 65535.0f) + lLVector3.y;
            float f3 = (((shortBufferAsShortBuffer.get() & 65535) * (lLVector32.z - lLVector3.z)) / 65535.0f) + lLVector3.z;
            this.vertexBuffer.putFloat(f);
            this.vertexBuffer.putFloat(f2);
            this.vertexBuffer.putFloat(f3);
            if (shortBufferAsShortBuffer2 != null) {
                this.vertexBuffer.putFloat((((shortBufferAsShortBuffer2.get() & 65535) * 2.0f) / 65535.0f) - 1.0f);
                this.vertexBuffer.putFloat((((shortBufferAsShortBuffer2.get() & 65535) * 2.0f) / 65535.0f) - 1.0f);
                this.vertexBuffer.putFloat((((shortBufferAsShortBuffer2.get() & 65535) * 2.0f) / 65535.0f) - 1.0f);
            } else {
                this.vertexBuffer.putFloat(0.0f);
                this.vertexBuffer.putFloat(0.0f);
                this.vertexBuffer.putFloat(0.0f);
            }
        }
        if (shortBufferAsShortBuffer3 != null) {
            this.texCoordsBuffer = new DirectByteBuffer(this.numVertices * 2 * 4);
            this.texCoordsBuffer.position(0);
            for (int i2 = 0; i2 < this.numVertices; i2++) {
                float f4 = (((shortBufferAsShortBuffer3.get() & 65535) * (lLVector22.x - lLVector2.x)) / 65535.0f) + lLVector2.x;
                float f5 = (((shortBufferAsShortBuffer3.get() & 65535) * (lLVector22.y - lLVector2.y)) / 65535.0f) + lLVector2.y;
                this.texCoordsBuffer.putFloat(f4);
                this.texCoordsBuffer.putFloat(f5);
            }
        } else {
            this.texCoordsBuffer = null;
        }
        byte[] bArrAsBinary4 = lLSDNode.byKey("TriangleList").asBinary();
        this.numIndices = bArrAsBinary4.length / 2;
        this.indexBuffer = new DirectByteBuffer(this.numIndices * 2);
        this.indexBuffer.loadFromByteArray(0, bArrAsBinary4, 0, this.numIndices * 2);
        if (!lLSDNode.keyExists("Weights")) {
            this.weightBuffer = null;
            return;
        }
        byte[] bArrAsBinary5 = lLSDNode.byKey("Weights").asBinary();
        this.weightBuffer = new DirectByteBuffer(bArrAsBinary5.length);
        this.weightBuffer.loadFromByteArray(0, bArrAsBinary5, 0, bArrAsBinary5.length);
    }

    void PrepareInfluenceBuffer(@Nonnull MeshWeightsBuffer meshWeightsBuffer, int i) {
        OpenJPEG.meshPrepareSeparateInfluenceBuffer(this.weightBuffer.asByteBuffer(), this.numVertices, meshWeightsBuffer.jointIndexBuffer.asByteBuffer(), meshWeightsBuffer.weightsBuffer.asByteBuffer(), i);
    }

    void PrepareInfluenceBuffer(DirectByteBuffer directByteBuffer, int i) {
        if (this.weightBuffer != null) {
            OpenJPEG.meshPrepareInfluenceBuffer(this.weightBuffer.asByteBuffer(), this.numVertices, directByteBuffer.asByteBuffer(), i);
        }
    }

    final void UpdateRigged(DirectByteBuffer directByteBuffer, int i, float[] fArr, float[] fArr2) {
        if (this.weightBuffer == null || this.vertexBuffer == null || directByteBuffer == null) {
            return;
        }
        OpenJPEG.applyRiggedMeshMorph(directByteBuffer.asByteBuffer(), i, fArr, fArr2, this.vertexBuffer.asByteBuffer(), this.weightBuffer.asByteBuffer(), this.numVertices);
    }

    public final DirectByteBuffer getIndices() {
        return this.indexBuffer;
    }

    public final int getNumIndices() {
        return this.numIndices;
    }

    public final int getNumVertices() {
        return this.numVertices;
    }

    public final DirectByteBuffer getTexCoords() {
        return this.texCoordsBuffer;
    }

    public final DirectByteBuffer getVertices() {
        return this.vertexBuffer;
    }
}
