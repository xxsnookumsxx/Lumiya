package com.lumiyaviewer.lumiya.ui.inventory;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.BaseAdapter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Table;
import com.lumiyaviewer.lumiya.R;
import com.lumiyaviewer.lumiya.orm.InventoryDB;
import com.lumiyaviewer.lumiya.orm.InventoryEntryList;
import com.lumiyaviewer.lumiya.slproto.assets.SLWearable;
import com.lumiyaviewer.lumiya.slproto.assets.SLWearableType;
import com.lumiyaviewer.lumiya.slproto.inventory.SLInventoryEntry;
import com.lumiyaviewer.lumiya.slproto.modules.SLAvatarAppearance;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/* JADX INFO: loaded from: classes.dex */
public class InventoryFolderAdapter extends BaseAdapter implements View.OnClickListener {
    private InventoryDB database;
    private final LayoutInflater inflater;
    private final boolean wornCheckboxes;

    @Nullable
    private UUID wornOutfitFolder;

    @Nonnull
    private InventoryEntryList data = new InventoryEntryList();

    @Nullable
    private Table<SLWearableType, UUID, SLWearable> wornWearables = null;

    @Nullable
    private ImmutableMap<UUID, String> wornAttachments = null;

    @Nullable
    private OnItemCheckboxClickListener onItemCheckboxClickListener = null;

    @Nullable
    private SLAvatarAppearance avatarAppearance = null;

    public interface OnItemCheckboxClickListener {
        void onItemCheckboxClicked(SLInventoryEntry sLInventoryEntry);
    }

    public InventoryFolderAdapter(LayoutInflater layoutInflater, boolean z) {
        this.inflater = layoutInflater;
        this.wornCheckboxes = z;
    }

    private boolean isItemWorn(SLInventoryEntry sLInventoryEntry) {
        return sLInventoryEntry.whatIsItemWornOn(this.wornAttachments, this.wornWearables, false) != null;
    }

    @Override // android.widget.Adapter
    public int getCount() {
        return this.data.size();
    }

    @Override // android.widget.Adapter
    public SLInventoryEntry getItem(int i) {
        return this.data.get(i);
    }

    @Override // android.widget.Adapter
    public long getItemId(int i) {
        SLInventoryEntry item = getItem(i);
        if (item != null) {
            return item.getId();
        }
        return -1L;
    }

    /* JADX WARN: Removed duplicated region for block: B:63:0x0144  */
    @Override // android.widget.Adapter
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public android.view.View getView(int r12, android.view.View r13, android.view.ViewGroup r14) {
        /*
            Method dump skipped, instruction units count: 382
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lumiyaviewer.lumiya.ui.inventory.InventoryFolderAdapter.getView(int, android.view.View, android.view.ViewGroup):android.view.View");
    }

    @Override // android.widget.BaseAdapter, android.widget.Adapter
    public boolean hasStableIds() {
        return true;
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        if (this.onItemCheckboxClickListener != null) {
            Object tag = view.getTag(R.id.tag_outfit_object);
            if (tag instanceof SLInventoryEntry) {
                this.onItemCheckboxClickListener.onItemCheckboxClicked((SLInventoryEntry) tag);
            }
        }
    }

    public void setAvatarAppearance(@Nullable SLAvatarAppearance sLAvatarAppearance) {
        this.avatarAppearance = sLAvatarAppearance;
        notifyDataSetChanged();
    }

    public void setData(@Nullable InventoryEntryList inventoryEntryList) {
        if (inventoryEntryList == null) {
            inventoryEntryList = new InventoryEntryList();
        }
        this.data = inventoryEntryList;
        notifyDataSetChanged();
    }

    public void setDatabase(InventoryDB inventoryDB) {
        this.database = inventoryDB;
        notifyDataSetChanged();
    }

    public void setOnItemCheckboxClickListener(@Nullable OnItemCheckboxClickListener onItemCheckboxClickListener) {
        this.onItemCheckboxClickListener = onItemCheckboxClickListener;
    }

    public void setWornAttachments(@Nullable ImmutableMap<UUID, String> immutableMap) {
        this.wornAttachments = immutableMap;
        notifyDataSetChanged();
    }

    public void setWornOutfitFolder(@Nullable UUID uuid) {
        this.wornOutfitFolder = uuid;
        notifyDataSetChanged();
    }

    public void setWornWearables(@Nullable Table<SLWearableType, UUID, SLWearable> table) {
        this.wornWearables = table;
        notifyDataSetChanged();
    }
}
