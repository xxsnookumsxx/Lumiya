package android.support.design.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.FloatRange;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.annotation.VisibleForTesting;
import android.support.design.R;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.math.MathUtils;
import android.support.v4.util.ObjectsCompat;
import android.support.v4.util.Pools;
import android.support.v4.view.AbsSavedState;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.NestedScrollingParent2;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.OnApplyWindowInsetsListener;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.WindowInsetsCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/* JADX INFO: loaded from: classes.dex */
public class CoordinatorLayout extends ViewGroup implements NestedScrollingParent2 {
    static final Class<?>[] CONSTRUCTOR_PARAMS;
    static final int EVENT_NESTED_SCROLL = 1;
    static final int EVENT_PRE_DRAW = 0;
    static final int EVENT_VIEW_REMOVED = 2;
    static final String TAG = "CoordinatorLayout";
    static final Comparator<View> TOP_SORTED_CHILDREN_COMPARATOR;
    private static final int TYPE_ON_INTERCEPT = 0;
    private static final int TYPE_ON_TOUCH = 1;
    static final String WIDGET_PACKAGE_NAME;
    static final ThreadLocal<Map<String, Constructor<Behavior>>> sConstructors;
    private static final Pools.Pool<Rect> sRectPool;
    private OnApplyWindowInsetsListener mApplyWindowInsetsListener;
    private View mBehaviorTouchView;
    private final DirectedAcyclicGraph<View> mChildDag;
    private final List<View> mDependencySortedChildren;
    private boolean mDisallowInterceptReset;
    private boolean mDrawStatusBarBackground;
    private boolean mIsAttachedToWindow;
    private int[] mKeylines;
    private WindowInsetsCompat mLastInsets;
    private boolean mNeedsPreDrawListener;
    private final NestedScrollingParentHelper mNestedScrollingParentHelper;
    private View mNestedScrollingTarget;
    ViewGroup.OnHierarchyChangeListener mOnHierarchyChangeListener;
    private OnPreDrawListener mOnPreDrawListener;
    private Paint mScrimPaint;
    private Drawable mStatusBarBackground;
    private final List<View> mTempDependenciesList;
    private final int[] mTempIntPair;
    private final List<View> mTempList1;

    public static abstract class Behavior<V extends View> {
        public Behavior() {
        }

        public Behavior(Context context, AttributeSet attributeSet) {
        }

        public static Object getTag(View view) {
            return ((LayoutParams) view.getLayoutParams()).mBehaviorTag;
        }

        public static void setTag(View view, Object obj) {
            ((LayoutParams) view.getLayoutParams()).mBehaviorTag = obj;
        }

        public boolean blocksInteractionBelow(CoordinatorLayout coordinatorLayout, V v) {
            return getScrimOpacity(coordinatorLayout, v) > 0.0f;
        }

        public boolean getInsetDodgeRect(@NonNull CoordinatorLayout coordinatorLayout, @NonNull V v, @NonNull Rect rect) {
            return false;
        }

        @ColorInt
        public int getScrimColor(CoordinatorLayout coordinatorLayout, V v) {
            return ViewCompat.MEASURED_STATE_MASK;
        }

        @FloatRange(from = 0.0d, to = 1.0d)
        public float getScrimOpacity(CoordinatorLayout coordinatorLayout, V v) {
            return 0.0f;
        }

        public boolean layoutDependsOn(CoordinatorLayout coordinatorLayout, V v, View view) {
            return false;
        }

        @NonNull
        public WindowInsetsCompat onApplyWindowInsets(CoordinatorLayout coordinatorLayout, V v, WindowInsetsCompat windowInsetsCompat) {
            return windowInsetsCompat;
        }

        public void onAttachedToLayoutParams(@NonNull LayoutParams layoutParams) {
        }

        public boolean onDependentViewChanged(CoordinatorLayout coordinatorLayout, V v, View view) {
            return false;
        }

        public void onDependentViewRemoved(CoordinatorLayout coordinatorLayout, V v, View view) {
        }

        public void onDetachedFromLayoutParams() {
        }

        public boolean onInterceptTouchEvent(CoordinatorLayout coordinatorLayout, V v, MotionEvent motionEvent) {
            return false;
        }

        public boolean onLayoutChild(CoordinatorLayout coordinatorLayout, V v, int i) {
            return false;
        }

        public boolean onMeasureChild(CoordinatorLayout coordinatorLayout, V v, int i, int i2, int i3, int i4) {
            return false;
        }

        public boolean onNestedFling(@NonNull CoordinatorLayout coordinatorLayout, @NonNull V v, @NonNull View view, float f, float f2, boolean z) {
            return false;
        }

        public boolean onNestedPreFling(@NonNull CoordinatorLayout coordinatorLayout, @NonNull V v, @NonNull View view, float f, float f2) {
            return false;
        }

        @Deprecated
        public void onNestedPreScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull V v, @NonNull View view, int i, int i2, @NonNull int[] iArr) {
        }

        public void onNestedPreScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull V v, @NonNull View view, int i, int i2, @NonNull int[] iArr, int i3) {
            if (i3 != 0) {
                return;
            }
            onNestedPreScroll(coordinatorLayout, v, view, i, i2, iArr);
        }

        @Deprecated
        public void onNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull V v, @NonNull View view, int i, int i2, int i3, int i4) {
        }

        public void onNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull V v, @NonNull View view, int i, int i2, int i3, int i4, int i5) {
            if (i5 != 0) {
                return;
            }
            onNestedScroll(coordinatorLayout, v, view, i, i2, i3, i4);
        }

        @Deprecated
        public void onNestedScrollAccepted(@NonNull CoordinatorLayout coordinatorLayout, @NonNull V v, @NonNull View view, @NonNull View view2, int i) {
        }

        public void onNestedScrollAccepted(@NonNull CoordinatorLayout coordinatorLayout, @NonNull V v, @NonNull View view, @NonNull View view2, int i, int i2) {
            if (i2 != 0) {
                return;
            }
            onNestedScrollAccepted(coordinatorLayout, v, view, view2, i);
        }

        public boolean onRequestChildRectangleOnScreen(CoordinatorLayout coordinatorLayout, V v, Rect rect, boolean z) {
            return false;
        }

        public void onRestoreInstanceState(CoordinatorLayout coordinatorLayout, V v, Parcelable parcelable) {
        }

        public Parcelable onSaveInstanceState(CoordinatorLayout coordinatorLayout, V v) {
            return View.BaseSavedState.EMPTY_STATE;
        }

        @Deprecated
        public boolean onStartNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull V v, @NonNull View view, @NonNull View view2, int i) {
            return false;
        }

        public boolean onStartNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull V v, @NonNull View view, @NonNull View view2, int i, int i2) {
            if (i2 != 0) {
                return false;
            }
            return onStartNestedScroll(coordinatorLayout, v, view, view2, i);
        }

        @Deprecated
        public void onStopNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull V v, @NonNull View view) {
        }

        public void onStopNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull V v, @NonNull View view, int i) {
            if (i != 0) {
                return;
            }
            onStopNestedScroll(coordinatorLayout, v, view);
        }

        public boolean onTouchEvent(CoordinatorLayout coordinatorLayout, V v, MotionEvent motionEvent) {
            return false;
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface DefaultBehavior {
        Class<? extends Behavior> value();
    }

    @Retention(RetentionPolicy.SOURCE)
    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
    public @interface DispatchChangeEvent {
    }

    private class HierarchyChangeListener implements ViewGroup.OnHierarchyChangeListener {
        HierarchyChangeListener() {
        }

        @Override // android.view.ViewGroup.OnHierarchyChangeListener
        public void onChildViewAdded(View view, View view2) {
            if (CoordinatorLayout.this.mOnHierarchyChangeListener == null) {
                return;
            }
            CoordinatorLayout.this.mOnHierarchyChangeListener.onChildViewAdded(view, view2);
        }

        @Override // android.view.ViewGroup.OnHierarchyChangeListener
        public void onChildViewRemoved(View view, View view2) {
            CoordinatorLayout.this.onChildViewsChanged(2);
            if (CoordinatorLayout.this.mOnHierarchyChangeListener == null) {
                return;
            }
            CoordinatorLayout.this.mOnHierarchyChangeListener.onChildViewRemoved(view, view2);
        }
    }

    public static class LayoutParams extends ViewGroup.MarginLayoutParams {
        public int anchorGravity;
        public int dodgeInsetEdges;
        public int gravity;
        public int insetEdge;
        public int keyline;
        View mAnchorDirectChild;
        int mAnchorId;
        View mAnchorView;
        Behavior mBehavior;
        boolean mBehaviorResolved;
        Object mBehaviorTag;
        private boolean mDidAcceptNestedScrollNonTouch;
        private boolean mDidAcceptNestedScrollTouch;
        private boolean mDidBlockInteraction;
        private boolean mDidChangeAfterNestedScroll;
        int mInsetOffsetX;
        int mInsetOffsetY;
        final Rect mLastChildRect;

        public LayoutParams(int i, int i2) {
            super(i, i2);
            this.mBehaviorResolved = false;
            this.gravity = 0;
            this.anchorGravity = 0;
            this.keyline = -1;
            this.mAnchorId = -1;
            this.insetEdge = 0;
            this.dodgeInsetEdges = 0;
            this.mLastChildRect = new Rect();
        }

        LayoutParams(Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
            this.mBehaviorResolved = false;
            this.gravity = 0;
            this.anchorGravity = 0;
            this.keyline = -1;
            this.mAnchorId = -1;
            this.insetEdge = 0;
            this.dodgeInsetEdges = 0;
            this.mLastChildRect = new Rect();
            TypedArray typedArrayObtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.CoordinatorLayout_Layout);
            this.gravity = typedArrayObtainStyledAttributes.getInteger(R.styleable.CoordinatorLayout_Layout_android_layout_gravity, 0);
            this.mAnchorId = typedArrayObtainStyledAttributes.getResourceId(R.styleable.CoordinatorLayout_Layout_layout_anchor, -1);
            this.anchorGravity = typedArrayObtainStyledAttributes.getInteger(R.styleable.CoordinatorLayout_Layout_layout_anchorGravity, 0);
            this.keyline = typedArrayObtainStyledAttributes.getInteger(R.styleable.CoordinatorLayout_Layout_layout_keyline, -1);
            this.insetEdge = typedArrayObtainStyledAttributes.getInt(R.styleable.CoordinatorLayout_Layout_layout_insetEdge, 0);
            this.dodgeInsetEdges = typedArrayObtainStyledAttributes.getInt(R.styleable.CoordinatorLayout_Layout_layout_dodgeInsetEdges, 0);
            this.mBehaviorResolved = typedArrayObtainStyledAttributes.hasValue(R.styleable.CoordinatorLayout_Layout_layout_behavior);
            if (this.mBehaviorResolved) {
                this.mBehavior = CoordinatorLayout.parseBehavior(context, attributeSet, typedArrayObtainStyledAttributes.getString(R.styleable.CoordinatorLayout_Layout_layout_behavior));
            }
            typedArrayObtainStyledAttributes.recycle();
            if (this.mBehavior == null) {
                return;
            }
            this.mBehavior.onAttachedToLayoutParams(this);
        }

        public LayoutParams(LayoutParams layoutParams) {
            super((ViewGroup.MarginLayoutParams) layoutParams);
            this.mBehaviorResolved = false;
            this.gravity = 0;
            this.anchorGravity = 0;
            this.keyline = -1;
            this.mAnchorId = -1;
            this.insetEdge = 0;
            this.dodgeInsetEdges = 0;
            this.mLastChildRect = new Rect();
        }

        public LayoutParams(ViewGroup.LayoutParams layoutParams) {
            super(layoutParams);
            this.mBehaviorResolved = false;
            this.gravity = 0;
            this.anchorGravity = 0;
            this.keyline = -1;
            this.mAnchorId = -1;
            this.insetEdge = 0;
            this.dodgeInsetEdges = 0;
            this.mLastChildRect = new Rect();
        }

        public LayoutParams(ViewGroup.MarginLayoutParams marginLayoutParams) {
            super(marginLayoutParams);
            this.mBehaviorResolved = false;
            this.gravity = 0;
            this.anchorGravity = 0;
            this.keyline = -1;
            this.mAnchorId = -1;
            this.insetEdge = 0;
            this.dodgeInsetEdges = 0;
            this.mLastChildRect = new Rect();
        }

        private void resolveAnchorView(View view, CoordinatorLayout coordinatorLayout) {
            this.mAnchorView = coordinatorLayout.findViewById(this.mAnchorId);
            if (this.mAnchorView == null) {
                if (!coordinatorLayout.isInEditMode()) {
                    throw new IllegalStateException("Could not find CoordinatorLayout descendant view with id " + coordinatorLayout.getResources().getResourceName(this.mAnchorId) + " to anchor view " + view);
                }
                this.mAnchorDirectChild = null;
                this.mAnchorView = null;
                return;
            }
            if (this.mAnchorView == coordinatorLayout) {
                if (!coordinatorLayout.isInEditMode()) {
                    throw new IllegalStateException("View can not be anchored to the the parent CoordinatorLayout");
                }
                this.mAnchorDirectChild = null;
                this.mAnchorView = null;
                return;
            }
            View view2 = this.mAnchorView;
            for (ViewParent parent = this.mAnchorView.getParent(); parent != coordinatorLayout && parent != null; parent = parent.getParent()) {
                if (parent == view) {
                    if (!coordinatorLayout.isInEditMode()) {
                        throw new IllegalStateException("Anchor must not be a descendant of the anchored view");
                    }
                    this.mAnchorDirectChild = null;
                    this.mAnchorView = null;
                    return;
                }
                if (parent instanceof View) {
                    view2 = (View) parent;
                }
            }
            this.mAnchorDirectChild = view2;
        }

        private boolean shouldDodge(View view, int i) {
            int absoluteGravity = GravityCompat.getAbsoluteGravity(((LayoutParams) view.getLayoutParams()).insetEdge, i);
            return absoluteGravity != 0 && (GravityCompat.getAbsoluteGravity(this.dodgeInsetEdges, i) & absoluteGravity) == absoluteGravity;
        }

        private boolean verifyAnchorView(View view, CoordinatorLayout coordinatorLayout) {
            if (this.mAnchorView.getId() != this.mAnchorId) {
                return false;
            }
            View view2 = this.mAnchorView;
            for (ViewParent parent = this.mAnchorView.getParent(); parent != coordinatorLayout; parent = parent.getParent()) {
                if (parent == null || parent == view) {
                    this.mAnchorDirectChild = null;
                    this.mAnchorView = null;
                    return false;
                }
                if (parent instanceof View) {
                    view2 = (View) parent;
                }
            }
            this.mAnchorDirectChild = view2;
            return true;
        }

        boolean checkAnchorChanged() {
            return this.mAnchorView == null && this.mAnchorId != -1;
        }

        boolean dependsOn(CoordinatorLayout coordinatorLayout, View view, View view2) {
            return view2 == this.mAnchorDirectChild || shouldDodge(view2, ViewCompat.getLayoutDirection(coordinatorLayout)) || (this.mBehavior != null && this.mBehavior.layoutDependsOn(coordinatorLayout, view, view2));
        }

        boolean didBlockInteraction() {
            if (this.mBehavior == null) {
                this.mDidBlockInteraction = false;
            }
            return this.mDidBlockInteraction;
        }

        View findAnchorView(CoordinatorLayout coordinatorLayout, View view) {
            if (this.mAnchorId == -1) {
                this.mAnchorDirectChild = null;
                this.mAnchorView = null;
                return null;
            }
            if (this.mAnchorView == null || !verifyAnchorView(view, coordinatorLayout)) {
                resolveAnchorView(view, coordinatorLayout);
            }
            return this.mAnchorView;
        }

        @IdRes
        public int getAnchorId() {
            return this.mAnchorId;
        }

        @Nullable
        public Behavior getBehavior() {
            return this.mBehavior;
        }

        boolean getChangedAfterNestedScroll() {
            return this.mDidChangeAfterNestedScroll;
        }

        Rect getLastChildRect() {
            return this.mLastChildRect;
        }

        void invalidateAnchor() {
            this.mAnchorDirectChild = null;
            this.mAnchorView = null;
        }

        boolean isBlockingInteractionBelow(CoordinatorLayout coordinatorLayout, View view) {
            if (this.mDidBlockInteraction) {
                return true;
            }
            boolean zBlocksInteractionBelow = (this.mBehavior != null ? this.mBehavior.blocksInteractionBelow(coordinatorLayout, view) : false) | this.mDidBlockInteraction;
            this.mDidBlockInteraction = zBlocksInteractionBelow;
            return zBlocksInteractionBelow;
        }

        boolean isNestedScrollAccepted(int i) {
            switch (i) {
                case 0:
                    return this.mDidAcceptNestedScrollTouch;
                case 1:
                    return this.mDidAcceptNestedScrollNonTouch;
                default:
                    return false;
            }
        }

        void resetChangedAfterNestedScroll() {
            this.mDidChangeAfterNestedScroll = false;
        }

        void resetNestedScroll(int i) {
            setNestedScrollAccepted(i, false);
        }

        void resetTouchBehaviorTracking() {
            this.mDidBlockInteraction = false;
        }

        public void setAnchorId(@IdRes int i) {
            invalidateAnchor();
            this.mAnchorId = i;
        }

        public void setBehavior(@Nullable Behavior behavior) {
            if (this.mBehavior == behavior) {
                return;
            }
            if (this.mBehavior != null) {
                this.mBehavior.onDetachedFromLayoutParams();
            }
            this.mBehavior = behavior;
            this.mBehaviorTag = null;
            this.mBehaviorResolved = true;
            if (behavior != null) {
                behavior.onAttachedToLayoutParams(this);
            }
        }

        void setChangedAfterNestedScroll(boolean z) {
            this.mDidChangeAfterNestedScroll = z;
        }

        void setLastChildRect(Rect rect) {
            this.mLastChildRect.set(rect);
        }

        void setNestedScrollAccepted(int i, boolean z) {
            switch (i) {
                case 0:
                    this.mDidAcceptNestedScrollTouch = z;
                    break;
                case 1:
                    this.mDidAcceptNestedScrollNonTouch = z;
                    break;
            }
        }
    }

    class OnPreDrawListener implements ViewTreeObserver.OnPreDrawListener {
        OnPreDrawListener() {
        }

        @Override // android.view.ViewTreeObserver.OnPreDrawListener
        public boolean onPreDraw() {
            CoordinatorLayout.this.onChildViewsChanged(0);
            return true;
        }
    }

    protected static class SavedState extends AbsSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.ClassLoaderCreator<SavedState>() { // from class: android.support.design.widget.CoordinatorLayout.SavedState.1
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
        SparseArray<Parcelable> behaviorStates;

        public SavedState(Parcel parcel, ClassLoader classLoader) {
            super(parcel, classLoader);
            int i = parcel.readInt();
            int[] iArr = new int[i];
            parcel.readIntArray(iArr);
            Parcelable[] parcelableArray = parcel.readParcelableArray(classLoader);
            this.behaviorStates = new SparseArray<>(i);
            for (int i2 = 0; i2 < i; i2++) {
                this.behaviorStates.append(iArr[i2], parcelableArray[i2]);
            }
        }

        public SavedState(Parcelable parcelable) {
            super(parcelable);
        }

        @Override // android.support.v4.view.AbsSavedState, android.os.Parcelable
        public void writeToParcel(Parcel parcel, int i) {
            super.writeToParcel(parcel, i);
            int size = this.behaviorStates == null ? 0 : this.behaviorStates.size();
            parcel.writeInt(size);
            int[] iArr = new int[size];
            Parcelable[] parcelableArr = new Parcelable[size];
            for (int i2 = 0; i2 < size; i2++) {
                iArr[i2] = this.behaviorStates.keyAt(i2);
                parcelableArr[i2] = this.behaviorStates.valueAt(i2);
            }
            parcel.writeIntArray(iArr);
            parcel.writeParcelableArray(parcelableArr, i);
        }
    }

    static class ViewElevationComparator implements Comparator<View> {
        ViewElevationComparator() {
        }

        @Override // java.util.Comparator
        public int compare(View view, View view2) {
            float z = ViewCompat.getZ(view);
            float z2 = ViewCompat.getZ(view2);
            if (z > z2) {
                return -1;
            }
            return z < z2 ? 1 : 0;
        }
    }

    static {
        Package r0 = CoordinatorLayout.class.getPackage();
        WIDGET_PACKAGE_NAME = r0 == null ? null : r0.getName();
        if (Build.VERSION.SDK_INT < 21) {
            TOP_SORTED_CHILDREN_COMPARATOR = null;
        } else {
            TOP_SORTED_CHILDREN_COMPARATOR = new ViewElevationComparator();
        }
        CONSTRUCTOR_PARAMS = new Class[]{Context.class, AttributeSet.class};
        sConstructors = new ThreadLocal<>();
        sRectPool = new Pools.SynchronizedPool(12);
    }

    public CoordinatorLayout(Context context) {
        this(context, null);
    }

    public CoordinatorLayout(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public CoordinatorLayout(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mDependencySortedChildren = new ArrayList();
        this.mChildDag = new DirectedAcyclicGraph<>();
        this.mTempList1 = new ArrayList();
        this.mTempDependenciesList = new ArrayList();
        this.mTempIntPair = new int[2];
        this.mNestedScrollingParentHelper = new NestedScrollingParentHelper(this);
        ThemeUtils.checkAppCompatTheme(context);
        TypedArray typedArrayObtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.CoordinatorLayout, i, R.style.Widget_Design_CoordinatorLayout);
        int resourceId = typedArrayObtainStyledAttributes.getResourceId(R.styleable.CoordinatorLayout_keylines, 0);
        if (resourceId != 0) {
            Resources resources = context.getResources();
            this.mKeylines = resources.getIntArray(resourceId);
            float f = resources.getDisplayMetrics().density;
            int length = this.mKeylines.length;
            for (int i2 = 0; i2 < length; i2++) {
                this.mKeylines[i2] = (int) (this.mKeylines[i2] * f);
            }
        }
        this.mStatusBarBackground = typedArrayObtainStyledAttributes.getDrawable(R.styleable.CoordinatorLayout_statusBarBackground);
        typedArrayObtainStyledAttributes.recycle();
        setupForInsets();
        super.setOnHierarchyChangeListener(new HierarchyChangeListener());
    }

    @NonNull
    private static Rect acquireTempRect() {
        Rect rectAcquire = sRectPool.acquire();
        return rectAcquire != null ? rectAcquire : new Rect();
    }

    private void constrainChildRect(LayoutParams layoutParams, Rect rect, int i, int i2) {
        int width = getWidth();
        int height = getHeight();
        int iMax = Math.max(getPaddingLeft() + layoutParams.leftMargin, Math.min(rect.left, ((width - getPaddingRight()) - i) - layoutParams.rightMargin));
        int iMax2 = Math.max(getPaddingTop() + layoutParams.topMargin, Math.min(rect.top, ((height - getPaddingBottom()) - i2) - layoutParams.bottomMargin));
        rect.set(iMax, iMax2, iMax + i, iMax2 + i2);
    }

    private WindowInsetsCompat dispatchApplyWindowInsetsToBehaviors(WindowInsetsCompat windowInsetsCompat) {
        WindowInsetsCompat windowInsetsCompatOnApplyWindowInsets;
        if (windowInsetsCompat.isConsumed()) {
            return windowInsetsCompat;
        }
        int childCount = getChildCount();
        int i = 0;
        WindowInsetsCompat windowInsetsCompat2 = windowInsetsCompat;
        while (i < childCount) {
            View childAt = getChildAt(i);
            if (ViewCompat.getFitsSystemWindows(childAt)) {
                Behavior behavior = ((LayoutParams) childAt.getLayoutParams()).getBehavior();
                if (behavior == null) {
                    windowInsetsCompatOnApplyWindowInsets = windowInsetsCompat2;
                } else {
                    windowInsetsCompatOnApplyWindowInsets = behavior.onApplyWindowInsets(this, childAt, windowInsetsCompat2);
                    if (windowInsetsCompatOnApplyWindowInsets.isConsumed()) {
                        return windowInsetsCompatOnApplyWindowInsets;
                    }
                }
            } else {
                windowInsetsCompatOnApplyWindowInsets = windowInsetsCompat2;
            }
            i++;
            windowInsetsCompat2 = windowInsetsCompatOnApplyWindowInsets;
        }
        return windowInsetsCompat2;
    }

    private void getDesiredAnchoredChildRectWithoutConstraints(View view, int i, Rect rect, Rect rect2, LayoutParams layoutParams, int i2, int i3) {
        int iWidth;
        int iHeight;
        int absoluteGravity = GravityCompat.getAbsoluteGravity(resolveAnchoredChildGravity(layoutParams.gravity), i);
        int absoluteGravity2 = GravityCompat.getAbsoluteGravity(resolveGravity(layoutParams.anchorGravity), i);
        int i4 = absoluteGravity & 7;
        int i5 = absoluteGravity & 112;
        int i6 = absoluteGravity2 & 112;
        switch (absoluteGravity2 & 7) {
            case 1:
                iWidth = (rect.width() / 2) + rect.left;
                break;
            case 2:
            case 3:
            case 4:
            default:
                iWidth = rect.left;
                break;
            case 5:
                iWidth = rect.right;
                break;
        }
        switch (i6) {
            case 16:
                iHeight = rect.top + (rect.height() / 2);
                break;
            case 48:
            default:
                iHeight = rect.top;
                break;
            case 80:
                iHeight = rect.bottom;
                break;
        }
        switch (i4) {
            case 1:
                iWidth -= i2 / 2;
                break;
            case 2:
            case 3:
            case 4:
            default:
                iWidth -= i2;
                break;
            case 5:
                break;
        }
        switch (i5) {
            case 16:
                iHeight -= i3 / 2;
                break;
            case 48:
            default:
                iHeight -= i3;
                break;
            case 80:
                break;
        }
        rect2.set(iWidth, iHeight, iWidth + i2, iHeight + i3);
    }

    private int getKeyline(int i) {
        if (this.mKeylines == null) {
            Log.e(TAG, "No keylines defined for " + this + " - attempted index lookup " + i);
            return 0;
        }
        if (i >= 0 && i < this.mKeylines.length) {
            return this.mKeylines[i];
        }
        Log.e(TAG, "Keyline index " + i + " out of range for " + this);
        return 0;
    }

    private void getTopSortedChildren(List<View> list) {
        list.clear();
        boolean zIsChildrenDrawingOrderEnabled = isChildrenDrawingOrderEnabled();
        int childCount = getChildCount();
        for (int i = childCount - 1; i >= 0; i--) {
            list.add(getChildAt(!zIsChildrenDrawingOrderEnabled ? i : getChildDrawingOrder(childCount, i)));
        }
        if (TOP_SORTED_CHILDREN_COMPARATOR == null) {
            return;
        }
        Collections.sort(list, TOP_SORTED_CHILDREN_COMPARATOR);
    }

    private boolean hasDependencies(View view) {
        return this.mChildDag.hasOutgoingEdges(view);
    }

    private void layoutChild(View view, int i) {
        LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        Rect rectAcquireTempRect = acquireTempRect();
        rectAcquireTempRect.set(getPaddingLeft() + layoutParams.leftMargin, getPaddingTop() + layoutParams.topMargin, (getWidth() - getPaddingRight()) - layoutParams.rightMargin, (getHeight() - getPaddingBottom()) - layoutParams.bottomMargin);
        if (this.mLastInsets != null && ViewCompat.getFitsSystemWindows(this) && !ViewCompat.getFitsSystemWindows(view)) {
            rectAcquireTempRect.left += this.mLastInsets.getSystemWindowInsetLeft();
            rectAcquireTempRect.top += this.mLastInsets.getSystemWindowInsetTop();
            rectAcquireTempRect.right -= this.mLastInsets.getSystemWindowInsetRight();
            rectAcquireTempRect.bottom -= this.mLastInsets.getSystemWindowInsetBottom();
        }
        Rect rectAcquireTempRect2 = acquireTempRect();
        GravityCompat.apply(resolveGravity(layoutParams.gravity), view.getMeasuredWidth(), view.getMeasuredHeight(), rectAcquireTempRect, rectAcquireTempRect2, i);
        view.layout(rectAcquireTempRect2.left, rectAcquireTempRect2.top, rectAcquireTempRect2.right, rectAcquireTempRect2.bottom);
        releaseTempRect(rectAcquireTempRect);
        releaseTempRect(rectAcquireTempRect2);
    }

    private void layoutChildWithAnchor(View view, View view2, int i) {
        Rect rectAcquireTempRect = acquireTempRect();
        Rect rectAcquireTempRect2 = acquireTempRect();
        try {
            getDescendantRect(view2, rectAcquireTempRect);
            getDesiredAnchoredChildRect(view, i, rectAcquireTempRect, rectAcquireTempRect2);
            view.layout(rectAcquireTempRect2.left, rectAcquireTempRect2.top, rectAcquireTempRect2.right, rectAcquireTempRect2.bottom);
        } finally {
            releaseTempRect(rectAcquireTempRect);
            releaseTempRect(rectAcquireTempRect2);
        }
    }

    private void layoutChildWithKeyline(View view, int i, int i2) {
        int i3 = 0;
        LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        int absoluteGravity = GravityCompat.getAbsoluteGravity(resolveKeylineGravity(layoutParams.gravity), i2);
        int i4 = absoluteGravity & 7;
        int i5 = absoluteGravity & 112;
        int width = getWidth();
        int height = getHeight();
        int measuredWidth = view.getMeasuredWidth();
        int measuredHeight = view.getMeasuredHeight();
        if (i2 == 1) {
            i = width - i;
        }
        int keyline = getKeyline(i) - measuredWidth;
        switch (i4) {
            case 1:
                keyline += measuredWidth / 2;
                break;
            case 5:
                keyline += measuredWidth;
                break;
        }
        switch (i5) {
            case 16:
                i3 = (measuredHeight / 2) + 0;
                break;
            case 80:
                i3 = measuredHeight + 0;
                break;
        }
        int iMax = Math.max(getPaddingLeft() + layoutParams.leftMargin, Math.min(keyline, ((width - getPaddingRight()) - measuredWidth) - layoutParams.rightMargin));
        int iMax2 = Math.max(getPaddingTop() + layoutParams.topMargin, Math.min(i3, ((height - getPaddingBottom()) - measuredHeight) - layoutParams.bottomMargin));
        view.layout(iMax, iMax2, iMax + measuredWidth, iMax2 + measuredHeight);
    }

    private void offsetChildByInset(View view, Rect rect, int i) {
        int i2;
        boolean z;
        int height;
        int i3;
        boolean z2;
        boolean z3;
        if (!ViewCompat.isLaidOut(view) || view.getWidth() <= 0 || view.getHeight() <= 0) {
            return;
        }
        LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        Behavior behavior = layoutParams.getBehavior();
        Rect rectAcquireTempRect = acquireTempRect();
        Rect rectAcquireTempRect2 = acquireTempRect();
        rectAcquireTempRect2.set(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
        if (behavior == null || !behavior.getInsetDodgeRect(this, view, rectAcquireTempRect)) {
            rectAcquireTempRect.set(rectAcquireTempRect2);
        } else if (!rectAcquireTempRect2.contains(rectAcquireTempRect)) {
            throw new IllegalArgumentException("Rect should be within the child's bounds. Rect:" + rectAcquireTempRect.toShortString() + " | Bounds:" + rectAcquireTempRect2.toShortString());
        }
        releaseTempRect(rectAcquireTempRect2);
        if (rectAcquireTempRect.isEmpty()) {
            releaseTempRect(rectAcquireTempRect);
            return;
        }
        int absoluteGravity = GravityCompat.getAbsoluteGravity(layoutParams.dodgeInsetEdges, i);
        if ((absoluteGravity & 48) == 48 && (i2 = (rectAcquireTempRect.top - layoutParams.topMargin) - layoutParams.mInsetOffsetY) < rect.top) {
            setInsetOffsetY(view, rect.top - i2);
            z = true;
        } else {
            z = false;
        }
        if ((absoluteGravity & 80) == 80 && (height = ((getHeight() - rectAcquireTempRect.bottom) - layoutParams.bottomMargin) + layoutParams.mInsetOffsetY) < rect.bottom) {
            setInsetOffsetY(view, height - rect.bottom);
            z = true;
        }
        if (!z) {
            setInsetOffsetY(view, 0);
        }
        if ((absoluteGravity & 3) == 3 && (i3 = (rectAcquireTempRect.left - layoutParams.leftMargin) - layoutParams.mInsetOffsetX) < rect.left) {
            setInsetOffsetX(view, rect.left - i3);
            z2 = true;
        } else {
            z2 = false;
        }
        if ((absoluteGravity & 5) != 5) {
            z3 = z2;
        } else {
            int width = layoutParams.mInsetOffsetX + ((getWidth() - rectAcquireTempRect.right) - layoutParams.rightMargin);
            if (width >= rect.right) {
                z3 = z2;
            } else {
                setInsetOffsetX(view, width - rect.right);
                z3 = true;
            }
        }
        if (!z3) {
            setInsetOffsetX(view, 0);
        }
        releaseTempRect(rectAcquireTempRect);
    }

    /* JADX WARN: Multi-variable type inference failed */
    static Behavior parseBehavior(Context context, AttributeSet attributeSet, String str) {
        Map<String, Constructor<Behavior>> map;
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        if (str.startsWith(".")) {
            str = context.getPackageName() + str;
        } else if (str.indexOf(46) < 0 && !TextUtils.isEmpty(WIDGET_PACKAGE_NAME)) {
            str = WIDGET_PACKAGE_NAME + '.' + str;
        }
        try {
            Map<String, Constructor<Behavior>> map2 = sConstructors.get();
            if (map2 != null) {
                map = map2;
            } else {
                HashMap map3 = new HashMap();
                sConstructors.set(map3);
                map = map3;
            }
            Constructor<Behavior> constructor = map.get(str);
            if (constructor == null) {
                constructor = Class.forName(str, true, context.getClassLoader()).getConstructor(CONSTRUCTOR_PARAMS);
                constructor.setAccessible(true);
                map.put(str, constructor);
            }
            return constructor.newInstance(context, attributeSet);
        } catch (Exception e) {
            throw new RuntimeException("Could not inflate Behavior subclass " + str, e);
        }
    }

    private boolean performIntercept(MotionEvent motionEvent, int i) {
        boolean z;
        MotionEvent motionEventObtain;
        boolean zOnTouchEvent;
        boolean z2 = false;
        boolean z3 = false;
        MotionEvent motionEvent2 = null;
        int actionMasked = motionEvent.getActionMasked();
        List<View> list = this.mTempList1;
        getTopSortedChildren(list);
        int size = list.size();
        int i2 = 0;
        while (true) {
            if (i2 < size) {
                View view = list.get(i2);
                LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
                Behavior behavior = layoutParams.getBehavior();
                if (!(z2 || z3) || actionMasked == 0) {
                    if (z2 || behavior == null) {
                        zOnTouchEvent = z2;
                    } else {
                        switch (i) {
                            case 0:
                                zOnTouchEvent = behavior.onInterceptTouchEvent(this, view, motionEvent);
                                break;
                            case 1:
                                zOnTouchEvent = behavior.onTouchEvent(this, view, motionEvent);
                                break;
                            default:
                                zOnTouchEvent = z2;
                                break;
                        }
                        if (zOnTouchEvent) {
                            this.mBehaviorTouchView = view;
                        }
                    }
                    boolean zDidBlockInteraction = layoutParams.didBlockInteraction();
                    boolean zIsBlockingInteractionBelow = layoutParams.isBlockingInteractionBelow(this, view);
                    boolean z4 = zIsBlockingInteractionBelow && !zDidBlockInteraction;
                    if (zIsBlockingInteractionBelow && !z4) {
                        z2 = zOnTouchEvent;
                    } else {
                        z2 = zOnTouchEvent;
                        z = z4;
                        motionEventObtain = motionEvent2;
                    }
                } else if (behavior == null) {
                    motionEventObtain = motionEvent2;
                    z = z3;
                } else {
                    if (motionEvent2 != null) {
                        motionEventObtain = motionEvent2;
                    } else {
                        long jUptimeMillis = SystemClock.uptimeMillis();
                        motionEventObtain = MotionEvent.obtain(jUptimeMillis, jUptimeMillis, 3, 0.0f, 0.0f, 0);
                    }
                    switch (i) {
                        case 0:
                            behavior.onInterceptTouchEvent(this, view, motionEventObtain);
                            break;
                        case 1:
                            behavior.onTouchEvent(this, view, motionEventObtain);
                            break;
                    }
                    z = z3;
                }
                i2++;
                z3 = z;
                motionEvent2 = motionEventObtain;
            }
        }
        list.clear();
        return z2;
    }

    private void prepareChildren() {
        this.mDependencySortedChildren.clear();
        this.mChildDag.clear();
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = getChildAt(i);
            LayoutParams resolvedLayoutParams = getResolvedLayoutParams(childAt);
            resolvedLayoutParams.findAnchorView(this, childAt);
            this.mChildDag.addNode(childAt);
            for (int i2 = 0; i2 < childCount; i2++) {
                if (i2 != i) {
                    View childAt2 = getChildAt(i2);
                    if (resolvedLayoutParams.dependsOn(this, childAt, childAt2)) {
                        if (!this.mChildDag.contains(childAt2)) {
                            this.mChildDag.addNode(childAt2);
                        }
                        this.mChildDag.addEdge(childAt2, childAt);
                    }
                }
            }
        }
        this.mDependencySortedChildren.addAll(this.mChildDag.getSortedList());
        Collections.reverse(this.mDependencySortedChildren);
    }

    private static void releaseTempRect(@NonNull Rect rect) {
        rect.setEmpty();
        sRectPool.release(rect);
    }

    private void resetTouchBehaviors() {
        if (this.mBehaviorTouchView != null) {
            Behavior behavior = ((LayoutParams) this.mBehaviorTouchView.getLayoutParams()).getBehavior();
            if (behavior != null) {
                long jUptimeMillis = SystemClock.uptimeMillis();
                MotionEvent motionEventObtain = MotionEvent.obtain(jUptimeMillis, jUptimeMillis, 3, 0.0f, 0.0f, 0);
                behavior.onTouchEvent(this, this.mBehaviorTouchView, motionEventObtain);
                motionEventObtain.recycle();
            }
            this.mBehaviorTouchView = null;
        }
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            ((LayoutParams) getChildAt(i).getLayoutParams()).resetTouchBehaviorTracking();
        }
        this.mDisallowInterceptReset = false;
    }

    private static int resolveAnchoredChildGravity(int i) {
        if (i != 0) {
            return i;
        }
        return 17;
    }

    private static int resolveGravity(int i) {
        int i2 = (i & 7) != 0 ? i : 8388611 | i;
        return (i2 & 112) != 0 ? i2 : i2 | 48;
    }

    private static int resolveKeylineGravity(int i) {
        if (i != 0) {
            return i;
        }
        return 8388661;
    }

    private void setInsetOffsetX(View view, int i) {
        LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        if (layoutParams.mInsetOffsetX == i) {
            return;
        }
        ViewCompat.offsetLeftAndRight(view, i - layoutParams.mInsetOffsetX);
        layoutParams.mInsetOffsetX = i;
    }

    private void setInsetOffsetY(View view, int i) {
        LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        if (layoutParams.mInsetOffsetY == i) {
            return;
        }
        ViewCompat.offsetTopAndBottom(view, i - layoutParams.mInsetOffsetY);
        layoutParams.mInsetOffsetY = i;
    }

    private void setupForInsets() {
        if (Build.VERSION.SDK_INT >= 21) {
            if (!ViewCompat.getFitsSystemWindows(this)) {
                ViewCompat.setOnApplyWindowInsetsListener(this, null);
                return;
            }
            if (this.mApplyWindowInsetsListener == null) {
                this.mApplyWindowInsetsListener = new OnApplyWindowInsetsListener() { // from class: android.support.design.widget.CoordinatorLayout.1
                    @Override // android.support.v4.view.OnApplyWindowInsetsListener
                    public WindowInsetsCompat onApplyWindowInsets(View view, WindowInsetsCompat windowInsetsCompat) {
                        return CoordinatorLayout.this.setWindowInsets(windowInsetsCompat);
                    }
                };
            }
            ViewCompat.setOnApplyWindowInsetsListener(this, this.mApplyWindowInsetsListener);
            setSystemUiVisibility(1280);
        }
    }

    void addPreDrawListener() {
        if (this.mIsAttachedToWindow) {
            if (this.mOnPreDrawListener == null) {
                this.mOnPreDrawListener = new OnPreDrawListener();
            }
            getViewTreeObserver().addOnPreDrawListener(this.mOnPreDrawListener);
        }
        this.mNeedsPreDrawListener = true;
    }

    @Override // android.view.ViewGroup
    protected boolean checkLayoutParams(ViewGroup.LayoutParams layoutParams) {
        return (layoutParams instanceof LayoutParams) && super.checkLayoutParams(layoutParams);
    }

    public void dispatchDependentViewsChanged(View view) {
        int i = 0;
        List incomingEdges = this.mChildDag.getIncomingEdges(view);
        if (incomingEdges == null || incomingEdges.isEmpty()) {
            return;
        }
        while (true) {
            int i2 = i;
            if (i2 >= incomingEdges.size()) {
                return;
            }
            View view2 = (View) incomingEdges.get(i2);
            Behavior behavior = ((LayoutParams) view2.getLayoutParams()).getBehavior();
            if (behavior != null) {
                behavior.onDependentViewChanged(this, view2, view);
            }
            i = i2 + 1;
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:16:0x0031  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public boolean doViewsOverlap(android.view.View r7, android.view.View r8) {
        /*
            r6 = this;
            r2 = 1
            r1 = 0
            int r0 = r7.getVisibility()
            if (r0 == 0) goto L9
        L8:
            return r1
        L9:
            int r0 = r8.getVisibility()
            if (r0 != 0) goto L8
            android.graphics.Rect r3 = acquireTempRect()
            android.view.ViewParent r0 = r7.getParent()
            if (r0 != r6) goto L39
            r0 = r1
        L1a:
            r6.getChildRect(r7, r0, r3)
            android.graphics.Rect r4 = acquireTempRect()
            android.view.ViewParent r0 = r8.getParent()
            if (r0 != r6) goto L3b
            r0 = r1
        L28:
            r6.getChildRect(r8, r0, r4)
            int r0 = r3.left     // Catch: java.lang.Throwable -> L50
            int r5 = r4.right     // Catch: java.lang.Throwable -> L50
            if (r0 <= r5) goto L3d
        L31:
            r2 = r1
        L32:
            releaseTempRect(r3)
            releaseTempRect(r4)
            return r2
        L39:
            r0 = r2
            goto L1a
        L3b:
            r0 = r2
            goto L28
        L3d:
            int r0 = r3.top     // Catch: java.lang.Throwable -> L50
            int r5 = r4.bottom     // Catch: java.lang.Throwable -> L50
            if (r0 > r5) goto L31
            int r0 = r3.right     // Catch: java.lang.Throwable -> L50
            int r5 = r4.left     // Catch: java.lang.Throwable -> L50
            if (r0 < r5) goto L31
            int r0 = r3.bottom     // Catch: java.lang.Throwable -> L50
            int r5 = r4.top     // Catch: java.lang.Throwable -> L50
            if (r0 >= r5) goto L32
            goto L31
        L50:
            r0 = move-exception
            releaseTempRect(r3)
            releaseTempRect(r4)
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.design.widget.CoordinatorLayout.doViewsOverlap(android.view.View, android.view.View):boolean");
    }

    @Override // android.view.ViewGroup
    protected boolean drawChild(Canvas canvas, View view, long j) {
        LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        if (layoutParams.mBehavior != null) {
            float scrimOpacity = layoutParams.mBehavior.getScrimOpacity(this, view);
            if (scrimOpacity > 0.0f) {
                if (this.mScrimPaint == null) {
                    this.mScrimPaint = new Paint();
                }
                this.mScrimPaint.setColor(layoutParams.mBehavior.getScrimColor(this, view));
                this.mScrimPaint.setAlpha(MathUtils.clamp(Math.round(scrimOpacity * 255.0f), 0, 255));
                int iSave = canvas.save();
                if (view.isOpaque()) {
                    canvas.clipRect(view.getLeft(), view.getTop(), view.getRight(), view.getBottom(), Region.Op.DIFFERENCE);
                }
                canvas.drawRect(getPaddingLeft(), getPaddingTop(), getWidth() - getPaddingRight(), getHeight() - getPaddingBottom(), this.mScrimPaint);
                canvas.restoreToCount(iSave);
            }
        }
        return super.drawChild(canvas, view, j);
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void drawableStateChanged() {
        boolean state = false;
        super.drawableStateChanged();
        int[] drawableState = getDrawableState();
        Drawable drawable = this.mStatusBarBackground;
        if (drawable != null && drawable.isStateful()) {
            state = drawable.setState(drawableState) | false;
        }
        if (state) {
            invalidate();
        }
    }

    void ensurePreDrawListener() {
        boolean z = false;
        int childCount = getChildCount();
        int i = 0;
        while (true) {
            if (i >= childCount) {
                break;
            }
            if (hasDependencies(getChildAt(i))) {
                z = true;
                break;
            }
            i++;
        }
        if (z == this.mNeedsPreDrawListener) {
            return;
        }
        if (z) {
            addPreDrawListener();
        } else {
            removePreDrawListener();
        }
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
        return !(layoutParams instanceof LayoutParams) ? !(layoutParams instanceof ViewGroup.MarginLayoutParams) ? new LayoutParams(layoutParams) : new LayoutParams((ViewGroup.MarginLayoutParams) layoutParams) : new LayoutParams((LayoutParams) layoutParams);
    }

    void getChildRect(View view, boolean z, Rect rect) {
        if (view.isLayoutRequested() || view.getVisibility() == 8) {
            rect.setEmpty();
        } else if (z) {
            getDescendantRect(view, rect);
        } else {
            rect.set(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
        }
    }

    @NonNull
    public List<View> getDependencies(@NonNull View view) {
        List<View> outgoingEdges = this.mChildDag.getOutgoingEdges(view);
        this.mTempDependenciesList.clear();
        if (outgoingEdges != null) {
            this.mTempDependenciesList.addAll(outgoingEdges);
        }
        return this.mTempDependenciesList;
    }

    @VisibleForTesting
    final List<View> getDependencySortedChildren() {
        prepareChildren();
        return Collections.unmodifiableList(this.mDependencySortedChildren);
    }

    @NonNull
    public List<View> getDependents(@NonNull View view) {
        List incomingEdges = this.mChildDag.getIncomingEdges(view);
        this.mTempDependenciesList.clear();
        if (incomingEdges != null) {
            this.mTempDependenciesList.addAll(incomingEdges);
        }
        return this.mTempDependenciesList;
    }

    void getDescendantRect(View view, Rect rect) {
        ViewGroupUtils.getDescendantRect(this, view, rect);
    }

    void getDesiredAnchoredChildRect(View view, int i, Rect rect, Rect rect2) {
        LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        int measuredWidth = view.getMeasuredWidth();
        int measuredHeight = view.getMeasuredHeight();
        getDesiredAnchoredChildRectWithoutConstraints(view, i, rect, rect2, layoutParams, measuredWidth, measuredHeight);
        constrainChildRect(layoutParams, rect2, measuredWidth, measuredHeight);
    }

    void getLastChildRect(View view, Rect rect) {
        rect.set(((LayoutParams) view.getLayoutParams()).getLastChildRect());
    }

    final WindowInsetsCompat getLastWindowInsets() {
        return this.mLastInsets;
    }

    @Override // android.view.ViewGroup, android.support.v4.view.NestedScrollingParent
    public int getNestedScrollAxes() {
        return this.mNestedScrollingParentHelper.getNestedScrollAxes();
    }

    LayoutParams getResolvedLayoutParams(View view) {
        DefaultBehavior defaultBehavior;
        DefaultBehavior defaultBehavior2 = null;
        LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        if (!layoutParams.mBehaviorResolved) {
            Class<?> superclass = view.getClass();
            while (true) {
                if (superclass == null) {
                    defaultBehavior = defaultBehavior2;
                    break;
                }
                defaultBehavior2 = (DefaultBehavior) superclass.getAnnotation(DefaultBehavior.class);
                if (defaultBehavior2 != null) {
                    defaultBehavior = defaultBehavior2;
                    break;
                }
                superclass = superclass.getSuperclass();
            }
            if (defaultBehavior != null) {
                try {
                    layoutParams.setBehavior(defaultBehavior.value().getDeclaredConstructor(new Class[0]).newInstance(new Object[0]));
                } catch (Exception e) {
                    Log.e(TAG, "Default behavior class " + defaultBehavior.value().getName() + " could not be instantiated. Did you forget a default constructor?", e);
                }
            }
            layoutParams.mBehaviorResolved = true;
        }
        return layoutParams;
    }

    @Nullable
    public Drawable getStatusBarBackground() {
        return this.mStatusBarBackground;
    }

    @Override // android.view.View
    protected int getSuggestedMinimumHeight() {
        return Math.max(super.getSuggestedMinimumHeight(), getPaddingTop() + getPaddingBottom());
    }

    @Override // android.view.View
    protected int getSuggestedMinimumWidth() {
        return Math.max(super.getSuggestedMinimumWidth(), getPaddingLeft() + getPaddingRight());
    }

    public boolean isPointInChildBounds(View view, int i, int i2) {
        Rect rectAcquireTempRect = acquireTempRect();
        getDescendantRect(view, rectAcquireTempRect);
        try {
            return rectAcquireTempRect.contains(i, i2);
        } finally {
            releaseTempRect(rectAcquireTempRect);
        }
    }

    void offsetChildToAnchor(View view, int i) {
        Behavior behavior;
        LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        if (layoutParams.mAnchorView == null) {
            return;
        }
        Rect rectAcquireTempRect = acquireTempRect();
        Rect rectAcquireTempRect2 = acquireTempRect();
        Rect rectAcquireTempRect3 = acquireTempRect();
        getDescendantRect(layoutParams.mAnchorView, rectAcquireTempRect);
        getChildRect(view, false, rectAcquireTempRect2);
        int measuredWidth = view.getMeasuredWidth();
        int measuredHeight = view.getMeasuredHeight();
        getDesiredAnchoredChildRectWithoutConstraints(view, i, rectAcquireTempRect, rectAcquireTempRect3, layoutParams, measuredWidth, measuredHeight);
        boolean z = (rectAcquireTempRect3.left == rectAcquireTempRect2.left && rectAcquireTempRect3.top == rectAcquireTempRect2.top) ? false : true;
        constrainChildRect(layoutParams, rectAcquireTempRect3, measuredWidth, measuredHeight);
        int i2 = rectAcquireTempRect3.left - rectAcquireTempRect2.left;
        int i3 = rectAcquireTempRect3.top - rectAcquireTempRect2.top;
        if (i2 != 0) {
            ViewCompat.offsetLeftAndRight(view, i2);
        }
        if (i3 != 0) {
            ViewCompat.offsetTopAndBottom(view, i3);
        }
        if (z && (behavior = layoutParams.getBehavior()) != null) {
            behavior.onDependentViewChanged(this, view, layoutParams.mAnchorView);
        }
        releaseTempRect(rectAcquireTempRect);
        releaseTempRect(rectAcquireTempRect2);
        releaseTempRect(rectAcquireTempRect3);
    }

    @Override // android.view.ViewGroup, android.view.View
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        resetTouchBehaviors();
        if (this.mNeedsPreDrawListener) {
            if (this.mOnPreDrawListener == null) {
                this.mOnPreDrawListener = new OnPreDrawListener();
            }
            getViewTreeObserver().addOnPreDrawListener(this.mOnPreDrawListener);
        }
        if (this.mLastInsets == null && ViewCompat.getFitsSystemWindows(this)) {
            ViewCompat.requestApplyInsets(this);
        }
        this.mIsAttachedToWindow = true;
    }

    /* JADX WARN: Removed duplicated region for block: B:43:0x00d1  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    final void onChildViewsChanged(int r15) {
        /*
            Method dump skipped, instruction units count: 296
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.design.widget.CoordinatorLayout.onChildViewsChanged(int):void");
    }

    @Override // android.view.ViewGroup, android.view.View
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        resetTouchBehaviors();
        if (this.mNeedsPreDrawListener && this.mOnPreDrawListener != null) {
            getViewTreeObserver().removeOnPreDrawListener(this.mOnPreDrawListener);
        }
        if (this.mNestedScrollingTarget != null) {
            onStopNestedScroll(this.mNestedScrollingTarget);
        }
        this.mIsAttachedToWindow = false;
    }

    @Override // android.view.View
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.mDrawStatusBarBackground && this.mStatusBarBackground != null) {
            int systemWindowInsetTop = this.mLastInsets == null ? 0 : this.mLastInsets.getSystemWindowInsetTop();
            if (systemWindowInsetTop > 0) {
                this.mStatusBarBackground.setBounds(0, 0, getWidth(), systemWindowInsetTop);
                this.mStatusBarBackground.draw(canvas);
            }
        }
    }

    @Override // android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        MotionEvent motionEvent2 = null;
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked == 0) {
            resetTouchBehaviors();
        }
        boolean zPerformIntercept = performIntercept(motionEvent, 0);
        if (0 != 0) {
            motionEvent2.recycle();
        }
        if (actionMasked == 1 || actionMasked == 3) {
            resetTouchBehaviors();
        }
        return zPerformIntercept;
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        Behavior behavior;
        int layoutDirection = ViewCompat.getLayoutDirection(this);
        int size = this.mDependencySortedChildren.size();
        for (int i5 = 0; i5 < size; i5++) {
            View view = this.mDependencySortedChildren.get(i5);
            if (view.getVisibility() != 8 && ((behavior = ((LayoutParams) view.getLayoutParams()).getBehavior()) == null || !behavior.onLayoutChild(this, view, layoutDirection))) {
                onLayoutChild(view, layoutDirection);
            }
        }
    }

    public void onLayoutChild(View view, int i) {
        LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        if (layoutParams.checkAnchorChanged()) {
            throw new IllegalStateException("An anchor may not be changed after CoordinatorLayout measurement begins before layout is complete.");
        }
        if (layoutParams.mAnchorView != null) {
            layoutChildWithAnchor(view, layoutParams.mAnchorView, i);
        } else if (layoutParams.keyline < 0) {
            layoutChild(view, i);
        } else {
            layoutChildWithKeyline(view, layoutParams.keyline, i);
        }
    }

    @Override // android.view.View
    protected void onMeasure(int i, int i2) {
        int iCombineMeasuredStates;
        int iMax;
        int i3;
        int iMakeMeasureSpec;
        int iMakeMeasureSpec2;
        prepareChildren();
        ensurePreDrawListener();
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();
        int layoutDirection = ViewCompat.getLayoutDirection(this);
        boolean z = layoutDirection == 1;
        int mode = View.MeasureSpec.getMode(i);
        int size = View.MeasureSpec.getSize(i);
        int mode2 = View.MeasureSpec.getMode(i2);
        int size2 = View.MeasureSpec.getSize(i2);
        int i4 = paddingLeft + paddingRight;
        int i5 = paddingTop + paddingBottom;
        int suggestedMinimumWidth = getSuggestedMinimumWidth();
        int suggestedMinimumHeight = getSuggestedMinimumHeight();
        int i6 = 0;
        boolean z2 = this.mLastInsets != null && ViewCompat.getFitsSystemWindows(this);
        int size3 = this.mDependencySortedChildren.size();
        int i7 = 0;
        while (i7 < size3) {
            View view = this.mDependencySortedChildren.get(i7);
            if (view.getVisibility() != 8) {
                LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
                int iMax2 = 0;
                if (layoutParams.keyline >= 0 && mode != 0) {
                    int keyline = getKeyline(layoutParams.keyline);
                    int absoluteGravity = GravityCompat.getAbsoluteGravity(resolveKeylineGravity(layoutParams.gravity), layoutDirection) & 7;
                    if ((absoluteGravity == 3 && !z) || (absoluteGravity == 5 && z)) {
                        iMax2 = Math.max(0, (size - paddingRight) - keyline);
                    } else if ((absoluteGravity == 5 && !z) || (absoluteGravity == 3 && z)) {
                        iMax2 = Math.max(0, keyline - paddingLeft);
                    }
                }
                if (z2 && !ViewCompat.getFitsSystemWindows(view)) {
                    int systemWindowInsetLeft = this.mLastInsets.getSystemWindowInsetLeft() + this.mLastInsets.getSystemWindowInsetRight();
                    int systemWindowInsetTop = this.mLastInsets.getSystemWindowInsetTop() + this.mLastInsets.getSystemWindowInsetBottom();
                    iMakeMeasureSpec = View.MeasureSpec.makeMeasureSpec(size - systemWindowInsetLeft, mode);
                    iMakeMeasureSpec2 = View.MeasureSpec.makeMeasureSpec(size2 - systemWindowInsetTop, mode2);
                } else {
                    iMakeMeasureSpec2 = i2;
                    iMakeMeasureSpec = i;
                }
                Behavior behavior = layoutParams.getBehavior();
                if (behavior == null || !behavior.onMeasureChild(this, view, iMakeMeasureSpec, iMax2, iMakeMeasureSpec2, 0)) {
                    onMeasureChild(view, iMakeMeasureSpec, iMax2, iMakeMeasureSpec2, 0);
                }
                int iMax3 = Math.max(suggestedMinimumWidth, view.getMeasuredWidth() + i4 + layoutParams.leftMargin + layoutParams.rightMargin);
                iMax = Math.max(suggestedMinimumHeight, view.getMeasuredHeight() + i5 + layoutParams.topMargin + layoutParams.bottomMargin);
                iCombineMeasuredStates = View.combineMeasuredStates(i6, view.getMeasuredState());
                i3 = iMax3;
            } else {
                iCombineMeasuredStates = i6;
                iMax = suggestedMinimumHeight;
                i3 = suggestedMinimumWidth;
            }
            i7++;
            i6 = iCombineMeasuredStates;
            suggestedMinimumHeight = iMax;
            suggestedMinimumWidth = i3;
        }
        setMeasuredDimension(View.resolveSizeAndState(suggestedMinimumWidth, i, (-16777216) & i6), View.resolveSizeAndState(suggestedMinimumHeight, i2, i6 << 16));
    }

    public void onMeasureChild(View view, int i, int i2, int i3, int i4) {
        measureChildWithMargins(view, i, i2, i3, i4);
    }

    @Override // android.view.ViewGroup, android.view.ViewParent, android.support.v4.view.NestedScrollingParent
    public boolean onNestedFling(View view, float f, float f2, boolean z) {
        boolean zOnNestedFling;
        int childCount = getChildCount();
        int i = 0;
        boolean z2 = false;
        while (i < childCount) {
            View childAt = getChildAt(i);
            if (childAt.getVisibility() != 8) {
                LayoutParams layoutParams = (LayoutParams) childAt.getLayoutParams();
                if (layoutParams.isNestedScrollAccepted(0)) {
                    Behavior behavior = layoutParams.getBehavior();
                    zOnNestedFling = behavior == null ? z2 : behavior.onNestedFling(this, childAt, view, f, f2, z) | z2;
                } else {
                    zOnNestedFling = z2;
                }
            } else {
                zOnNestedFling = z2;
            }
            i++;
            z2 = zOnNestedFling;
        }
        if (z2) {
            onChildViewsChanged(1);
        }
        return z2;
    }

    @Override // android.view.ViewGroup, android.view.ViewParent, android.support.v4.view.NestedScrollingParent
    public boolean onNestedPreFling(View view, float f, float f2) {
        boolean zOnNestedPreFling;
        int childCount = getChildCount();
        int i = 0;
        boolean z = false;
        while (i < childCount) {
            View childAt = getChildAt(i);
            if (childAt.getVisibility() != 8) {
                LayoutParams layoutParams = (LayoutParams) childAt.getLayoutParams();
                if (layoutParams.isNestedScrollAccepted(0)) {
                    Behavior behavior = layoutParams.getBehavior();
                    zOnNestedPreFling = behavior == null ? z : behavior.onNestedPreFling(this, childAt, view, f, f2) | z;
                } else {
                    zOnNestedPreFling = z;
                }
            } else {
                zOnNestedPreFling = z;
            }
            i++;
            z = zOnNestedPreFling;
        }
        return z;
    }

    @Override // android.view.ViewGroup, android.view.ViewParent, android.support.v4.view.NestedScrollingParent
    public void onNestedPreScroll(View view, int i, int i2, int[] iArr) {
        onNestedPreScroll(view, i, i2, iArr, 0);
    }

    @Override // android.support.v4.view.NestedScrollingParent2
    public void onNestedPreScroll(View view, int i, int i2, int[] iArr, int i3) {
        boolean z;
        int i4;
        int iMin;
        int i5 = 0;
        int i6 = 0;
        boolean z2 = false;
        int childCount = getChildCount();
        int i7 = 0;
        while (i7 < childCount) {
            View childAt = getChildAt(i7);
            if (childAt.getVisibility() != 8) {
                LayoutParams layoutParams = (LayoutParams) childAt.getLayoutParams();
                if (layoutParams.isNestedScrollAccepted(i3)) {
                    Behavior behavior = layoutParams.getBehavior();
                    if (behavior == null) {
                        z = z2;
                        i4 = i5;
                        iMin = i6;
                    } else {
                        int[] iArr2 = this.mTempIntPair;
                        this.mTempIntPair[1] = 0;
                        iArr2[0] = 0;
                        behavior.onNestedPreScroll(this, childAt, view, i, i2, this.mTempIntPair, i3);
                        int iMin2 = i <= 0 ? Math.min(i5, this.mTempIntPair[0]) : Math.max(i5, this.mTempIntPair[0]);
                        iMin = i2 <= 0 ? Math.min(i6, this.mTempIntPair[1]) : Math.max(i6, this.mTempIntPair[1]);
                        i4 = iMin2;
                        z = true;
                    }
                } else {
                    z = z2;
                    i4 = i5;
                    iMin = i6;
                }
            } else {
                z = z2;
                i4 = i5;
                iMin = i6;
            }
            i7++;
            i6 = iMin;
            i5 = i4;
            z2 = z;
        }
        iArr[0] = i5;
        iArr[1] = i6;
        if (z2) {
            onChildViewsChanged(1);
        }
    }

    @Override // android.view.ViewGroup, android.view.ViewParent, android.support.v4.view.NestedScrollingParent
    public void onNestedScroll(View view, int i, int i2, int i3, int i4) {
        onNestedScroll(view, i, i2, i3, i4, 0);
    }

    @Override // android.support.v4.view.NestedScrollingParent2
    public void onNestedScroll(View view, int i, int i2, int i3, int i4, int i5) {
        boolean z;
        int childCount = getChildCount();
        boolean z2 = false;
        int i6 = 0;
        while (i6 < childCount) {
            View childAt = getChildAt(i6);
            if (childAt.getVisibility() != 8) {
                LayoutParams layoutParams = (LayoutParams) childAt.getLayoutParams();
                if (layoutParams.isNestedScrollAccepted(i5)) {
                    Behavior behavior = layoutParams.getBehavior();
                    if (behavior == null) {
                        z = z2;
                    } else {
                        behavior.onNestedScroll(this, childAt, view, i, i2, i3, i4, i5);
                        z = true;
                    }
                } else {
                    z = z2;
                }
            } else {
                z = z2;
            }
            i6++;
            z2 = z;
        }
        if (z2) {
            onChildViewsChanged(1);
        }
    }

    @Override // android.view.ViewGroup, android.view.ViewParent, android.support.v4.view.NestedScrollingParent
    public void onNestedScrollAccepted(View view, View view2, int i) {
        onNestedScrollAccepted(view, view2, i, 0);
    }

    @Override // android.support.v4.view.NestedScrollingParent2
    public void onNestedScrollAccepted(View view, View view2, int i, int i2) {
        Behavior behavior;
        this.mNestedScrollingParentHelper.onNestedScrollAccepted(view, view2, i, i2);
        this.mNestedScrollingTarget = view2;
        int childCount = getChildCount();
        for (int i3 = 0; i3 < childCount; i3++) {
            View childAt = getChildAt(i3);
            LayoutParams layoutParams = (LayoutParams) childAt.getLayoutParams();
            if (layoutParams.isNestedScrollAccepted(i2) && (behavior = layoutParams.getBehavior()) != null) {
                behavior.onNestedScrollAccepted(this, childAt, view, view2, i, i2);
            }
        }
    }

    @Override // android.view.View
    protected void onRestoreInstanceState(Parcelable parcelable) {
        Parcelable parcelable2;
        if (!(parcelable instanceof SavedState)) {
            super.onRestoreInstanceState(parcelable);
            return;
        }
        SavedState savedState = (SavedState) parcelable;
        super.onRestoreInstanceState(savedState.getSuperState());
        SparseArray<Parcelable> sparseArray = savedState.behaviorStates;
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = getChildAt(i);
            int id = childAt.getId();
            Behavior behavior = getResolvedLayoutParams(childAt).getBehavior();
            if (id != -1 && behavior != null && (parcelable2 = sparseArray.get(id)) != null) {
                behavior.onRestoreInstanceState(this, childAt, parcelable2);
            }
        }
    }

    @Override // android.view.View
    protected Parcelable onSaveInstanceState() {
        Parcelable parcelableOnSaveInstanceState;
        SavedState savedState = new SavedState(super.onSaveInstanceState());
        SparseArray<Parcelable> sparseArray = new SparseArray<>();
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = getChildAt(i);
            int id = childAt.getId();
            Behavior behavior = ((LayoutParams) childAt.getLayoutParams()).getBehavior();
            if (id != -1 && behavior != null && (parcelableOnSaveInstanceState = behavior.onSaveInstanceState(this, childAt)) != null) {
                sparseArray.append(id, parcelableOnSaveInstanceState);
            }
        }
        savedState.behaviorStates = sparseArray;
        return savedState;
    }

    @Override // android.view.ViewGroup, android.view.ViewParent, android.support.v4.view.NestedScrollingParent
    public boolean onStartNestedScroll(View view, View view2, int i) {
        return onStartNestedScroll(view, view2, i, 0);
    }

    @Override // android.support.v4.view.NestedScrollingParent2
    public boolean onStartNestedScroll(View view, View view2, int i, int i2) {
        boolean z;
        boolean z2 = false;
        int childCount = getChildCount();
        int i3 = 0;
        while (i3 < childCount) {
            View childAt = getChildAt(i3);
            if (childAt.getVisibility() != 8) {
                LayoutParams layoutParams = (LayoutParams) childAt.getLayoutParams();
                Behavior behavior = layoutParams.getBehavior();
                if (behavior == null) {
                    layoutParams.setNestedScrollAccepted(i2, false);
                    z = z2;
                } else {
                    boolean zOnStartNestedScroll = behavior.onStartNestedScroll(this, childAt, view, view2, i, i2);
                    z = z2 | zOnStartNestedScroll;
                    layoutParams.setNestedScrollAccepted(i2, zOnStartNestedScroll);
                }
            } else {
                z = z2;
            }
            i3++;
            z2 = z;
        }
        return z2;
    }

    @Override // android.view.ViewGroup, android.view.ViewParent, android.support.v4.view.NestedScrollingParent
    public void onStopNestedScroll(View view) {
        onStopNestedScroll(view, 0);
    }

    @Override // android.support.v4.view.NestedScrollingParent2
    public void onStopNestedScroll(View view, int i) {
        this.mNestedScrollingParentHelper.onStopNestedScroll(view, i);
        int childCount = getChildCount();
        for (int i2 = 0; i2 < childCount; i2++) {
            View childAt = getChildAt(i2);
            LayoutParams layoutParams = (LayoutParams) childAt.getLayoutParams();
            if (layoutParams.isNestedScrollAccepted(i)) {
                Behavior behavior = layoutParams.getBehavior();
                if (behavior != null) {
                    behavior.onStopNestedScroll(this, childAt, view, i);
                }
                layoutParams.resetNestedScroll(i);
                layoutParams.resetChangedAfterNestedScroll();
            }
        }
        this.mNestedScrollingTarget = null;
    }

    /* JADX WARN: Removed duplicated region for block: B:10:0x0021  */
    /* JADX WARN: Removed duplicated region for block: B:13:0x0027  */
    /* JADX WARN: Removed duplicated region for block: B:19:0x003c  */
    /* JADX WARN: Removed duplicated region for block: B:24:0x0055  */
    @Override // android.view.View
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public boolean onTouchEvent(android.view.MotionEvent r12) {
        /*
            r11 = this;
            r4 = 3
            r10 = 1
            r5 = 0
            r2 = 0
            r7 = 0
            int r9 = r12.getActionMasked()
            android.view.View r0 = r11.mBehaviorTouchView
            if (r0 == 0) goto L2b
            r1 = r7
        Le:
            android.view.View r0 = r11.mBehaviorTouchView
            android.view.ViewGroup$LayoutParams r0 = r0.getLayoutParams()
            android.support.design.widget.CoordinatorLayout$LayoutParams r0 = (android.support.design.widget.CoordinatorLayout.LayoutParams) r0
            android.support.design.widget.CoordinatorLayout$Behavior r0 = r0.getBehavior()
            if (r0 != 0) goto L34
            r8 = r7
        L1d:
            android.view.View r0 = r11.mBehaviorTouchView
            if (r0 == 0) goto L3c
            if (r1 != 0) goto L42
        L23:
            if (r2 != 0) goto L55
        L25:
            if (r9 != r10) goto L59
        L27:
            r11.resetTouchBehaviors()
        L2a:
            return r8
        L2b:
            boolean r0 = r11.performIntercept(r12, r10)
            if (r0 != 0) goto L5c
            r1 = r0
            r8 = r7
            goto L1d
        L34:
            android.view.View r3 = r11.mBehaviorTouchView
            boolean r0 = r0.onTouchEvent(r11, r3, r12)
            r8 = r0
            goto L1d
        L3c:
            boolean r0 = super.onTouchEvent(r12)
            r8 = r8 | r0
            goto L23
        L42:
            if (r2 == 0) goto L4a
            r0 = r2
        L45:
            super.onTouchEvent(r0)
            r2 = r0
            goto L23
        L4a:
            long r0 = android.os.SystemClock.uptimeMillis()
            r2 = r0
            r6 = r5
            android.view.MotionEvent r0 = android.view.MotionEvent.obtain(r0, r2, r4, r5, r6, r7)
            goto L45
        L55:
            r2.recycle()
            goto L25
        L59:
            if (r9 == r4) goto L27
            goto L2a
        L5c:
            r1 = r0
            goto Le
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.design.widget.CoordinatorLayout.onTouchEvent(android.view.MotionEvent):boolean");
    }

    void recordLastChildRect(View view, Rect rect) {
        ((LayoutParams) view.getLayoutParams()).setLastChildRect(rect);
    }

    void removePreDrawListener() {
        if (this.mIsAttachedToWindow && this.mOnPreDrawListener != null) {
            getViewTreeObserver().removeOnPreDrawListener(this.mOnPreDrawListener);
        }
        this.mNeedsPreDrawListener = false;
    }

    @Override // android.view.ViewGroup, android.view.ViewParent
    public boolean requestChildRectangleOnScreen(View view, Rect rect, boolean z) {
        Behavior behavior = ((LayoutParams) view.getLayoutParams()).getBehavior();
        if (behavior != null && behavior.onRequestChildRectangleOnScreen(this, view, rect, z)) {
            return true;
        }
        return super.requestChildRectangleOnScreen(view, rect, z);
    }

    @Override // android.view.ViewGroup, android.view.ViewParent
    public void requestDisallowInterceptTouchEvent(boolean z) {
        super.requestDisallowInterceptTouchEvent(z);
        if (z && !this.mDisallowInterceptReset) {
            resetTouchBehaviors();
            this.mDisallowInterceptReset = true;
        }
    }

    @Override // android.view.View
    public void setFitsSystemWindows(boolean z) {
        super.setFitsSystemWindows(z);
        setupForInsets();
    }

    @Override // android.view.ViewGroup
    public void setOnHierarchyChangeListener(ViewGroup.OnHierarchyChangeListener onHierarchyChangeListener) {
        this.mOnHierarchyChangeListener = onHierarchyChangeListener;
    }

    public void setStatusBarBackground(@Nullable Drawable drawable) {
        if (this.mStatusBarBackground == drawable) {
            return;
        }
        if (this.mStatusBarBackground != null) {
            this.mStatusBarBackground.setCallback(null);
        }
        this.mStatusBarBackground = drawable != null ? drawable.mutate() : null;
        if (this.mStatusBarBackground != null) {
            if (this.mStatusBarBackground.isStateful()) {
                this.mStatusBarBackground.setState(getDrawableState());
            }
            DrawableCompat.setLayoutDirection(this.mStatusBarBackground, ViewCompat.getLayoutDirection(this));
            this.mStatusBarBackground.setVisible(getVisibility() == 0, false);
            this.mStatusBarBackground.setCallback(this);
        }
        ViewCompat.postInvalidateOnAnimation(this);
    }

    public void setStatusBarBackgroundColor(@ColorInt int i) {
        setStatusBarBackground(new ColorDrawable(i));
    }

    public void setStatusBarBackgroundResource(@DrawableRes int i) {
        setStatusBarBackground(i == 0 ? null : ContextCompat.getDrawable(getContext(), i));
    }

    @Override // android.view.View
    public void setVisibility(int i) {
        super.setVisibility(i);
        boolean z = i == 0;
        if (this.mStatusBarBackground == null || this.mStatusBarBackground.isVisible() == z) {
            return;
        }
        this.mStatusBarBackground.setVisible(z, false);
    }

    final WindowInsetsCompat setWindowInsets(WindowInsetsCompat windowInsetsCompat) {
        if (ObjectsCompat.equals(this.mLastInsets, windowInsetsCompat)) {
            return windowInsetsCompat;
        }
        this.mLastInsets = windowInsetsCompat;
        this.mDrawStatusBarBackground = windowInsetsCompat != null && windowInsetsCompat.getSystemWindowInsetTop() > 0;
        setWillNotDraw(!this.mDrawStatusBarBackground && getBackground() == null);
        WindowInsetsCompat windowInsetsCompatDispatchApplyWindowInsetsToBehaviors = dispatchApplyWindowInsetsToBehaviors(windowInsetsCompat);
        requestLayout();
        return windowInsetsCompatDispatchApplyWindowInsetsToBehaviors;
    }

    @Override // android.view.View
    protected boolean verifyDrawable(Drawable drawable) {
        return super.verifyDrawable(drawable) || drawable == this.mStatusBarBackground;
    }
}
