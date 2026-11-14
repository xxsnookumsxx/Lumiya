package com.lumiyaviewer.lumiya.ui.common;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import com.lumiyaviewer.lumiya.Debug;
import com.lumiyaviewer.lumiya.R;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/* loaded from: classes.dex */
public class DetailsActivity extends ConnectedActivity {
    public static final String DEFAULT_DETAILS_FRAGMENT_TAG = "defaultDetails";
    private static final String DEFAULT_SUBTITLE_TAG = "DetailsActivity:defaultSubTitle";
    private static final String DEFAULT_TITLE_TAG = "DetailsActivity:defaultTitle";
    private static final String DETAILS_STACK_TAG = "DetailsActivity:DetailsStack";
    private final ArrayList<DetailsStackEntry> detailsStack = new ArrayList<>();

    @Nullable
    private String defaultTitle = null;

    @Nullable
    private String defaultSubTitle = null;

    private static class DetailsStackEntry implements Parcelable {
        public static final Parcelable.Creator<DetailsStackEntry> CREATOR = new Parcelable.Creator<DetailsStackEntry>() { // from class: com.lumiyaviewer.lumiya.ui.common.DetailsActivity.DetailsStackEntry.1
            /* JADX WARN: Can't rename method to resolve collision */
            @Override // android.os.Parcelable.Creator
            public DetailsStackEntry createFromParcel(Parcel parcel) {
                return new DetailsStackEntry(parcel);
            }

            /* JADX WARN: Can't rename method to resolve collision */
            @Override // android.os.Parcelable.Creator
            public DetailsStackEntry[] newArray(int i) {
                return new DetailsStackEntry[i];
            }
        };
        public final Bundle arguments;
        public final String className;
        public final SoftReference<Fragment> fragment;
        public final Fragment.SavedState savedState;

        protected DetailsStackEntry(Parcel parcel) {
            this.fragment = null;
            this.className = parcel.readString();
            if (parcel.readByte() != 0) {
                this.arguments = parcel.readBundle(getClass().getClassLoader());
            } else {
                this.arguments = null;
            }
            if (parcel.readByte() != 0) {
                this.savedState = (Fragment.SavedState) parcel.readBundle(getClass().getClassLoader()).getParcelable("savedState");
            } else {
                this.savedState = null;
            }
        }

        private DetailsStackEntry(@Nonnull Fragment fragment) {
            this.fragment = new SoftReference<>(fragment);
            this.className = fragment.getClass().getName();
            this.arguments = fragment.getArguments();
            FragmentManager fragmentManager = fragment.getFragmentManager();
            if (fragmentManager != null) {
                this.savedState = fragmentManager.saveFragmentInstanceState(fragment);
            } else {
                this.savedState = null;
            }
        }

        /* synthetic */ DetailsStackEntry(Fragment fragment, DetailsStackEntry detailsStackEntry) {
            this(fragment);
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        public Fragment getFragment(Context context) {
            Fragment fragment = this.fragment.get();
            if (fragment == null) {
                fragment = Fragment.instantiate(context, this.className, this.arguments);
                if (this.savedState != null) {
                    fragment.setInitialSavedState(this.savedState);
                }
            }
            return fragment;
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeString(this.className);
            if (this.arguments != null) {
                parcel.writeByte((byte) 1);
                parcel.writeBundle(this.arguments);
            } else {
                parcel.writeByte((byte) 0);
            }
            if (this.savedState == null) {
                parcel.writeByte((byte) 0);
                return;
            }
            parcel.writeByte((byte) 1);
            Bundle bundle = new Bundle();
            bundle.putParcelable("savedState", this.savedState);
            parcel.writeBundle(bundle);
        }
    }

    private boolean goBack(FragmentManager fragmentManager) {
        Debug.Printf("DetailsActivity: goBack, detailsStack size %d", Integer.valueOf(this.detailsStack.size()));
        if (this.detailsStack.size() == 0) {
            boolean onDetailsStackEmpty = onDetailsStackEmpty();
            Debug.Printf("DetailsActivity: goBack, onDetailsStackEmpty: really empty: %b", Boolean.valueOf(onDetailsStackEmpty));
            return !onDetailsStackEmpty;
        }
        DetailsStackEntry remove = this.detailsStack.remove(this.detailsStack.size() - 1);
        FragmentTransaction beginTransaction = fragmentManager.beginTransaction();
        beginTransaction.replace(R.id.details, remove.getFragment(this));
        beginTransaction.commit();
        updateTitle();
        return true;
    }

    public static void showDetails(Activity activity, FragmentActivityFactory fragmentActivityFactory, Bundle bundle) {
        if (showEmbeddedDetails(activity, fragmentActivityFactory.getFragmentClass(), bundle)) {
            return;
        }
        activity.startActivity(fragmentActivityFactory.createIntent(activity, bundle));
    }

    public static boolean showEmbeddedDetails(Activity activity, Class<? extends Fragment> cls, Bundle bundle) {
        if (!(activity instanceof DetailsActivity) || !((DetailsActivity) activity).acceptsDetailFragment(cls)) {
            return false;
        }
        ((DetailsActivity) activity).showDetailsFragment(cls, activity.getIntent(), bundle);
        return true;
    }

    protected boolean acceptsDetailFragment(Class<? extends Fragment> cls) {
        return true;
    }

    protected void addDetailsToStack(FragmentManager fragmentManager) {
        DetailsStackEntry detailsStackEntry = null;
        Fragment findFragmentById = fragmentManager.findFragmentById(R.id.details);
        if (findFragmentById != null) {
            this.detailsStack.add(new DetailsStackEntry(findFragmentById, detailsStackEntry));
        }
    }

    void clearDetailsStack() {
        this.detailsStack.clear();
    }

    public boolean closeDetailsFragment(Fragment fragment) {
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        if (supportFragmentManager.findFragmentById(R.id.details) == fragment) {
            return goBack(supportFragmentManager);
        }
        return false;
    }

    @Nullable
    public Fragment getCurrentDetailsFragment() {
        Fragment findFragmentById;
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        if (supportFragmentManager != null && (findFragmentById = supportFragmentManager.findFragmentById(R.id.details)) != null && findFragmentById.isAdded() && (!findFragmentById.isDetached()) && (!findFragmentById.isHidden())) {
            return findFragmentById;
        }
        return null;
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // com.lumiyaviewer.lumiya.ui.common.ConnectedActivity
    public boolean handleBackPressed() {
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        Fragment findFragmentById = supportFragmentManager.findFragmentById(R.id.details);
        if ((findFragmentById instanceof BackButtonHandler) && findFragmentById.isAdded() && (!findFragmentById.isDetached()) && ((BackButtonHandler) findFragmentById).onBackButtonPressed()) {
            return true;
        }
        if (supportFragmentManager.getBackStackEntryCount() != 0) {
            return false;
        }
        return goBack(supportFragmentManager);
    }

    protected boolean isRootDetailsFragment(Class<? extends Fragment> cls) {
        return true;
    }

    @Override // com.lumiyaviewer.lumiya.ui.common.ConnectedActivity, com.lumiyaviewer.lumiya.ui.common.ThemedActivity, android.support.v7.app.AppCompatActivity, android.support.v4.app.FragmentActivity, android.support.v4.app.SupportActivity, android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (bundle != null) {
            ArrayList parcelableArrayList = bundle.getParcelableArrayList(DETAILS_STACK_TAG);
            if (parcelableArrayList != null) {
                this.detailsStack.addAll(parcelableArrayList);
            }
            this.defaultTitle = bundle.getString(DEFAULT_TITLE_TAG);
            this.defaultSubTitle = bundle.getString(DEFAULT_SUBTITLE_TAG);
        }
    }

    protected boolean onDetailsStackEmpty() {
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        Fragment findFragmentById = supportFragmentManager.findFragmentById(R.id.details);
        if (findFragmentById == null) {
            return true;
        }
        FragmentTransaction beginTransaction = supportFragmentManager.beginTransaction();
        beginTransaction.remove(findFragmentById);
        beginTransaction.commit();
        updateTitle();
        return false;
    }

    public void onFragmentTitleUpdated() {
        updateTitle();
    }

    @Override // com.lumiyaviewer.lumiya.ui.common.ConnectedActivity, android.support.v7.app.AppCompatActivity, android.app.Activity
    protected void onPostCreate(@android.support.annotation.Nullable Bundle bundle) {
        super.onPostCreate(bundle);
        updateTitle();
    }

    @Override // android.support.v4.app.FragmentActivity, android.app.Activity, android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback
    public void onRequestPermissionsResult(int i, @NonNull String[] strArr, @NonNull int[] iArr) {
        super.onRequestPermissionsResult(i, strArr, iArr);
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments != null) {
            Iterator it = fragments.iterator();
            while (it.hasNext()) {
                ((Fragment) it.next()).onRequestPermissionsResult(i, strArr, iArr);
            }
        }
    }

    @Override // com.lumiyaviewer.lumiya.ui.common.ConnectedActivity, android.support.v7.app.AppCompatActivity, android.support.v4.app.FragmentActivity, android.support.v4.app.SupportActivity, android.app.Activity
    protected void onSaveInstanceState(Bundle bundle) {
        bundle.putParcelableArrayList(DETAILS_STACK_TAG, this.detailsStack);
        bundle.putString(DEFAULT_TITLE_TAG, this.defaultTitle);
        bundle.putString(DEFAULT_SUBTITLE_TAG, this.defaultSubTitle);
        super.onSaveInstanceState(bundle);
    }

    protected void removeAllDetails() {
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        if (supportFragmentManager.findFragmentById(R.id.details) != null) {
            clearDetailsStack();
            goBack(supportFragmentManager);
        }
    }

    protected void replaceDetailsFragment(FragmentManager fragmentManager, Fragment fragment) {
        FragmentTransaction beginTransaction = fragmentManager.beginTransaction();
        beginTransaction.setCustomAnimations(R.anim.slide_from_right, 0, 0, R.anim.slide_to_right);
        beginTransaction.replace(R.id.details, fragment);
        beginTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        beginTransaction.commit();
        updateTitle();
    }

    protected void setActivityTitle(@Nullable String str, @Nullable String str2) {
        ActionBar supportActionBar = getSupportActionBar();
        Debug.Printf("updateTitle: title '%s' actionBar %s", str, supportActionBar);
        if (supportActionBar != null) {
            supportActionBar.setTitle(str);
            supportActionBar.setSubtitle(str2);
        }
        setTitle(str);
    }

    /* JADX WARN: Multi-variable type inference failed */
    public boolean setCurrentDetailsArguments(Class<? extends Fragment> cls, Bundle bundle) {
        Fragment findFragmentById;
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        if (supportFragmentManager == null || (findFragmentById = supportFragmentManager.findFragmentById(R.id.details)) == null || !cls.isInstance(findFragmentById) || !(findFragmentById instanceof ReloadableFragment) || findFragmentById.getArguments() == null) {
            return false;
        }
        ((ReloadableFragment) findFragmentById).setFragmentArgs(getIntent(), bundle);
        return true;
    }

    public void setDefaultTitle(@Nullable String str, @Nullable String str2) {
        this.defaultTitle = str;
        this.defaultSubTitle = str2;
        updateTitle();
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Nullable
    public Fragment showDetailsFragment(Class<? extends Fragment> cls, Intent intent, Bundle bundle) {
        Fragment newInstance;
        Debug.Printf("DetailsActivity: fragmentClass %s, intent %s, arguments %s", cls.toString(), intent, bundle);
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        if (supportFragmentManager == null) {
            return null;
        }
        boolean isRootDetailsFragment = isRootDetailsFragment(cls);
        Fragment findFragmentById = supportFragmentManager.findFragmentById(R.id.details);
        Debug.Printf("DetailsActivity: isRootFragment %b existing fragment: %s", Boolean.valueOf(isRootDetailsFragment), findFragmentById);
        if (findFragmentById != null) {
            Debug.Printf("DetailsActivity: is good instance: %b", Boolean.valueOf(cls.isInstance(findFragmentById)));
            Debug.Printf("DetailsActivity: is reloadable: %b", Boolean.valueOf(findFragmentById instanceof ReloadableFragment));
            Debug.Printf("DetailsActivity: has arguments: %b", findFragmentById.getArguments());
        }
        if (findFragmentById != null && findFragmentById.isVisible() && cls.isInstance(findFragmentById) && (findFragmentById instanceof ReloadableFragment) && findFragmentById.getArguments() != null) {
            ((ReloadableFragment) findFragmentById).setFragmentArgs(intent, bundle);
            invalidateOptionsMenu();
            return findFragmentById;
        }
        if (isRootDetailsFragment) {
            clearDetailsStack();
        } else {
            addDetailsToStack(supportFragmentManager);
        }
        try {
            newInstance = cls.newInstance();
        } catch (Exception e) {
            Debug.Warning(e);
            return null;
        }
        try {
            if (newInstance instanceof ReloadableFragment) {
                newInstance.setArguments(new Bundle());
                ((ReloadableFragment) newInstance).setFragmentArgs(intent, bundle);
            } else {
                newInstance.setArguments(bundle);
            }
            replaceDetailsFragment(supportFragmentManager, newInstance);
            return newInstance;
        } catch (Exception e2) {
            findFragmentById = newInstance;
            Exception e = e2;
            Debug.Warning(e);
            return findFragmentById;
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Removed duplicated region for block: B:15:0x007c  */
    /* JADX WARN: Removed duplicated region for block: B:18:? A[RETURN, SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    protected void updateTitle() {
        boolean z = false;
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        if (supportFragmentManager != null) {
            Fragment findFragmentById = supportFragmentManager.findFragmentById(R.id.details);
            Debug.Printf("updateTitle: detailsFragment %s", findFragmentById);
            if (findFragmentById instanceof FragmentHasTitle) {
                Debug.Printf("updateTitle: detailsFragment added %b hidden %b detached %b", Boolean.valueOf(findFragmentById.isAdded()), Boolean.valueOf(findFragmentById.isHidden()), Boolean.valueOf(findFragmentById.isDetached()));
                if (findFragmentById.isAdded() && !findFragmentById.isHidden() && !findFragmentById.isDetached()) {
                    String title = ((FragmentHasTitle) findFragmentById).getTitle();
                    String subTitle = ((FragmentHasTitle) findFragmentById).getSubTitle();
                    Debug.Printf("updateTitle: got title '%s', subtitle '%s'", title, subTitle);
                    if (title != null) {
                        setActivityTitle(title, subTitle);
                        z = true;
                    }
                }
            }
        }
        if (!z) {
            updateTitleNoDetails();
        }
    }

    protected void updateTitleNoDetails() {
        if (this.defaultTitle != null) {
            setActivityTitle(this.defaultTitle, this.defaultSubTitle);
        }
    }
}
