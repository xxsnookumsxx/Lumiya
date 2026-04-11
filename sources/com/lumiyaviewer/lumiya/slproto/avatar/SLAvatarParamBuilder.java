package com.lumiyaviewer.lumiya.slproto.avatar;

import android.content.res.AssetManager;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.lumiyaviewer.lumiya.Debug;
import com.lumiyaviewer.lumiya.LumiyaApp;
import com.lumiyaviewer.lumiya.slproto.avatar.SLAvatarParamColor;
import com.lumiyaviewer.lumiya.slproto.avatar.SLAvatarParams;
import com.lumiyaviewer.lumiya.slproto.llsd.LLSDException;
import com.lumiyaviewer.lumiya.slproto.llsd.LLSDNode;
import com.lumiyaviewer.lumiya.slproto.types.ImmutableVector;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.Map;
import javax.annotation.Nullable;

/* JADX INFO: loaded from: classes.dex */
class SLAvatarParamBuilder {
    SLAvatarParamBuilder() {
    }

    static void buildParams(SLAvatarParams.ParamSet[] paramSetArr, Map<Integer, SLAvatarParams.ParamSet> map) {
        try {
            AssetManager assetManager = LumiyaApp.getAssetManager();
            if (assetManager != null) {
                InputStream inputStreamOpen = assetManager.open("character/avatar_params.xml", 3);
                LLSDNode xml = LLSDNode.parseXML(inputStreamOpen, "UTF-8");
                int count = xml.getCount();
                for (int i = 0; i < count; i++) {
                    LLSDNode lLSDNodeByIndex = xml.byIndex(i);
                    LLSDNode lLSDNodeByKey = lLSDNodeByIndex.byKey("params");
                    int count2 = lLSDNodeByKey.getCount();
                    ImmutableList.Builder builder = ImmutableList.builder();
                    for (int i2 = 0; i2 < count2; i2++) {
                        LLSDNode lLSDNodeByIndex2 = lLSDNodeByKey.byIndex(i2);
                        SLAvatarParamColor sLAvatarParamColor = null;
                        SLAvatarParamAlpha sLAvatarParamAlpha = null;
                        if (lLSDNodeByIndex2.keyExists("paramColor")) {
                            LLSDNode lLSDNodeByKey2 = lLSDNodeByIndex2.byKey("paramColor");
                            LLSDNode lLSDNodeByKey3 = lLSDNodeByKey2.byKey("values");
                            int[] iArr = new int[lLSDNodeByKey3.getCount()];
                            for (int i3 = 0; i3 < iArr.length; i3++) {
                                iArr[i3] = lLSDNodeByKey3.byIndex(i3).asInt();
                            }
                            sLAvatarParamColor = new SLAvatarParamColor(SLAvatarParamColor.ColorOperation.valueOf(lLSDNodeByKey2.byKey("opcode").asString()), iArr);
                        }
                        if (lLSDNodeByIndex2.keyExists("paramAlpha")) {
                            LLSDNode lLSDNodeByKey4 = lLSDNodeByIndex2.byKey("paramAlpha");
                            sLAvatarParamAlpha = new SLAvatarParamAlpha((float) lLSDNodeByKey4.byKey("domain").asDouble(), lLSDNodeByKey4.keyExists("tgaFile") ? lLSDNodeByKey4.byKey("tgaFile").asString() : null, lLSDNodeByKey4.byKey("skipIfZero").asBoolean(), lLSDNodeByKey4.byKey("multiplyBlend").asBoolean());
                        }
                        MeshIndex meshIndexValueOf = lLSDNodeByIndex2.keyExists("meshIndex") ? MeshIndex.valueOf(lLSDNodeByIndex2.byKey("meshIndex").asString()) : null;
                        ImmutableList immutableListBuild = null;
                        if (lLSDNodeByIndex2.keyExists("driven")) {
                            ImmutableList.Builder builder2 = ImmutableList.builder();
                            LLSDNode lLSDNodeByKey5 = lLSDNodeByIndex2.byKey("driven");
                            for (int i4 = 0; i4 < lLSDNodeByKey5.getCount(); i4++) {
                                LLSDNode lLSDNodeByIndex3 = lLSDNodeByKey5.byIndex(i4);
                                builder2.add(new SLAvatarParams.DrivenParam(lLSDNodeByIndex3.byKey("driven_id").asInt(), (float) lLSDNodeByIndex3.byKey("min1").asDouble(), (float) lLSDNodeByIndex3.byKey("max1").asDouble(), (float) lLSDNodeByIndex3.byKey("min2").asDouble(), (float) lLSDNodeByIndex3.byKey("max2").asDouble()));
                            }
                            immutableListBuild = builder2.build();
                        }
                        ImmutableMap immutableMapImmutableEnumMap = null;
                        if (lLSDNodeByIndex2.keyExists("skeleton")) {
                            EnumMap enumMap = new EnumMap(SLSkeletonBoneID.class);
                            LLSDNode lLSDNodeByKey6 = lLSDNodeByIndex2.byKey("skeleton");
                            for (int i5 = 0; i5 < lLSDNodeByKey6.getCount(); i5++) {
                                LLSDNode lLSDNodeByIndex4 = lLSDNodeByKey6.byIndex(i5);
                                SLSkeletonBoneID sLSkeletonBoneID = SLSkeletonBoneID.bones.get(lLSDNodeByIndex4.byKey("bone_id").asString());
                                if (sLSkeletonBoneID != null) {
                                    enumMap.put(sLSkeletonBoneID, new SLAvatarParams.SkeletonParamDefinition(vectorFromNode(lLSDNodeByIndex4, "scale"), vectorFromNode(lLSDNodeByIndex4, "offset")));
                                }
                            }
                            immutableMapImmutableEnumMap = Maps.immutableEnumMap(enumMap);
                        }
                        builder.add(new SLAvatarParams.AvatarParam(meshIndexValueOf, (float) lLSDNodeByIndex2.byKey("minValue").asDouble(), (float) lLSDNodeByIndex2.byKey("maxValue").asDouble(), (float) lLSDNodeByIndex2.byKey("defValue").asDouble(), lLSDNodeByIndex2.byKey("morph").asBoolean(), sLAvatarParamColor, sLAvatarParamAlpha, immutableListBuild, immutableMapImmutableEnumMap));
                    }
                    SLAvatarParams.ParamSet paramSet = new SLAvatarParams.ParamSet(lLSDNodeByIndex.byKey("setId").asInt(), lLSDNodeByIndex.byKey("appearanceIndex").asInt(), SLVisualParamID.valueOf(lLSDNodeByIndex.byKey("setName").asString()), builder.build());
                    map.put(Integer.valueOf(paramSet.id), paramSet);
                    if (paramSet.appearanceIndex != -1) {
                        paramSetArr[paramSet.appearanceIndex] = paramSet;
                    }
                }
                inputStreamOpen.close();
            }
        } catch (LLSDException e) {
            Debug.Warning(e);
        } catch (IOException e2) {
            Debug.Warning(e2);
        }
    }

    @Nullable
    private static ImmutableVector vectorFromNode(LLSDNode lLSDNode, String str) throws LLSDException {
        if (!lLSDNode.keyExists(str)) {
            return null;
        }
        LLSDNode lLSDNodeByKey = lLSDNode.byKey(str);
        return new ImmutableVector((float) lLSDNodeByKey.byKey("x").asDouble(), (float) lLSDNodeByKey.byKey("y").asDouble(), (float) lLSDNodeByKey.byKey("z").asDouble());
    }
}
