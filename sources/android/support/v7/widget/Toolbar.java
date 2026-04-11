package android.support.v7.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.MenuRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.annotation.StringRes;
import android.support.annotation.StyleRes;
import android.support.v4.view.AbsSavedState;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MarginLayoutParamsCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.appcompat.R;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.view.CollapsibleActionView;
import android.support.v7.view.SupportMenuInflater;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuItemImpl;
import android.support.v7.view.menu.MenuPresenter;
import android.support.v7.view.menu.MenuView;
import android.support.v7.view.menu.SubMenuBuilder;
import android.support.v7.widget.ActionMenuView;
import android.text.Layout;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

/* JADX INFO: loaded from: classes.dex */
public class Toolbar extends ViewGroup {
    private static final String TAG = "Toolbar";
    private MenuPresenter.Callback mActionMenuPresenterCallback;
    int mButtonGravity;
    ImageButton mCollapseButtonView;
    private CharSequence mCollapseDescription;
    private Drawable mCollapseIcon;
    private boolean mCollapsible;
    private int mContentInsetEndWithActions;
    private int mContentInsetStartWithNavigation;
    private RtlSpacingHelper mContentInsets;
    private boolean mEatingHover;
    private boolean mEatingTouch;
    View mExpandedActionView;
    private ExpandedActionViewMenuPresenter mExpandedMenuPresenter;
    private int mGravity;
    private final ArrayList<View> mHiddenViews;
    private ImageView mLogoView;
    private int mMaxButtonHeight;
    private MenuBuilder.Callback mMenuBuilderCallback;
    private ActionMenuView mMenuView;
    private final ActionMenuView.OnMenuItemClickListener mMenuViewItemClickListener;
    private ImageButton mNavButtonView;
    OnMenuItemClickListener mOnMenuItemClickListener;
    private ActionMenuPresenter mOuterActionMenuPresenter;
    private Context mPopupContext;
    private int mPopupTheme;
    private final Runnable mShowOverflowMenuRunnable;
    private CharSequence mSubtitleText;
    private int mSubtitleTextAppearance;
    private int mSubtitleTextColor;
    private TextView mSubtitleTextView;
    private final int[] mTempMargins;
    private final ArrayList<View> mTempViews;
    private int mTitleMarginBottom;
    private int mTitleMarginEnd;
    private int mTitleMarginStart;
    private int mTitleMarginTop;
    private CharSequence mTitleText;
    private int mTitleTextAppearance;
    private int mTitleTextColor;
    private TextView mTitleTextView;
    private ToolbarWidgetWrapper mWrapper;

    private class ExpandedActionViewMenuPresenter implements MenuPresenter {
        MenuItemImpl mCurrentExpandedItem;
        MenuBuilder mMenu;

        ExpandedActionViewMenuPresenter() {
        }

        @Override // android.support.v7.view.menu.MenuPresenter
        public boolean collapseItemActionView(MenuBuilder menuBuilder, MenuItemImpl menuItemImpl) {
            if (Toolbar.this.mExpandedActionView instanceof CollapsibleActionView) {
                ((CollapsibleActionView) Toolbar.this.mExpandedActionView).onActionViewCollapsed();
            }
            Toolbar.this.removeView(Toolbar.this.mExpandedActionView);
            Toolbar.this.removeView(Toolbar.this.mCollapseButtonView);
            Toolbar.this.mExpandedActionView = null;
            Toolbar.this.addChildrenForExpandedActionView();
            this.mCurrentExpandedItem = null;
            Toolbar.this.requestLayout();
            menuItemImpl.setActionViewExpanded(false);
            return true;
        }

        @Override // android.support.v7.view.menu.MenuPresenter
        public boolean expandItemActionView(MenuBuilder menuBuilder, MenuItemImpl menuItemImpl) {
            Toolbar.this.ensureCollapseButtonView();
            if (Toolbar.this.mCollapseButtonView.getParent() != Toolbar.this) {
                Toolbar.this.addView(Toolbar.this.mCollapseButtonView);
            }
            Toolbar.this.mExpandedActionView = menuItemImpl.getActionView();
            this.mCurrentExpandedItem = menuItemImpl;
            if (Toolbar.this.mExpandedActionView.getParent() != Toolbar.this) {
                LayoutParams layoutParamsGenerateDefaultLayoutParams = Toolbar.this.generateDefaultLayoutParams();
                layoutParamsGenerateDefaultLayoutParams.gravity = (Toolbar.this.mButtonGravity & 112) | GravityCompat.START;
                layoutParamsGenerateDefaultLayoutParams.mViewType = 2;
                Toolbar.this.mExpandedActionView.setLayoutParams(layoutParamsGenerateDefaultLayoutParams);
                Toolbar.this.addView(Toolbar.this.mExpandedActionView);
            }
            Toolbar.this.removeChildrenForExpandedActionView();
            Toolbar.this.requestLayout();
            menuItemImpl.setActionViewExpanded(true);
            if (Toolbar.this.mExpandedActionView instanceof CollapsibleActionView) {
                ((CollapsibleActionView) Toolbar.this.mExpandedActionView).onActionViewExpanded();
            }
            return true;
        }

        @Override // android.support.v7.view.menu.MenuPresenter
        public boolean flagActionItems() {
            return false;
        }

        @Override // android.support.v7.view.menu.MenuPresenter
        public int getId() {
            return 0;
        }

        @Override // android.support.v7.view.menu.MenuPresenter
        public MenuView getMenuView(ViewGroup viewGroup) {
            return null;
        }

        @Override // android.support.v7.view.menu.MenuPresenter
        public void initForMenu(Context context, MenuBuilder menuBuilder) {
            if (this.mMenu != null && this.mCurrentExpandedItem != null) {
                this.mMenu.collapseItemActionView(this.mCurrentExpandedItem);
            }
            this.mMenu = menuBuilder;
        }

        @Override // android.support.v7.view.menu.MenuPresenter
        public void onCloseMenu(MenuBuilder menuBuilder, boolean z) {
        }

        @Override // android.support.v7.view.menu.MenuPresenter
        public void onRestoreInstanceState(Parcelable parcelable) {
        }

        @Override // android.support.v7.view.menu.MenuPresenter
        public Parcelable onSaveInstanceState() {
            return null;
        }

        @Override // android.support.v7.view.menu.MenuPresenter
        public boolean onSubMenuSelected(SubMenuBuilder subMenuBuilder) {
            return false;
        }

        @Override // android.support.v7.view.menu.MenuPresenter
        public void setCallback(MenuPresenter.Callback callback) {
        }

        @Override // android.support.v7.view.menu.MenuPresenter
        public void updateMenuView(boolean z) {
            boolean z2 = false;
            if (this.mCurrentExpandedItem == null) {
                return;
            }
            if (this.mMenu != null) {
                int size = this.mMenu.size();
                int i = 0;
                while (true) {
                    if (i < size) {
                        if (this.mMenu.getItem(i) == this.mCurrentExpandedItem) {
                            z2 = true;
                            break;
                        }
                        i++;
                    } else {
                        break;
                    }
                }
            }
            if (z2) {
                return;
            }
            collapseItemActionView(this.mMenu, this.mCurrentExpandedItem);
        }
    }

    public static class LayoutParams extends ActionBar.LayoutParams {
        static final int CUSTOM = 0;
        static final int EXPANDED = 2;
        static final int SYSTEM = 1;
        int mViewType;

        public LayoutParams(int i) {
            this(-2, -1, i);
        }

        public LayoutParams(int i, int i2) {
            super(i, i2);
            this.mViewType = 0;
            this.gravity = 8388627;
        }

        public LayoutParams(int i, int i2, int i3) {
            super(i, i2);
            this.mViewType = 0;
            this.gravity = i3;
        }

        public LayoutParams(@NonNull Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
            this.mViewType = 0;
        }

        public LayoutParams(ActionBar.LayoutParams layoutParams) {
            super(layoutParams);
            this.mViewType = 0;
        }

        public LayoutParams(LayoutParams layoutParams) {
            super((ActionBar.LayoutParams) layoutParams);
            this.mViewType = 0;
            this.mViewType = layoutParams.mViewType;
        }

        public LayoutParams(ViewGroup.LayoutParams layoutParams) {
            super(layoutParams);
            this.mViewType = 0;
        }

        public LayoutParams(ViewGroup.MarginLayoutParams marginLayoutParams) {
            super(marginLayoutParams);
            this.mViewType = 0;
            copyMarginsFromCompat(marginLayoutParams);
        }

        void copyMarginsFromCompat(ViewGroup.MarginLayoutParams marginLayoutParams) {
            this.leftMargin = marginLayoutParams.leftMargin;
            this.topMargin = marginLayoutParams.topMargin;
            this.rightMargin = marginLayoutParams.rightMargin;
            this.bottomMargin = marginLayoutParams.bottomMargin;
        }
    }

    public interface OnMenuItemClickListener {
        boolean onMenuItemClick(MenuItem menuItem);
    }

    public static class SavedState extends AbsSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.ClassLoaderCreator<SavedState>() { // from class: android.support.v7.widget.Toolbar.SavedState.1
            @Override // android.os.Parcelable.Creator
            public SavedState createFromParcel(Parcel parcel) {
                return new SavedState(parcel, null);
            }

            /* JADX WARN: Can't rename method to resolve collision */
            @Override // android.os.Parcelable.ClassLoaderCreator
            public SavedState createFromParcel(Parcel parcel, ClassLoader classLoader) {
                return new SavedState(parcel, classLoader);
            }

            @Override // android.os.Parcelable.Creator
            public SavedState[] newArray(int i) {
                return new SavedState[i];
            }
        };
        int expandedMenuItemId;
        boolean isOverflowOpen;

        public SavedState(Parcel parcel) {
            this(parcel, null);
        }

        public SavedState(Parcel parcel, ClassLoader classLoader) {
            super(parcel, classLoader);
            this.expandedMenuItemId = parcel.readInt();
            this.isOverflowOpen = parcel.readInt() != 0;
        }

        public SavedState(Parcelable parcelable) {
            super(parcelable);
        }

        @Override // android.support.v4.view.AbsSavedState, android.os.Parcelable
        public void writeToParcel(Parcel parcel, int i) {
            super.writeToParcel(parcel, i);
            parcel.writeInt(this.expandedMenuItemId);
            parcel.writeInt(this.isOverflowOpen ? 1 : 0);
        }
    }

    public Toolbar(Context context) {
        this(context, null);
    }

    public Toolbar(Context context, @Nullable AttributeSet attributeSet) {
        this(context, attributeSet, R.attr.toolbarStyle);
    }

    public Toolbar(Context context, @Nullable AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mGravity = 8388627;
        this.mTempViews = new ArrayList<>();
        this.mHiddenViews = new ArrayList<>();
        this.mTempMargins = new int[2];
        this.mMenuViewItemClickListener = new ActionMenuView.OnMenuItemClickListener() { // from class: android.support.v7.widget.Toolbar.1
            @Override // android.support.v7.widget.ActionMenuView.OnMenuItemClickListener
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (Toolbar.this.mOnMenuItemClickListener == null) {
                    return false;
                }
                return Toolbar.this.mOnMenuItemClickListener.onMenuItemClick(menuItem);
            }
        };
        this.mShowOverflowMenuRunnable = new Runnable() { // from class: android.support.v7.widget.Toolbar.2
            @Override // java.lang.Runnable
            public void run() {
                Toolbar.this.showOverflowMenu();
            }
        };
        TintTypedArray tintTypedArrayObtainStyledAttributes = TintTypedArray.obtainStyledAttributes(getContext(), attributeSet, R.styleable.Toolbar, i, 0);
        this.mTitleTextAppearance = tintTypedArrayObtainStyledAttributes.getResourceId(R.styleable.Toolbar_titleTextAppearance, 0);
        this.mSubtitleTextAppearance = tintTypedArrayObtainStyledAttributes.getResourceId(R.styleable.Toolbar_subtitleTextAppearance, 0);
        this.mGravity = tintTypedArrayObtainStyledAttributes.getInteger(R.styleable.Toolbar_android_gravity, this.mGravity);
        this.mButtonGravity = tintTypedArrayObtainStyledAttributes.getInteger(R.styleable.Toolbar_buttonGravity, 48);
        int dimensionPixelOffset = tintTypedArrayObtainStyledAttributes.getDimensionPixelOffset(R.styleable.Toolbar_titleMargin, 0);
        dimensionPixelOffset = tintTypedArrayObtainStyledAttributes.hasValue(R.styleable.Toolbar_titleMargins) ? tintTypedArrayObtainStyledAttributes.getDimensionPixelOffset(R.styleable.Toolbar_titleMargins, dimensionPixelOffset) : dimensionPixelOffset;
        this.mTitleMarginBottom = dimensionPixelOffset;
        this.mTitleMarginTop = dimensionPixelOffset;
        this.mTitleMarginEnd = dimensionPixelOffset;
        this.mTitleMarginStart = dimensionPixelOffset;
        int dimensionPixelOffset2 = tintTypedArrayObtainStyledAttributes.getDimensionPixelOffset(R.styleable.Toolbar_titleMarginStart, -1);
        if (dimensionPixelOffset2 >= 0) {
            this.mTitleMarginStart = dimensionPixelOffset2;
        }
        int dimensionPixelOffset3 = tintTypedArrayObtainStyledAttributes.getDimensionPixelOffset(R.styleable.Toolbar_titleMarginEnd, -1);
        if (dimensionPixelOffset3 >= 0) {
            this.mTitleMarginEnd = dimensionPixelOffset3;
        }
        int dimensionPixelOffset4 = tintTypedArrayObtainStyledAttributes.getDimensionPixelOffset(R.styleable.Toolbar_titleMarginTop, -1);
        if (dimensionPixelOffset4 >= 0) {
            this.mTitleMarginTop = dimensionPixelOffset4;
        }
        int dimensionPixelOffset5 = tintTypedArrayObtainStyledAttributes.getDimensionPixelOffset(R.styleable.Toolbar_titleMarginBottom, -1);
        if (dimensionPixelOffset5 >= 0) {
            this.mTitleMarginBottom = dimensionPixelOffset5;
        }
        this.mMaxButtonHeight = tintTypedArrayObtainStyledAttributes.getDimensionPixelSize(R.styleable.Toolbar_maxButtonHeight, -1);
        int dimensionPixelOffset6 = tintTypedArrayObtainStyledAttributes.getDimensionPixelOffset(R.styleable.Toolbar_contentInsetStart, Integer.MIN_VALUE);
        int dimensionPixelOffset7 = tintTypedArrayObtainStyledAttributes.getDimensionPixelOffset(R.styleable.Toolbar_contentInsetEnd, Integer.MIN_VALUE);
        int dimensionPixelSize = tintTypedArrayObtainStyledAttributes.getDimensionPixelSize(R.styleable.Toolbar_contentInsetLeft, 0);
        int dimensionPixelSize2 = tintTypedArrayObtainStyledAttributes.getDimensionPixelSize(R.styleable.Toolbar_contentInsetRight, 0);
        ensureContentInsets();
        this.mContentInsets.setAbsolute(dimensionPixelSize, dimensionPixelSize2);
        if (dimensionPixelOffset6 != Integer.MIN_VALUE || dimensionPixelOffset7 != Integer.MIN_VALUE) {
            this.mContentInsets.setRelative(dimensionPixelOffset6, dimensionPixelOffset7);
        }
        this.mContentInsetStartWithNavigation = tintTypedArrayObtainStyledAttributes.getDimensionPixelOffset(R.styleable.Toolbar_contentInsetStartWithNavigation, Integer.MIN_VALUE);
        this.mContentInsetEndWithActions = tintTypedArrayObtainStyledAttributes.getDimensionPixelOffset(R.styleable.Toolbar_contentInsetEndWithActions, Integer.MIN_VALUE);
        this.mCollapseIcon = tintTypedArrayObtainStyledAttributes.getDrawable(R.styleable.Toolbar_collapseIcon);
        this.mCollapseDescription = tintTypedArrayObtainStyledAttributes.getText(R.styleable.Toolbar_collapseContentDescription);
        CharSequence text = tintTypedArrayObtainStyledAttributes.getText(R.styleable.Toolbar_title);
        if (!TextUtils.isEmpty(text)) {
            setTitle(text);
        }
        CharSequence text2 = tintTypedArrayObtainStyledAttributes.getText(R.styleable.Toolbar_subtitle);
        if (!TextUtils.isEmpty(text2)) {
            setSubtitle(text2);
        }
        this.mPopupContext = getContext();
        setPopupTheme(tintTypedArrayObtainStyledAttributes.getResourceId(R.styleable.Toolbar_popupTheme, 0));
        Drawable drawable = tintTypedArrayObtainStyledAttributes.getDrawable(R.styleable.Toolbar_navigationIcon);
        if (drawable != null) {
            setNavigationIcon(drawable);
        }
        CharSequence text3 = tintTypedArrayObtainStyledAttributes.getText(R.styleable.Toolbar_navigationContentDescription);
        if (!TextUtils.isEmpty(text3)) {
            setNavigationContentDescription(text3);
        }
        Drawable drawable2 = tintTypedArrayObtainStyledAttributes.getDrawable(R.styleable.Toolbar_logo);
        if (drawable2 != null) {
            setLogo(drawable2);
        }
        CharSequence text4 = tintTypedArrayObtainStyledAttributes.getText(R.styleable.Toolbar_logoDescription);
        if (!TextUtils.isEmpty(text4)) {
            setLogoDescription(text4);
        }
        if (tintTypedArrayObtainStyledAttributes.hasValue(R.styleable.Toolbar_titleTextColor)) {
            setTitleTextColor(tintTypedArrayObtainStyledAttributes.getColor(R.styleable.Toolbar_titleTextColor, -1));
        }
        if (tintTypedArrayObtainStyledAttributes.hasValue(R.styleable.Toolbar_subtitleTextColor)) {
            setSubtitleTextColor(tintTypedArrayObtainStyledAttributes.getColor(R.styleable.Toolbar_subtitleTextColor, -1));
        }
        tintTypedArrayObtainStyledAttributes.recycle();
    }

    private void addCustomViewsWithGravity(List<View> list, int i) {
        boolean z = ViewCompat.getLayoutDirection(this) == 1;
        int childCount = getChildCount();
        int absoluteGravity = GravityCompat.getAbsoluteGravity(i, ViewCompat.getLayoutDirection(this));
        list.clear();
        if (!z) {
            for (int i2 = 0; i2 < childCount; i2++) {
                View childAt = getChildAt(i2);
                LayoutParams layoutParams = (LayoutParams) childAt.getLayoutParams();
                if (layoutParams.mViewType == 0 && shouldLayout(childAt) && getChildHorizontalGravity(layoutParams.gravity) == absoluteGravity) {
                    list.add(childAt);
                }
            }
            return;
        }
        for (int i3 = childCount - 1; i3 >= 0; i3--) {
            View childAt2 = getChildAt(i3);
            LayoutParams layoutParams2 = (LayoutParams) childAt2.getLayoutParams();
            if (layoutParams2.mViewType == 0 && shouldLayout(childAt2) && getChildHorizontalGravity(layoutParams2.gravity) == absoluteGravity) {
                list.add(childAt2);
            }
        }
    }

    private void addSystemView(View view, boolean z) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        LayoutParams layoutParamsGenerateLayoutParams = layoutParams != null ? checkLayoutParams(layoutParams) ? (LayoutParams) layoutParams : generateLayoutParams(layoutParams) : generateDefaultLayoutParams();
        layoutParamsGenerateLayoutParams.mViewType = 1;
        if (!z || this.mExpandedActionView == null) {
            addView(view, layoutParamsGenerateLayoutParams);
        } else {
            view.setLayoutParams(layoutParamsGenerateLayoutParams);
            this.mHiddenViews.add(view);
        }
    }

    private void ensureContentInsets() {
        if (this.mContentInsets != null) {
            return;
        }
        this.mContentInsets = new RtlSpacingHelper();
    }

    private void ensureLogoView() {
        if (this.mLogoView != null) {
            return;
        }
        this.mLogoView = new AppCompatImageView(getContext());
    }

    private void ensureMenu() {
        ensureMenuView();
        if (this.mMenuView.peekMenu() != null) {
            return;
        }
        MenuBuilder menuBuilder = (MenuBuilder) this.mMenuView.getMenu();
        if (this.mExpandedMenuPresenter == null) {
            this.mExpandedMenuPresenter = new ExpandedActionViewMenuPresenter();
        }
        this.mMenuView.setExpandedActionViewsExclusive(true);
        menuBuilder.addMenuPresenter(this.mExpandedMenuPresenter, this.mPopupContext);
    }

    private void ensureMenuView() {
        if (this.mMenuView != null) {
            return;
        }
        this.mMenuView = new ActionMenuView(getContext());
        this.mMenuView.setPopupTheme(this.mPopupTheme);
        this.mMenuView.setOnMenuItemClickListener(this.mMenuViewItemClickListener);
        this.mMenuView.setMenuCallbacks(this.mActionMenuPresenterCallback, this.mMenuBuilderCallback);
        LayoutParams layoutParamsGenerateDefaultLayoutParams = generateDefaultLayoutParams();
        layoutParamsGenerateDefaultLayoutParams.gravity = (this.mButtonGravity & 112) | GravityCompat.END;
        this.mMenuView.setLayoutParams(layoutParamsGenerateDefaultLayoutParams);
        addSystemView(this.mMenuView, false);
    }

    private void ensureNavButtonView() {
        if (this.mNavButtonView != null) {
            return;
        }
        this.mNavButtonView = new AppCompatImageButton(getContext(), null, R.attr.toolbarNavigationButtonStyle);
        LayoutParams layoutParamsGenerateDefaultLayoutParams = generateDefaultLayoutParams();
        layoutParamsGenerateDefaultLayoutParams.gravity = (this.mButtonGravity & 112) | GravityCompat.START;
        this.mNavButtonView.setLayoutParams(layoutParamsGenerateDefaultLayoutParams);
    }

    private int getChildHorizontalGravity(int i) {
        int layoutDirection = ViewCompat.getLayoutDirection(this);
        int absoluteGravity = GravityCompat.getAbsoluteGravity(i, layoutDirection) & 7;
        switch (absoluteGravity) {
            case 1:
            case 3:
            case 5:
                return absoluteGravity;
            case 2:
            case 4:
            default:
                return layoutDirection != 1 ? 3 : 5;
        }
    }

    private int getChildTop(View view, int i) {
        int iMax;
        LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        int measuredHeight = view.getMeasuredHeight();
        int i2 = i <= 0 ? 0 : (measuredHeight - i) / 2;
        switch (getChildVerticalGravity(layoutParams.gravity)) {
            case 16:
            default:
                int paddingTop = getPaddingTop();
                int paddingBottom = getPaddingBottom();
                int height = getHeight();
                int i3 = (((height - paddingTop) - paddingBottom) - measuredHeight) / 2;
                if (i3 >= layoutParams.topMargin) {
                    int i4 = (((height - paddingBottom) - measuredHeight) - i3) - paddingTop;
                    iMax = i4 >= layoutParams.bottomMargin ? i3 : Math.max(0, i3 - (layoutParams.bottomMargin - i4));
                } else {
                    iMax = layoutParams.topMargin;
                }
                return iMax + paddingTop;
            case 48:
                return getPaddingTop() - i2;
            case 80:
                return (((getHeight() - getPaddingBottom()) - measuredHeight) - layoutParams.bottomMargin) - i2;
        }
    }

    private int getChildVerticalGravity(int i) {
        int i2 = i & 112;
        switch (i2) {
            case 16:
            case 48:
            case 80:
                return i2;
            default:
                return this.mGravity & 112;
        }
    }

    private int getHorizontalMargins(View view) {
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        return MarginLayoutParamsCompat.getMarginEnd(marginLayoutParams) + MarginLayoutParamsCompat.getMarginStart(marginLayoutParams);
    }

    private MenuInflater getMenuInflater() {
        return new SupportMenuInflater(getContext());
    }

    private int getVerticalMargins(View view) {
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        return marginLayoutParams.bottomMargin + marginLayoutParams.topMargin;
    }

    private int getViewListMeasuredWidth(List<View> list, int[] iArr) {
        int i = iArr[0];
        int i2 = iArr[1];
        int size = list.size();
        int i3 = 0;
        int measuredWidth = 0;
        int iMax = i2;
        int iMax2 = i;
        while (i3 < size) {
            View view = list.get(i3);
            LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
            int i4 = layoutParams.leftMargin - iMax2;
            int i5 = layoutParams.rightMargin - iMax;
            int iMax3 = Math.max(0, i4);
            int iMax4 = Math.max(0, i5);
            iMax2 = Math.max(0, -i4);
            iMax = Math.max(0, -i5);
            i3++;
            measuredWidth += view.getMeasuredWidth() + iMax3 + iMax4;
        }
        return measuredWidth;
    }

    private boolean isChildOrHidden(View view) {
        return view.getParent() == this || this.mHiddenViews.contains(view);
    }

    private static boolean isCustomView(View view) {
        return ((LayoutParams) view.getLayoutParams()).mViewType == 0;
    }

    private int layoutChildLeft(View view, int i, int[] iArr, int i2) {
        LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        int i3 = layoutParams.leftMargin - iArr[0];
        int iMax = Math.max(0, i3) + i;
        iArr[0] = Math.max(0, -i3);
        int childTop = getChildTop(view, i2);
        int measuredWidth = view.getMeasuredWidth();
        view.layout(iMax, childTop, iMax + measuredWidth, view.getMeasuredHeight() + childTop);
        return layoutParams.rightMargin + measuredWidth + iMax;
    }

    private int layoutChildRight(View view, int i, int[] iArr, int i2) {
        LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        int i3 = layoutParams.rightMargin - iArr[1];
        int iMax = i - Math.max(0, i3);
        iArr[1] = Math.max(0, -i3);
        int childTop = getChildTop(view, i2);
        int measuredWidth = view.getMeasuredWidth();
        view.layout(iMax - measuredWidth, childTop, iMax, view.getMeasuredHeight() + childTop);
        return iMax - (layoutParams.leftMargin + measuredWidth);
    }

    private int measureChildCollapseMargins(View view, int i, int i2, int i3, int i4, int[] iArr) {
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        int i5 = marginLayoutParams.leftMargin - iArr[0];
        int i6 = marginLayoutParams.rightMargin - iArr[1];
        int iMax = Math.max(0, i5) + Math.max(0, i6);
        iArr[0] = Math.max(0, -i5);
        iArr[1] = Math.max(0, -i6);
        view.measure(getChildMeasureSpec(i, getPaddingLeft() + getPaddingRight() + iMax + i2, marginLayoutParams.width), getChildMeasureSpec(i3, getPaddingTop() + getPaddingBottom() + marginLayoutParams.topMargin + marginLayoutParams.bottomMargin + i4, marginLayoutParams.height));
        return view.getMeasuredWidth() + iMax;
    }

    private void measureChildConstrained(View view, int i, int i2, int i3, int i4, int i5) {
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        int childMeasureSpec = getChildMeasureSpec(i, getPaddingLeft() + getPaddingRight() + marginLayoutParams.leftMargin + marginLayoutParams.rightMargin + i2, marginLayoutParams.width);
        int childMeasureSpec2 = getChildMeasureSpec(i3, getPaddingTop() + getPaddingBottom() + marginLayoutParams.topMargin + marginLayoutParams.bottomMargin + i4, marginLayoutParams.height);
        int mode = View.MeasureSpec.getMode(childMeasureSpec2);
        if (mode != 1073741824 && i5 >= 0) {
            if (mode != 0) {
                i5 = Math.min(View.MeasureSpec.getSize(childMeasureSpec2), i5);
            }
            childMeasureSpec2 = View.MeasureSpec.makeMeasureSpec(i5, 1073741824);
        }
        view.measure(childMeasureSpec, childMeasureSpec2);
    }

    private void postShowOverflowMenu() {
        removeCallbacks(this.mShowOverflowMenuRunnable);
        post(this.mShowOverflowMenuRunnable);
    }

    private boolean shouldCollapse() {
        if (!this.mCollapsible) {
            return false;
        }
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = getChildAt(i);
            if (shouldLayout(childAt) && childAt.getMeasuredWidth() > 0 && childAt.getMeasuredHeight() > 0) {
                return false;
            }
        }
        return true;
    }

    private boolean shouldLayout(View view) {
        return (view == null || view.getParent() != this || view.getVisibility() == 8) ? false : true;
    }

    void addChildrenForExpandedActionView() {
        for (int size = this.mHiddenViews.size() - 1; size >= 0; size--) {
            addView(this.mHiddenViews.get(size));
        }
        this.mHiddenViews.clear();
    }

    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
    public boolean canShowOverflowMenu() {
        return getVisibility() == 0 && this.mMenuView != null && this.mMenuView.isOverflowReserved();
    }

    @Override // android.view.ViewGroup
    protected boolean checkLayoutParams(ViewGroup.LayoutParams layoutParams) {
        return super.checkLayoutParams(layoutParams) && (layoutParams instanceof LayoutParams);
    }

    public void collapseActionView() {
        MenuItemImpl menuItemImpl = this.mExpandedMenuPresenter != null ? this.mExpandedMenuPresenter.mCurrentExpandedItem : null;
        if (menuItemImpl == null) {
            return;
        }
        menuItemImpl.collapseActionView();
    }

    public void dismissPopupMenus() {
        if (this.mMenuView == null) {
            return;
        }
        this.mMenuView.dismissPopupMenus();
    }

    void ensureCollapseButtonView() {
        if (this.mCollapseButtonView != null) {
            return;
        }
        this.mCollapseButtonView = new AppCompatImageButton(getContext(), null, R.attr.toolbarNavigationButtonStyle);
        this.mCollapseButtonView.setImageDrawable(this.mCollapseIcon);
        this.mCollapseButtonView.setContentDescription(this.mCollapseDescription);
        LayoutParams layoutParamsGenerateDefaultLayoutParams = generateDefaultLayoutParams();
        layoutParamsGenerateDefaultLayoutParams.gravity = (this.mButtonGravity & 112) | GravityCompat.START;
        layoutParamsGenerateDefaultLayoutParams.mViewType = 2;
        this.mCollapseButtonView.setLayoutParams(layoutParamsGenerateDefaultLayoutParams);
        this.mCollapseButtonView.setOnClickListener(new View.OnClickListener() { // from class: android.support.v7.widget.Toolbar.3
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                Toolbar.this.collapseActionView();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.view.ViewGroup
    public LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(-2, -2);
    }

    @Override // android.view.ViewGroup
    public LayoutParams generateLayoutParams(AttributeSet attributeSet) {
        return new LayoutParams(getContext(), attributeSet);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.view.ViewGroup
    public LayoutParams generateLayoutParams(ViewGroup.LayoutParams layoutParams) {
        return !(layoutParams instanceof LayoutParams) ? !(layoutParams instanceof ActionBar.LayoutParams) ? !(layoutParams instanceof ViewGroup.MarginLayoutParams) ? new LayoutParams(layoutParams) : new LayoutParams((ViewGroup.MarginLayoutParams) layoutParams) : new LayoutParams((ActionBar.LayoutParams) layoutParams) : new LayoutParams((LayoutParams) layoutParams);
    }

    public int getContentInsetEnd() {
        if (this.mContentInsets == null) {
            return 0;
        }
        return this.mContentInsets.getEnd();
    }

    public int getContentInsetEndWithActions() {
        return this.mContentInsetEndWithActions == Integer.MIN_VALUE ? getContentInsetEnd() : this.mContentInsetEndWithActions;
    }

    public int getContentInsetLeft() {
        if (this.mContentInsets == null) {
            return 0;
        }
        return this.mContentInsets.getLeft();
    }

    public int getContentInsetRight() {
        if (this.mContentInsets == null) {
            return 0;
        }
        return this.mContentInsets.getRight();
    }

    public int getContentInsetStart() {
        if (this.mContentInsets == null) {
            return 0;
        }
        return this.mContentInsets.getStart();
    }

    public int getContentInsetStartWithNavigation() {
        return this.mContentInsetStartWithNavigation == Integer.MIN_VALUE ? getContentInsetStart() : this.mContentInsetStartWithNavigation;
    }

    public int getCurrentContentInsetEnd() {
        MenuBuilder menuBuilderPeekMenu;
        boolean z = (this.mMenuView == null || (menuBuilderPeekMenu = this.mMenuView.peekMenu()) == null || !menuBuilderPeekMenu.hasVisibleItems()) ? false : true;
        return !z ? getContentInsetEnd() : Math.max(getContentInsetEnd(), Math.max(this.mContentInsetEndWithActions, 0));
    }

    public int getCurrentContentInsetLeft() {
        return ViewCompat.getLayoutDirection(this) != 1 ? getCurrentContentInsetStart() : getCurrentContentInsetEnd();
    }

    public int getCurrentContentInsetRight() {
        return ViewCompat.getLayoutDirection(this) != 1 ? getCurrentContentInsetEnd() : getCurrentContentInsetStart();
    }

    public int getCurrentContentInsetStart() {
        return getNavigationIcon() == null ? getContentInsetStart() : Math.max(getContentInsetStart(), Math.max(this.mContentInsetStartWithNavigation, 0));
    }

    public Drawable getLogo() {
        if (this.mLogoView == null) {
            return null;
        }
        return this.mLogoView.getDrawable();
    }

    public CharSequence getLogoDescription() {
        if (this.mLogoView == null) {
            return null;
        }
        return this.mLogoView.getContentDescription();
    }

    public Menu getMenu() {
        ensureMenu();
        return this.mMenuView.getMenu();
    }

    @Nullable
    public CharSequence getNavigationContentDescription() {
        if (this.mNavButtonView == null) {
            return null;
        }
        return this.mNavButtonView.getContentDescription();
    }

    @Nullable
    public Drawable getNavigationIcon() {
        if (this.mNavButtonView == null) {
            return null;
        }
        return this.mNavButtonView.getDrawable();
    }

    ActionMenuPresenter getOuterActionMenuPresenter() {
        return this.mOuterActionMenuPresenter;
    }

    @Nullable
    public Drawable getOverflowIcon() {
        ensureMenu();
        return this.mMenuView.getOverflowIcon();
    }

    Context getPopupContext() {
        return this.mPopupContext;
    }

    public int getPopupTheme() {
        return this.mPopupTheme;
    }

    public CharSequence getSubtitle() {
        return this.mSubtitleText;
    }

    public CharSequence getTitle() {
        return this.mTitleText;
    }

    public int getTitleMarginBottom() {
        return this.mTitleMarginBottom;
    }

    public int getTitleMarginEnd() {
        return this.mTitleMarginEnd;
    }

    public int getTitleMarginStart() {
        return this.mTitleMarginStart;
    }

    public int getTitleMarginTop() {
        return this.mTitleMarginTop;
    }

    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
    public DecorToolbar getWrapper() {
        if (this.mWrapper == null) {
            this.mWrapper = new ToolbarWidgetWrapper(this, true);
        }
        return this.mWrapper;
    }

    public boolean hasExpandedActionView() {
        return (this.mExpandedMenuPresenter == null || this.mExpandedMenuPresenter.mCurrentExpandedItem == null) ? false : true;
    }

    public boolean hideOverflowMenu() {
        return this.mMenuView != null && this.mMenuView.hideOverflowMenu();
    }

    public void inflateMenu(@MenuRes int i) {
        getMenuInflater().inflate(i, getMenu());
    }

    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
    public boolean isOverflowMenuShowPending() {
        return this.mMenuView != null && this.mMenuView.isOverflowMenuShowPending();
    }

    public boolean isOverflowMenuShowing() {
        return this.mMenuView != null && this.mMenuView.isOverflowMenuShowing();
    }

    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
    public boolean isTitleTruncated() {
        Layout layout;
        if (this.mTitleTextView == null || (layout = this.mTitleTextView.getLayout()) == null) {
            return false;
        }
        int lineCount = layout.getLineCount();
        for (int i = 0; i < lineCount; i++) {
            if (layout.getEllipsisCount(i) > 0) {
                return true;
            }
        }
        return false;
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeCallbacks(this.mShowOverflowMenuRunnable);
    }

    @Override // android.view.View
    public boolean onHoverEvent(MotionEvent motionEvent) {
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked == 9) {
            this.mEatingHover = false;
        }
        if (!this.mEatingHover) {
            boolean zOnHoverEvent = super.onHoverEvent(motionEvent);
            if (actionMasked == 9 && !zOnHoverEvent) {
                this.mEatingHover = true;
            }
        }
        if (actionMasked == 10 || actionMasked == 3) {
            this.mEatingHover = false;
        }
        return true;
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        int iLayoutChildRight;
        int iLayoutChildLeft;
        int iLayoutChildRight2;
        int iLayoutChildLeft2;
        int measuredHeight;
        int paddingTop;
        int i5;
        int i6;
        int i7;
        int i8;
        int i9;
        int iMax;
        boolean z2 = ViewCompat.getLayoutDirection(this) == 1;
        int width = getWidth();
        int height = getHeight();
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int paddingTop2 = getPaddingTop();
        int paddingBottom = getPaddingBottom();
        int i10 = width - paddingRight;
        int[] iArr = this.mTempMargins;
        iArr[1] = 0;
        iArr[0] = 0;
        int minimumHeight = ViewCompat.getMinimumHeight(this);
        int iMin = minimumHeight < 0 ? 0 : Math.min(minimumHeight, i4 - i2);
        if (!shouldLayout(this.mNavButtonView)) {
            iLayoutChildRight = i10;
            iLayoutChildLeft = paddingLeft;
        } else if (z2) {
            iLayoutChildRight = layoutChildRight(this.mNavButtonView, i10, iArr, iMin);
            iLayoutChildLeft = paddingLeft;
        } else {
            iLayoutChildLeft = layoutChildLeft(this.mNavButtonView, paddingLeft, iArr, iMin);
            iLayoutChildRight = i10;
        }
        if (shouldLayout(this.mCollapseButtonView)) {
            if (z2) {
                iLayoutChildRight = layoutChildRight(this.mCollapseButtonView, iLayoutChildRight, iArr, iMin);
            } else {
                iLayoutChildLeft = layoutChildLeft(this.mCollapseButtonView, iLayoutChildLeft, iArr, iMin);
            }
        }
        if (shouldLayout(this.mMenuView)) {
            if (z2) {
                iLayoutChildLeft = layoutChildLeft(this.mMenuView, iLayoutChildLeft, iArr, iMin);
            } else {
                iLayoutChildRight = layoutChildRight(this.mMenuView, iLayoutChildRight, iArr, iMin);
            }
        }
        int currentContentInsetLeft = getCurrentContentInsetLeft();
        int currentContentInsetRight = getCurrentContentInsetRight();
        iArr[0] = Math.max(0, currentContentInsetLeft - iLayoutChildLeft);
        iArr[1] = Math.max(0, currentContentInsetRight - ((width - paddingRight) - iLayoutChildRight));
        int iMax2 = Math.max(iLayoutChildLeft, currentContentInsetLeft);
        int iMin2 = Math.min(iLayoutChildRight, (width - paddingRight) - currentContentInsetRight);
        if (shouldLayout(this.mExpandedActionView)) {
            if (z2) {
                iMin2 = layoutChildRight(this.mExpandedActionView, iMin2, iArr, iMin);
            } else {
                iMax2 = layoutChildLeft(this.mExpandedActionView, iMax2, iArr, iMin);
            }
        }
        if (!shouldLayout(this.mLogoView)) {
            iLayoutChildRight2 = iMin2;
            iLayoutChildLeft2 = iMax2;
        } else if (z2) {
            iLayoutChildRight2 = layoutChildRight(this.mLogoView, iMin2, iArr, iMin);
            iLayoutChildLeft2 = iMax2;
        } else {
            iLayoutChildRight2 = iMin2;
            iLayoutChildLeft2 = layoutChildLeft(this.mLogoView, iMax2, iArr, iMin);
        }
        boolean zShouldLayout = shouldLayout(this.mTitleTextView);
        boolean zShouldLayout2 = shouldLayout(this.mSubtitleTextView);
        int measuredHeight2 = 0;
        if (zShouldLayout) {
            LayoutParams layoutParams = (LayoutParams) this.mTitleTextView.getLayoutParams();
            measuredHeight2 = layoutParams.bottomMargin + layoutParams.topMargin + this.mTitleTextView.getMeasuredHeight() + 0;
        }
        if (zShouldLayout2) {
            LayoutParams layoutParams2 = (LayoutParams) this.mSubtitleTextView.getLayoutParams();
            measuredHeight = layoutParams2.bottomMargin + layoutParams2.topMargin + this.mSubtitleTextView.getMeasuredHeight() + measuredHeight2;
        } else {
            measuredHeight = measuredHeight2;
        }
        if (zShouldLayout || zShouldLayout2) {
            TextView textView = !zShouldLayout ? this.mSubtitleTextView : this.mTitleTextView;
            TextView textView2 = !zShouldLayout2 ? this.mTitleTextView : this.mSubtitleTextView;
            LayoutParams layoutParams3 = (LayoutParams) textView.getLayoutParams();
            LayoutParams layoutParams4 = (LayoutParams) textView2.getLayoutParams();
            boolean z3 = (zShouldLayout && this.mTitleTextView.getMeasuredWidth() > 0) || (zShouldLayout2 && this.mSubtitleTextView.getMeasuredWidth() > 0);
            switch (this.mGravity & 112) {
                case 16:
                default:
                    int i11 = (((height - paddingTop2) - paddingBottom) - measuredHeight) / 2;
                    if (i11 >= layoutParams3.topMargin + this.mTitleMarginTop) {
                        int i12 = (((height - paddingBottom) - measuredHeight) - i11) - paddingTop2;
                        iMax = i12 >= layoutParams3.bottomMargin + this.mTitleMarginBottom ? i11 : Math.max(0, i11 - ((layoutParams4.bottomMargin + this.mTitleMarginBottom) - i12));
                    } else {
                        iMax = layoutParams3.topMargin + this.mTitleMarginTop;
                    }
                    paddingTop = paddingTop2 + iMax;
                    break;
                case 48:
                    paddingTop = layoutParams3.topMargin + getPaddingTop() + this.mTitleMarginTop;
                    break;
                case 80:
                    paddingTop = (((height - paddingBottom) - layoutParams4.bottomMargin) - this.mTitleMarginBottom) - measuredHeight;
                    break;
            }
            if (z2) {
                int i13 = (!z3 ? 0 : this.mTitleMarginStart) - iArr[1];
                int iMax3 = iLayoutChildRight2 - Math.max(0, i13);
                iArr[1] = Math.max(0, -i13);
                if (zShouldLayout) {
                    LayoutParams layoutParams5 = (LayoutParams) this.mTitleTextView.getLayoutParams();
                    int measuredWidth = iMax3 - this.mTitleTextView.getMeasuredWidth();
                    int measuredHeight3 = this.mTitleTextView.getMeasuredHeight() + paddingTop;
                    this.mTitleTextView.layout(measuredWidth, paddingTop, iMax3, measuredHeight3);
                    int i14 = measuredWidth - this.mTitleMarginEnd;
                    paddingTop = measuredHeight3 + layoutParams5.bottomMargin;
                    i5 = i14;
                } else {
                    i5 = iMax3;
                }
                if (zShouldLayout2) {
                    LayoutParams layoutParams6 = (LayoutParams) this.mSubtitleTextView.getLayoutParams();
                    int i15 = layoutParams6.topMargin + paddingTop;
                    this.mSubtitleTextView.layout(iMax3 - this.mSubtitleTextView.getMeasuredWidth(), i15, iMax3, this.mSubtitleTextView.getMeasuredHeight() + i15);
                    int i16 = iMax3 - this.mTitleMarginEnd;
                    int i17 = layoutParams6.bottomMargin;
                    i6 = i16;
                } else {
                    i6 = iMax3;
                }
                iLayoutChildRight2 = !z3 ? iMax3 : Math.min(i5, i6);
            } else {
                int i18 = (!z3 ? 0 : this.mTitleMarginStart) - iArr[0];
                iLayoutChildLeft2 += Math.max(0, i18);
                iArr[0] = Math.max(0, -i18);
                if (zShouldLayout) {
                    LayoutParams layoutParams7 = (LayoutParams) this.mTitleTextView.getLayoutParams();
                    int measuredWidth2 = this.mTitleTextView.getMeasuredWidth() + iLayoutChildLeft2;
                    int measuredHeight4 = this.mTitleTextView.getMeasuredHeight() + paddingTop;
                    this.mTitleTextView.layout(iLayoutChildLeft2, paddingTop, measuredWidth2, measuredHeight4);
                    int i19 = measuredWidth2 + this.mTitleMarginEnd;
                    int i20 = layoutParams7.bottomMargin + measuredHeight4;
                    i7 = i19;
                    i8 = i20;
                } else {
                    i7 = iLayoutChildLeft2;
                    i8 = paddingTop;
                }
                if (zShouldLayout2) {
                    LayoutParams layoutParams8 = (LayoutParams) this.mSubtitleTextView.getLayoutParams();
                    int i21 = i8 + layoutParams8.topMargin;
                    int measuredWidth3 = this.mSubtitleTextView.getMeasuredWidth() + iLayoutChildLeft2;
                    this.mSubtitleTextView.layout(iLayoutChildLeft2, i21, measuredWidth3, this.mSubtitleTextView.getMeasuredHeight() + i21);
                    int i22 = this.mTitleMarginEnd + measuredWidth3;
                    int i23 = layoutParams8.bottomMargin;
                    i9 = i22;
                } else {
                    i9 = iLayoutChildLeft2;
                }
                if (z3) {
                    iLayoutChildLeft2 = Math.max(i7, i9);
                }
            }
        }
        addCustomViewsWithGravity(this.mTempViews, 3);
        int size = this.mTempViews.size();
        int iLayoutChildLeft3 = iLayoutChildLeft2;
        for (int i24 = 0; i24 < size; i24++) {
            iLayoutChildLeft3 = layoutChildLeft(this.mTempViews.get(i24), iLayoutChildLeft3, iArr, iMin);
        }
        addCustomViewsWithGravity(this.mTempViews, 5);
        int size2 = this.mTempViews.size();
        for (int i25 = 0; i25 < size2; i25++) {
            iLayoutChildRight2 = layoutChildRight(this.mTempViews.get(i25), iLayoutChildRight2, iArr, iMin);
        }
        addCustomViewsWithGravity(this.mTempViews, 1);
        int viewListMeasuredWidth = getViewListMeasuredWidth(this.mTempViews, iArr);
        int i26 = ((((width - paddingLeft) - paddingRight) / 2) + paddingLeft) - (viewListMeasuredWidth / 2);
        int i27 = viewListMeasuredWidth + i26;
        if (i26 < iLayoutChildLeft3) {
            i26 = iLayoutChildLeft3;
        } else if (i27 > iLayoutChildRight2) {
            i26 -= i27 - iLayoutChildRight2;
        }
        int size3 = this.mTempViews.size();
        int iLayoutChildLeft4 = i26;
        for (int i28 = 0; i28 < size3; i28++) {
            iLayoutChildLeft4 = layoutChildLeft(this.mTempViews.get(i28), iLayoutChildLeft4, iArr, iMin);
        }
        this.mTempViews.clear();
    }

    @Override // android.view.View
    protected void onMeasure(int i, int i2) {
        char c;
        char c2;
        int iCombineMeasuredStates;
        int iMax;
        int iCombineMeasuredStates2;
        int i3;
        int[] iArr = this.mTempMargins;
        if (ViewUtils.isLayoutRtl(this)) {
            c = 0;
            c2 = 1;
        } else {
            c = 1;
            c2 = 0;
        }
        int measuredWidth = 0;
        if (shouldLayout(this.mNavButtonView)) {
            measureChildConstrained(this.mNavButtonView, i, 0, i2, 0, this.mMaxButtonHeight);
            measuredWidth = this.mNavButtonView.getMeasuredWidth() + getHorizontalMargins(this.mNavButtonView);
            int iMax2 = Math.max(0, this.mNavButtonView.getMeasuredHeight() + getVerticalMargins(this.mNavButtonView));
            iCombineMeasuredStates = View.combineMeasuredStates(0, this.mNavButtonView.getMeasuredState());
            iMax = iMax2;
        } else {
            iCombineMeasuredStates = 0;
            iMax = 0;
        }
        if (shouldLayout(this.mCollapseButtonView)) {
            measureChildConstrained(this.mCollapseButtonView, i, 0, i2, 0, this.mMaxButtonHeight);
            measuredWidth = this.mCollapseButtonView.getMeasuredWidth() + getHorizontalMargins(this.mCollapseButtonView);
            iMax = Math.max(iMax, this.mCollapseButtonView.getMeasuredHeight() + getVerticalMargins(this.mCollapseButtonView));
            iCombineMeasuredStates = View.combineMeasuredStates(iCombineMeasuredStates, this.mCollapseButtonView.getMeasuredState());
        }
        int currentContentInsetStart = getCurrentContentInsetStart();
        int iMax3 = Math.max(currentContentInsetStart, measuredWidth) + 0;
        iArr[c2] = Math.max(0, currentContentInsetStart - measuredWidth);
        int measuredWidth2 = 0;
        if (shouldLayout(this.mMenuView)) {
            measureChildConstrained(this.mMenuView, i, iMax3, i2, 0, this.mMaxButtonHeight);
            measuredWidth2 = this.mMenuView.getMeasuredWidth() + getHorizontalMargins(this.mMenuView);
            iMax = Math.max(iMax, this.mMenuView.getMeasuredHeight() + getVerticalMargins(this.mMenuView));
            iCombineMeasuredStates = View.combineMeasuredStates(iCombineMeasuredStates, this.mMenuView.getMeasuredState());
        }
        int currentContentInsetEnd = getCurrentContentInsetEnd();
        int iMax4 = iMax3 + Math.max(currentContentInsetEnd, measuredWidth2);
        iArr[c] = Math.max(0, currentContentInsetEnd - measuredWidth2);
        if (shouldLayout(this.mExpandedActionView)) {
            iMax4 += measureChildCollapseMargins(this.mExpandedActionView, i, iMax4, i2, 0, iArr);
            iMax = Math.max(iMax, this.mExpandedActionView.getMeasuredHeight() + getVerticalMargins(this.mExpandedActionView));
            iCombineMeasuredStates = View.combineMeasuredStates(iCombineMeasuredStates, this.mExpandedActionView.getMeasuredState());
        }
        if (shouldLayout(this.mLogoView)) {
            iMax4 += measureChildCollapseMargins(this.mLogoView, i, iMax4, i2, 0, iArr);
            iMax = Math.max(iMax, this.mLogoView.getMeasuredHeight() + getVerticalMargins(this.mLogoView));
            iCombineMeasuredStates = View.combineMeasuredStates(iCombineMeasuredStates, this.mLogoView.getMeasuredState());
        }
        int childCount = getChildCount();
        int i4 = 0;
        int i5 = iMax;
        int iCombineMeasuredStates3 = iCombineMeasuredStates;
        while (i4 < childCount) {
            View childAt = getChildAt(i4);
            if (((LayoutParams) childAt.getLayoutParams()).mViewType != 0) {
                iCombineMeasuredStates2 = iCombineMeasuredStates3;
                i3 = i5;
            } else if (shouldLayout(childAt)) {
                iMax4 += measureChildCollapseMargins(childAt, i, iMax4, i2, 0, iArr);
                int iMax5 = Math.max(i5, childAt.getMeasuredHeight() + getVerticalMargins(childAt));
                iCombineMeasuredStates2 = View.combineMeasuredStates(iCombineMeasuredStates3, childAt.getMeasuredState());
                i3 = iMax5;
            } else {
                iCombineMeasuredStates2 = iCombineMeasuredStates3;
                i3 = i5;
            }
            i4++;
            iCombineMeasuredStates3 = iCombineMeasuredStates2;
            i5 = i3;
        }
        int horizontalMargins = 0;
        int measuredHeight = 0;
        int i6 = this.mTitleMarginTop + this.mTitleMarginBottom;
        int i7 = this.mTitleMarginStart + this.mTitleMarginEnd;
        if (shouldLayout(this.mTitleTextView)) {
            measureChildCollapseMargins(this.mTitleTextView, i, iMax4 + i7, i2, i6, iArr);
            horizontalMargins = getHorizontalMargins(this.mTitleTextView) + this.mTitleTextView.getMeasuredWidth();
            measuredHeight = this.mTitleTextView.getMeasuredHeight() + getVerticalMargins(this.mTitleTextView);
            iCombineMeasuredStates3 = View.combineMeasuredStates(iCombineMeasuredStates3, this.mTitleTextView.getMeasuredState());
        }
        if (shouldLayout(this.mSubtitleTextView)) {
            horizontalMargins = Math.max(horizontalMargins, measureChildCollapseMargins(this.mSubtitleTextView, i, iMax4 + i7, i2, i6 + measuredHeight, iArr));
            measuredHeight += this.mSubtitleTextView.getMeasuredHeight() + getVerticalMargins(this.mSubtitleTextView);
            iCombineMeasuredStates3 = View.combineMeasuredStates(iCombineMeasuredStates3, this.mSubtitleTextView.getMeasuredState());
        }
        int iMax6 = Math.max(i5, measuredHeight);
        int paddingLeft = horizontalMargins + iMax4 + getPaddingLeft() + getPaddingRight();
        int paddingTop = iMax6 + getPaddingTop() + getPaddingBottom();
        int iResolveSizeAndState = View.resolveSizeAndState(Math.max(paddingLeft, getSuggestedMinimumWidth()), i, (-16777216) & iCombineMeasuredStates3);
        int iResolveSizeAndState2 = View.resolveSizeAndState(Math.max(paddingTop, getSuggestedMinimumHeight()), i2, iCombineMeasuredStates3 << 16);
        if (shouldCollapse()) {
            iResolveSizeAndState2 = 0;
        }
        setMeasuredDimension(iResolveSizeAndState, iResolveSizeAndState2);
    }

    @Override // android.view.View
    protected void onRestoreInstanceState(Parcelable parcelable) {
        MenuItem menuItemFindItem;
        if (!(parcelable instanceof SavedState)) {
            super.onRestoreInstanceState(parcelable);
            return;
        }
        SavedState savedState = (SavedState) parcelable;
        super.onRestoreInstanceState(savedState.getSuperState());
        MenuBuilder menuBuilderPeekMenu = this.mMenuView != null ? this.mMenuView.peekMenu() : null;
        if (savedState.expandedMenuItemId != 0 && this.mExpandedMenuPresenter != null && menuBuilderPeekMenu != null && (menuItemFindItem = menuBuilderPeekMenu.findItem(savedState.expandedMenuItemId)) != null) {
            menuItemFindItem.expandActionView();
        }
        if (savedState.isOverflowOpen) {
            postShowOverflowMenu();
        }
    }

    @Override // android.view.View
    public void onRtlPropertiesChanged(int i) {
        if (Build.VERSION.SDK_INT >= 17) {
            super.onRtlPropertiesChanged(i);
        }
        ensureContentInsets();
        this.mContentInsets.setDirection(i == 1);
    }

    @Override // android.view.View
    protected Parcelable onSaveInstanceState() {
        SavedState savedState = new SavedState(super.onSaveInstanceState());
        if (this.mExpandedMenuPresenter != null && this.mExpandedMenuPresenter.mCurrentExpandedItem != null) {
            savedState.expandedMenuItemId = this.mExpandedMenuPresenter.mCurrentExpandedItem.getItemId();
        }
        savedState.isOverflowOpen = isOverflowMenuShowing();
        return savedState;
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked == 0) {
            this.mEatingTouch = false;
        }
        if (!this.mEatingTouch) {
            boolean zOnTouchEvent = super.onTouchEvent(motionEvent);
            if (actionMasked == 0 && !zOnTouchEvent) {
                this.mEatingTouch = true;
            }
        }
        if (actionMasked == 1 || actionMasked == 3) {
            this.mEatingTouch = false;
        }
        return true;
    }

    void removeChildrenForExpandedActionView() {
        for (int childCount = getChildCount() - 1; childCount >= 0; childCount--) {
            View childAt = getChildAt(childCount);
            if (((LayoutParams) childAt.getLayoutParams()).mViewType != 2 && childAt != this.mMenuView) {
                removeViewAt(childCount);
                this.mHiddenViews.add(childAt);
            }
        }
    }

    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
    public void setCollapsible(boolean z) {
        this.mCollapsible = z;
        requestLayout();
    }

    public void setContentInsetEndWithActions(int i) {
        if (i < 0) {
            i = Integer.MIN_VALUE;
        }
        if (i == this.mContentInsetEndWithActions) {
            return;
        }
        this.mContentInsetEndWithActions = i;
        if (getNavigationIcon() != null) {
            requestLayout();
        }
    }

    public void setContentInsetStartWithNavigation(int i) {
        if (i < 0) {
            i = Integer.MIN_VALUE;
        }
        if (i == this.mContentInsetStartWithNavigation) {
            return;
        }
        this.mContentInsetStartWithNavigation = i;
        if (getNavigationIcon() != null) {
            requestLayout();
        }
    }

    public void setContentInsetsAbsolute(int i, int i2) {
        ensureContentInsets();
        this.mContentInsets.setAbsolute(i, i2);
    }

    public void setContentInsetsRelative(int i, int i2) {
        ensureContentInsets();
        this.mContentInsets.setRelative(i, i2);
    }

    public void setLogo(@DrawableRes int i) {
        setLogo(AppCompatResources.getDrawable(getContext(), i));
    }

    public void setLogo(Drawable drawable) {
        if (drawable != null) {
            ensureLogoView();
            if (!isChildOrHidden(this.mLogoView)) {
                addSystemView(this.mLogoView, true);
            }
        } else if (this.mLogoView != null && isChildOrHidden(this.mLogoView)) {
            removeView(this.mLogoView);
            this.mHiddenViews.remove(this.mLogoView);
        }
        if (this.mLogoView == null) {
            return;
        }
        this.mLogoView.setImageDrawable(drawable);
    }

    public void setLogoDescription(@StringRes int i) {
        setLogoDescription(getContext().getText(i));
    }

    public void setLogoDescription(CharSequence charSequence) {
        if (!TextUtils.isEmpty(charSequence)) {
            ensureLogoView();
        }
        if (this.mLogoView == null) {
            return;
        }
        this.mLogoView.setContentDescription(charSequence);
    }

    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
    public void setMenu(MenuBuilder menuBuilder, ActionMenuPresenter actionMenuPresenter) {
        if (menuBuilder == null && this.mMenuView == null) {
            return;
        }
        ensureMenuView();
        MenuBuilder menuBuilderPeekMenu = this.mMenuView.peekMenu();
        if (menuBuilderPeekMenu != menuBuilder) {
            if (menuBuilderPeekMenu != null) {
                menuBuilderPeekMenu.removeMenuPresenter(this.mOuterActionMenuPresenter);
                menuBuilderPeekMenu.removeMenuPresenter(this.mExpandedMenuPresenter);
            }
            if (this.mExpandedMenuPresenter == null) {
                this.mExpandedMenuPresenter = new ExpandedActionViewMenuPresenter();
            }
            actionMenuPresenter.setExpandedActionViewsExclusive(true);
            if (menuBuilder == null) {
                actionMenuPresenter.initForMenu(this.mPopupContext, null);
                this.mExpandedMenuPresenter.initForMenu(this.mPopupContext, null);
                actionMenuPresenter.updateMenuView(true);
                this.mExpandedMenuPresenter.updateMenuView(true);
            } else {
                menuBuilder.addMenuPresenter(actionMenuPresenter, this.mPopupContext);
                menuBuilder.addMenuPresenter(this.mExpandedMenuPresenter, this.mPopupContext);
            }
            this.mMenuView.setPopupTheme(this.mPopupTheme);
            this.mMenuView.setPresenter(actionMenuPresenter);
            this.mOuterActionMenuPresenter = actionMenuPresenter;
        }
    }

    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
    public void setMenuCallbacks(MenuPresenter.Callback callback, MenuBuilder.Callback callback2) {
        this.mActionMenuPresenterCallback = callback;
        this.mMenuBuilderCallback = callback2;
        if (this.mMenuView == null) {
            return;
        }
        this.mMenuView.setMenuCallbacks(callback, callback2);
    }

    public void setNavigationContentDescription(@StringRes int i) {
        setNavigationContentDescription(i == 0 ? null : getContext().getText(i));
    }

    public void setNavigationContentDescription(@Nullable CharSequence charSequence) {
        if (!TextUtils.isEmpty(charSequence)) {
            ensureNavButtonView();
        }
        if (this.mNavButtonView == null) {
            return;
        }
        this.mNavButtonView.setContentDescription(charSequence);
    }

    public void setNavigationIcon(@DrawableRes int i) {
        setNavigationIcon(AppCompatResources.getDrawable(getContext(), i));
    }

    public void setNavigationIcon(@Nullable Drawable drawable) {
        if (drawable != null) {
            ensureNavButtonView();
            if (!isChildOrHidden(this.mNavButtonView)) {
                addSystemView(this.mNavButtonView, true);
            }
        } else if (this.mNavButtonView != null && isChildOrHidden(this.mNavButtonView)) {
            removeView(this.mNavButtonView);
            this.mHiddenViews.remove(this.mNavButtonView);
        }
        if (this.mNavButtonView == null) {
            return;
        }
        this.mNavButtonView.setImageDrawable(drawable);
    }

    public void setNavigationOnClickListener(View.OnClickListener onClickListener) {
        ensureNavButtonView();
        this.mNavButtonView.setOnClickListener(onClickListener);
    }

    public void setOnMenuItemClickListener(OnMenuItemClickListener onMenuItemClickListener) {
        this.mOnMenuItemClickListener = onMenuItemClickListener;
    }

    public void setOverflowIcon(@Nullable Drawable drawable) {
        ensureMenu();
        this.mMenuView.setOverflowIcon(drawable);
    }

    public void setPopupTheme(@StyleRes int i) {
        if (this.mPopupTheme == i) {
            return;
        }
        this.mPopupTheme = i;
        if (i != 0) {
            this.mPopupContext = new ContextThemeWrapper(getContext(), i);
        } else {
            this.mPopupContext = getContext();
        }
    }

    public void setSubtitle(@StringRes int i) {
        setSubtitle(getContext().getText(i));
    }

    public void setSubtitle(CharSequence charSequence) {
        if (!TextUtils.isEmpty(charSequence)) {
            if (this.mSubtitleTextView == null) {
                Context context = getContext();
                this.mSubtitleTextView = new AppCompatTextView(context);
                this.mSubtitleTextView.setSingleLine();
                this.mSubtitleTextView.setEllipsize(TextUtils.TruncateAt.END);
                if (this.mSubtitleTextAppearance != 0) {
                    this.mSubtitleTextView.setTextAppearance(context, this.mSubtitleTextAppearance);
                }
                if (this.mSubtitleTextColor != 0) {
                    this.mSubtitleTextView.setTextColor(this.mSubtitleTextColor);
                }
            }
            if (!isChildOrHidden(this.mSubtitleTextView)) {
                addSystemView(this.mSubtitleTextView, true);
            }
        } else if (this.mSubtitleTextView != null && isChildOrHidden(this.mSubtitleTextView)) {
            removeView(this.mSubtitleTextView);
            this.mHiddenViews.remove(this.mSubtitleTextView);
        }
        if (this.mSubtitleTextView != null) {
            this.mSubtitleTextView.setText(charSequence);
        }
        this.mSubtitleText = charSequence;
    }

    public void setSubtitleTextAppearance(Context context, @StyleRes int i) {
        this.mSubtitleTextAppearance = i;
        if (this.mSubtitleTextView == null) {
            return;
        }
        this.mSubtitleTextView.setTextAppearance(context, i);
    }

    public void setSubtitleTextColor(@ColorInt int i) {
        this.mSubtitleTextColor = i;
        if (this.mSubtitleTextView == null) {
            return;
        }
        this.mSubtitleTextView.setTextColor(i);
    }

    public void setTitle(@StringRes int i) {
        setTitle(getContext().getText(i));
    }

    public void setTitle(CharSequence charSequence) {
        if (!TextUtils.isEmpty(charSequence)) {
            if (this.mTitleTextView == null) {
                Context context = getContext();
                this.mTitleTextView = new AppCompatTextView(context);
                this.mTitleTextView.setSingleLine();
                this.mTitleTextView.setEllipsize(TextUtils.TruncateAt.END);
                if (this.mTitleTextAppearance != 0) {
                    this.mTitleTextView.setTextAppearance(context, this.mTitleTextAppearance);
                }
                if (this.mTitleTextColor != 0) {
                    this.mTitleTextView.setTextColor(this.mTitleTextColor);
                }
            }
            if (!isChildOrHidden(this.mTitleTextView)) {
                addSystemView(this.mTitleTextView, true);
            }
        } else if (this.mTitleTextView != null && isChildOrHidden(this.mTitleTextView)) {
            removeView(this.mTitleTextView);
            this.mHiddenViews.remove(this.mTitleTextView);
        }
        if (this.mTitleTextView != null) {
            this.mTitleTextView.setText(charSequence);
        }
        this.mTitleText = charSequence;
    }

    public void setTitleMargin(int i, int i2, int i3, int i4) {
        this.mTitleMarginStart = i;
        this.mTitleMarginTop = i2;
        this.mTitleMarginEnd = i3;
        this.mTitleMarginBottom = i4;
        requestLayout();
    }

    public void setTitleMarginBottom(int i) {
        this.mTitleMarginBottom = i;
        requestLayout();
    }

    public void setTitleMarginEnd(int i) {
        this.mTitleMarginEnd = i;
        requestLayout();
    }

    public void setTitleMarginStart(int i) {
        this.mTitleMarginStart = i;
        requestLayout();
    }

    public void setTitleMarginTop(int i) {
        this.mTitleMarginTop = i;
        requestLayout();
    }

    public void setTitleTextAppearance(Context context, @StyleRes int i) {
        this.mTitleTextAppearance = i;
        if (this.mTitleTextView == null) {
            return;
        }
        this.mTitleTextView.setTextAppearance(context, i);
    }

    public void setTitleTextColor(@ColorInt int i) {
        this.mTitleTextColor = i;
        if (this.mTitleTextView == null) {
            return;
        }
        this.mTitleTextView.setTextColor(i);
    }

    public boolean showOverflowMenu() {
        return this.mMenuView != null && this.mMenuView.showOverflowMenu();
    }
}
