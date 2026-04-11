package com.lumiyaviewer.lumiya.slproto.auth;

import com.google.common.collect.ImmutableList;
import com.google.vr.cardboard.VrSettingsProviderContract;
import com.lumiyaviewer.lumiya.Debug;
import com.lumiyaviewer.lumiya.utils.UUIDPool;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/* JADX INFO: loaded from: classes.dex */
public final class SLAuthReply {
    public final String agentAppearanceService;
    public final UUID agentID;
    public final int circuitCode;
    public final ImmutableList<Friend> friends;
    public final boolean fromTeleport;
    public final String gridName;
    public final UUID inventoryRoot;
    public final boolean isIndeterminate;
    public final boolean isTemporary;
    public final String loginURL;
    public final String message;
    public final String nextMethod;
    public final String nextURL;
    public final UUID secureSessionID;
    public final String seedCapability;
    public final UUID sessionID;
    public final String simAddress;
    public final int simPort;
    public final boolean success;

    public static class Friend {
        public final int rightsGiven;
        public final int rightsHas;

        @Nonnull
        public final UUID uuid;

        public Friend(@Nonnull UUID uuid, int i, int i2) {
            this.uuid = uuid;
            this.rightsGiven = i;
            this.rightsHas = i2;
        }
    }

    public SLAuthReply(SLAuthReply sLAuthReply, boolean z, boolean z2, UUID uuid, String str, int i, String str2) {
        this.gridName = sLAuthReply.gridName;
        this.loginURL = sLAuthReply.loginURL;
        this.sessionID = sLAuthReply.sessionID;
        this.secureSessionID = sLAuthReply.secureSessionID;
        this.agentID = uuid == null ? sLAuthReply.agentID : uuid;
        this.circuitCode = sLAuthReply.circuitCode;
        this.simAddress = str;
        this.simPort = i;
        this.seedCapability = str2;
        this.success = sLAuthReply.success;
        this.message = sLAuthReply.message;
        this.agentAppearanceService = sLAuthReply.agentAppearanceService;
        this.inventoryRoot = sLAuthReply.inventoryRoot;
        this.friends = sLAuthReply.friends;
        this.isIndeterminate = sLAuthReply.isIndeterminate;
        this.nextMethod = sLAuthReply.nextMethod;
        this.nextURL = sLAuthReply.nextURL;
        this.fromTeleport = z;
        this.isTemporary = z2;
    }

    public SLAuthReply(String str, String str2, @Nonnull XmlPullParser xmlPullParser) throws XmlPullParserException, IOException {
        this.gridName = str;
        this.loginURL = str2;
        boolean zEqualsIgnoreCase = false;
        String simpleValue = null;
        String simpleValue2 = null;
        UUID uuid = null;
        UUID uuid2 = null;
        UUID uuid3 = null;
        int iIntValue = 0;
        String simpleValue3 = null;
        int iIntValue2 = 0;
        String simpleValue4 = null;
        boolean zEqualsIgnoreCase2 = false;
        String simpleValue5 = "";
        String simpleValue6 = null;
        UUID inventoryRootValue = null;
        List<Friend> listOf = ImmutableList.of();
        xmlPullParser.nextTag();
        xmlPullParser.require(2, null, "methodResponse");
        xmlPullParser.nextTag();
        if (skipUntilTag(xmlPullParser, "params")) {
            if (skipUntilTag(xmlPullParser, "param")) {
                if (skipUntilTag(xmlPullParser, VrSettingsProviderContract.SETTING_VALUE_KEY)) {
                    if (skipUntilTag(xmlPullParser, "struct")) {
                        while (skipUntilTag(xmlPullParser, "member")) {
                            if (!skipUntilTag(xmlPullParser, "name")) {
                                throw new XmlPullParserException("Not found name", xmlPullParser, null);
                            }
                            String innerText = getInnerText(xmlPullParser);
                            finishTag(xmlPullParser);
                            if (skipUntilTag(xmlPullParser, VrSettingsProviderContract.SETTING_VALUE_KEY)) {
                                if (innerText.equalsIgnoreCase("session_id")) {
                                    uuid = UUIDPool.getUUID(getSimpleValue(xmlPullParser));
                                } else if (innerText.equalsIgnoreCase("secure_session_id")) {
                                    uuid2 = UUIDPool.getUUID(getSimpleValue(xmlPullParser));
                                } else if (innerText.equalsIgnoreCase("agent_id")) {
                                    uuid3 = UUIDPool.getUUID(getSimpleValue(xmlPullParser));
                                } else if (innerText.equalsIgnoreCase("circuit_code")) {
                                    iIntValue = Integer.decode(getSimpleValue(xmlPullParser)).intValue();
                                } else if (innerText.equalsIgnoreCase("sim_ip")) {
                                    simpleValue3 = getSimpleValue(xmlPullParser);
                                } else if (innerText.equalsIgnoreCase("sim_port")) {
                                    iIntValue2 = Integer.decode(getSimpleValue(xmlPullParser)).intValue();
                                } else if (innerText.equalsIgnoreCase("seed_capability")) {
                                    simpleValue4 = getSimpleValue(xmlPullParser);
                                } else if (innerText.equalsIgnoreCase("login")) {
                                    String simpleValue7 = getSimpleValue(xmlPullParser);
                                    zEqualsIgnoreCase2 = simpleValue7.equalsIgnoreCase("true");
                                    zEqualsIgnoreCase = simpleValue7.equalsIgnoreCase("indeterminate");
                                } else if (innerText.equalsIgnoreCase("next_url")) {
                                    simpleValue = getSimpleValue(xmlPullParser);
                                } else if (innerText.equalsIgnoreCase("next_method")) {
                                    simpleValue2 = getSimpleValue(xmlPullParser);
                                } else if (innerText.equalsIgnoreCase("message")) {
                                    simpleValue5 = getSimpleValue(xmlPullParser);
                                } else if (innerText.equalsIgnoreCase("agent_appearance_service")) {
                                    simpleValue6 = getSimpleValue(xmlPullParser);
                                } else if (innerText.equalsIgnoreCase("inventory-root")) {
                                    inventoryRootValue = getInventoryRootValue(xmlPullParser);
                                } else if (innerText.equalsIgnoreCase("buddy-list")) {
                                    listOf = parseBuddyList(xmlPullParser);
                                }
                                finishTag(xmlPullParser);
                            }
                            finishTag(xmlPullParser);
                        }
                        finishTag(xmlPullParser);
                    }
                    finishTag(xmlPullParser);
                }
                finishTag(xmlPullParser);
            }
            finishTag(xmlPullParser);
        }
        this.sessionID = uuid;
        this.secureSessionID = uuid2;
        this.agentID = uuid3;
        this.circuitCode = iIntValue;
        this.simAddress = simpleValue3;
        this.simPort = iIntValue2;
        this.seedCapability = simpleValue4;
        this.success = zEqualsIgnoreCase2;
        this.message = simpleValue5;
        this.agentAppearanceService = simpleValue6;
        this.inventoryRoot = inventoryRootValue;
        this.friends = ImmutableList.copyOf((Collection) listOf);
        this.fromTeleport = false;
        this.isTemporary = false;
        this.isIndeterminate = zEqualsIgnoreCase;
        this.nextURL = simpleValue;
        this.nextMethod = simpleValue2;
    }

    private void finishTag(XmlPullParser xmlPullParser) throws XmlPullParserException, IOException {
        while (xmlPullParser.getEventType() != 1) {
            if (xmlPullParser.getEventType() == 3) {
                xmlPullParser.next();
                return;
            } else if (xmlPullParser.getEventType() == 2) {
                skipTag(xmlPullParser);
            } else {
                xmlPullParser.next();
            }
        }
    }

    private String getInnerText(XmlPullParser xmlPullParser) throws XmlPullParserException, IOException {
        if (xmlPullParser.getEventType() != 4) {
            return "";
        }
        String text = xmlPullParser.getText();
        xmlPullParser.next();
        return text;
    }

    private UUID getInventoryRootValue(XmlPullParser xmlPullParser) throws XmlPullParserException, IOException {
        UUID uuidFromString = null;
        if (skipUntilTag(xmlPullParser, "array")) {
            if (skipUntilTag(xmlPullParser, "data")) {
                while (skipUntilTag(xmlPullParser, VrSettingsProviderContract.SETTING_VALUE_KEY)) {
                    if (skipUntilTag(xmlPullParser, "struct")) {
                        while (skipUntilTag(xmlPullParser, "member")) {
                            if (skipUntilTag(xmlPullParser, "name")) {
                                String innerText = getInnerText(xmlPullParser);
                                finishTag(xmlPullParser);
                                if (skipUntilTag(xmlPullParser, VrSettingsProviderContract.SETTING_VALUE_KEY)) {
                                    if (innerText.equalsIgnoreCase("folder_id")) {
                                        uuidFromString = UUID.fromString(getSimpleValue(xmlPullParser));
                                    }
                                    finishTag(xmlPullParser);
                                }
                            }
                            finishTag(xmlPullParser);
                        }
                        finishTag(xmlPullParser);
                    }
                    finishTag(xmlPullParser);
                }
                finishTag(xmlPullParser);
            }
            finishTag(xmlPullParser);
        }
        return uuidFromString;
    }

    private String getSimpleValue(XmlPullParser xmlPullParser) throws XmlPullParserException, IOException {
        while (xmlPullParser.getEventType() == 4) {
            xmlPullParser.next();
        }
        String strNextText = xmlPullParser.nextText();
        xmlPullParser.nextTag();
        Debug.Printf("got value '%s'", strNextText);
        return strNextText;
    }

    private List<Friend> parseBuddyList(XmlPullParser xmlPullParser) throws XmlPullParserException, IOException {
        LinkedList linkedList = new LinkedList();
        if (skipUntilTag(xmlPullParser, "array")) {
            if (skipUntilTag(xmlPullParser, "data")) {
                while (skipUntilTag(xmlPullParser, VrSettingsProviderContract.SETTING_VALUE_KEY)) {
                    if (skipUntilTag(xmlPullParser, "struct")) {
                        int i = 0;
                        int i2 = 0;
                        UUID uuid = null;
                        while (skipUntilTag(xmlPullParser, "member")) {
                            if (skipUntilTag(xmlPullParser, "name")) {
                                String innerText = getInnerText(xmlPullParser);
                                finishTag(xmlPullParser);
                                if (skipUntilTag(xmlPullParser, VrSettingsProviderContract.SETTING_VALUE_KEY)) {
                                    if (innerText.equalsIgnoreCase("buddy_id")) {
                                        uuid = UUIDPool.getUUID(getSimpleValue(xmlPullParser));
                                    } else if (innerText.equalsIgnoreCase("buddy_rights_given")) {
                                        i2 = Integer.parseInt(getSimpleValue(xmlPullParser));
                                    } else if (innerText.equalsIgnoreCase("buddy_rights_has")) {
                                        i = Integer.parseInt(getSimpleValue(xmlPullParser));
                                    }
                                    finishTag(xmlPullParser);
                                }
                            }
                            finishTag(xmlPullParser);
                        }
                        if (uuid != null) {
                            linkedList.add(new Friend(uuid, i2, i));
                        }
                        finishTag(xmlPullParser);
                    }
                    finishTag(xmlPullParser);
                }
                finishTag(xmlPullParser);
            }
            finishTag(xmlPullParser);
        }
        return linkedList;
    }

    private void skipTag(XmlPullParser xmlPullParser) throws XmlPullParserException, IOException {
        int i = 0;
        while (true) {
            switch (xmlPullParser.next()) {
                case 1:
                    return;
                case 2:
                    i++;
                    break;
                case 3:
                    if (i == 0) {
                        xmlPullParser.nextTag();
                        return;
                    }
                    i--;
                    break;
            }
        }
    }

    private boolean skipUntilTag(XmlPullParser xmlPullParser, String str) throws XmlPullParserException, IOException {
        while (xmlPullParser.getEventType() != 3 && xmlPullParser.getEventType() != 1) {
            if (xmlPullParser.getEventType() == 4) {
                xmlPullParser.next();
            } else {
                if (xmlPullParser.getEventType() == 2 && xmlPullParser.getName().equalsIgnoreCase(str)) {
                    xmlPullParser.next();
                    return true;
                }
                skipTag(xmlPullParser);
            }
        }
        return false;
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof SLAuthReply)) {
            return false;
        }
        SLAuthReply sLAuthReply = (SLAuthReply) obj;
        return this.simAddress.equals(sLAuthReply.simAddress) && this.simPort == sLAuthReply.simPort && this.agentID.equals(sLAuthReply.agentID) && this.sessionID.equals(sLAuthReply.sessionID) && this.circuitCode == sLAuthReply.circuitCode;
    }

    public int hashCode() {
        return this.simAddress.hashCode() + 0 + this.simPort + this.agentID.hashCode() + this.sessionID.hashCode() + this.circuitCode;
    }
}
