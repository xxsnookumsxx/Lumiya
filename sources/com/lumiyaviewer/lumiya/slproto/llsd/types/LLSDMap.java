package com.lumiyaviewer.lumiya.slproto.llsd.types;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.logging.nano.Vr;
import com.lumiyaviewer.lumiya.slproto.SLMessage;
import com.lumiyaviewer.lumiya.slproto.llsd.LLSDException;
import com.lumiyaviewer.lumiya.slproto.llsd.LLSDInvalidKeyException;
import com.lumiyaviewer.lumiya.slproto.llsd.LLSDNode;
import com.lumiyaviewer.lumiya.slproto.llsd.LLSDNodeFactory;
import com.lumiyaviewer.lumiya.slproto.llsd.LLSDSerialized;
import com.lumiyaviewer.lumiya.slproto.llsd.LLSDValueTypeException;
import com.lumiyaviewer.lumiya.slproto.llsd.LLSDXMLException;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

/* JADX INFO: loaded from: classes.dex */
public class LLSDMap extends LLSDNode {

    @Nonnull
    private final ImmutableMap<String, LLSDNode> items;

    public static class LLSDMapEntry {
        final String key;
        final LLSDNode value;

        public LLSDMapEntry(String str, LLSDNode lLSDNode) {
            this.key = str;
            this.value = lLSDNode;
        }
    }

    public LLSDMap(Map<String, LLSDNode> map) {
        this.items = ImmutableMap.copyOf((Map) map);
    }

    public LLSDMap(XmlPullParser xmlPullParser) throws XmlPullParserException, IOException, LLSDXMLException {
        HashMap map = new HashMap();
        while (xmlPullParser.nextTag() != 3) {
            xmlPullParser.require(2, null, "key");
            String strNextText = xmlPullParser.nextText();
            xmlPullParser.nextTag();
            map.put(strNextText, LLSDNodeFactory.parseNode(xmlPullParser));
        }
        this.items = ImmutableMap.copyOf((Map) map);
    }

    public LLSDMap(LLSDMapEntry... lLSDMapEntryArr) {
        HashMap map = new HashMap(lLSDMapEntryArr.length);
        for (LLSDMapEntry lLSDMapEntry : lLSDMapEntryArr) {
            map.put(lLSDMapEntry.key, lLSDMapEntry.value);
        }
        this.items = ImmutableMap.copyOf((Map) map);
    }

    @Override // com.lumiyaviewer.lumiya.slproto.llsd.LLSDNode
    public LLSDNode byKey(String str) throws LLSDInvalidKeyException {
        LLSDNode lLSDNode = this.items.get(str);
        if (lLSDNode != null) {
            return lLSDNode;
        }
        throw new LLSDInvalidKeyException("Map key not found, requested \"" + str + "\"");
    }

    public Set<Map.Entry<String, LLSDNode>> entrySet() {
        return this.items.entrySet();
    }

    @Override // com.lumiyaviewer.lumiya.slproto.llsd.LLSDNode
    public boolean keyExists(String str) {
        return this.items.containsKey(str);
    }

    @Override // com.lumiyaviewer.lumiya.slproto.llsd.LLSDNode
    public void toBinary(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeByte(Vr.VREvent.VrCore.ErrorCode.CONTROLLER_GATT_CHARACTERISTIC_NOT_FOUND);
        ImmutableSet<Map.Entry<String, LLSDNode>> immutableSetEntrySet = this.items.entrySet();
        dataOutputStream.writeInt(immutableSetEntrySet.size());
        for (Map.Entry<String, LLSDNode> entry : immutableSetEntrySet) {
            dataOutputStream.writeByte(107);
            byte[] bArrStringToVariableUTF = SLMessage.stringToVariableUTF(entry.getKey());
            dataOutputStream.writeInt(bArrStringToVariableUTF.length);
            dataOutputStream.write(bArrStringToVariableUTF);
            entry.getValue().toBinary(dataOutputStream);
        }
        dataOutputStream.writeByte(Vr.VREvent.VrCore.ErrorCode.CONTROLLER_BATTERY_READ_FAILED);
    }

    @Override // com.lumiyaviewer.lumiya.slproto.llsd.LLSDNode
    public <T> T toObject(Class<? extends T> cls) throws LLSDException {
        try {
            T tNewInstance = cls.newInstance();
            for (Field field : cls.getDeclaredFields()) {
                LLSDSerialized lLSDSerialized = (LLSDSerialized) field.getAnnotation(LLSDSerialized.class);
                if (lLSDSerialized != null) {
                    String strName = lLSDSerialized.name();
                    if (Strings.isNullOrEmpty(strName)) {
                        strName = field.getName();
                    }
                    Class<?> type = field.getType();
                    if (keyExists(strName)) {
                        LLSDNode lLSDNodeByKey = byKey(strName);
                        if (type.equals(Boolean.TYPE)) {
                            field.setBoolean(tNewInstance, lLSDNodeByKey.asBoolean());
                        } else if (type.equals(Integer.TYPE)) {
                            field.setInt(tNewInstance, lLSDNodeByKey.asInt());
                        } else if (type.equals(Double.TYPE)) {
                            field.setDouble(tNewInstance, lLSDNodeByKey.asDouble());
                        } else if (type.equals(Long.TYPE)) {
                            field.setLong(tNewInstance, lLSDNodeByKey.asLong());
                        } else if (type.equals(String.class)) {
                            field.set(tNewInstance, lLSDNodeByKey.asString());
                        } else if (type.equals(UUID.class)) {
                            field.set(tNewInstance, lLSDNodeByKey.asUUID());
                        } else if (type.equals(URI.class)) {
                            field.set(tNewInstance, lLSDNodeByKey.asURI());
                        } else if (type.equals(Date.class)) {
                            field.set(tNewInstance, lLSDNodeByKey.asDate());
                        } else if (type.equals(byte[].class)) {
                            field.set(tNewInstance, lLSDNodeByKey.asBinary());
                        } else if (type.isAssignableFrom(List.class)) {
                            Type genericType = field.getGenericType();
                            if (!(genericType instanceof ParameterizedType)) {
                                throw new LLSDValueTypeException(type.getName(), lLSDNodeByKey);
                            }
                            Type[] actualTypeArguments = ((ParameterizedType) genericType).getActualTypeArguments();
                            if (actualTypeArguments.length != 1) {
                                throw new LLSDValueTypeException(type.getName(), lLSDNodeByKey);
                            }
                            Type type2 = actualTypeArguments[0];
                            if (!(type2 instanceof Class)) {
                                throw new LLSDValueTypeException(type.getName(), lLSDNodeByKey);
                            }
                            int count = lLSDNodeByKey.getCount();
                            ArrayList arrayList = new ArrayList(count);
                            for (int i = 0; i < count; i++) {
                                arrayList.add(lLSDNodeByKey.byIndex(i).toObject((Class) type2));
                            }
                            field.set(tNewInstance, arrayList);
                        } else {
                            continue;
                        }
                    } else {
                        continue;
                    }
                }
            }
            return tNewInstance;
        } catch (IllegalAccessException e) {
            throw new LLSDException(e.getMessage());
        } catch (java.lang.InstantiationException e2) {
            throw new LLSDException(e2.getMessage());
        }
    }

    @Override // com.lumiyaviewer.lumiya.slproto.llsd.LLSDNode
    public void toXML(XmlSerializer xmlSerializer) throws IOException {
        xmlSerializer.startTag("", "map");
        for (Map.Entry<String, LLSDNode> entry : this.items.entrySet()) {
            xmlSerializer.startTag("", "key");
            xmlSerializer.text(entry.getKey());
            xmlSerializer.endTag("", "key");
            entry.getValue().toXML(xmlSerializer);
        }
        xmlSerializer.endTag("", "map");
    }
}
