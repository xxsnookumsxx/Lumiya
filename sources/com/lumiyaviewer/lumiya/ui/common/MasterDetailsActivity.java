package com.lumiyaviewer.lumiya.ui.common;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import com.lumiyaviewer.lumiya.Debug;
import com.lumiyaviewer.lumiya.LumiyaApp;
import com.lumiyaviewer.lumiya.R;

/* loaded from: classes.dex */
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
    /* JADX WARN: Removed duplicated region for block: B:38:0x00eb  */
    /* JADX WARN: Removed duplicated region for block: B:40:0x00f0  */
    /* JADX WARN: Removed duplicated region for block: B:53:0x0122  */
    /* JADX WARN: Removed duplicated region for block: B:56:0x0141  */
    /* JADX WARN: Removed duplicated region for block: B:70:0x01a4  */
    /* JADX WARN: Removed duplicated region for block: B:73:? A[RETURN, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:83:0x0222  */
    /* JADX WARN: Removed duplicated region for block: B:87:0x01f2  */
    /* JADX WARN: Removed duplicated region for block: B:94:0x01e5  */
    /* JADX WARN: Removed duplicated region for block: B:98:0x01c0  */
    @Override // com.lumiyaviewer.lumiya.ui.common.DetailsActivity, com.lumiyaviewer.lumiya.ui.common.ConnectedActivity, com.lumiyaviewer.lumiya.ui.common.ThemedActivity, android.support.v7.app.AppCompatActivity, android.support.v4.app.FragmentActivity, android.support.v4.app.SupportActivity, android.app.Activity
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    protected void onCreate(@Nullable Bundle bundle) {
        boolean z;
        Bundle bundle2;
        boolean z2;
        Bundle bundle3;
        boolean z3;
        boolean z4;
        Bundle arguments;
        Bundle arguments2;
        Bundle bundleExtra;
        Bundle arguments3;
        super.onCreate(bundle);
        this.isSplitScreen = LumiyaApp.isSplitScreenNeeded(this);
        if (this.isSplitScreen) {
            setContentView(R.layout.split_two_panels);
        } else {
            setContentView(R.layout.split_one_panel);
        }
        Object[] objArr = new Object[4];
        objArr[0] = Boolean.valueOf(findViewById(R.id.selector) != null);
        objArr[1] = Boolean.valueOf(getSupportFragmentManager().findFragmentById(R.id.selector) != null);
        objArr[2] = Boolean.valueOf(findViewById(R.id.details) != null);
        objArr[3] = Boolean.valueOf(getSupportFragmentManager().findFragmentById(R.id.details) != null);
        Debug.Printf("MasterDetailsActivity: hasSelectorView = %b, sel fragment %b, hasDetailsView = %b, details fragment %b", objArr);
        Debug.Printf("MasterDetailsActivity: intent = %s", getIntent());
        FragmentTransaction beginTransaction = getSupportFragmentManager().beginTransaction();
        Fragment findFragmentById = getSupportFragmentManager().findFragmentById(R.id.selector);
        Fragment findFragmentById2 = getSupportFragmentManager().findFragmentById(R.id.details);
        z = false;
        if (findFragmentById2 != null && (arguments3 = findFragmentById2.getArguments()) != null) {
            Debug.Printf("MasterDetailsActivity: implicit details tag = %b", Boolean.valueOf(arguments3.getBoolean(IMPLICIT_DETAILS_TAG, false)));
            if (!arguments3.getBoolean(IMPLICIT_DETAILS_TAG, false)) {
                z = true;
            }
        }
        Debug.Printf("MasterDetailsActivity: hasExplicitDetails = %b", Boolean.valueOf(z));
        if (!z && bundle == null && (bundle2 = getIntent().getBundleExtra(INTENT_SELECTION_KEY)) != null) {
            z = true;
        } else {
            bundle2 = null;
        }
        if (z || bundle != null) {
            z2 = z;
            bundle3 = bundle2;
        } else if (!this.isSplitScreen || (bundleExtra = getIntent().getBundleExtra(WEAK_SELECTION_KEY)) == null) {
            z2 = z;
            bundle3 = bundle2;
        } else {
            bundle3 = bundleExtra;
            z2 = true;
        }
        z3 = this.isSplitScreen ? true : !z2;
        if (z3) {
            Object[] objArr2 = new Object[1];
            objArr2[0] = findFragmentById != null ? findFragmentById.toString() : "null";
            Debug.Printf("MasterDetailsActivity: existing fragment %s", objArr2);
            if (findFragmentById != null) {
                Object[] objArr3 = new Object[1];
                objArr3[0] = findFragmentById.isVisible() ? "visible" : "not visible";
                Debug.Printf("MasterDetailsActivity: existing fragment is %s", objArr3);
                if (findFragmentById.isDetached()) {
                    beginTransaction.attach(findFragmentById);
                } else if (findFragmentById.isHidden()) {
                    beginTransaction.show(findFragmentById);
                }
            } else {
                beginTransaction.add(R.id.selector, onCreateMasterFragment(getIntent(), bundle3));
            }
        } else if (findFragmentById != null && !findFragmentById.isDetached()) {
            beginTransaction.detach(findFragmentById);
        }
        z4 = this.isSplitScreen ? true : z2;
        Debug.Printf("MasterDetailsActivity: selectorVisible %b, detailsVisible %b, hasExplicitDetails %b", Boolean.valueOf(z3), Boolean.valueOf(z4), Boolean.valueOf(z2));
        if (!z4) {
            if (findFragmentById2 != null && !findFragmentById2.isDetached()) {
                beginTransaction.remove(findFragmentById2);
            }
        } else if (findFragmentById2 == null) {
            Debug.Printf("MasterDetailsActivity: creating new details fragment", new Object[0]);
            arguments = null;
            if (findFragmentById != null) {
                try {
                    arguments = findFragmentById.getArguments();
                } catch (Exception e) {
                    Debug.Warning(e);
                }
            }
            Bundle newDetailsFragmentArguments = getNewDetailsFragmentArguments(arguments, bundle3);
            try {
                Fragment newInstance = (Fragment) getDetailsFragmentFactory().getFragmentClass().newInstance();
                if (newInstance instanceof ReloadableFragment) {
                    newInstance.setArguments(new Bundle());
                    ((ReloadableFragment) newInstance).setFragmentArgs(getIntent(), newDetailsFragmentArguments);
                } else {
                    newInstance.setArguments(newDetailsFragmentArguments);
                }
                if (!z2 && (arguments2 = newInstance.getArguments()) != null) {
                    arguments2.putBoolean(IMPLICIT_DETAILS_TAG, true);
                }
                Debug.Printf("MasterDetailsActivity: adding new details fragment: %s", newInstance);
                beginTransaction.add(R.id.details, newInstance, DetailsActivity.DEFAULT_DETAILS_FRAGMENT_TAG);
            } catch (Exception e2) {
                Debug.Warning(e2);
            }
        } else {
            Debug.Printf("MasterDetailsActivity: not creating new details fragment. existing is detached: %b (%s)", Boolean.valueOf(findFragmentById2.isDetached()), findFragmentById2);
            if (findFragmentById2.isDetached()) {
                beginTransaction.attach(findFragmentById2);
            }
        }
        if (!beginTransaction.isEmpty()) {
            beginTransaction.commit();
        }
    }

    protected abstract Fragment onCreateMasterFragment(Intent intent, @Nullable Bundle bundle);

    @Override // com.lumiyaviewer.lumiya.ui.common.DetailsActivity
    protected boolean onDetailsStackEmpty() {
        FragmentManager supportFragmentManager;
        Fragment findFragmentById;
        if (this.isSplitScreen || (findFragmentById = (supportFragmentManager = getSupportFragmentManager()).findFragmentById(R.id.details)) == null || !(!findFragmentById.isDetached())) {
            return true;
        }
        Debug.Printf("MasterDetailsFragment: onDetailsStackEmpty has detailsFragment (%s), detached: %b", findFragmentById, Boolean.valueOf(findFragmentById.isDetached()));
        FragmentTransaction beginTransaction = supportFragmentManager.beginTransaction();
        beginTransaction.setCustomAnimations(android.R.anim.fade_in, R.anim.slide_to_right, 0, android.R.anim.fade_out);
        beginTransaction.remove(findFragmentById);
        Fragment findFragmentById2 = supportFragmentManager.findFragmentById(R.id.selector);
        Object[] objArr = new Object[3];
        objArr[0] = Boolean.valueOf(findFragmentById2 != null);
        objArr[1] = Boolean.valueOf(findFragmentById2 != null ? findFragmentById2.isDetached() : false);
        objArr[2] = Boolean.valueOf(findFragmentById2 != null ? findFragmentById2.isHidden() : false);
        Debug.Printf("MasterDetailsFragment: existing selector %b, detached %b, hidden %b", objArr);
        if (findFragmentById2 == null) {
            beginTransaction.add(R.id.selector, onCreateMasterFragment(getIntent(), null));
        } else {
            if (findFragmentById2.isDetached()) {
                beginTransaction.attach(findFragmentById2);
            }
            if (findFragmentById2.isHidden()) {
                beginTransaction.show(findFragmentById2);
            }
        }
        beginTransaction.commit();
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
        Fragment findFragmentById;
        FragmentTransaction beginTransaction = fragmentManager.beginTransaction();
        beginTransaction.setCustomAnimations(R.anim.slide_from_right, android.R.anim.fade_out, 0, android.R.anim.fade_out);
        if (!this.isSplitScreen && (findFragmentById = fragmentManager.findFragmentById(R.id.selector)) != null && findFragmentById.isVisible()) {
            beginTransaction.hide(findFragmentById);
        }
        beginTransaction.replace(R.id.details, fragment);
        beginTransaction.commit();
        updateTitle();
    }

    @Override // com.lumiyaviewer.lumiya.ui.common.DetailsActivity
    public Fragment showDetailsFragment(Class<? extends Fragment> cls, Intent intent, Bundle bundle) {
        Bundle arguments;
        Fragment showDetailsFragment = super.showDetailsFragment(cls, intent, bundle);
        if (showDetailsFragment != null && (arguments = showDetailsFragment.getArguments()) != null) {
            arguments.putBoolean(IMPLICIT_DETAILS_TAG, isAlwaysImplicitFragment(cls));
        }
        return showDetailsFragment;
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // com.lumiyaviewer.lumiya.ui.common.DetailsActivity
    protected void updateTitleNoDetails() {
        boolean z;
        Fragment findFragmentById = getSupportFragmentManager().findFragmentById(R.id.selector);
        if (findFragmentById != null && (findFragmentById instanceof FragmentHasTitle) && findFragmentById.isAdded()) {
            if (!findFragmentById.isDetached()) {
                String title = ((FragmentHasTitle) findFragmentById).getTitle();
                String subTitle = ((FragmentHasTitle) findFragmentById).getSubTitle();
                if (title != null) {
                    z = true;
                    setActivityTitle(title, subTitle);
                }
            }
            z = false;
        } else {
            z = false;
        }
        if (z) {
            return;
        }
        super.updateTitleNoDetails();
    }
}
