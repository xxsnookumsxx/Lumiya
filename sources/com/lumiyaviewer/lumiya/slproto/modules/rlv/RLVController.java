package com.lumiyaviewer.lumiya.slproto.modules.rlv;

import android.database.Cursor;
import com.google.common.collect.Table;
import com.lumiyaviewer.lumiya.Debug;
import com.lumiyaviewer.lumiya.GlobalOptions;
import com.lumiyaviewer.lumiya.orm.InventoryDB;
import com.lumiyaviewer.lumiya.slproto.SLAgentCircuit;
import com.lumiyaviewer.lumiya.slproto.SLMessage;
import com.lumiyaviewer.lumiya.slproto.SLParcelInfo;
import com.lumiyaviewer.lumiya.slproto.assets.SLWearable;
import com.lumiyaviewer.lumiya.slproto.assets.SLWearableType;
import com.lumiyaviewer.lumiya.slproto.avatar.SLAttachmentPoint;
import com.lumiyaviewer.lumiya.slproto.chat.SLEnableRLVOfferEvent;
import com.lumiyaviewer.lumiya.slproto.inventory.SLAssetType;
import com.lumiyaviewer.lumiya.slproto.inventory.SLInventoryEntry;
import com.lumiyaviewer.lumiya.slproto.inventory.SLInventoryType;
import com.lumiyaviewer.lumiya.slproto.messages.ChatFromSimulator;
import com.lumiyaviewer.lumiya.slproto.messages.ChatFromViewer;
import com.lumiyaviewer.lumiya.slproto.messages.ImprovedInstantMessage;
import com.lumiyaviewer.lumiya.slproto.modules.SLAvatarAppearance;
import com.lumiyaviewer.lumiya.slproto.modules.SLModule;
import com.lumiyaviewer.lumiya.slproto.modules.SLModules;
import com.lumiyaviewer.lumiya.slproto.modules.groups.AvatarGroupList;
import com.lumiyaviewer.lumiya.slproto.modules.rlv.commands.RLVCmdVersion;
import com.lumiyaviewer.lumiya.slproto.objects.SLObjectAvatarInfo;
import com.lumiyaviewer.lumiya.slproto.objects.SLObjectInfo;
import com.lumiyaviewer.lumiya.slproto.types.LLVector3;
import com.lumiyaviewer.lumiya.slproto.users.manager.UserManager;
import com.lumiyaviewer.lumiya.utils.UUIDPool;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;

/* loaded from: classes.dex */
public class RLVController extends SLModule {
    private boolean RLVEnabled;
    private String RLVEnablingCommand;
    private boolean RLVEnablingOffered;
    private UUID RLVEnablingUUID;
    private LLVector3 lastStandingPosition;
    private RLVRestrictions restrictions;

    public RLVController(SLAgentCircuit sLAgentCircuit) {
        super(sLAgentCircuit);
        this.RLVEnabled = false;
        this.RLVEnablingOffered = false;
        this.RLVEnablingCommand = null;
        this.RLVEnablingUUID = null;
        this.lastStandingPosition = null;
        this.restrictions = new RLVRestrictions();
        this.RLVEnabled = GlobalOptions.getInstance().getRLVEnabled();
    }

    private void handleRLVCommand(UUID uuid, String str) {
        if (str == null) {
            return;
        }
        str = str.trim();
        if (str.isEmpty()) {
            return;
        }
        Debug.Printf("RLV command: '%s'", str);
        String strSubstring = "";
        String strSubstring2 = "";
        int iIndexOf = str.indexOf(61);
        if (iIndexOf >= 0) {
            strSubstring = str.substring(iIndexOf + 1).trim().toLowerCase();
            str = str.substring(0, iIndexOf).trim();
        }
        int iIndexOf2 = str.indexOf(58);
        if (iIndexOf2 >= 0) {
            strSubstring2 = str.substring(iIndexOf2 + 1).trim();
            str = str.substring(0, iIndexOf2).trim();
        }
        handleRLVCommandParsed(uuid, str.toLowerCase(), strSubstring, strSubstring2);
    }

    private void handleRLVCommandParsed(UUID uuid, String str, String str2, String str3) {
        RLVCommand handler;
        Debug.Printf("RLV command: '%s' param '%s' option '%s'", str, str2, str3);
        RLVCommands command = RLVCommands.getCommand(str);
        if (command == null || (handler = command.getHandler()) == null) {
            return;
        }
        handler.Handle(this, uuid, command, str2, str3);
    }

    private void handleRLVCommands(UUID uuid, String str) {
        for (String str2 : str.split(",")) {
            handleRLVCommand(uuid, str2);
        }
    }

    private void offerRLVEnable(ChatFromSimulator chatFromSimulator) {
        this.agentCircuit.HandleChatEvent(this.agentCircuit.getLocalChatterID(), new SLEnableRLVOfferEvent(chatFromSimulator, this.agentCircuit.getAgentUUID()), true);
    }

    @Override // com.lumiyaviewer.lumiya.slproto.modules.SLModule
    public void HandleGlobalOptionsChange() {
        boolean rLVEnabled = GlobalOptions.getInstance().getRLVEnabled();
        if (rLVEnabled && (!this.RLVEnabled) && this.RLVEnablingOffered && this.RLVEnablingCommand != null) {
            this.RLVEnablingOffered = false;
            Debug.Printf("Enabling accepted, original command: '%s'", this.RLVEnablingCommand);
            handleRLVCommands(this.RLVEnablingUUID, this.RLVEnablingCommand);
        }
        this.RLVEnabled = rLVEnabled;
    }

    public boolean autoAcceptTeleport(UUID uuid) {
        return this.RLVEnabled && this.restrictions.isAllowed(RLVRestrictionType.accepttp, uuid.toString(), null);
    }

    public boolean canDetachItem(int i, UUID uuid) {
        SLAttachmentPoint sLAttachmentPoint;
        String str = null;
        if (!this.RLVEnabled) {
            return true;
        }
        if (i >= 0 && i < SLAttachmentPoint.NUM_ATTACHMENT_POINTS && (sLAttachmentPoint = SLAttachmentPoint.attachmentPoints[i]) != null) {
            str = sLAttachmentPoint.name;
        }
        return str == null || this.restrictions.isAllowed(RLVRestrictionType.detach, str, uuid);
    }

    public boolean canEditObjects(@Nullable UUID uuid) {
        if (!this.RLVEnabled) {
            return true;
        }
        return this.restrictions.isAllowed(RLVRestrictionType.edit, uuid != null ? uuid.toString() : "", null);
    }

    public boolean canRecvChat(String str, UUID uuid) {
        return !this.RLVEnabled || str.startsWith("/") || this.restrictions.isAllowed(RLVRestrictionType.recvchat, uuid.toString(), null);
    }

    public boolean canRecvIM(UUID uuid) {
        return !this.RLVEnabled || this.restrictions.isAllowed(RLVRestrictionType.recvim, uuid.toString(), null);
    }

    public boolean canRezObjects() {
        return !this.RLVEnabled || this.restrictions.isAllowed(RLVRestrictionType.rez, "", null);
    }

    public boolean canSendIM(UUID uuid) {
        return !this.RLVEnabled || this.restrictions.isAllowed(RLVRestrictionType.sendim, uuid.toString(), null);
    }

    public boolean canSetGroup() {
        return !this.RLVEnabled || this.restrictions.isAllowed(RLVRestrictionType.setgroup, "", null);
    }

    public boolean canShowInventory() {
        return !this.RLVEnabled || this.restrictions.isAllowed(RLVRestrictionType.showinv, "", null);
    }

    public boolean canShowNames(@Nullable UUID uuid) {
        if (!this.RLVEnabled) {
            return true;
        }
        String target = uuid != null ? uuid.toString() : "";
        return this.restrictions.isAllowed(RLVRestrictionType.shownames, target, null) && this.restrictions.isAllowed(RLVRestrictionType.shownametags, target, null);
    }

    public boolean canSit() {
        return !this.RLVEnabled || this.restrictions.isAllowed(RLVRestrictionType.sit, "", null);
    }

    public boolean canStandUp() {
        return !this.RLVEnabled || this.restrictions.isAllowed(RLVRestrictionType.unsit, "", null);
    }

    public boolean canTakeItemOff(SLWearableType sLWearableType) {
        return !this.RLVEnabled || this.restrictions.isAllowed(RLVRestrictionType.remoutfit, sLWearableType.getName(), null);
    }

    public boolean canTeleportBySitting() {
        return !this.RLVEnabled || this.restrictions.isAllowed(RLVRestrictionType.sittp, "", null);
    }

    public boolean canTeleportToLandmark() {
        return !this.RLVEnabled || this.restrictions.isAllowed(RLVRestrictionType.tplm, "", null);
    }

    public boolean canTeleportToLocation() {
        return !this.RLVEnabled || this.restrictions.isAllowed(RLVRestrictionType.tploc, "", null);
    }

    public boolean canTeleportToLure(UUID uuid) {
        return !this.RLVEnabled || this.restrictions.isAllowed(RLVRestrictionType.tplure, uuid.toString(), null);
    }

    public boolean canTouchObject(@Nullable SLObjectInfo sLObjectInfo) {
        if (!this.RLVEnabled || sLObjectInfo == null) {
            return true;
        }
        if (!this.restrictions.isAllowed(RLVRestrictionType.touchall, "", null)) {
            return false;
        }
        UUID objId = sLObjectInfo.getId();
        SLObjectInfo parentObj = sLObjectInfo.getParentObject();
        UUID rootId = (parentObj != null && parentObj.getId() != null) ? parentObj.getId() : objId;
        if (objId != null && !this.restrictions.isAllowed(RLVRestrictionType.touchthis, objId.toString(), null)) {
            return false;
        }
        if (rootId != null && !rootId.equals(objId) && !this.restrictions.isAllowed(RLVRestrictionType.touchthis, rootId.toString(), null)) {
            return false;
        }
        boolean isAttachment = sLObjectInfo.isMyAttachment() || (parentObj != null && parentObj.isMyAttachment());
        if (isAttachment) {
            return this.restrictions.isAllowed(RLVRestrictionType.touchme, "", null);
        }
        String objTarget = objId != null ? objId.toString() : "";
        String rootTarget = rootId != null ? rootId.toString() : "";
        if (!this.restrictions.isAllowed(RLVRestrictionType.touchworld, objTarget, null) && (rootTarget.isEmpty() || !this.restrictions.isAllowed(RLVRestrictionType.touchworld, rootTarget, null))) {
            return false;
        }
        if (!this.restrictions.isAllowed(RLVRestrictionType.fartouch, "", null)) {
            LLVector3 agentPos = getModules().avatarControl.getAgentPosition().getPosition();
            LLVector3 objPos = sLObjectInfo.getAbsolutePosition();
            if (agentPos != null && objPos != null) {
                float dx = agentPos.x - objPos.x;
                float dy = agentPos.y - objPos.y;
                float dz = agentPos.z - objPos.z;
                if ((dx * dx) + (dy * dy) + (dz * dz) > 2.25f) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean canViewNotecard() {
        return !this.RLVEnabled || this.restrictions.isAllowed(RLVRestrictionType.viewnote, "", null);
    }

    public boolean canWearItem(SLWearableType sLWearableType) {
        return !this.RLVEnabled || this.restrictions.isAllowed(RLVRestrictionType.addoutfit, sLWearableType.getName(), null);
    }

    public void recordStandingPosition() {
        SLParcelInfo parcelInfo = this.agentCircuit.getGridConnection() != null ? this.agentCircuit.getGridConnection().parcelInfo : null;
        SLObjectAvatarInfo agentAvatar = parcelInfo != null ? parcelInfo.getAgentAvatar() : null;
        if (agentAvatar == null || agentAvatar.getParentObject() == null) {
            LLVector3 pos = getModules().avatarControl.getAgentPosition().getPosition();
            if (pos != null) {
                this.lastStandingPosition = new LLVector3(pos);
            }
        }
    }

    public void handleStandTp(@Nullable UUID uuid) {
        if (this.RLVEnabled && this.lastStandingPosition != null && !this.restrictions.isAllowed(RLVRestrictionType.standtp, "", null)) {
            this.agentCircuit.TeleportToLocalPosition(this.lastStandingPosition);
        }
    }

    public String getActiveGroupName() {
        UserManager userManager = UserManager.getUserManager(this.agentCircuit.getAgentUUID());
        UUID activeGroupID = getModules().groupManager != null ? getModules().groupManager.getActiveGroupID() : null;
        if (activeGroupID == null || UUIDPool.ZeroUUID.equals(activeGroupID) || userManager == null) {
            return "none";
        }
        AvatarGroupList groupList = userManager.getChatterList().getGroupManager().getAvatarGroupList();
        if (groupList != null && groupList.Groups != null) {
            AvatarGroupList.AvatarGroupEntry entry = groupList.Groups.get(activeGroupID);
            if (entry != null && entry.GroupName != null && !entry.GroupName.isEmpty()) {
                return entry.GroupName;
            }
        }
        return "none";
    }

    public void forceActivateGroup(String str) {
        if (getModules().groupManager == null) {
            return;
        }
        if (str == null || str.isEmpty() || "none".equalsIgnoreCase(str)) {
            getModules().groupManager.ForceActivateGroup(UUIDPool.ZeroUUID);
            return;
        }
        try {
            UUID groupUUID = UUID.fromString(str);
            getModules().groupManager.ForceActivateGroup(groupUUID);
            return;
        } catch (IllegalArgumentException e) {
        }
        UserManager userManager = UserManager.getUserManager(this.agentCircuit.getAgentUUID());
        if (userManager != null) {
            AvatarGroupList groupList = userManager.getChatterList().getGroupManager().getAvatarGroupList();
            if (groupList != null && groupList.Groups != null) {
                for (AvatarGroupList.AvatarGroupEntry entry : groupList.Groups.values()) {
                    if (entry != null && entry.GroupName != null && entry.GroupName.equalsIgnoreCase(str)) {
                        getModules().groupManager.ForceActivateGroup(entry.GroupID);
                        return;
                    }
                }
            }
        }
    }

    @Nullable
    private InventoryDB getInventoryDB() {
        UserManager userManager = UserManager.getUserManager(this.agentCircuit.getAgentUUID());
        if (userManager != null && userManager.getInventoryManager() != null) {
            return userManager.getInventoryManager().getDatabase();
        }
        return null;
    }

    private List<SLInventoryEntry> getChildEntries(@Nullable InventoryDB inventoryDB, @Nullable SLInventoryEntry folder) {
        ArrayList<SLInventoryEntry> children = new ArrayList<>();
        if (inventoryDB == null || folder == null) {
            return children;
        }
        Cursor cursor = SLInventoryEntry.query(inventoryDB.getDatabase(), "parent_id = ?", new String[]{Long.toString(folder.getId())}, "name ASC");
        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    children.add(new SLInventoryEntry(cursor));
                }
            } finally {
                cursor.close();
            }
        }
        return children;
    }

    @Nullable
    private SLInventoryEntry getRLVRootFolder(@Nullable InventoryDB inventoryDB) {
        if (inventoryDB == null || getModules().inventory == null || getModules().inventory.rootFolder == null) {
            return null;
        }
        for (SLInventoryEntry entry : getChildEntries(inventoryDB, getModules().inventory.rootFolder)) {
            if (entry.isFolder && entry.name != null && "#rlv".equalsIgnoreCase(entry.name.trim())) {
                return entry;
            }
        }
        return null;
    }

    public String normalizeRLVPath(String str) {
        if (str == null) {
            return "";
        }
        String trimmed = str.trim();
        while (trimmed.startsWith("/") || trimmed.startsWith("~")) {
            trimmed = trimmed.substring(1).trim();
        }
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1).trim();
        }
        return trimmed;
    }

    @Nullable
    private SLInventoryEntry resolveRLVFolderPath(@Nullable InventoryDB inventoryDB, String str) {
        SLInventoryEntry current = getRLVRootFolder(inventoryDB);
        if (current == null) {
            return null;
        }
        String norm = normalizeRLVPath(str);
        if (norm.isEmpty()) {
            return current;
        }
        for (String segment : norm.split("/")) {
            String seg = segment.trim();
            if (seg.isEmpty() || seg.startsWith(".")) {
                return null;
            }
            SLInventoryEntry next = null;
            for (SLInventoryEntry child : getChildEntries(inventoryDB, current)) {
                if (child.isFolder && child.name != null && !child.name.startsWith(".") && child.name.trim().equalsIgnoreCase(seg)) {
                    next = child;
                    break;
                }
            }
            if (next == null) {
                return null;
            }
            current = next;
        }
        return current;
    }

    private boolean isFolderActionAllowed(String folderPath, boolean isAttach) {
        String norm = normalizeRLVPath(folderPath).toLowerCase();
        RLVRestrictionType singleType = isAttach ? RLVRestrictionType.attachthis : RLVRestrictionType.detachthis;
        RLVRestrictionType recursiveType = isAttach ? RLVRestrictionType.attachallthis : RLVRestrictionType.detachallthis;
        if (!this.restrictions.isAllowed(singleType, norm, null) || !this.restrictions.isAllowed(recursiveType, norm, null)) {
            return false;
        }
        int slashIdx = norm.lastIndexOf('/');
        while (slashIdx > 0) {
            String ancestor = norm.substring(0, slashIdx);
            if (!this.restrictions.isAllowed(recursiveType, ancestor, null)) {
                return false;
            }
            slashIdx = ancestor.lastIndexOf('/');
        }
        return true;
    }

    private int parseAttachmentPointFromName(@Nullable String name) {
        if (name == null) {
            return 0;
        }
        int openIdx = name.lastIndexOf('(');
        int closeIdx = name.lastIndexOf(')');
        if (openIdx >= 0 && closeIdx > openIdx) {
            String ptName = name.substring(openIdx + 1, closeIdx).trim().replace('_', ' ').toLowerCase();
            for (int i = 0; i < SLAttachmentPoint.NUM_ATTACHMENT_POINTS; i++) {
                if (SLAttachmentPoint.attachmentPoints[i] != null && SLAttachmentPoint.attachmentPoints[i].name.equalsIgnoreCase(ptName)) {
                    return i;
                }
            }
        }
        return 0;
    }

    public void attachRLVFolder(String str, boolean recursive, boolean replace) {
        InventoryDB db = getInventoryDB();
        String norm = normalizeRLVPath(str);
        if (db == null || norm.isEmpty()) {
            return;
        }
        SLInventoryEntry folder = resolveRLVFolderPath(db, norm);
        if (folder != null) {
            attachRLVFolderEntry(db, folder, norm, recursive, replace);
        }
    }

    private void attachRLVFolderEntry(InventoryDB db, SLInventoryEntry folder, String path, boolean recursive, boolean replace) {
        if (!isFolderActionAllowed(path, true)) {
            return;
        }
        SLAvatarAppearance appearance = getModules().avatarAppearance;
        int folderAttachPt = parseAttachmentPointFromName(folder.name);
        for (SLInventoryEntry child : getChildEntries(db, folder)) {
            if (child.isFolder) {
                if (child.name != null && !child.name.startsWith(".")) {
                    if (recursive || child.name.startsWith("+")) {
                        String subPath = path.isEmpty() ? child.name : path + "/" + child.name;
                        attachRLVFolderEntry(db, child, subPath, recursive, replace);
                    }
                }
            } else {
                SLInventoryEntry resolved = db.resolveLink(child);
                if (resolved != null) {
                    if (resolved.assetType == SLAssetType.AT_CLOTHING.getTypeCode() || resolved.assetType == SLAssetType.AT_BODYPART.getTypeCode()) {
                        appearance.WearItem(resolved, replace);
                    } else if (resolved.assetType == SLAssetType.AT_OBJECT.getTypeCode()) {
                        int pt = parseAttachmentPointFromName(resolved.name);
                        if (pt == 0) {
                            pt = folderAttachPt;
                        }
                        appearance.AttachInventoryItem(resolved, pt, replace);
                    }
                }
            }
        }
    }

    public void detachRLVFolder(String str, boolean recursive) {
        InventoryDB db = getInventoryDB();
        String norm = normalizeRLVPath(str);
        if (db == null || norm.isEmpty()) {
            return;
        }
        SLInventoryEntry folder = resolveRLVFolderPath(db, norm);
        if (folder != null) {
            detachRLVFolderEntry(db, folder, norm, recursive);
        }
    }

    private void detachRLVFolderEntry(InventoryDB db, SLInventoryEntry folder, String path, boolean recursive) {
        if (!isFolderActionAllowed(path, false)) {
            return;
        }
        SLAvatarAppearance appearance = getModules().avatarAppearance;
        for (SLInventoryEntry child : getChildEntries(db, folder)) {
            if (child.isFolder) {
                if (recursive && child.name != null && !child.name.startsWith(".")) {
                    String subPath = path.isEmpty() ? child.name : path + "/" + child.name;
                    detachRLVFolderEntry(db, child, subPath, true);
                }
            } else {
                SLInventoryEntry resolved = db.resolveLink(child);
                if (resolved != null) {
                    if (resolved.assetType == SLAssetType.AT_CLOTHING.getTypeCode() || resolved.assetType == SLAssetType.AT_BODYPART.getTypeCode()) {
                        if (appearance.canTakeItemOff(resolved)) {
                            appearance.TakeItemOff(resolved);
                        }
                    } else if (resolved.assetType == SLAssetType.AT_OBJECT.getTypeCode()) {
                        if (appearance.canDetachItem(resolved)) {
                            appearance.DetachInventoryItem(resolved);
                        }
                    }
                }
            }
        }
    }

    public String getRLVInv(String str) {
        InventoryDB db = getInventoryDB();
        SLInventoryEntry folder = resolveRLVFolderPath(db, str);
        if (db == null || folder == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (SLInventoryEntry child : getChildEntries(db, folder)) {
            if (child.isFolder && child.name != null && !child.name.startsWith(".")) {
                if (sb.length() > 0) {
                    sb.append(',');
                }
                sb.append(child.name);
            }
        }
        return sb.toString();
    }

    private int[] computeFolderWornCounts(InventoryDB db, SLInventoryEntry folder, boolean recursive) {
        int total = 0;
        int worn = 0;
        SLAvatarAppearance appearance = getModules().avatarAppearance;
        for (SLInventoryEntry child : getChildEntries(db, folder)) {
            if (child.isFolder) {
                if (recursive && child.name != null && !child.name.startsWith(".")) {
                    int[] sub = computeFolderWornCounts(db, child, true);
                    total += sub[0];
                    worn += sub[1];
                }
            } else {
                SLInventoryEntry resolved = db.resolveLink(child);
                if (resolved != null && (resolved.assetType == SLAssetType.AT_OBJECT.getTypeCode() || resolved.assetType == SLAssetType.AT_CLOTHING.getTypeCode() || resolved.assetType == SLAssetType.AT_BODYPART.getTypeCode())) {
                    total++;
                    if (appearance.isItemWorn(resolved)) {
                        worn++;
                    }
                }
            }
        }
        return new int[]{total, worn};
    }

    private char wornDigit(int[] counts) {
        if (counts[0] == 0) {
            return '0';
        }
        if (counts[1] == 0) {
            return '1';
        }
        if (counts[1] < counts[0]) {
            return '2';
        }
        return '3';
    }

    public String getRLVInvWorn(String str) {
        InventoryDB db = getInventoryDB();
        SLInventoryEntry folder = resolveRLVFolderPath(db, str);
        if (db == null || folder == null) {
            return "|00";
        }
        StringBuilder sb = new StringBuilder();
        sb.append('|').append(wornDigit(computeFolderWornCounts(db, folder, false))).append(wornDigit(computeFolderWornCounts(db, folder, true)));
        for (SLInventoryEntry child : getChildEntries(db, folder)) {
            if (child.isFolder && child.name != null && !child.name.startsWith(".")) {
                sb.append('|').append(child.name).append(',').append(wornDigit(computeFolderWornCounts(db, child, false))).append(wornDigit(computeFolderWornCounts(db, child, true)));
            }
        }
        return sb.toString();
    }

    public String findRLVFolders(String criteria, boolean allMatches) {
        InventoryDB db = getInventoryDB();
        SLInventoryEntry root = getRLVRootFolder(db);
        if (db == null || root == null || criteria == null || criteria.trim().isEmpty()) {
            return "";
        }
        String[] tokens = criteria.toLowerCase().split("&&");
        List<String> matches = new ArrayList<>();
        searchRLVFoldersRecursive(db, root, "", tokens, matches);
        if (matches.isEmpty()) {
            return "";
        }
        if (!allMatches) {
            return matches.get(0);
        }
        StringBuilder sb = new StringBuilder();
        for (String match : matches) {
            if (sb.length() > 0) {
                sb.append(',');
            }
            sb.append(match);
        }
        return sb.toString();
    }

    private void searchRLVFoldersRecursive(InventoryDB db, SLInventoryEntry folder, String currentPath, String[] tokens, List<String> results) {
        for (SLInventoryEntry child : getChildEntries(db, folder)) {
            if (child.isFolder && child.name != null && !child.name.startsWith(".") && !child.name.startsWith("~")) {
                String childPath = currentPath.isEmpty() ? child.name : currentPath + "/" + child.name;
                String lowerPath = childPath.toLowerCase();
                boolean matchedAll = true;
                for (String token : tokens) {
                    String t = token.trim();
                    if (!t.isEmpty() && !lowerPath.contains(t)) {
                        matchedAll = false;
                        break;
                    }
                }
                if (matchedAll) {
                    results.add(childPath);
                }
                searchRLVFoldersRecursive(db, child, childPath, tokens, results);
            }
        }
    }

    public String getRLVPathForTarget(UUID sourcePrimUUID, String option, boolean allowMultiple) {
        InventoryDB db = getInventoryDB();
        SLInventoryEntry rlvRoot = getRLVRootFolder(db);
        if (db == null || rlvRoot == null) {
            return "";
        }
        Set<UUID> targetItemUUIDs = new HashSet<>();
        String opt = option != null ? option.trim().replace('_', ' ') : "";
        SLParcelInfo parcelInfo = this.agentCircuit.getGridConnection() != null ? this.agentCircuit.getGridConnection().parcelInfo : null;
        SLObjectAvatarInfo agentAvatar = parcelInfo != null ? parcelInfo.getAgentAvatar() : null;
        if (opt.isEmpty()) {
            if (agentAvatar != null && sourcePrimUUID != null) {
                try {
                    for (SLObjectInfo obj : agentAvatar.treeNode) {
                        if (!obj.isDead && obj.attachedToUUID != null) {
                            if (sourcePrimUUID.equals(obj.getId())) {
                                targetItemUUIDs.add(obj.attachedToUUID);
                            }
                        }
                    }
                    if (targetItemUUIDs.isEmpty() && parcelInfo.allObjectsNearby != null) {
                        SLObjectInfo prim = parcelInfo.allObjectsNearby.get(sourcePrimUUID);
                        if (prim != null) {
                            SLObjectInfo parent = prim.getParentObject();
                            if (parent != null && parent.attachedToUUID != null) {
                                targetItemUUIDs.add(parent.attachedToUUID);
                            }
                        }
                    }
                } catch (NoSuchElementException e) {
                    Debug.Warning(e);
                }
            }
        } else {
            boolean matchedPt = false;
            for (int i = 0; i < SLAttachmentPoint.NUM_ATTACHMENT_POINTS; i++) {
                if (SLAttachmentPoint.attachmentPoints[i] != null && SLAttachmentPoint.attachmentPoints[i].name.equalsIgnoreCase(opt)) {
                    matchedPt = true;
                    if (agentAvatar != null) {
                        try {
                            for (SLObjectInfo obj : agentAvatar.treeNode) {
                                if (!obj.isDead && obj.attachmentID == i && obj.attachedToUUID != null) {
                                    targetItemUUIDs.add(obj.attachedToUUID);
                                }
                            }
                        } catch (NoSuchElementException e2) {
                            Debug.Warning(e2);
                        }
                    }
                    break;
                }
            }
            if (!matchedPt) {
                SLWearableType wearableType = null;
                for (SLWearableType wt : SLWearableType.valuesCustom()) {
                    if (wt.getName().equalsIgnoreCase(opt)) {
                        wearableType = wt;
                        break;
                    }
                }
                if (wearableType != null) {
                    Table<SLWearableType, UUID, SLWearable> wornWearables = getModules().avatarAppearance.getWornWearables();
                    Map<UUID, SLWearable> row = wornWearables.row(wearableType);
                    if (row != null) {
                        for (SLWearable wearable : row.values()) {
                            if (wearable != null && wearable.itemID != null) {
                                targetItemUUIDs.add(wearable.itemID);
                            }
                        }
                    }
                }
            }
        }
        if (targetItemUUIDs.isEmpty()) {
            return "";
        }
        List<String> paths = new ArrayList<>();
        findItemPathsInRLVRecursive(db, rlvRoot, "", targetItemUUIDs, paths);
        if (paths.isEmpty()) {
            return "";
        }
        if (!allowMultiple) {
            return paths.get(0);
        }
        StringBuilder sb = new StringBuilder();
        for (String p : paths) {
            if (sb.length() > 0) {
                sb.append(',');
            }
            sb.append(p);
        }
        return sb.toString();
    }

    private void findItemPathsInRLVRecursive(InventoryDB db, SLInventoryEntry folder, String currentPath, Set<UUID> targetItemUUIDs, List<String> paths) {
        boolean foundInCurrent = false;
        for (SLInventoryEntry child : getChildEntries(db, folder)) {
            if (child.isFolder) {
                if (child.name != null && !child.name.startsWith(".")) {
                    String childPath = currentPath.isEmpty() ? child.name : currentPath + "/" + child.name;
                    findItemPathsInRLVRecursive(db, child, childPath, targetItemUUIDs, paths);
                }
            } else if (!foundInCurrent && !currentPath.isEmpty()) {
                if (targetItemUUIDs.contains(child.uuid) || (child.assetUUID != null && targetItemUUIDs.contains(child.assetUUID))) {
                    paths.add(currentPath);
                    foundInCurrent = true;
                }
            }
        }
    }

    public SLModules getModules() {
        return this.agentCircuit.getModules();
    }

    public RLVRestrictions getRestrictions() {
        return this.restrictions;
    }

    public boolean onIncomingChat(ChatFromSimulator chatFromSimulator) {
        if (chatFromSimulator.ChatData_Field.SourceType != 2 || chatFromSimulator.ChatData_Field.ChatType != 8) {
            return false;
        }
        String strStringFromVariableUTF = SLMessage.stringFromVariableUTF(chatFromSimulator.ChatData_Field.Message);
        if (!strStringFromVariableUTF.startsWith("@")) {
            return false;
        }
        UUID uuid = chatFromSimulator.ChatData_Field.SourceID;
        if (this.RLVEnabled) {
            handleRLVCommands(uuid, strStringFromVariableUTF.substring(1));
        } else if (!this.RLVEnablingOffered) {
            this.RLVEnablingOffered = true;
            this.RLVEnablingUUID = uuid;
            this.RLVEnablingCommand = strStringFromVariableUTF.substring(1);
            offerRLVEnable(chatFromSimulator);
        }
        return true;
    }

    public boolean onIncomingIM(ImprovedInstantMessage improvedInstantMessage) {
        if (!this.RLVEnabled) {
            return false;
        }
        int i = improvedInstantMessage.MessageBlock_Field.Dialog;
        String strStringFromVariableOEM = SLMessage.stringFromVariableOEM(improvedInstantMessage.MessageBlock_Field.FromAgentName);
        String strStringFromVariableUTF = SLMessage.stringFromVariableUTF(improvedInstantMessage.MessageBlock_Field.Message);
        Debug.Printf("IM: type %d from '%s' text '%s'", Integer.valueOf(i), strStringFromVariableOEM, strStringFromVariableUTF);
        switch (i) {
            case 0:
                if (strStringFromVariableUTF.equalsIgnoreCase("@version")) {
                    this.agentCircuit.SendInstantMessage(improvedInstantMessage.AgentData_Field.AgentID, RLVCmdVersion.getManualVersionReply());
                    return true;
                }
                return false;
            default:
                return false;
        }
    }

    public boolean onSendLocalChat(int i, String str) {
        if (!this.RLVEnabled) {
            return true;
        }
        if (i == 0) {
            boolean isEmote = str.startsWith("/me ") || str.startsWith("/me'") || str.startsWith(":");
            if (isEmote) {
                Set<String> emoteTargets = this.restrictions.getTargetsForRestriction(RLVRestrictionType.rediremote);
                if (emoteTargets != null) {
                    for (String chStr : emoteTargets) {
                        try {
                            sayOnChannel(Integer.parseInt(chStr), str);
                        } catch (NumberFormatException e) {
                            Debug.Warning(e);
                        }
                    }
                }
            } else if (!str.startsWith("/")) {
                Set<String> targetsForRestriction = this.restrictions.getTargetsForRestriction(RLVRestrictionType.redirchat);
                if (targetsForRestriction != null) {
                    Iterator<String> it = targetsForRestriction.iterator();
                    while (it.hasNext()) {
                        try {
                            int i2 = Integer.parseInt(it.next());
                            ChatFromViewer chatFromViewer = new ChatFromViewer();
                            chatFromViewer.AgentData_Field.AgentID = this.circuitInfo.agentID;
                            chatFromViewer.AgentData_Field.SessionID = this.circuitInfo.sessionID;
                            chatFromViewer.ChatData_Field.Channel = i2;
                            chatFromViewer.ChatData_Field.Type = 1;
                            chatFromViewer.ChatData_Field.Message = SLMessage.stringToVariableUTF(str);
                            chatFromViewer.isReliable = true;
                            SendMessage(chatFromViewer);
                        } catch (NumberFormatException e2) {
                            Debug.Warning(e2);
                        }
                    }
                }
                if (!this.restrictions.isAllowed(RLVRestrictionType.sendchat, "", null)) {
                    return false;
                }
            }
        } else if (!this.restrictions.isAllowed(RLVRestrictionType.sendchannel, Integer.toString(i), null)) {
            return false;
        }
        return true;
    }

    public void sayOnChannel(int i, String str) {
        Debug.Printf("RLV reply (%d): '%s'", Integer.valueOf(i), str);
        ChatFromViewer chatFromViewer = new ChatFromViewer();
        chatFromViewer.AgentData_Field.AgentID = this.circuitInfo.agentID;
        chatFromViewer.AgentData_Field.SessionID = this.circuitInfo.sessionID;
        chatFromViewer.ChatData_Field.Channel = i;
        chatFromViewer.ChatData_Field.Type = 1;
        chatFromViewer.ChatData_Field.Message = SLMessage.stringToVariableUTF(str);
        chatFromViewer.isReliable = true;
        SendMessage(chatFromViewer);
    }

    public void teleportToGlobalPos(UUID uuid, LLVector3 lLVector3) {
        if (this.RLVEnabled && this.restrictions.isAllowed(RLVRestrictionType.tploc, "", null, uuid)) {
            this.agentCircuit.TeleportToGlobalPosition(lLVector3);
        }
    }
}
