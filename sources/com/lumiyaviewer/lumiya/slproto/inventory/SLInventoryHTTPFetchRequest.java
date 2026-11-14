package com.lumiyaviewer.lumiya.slproto.inventory;

import android.database.sqlite.SQLiteStatement;
import android.os.Build;
import com.lumiyaviewer.lumiya.Debug;
import com.lumiyaviewer.lumiya.orm.DBObject;
import com.lumiyaviewer.lumiya.slproto.https.GenericHTTPExecutor;
import com.lumiyaviewer.lumiya.slproto.https.LLSDStreamingXMLRequest;
import com.lumiyaviewer.lumiya.slproto.inventory.SLInventory;
import com.lumiyaviewer.lumiya.slproto.llsd.LLSDNode;
import com.lumiyaviewer.lumiya.slproto.llsd.LLSDStreamingParser;
import com.lumiyaviewer.lumiya.slproto.llsd.LLSDValueTypeException;
import com.lumiyaviewer.lumiya.slproto.llsd.LLSDXMLException;
import com.lumiyaviewer.lumiya.slproto.llsd.types.LLSDArray;
import com.lumiyaviewer.lumiya.slproto.llsd.types.LLSDBoolean;
import com.lumiyaviewer.lumiya.slproto.llsd.types.LLSDMap;
import com.lumiyaviewer.lumiya.slproto.llsd.types.LLSDUUID;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/* loaded from: classes.dex */
class SLInventoryHTTPFetchRequest extends SLInventoryFetchRequest {
    private final String capURL;
    private final AtomicReference<Future<?>> futureRef;
    private final Runnable httpRequest;
    private final AtomicBoolean isCancelled;
    private final AtomicReference<LLSDStreamingXMLRequest> streamingXmlReqRef;

    private final class DatabaseCommitThread extends Thread {
        private volatile boolean aborted;
        private final BlockingQueue<SLInventoryEntry> commitEntryQueue;
        private final SLInventoryEntry stopEntry;

        private DatabaseCommitThread() {
            this.commitEntryQueue = new LinkedBlockingQueue(100);
            this.stopEntry = new SLInventoryEntry();
            this.aborted = false;
        }

        /* synthetic */ DatabaseCommitThread(SLInventoryHTTPFetchRequest sLInventoryHTTPFetchRequest, DatabaseCommitThread databaseCommitThread) {
            this();
        }

        void addEntry(SLInventoryEntry sLInventoryEntry) throws InterruptedException {
            this.commitEntryQueue.put(sLInventoryEntry);
        }

        /* JADX WARN: Removed duplicated region for block: B:32:0x0045  */
        /* JADX WARN: Removed duplicated region for block: B:38:0x006f  */
        /* JADX WARN: Removed duplicated region for block: B:40:0x0074  */
        /* JADX WARN: Removed duplicated region for block: B:42:0x0079  */
        /* JADX WARN: Removed duplicated region for block: B:48:? A[ADDED_TO_REGION, RETURN, SYNTHETIC] */
        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            boolean z = false;
            boolean z3 = false;
            SLInventoryEntry sLInventoryEntry;
            int i;
            SQLiteStatement sQLiteStatement;
            SQLiteStatement sQLiteStatement2 = null;
            HashSet hashSet = new HashSet();
            SQLiteStatement sQLiteStatement3 = null;
            boolean z2 = false;
            int i2 = 0;
            while (!Thread.interrupted()) {
                try {
                    SLInventoryEntry poll = this.commitEntryQueue.poll();
                    if (poll == null) {
                        if (z2) {
                            SLInventoryHTTPFetchRequest.this.db.setTransactionSuccessful();
                            SLInventoryHTTPFetchRequest.this.db.endTransaction();
                            z2 = false;
                            i2 = 0;
                        }
                        sLInventoryEntry = this.commitEntryQueue.take();
                        i = i2;
                    } else {
                        sLInventoryEntry = poll;
                        i = i2;
                    }
                    if (sLInventoryEntry == this.stopEntry) {
                        break;
                    }
                    if (!z2) {
                        SLInventoryHTTPFetchRequest.this.db.beginTransaction();
                        z2 = true;
                    }
                    int i3 = i + 1;
                    if (i3 >= 16) {
                        SLInventoryHTTPFetchRequest.this.db.yieldIfContendedSafely();
                        i2 = 0;
                    } else {
                        i2 = i3;
                    }
                    hashSet.add(sLInventoryEntry.uuid);
                    if (Build.VERSION.SDK_INT >= 11) {
                        if (sQLiteStatement3 == null) {
                            sQLiteStatement3 = SLInventoryEntry.getInsertStatement(SLInventoryHTTPFetchRequest.this.db.getDatabase());
                        }
                        sQLiteStatement = sQLiteStatement2 == null ? SLInventoryEntry.getUpdateStatement(SLInventoryHTTPFetchRequest.this.db.getDatabase()) : sQLiteStatement2;
                        sLInventoryEntry.updateOrInsert(sQLiteStatement, sQLiteStatement3);
                    } else {
                        sLInventoryEntry.updateOrInsert(SLInventoryHTTPFetchRequest.this.db.getDatabase());
                        sQLiteStatement = sQLiteStatement2;
                    }
                    sQLiteStatement3 = sQLiteStatement3;
                    sQLiteStatement2 = sQLiteStatement;
                } catch (DBObject.DatabaseBindingException e3) {
                    Debug.Warning(e3);
                    z3 = true;
                    break;
                } catch (InterruptedException e4) {
                    Debug.Warning(e4);
                    z3 = true;
                    break;
                }
            }
            if (!z3) {
                z = !Thread.interrupted();
            }
            if (z2) {
                Object[] objArr = new Object[2];
                objArr[0] = z ? "true" : "false";
                objArr[1] = Integer.valueOf(hashSet.size());
                Debug.Printf("InvFetch: commit thread ending transaction (success: %s, count %d).", objArr);
                SLInventoryHTTPFetchRequest.this.db.setTransactionSuccessful();
                SLInventoryHTTPFetchRequest.this.db.endTransaction();
            }
            if (sQLiteStatement3 != null) {
                sQLiteStatement3.close();
            }
            if (sQLiteStatement2 != null) {
                sQLiteStatement2.close();
            }
            if (z && !this.aborted) {
                Debug.Printf("InvFetch: commit thread successful, calling retainChildren.", new Object[0]);
                SLInventoryHTTPFetchRequest.this.db.retainChildren(SLInventoryHTTPFetchRequest.this.folderId, hashSet);
            }
        }

        void stopAndWait(boolean z) throws InterruptedException {
            if (!z) {
                this.aborted = true;
            }
            this.commitEntryQueue.put(this.stopEntry);
            join();
        }
    }

    private class FolderDataContentHandler extends LLSDStreamingParser.LLSDDefaultContentHandler {
        private final DatabaseCommitThread commitThread;
        private UUID gotUUID;
        private int gotVersion;

        private FolderDataContentHandler(DatabaseCommitThread databaseCommitThread) {
            this.commitThread = databaseCommitThread;
        }

        /* synthetic */ FolderDataContentHandler(SLInventoryHTTPFetchRequest sLInventoryHTTPFetchRequest, DatabaseCommitThread databaseCommitThread, FolderDataContentHandler folderDataContentHandler) {
            this(databaseCommitThread);
        }

        @Override // com.lumiyaviewer.lumiya.slproto.llsd.LLSDStreamingParser.LLSDDefaultContentHandler, com.lumiyaviewer.lumiya.slproto.llsd.LLSDStreamingParser.LLSDContentHandler
        public LLSDStreamingParser.LLSDContentHandler onArrayBegin(String str) throws LLSDXMLException {
            return str.equals("categories") ? new LLSDStreamingParser.LLSDDefaultContentHandler() { // from class: com.lumiyaviewer.lumiya.slproto.inventory.SLInventoryHTTPFetchRequest.FolderDataContentHandler.1
                @Override // com.lumiyaviewer.lumiya.slproto.llsd.LLSDStreamingParser.LLSDDefaultContentHandler, com.lumiyaviewer.lumiya.slproto.llsd.LLSDStreamingParser.LLSDContentHandler
                public LLSDStreamingParser.LLSDContentHandler onMapBegin(String str2) throws LLSDXMLException {
                    return SLInventoryHTTPFetchRequest.this.new FolderEntryContentHandler(FolderDataContentHandler.this.commitThread);
                }
            } : str.equals("items") ? new LLSDStreamingParser.LLSDDefaultContentHandler() { // from class: com.lumiyaviewer.lumiya.slproto.inventory.SLInventoryHTTPFetchRequest.FolderDataContentHandler.2
                @Override // com.lumiyaviewer.lumiya.slproto.llsd.LLSDStreamingParser.LLSDDefaultContentHandler, com.lumiyaviewer.lumiya.slproto.llsd.LLSDStreamingParser.LLSDContentHandler
                public LLSDStreamingParser.LLSDContentHandler onMapBegin(String str2) throws LLSDXMLException {
                    return SLInventoryHTTPFetchRequest.this.new ItemEntryContentHandler(FolderDataContentHandler.this.commitThread);
                }
            } : super.onArrayBegin(str);
        }

        @Override // com.lumiyaviewer.lumiya.slproto.llsd.LLSDStreamingParser.LLSDDefaultContentHandler, com.lumiyaviewer.lumiya.slproto.llsd.LLSDStreamingParser.LLSDContentHandler
        public void onMapEnd(String str) throws LLSDXMLException, InterruptedException {
            if (this.gotUUID == null || !this.gotUUID.equals(SLInventoryHTTPFetchRequest.this.folderUUID) || this.gotVersion == SLInventoryHTTPFetchRequest.this.folderEntry.version) {
                return;
            }
            SLInventoryHTTPFetchRequest.this.folderEntry.version = this.gotVersion;
            this.commitThread.addEntry(SLInventoryHTTPFetchRequest.this.folderEntry);
        }

        @Override // com.lumiyaviewer.lumiya.slproto.llsd.LLSDStreamingParser.LLSDDefaultContentHandler, com.lumiyaviewer.lumiya.slproto.llsd.LLSDStreamingParser.LLSDContentHandler
        public void onPrimitiveValue(String str, LLSDNode lLSDNode) throws LLSDXMLException, LLSDValueTypeException {
            Debug.Printf("InvFetch: FolderDataContentHandler: key '%s' value '%s'", str, lLSDNode);
            if (str.equals("version")) {
                this.gotVersion = lLSDNode.asInt();
            } else if (str.equals("folder_id")) {
                this.gotUUID = lLSDNode.asUUID();
            }
        }
    }

    private class FolderEntryContentHandler extends LLSDStreamingParser.LLSDDefaultContentHandler {

        /* renamed from: -com-lumiyaviewer-lumiya-slproto-inventory-SLInventoryHTTPFetchRequest$FolderValueKeySwitchesValues, reason: not valid java name */
        private /* synthetic */ int[] f111xb781ade = null;
        private final DatabaseCommitThread commitThread;
        private final SLInventoryEntry entry = new SLInventoryEntry();

        /* renamed from: -getcom-lumiyaviewer-lumiya-slproto-inventory-SLInventoryHTTPFetchRequest$FolderValueKeySwitchesValues, reason: not valid java name */
        private /* synthetic */ int[] m199x6cbd47ba() {
            if (f111xb781ade != null) {
                return f111xb781ade;
            }
            int[] iArr = new int[FolderValueKey.valuesCustom().length];
            try {
                iArr[FolderValueKey.agent_id.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[FolderValueKey.category_id.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[FolderValueKey.folder_id.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[FolderValueKey.name.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                iArr[FolderValueKey.parent_id.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                iArr[FolderValueKey.preferred_type.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                iArr[FolderValueKey.type.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                iArr[FolderValueKey.type_default.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
            try {
                iArr[FolderValueKey.version.ordinal()] = 9;
            } catch (NoSuchFieldError e9) {
            }
            f111xb781ade = iArr;
            return iArr;
        }

        FolderEntryContentHandler(DatabaseCommitThread databaseCommitThread) {
            this.commitThread = databaseCommitThread;
            this.entry.isFolder = true;
        }

        @Override // com.lumiyaviewer.lumiya.slproto.llsd.LLSDStreamingParser.LLSDDefaultContentHandler, com.lumiyaviewer.lumiya.slproto.llsd.LLSDStreamingParser.LLSDContentHandler
        public void onMapEnd(String str) throws LLSDXMLException, InterruptedException {
            if (this.entry.parentUUID == null) {
                this.entry.parentUUID = SLInventoryHTTPFetchRequest.this.folderUUID;
                this.entry.parent_id = SLInventoryHTTPFetchRequest.this.folderEntry.getId();
            }
            if (this.entry.agentUUID == null) {
                this.entry.agentUUID = SLInventoryHTTPFetchRequest.this.folderEntry.agentUUID;
            }
            this.commitThread.addEntry(this.entry);
        }

        private int parseFolderType(LLSDNode lLSDNode) throws LLSDValueTypeException {
            if (lLSDNode.isInt()) {
                return lLSDNode.asInt();
            }
            String strAsString = lLSDNode.asString();
            if ("current".equals(strAsString)) {
                return SLInventoryEntry.FT_CURRENT_OUTFIT;
            }
            if ("outfit".equals(strAsString)) {
                return SLInventoryEntry.FT_OUTFIT;
            }
            if ("my_outfits".equals(strAsString)) {
                return SLInventoryEntry.FT_MY_OUTFITS;
            }
            if ("favorite".equals(strAsString)) {
                return SLInventoryEntry.FT_FAVORITE;
            }
            if ("inbox".equals(strAsString)) {
                return SLInventoryEntry.FT_INBOX;
            }
            if ("outbox".equals(strAsString)) {
                return SLInventoryEntry.FT_OUTBOX;
            }
            if ("basic_root".equals(strAsString)) {
                return SLInventoryEntry.FT_BASIC_ROOT;
            }
            if ("lstndfnd".equals(strAsString)) {
                return SLInventoryEntry.FT_LOST_AND_FOUND;
            }
            SLAssetType byString = SLAssetType.getByString(strAsString);
            if (byString != SLAssetType.AT_UNKNOWN) {
                return byString.getSpecialFolderType() >= 0 ? byString.getSpecialFolderType() : byString.getInventoryType().getTypeCode();
            }
            return SLInventoryType.getByString(strAsString).getTypeCode();
        }

        @Override // com.lumiyaviewer.lumiya.slproto.llsd.LLSDStreamingParser.LLSDDefaultContentHandler, com.lumiyaviewer.lumiya.slproto.llsd.LLSDStreamingParser.LLSDContentHandler
        public void onPrimitiveValue(String str, LLSDNode lLSDNode) throws LLSDXMLException, LLSDValueTypeException {
            FolderValueKey byTag = FolderValueKey.byTag(str);
            if (byTag == null) {
                Debug.Printf("InvFetch: Folder unknown key '%s'", str);
                return;
            }
            switch (m199x6cbd47ba()[byTag.ordinal()]) {
                case 1:
                    this.entry.agentUUID = lLSDNode.asUUID();
                    break;
                case 2:
                    this.entry.uuid = lLSDNode.asUUID();
                    break;
                case 3:
                    this.entry.uuid = lLSDNode.asUUID();
                    break;
                case 4:
                    this.entry.name = lLSDNode.asString();
                    break;
                case 5:
                    this.entry.parentUUID = lLSDNode.asUUID();
                    if (!this.entry.parentUUID.equals(SLInventoryHTTPFetchRequest.this.folderUUID)) {
                        SLInventoryEntry findEntry = SLInventoryHTTPFetchRequest.this.db.findEntry(this.entry.parentUUID);
                        if (findEntry == null) {
                            this.entry.parent_id = 0L;
                            break;
                        } else {
                            this.entry.parent_id = findEntry.getId();
                            break;
                        }
                    } else {
                        this.entry.parent_id = SLInventoryHTTPFetchRequest.this.folderEntry.getId();
                        break;
                    }
                case 6:
                case 7:
                case 8:
                    int iParseFolderType = parseFolderType(lLSDNode);
                    if (iParseFolderType >= 0 || this.entry.typeDefault == 0) {
                        this.entry.typeDefault = iParseFolderType;
                    }
                    break;
                case 9:
                    this.entry.version = lLSDNode.asInt();
                    break;
            }
        }
    }

    private enum FolderValueKey {
        category_id,
        folder_id,
        agent_id,
        name,
        type_default,
        type,
        version,
        parent_id,
        preferred_type;

        private static final Map<String, FolderValueKey> tagMap = new HashMap(valuesCustom().length * 2);

        static {
            for (FolderValueKey folderValueKey : valuesCustom()) {
                tagMap.put(folderValueKey.toString(), folderValueKey);
            }
        }

        public static FolderValueKey byTag(String str) {
            return tagMap.get(str);
        }

        /* renamed from: values, reason: to resolve conflict with enum method */
        public static FolderValueKey[] valuesCustom() {
            return values();
        }
    }

    private class ItemEntryContentHandler extends LLSDStreamingParser.LLSDDefaultContentHandler {

        /* renamed from: -com-lumiyaviewer-lumiya-slproto-inventory-SLInventoryHTTPFetchRequest$ItemValueKeySwitchesValues, reason: not valid java name */
        private /* synthetic */ int[] f112xeca6ff43 = null;
        private final DatabaseCommitThread commitThread;
        private final LLSDStreamingParser.LLSDContentHandler permissionsHandler = new LLSDStreamingParser.LLSDDefaultContentHandler() { // from class: com.lumiyaviewer.lumiya.slproto.inventory.SLInventoryHTTPFetchRequest.ItemEntryContentHandler.1

            /* renamed from: -com-lumiyaviewer-lumiya-slproto-inventory-SLInventoryHTTPFetchRequest$PermissionsValueKeySwitchesValues, reason: not valid java name */
            private /* synthetic */ int[] f113xc291c278 = null;
            /* renamed from: -getcom-lumiyaviewer-lumiya-slproto-inventory-SLInventoryHTTPFetchRequest$PermissionsValueKeySwitchesValues, reason: not valid java name */
            private /* synthetic */ int[] m202xf1123f1c() {
                if (f113xc291c278 != null) {
                    return f113xc291c278;
                }
                int[] iArr = new int[PermissionsValueKey.valuesCustom().length];
                try {
                    iArr[PermissionsValueKey.base_mask.ordinal()] = 1;
                } catch (NoSuchFieldError e) {
                }
                try {
                    iArr[PermissionsValueKey.creator_id.ordinal()] = 2;
                } catch (NoSuchFieldError e2) {
                }
                try {
                    iArr[PermissionsValueKey.everyone_mask.ordinal()] = 3;
                } catch (NoSuchFieldError e3) {
                }
                try {
                    iArr[PermissionsValueKey.group_id.ordinal()] = 4;
                } catch (NoSuchFieldError e4) {
                }
                try {
                    iArr[PermissionsValueKey.group_mask.ordinal()] = 5;
                } catch (NoSuchFieldError e5) {
                }
                try {
                    iArr[PermissionsValueKey.is_owner_group.ordinal()] = 6;
                } catch (NoSuchFieldError e6) {
                }
                try {
                    iArr[PermissionsValueKey.last_owner_id.ordinal()] = 7;
                } catch (NoSuchFieldError e7) {
                }
                try {
                    iArr[PermissionsValueKey.next_owner_mask.ordinal()] = 8;
                } catch (NoSuchFieldError e8) {
                }
                try {
                    iArr[PermissionsValueKey.owner_id.ordinal()] = 9;
                } catch (NoSuchFieldError e9) {
                }
                try {
                    iArr[PermissionsValueKey.owner_mask.ordinal()] = 10;
                } catch (NoSuchFieldError e10) {
                }
                f113xc291c278 = iArr;
                return iArr;
            }

            @Override // com.lumiyaviewer.lumiya.slproto.llsd.LLSDStreamingParser.LLSDDefaultContentHandler, com.lumiyaviewer.lumiya.slproto.llsd.LLSDStreamingParser.LLSDContentHandler
            public void onPrimitiveValue(String str, LLSDNode lLSDNode) throws LLSDXMLException, LLSDValueTypeException {
                PermissionsValueKey byTag = PermissionsValueKey.byTag(str);
                if (byTag == null) {
                    Debug.Printf("InvFetch: Permissions unknown key '%s'", str);
                    return;
                }
                switch (m202xf1123f1c()[byTag.ordinal()]) {
                    case 1:
                        ItemEntryContentHandler.this.entry.baseMask = lLSDNode.asInt();
                        break;
                    case 2:
                        ItemEntryContentHandler.this.entry.creatorUUID = lLSDNode.asUUID();
                        break;
                    case 3:
                        ItemEntryContentHandler.this.entry.everyoneMask = lLSDNode.asInt();
                        break;
                    case 4:
                        ItemEntryContentHandler.this.entry.groupUUID = lLSDNode.asUUID();
                        break;
                    case 5:
                        ItemEntryContentHandler.this.entry.groupMask = lLSDNode.asInt();
                        break;
                    case 6:
                        ItemEntryContentHandler.this.entry.isGroupOwned = lLSDNode.asBoolean();
                        break;
                    case 7:
                        ItemEntryContentHandler.this.entry.lastOwnerUUID = lLSDNode.asUUID();
                        break;
                    case 8:
                        ItemEntryContentHandler.this.entry.nextOwnerMask = lLSDNode.asInt();
                        break;
                    case 9:
                        ItemEntryContentHandler.this.entry.ownerUUID = lLSDNode.asUUID();
                        break;
                    case 10:
                        ItemEntryContentHandler.this.entry.ownerMask = lLSDNode.asInt();
                        break;
                }
            }
        };
        private final LLSDStreamingParser.LLSDContentHandler saleInfoHandler = new LLSDStreamingParser.LLSDDefaultContentHandler() { // from class: com.lumiyaviewer.lumiya.slproto.inventory.SLInventoryHTTPFetchRequest.ItemEntryContentHandler.2
            @Override // com.lumiyaviewer.lumiya.slproto.llsd.LLSDStreamingParser.LLSDDefaultContentHandler, com.lumiyaviewer.lumiya.slproto.llsd.LLSDStreamingParser.LLSDContentHandler
            public void onPrimitiveValue(String str, LLSDNode lLSDNode) throws LLSDXMLException, LLSDValueTypeException {
                if (str.equals("sale_type")) {
                    if (lLSDNode.isString()) {
                        ItemEntryContentHandler.this.entry.saleType = SLSaleType.getByString(lLSDNode.asString()).getTypeCode();
                        return;
                    } else {
                        ItemEntryContentHandler.this.entry.saleType = lLSDNode.asInt();
                        return;
                    }
                }
                if (str.equals("sale_price")) {
                    ItemEntryContentHandler.this.entry.salePrice = lLSDNode.asInt();
                } else {
                    Debug.Printf("InvFetch: Sale info unknown key '%s'", str);
                }
            }
        };
        private final SLInventoryEntry entry = new SLInventoryEntry();

        /* renamed from: -getcom-lumiyaviewer-lumiya-slproto-inventory-SLInventoryHTTPFetchRequest$ItemValueKeySwitchesValues, reason: not valid java name */
        private /* synthetic */ int[] m201x8bc9b31f() {
            if (f112xeca6ff43 != null) {
                return f112xeca6ff43;
            }
            int[] iArr = new int[ItemValueKey.valuesCustom().length];
            try {
                iArr[ItemValueKey.agent_id.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[ItemValueKey.asset_id.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[ItemValueKey.created_at.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[ItemValueKey.desc.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                iArr[ItemValueKey.flags.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                iArr[ItemValueKey.inv_type.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                iArr[ItemValueKey.item_id.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                iArr[ItemValueKey.name.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
            try {
                iArr[ItemValueKey.parent_id.ordinal()] = 9;
            } catch (NoSuchFieldError e9) {
            }
            try {
                iArr[ItemValueKey.type.ordinal()] = 10;
            } catch (NoSuchFieldError e10) {
            }
            f112xeca6ff43 = iArr;
            return iArr;
        }

        ItemEntryContentHandler(DatabaseCommitThread databaseCommitThread) {
            this.commitThread = databaseCommitThread;
            this.entry.isFolder = false;
        }

        @Override // com.lumiyaviewer.lumiya.slproto.llsd.LLSDStreamingParser.LLSDDefaultContentHandler, com.lumiyaviewer.lumiya.slproto.llsd.LLSDStreamingParser.LLSDContentHandler
        public LLSDStreamingParser.LLSDContentHandler onMapBegin(String str) throws LLSDXMLException {
            return str.equals("permissions") ? this.permissionsHandler : str.equals("sale_info") ? this.saleInfoHandler : super.onMapBegin(str);
        }

        @Override // com.lumiyaviewer.lumiya.slproto.llsd.LLSDStreamingParser.LLSDDefaultContentHandler, com.lumiyaviewer.lumiya.slproto.llsd.LLSDStreamingParser.LLSDContentHandler
        public void onMapEnd(String str) throws LLSDXMLException, InterruptedException {
            if (this.entry.parentUUID == null) {
                this.entry.parentUUID = SLInventoryHTTPFetchRequest.this.folderUUID;
                this.entry.parent_id = SLInventoryHTTPFetchRequest.this.folderEntry.getId();
            }
            if (this.entry.agentUUID == null) {
                this.entry.agentUUID = SLInventoryHTTPFetchRequest.this.folderEntry.agentUUID;
            }
            this.commitThread.addEntry(this.entry);
        }

        @Override // com.lumiyaviewer.lumiya.slproto.llsd.LLSDStreamingParser.LLSDDefaultContentHandler, com.lumiyaviewer.lumiya.slproto.llsd.LLSDStreamingParser.LLSDContentHandler
        public void onPrimitiveValue(String str, LLSDNode lLSDNode) throws LLSDXMLException, LLSDValueTypeException {
            ItemValueKey byTag = ItemValueKey.byTag(str);
            if (byTag == null) {
                Debug.Printf("InvFetch: Item unknown key '%s'", str);
                return;
            }
            switch (m201x8bc9b31f()[byTag.ordinal()]) {
                case 1:
                    this.entry.agentUUID = lLSDNode.asUUID();
                    break;
                case 2:
                    this.entry.assetUUID = lLSDNode.asUUID();
                    break;
                case 3:
                    this.entry.creationDate = lLSDNode.asInt();
                    break;
                case 4:
                    this.entry.description = lLSDNode.asString();
                    break;
                case 5:
                    this.entry.flags = lLSDNode.asInt();
                    break;
                case 6:
                    if (!lLSDNode.isInt()) {
                        this.entry.invType = SLInventoryType.getByString(lLSDNode.asString()).getTypeCode();
                        break;
                    } else {
                        this.entry.invType = lLSDNode.asInt();
                        break;
                    }
                case 7:
                    this.entry.uuid = lLSDNode.asUUID();
                    break;
                case 8:
                    this.entry.name = lLSDNode.asString();
                    break;
                case 9:
                    this.entry.parentUUID = lLSDNode.asUUID();
                    if (!this.entry.parentUUID.equals(SLInventoryHTTPFetchRequest.this.folderUUID)) {
                        SLInventoryEntry findEntry = SLInventoryHTTPFetchRequest.this.db.findEntry(this.entry.parentUUID);
                        if (findEntry == null) {
                            this.entry.parent_id = 0L;
                            break;
                        } else {
                            this.entry.parent_id = findEntry.getId();
                            break;
                        }
                    } else {
                        this.entry.parent_id = SLInventoryHTTPFetchRequest.this.folderEntry.getId();
                        break;
                    }
                case 10:
                    if (!lLSDNode.isInt()) {
                        this.entry.assetType = SLAssetType.getByString(lLSDNode.asString()).getTypeCode();
                        break;
                    } else {
                        this.entry.assetType = lLSDNode.asInt();
                        break;
                    }
            }
        }
    }

    private enum ItemValueKey {
        item_id,
        name,
        parent_id,
        agent_id,
        type,
        inv_type,
        desc,
        flags,
        created_at,
        asset_id;

        private static final Map<String, ItemValueKey> tagMap = new HashMap(valuesCustom().length * 2);

        static {
            for (ItemValueKey itemValueKey : valuesCustom()) {
                tagMap.put(itemValueKey.toString(), itemValueKey);
            }
        }

        public static ItemValueKey byTag(String str) {
            return tagMap.get(str);
        }

        /* renamed from: values, reason: to resolve conflict with enum method */
        public static ItemValueKey[] valuesCustom() {
            return values();
        }
    }

    private enum PermissionsValueKey {
        creator_id,
        group_id,
        owner_id,
        last_owner_id,
        is_owner_group,
        base_mask,
        owner_mask,
        next_owner_mask,
        group_mask,
        everyone_mask;

        private static final Map<String, PermissionsValueKey> tagMap = new HashMap(valuesCustom().length * 2);

        static {
            for (PermissionsValueKey permissionsValueKey : valuesCustom()) {
                tagMap.put(permissionsValueKey.toString(), permissionsValueKey);
            }
        }

        public static PermissionsValueKey byTag(String str) {
            return tagMap.get(str);
        }

        /* renamed from: values, reason: to resolve conflict with enum method */
        public static PermissionsValueKey[] valuesCustom() {
            return values();
        }
    }

    private class RootContentHandler extends LLSDStreamingParser.LLSDDefaultContentHandler {
        private final DatabaseCommitThread commitThread;

        private RootContentHandler(DatabaseCommitThread databaseCommitThread) {
            this.commitThread = databaseCommitThread;
        }

        /* synthetic */ RootContentHandler(SLInventoryHTTPFetchRequest sLInventoryHTTPFetchRequest, DatabaseCommitThread databaseCommitThread, RootContentHandler rootContentHandler) {
            this(databaseCommitThread);
        }

        @Override // com.lumiyaviewer.lumiya.slproto.llsd.LLSDStreamingParser.LLSDDefaultContentHandler, com.lumiyaviewer.lumiya.slproto.llsd.LLSDStreamingParser.LLSDContentHandler
        public LLSDStreamingParser.LLSDContentHandler onMapBegin(String str) throws LLSDXMLException {
            return new LLSDStreamingParser.LLSDDefaultContentHandler() { // from class: com.lumiyaviewer.lumiya.slproto.inventory.SLInventoryHTTPFetchRequest.RootContentHandler.1
                @Override // com.lumiyaviewer.lumiya.slproto.llsd.LLSDStreamingParser.LLSDDefaultContentHandler, com.lumiyaviewer.lumiya.slproto.llsd.LLSDStreamingParser.LLSDContentHandler
                public LLSDStreamingParser.LLSDContentHandler onArrayBegin(String str2) throws LLSDXMLException {
                    return str2.equals("folders") ? new LLSDStreamingParser.LLSDDefaultContentHandler() { // from class: com.lumiyaviewer.lumiya.slproto.inventory.SLInventoryHTTPFetchRequest.RootContentHandler.1.1
                        @Override // com.lumiyaviewer.lumiya.slproto.llsd.LLSDStreamingParser.LLSDDefaultContentHandler, com.lumiyaviewer.lumiya.slproto.llsd.LLSDStreamingParser.LLSDContentHandler
                        public LLSDStreamingParser.LLSDContentHandler onMapBegin(String str3) throws LLSDXMLException {
                            return new FolderDataContentHandler(SLInventoryHTTPFetchRequest.this, RootContentHandler.this.commitThread, null);
                        }
                    } : super.onArrayBegin(str2);
                }
            };
        }
    }

    SLInventoryHTTPFetchRequest(SLInventory sLInventory, UUID uuid, String str) throws SLInventory.NoInventoryItemException {
        super(sLInventory, uuid);
        this.futureRef = new AtomicReference<>(null);
        this.streamingXmlReqRef = new AtomicReference<>(null);
        this.isCancelled = new AtomicBoolean(false);
        this.httpRequest = new Runnable() { // from class: com.lumiyaviewer.lumiya.slproto.inventory.SLInventoryHTTPFetchRequest.1
            @Override // java.lang.Runnable
            public void run() {
                boolean z = false;
                try {
                    long currentTimeMillis = System.currentTimeMillis();
                    Debug.Printf("InventoryFetcher: Going to fetch folder: %s", SLInventoryHTTPFetchRequest.this.folderUUID);
                    LLSDStreamingXMLRequest lLSDStreamingXMLRequest = new LLSDStreamingXMLRequest();
                    LLSDArray lLSDArray = new LLSDArray();
                    lLSDArray.add(new LLSDMap(new LLSDMap.LLSDMapEntry("folder_id", new LLSDUUID(SLInventoryHTTPFetchRequest.this.folderUUID)), new LLSDMap.LLSDMapEntry("fetch_folders", new LLSDBoolean(true)), new LLSDMap.LLSDMapEntry("fetch_items", new LLSDBoolean(true))));
                    LLSDMap lLSDMap = new LLSDMap(new LLSDMap.LLSDMapEntry("folders", lLSDArray));
                    SLInventoryHTTPFetchRequest.this.streamingXmlReqRef.set(lLSDStreamingXMLRequest);
                    for (int i = 0; i < 3; i++) {
                        DatabaseCommitThread databaseCommitThread = new DatabaseCommitThread(SLInventoryHTTPFetchRequest.this, null);
                        try {
                            databaseCommitThread.start();
                            Debug.Printf("InventoryFetcher: Starting HTTP request for folder: %s", SLInventoryHTTPFetchRequest.this.folderUUID);
                            lLSDStreamingXMLRequest.PerformRequest(SLInventoryHTTPFetchRequest.this.capURL, lLSDMap, new RootContentHandler(SLInventoryHTTPFetchRequest.this, databaseCommitThread, null));
                            Debug.Printf("InvFetch: done parsing,  waiting for commit thread", new Object[0]);
                            databaseCommitThread.stopAndWait(true);
                            Debug.Printf("InvFetch: commit thread finished", new Object[0]);
                            z = true;
                        } catch (LLSDXMLException e) {
                            e.printStackTrace();
                            try {
                                Debug.Log("InventoryFetcher: malformed xml after req = " + lLSDMap.serializeToXML());
                            } catch (Exception e2) {
                            }
                        } catch (IOException e3) {
                            e3.printStackTrace();
                        }
                        if (z) {
                            break;
                        }
                        databaseCommitThread.interrupt();
                        if (Thread.interrupted() || SLInventoryHTTPFetchRequest.this.isCancelled.get()) {
                            break;
                        }
                    }
                    Debug.Printf("InventoryFetcher: Fetched folder: %s (fetch time = %d)", SLInventoryHTTPFetchRequest.this.folderUUID.toString(), Long.valueOf(System.currentTimeMillis() - currentTimeMillis));
                    SLInventoryHTTPFetchRequest.this.streamingXmlReqRef.set(null);
                } catch (Exception e5) {
                    Debug.Warning(e5);
                }
                boolean z2 = Thread.interrupted() ? true : SLInventoryHTTPFetchRequest.this.isCancelled.get();
                Object[] objArr2 = new Object[3];
                objArr2[0] = SLInventoryHTTPFetchRequest.this.folderUUID.toString();
                objArr2[1] = z ? "true" : "false";
                objArr2[2] = Boolean.valueOf(z2);
                Debug.Printf("InventoryFetcher: done processing folder %s: success %s cancelled %b", objArr2);
                SLInventoryHTTPFetchRequest.this.completeFetch(z, z2);
            }
        };
        this.capURL = str;
    }

    @Override // com.lumiyaviewer.lumiya.slproto.inventory.SLInventoryFetchRequest
    public void cancel() {
        this.isCancelled.set(true);
        LLSDStreamingXMLRequest lLSDStreamingXMLRequest = this.streamingXmlReqRef.get();
        if (lLSDStreamingXMLRequest != null) {
            lLSDStreamingXMLRequest.InterruptRequest();
        }
        Future<?> andSet = this.futureRef.getAndSet(null);
        if (andSet != null) {
            andSet.cancel(true);
        }
    }

    @Override // com.lumiyaviewer.lumiya.slproto.inventory.SLInventoryFetchRequest
    public void start() {
        if (this.isCancelled.get() || this.futureRef.get() != null) {
            return;
        }
        this.futureRef.set(GenericHTTPExecutor.getInstance().submit(this.httpRequest));
    }
}
