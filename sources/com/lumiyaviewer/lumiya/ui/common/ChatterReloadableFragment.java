package com.lumiyaviewer.lumiya.ui.common;

import android.content.Intent;
import android.os.Bundle;
import com.lumiyaviewer.lumiya.slproto.users.ChatterID;
import java.util.UUID;

/* JADX INFO: loaded from: classes.dex */
public abstract class ChatterReloadableFragment extends ChatterFragment implements ReloadableFragment {
    @Override // com.lumiyaviewer.lumiya.ui.common.ReloadableFragment
    public void setFragmentArgs(Intent intent, Bundle bundle) {
        UUID activeAgentID;
        ChatterID localChatterID = bundle != null ? (ChatterID) bundle.getParcelable(ChatterFragment.CHATTER_ID_KEY) : null;
        if (localChatterID == null && (activeAgentID = ActivityUtils.getActiveAgentID(intent)) != null) {
            localChatterID = ChatterID.getLocalChatterID(activeAgentID);
        }
        if (bundle != null) {
            getArguments().putAll(bundle);
        }
        if (isFragmentStarted()) {
            setNewUser(localChatterID);
        }
    }
}
