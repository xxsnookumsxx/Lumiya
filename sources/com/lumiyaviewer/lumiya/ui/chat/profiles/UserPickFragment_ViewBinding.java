package com.lumiyaviewer.lumiya.ui.chat.profiles;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Utils;
import com.lumiyaviewer.lumiya.R;
import com.lumiyaviewer.lumiya.ui.common.ImageAssetView;

/* JADX INFO: loaded from: classes.dex */
public class UserPickFragment_ViewBinding implements Unbinder {
    private UserPickFragment target;
    private View view2131755696;
    private View view2131755697;
    private View view2131755698;
    private View view2131755700;

    @UiThread
    public UserPickFragment_ViewBinding(final UserPickFragment userPickFragment, View view) {
        this.target = userPickFragment;
        View viewFindRequiredView = Utils.findRequiredView(view, R.id.user_pick_set_location_button, "field 'setLocationButton' and method 'onSetLocation'");
        userPickFragment.setLocationButton = (Button) Utils.castView(viewFindRequiredView, R.id.user_pick_set_location_button, "field 'setLocationButton'", Button.class);
        this.view2131755696 = viewFindRequiredView;
        viewFindRequiredView.setOnClickListener(new DebouncingOnClickListener() { // from class: com.lumiyaviewer.lumiya.ui.chat.profiles.UserPickFragment_ViewBinding.1
            @Override // butterknife.internal.DebouncingOnClickListener
            public void doClick(View view2) {
                userPickFragment.onSetLocation(view2);
            }
        });
        View viewFindRequiredView2 = Utils.findRequiredView(view, R.id.change_pic_button, "field 'changePicButton' and method 'onChangePic'");
        userPickFragment.changePicButton = (Button) Utils.castView(viewFindRequiredView2, R.id.change_pic_button, "field 'changePicButton'", Button.class);
        this.view2131755698 = viewFindRequiredView2;
        viewFindRequiredView2.setOnClickListener(new DebouncingOnClickListener() { // from class: com.lumiyaviewer.lumiya.ui.chat.profiles.UserPickFragment_ViewBinding.2
            @Override // butterknife.internal.DebouncingOnClickListener
            public void doClick(View view2) {
                userPickFragment.onChangePic(view2);
            }
        });
        userPickFragment.userPickImageView = (ImageAssetView) Utils.findRequiredViewAsType(view, R.id.user_pick_image_view, "field 'userPickImageView'", ImageAssetView.class);
        userPickFragment.pickDescription = (TextView) Utils.findRequiredViewAsType(view, R.id.pick_description, "field 'pickDescription'", TextView.class);
        View viewFindRequiredView3 = Utils.findRequiredView(view, R.id.user_pick_desc_edit_button, "field 'userPickDescEditButton' and method 'onDescEdit'");
        userPickFragment.userPickDescEditButton = (Button) Utils.castView(viewFindRequiredView3, R.id.user_pick_desc_edit_button, "field 'userPickDescEditButton'", Button.class);
        this.view2131755700 = viewFindRequiredView3;
        viewFindRequiredView3.setOnClickListener(new DebouncingOnClickListener() { // from class: com.lumiyaviewer.lumiya.ui.chat.profiles.UserPickFragment_ViewBinding.3
            @Override // butterknife.internal.DebouncingOnClickListener
            public void doClick(View view2) {
                userPickFragment.onDescEdit(view2);
            }
        });
        View viewFindRequiredView4 = Utils.findRequiredView(view, R.id.user_pick_teleport_button, "method 'onTeleportToPickClick'");
        this.view2131755697 = viewFindRequiredView4;
        viewFindRequiredView4.setOnClickListener(new DebouncingOnClickListener() { // from class: com.lumiyaviewer.lumiya.ui.chat.profiles.UserPickFragment_ViewBinding.4
            @Override // butterknife.internal.DebouncingOnClickListener
            public void doClick(View view2) {
                userPickFragment.onTeleportToPickClick(view2);
            }
        });
    }

    @Override // butterknife.Unbinder
    @CallSuper
    public void unbind() {
        UserPickFragment userPickFragment = this.target;
        if (userPickFragment == null) {
            throw new IllegalStateException("Bindings already cleared.");
        }
        this.target = null;
        userPickFragment.setLocationButton = null;
        userPickFragment.changePicButton = null;
        userPickFragment.userPickImageView = null;
        userPickFragment.pickDescription = null;
        userPickFragment.userPickDescEditButton = null;
        this.view2131755696.setOnClickListener(null);
        this.view2131755696 = null;
        this.view2131755698.setOnClickListener(null);
        this.view2131755698 = null;
        this.view2131755700.setOnClickListener(null);
        this.view2131755700 = null;
        this.view2131755697.setOnClickListener(null);
        this.view2131755697 = null;
    }
}
