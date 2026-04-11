package com.lumiyaviewer.lumiya.ui.common;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import com.lumiyaviewer.lumiya.Debug;
import com.lumiyaviewer.lumiya.R;

/* JADX INFO: loaded from: classes.dex */
public abstract class MasterDetailsActivity extends DetailsActivity {
    protected static final String FROM_SAME_ACTIVITY = "fromSameActivity";
    private static final String IMPLICIT_DETAILS_TAG = "MasterDetailsActivityIsImplicitDetails";
    public static final String INTENT_SELECTION_KEY = "selection";
    public static final String WEAK_SELECTION_KEY = "weakSelection";
    private boolean isSplitScreen = false;

    protected abstract FragmentActivityFactory getDetailsFragmentFactory();

    protected Bundle getNewDetailsFragmentArguments(@Nullable Bundle bundle, @Nullable Bundle bundle2) {
        return bundle2;
    }

    protected boolean isAlwaysImplicitFragment(Class<? extends Fragment> cls) {
        return false;
    }

    @Override // com.lumiyaviewer.lumiya.ui.common.DetailsActivity
    protected boolean isRootDetailsFragment(Class<? extends Fragment> cls) {
        return getDetailsFragmentFactory().getFragmentClass().isAssignableFrom(cls);
    }

    public boolean isSplitScreen() {
        return this.isSplitScreen;
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Removed duplicated region for block: B:105:0x0236  */
    @Override // com.lumiyaviewer.lumiya.ui.common.DetailsActivity, com.lumiyaviewer.lumiya.ui.common.ConnectedActivity, com.lumiyaviewer.lumiya.ui.common.ThemedActivity, android.support.v7.app.AppCompatActivity, android.support.v4.app.FragmentActivity, android.support.v4.app.SupportActivity, android.app.Activity
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    protected void onCreate(@android.support.annotation.Nullable android.os.Bundle r15) {
        /*
            Method dump skipped, instruction units count: 569
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lumiyaviewer.lumiya.ui.common.MasterDetailsActivity.onCreate(android.os.Bundle):void");
    }

    protected abstract Fragment onCreateMasterFragment(Intent intent, @Nullable Bundle bundle);

    @Override // com.lumiyaviewer.lumiya.ui.common.DetailsActivity
    protected boolean onDetailsStackEmpty() {
        FragmentManager supportFragmentManager;
        Fragment fragmentFindFragmentById;
        if (this.isSplitScreen || (fragmentFindFragmentById = (supportFragmentManager = getSupportFragmentManager()).findFragmentById(R.id.details)) == null || !(!fragmentFindFragmentById.isDetached())) {
            return true;
        }
        Debug.Printf("MasterDetailsFragment: onDetailsStackEmpty has detailsFragment (%s), detached: %b", fragmentFindFragmentById, Boolean.valueOf(fragmentFindFragmentById.isDetached()));
        FragmentTransaction fragmentTransactionBeginTransaction = supportFragmentManager.beginTransaction();
        fragmentTransactionBeginTransaction.setCustomAnimations(android.R.anim.fade_in, R.anim.slide_to_right, 0, android.R.anim.fade_out);
        fragmentTransactionBeginTransaction.remove(fragmentFindFragmentById);
        Fragment fragmentFindFragmentById2 = supportFragmentManager.findFragmentById(R.id.selector);
        Object[] objArr = new Object[3];
        objArr[0] = Boolean.valueOf(fragmentFindFragmentById2 != null);
        objArr[1] = Boolean.valueOf(fragmentFindFragmentById2 != null ? fragmentFindFragmentById2.isDetached() : false);
        objArr[2] = Boolean.valueOf(fragmentFindFragmentById2 != null ? fragmentFindFragmentById2.isHidden() : false);
        Debug.Printf("MasterDetailsFragment: existing selector %b, detached %b, hidden %b", objArr);
        if (fragmentFindFragmentById2 == null) {
            fragmentTransactionBeginTransaction.add(R.id.selector, onCreateMasterFragment(getIntent(), null));
        } else {
            if (fragmentFindFragmentById2.isDetached()) {
                fragmentTransactionBeginTransaction.attach(fragmentFindFragmentById2);
            }
            if (fragmentFindFragmentById2.isHidden()) {
                fragmentTransactionBeginTransaction.show(fragmentFindFragmentById2);
            }
        }
        fragmentTransactionBeginTransaction.commit();
        updateTitle();
        return false;
    }

    @Override // com.lumiyaviewer.lumiya.ui.common.ConnectedActivity, android.support.v4.app.FragmentActivity, android.app.Activity
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Debug.Printf("MasterDetailsActivity: onNewIntent, intent = %s", intent);
        Bundle bundleExtra = intent.hasExtra(INTENT_SELECTION_KEY) ? intent.getBundleExtra(INTENT_SELECTION_KEY) : null;
        Bundle bundleExtra2 = intent.hasExtra(WEAK_SELECTION_KEY) ? intent.getBundleExtra(WEAK_SELECTION_KEY) : null;
        if (bundleExtra != null) {
            showDetails(this, getDetailsFragmentFactory(), bundleExtra);
            return;
        }
        if (this.isSplitScreen && bundleExtra2 != null) {
            showDetails(this, getDetailsFragmentFactory(), bundleExtra2);
            return;
        }
        if (this.isSplitScreen) {
            return;
        }
        if (getSupportFragmentManager().findFragmentById(R.id.details) == null && bundleExtra2 != null && intent.getBooleanExtra(FROM_SAME_ACTIVITY, false)) {
            showDetails(this, getDetailsFragmentFactory(), bundleExtra2);
        } else {
            clearDetailsStack();
            onDetailsStackEmpty();
        }
    }

    @Override // com.lumiyaviewer.lumiya.ui.common.DetailsActivity, com.lumiyaviewer.lumiya.ui.common.ConnectedActivity, android.support.v7.app.AppCompatActivity, android.support.v4.app.FragmentActivity, android.support.v4.app.SupportActivity, android.app.Activity
    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
    }

    @Override // com.lumiyaviewer.lumiya.ui.common.DetailsActivity
    protected void replaceDetailsFragment(FragmentManager fragmentManager, Fragment fragment) {
        Fragment fragmentFindFragmentById;
        FragmentTransaction fragmentTransactionBeginTransaction = fragmentManager.beginTransaction();
        fragmentTransactionBeginTransaction.setCustomAnimations(R.anim.slide_from_right, android.R.anim.fade_out, 0, android.R.anim.fade_out);
        if (!this.isSplitScreen && (fragmentFindFragmentById = fragmentManager.findFragmentById(R.id.selector)) != null && fragmentFindFragmentById.isVisible()) {
            fragmentTransactionBeginTransaction.hide(fragmentFindFragmentById);
        }
        fragmentTransactionBeginTransaction.replace(R.id.details, fragment);
        fragmentTransactionBeginTransaction.commit();
        updateTitle();
    }

    @Override // com.lumiyaviewer.lumiya.ui.common.DetailsActivity
    public Fragment showDetailsFragment(Class<? extends Fragment> cls, Intent intent, Bundle bundle) {
        Bundle arguments;
        Fragment fragmentShowDetailsFragment = super.showDetailsFragment(cls, intent, bundle);
        if (fragmentShowDetailsFragment != null && (arguments = fragmentShowDetailsFragment.getArguments()) != null) {
            arguments.putBoolean(IMPLICIT_DETAILS_TAG, isAlwaysImplicitFragment(cls));
        }
        return fragmentShowDetailsFragment;
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Removed duplicated region for block: B:17:0x003b  */
    @Override // com.lumiyaviewer.lumiya.ui.common.DetailsActivity
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    protected void updateTitleNoDetails() {
        /*
            r4 = this;
            r2 = 0
            android.support.v4.app.FragmentManager r0 = r4.getSupportFragmentManager()
            r1 = 2131755654(0x7f100286, float:1.9142193E38)
            android.support.v4.app.Fragment r1 = r0.findFragmentById(r1)
            if (r1 == 0) goto L39
            boolean r0 = r1 instanceof com.lumiyaviewer.lumiya.ui.common.FragmentHasTitle
            if (r0 == 0) goto L39
            boolean r0 = r1.isAdded()
            if (r0 == 0) goto L39
            boolean r0 = r1.isDetached()
            r0 = r0 ^ 1
            if (r0 == 0) goto L3b
            r0 = r1
            com.lumiyaviewer.lumiya.ui.common.FragmentHasTitle r0 = (com.lumiyaviewer.lumiya.ui.common.FragmentHasTitle) r0
            java.lang.String r3 = r0.getTitle()
            com.lumiyaviewer.lumiya.ui.common.FragmentHasTitle r1 = (com.lumiyaviewer.lumiya.ui.common.FragmentHasTitle) r1
            java.lang.String r1 = r1.getSubTitle()
            if (r3 == 0) goto L3b
            r0 = 1
            r4.setActivityTitle(r3, r1)
        L33:
            if (r0 != 0) goto L38
            super.updateTitleNoDetails()
        L38:
            return
        L39:
            r0 = r2
            goto L33
        L3b:
            r0 = r2
            goto L33
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lumiyaviewer.lumiya.ui.common.MasterDetailsActivity.updateTitleNoDetails():void");
    }
}
