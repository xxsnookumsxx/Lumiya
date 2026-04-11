package com.lumiyaviewer.lumiya.slproto.mesh;

import android.opengl.GLES20;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.lumiyaviewer.lumiya.Debug;
import com.lumiyaviewer.lumiya.GlobalOptions;
import com.lumiyaviewer.lumiya.render.RenderContext;
import com.lumiyaviewer.lumiya.render.avatar.AvatarSkeleton;
import com.lumiyaviewer.lumiya.render.glres.buffers.GLLoadableBuffer;
import com.lumiyaviewer.lumiya.slproto.avatar.SLAttachmentPoint;
import com.lumiyaviewer.lumiya.slproto.avatar.SLSkeletonBoneID;
import com.lumiyaviewer.lumiya.slproto.llsd.LLSDException;
import com.lumiyaviewer.lumiya.slproto.llsd.LLSDNode;
import com.lumiyaviewer.rawbuffers.DirectByteBuffer;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import java.util.zip.InflaterInputStream;
import javax.annotation.Nullable;

/* JADX INFO: loaded from: classes.dex */
public class MeshData {
    public static final int MAX_RIGGED_MESH_JOINTS = 163;

    @Nullable
    private final float[] bindShapeMatrix;
    private final MeshFace[] faces;
    private GLLoadableBuffer glJointIndexBuffer;
    private GLLoadableBuffer glWeightsBuffer;

    @Nullable
    private final ImmutableMap<SLSkeletonBoneID, float[]> jointTranslations;
    private final float pelvisOffset;

    @Nullable
    private final MeshRiggingData riggingData;
    private MeshWeightsBuffer weightsBuffer;

    public MeshData(File file) throws IOException {
        float[] fArr;
        int[] iArr;
        float[] fArr2;
        SLAttachmentPoint sLAttachmentPoint;
        int length;
        GlobalOptions.MeshRendering meshRendering = GlobalOptions.getInstance().getMeshRendering();
        if (meshRendering == GlobalOptions.MeshRendering.disabled) {
            throw new IOException("Mesh rendering is disabled");
        }
        Debug.Printf("loading file '%s'", file.toString());
        boolean z = false;
        EnumMap enumMap = null;
        float fAsDouble = 0.0f;
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            DataInputStream dataInputStream = new DataInputStream(fileInputStream);
            try {
                LLSDNode lLSDNodeFromBinary = LLSDNode.fromBinary(dataInputStream);
                long jPosition = fileInputStream.getChannel().position();
                LLSDNode lLSDNodeByKey = null;
                if (lLSDNodeFromBinary.keyExists(meshRendering.getLODName())) {
                    lLSDNodeByKey = lLSDNodeFromBinary.byKey(meshRendering.getLODName());
                } else {
                    GlobalOptions.MeshRendering[] meshRenderingArrValuesCustom = GlobalOptions.MeshRendering.valuesCustom();
                    int iOrdinal = meshRendering.ordinal() + 1;
                    while (true) {
                        if (iOrdinal >= meshRenderingArrValuesCustom.length) {
                            break;
                        }
                        String lODName = meshRenderingArrValuesCustom[iOrdinal].getLODName();
                        if (lODName != null && lLSDNodeFromBinary.keyExists(lODName)) {
                            lLSDNodeByKey = lLSDNodeFromBinary.byKey(lODName);
                            break;
                        }
                        iOrdinal++;
                    }
                    if (lLSDNodeByKey == null) {
                        int iOrdinal2 = meshRendering.ordinal() - 1;
                        while (true) {
                            if (iOrdinal2 < 0) {
                                break;
                            }
                            String lODName2 = meshRenderingArrValuesCustom[iOrdinal2].getLODName();
                            if (lODName2 != null && lLSDNodeFromBinary.keyExists(lODName2)) {
                                lLSDNodeByKey = lLSDNodeFromBinary.byKey(lODName2);
                                break;
                            }
                            iOrdinal2--;
                        }
                    }
                }
                if (lLSDNodeByKey == null) {
                    throw new IOException("Mesh LOD not found");
                }
                fileInputStream.getChannel().position(((long) lLSDNodeByKey.byKey("offset").asInt()) + jPosition);
                InflaterInputStream inflaterInputStream = new InflaterInputStream(dataInputStream);
                DataInputStream dataInputStream2 = new DataInputStream(inflaterInputStream);
                LLSDNode lLSDNodeFromBinary2 = LLSDNode.fromBinary(dataInputStream2);
                int count = lLSDNodeFromBinary2.getCount();
                this.faces = new MeshFace[count];
                for (int i = 0; i < count; i++) {
                    this.faces[i] = new MeshFace(lLSDNodeFromBinary2.byIndex(i));
                }
                if (lLSDNodeFromBinary.keyExists("skin")) {
                    fileInputStream.getChannel().position(((long) lLSDNodeFromBinary.byKey("skin").byKey("offset").asInt()) + jPosition);
                    InflaterInputStream inflaterInputStream2 = new InflaterInputStream(dataInputStream);
                    DataInputStream dataInputStream3 = new DataInputStream(inflaterInputStream2);
                    LLSDNode lLSDNodeFromBinary3 = LLSDNode.fromBinary(dataInputStream3);
                    dataInputStream3.close();
                    inflaterInputStream2.close();
                    if (lLSDNodeFromBinary3.keyExists("bind_shape_matrix")) {
                        fArr = new float[16];
                        for (int i2 = 0; i2 < 16; i2++) {
                            fArr[i2] = (float) lLSDNodeFromBinary3.byKey("bind_shape_matrix").byIndex(i2).asDouble();
                        }
                    } else {
                        fArr = null;
                    }
                    if (lLSDNodeFromBinary3.keyExists("joint_names")) {
                        LLSDNode lLSDNodeByKey2 = lLSDNodeFromBinary3.byKey("joint_names");
                        int iMin = Math.min(lLSDNodeByKey2.getCount(), MAX_RIGGED_MESH_JOINTS);
                        int[] iArr2 = new int[iMin];
                        for (int i3 = 0; i3 < iMin; i3++) {
                            String strAsString = lLSDNodeByKey2.byIndex(i3).asString();
                            SLSkeletonBoneID sLSkeletonBoneID = SLSkeletonBoneID.bones.get(strAsString);
                            int iOrdinal3 = sLSkeletonBoneID != null ? sLSkeletonBoneID.ordinal() : -1;
                            if ((sLSkeletonBoneID == null || iOrdinal3 == -1) && (sLAttachmentPoint = SLAttachmentPoint.pointsByName.get(strAsString)) != null) {
                                sLSkeletonBoneID = sLAttachmentPoint.bone;
                                length = sLAttachmentPoint.nonHUDindex + SLSkeletonBoneID.VALUES.length;
                            } else {
                                length = iOrdinal3;
                            }
                            if (sLSkeletonBoneID != null && sLSkeletonBoneID.isExtended) {
                                z = true;
                            }
                            iArr2[i3] = length;
                        }
                        iArr = iArr2;
                    } else {
                        iArr = null;
                    }
                    if (!lLSDNodeFromBinary3.keyExists("inverse_bind_matrix") || iArr == null) {
                        fArr2 = null;
                    } else {
                        LLSDNode lLSDNodeByKey3 = lLSDNodeFromBinary3.byKey("inverse_bind_matrix");
                        fArr2 = new float[iArr.length * 16];
                        for (int i4 = 0; i4 < lLSDNodeByKey3.getCount(); i4++) {
                            if (i4 < iArr.length) {
                                LLSDNode lLSDNodeByIndex = lLSDNodeByKey3.byIndex(i4);
                                for (int i5 = 0; i5 < 16; i5++) {
                                    fArr2[(i4 * 16) + i5] = (float) lLSDNodeByIndex.byIndex(i5).asDouble();
                                }
                            }
                        }
                        Debug.Printf("inverseBindMatrix count %d", Integer.valueOf(lLSDNodeByKey3.getCount()));
                    }
                    if (lLSDNodeFromBinary3.keyExists("alt_inverse_bind_matrix")) {
                        LLSDNode lLSDNodeByKey4 = lLSDNodeFromBinary3.byKey("alt_inverse_bind_matrix");
                        float[] fArr3 = new float[lLSDNodeByKey4.getCount() * 16];
                        for (int i6 = 0; i6 < lLSDNodeByKey4.getCount(); i6++) {
                            LLSDNode lLSDNodeByIndex2 = lLSDNodeByKey4.byIndex(i6);
                            for (int i7 = 0; i7 < 16; i7++) {
                                fArr3[(i6 * 16) + i7] = (float) lLSDNodeByIndex2.byIndex(i7).asDouble();
                            }
                        }
                        if (iArr != null) {
                            enumMap = new EnumMap(SLSkeletonBoneID.class);
                            for (int i8 = 0; i8 < iArr.length; i8++) {
                                int i9 = iArr[i8];
                                if (i9 >= 0 && i9 < SLSkeletonBoneID.VALUES.length) {
                                    SLSkeletonBoneID sLSkeletonBoneID2 = SLSkeletonBoneID.VALUES[i9];
                                    float[] fArr4 = new float[3];
                                    for (int i10 = 0; i10 < 3; i10++) {
                                        fArr4[i10] = fArr3[(i8 * 16) + 12 + i10];
                                    }
                                    enumMap.put(sLSkeletonBoneID2, fArr4);
                                }
                            }
                        }
                        Debug.Printf("alt_inverse_bind_matrix count %d", Integer.valueOf(lLSDNodeByKey4.getCount()));
                    }
                    if (lLSDNodeFromBinary3.keyExists("pelvis_offset")) {
                        fAsDouble = (float) lLSDNodeFromBinary3.byKey("pelvis_offset").asDouble();
                        Debug.Printf("Pelvis offset: %f", Float.valueOf(fAsDouble));
                    }
                    dataInputStream2.close();
                    inflaterInputStream.close();
                } else {
                    fArr = null;
                    iArr = null;
                    fArr2 = null;
                }
                if (iArr == null || fArr == null || fArr2 == null) {
                    this.riggingData = null;
                    this.bindShapeMatrix = null;
                    this.jointTranslations = null;
                } else {
                    this.riggingData = MeshRiggingData.create(iArr, fArr2, z);
                    this.bindShapeMatrix = fArr;
                    this.jointTranslations = enumMap != null ? Maps.immutableEnumMap(enumMap) : null;
                }
                this.pelvisOffset = fAsDouble;
            } finally {
                dataInputStream.close();
                fileInputStream.close();
            }
        } catch (LLSDException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    private MeshWeightsBuffer makeInfluenceBuffers() {
        int numVertices = 0;
        for (MeshFace meshFace : this.faces) {
            if (meshFace != null) {
                numVertices += meshFace.getNumVertices();
            }
        }
        MeshWeightsBuffer meshWeightsBuffer = new MeshWeightsBuffer(numVertices);
        int numVertices2 = 0;
        for (MeshFace meshFace2 : this.faces) {
            if (meshFace2 != null) {
                meshFace2.PrepareInfluenceBuffer(meshWeightsBuffer, numVertices2);
                numVertices2 += meshFace2.getNumVertices();
            }
        }
        return meshWeightsBuffer;
    }

    public void ApplyJointTranslations(MeshJointTranslations meshJointTranslations) {
        meshJointTranslations.pelvisOffset += this.pelvisOffset;
        if (this.jointTranslations != null) {
            EnumMap<SLSkeletonBoneID, float[]> enumMap = meshJointTranslations.jointTranslations;
            for (Map.Entry<SLSkeletonBoneID, float[]> entry : this.jointTranslations.entrySet()) {
                enumMap.put(entry.getKey(), entry.getValue());
            }
        }
    }

    public void PrepareInfluenceBuffers(RenderContext renderContext) {
        if (this.riggingData != null) {
            if (this.glJointIndexBuffer == null || this.glWeightsBuffer == null) {
                if (this.weightsBuffer == null) {
                    this.weightsBuffer = makeInfluenceBuffers();
                }
                if (this.glJointIndexBuffer == null) {
                    this.glJointIndexBuffer = new GLLoadableBuffer(this.weightsBuffer.jointIndexBuffer);
                }
                if (this.glWeightsBuffer == null) {
                    this.glWeightsBuffer = new GLLoadableBuffer(this.weightsBuffer.weightsBuffer);
                }
            }
            this.riggingData.PrepareInfluenceBuffers(renderContext, this.bindShapeMatrix);
        }
    }

    public void PrepareInfluencesForFace(RenderContext renderContext, int i) {
        if (this.glJointIndexBuffer != null) {
            this.glJointIndexBuffer.Bind20(renderContext, renderContext.riggedMeshProgram.vJoint, 4, 5121, 4, i * 4);
        }
        if (this.glWeightsBuffer != null) {
            this.glWeightsBuffer.Bind20(renderContext, renderContext.riggedMeshProgram.vWeight, 4, 5126, 16, i * 4 * 4);
        }
    }

    public void SetupBuffers30(RenderContext renderContext) {
        renderContext.bindRiggingMeshData(this.riggingData);
        GLES20.glUniformMatrix4fv(renderContext.currentRiggedMeshProgram.uBindShapeMatrix, 1, false, this.bindShapeMatrix, 0);
    }

    public void SetupFace30(RenderContext renderContext, int i) {
        if (this.glJointIndexBuffer == null || this.glWeightsBuffer == null) {
            if (this.weightsBuffer == null) {
                this.weightsBuffer = makeInfluenceBuffers();
            }
            if (this.glJointIndexBuffer == null) {
                this.glJointIndexBuffer = new GLLoadableBuffer(this.weightsBuffer.jointIndexBuffer);
            }
            if (this.glWeightsBuffer == null) {
                this.glWeightsBuffer = new GLLoadableBuffer(this.weightsBuffer.weightsBuffer);
            }
        }
        this.glJointIndexBuffer.Bind30Integer(renderContext, renderContext.currentRiggedMeshProgram.vJoint, 4, 5121, 0, i * 4);
        this.glWeightsBuffer.Bind20(renderContext, renderContext.currentRiggedMeshProgram.vWeight, 4, 5126, 16, i * 4 * 4);
    }

    public void UpdateRigged(int i, DirectByteBuffer directByteBuffer, int i2) {
        if (this.riggingData != null) {
            this.riggingData.UpdateRigged(this.faces[i], this.bindShapeMatrix, directByteBuffer, i2);
        }
    }

    public void UpdateRiggedMatrices(AvatarSkeleton avatarSkeleton) {
        if (this.riggingData != null) {
            this.riggingData.UpdateRiggedMatrices(avatarSkeleton);
        }
    }

    public final MeshFace getFace(int i) {
        return this.faces[i];
    }

    public final int getFaceCount() {
        return this.faces.length;
    }

    public final boolean hasExtendedBones() {
        if (this.riggingData != null) {
            return this.riggingData.hasExtendedBones();
        }
        return false;
    }

    public final boolean isRiggedMesh() {
        return this.riggingData != null;
    }

    public final boolean riggingFitsGL20() {
        if (this.riggingData != null) {
            return this.riggingData.fitsGL20();
        }
        return false;
    }
}
