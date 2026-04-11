package com.lumiyaviewer.lumiya.slproto.inventory;

import com.lumiyaviewer.lumiya.orm.InventoryDB;
import com.lumiyaviewer.lumiya.slproto.inventory.SLInventory;
import java.util.UUID;
import javax.annotation.Nonnull;

/* JADX INFO: loaded from: classes.dex */
public abstract class SLInventoryFetchRequest {
    protected final InventoryDB db;

    @Nonnull
    protected final SLInventoryEntry folderEntry;
    protected final long folderId;
    protected final UUID folderUUID;
    protected final SLInventory inventory;

    SLInventoryFetchRequest(SLInventory sLInventory, UUID uuid) throws SLInventory.NoInventoryItemException {
        this.inventory = sLInventory;
        this.db = sLInventory.getDatabase();
        this.folderUUID = uuid;
        SLInventoryEntry sLInventoryEntryFindEntry = this.db.findEntry(uuid);
        if (sLInventoryEntryFindEntry == null) {
            throw new SLInventory.NoInventoryItemException(uuid);
        }
        this.folderEntry = sLInventoryEntryFindEntry;
        this.folderId = sLInventoryEntryFindEntry.getId();
    }

    public abstract void cancel();

    protected void completeFetch(boolean z, boolean z2) {
        this.inventory.onFetchComplete(this, this.folderUUID, this.folderId, z, z2);
    }

    public abstract void start();
}
