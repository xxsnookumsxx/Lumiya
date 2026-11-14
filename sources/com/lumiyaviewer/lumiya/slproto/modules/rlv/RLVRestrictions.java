package com.lumiyaviewer.lumiya.slproto.modules.rlv;

import com.lumiyaviewer.lumiya.Debug;
import com.lumiyaviewer.lumiya.slproto.modules.rlv.RLVRestrictionType;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/* loaded from: classes.dex */
public class RLVRestrictions {
    private Map<RLVRestrictionType, RLVRestrictionList> restrictions = new EnumMap(RLVRestrictionType.class);

    private static class RLVRestrictionList {

        /* renamed from: -com-lumiyaviewer-lumiya-slproto-modules-rlv-RLVRestrictionType$RLVRuleMatchTypeSwitchesValues, reason: not valid java name */
        private static /* synthetic */ int[] f127x829a01e5 = null;
        private Map<String, HashSet<UUID>> restMap;

        /* renamed from: -getcom-lumiyaviewer-lumiya-slproto-modules-rlv-RLVRestrictionType$RLVRuleMatchTypeSwitchesValues, reason: not valid java name */
        private static /* synthetic */ int[] m241xbc0b3b89() {
            if (f127x829a01e5 != null) {
                return f127x829a01e5;
            }
            int[] iArr = new int[RLVRestrictionType.RLVRuleMatchType.valuesCustom().length];
            try {
                iArr[RLVRestrictionType.RLVRuleMatchType.TargetNoExceptions.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[RLVRestrictionType.RLVRuleMatchType.TargetSpecifiesAllowance.ordinal()] = 4;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[RLVRestrictionType.RLVRuleMatchType.TargetSpecifiesException.ordinal()] = 2;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[RLVRestrictionType.RLVRuleMatchType.TargetSpecifiesRestriction.ordinal()] = 3;
            } catch (NoSuchFieldError e4) {
            }
            f127x829a01e5 = iArr;
            return iArr;
        }

        private RLVRestrictionList() {
            this.restMap = new HashMap();
        }

        /* synthetic */ RLVRestrictionList(RLVRestrictionList rLVRestrictionList) {
            this();
        }

        public void addRestriction(UUID uuid, String str) {
            HashSet<UUID> hashSet = this.restMap.get(str);
            if (hashSet == null) {
                hashSet = new HashSet<>();
                this.restMap.put(str, hashSet);
            }
            hashSet.add(uuid);
        }

        public Set<String> getTargets() {
            return this.restMap.keySet();
        }

        public boolean hasRestrictionsByObject(UUID uuid) {
            if (uuid == null) {
                return !this.restMap.isEmpty();
            }
            Iterator it = this.restMap.entrySet().iterator();
            while (it.hasNext()) {
                if (((HashSet) ((Map.Entry) it.next()).getValue()).contains(uuid)) {
                    return true;
                }
            }
            return false;
        }

        public boolean isAllowed(RLVRestrictionType.RLVRuleMatchType rLVRuleMatchType, String str, UUID uuid, UUID uuid2) {
            if (rLVRuleMatchType == RLVRestrictionType.RLVRuleMatchType.TargetSpecifiesAllowance) {
                if (this.restMap.containsKey("")) {
                    return true;
                }
                return !str.equals("") && this.restMap.containsKey(str);
            }
            if (this.restMap.isEmpty()) {
                return true;
            }
            switch (m241xbc0b3b89()[rLVRuleMatchType.ordinal()]) {
                case 1:
                    if (!this.restMap.isEmpty()) {
                        if (uuid2 == null) {
                            return false;
                        }
                        Iterator it = this.restMap.values().iterator();
                        while (it.hasNext()) {
                            HashSet hashSet = (HashSet) it.next();
                            if (hashSet.size() != 1 || !hashSet.contains(uuid2)) {
                                return false;
                            }
                        }
                    }
                    break;
                case 2:
                    if (this.restMap.containsKey("")) {
                        return !str.equals("") && this.restMap.containsKey(str);
                    }
                    break;
                case 3:
                    if (this.restMap.containsKey(str)) {
                        return false;
                    }
                    return (this.restMap.containsKey("") && (uuid == null || this.restMap.get("").contains(uuid))) ? false : true;
            }
            return true;
        }

        public boolean isEmpty() {
            return this.restMap.isEmpty();
        }

        public void removeAllForObject(UUID uuid) {
            HashSet hashSet = new HashSet();
            Iterator it = this.restMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                ((HashSet) entry.getValue()).remove(uuid);
                if (((HashSet) entry.getValue()).isEmpty()) {
                    hashSet.add((String) entry.getKey());
                }
            }
            Iterator it2 = hashSet.iterator();
            while (it2.hasNext()) {
                this.restMap.remove((String) it2.next());
            }
        }

        public void removeRestriction(UUID uuid, String str) {
            HashSet<UUID> hashSet = this.restMap.get(str);
            if (hashSet != null) {
                hashSet.remove(uuid);
                if (hashSet.isEmpty()) {
                    this.restMap.remove(str);
                }
            }
        }
    }

    public synchronized void addRestriction(RLVRestrictionType rLVRestrictionType, UUID uuid, String str) {
        if (str == null) {
            str = "";
        }
        Debug.Printf("RLV: adding restriction '%s' for object %s, target '%s'", rLVRestrictionType.toString(), uuid, str);
        RLVRestrictionList rLVRestrictionList = this.restrictions.get(rLVRestrictionType);
        if (rLVRestrictionList == null) {
            rLVRestrictionList = new RLVRestrictionList(null);
            this.restrictions.put(rLVRestrictionType, rLVRestrictionList);
        }
        rLVRestrictionList.addRestriction(uuid, str.toLowerCase());
    }

    public synchronized List<RLVRestrictionType> getRestrictionsByObject(UUID uuid) {
        LinkedList linkedList;
        linkedList = new LinkedList();
        Iterator it = this.restrictions.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            if (((RLVRestrictionList) entry.getValue()).hasRestrictionsByObject(uuid)) {
                linkedList.add((RLVRestrictionType) entry.getKey());
            }
        }
        return linkedList;
    }

    public synchronized Set<String> getTargetsForRestriction(RLVRestrictionType rLVRestrictionType) {
        RLVRestrictionList rLVRestrictionList = this.restrictions.get(rLVRestrictionType);
        if (rLVRestrictionList == null) {
            return null;
        }
        return rLVRestrictionList.getTargets();
    }

    public synchronized boolean isAllowed(RLVRestrictionType rLVRestrictionType, String str, UUID uuid) {
        return isAllowed(rLVRestrictionType, str, uuid, null);
    }

    public synchronized boolean isAllowed(RLVRestrictionType rLVRestrictionType, String str, UUID uuid, UUID uuid2) {
        if (str == null) {
            str = "";
        }
        RLVRestrictionList rLVRestrictionList = this.restrictions.get(rLVRestrictionType);
        if (rLVRestrictionList != null) {
            return rLVRestrictionList.isAllowed(rLVRestrictionType.getRuleMatchType(), str.toLowerCase(), uuid, uuid2);
        }
        return rLVRestrictionType.getRuleMatchType() != RLVRestrictionType.RLVRuleMatchType.TargetSpecifiesAllowance;
    }

    public synchronized void removeRestriction(RLVRestrictionType rLVRestrictionType, UUID uuid, String str) {
        if (str == null) {
            str = "";
        }
        Debug.Printf("RLV: removing restriction '%s' for object %s, target '%s'", rLVRestrictionType.toString(), uuid, str);
        RLVRestrictionList rLVRestrictionList = this.restrictions.get(rLVRestrictionType);
        if (rLVRestrictionList != null) {
            rLVRestrictionList.removeRestriction(uuid, str.toLowerCase());
            if (rLVRestrictionList.isEmpty()) {
                this.restrictions.remove(rLVRestrictionType);
            }
        }
    }

    public synchronized void removeRestrictions(UUID uuid, Set<RLVRestrictionType> set) {
        Debug.Printf("RLV: removing %d restrictions for object %s", Integer.valueOf(set.size()), uuid);
        for (RLVRestrictionType rLVRestrictionType : set) {
            RLVRestrictionList rLVRestrictionList = this.restrictions.get(rLVRestrictionType);
            if (rLVRestrictionList != null) {
                rLVRestrictionList.removeAllForObject(uuid);
                if (rLVRestrictionList.isEmpty()) {
                    this.restrictions.remove(rLVRestrictionType);
                }
            }
        }
    }
}
