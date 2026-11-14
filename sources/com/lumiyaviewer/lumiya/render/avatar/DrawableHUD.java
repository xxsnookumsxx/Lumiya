package com.lumiyaviewer.lumiya.render.avatar;

import android.opengl.GLES10;
import android.opengl.GLES20;
import android.opengl.Matrix;
import com.lumiyaviewer.lumiya.Debug;
import com.lumiyaviewer.lumiya.render.DrawableObject;
import com.lumiyaviewer.lumiya.render.DrawableStore;
import com.lumiyaviewer.lumiya.render.MatrixStack;
import com.lumiyaviewer.lumiya.render.RenderContext;
import com.lumiyaviewer.lumiya.render.TouchHUDEvent;
import com.lumiyaviewer.lumiya.render.picking.ObjectIntersectInfo;
import com.lumiyaviewer.lumiya.render.spatial.DrawEntryList;
import com.lumiyaviewer.lumiya.render.spatial.DrawListObjectEntry;
import com.lumiyaviewer.lumiya.render.spatial.DrawListPrimEntry;
import com.lumiyaviewer.lumiya.slproto.avatar.SLAttachmentPoint;
import com.lumiyaviewer.lumiya.slproto.objects.SLObjectInfo;
import com.lumiyaviewer.lumiya.slproto.types.LLVector3;
import com.lumiyaviewer.lumiya.slproto.types.Vector3Array;
import com.lumiyaviewer.lumiya.utils.LinkedTreeNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Set;

/* JADX INFO: loaded from: classes.dex */
public class DrawableHUD {
    private static final Comparator<DrawableObject> TRANSLUCENT_DEPTH_ORDER = new Comparator<DrawableObject>() {
        @Override
        public int compare(DrawableObject a, DrawableObject b) {
            float[] ma = a.getObjectInfo().worldMatrix;
            float[] mb = b.getObjectInfo().worldMatrix;
            float xa = ma != null ? ma[12] : 0.0f;
            float xb = mb != null ? mb[12] : 0.0f;
            return Float.compare(xb, xa);
        }
    };
    private final DrawableAvatar attachedTo;
    private final SLAttachmentPoint attachmentPoint;
    private final DrawableStore drawableStore;
    private final LLVector3 minPos = new LLVector3();
    private final LLVector3 maxPos = new LLVector3();
    private final Set<DrawableObject> hudObjects = Collections.newSetFromMap(new IdentityHashMap());

    public DrawableHUD(SLAttachmentPoint sLAttachmentPoint, DrawEntryList drawEntryList, SLObjectInfo sLObjectInfo, DrawableStore drawableStore, DrawableAvatar drawableAvatar) {
        this.attachmentPoint = sLAttachmentPoint;
        this.drawableStore = drawableStore;
        this.attachedTo = drawableAvatar;
        addObject(drawEntryList, sLObjectInfo, new MatrixStack(), true);
    }

    private void addObject(DrawEntryList drawEntryList, SLObjectInfo sLObjectInfo, MatrixStack matrixStack, boolean z) {
        matrixStack.glPushMatrix();
        processObjectExtents(sLObjectInfo, matrixStack, z);
        DrawListObjectEntry drawListEntry = sLObjectInfo.getDrawListEntry();
        drawEntryList.addEntry(drawListEntry);
        if (drawListEntry instanceof DrawListPrimEntry) {
            this.hudObjects.add(((DrawListPrimEntry) drawListEntry).getDrawableAttachment(this.drawableStore, this.attachedTo));
        }
        for (LinkedTreeNode<SLObjectInfo> firstChild = sLObjectInfo.treeNode.getFirstChild(); firstChild != null; firstChild = firstChild.getNextChild()) {
            SLObjectInfo dataObject = firstChild.getDataObject();
            if (dataObject != null) {
                addObject(drawEntryList, dataObject, matrixStack, false);
            }
        }
        matrixStack.glPopMatrix();
    }

    private void processObjectExtents(SLObjectInfo sLObjectInfo, MatrixStack matrixStack, boolean z) {
        Vector3Array objectCoords = sLObjectInfo.getObjectCoords();
        int elementOffset = objectCoords.getElementOffset(0);
        int elementOffset2 = objectCoords.getElementOffset(1);
        float[] data = objectCoords.getData();
        matrixStack.glTranslatef(data[elementOffset + 0], data[elementOffset + 1], data[elementOffset + 2]);
        matrixStack.glMultMatrixf(sLObjectInfo.getRotation().getInverseMatrix(), 0);
        float[] fArr = {(-data[elementOffset2 + 0]) / 2.0f, (-data[elementOffset2 + 1]) / 2.0f, (-data[elementOffset2 + 2]) / 2.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f};
        Matrix.multiplyMV(fArr, 4, matrixStack.getMatrixData(), matrixStack.getMatrixDataOffset(), fArr, 0);
        if (z) {
            this.minPos.x = fArr[4];
            this.minPos.y = fArr[5];
            this.minPos.z = fArr[6];
            this.maxPos.x = fArr[4];
            this.maxPos.y = fArr[5];
            this.maxPos.z = fArr[6];
        } else {
            this.minPos.x = Math.min(this.minPos.x, fArr[4]);
            this.minPos.y = Math.min(this.minPos.y, fArr[5]);
            this.minPos.z = Math.min(this.minPos.z, fArr[6]);
            this.maxPos.x = Math.max(this.maxPos.x, fArr[4]);
            this.maxPos.y = Math.max(this.maxPos.y, fArr[5]);
            this.maxPos.z = Math.max(this.maxPos.z, fArr[6]);
        }
        fArr[0] = data[elementOffset2 + 0] / 2.0f;
        fArr[1] = data[elementOffset2 + 1] / 2.0f;
        fArr[2] = data[elementOffset2 + 2] / 2.0f;
        fArr[3] = 1.0f;
        Matrix.multiplyMV(fArr, 4, matrixStack.getMatrixData(), matrixStack.getMatrixDataOffset(), fArr, 0);
        this.minPos.x = Math.min(this.minPos.x, fArr[4]);
        this.minPos.y = Math.min(this.minPos.y, fArr[5]);
        this.minPos.z = Math.min(this.minPos.z, fArr[6]);
        this.maxPos.x = Math.max(this.maxPos.x, fArr[4]);
        this.maxPos.y = Math.max(this.maxPos.y, fArr[5]);
        this.maxPos.z = Math.max(this.maxPos.z, fArr[6]);
    }

    public ObjectIntersectInfo Draw(RenderContext renderContext, float f, float f2, float f3, TouchHUDEvent touchHUDEvent, boolean z) {
        ObjectIntersectInfo objectIntersectInfo = null;
        renderContext.glModelPushMatrix();
        float centerX = (this.minPos.x + this.maxPos.x) / 2.0f;
        float centerY = (this.minPos.y + this.maxPos.y) / 2.0f;
        float centerZ = (this.minPos.z + this.maxPos.z) / 2.0f;
        float spanX = Math.max(0.001f, this.maxPos.x - this.minPos.x);
        float spanY = Math.max(0.001f, this.maxPos.y - this.minPos.y);
        float spanZ = Math.max(0.001f, this.maxPos.z - this.minPos.z);
        // Clamp Z span used for auto-fit so tall multi-panel HUDs fill ~90% of screen width instead of shrinking to a tiny strip
        float fitZ = Math.min(spanZ, Math.max(spanY * 2.0f, 1.25f));
        float baseScale = Math.min((2.0f * renderContext.aspectRatio * 0.90f) / spanY, 1.80f / fitZ);
        float depthScale = Math.min(1.0f, 1.80f / spanX);
        float hudScale = baseScale * f;
        renderContext.glModelTranslatef(0.0f, f2 * 4.0f, f3 * 4.0f);
        renderContext.glModelScalef(depthScale, hudScale, hudScale);
        renderContext.glModelTranslatef(-centerX, -centerY, -centerZ);
        if (z) {
            for (DrawableObject drawableObject : this.hudObjects) {
                drawableObject.DrawHoverText(renderContext, true);
            }
        } else {
            ArrayList<DrawableObject> pass2Objects = null;
            ArrayList<DrawableObject> pass4Objects = null;
            if (renderContext.hasGL20) {
                GLES20.glDisable(2884);
                GLES20.glDisable(3042);
                GLES20.glDepthMask(true);
            } else {
                GLES10.glDisable(2884);
                GLES10.glDisable(3042);
                GLES10.glDisable(3008);
                GLES10.glDepthMask(true);
            }
            for (DrawableObject drawableObject : this.hudObjects) {
                int mask = drawableObject.Draw(renderContext, 1);
                if ((mask & 2) != 0) {
                    if (pass2Objects == null) {
                        pass2Objects = new ArrayList<>();
                    }
                    pass2Objects.add(drawableObject);
                }
                if ((mask & 4) != 0) {
                    if (pass4Objects == null) {
                        pass4Objects = new ArrayList<>();
                    }
                    pass4Objects.add(drawableObject);
                }
                if (touchHUDEvent != null) {
                    ObjectIntersectInfo picked = drawableObject.PickObject(renderContext, touchHUDEvent.x, touchHUDEvent.y, Float.NEGATIVE_INFINITY);
                    if (picked != null && (objectIntersectInfo == null || picked.pickDepth < objectIntersectInfo.pickDepth)) {
                        objectIntersectInfo = picked;
                    }
                }
            }
            if (pass2Objects != null && !pass2Objects.isEmpty()) {
                if (renderContext.hasGL20) {
                    GLES20.glEnable(3042);
                } else {
                    GLES10.glEnable(3042);
                    GLES10.glEnable(3008);
                }
                for (int i = 0; i < pass2Objects.size(); i++) {
                    pass2Objects.get(i).Draw(renderContext, 2);
                }
            }
            if (pass4Objects != null && !pass4Objects.isEmpty()) {
                if (pass4Objects.size() > 1) {
                    Collections.sort(pass4Objects, TRANSLUCENT_DEPTH_ORDER);
                }
                if (renderContext.hasGL20) {
                    GLES20.glEnable(3042);
                    GLES20.glDepthMask(false);
                } else {
                    GLES10.glEnable(3042);
                    GLES10.glEnable(3008);
                    GLES10.glDepthMask(false);
                }
                for (int i = 0; i < pass4Objects.size(); i++) {
                    pass4Objects.get(i).Draw(renderContext, 4);
                }
                if (renderContext.hasGL20) {
                    GLES20.glDepthMask(true);
                } else {
                    GLES10.glDepthMask(true);
                }
            }
            if (renderContext.hasGL20) {
                GLES20.glEnable(2884);
            } else {
                GLES10.glEnable(2884);
            }
        }
        renderContext.glModelPopMatrix();
        if (touchHUDEvent != null && objectIntersectInfo != null) {
            Debug.Printf("TouchHUD event: pickDepth %f objID %d", Float.valueOf(objectIntersectInfo.pickDepth), Integer.valueOf(objectIntersectInfo.objInfo.localID));
            if (objectIntersectInfo.intersectInfo != null) {
                Debug.Printf("TouchHUD event: intersect face %d uv (%f, %f) st (%f, %f)", Integer.valueOf(objectIntersectInfo.intersectInfo.faceID), Float.valueOf(objectIntersectInfo.intersectInfo.u), Float.valueOf(objectIntersectInfo.intersectInfo.v), Float.valueOf(objectIntersectInfo.intersectInfo.s), Float.valueOf(objectIntersectInfo.intersectInfo.t));
            }
        }
        return objectIntersectInfo;
    }

    public SLAttachmentPoint getAttachmentPoint() {
        return this.attachmentPoint;
    }
}
