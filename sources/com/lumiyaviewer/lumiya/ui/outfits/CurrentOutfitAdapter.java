package com.lumiyaviewer.lumiya.ui.outfits;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.common.collect.ImmutableList;
import com.lumiyaviewer.lumiya.R;
import com.lumiyaviewer.lumiya.slproto.modules.SLAvatarAppearance;
import com.lumiyaviewer.lumiya.ui.common.DismissableAdapter;
import com.lumiyaviewer.lumiya.ui.common.SwipeDismissListViewTouchListener;

/* JADX INFO: loaded from: classes.dex */
class CurrentOutfitAdapter extends BaseAdapter implements DismissableAdapter {

    @Nullable
    private SLAvatarAppearance avatarAppearance;
    private final LayoutInflater inflater;

    @NonNull
    private ImmutableList<SLAvatarAppearance.WornItem> wornItems = ImmutableList.of();

    CurrentOutfitAdapter(Context context) {
        this.inflater = LayoutInflater.from(context);
    }

    @Override // com.lumiyaviewer.lumiya.ui.common.DismissableAdapter
    public boolean canDismiss(int i) {
        SLAvatarAppearance.WornItem item = getItem(i);
        if (item == null || this.avatarAppearance == null) {
            return false;
        }
        if (item.getWornOn() == null) {
            return this.avatarAppearance.canDetachItem(item);
        }
        if (item.getWornOn().isBodyPart()) {
            return false;
        }
        return this.avatarAppearance.canTakeItemOff(item.getWornOn());
    }

    @Override // android.widget.Adapter
    public int getCount() {
        return this.wornItems.size();
    }

    @Override // android.widget.Adapter
    public SLAvatarAppearance.WornItem getItem(int i) {
        if (i < 0 || i >= this.wornItems.size()) {
            return null;
        }
        return this.wornItems.get(i);
    }

    @Override // android.widget.Adapter
    public long getItemId(int i) {
        return i;
    }

    @Override // android.widget.BaseAdapter, android.widget.Adapter
    public int getItemViewType(int i) {
        return 0;
    }

    @Override // android.widget.Adapter
    public View getView(int i, View view, ViewGroup viewGroup) {
        View view2 = (view == null || view.getId() == R.id.outfitItemLayout) ? view : null;
        View viewInflate = view2 == null ? this.inflater.inflate(R.layout.outfit_item, viewGroup, false) : view2;
        SLAvatarAppearance.WornItem wornItem = this.wornItems.get(i);
        ((TextView) viewInflate.findViewById(R.id.itemNameTextView)).setText(wornItem.getName());
        if (wornItem.getWornOn() != null) {
            ((ImageView) viewInflate.findViewById(R.id.itemTypeIconView)).setImageResource(R.drawable.inv_clothes);
            viewInflate.findViewById(R.id.itemTouchableIcon).setVisibility(8);
        } else {
            ((ImageView) viewInflate.findViewById(R.id.itemTypeIconView)).setImageResource(R.drawable.inv_object);
            viewInflate.findViewById(R.id.itemTouchableIcon).setVisibility(wornItem.getIsTouchable() ? 0 : 8);
        }
        SwipeDismissListViewTouchListener.restoreViewState(viewInflate);
        return viewInflate;
    }

    @Override // android.widget.BaseAdapter, android.widget.Adapter
    public boolean hasStableIds() {
        return false;
    }

    @Override // android.widget.BaseAdapter, android.widget.Adapter
    public boolean isEmpty() {
        return this.wornItems.isEmpty();
    }

    @Override // com.lumiyaviewer.lumiya.ui.common.DismissableAdapter
    public void onDismiss(int i) {
        SLAvatarAppearance.WornItem item = getItem(i);
        if (item == null || this.avatarAppearance == null) {
            return;
        }
        if (item.getWornOn() != null) {
            this.avatarAppearance.TakeItemOff(item.itemID());
        } else {
            this.avatarAppearance.DetachItem(item);
        }
    }

    public void setAvatarAppearance(@Nullable SLAvatarAppearance sLAvatarAppearance) {
        this.avatarAppearance = sLAvatarAppearance;
    }

    public void setData(ImmutableList<SLAvatarAppearance.WornItem> immutableList) {
        if (immutableList == null) {
            immutableList = ImmutableList.of();
        }
        this.wornItems = immutableList;
        notifyDataSetChanged();
    }
}
