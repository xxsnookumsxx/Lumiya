package com.lumiyaviewer.lumiya.slproto.prims;

import com.lumiyaviewer.lumiya.slproto.types.LLVector2;
import com.lumiyaviewer.lumiya.slproto.types.LLVector4;

/* JADX INFO: loaded from: classes.dex */
public class VertexData {
    public LLVector4 Normal;
    public LLVector4 Position;
    public LLVector2 TexCoord;

    public static VertexData LerpPlanarVertex(VertexData vertexData, VertexData vertexData2, VertexData vertexData3, float f, float f2) {
        LLVector4 lLVector4Sub = LLVector4.sub(vertexData2.Position, vertexData.Position);
        lLVector4Sub.mul(f);
        LLVector4 lLVector4Sub2 = LLVector4.sub(vertexData3.Position, vertexData.Position);
        lLVector4Sub2.mul(f2);
        lLVector4Sub2.add(lLVector4Sub);
        lLVector4Sub2.add(vertexData.Position);
        VertexData vertexData4 = new VertexData();
        vertexData4.Position = lLVector4Sub2;
        if (vertexData.Normal != null) {
            vertexData4.Normal = new LLVector4(vertexData.Normal);
        }
        LLVector2 lLVector2Sub = LLVector2.sub(vertexData2.TexCoord, vertexData.TexCoord);
        lLVector2Sub.mul(f);
        LLVector2 lLVector2Sub2 = LLVector2.sub(vertexData3.TexCoord, vertexData.TexCoord);
        lLVector2Sub2.mul(f2);
        LLVector2 lLVector2 = new LLVector2(vertexData.TexCoord);
        lLVector2.add(lLVector2Sub);
        lLVector2.add(lLVector2Sub2);
        vertexData4.TexCoord = lLVector2;
        return vertexData4;
    }
}
