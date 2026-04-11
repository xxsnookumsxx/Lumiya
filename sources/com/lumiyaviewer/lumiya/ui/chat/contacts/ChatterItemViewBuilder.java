package com.lumiyaviewer.lumiya.ui.chat.contacts;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.lumiyaviewer.lumiya.R;
import com.lumiyaviewer.lumiya.slproto.users.ChatterID;
import com.lumiyaviewer.lumiya.ui.chat.ChatterPicView;
import com.lumiyaviewer.lumiya.ui.chat.TypingIndicatorView;
import javax.annotation.Nullable;

/* JADX INFO: loaded from: classes.dex */
public class ChatterItemViewBuilder {
    private float distance;
    private boolean isActiveGroup;
    private boolean isOnline;
    private String label;
    private String lastMessage;
    private ChatterID thumbnailChatterID;
    private int thumbnailDefaultIcon;
    private String thumbnailLabel;
    private int unreadCount;
    private boolean voiceActive;
    private boolean onlineVisible = false;
    private boolean distanceSet = false;

    public ChatterItemViewBuilder() {
        reset();
    }

    @Nullable
    public View getView(LayoutInflater layoutInflater, View view, ViewGroup viewGroup, boolean z) {
        int i = R.id.userDistanceInlineTextView;
        View view2 = (view == null || view.getId() != R.id.contactListItemLayout) ? null : view;
        View viewInflate = view2 == null ? layoutInflater.inflate(R.layout.contact_list_item, viewGroup, false) : view2;
        if (viewInflate != null) {
            ((TextView) viewInflate.findViewById(R.id.userNameTextView)).setText(this.label);
            View viewFindViewById = viewInflate.findViewById(R.id.onlineUserIcon);
            if (viewFindViewById != null) {
                if (this.onlineVisible) {
                    viewFindViewById.setVisibility(0);
                } else {
                    viewFindViewById.setVisibility(8);
                }
            }
            View viewFindViewById2 = viewInflate.findViewById(R.id.activeVoiceIcon);
            if (viewFindViewById2 != null) {
                viewFindViewById2.setVisibility(this.voiceActive ? 0 : 8);
            }
            TextView textView = (TextView) viewInflate.findViewById(z ? R.id.userDistanceInlineTextView : R.id.userDistanceTextView);
            if (textView != null) {
                if (this.distanceSet) {
                    textView.setText((this.distance >= 9.5f ? Integer.toString(Math.round(this.distance)) : String.format("%.1f", Float.valueOf(this.distance))) + " m");
                    if (this.distance <= 20.0f) {
                        textView.setTypeface(textView.getTypeface(), 1);
                    } else {
                        textView.setTypeface(Typeface.create(textView.getTypeface(), 0));
                    }
                    textView.setVisibility(0);
                } else {
                    textView.setText((CharSequence) null);
                    textView.setVisibility(z ? 8 : 4);
                }
            }
            if (z) {
                i = R.id.userDistanceTextView;
            }
            View viewFindViewById3 = viewInflate.findViewById(i);
            if (viewFindViewById3 != null) {
                viewFindViewById3.setVisibility(8);
            }
            TextView textView2 = (TextView) viewInflate.findViewById(R.id.unreadCountTextView);
            if (textView2 != null) {
                textView2.setText(Integer.toString(this.unreadCount));
                if (this.unreadCount != 0) {
                    textView2.setVisibility(0);
                } else {
                    textView2.setVisibility(8);
                }
            }
            TextView textView3 = (TextView) viewInflate.findViewById(R.id.lastMessageText);
            if (textView3 != null) {
                if (this.lastMessage != null) {
                    textView3.setText(this.lastMessage);
                    textView3.setVisibility(0);
                } else {
                    textView3.setVisibility(8);
                }
            }
            View viewFindViewById4 = viewInflate.findViewById(R.id.activeGroupIcon);
            if (viewFindViewById4 != null) {
                viewFindViewById4.setVisibility(this.isActiveGroup ? 0 : 8);
            }
            ChatterPicView chatterPicView = (ChatterPicView) viewInflate.findViewById(R.id.userPicView);
            if (chatterPicView != null) {
                chatterPicView.setDefaultIcon(this.thumbnailDefaultIcon, false);
                chatterPicView.setChatterID(this.thumbnailChatterID, this.thumbnailLabel);
                chatterPicView.setVisibility((this.thumbnailChatterID == null && this.thumbnailDefaultIcon == -1) ? 8 : 0);
            }
            TypingIndicatorView typingIndicatorView = (TypingIndicatorView) viewInflate.findViewById(R.id.typing_indicator);
            if (typingIndicatorView != null) {
                typingIndicatorView.setChatterID(this.thumbnailChatterID);
            }
        }
        return viewInflate;
    }

    public void reset() {
        this.label = null;
        this.onlineVisible = false;
        this.distanceSet = false;
        this.unreadCount = 0;
        this.lastMessage = null;
        this.isActiveGroup = false;
        this.thumbnailChatterID = null;
        this.thumbnailLabel = null;
        this.thumbnailDefaultIcon = -1;
        this.voiceActive = false;
    }

    public void setActiveGroup(boolean z) {
        this.isActiveGroup = z;
    }

    public void setDistance(float f) {
        if (Float.isNaN(f)) {
            this.distanceSet = false;
        } else {
            this.distanceSet = true;
            this.distance = f;
        }
    }

    public void setLabel(String str) {
        this.label = str;
    }

    public void setLastMessage(String str) {
        this.lastMessage = str;
    }

    public void setOnlineStatusIcon(boolean z, boolean z2) {
        this.onlineVisible = z;
        this.isOnline = z2;
    }

    public void setThumbnailChatterID(ChatterID chatterID, String str) {
        this.thumbnailChatterID = chatterID;
        this.thumbnailLabel = str;
    }

    public void setThumbnailDefaultIcon(int i) {
        this.thumbnailDefaultIcon = i;
    }

    public void setUnreadCount(int i) {
        this.unreadCount = i;
    }

    public void setVoiceActive(boolean z) {
        this.voiceActive = z;
    }
}
