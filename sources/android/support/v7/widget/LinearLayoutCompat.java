package android.support.v7.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.RestrictTo;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.InputDeviceCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.appcompat.R;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/* JADX INFO: loaded from: classes.dex */
public class LinearLayoutCompat extends ViewGroup {
    public static final int HORIZONTAL = 0;
    private static final int INDEX_BOTTOM = 2;
    private static final int INDEX_CENTER_VERTICAL = 0;
    private static final int INDEX_FILL = 3;
    private static final int INDEX_TOP = 1;
    public static final int SHOW_DIVIDER_BEGINNING = 1;
    public static final int SHOW_DIVIDER_END = 4;
    public static final int SHOW_DIVIDER_MIDDLE = 2;
    public static final int SHOW_DIVIDER_NONE = 0;
    public static final int VERTICAL = 1;
    private static final int VERTICAL_GRAVITY_COUNT = 4;
    private boolean mBaselineAligned;
    private int mBaselineAlignedChildIndex;
    private int mBaselineChildTop;
    private Drawable mDivider;
    private int mDividerHeight;
    private int mDividerPadding;
    private int mDividerWidth;
    private int mGravity;
    private int[] mMaxAscent;
    private int[] mMaxDescent;
    private int mOrientation;
    private int mShowDividers;
    private int mTotalLength;
    private boolean mUseLargestChild;
    private float mWeightSum;

    @Retention(RetentionPolicy.SOURCE)
    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
    public @interface DividerMode {
    }

    public static class LayoutParams extends ViewGroup.MarginLayoutParams {
        public int gravity;
        public float weight;

        public LayoutParams(int i, int i2) {
            super(i, i2);
            this.gravity = -1;
            this.weight = 0.0f;
        }

        public LayoutParams(int i, int i2, float f) {
            super(i, i2);
            this.gravity = -1;
            this.weight = f;
        }

        public LayoutParams(Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
            this.gravity = -1;
            TypedArray typedArrayObtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.LinearLayoutCompat_Layout);
            this.weight = typedArrayObtainStyledAttributes.getFloat(R.styleable.LinearLayoutCompat_Layout_android_layout_weight, 0.0f);
            this.gravity = typedArrayObtainStyledAttributes.getInt(R.styleable.LinearLayoutCompat_Layout_android_layout_gravity, -1);
            typedArrayObtainStyledAttributes.recycle();
        }

        public LayoutParams(LayoutParams layoutParams) {
            super((ViewGroup.MarginLayoutParams) layoutParams);
            this.gravity = -1;
            this.weight = layoutParams.weight;
            this.gravity = layoutParams.gravity;
        }

        public LayoutParams(ViewGroup.LayoutParams layoutParams) {
            super(layoutParams);
            this.gravity = -1;
        }

        public LayoutParams(ViewGroup.MarginLayoutParams marginLayoutParams) {
            super(marginLayoutParams);
            this.gravity = -1;
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
    public @interface OrientationMode {
    }

    public LinearLayoutCompat(Context context) {
        this(context, null);
    }

    public LinearLayoutCompat(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public LinearLayoutCompat(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mBaselineAligned = true;
        this.mBaselineAlignedChildIndex = -1;
        this.mBaselineChildTop = 0;
        this.mGravity = 8388659;
        TintTypedArray tintTypedArrayObtainStyledAttributes = TintTypedArray.obtainStyledAttributes(context, attributeSet, R.styleable.LinearLayoutCompat, i, 0);
        int i2 = tintTypedArrayObtainStyledAttributes.getInt(R.styleable.LinearLayoutCompat_android_orientation, -1);
        if (i2 >= 0) {
            setOrientation(i2);
        }
        int i3 = tintTypedArrayObtainStyledAttributes.getInt(R.styleable.LinearLayoutCompat_android_gravity, -1);
        if (i3 >= 0) {
            setGravity(i3);
        }
        boolean z = tintTypedArrayObtainStyledAttributes.getBoolean(R.styleable.LinearLayoutCompat_android_baselineAligned, true);
        if (!z) {
            setBaselineAligned(z);
        }
        this.mWeightSum = tintTypedArrayObtainStyledAttributes.getFloat(R.styleable.LinearLayoutCompat_android_weightSum, -1.0f);
        this.mBaselineAlignedChildIndex = tintTypedArrayObtainStyledAttributes.getInt(R.styleable.LinearLayoutCompat_android_baselineAlignedChildIndex, -1);
        this.mUseLargestChild = tintTypedArrayObtainStyledAttributes.getBoolean(R.styleable.LinearLayoutCompat_measureWithLargestChild, false);
        setDividerDrawable(tintTypedArrayObtainStyledAttributes.getDrawable(R.styleable.LinearLayoutCompat_divider));
        this.mShowDividers = tintTypedArrayObtainStyledAttributes.getInt(R.styleable.LinearLayoutCompat_showDividers, 0);
        this.mDividerPadding = tintTypedArrayObtainStyledAttributes.getDimensionPixelSize(R.styleable.LinearLayoutCompat_dividerPadding, 0);
        tintTypedArrayObtainStyledAttributes.recycle();
    }

    private void forceUniformHeight(int i, int i2) {
        int iMakeMeasureSpec = View.MeasureSpec.makeMeasureSpec(getMeasuredHeight(), 1073741824);
        for (int i3 = 0; i3 < i; i3++) {
            View virtualChildAt = getVirtualChildAt(i3);
            if (virtualChildAt.getVisibility() != 8) {
                LayoutParams layoutParams = (LayoutParams) virtualChildAt.getLayoutParams();
                if (layoutParams.height == -1) {
                    int i4 = layoutParams.width;
                    layoutParams.width = virtualChildAt.getMeasuredWidth();
                    measureChildWithMargins(virtualChildAt, i2, 0, iMakeMeasureSpec, 0);
                    layoutParams.width = i4;
                }
            }
        }
    }

    private void forceUniformWidth(int i, int i2) {
        int iMakeMeasureSpec = View.MeasureSpec.makeMeasureSpec(getMeasuredWidth(), 1073741824);
        for (int i3 = 0; i3 < i; i3++) {
            View virtualChildAt = getVirtualChildAt(i3);
            if (virtualChildAt.getVisibility() != 8) {
                LayoutParams layoutParams = (LayoutParams) virtualChildAt.getLayoutParams();
                if (layoutParams.width == -1) {
                    int i4 = layoutParams.height;
                    layoutParams.height = virtualChildAt.getMeasuredHeight();
                    measureChildWithMargins(virtualChildAt, iMakeMeasureSpec, 0, i2, 0);
                    layoutParams.height = i4;
                }
            }
        }
    }

    private void setChildFrame(View view, int i, int i2, int i3, int i4) {
        view.layout(i, i2, i + i3, i2 + i4);
    }

    @Override // android.view.ViewGroup
    protected boolean checkLayoutParams(ViewGroup.LayoutParams layoutParams) {
        return layoutParams instanceof LayoutParams;
    }

    void drawDividersHorizontal(Canvas canvas) {
        int width;
        int virtualChildCount = getVirtualChildCount();
        boolean zIsLayoutRtl = ViewUtils.isLayoutRtl(this);
        for (int i = 0; i < virtualChildCount; i++) {
            View virtualChildAt = getVirtualChildAt(i);
            if (virtualChildAt != null && virtualChildAt.getVisibility() != 8 && hasDividerBeforeChildAt(i)) {
                LayoutParams layoutParams = (LayoutParams) virtualChildAt.getLayoutParams();
                drawVerticalDivider(canvas, !zIsLayoutRtl ? (virtualChildAt.getLeft() - layoutParams.leftMargin) - this.mDividerWidth : layoutParams.rightMargin + virtualChildAt.getRight());
            }
        }
        if (hasDividerBeforeChildAt(virtualChildCount)) {
            View virtualChildAt2 = getVirtualChildAt(virtualChildCount - 1);
            if (virtualChildAt2 != null) {
                LayoutParams layoutParams2 = (LayoutParams) virtualChildAt2.getLayoutParams();
                width = !zIsLayoutRtl ? layoutParams2.rightMargin + virtualChildAt2.getRight() : (virtualChildAt2.getLeft() - layoutParams2.leftMargin) - this.mDividerWidth;
            } else {
                width = !zIsLayoutRtl ? (getWidth() - getPaddingRight()) - this.mDividerWidth : getPaddingLeft();
            }
            drawVerticalDivider(canvas, width);
        }
    }

    void drawDividersVertical(Canvas canvas) {
        int virtualChildCount = getVirtualChildCount();
        for (int i = 0; i < virtualChildCount; i++) {
            View virtualChildAt = getVirtualChildAt(i);
            if (virtualChildAt != null && virtualChildAt.getVisibility() != 8 && hasDividerBeforeChildAt(i)) {
                drawHorizontalDivider(canvas, (virtualChildAt.getTop() - ((LayoutParams) virtualChildAt.getLayoutParams()).topMargin) - this.mDividerHeight);
            }
        }
        if (hasDividerBeforeChildAt(virtualChildCount)) {
            View virtualChildAt2 = getVirtualChildAt(virtualChildCount - 1);
            drawHorizontalDivider(canvas, virtualChildAt2 != null ? ((LayoutParams) virtualChildAt2.getLayoutParams()).bottomMargin + virtualChildAt2.getBottom() : (getHeight() - getPaddingBottom()) - this.mDividerHeight);
        }
    }

    void drawHorizontalDivider(Canvas canvas, int i) {
        this.mDivider.setBounds(getPaddingLeft() + this.mDividerPadding, i, (getWidth() - getPaddingRight()) - this.mDividerPadding, this.mDividerHeight + i);
        this.mDivider.draw(canvas);
    }

    void drawVerticalDivider(Canvas canvas, int i) {
        this.mDivider.setBounds(i, getPaddingTop() + this.mDividerPadding, this.mDividerWidth + i, (getHeight() - getPaddingBottom()) - this.mDividerPadding);
        this.mDivider.draw(canvas);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.view.ViewGroup
    public LayoutParams generateDefaultLayoutParams() {
        if (this.mOrientation == 0) {
            return new LayoutParams(-2, -2);
        }
        if (this.mOrientation != 1) {
            return null;
        }
        return new LayoutParams(-1, -2);
    }

    @Override // android.view.ViewGroup
    public LayoutParams generateLayoutParams(AttributeSet attributeSet) {
        return new LayoutParams(getContext(), attributeSet);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.view.ViewGroup
    public LayoutParams generateLayoutParams(ViewGroup.LayoutParams layoutParams) {
        return new LayoutParams(layoutParams);
    }

    @Override // android.view.View
    public int getBaseline() {
        int i;
        int bottom;
        if (this.mBaselineAlignedChildIndex < 0) {
            return super.getBaseline();
        }
        if (getChildCount() <= this.mBaselineAlignedChildIndex) {
            throw new RuntimeException("mBaselineAlignedChildIndex of LinearLayout set to an index that is out of bounds.");
        }
        View childAt = getChildAt(this.mBaselineAlignedChildIndex);
        int baseline = childAt.getBaseline();
        if (baseline == -1) {
            if (this.mBaselineAlignedChildIndex != 0) {
                throw new RuntimeException("mBaselineAlignedChildIndex of LinearLayout points to a View that doesn't know how to get its baseline.");
            }
            return -1;
        }
        int i2 = this.mBaselineChildTop;
        if (this.mOrientation == 1 && (i = this.mGravity & 112) != 48) {
            switch (i) {
                case 16:
                    bottom = i2 + (((((getBottom() - getTop()) - getPaddingTop()) - getPaddingBottom()) - this.mTotalLength) / 2);
                    break;
                case 80:
                    bottom = ((getBottom() - getTop()) - getPaddingBottom()) - this.mTotalLength;
                    break;
                default:
                    bottom = i2;
                    break;
            }
        } else {
            bottom = i2;
        }
        return ((LayoutParams) childAt.getLayoutParams()).topMargin + bottom + baseline;
    }

    public int getBaselineAlignedChildIndex() {
        return this.mBaselineAlignedChildIndex;
    }

    int getChildrenSkipCount(View view, int i) {
        return 0;
    }

    public Drawable getDividerDrawable() {
        return this.mDivider;
    }

    public int getDividerPadding() {
        return this.mDividerPadding;
    }

    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
    public int getDividerWidth() {
        return this.mDividerWidth;
    }

    public int getGravity() {
        return this.mGravity;
    }

    int getLocationOffset(View view) {
        return 0;
    }

    int getNextLocationOffset(View view) {
        return 0;
    }

    public int getOrientation() {
        return this.mOrientation;
    }

    public int getShowDividers() {
        return this.mShowDividers;
    }

    View getVirtualChildAt(int i) {
        return getChildAt(i);
    }

    int getVirtualChildCount() {
        return getChildCount();
    }

    public float getWeightSum() {
        return this.mWeightSum;
    }

    protected boolean hasDividerBeforeChildAt(int i) {
        if (i == 0) {
            return (this.mShowDividers & 1) != 0;
        }
        if (i == getChildCount()) {
            return (this.mShowDividers & 4) != 0;
        }
        if ((this.mShowDividers & 2) == 0) {
            return false;
        }
        for (int i2 = i - 1; i2 >= 0; i2--) {
            if (getChildAt(i2).getVisibility() != 8) {
                return true;
            }
        }
        return false;
    }

    public boolean isBaselineAligned() {
        return this.mBaselineAligned;
    }

    public boolean isMeasureWithLargestChildEnabled() {
        return this.mUseLargestChild;
    }

    void layoutHorizontal(int i, int i2, int i3, int i4) {
        int paddingLeft;
        int i5;
        int i6;
        int childrenSkipCount;
        int measuredHeight;
        boolean zIsLayoutRtl = ViewUtils.isLayoutRtl(this);
        int paddingTop = getPaddingTop();
        int i7 = i4 - i2;
        int paddingBottom = i7 - getPaddingBottom();
        int paddingBottom2 = (i7 - paddingTop) - getPaddingBottom();
        int virtualChildCount = getVirtualChildCount();
        int i8 = this.mGravity & GravityCompat.RELATIVE_HORIZONTAL_GRAVITY_MASK;
        int i9 = this.mGravity & 112;
        boolean z = this.mBaselineAligned;
        int[] iArr = this.mMaxAscent;
        int[] iArr2 = this.mMaxDescent;
        switch (GravityCompat.getAbsoluteGravity(i8, ViewCompat.getLayoutDirection(this))) {
            case 1:
                paddingLeft = getPaddingLeft() + (((i3 - i) - this.mTotalLength) / 2);
                break;
            case 2:
            case 3:
            case 4:
            default:
                paddingLeft = getPaddingLeft();
                break;
            case 5:
                paddingLeft = ((getPaddingLeft() + i3) - i) - this.mTotalLength;
                break;
        }
        if (zIsLayoutRtl) {
            i5 = -1;
            i6 = virtualChildCount - 1;
        } else {
            i5 = 1;
            i6 = 0;
        }
        int i10 = 0;
        int iMeasureNullChild = paddingLeft;
        while (i10 < virtualChildCount) {
            int i11 = i6 + (i5 * i10);
            View virtualChildAt = getVirtualChildAt(i11);
            if (virtualChildAt == null) {
                iMeasureNullChild += measureNullChild(i11);
                childrenSkipCount = i10;
            } else if (virtualChildAt.getVisibility() == 8) {
                childrenSkipCount = i10;
            } else {
                int measuredWidth = virtualChildAt.getMeasuredWidth();
                int measuredHeight2 = virtualChildAt.getMeasuredHeight();
                LayoutParams layoutParams = (LayoutParams) virtualChildAt.getLayoutParams();
                int baseline = (z && layoutParams.height != -1) ? virtualChildAt.getBaseline() : -1;
                int i12 = layoutParams.gravity;
                if (i12 < 0) {
                    i12 = i9;
                }
                switch (i12 & 112) {
                    case 16:
                        measuredHeight = ((((paddingBottom2 - measuredHeight2) / 2) + paddingTop) + layoutParams.topMargin) - layoutParams.bottomMargin;
                        break;
                    case 48:
                        measuredHeight = paddingTop + layoutParams.topMargin;
                        if (baseline != -1) {
                            measuredHeight += iArr[1] - baseline;
                        }
                        break;
                    case 80:
                        measuredHeight = (paddingBottom - measuredHeight2) - layoutParams.bottomMargin;
                        if (baseline != -1) {
                            measuredHeight -= iArr2[2] - (virtualChildAt.getMeasuredHeight() - baseline);
                        }
                        break;
                    default:
                        measuredHeight = paddingTop;
                        break;
                }
                int i13 = (!hasDividerBeforeChildAt(i11) ? iMeasureNullChild : this.mDividerWidth + iMeasureNullChild) + layoutParams.leftMargin;
                setChildFrame(virtualChildAt, i13 + getLocationOffset(virtualChildAt), measuredHeight, measuredWidth, measuredHeight2);
                iMeasureNullChild = i13 + layoutParams.rightMargin + measuredWidth + getNextLocationOffset(virtualChildAt);
                childrenSkipCount = getChildrenSkipCount(virtualChildAt, i11) + i10;
            }
            i10 = childrenSkipCount + 1;
        }
    }

    void layoutVertical(int i, int i2, int i3, int i4) {
        int paddingTop;
        int childrenSkipCount;
        int i5;
        int paddingLeft = getPaddingLeft();
        int i6 = i3 - i;
        int paddingRight = i6 - getPaddingRight();
        int paddingRight2 = (i6 - paddingLeft) - getPaddingRight();
        int virtualChildCount = getVirtualChildCount();
        int i7 = this.mGravity & 112;
        int i8 = this.mGravity & GravityCompat.RELATIVE_HORIZONTAL_GRAVITY_MASK;
        switch (i7) {
            case 16:
                paddingTop = getPaddingTop() + (((i4 - i2) - this.mTotalLength) / 2);
                break;
            case 48:
            default:
                paddingTop = getPaddingTop();
                break;
            case 80:
                paddingTop = ((getPaddingTop() + i4) - i2) - this.mTotalLength;
                break;
        }
        int i9 = 0;
        int iMeasureNullChild = paddingTop;
        while (i9 < virtualChildCount) {
            View virtualChildAt = getVirtualChildAt(i9);
            if (virtualChildAt == null) {
                iMeasureNullChild += measureNullChild(i9);
                childrenSkipCount = i9;
            } else if (virtualChildAt.getVisibility() == 8) {
                childrenSkipCount = i9;
            } else {
                int measuredWidth = virtualChildAt.getMeasuredWidth();
                int measuredHeight = virtualChildAt.getMeasuredHeight();
                LayoutParams layoutParams = (LayoutParams) virtualChildAt.getLayoutParams();
                int i10 = layoutParams.gravity;
                if (i10 < 0) {
                    i10 = i8;
                }
                switch (GravityCompat.getAbsoluteGravity(i10, ViewCompat.getLayoutDirection(this)) & 7) {
                    case 1:
                        i5 = ((((paddingRight2 - measuredWidth) / 2) + paddingLeft) + layoutParams.leftMargin) - layoutParams.rightMargin;
                        break;
                    case 2:
                    case 3:
                    case 4:
                    default:
                        i5 = paddingLeft + layoutParams.leftMargin;
                        break;
                    case 5:
                        i5 = (paddingRight - measuredWidth) - layoutParams.rightMargin;
                        break;
                }
                int i11 = (!hasDividerBeforeChildAt(i9) ? iMeasureNullChild : this.mDividerHeight + iMeasureNullChild) + layoutParams.topMargin;
                setChildFrame(virtualChildAt, i5, i11 + getLocationOffset(virtualChildAt), measuredWidth, measuredHeight);
                iMeasureNullChild = i11 + layoutParams.bottomMargin + measuredHeight + getNextLocationOffset(virtualChildAt);
                childrenSkipCount = getChildrenSkipCount(virtualChildAt, i9) + i9;
            }
            i9 = childrenSkipCount + 1;
        }
    }

    void measureChildBeforeLayout(View view, int i, int i2, int i3, int i4, int i5) {
        measureChildWithMargins(view, i2, i3, i4, i5);
    }

    void measureHorizontal(int i, int i2) {
        int childrenSkipCount;
        int iMax;
        int i3;
        float f;
        int i4;
        int i5;
        int iMax2;
        boolean z;
        int i6;
        int iCombineMeasuredStates;
        float f2;
        float f3;
        boolean z2;
        int i7;
        int i8;
        int iMax3;
        boolean z3;
        boolean z4;
        int iMax4;
        int iMax5;
        int i9;
        this.mTotalLength = 0;
        int i10 = 0;
        int i11 = 0;
        int i12 = 0;
        int i13 = 0;
        boolean z5 = true;
        float f4 = 0.0f;
        int virtualChildCount = getVirtualChildCount();
        int mode = View.MeasureSpec.getMode(i);
        int mode2 = View.MeasureSpec.getMode(i2);
        boolean z6 = false;
        boolean z7 = false;
        if (this.mMaxAscent == null || this.mMaxDescent == null) {
            this.mMaxAscent = new int[4];
            this.mMaxDescent = new int[4];
        }
        int[] iArr = this.mMaxAscent;
        int[] iArr2 = this.mMaxDescent;
        iArr[3] = -1;
        iArr[2] = -1;
        iArr[1] = -1;
        iArr[0] = -1;
        iArr2[3] = -1;
        iArr2[2] = -1;
        iArr2[1] = -1;
        iArr2[0] = -1;
        boolean z8 = this.mBaselineAligned;
        boolean z9 = this.mUseLargestChild;
        boolean z10 = mode == 1073741824;
        int i14 = Integer.MIN_VALUE;
        int childrenSkipCount2 = 0;
        while (childrenSkipCount2 < virtualChildCount) {
            View virtualChildAt = getVirtualChildAt(childrenSkipCount2);
            if (virtualChildAt == null) {
                this.mTotalLength += measureNullChild(childrenSkipCount2);
                f3 = f4;
                z2 = z5;
                i7 = i11;
                i8 = i10;
            } else if (virtualChildAt.getVisibility() != 8) {
                if (hasDividerBeforeChildAt(childrenSkipCount2)) {
                    this.mTotalLength += this.mDividerWidth;
                }
                LayoutParams layoutParams = (LayoutParams) virtualChildAt.getLayoutParams();
                float f5 = f4 + layoutParams.weight;
                if (mode == 1073741824 && layoutParams.width == 0 && layoutParams.weight > 0.0f) {
                    if (z10) {
                        this.mTotalLength += layoutParams.leftMargin + layoutParams.rightMargin;
                    } else {
                        int i15 = this.mTotalLength;
                        this.mTotalLength = Math.max(i15, layoutParams.leftMargin + i15 + layoutParams.rightMargin);
                    }
                    if (z8) {
                        int iMakeMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, 0);
                        virtualChildAt.measure(iMakeMeasureSpec, iMakeMeasureSpec);
                        iMax3 = i14;
                        z3 = z7;
                    } else {
                        z3 = true;
                        iMax3 = i14;
                    }
                } else {
                    if (layoutParams.width == 0 && layoutParams.weight > 0.0f) {
                        layoutParams.width = -2;
                        i9 = 0;
                    } else {
                        i9 = Integer.MIN_VALUE;
                    }
                    measureChildBeforeLayout(virtualChildAt, childrenSkipCount2, i, f5 == 0.0f ? this.mTotalLength : 0, i2, 0);
                    if (i9 != Integer.MIN_VALUE) {
                        layoutParams.width = i9;
                    }
                    int measuredWidth = virtualChildAt.getMeasuredWidth();
                    if (z10) {
                        this.mTotalLength += layoutParams.leftMargin + measuredWidth + layoutParams.rightMargin + getNextLocationOffset(virtualChildAt);
                    } else {
                        int i16 = this.mTotalLength;
                        this.mTotalLength = Math.max(i16, i16 + measuredWidth + layoutParams.leftMargin + layoutParams.rightMargin + getNextLocationOffset(virtualChildAt));
                    }
                    if (z9) {
                        iMax3 = Math.max(measuredWidth, i14);
                        z3 = z7;
                    } else {
                        iMax3 = i14;
                        z3 = z7;
                    }
                }
                boolean z11 = false;
                if (mode2 != 1073741824 && layoutParams.height == -1) {
                    z4 = true;
                    z11 = true;
                } else {
                    z4 = z6;
                }
                int i17 = layoutParams.topMargin + layoutParams.bottomMargin;
                int measuredHeight = virtualChildAt.getMeasuredHeight() + i17;
                int iCombineMeasuredStates2 = View.combineMeasuredStates(i11, virtualChildAt.getMeasuredState());
                if (z8) {
                    int baseline = virtualChildAt.getBaseline();
                    if (baseline != -1) {
                        int i18 = ((((layoutParams.gravity >= 0 ? layoutParams.gravity : this.mGravity) & 112) >> 4) & (-2)) >> 1;
                        iArr[i18] = Math.max(iArr[i18], baseline);
                        iArr2[i18] = Math.max(iArr2[i18], measuredHeight - baseline);
                    }
                }
                int iMax6 = Math.max(i10, measuredHeight);
                z2 = z5 && layoutParams.height == -1;
                if (layoutParams.weight > 0.0f) {
                    iMax5 = Math.max(i13, !z11 ? measuredHeight : i17);
                    iMax4 = i12;
                } else {
                    if (z11) {
                        measuredHeight = i17;
                    }
                    iMax4 = Math.max(i12, measuredHeight);
                    iMax5 = i13;
                }
                childrenSkipCount2 += getChildrenSkipCount(virtualChildAt, childrenSkipCount2);
                i14 = iMax3;
                z7 = z3;
                i13 = iMax5;
                i12 = iMax4;
                i7 = iCombineMeasuredStates2;
                f3 = f5;
                z6 = z4;
                i8 = iMax6;
            } else {
                childrenSkipCount2 += getChildrenSkipCount(virtualChildAt, childrenSkipCount2);
                f3 = f4;
                z2 = z5;
                i7 = i11;
                i8 = i10;
            }
            childrenSkipCount2++;
            z5 = z2;
            i11 = i7;
            i10 = i8;
            f4 = f3;
        }
        if (this.mTotalLength > 0 && hasDividerBeforeChildAt(virtualChildCount)) {
            this.mTotalLength += this.mDividerWidth;
        }
        int iMax7 = (iArr[1] == -1 && iArr[0] == -1 && iArr[2] == -1 && iArr[3] == -1) ? i10 : Math.max(i10, Math.max(iArr[3], Math.max(iArr[0], Math.max(iArr[1], iArr[2]))) + Math.max(iArr2[3], Math.max(iArr2[0], Math.max(iArr2[1], iArr2[2]))));
        if (z9 && (mode == Integer.MIN_VALUE || mode == 0)) {
            this.mTotalLength = 0;
            int i19 = 0;
            while (i19 < virtualChildCount) {
                View virtualChildAt2 = getVirtualChildAt(i19);
                if (virtualChildAt2 == null) {
                    this.mTotalLength += measureNullChild(i19);
                    childrenSkipCount = i19;
                } else if (virtualChildAt2.getVisibility() != 8) {
                    LayoutParams layoutParams2 = (LayoutParams) virtualChildAt2.getLayoutParams();
                    if (z10) {
                        this.mTotalLength = layoutParams2.rightMargin + layoutParams2.leftMargin + i14 + getNextLocationOffset(virtualChildAt2) + this.mTotalLength;
                        childrenSkipCount = i19;
                    } else {
                        int i20 = this.mTotalLength;
                        this.mTotalLength = Math.max(i20, layoutParams2.rightMargin + i20 + i14 + layoutParams2.leftMargin + getNextLocationOffset(virtualChildAt2));
                        childrenSkipCount = i19;
                    }
                } else {
                    childrenSkipCount = getChildrenSkipCount(virtualChildAt2, i19) + i19;
                }
                i19 = childrenSkipCount + 1;
            }
        }
        this.mTotalLength += getPaddingLeft() + getPaddingRight();
        int iResolveSizeAndState = View.resolveSizeAndState(Math.max(this.mTotalLength, getSuggestedMinimumWidth()), i, 0);
        int i21 = (16777215 & iResolveSizeAndState) - this.mTotalLength;
        if (!z7 && (i21 == 0 || f4 <= 0.0f)) {
            iMax = Math.max(i12, i13);
            if (z9 && mode != 1073741824) {
                int i22 = 0;
                while (true) {
                    int i23 = i22;
                    if (i23 >= virtualChildCount) {
                        break;
                    }
                    View virtualChildAt3 = getVirtualChildAt(i23);
                    if (virtualChildAt3 != null && virtualChildAt3.getVisibility() != 8 && ((LayoutParams) virtualChildAt3.getLayoutParams()).weight > 0.0f) {
                        virtualChildAt3.measure(View.MeasureSpec.makeMeasureSpec(i14, 1073741824), View.MeasureSpec.makeMeasureSpec(virtualChildAt3.getMeasuredHeight(), 1073741824));
                    }
                    i22 = i23 + 1;
                }
                i3 = iMax7;
            } else {
                i3 = iMax7;
            }
        } else {
            if (this.mWeightSum > 0.0f) {
                f4 = this.mWeightSum;
            }
            iArr[3] = -1;
            iArr[2] = -1;
            iArr[1] = -1;
            iArr[0] = -1;
            iArr2[3] = -1;
            iArr2[2] = -1;
            iArr2[1] = -1;
            iArr2[0] = -1;
            int iMax8 = -1;
            this.mTotalLength = 0;
            float f6 = f4;
            int i24 = 0;
            boolean z12 = z5;
            int i25 = i12;
            int i26 = i11;
            int i27 = i21;
            while (i24 < virtualChildCount) {
                View virtualChildAt4 = getVirtualChildAt(i24);
                if (virtualChildAt4 == null) {
                    f = f6;
                    i4 = i27;
                    i5 = iMax8;
                    iMax2 = i25;
                    z = z12;
                } else if (virtualChildAt4.getVisibility() != 8) {
                    LayoutParams layoutParams3 = (LayoutParams) virtualChildAt4.getLayoutParams();
                    float f7 = layoutParams3.weight;
                    if (f7 > 0.0f) {
                        int i28 = (int) ((i27 * f7) / f6);
                        float f8 = f6 - f7;
                        i6 = i27 - i28;
                        int childMeasureSpec = getChildMeasureSpec(i2, getPaddingTop() + getPaddingBottom() + layoutParams3.topMargin + layoutParams3.bottomMargin, layoutParams3.height);
                        if (layoutParams3.width == 0 && mode == 1073741824) {
                            if (i28 <= 0) {
                                i28 = 0;
                            }
                            virtualChildAt4.measure(View.MeasureSpec.makeMeasureSpec(i28, 1073741824), childMeasureSpec);
                        } else {
                            int measuredWidth2 = i28 + virtualChildAt4.getMeasuredWidth();
                            if (measuredWidth2 < 0) {
                                measuredWidth2 = 0;
                            }
                            virtualChildAt4.measure(View.MeasureSpec.makeMeasureSpec(measuredWidth2, 1073741824), childMeasureSpec);
                        }
                        iCombineMeasuredStates = View.combineMeasuredStates(i26, virtualChildAt4.getMeasuredState() & ViewCompat.MEASURED_STATE_MASK);
                        f2 = f8;
                    } else {
                        i6 = i27;
                        iCombineMeasuredStates = i26;
                        f2 = f6;
                    }
                    if (z10) {
                        this.mTotalLength += virtualChildAt4.getMeasuredWidth() + layoutParams3.leftMargin + layoutParams3.rightMargin + getNextLocationOffset(virtualChildAt4);
                    } else {
                        int i29 = this.mTotalLength;
                        this.mTotalLength = Math.max(i29, virtualChildAt4.getMeasuredWidth() + i29 + layoutParams3.leftMargin + layoutParams3.rightMargin + getNextLocationOffset(virtualChildAt4));
                    }
                    boolean z13 = mode2 != 1073741824 && layoutParams3.height == -1;
                    int i30 = layoutParams3.bottomMargin + layoutParams3.topMargin;
                    int measuredHeight2 = virtualChildAt4.getMeasuredHeight() + i30;
                    int iMax9 = Math.max(iMax8, measuredHeight2);
                    iMax2 = Math.max(i25, !z13 ? measuredHeight2 : i30);
                    boolean z14 = z12 && layoutParams3.height == -1;
                    if (z8) {
                        int baseline2 = virtualChildAt4.getBaseline();
                        if (baseline2 == -1) {
                            f = f2;
                            z = z14;
                            i4 = i6;
                            i26 = iCombineMeasuredStates;
                            i5 = iMax9;
                        } else {
                            int i31 = ((((layoutParams3.gravity >= 0 ? layoutParams3.gravity : this.mGravity) & 112) >> 4) & (-2)) >> 1;
                            iArr[i31] = Math.max(iArr[i31], baseline2);
                            iArr2[i31] = Math.max(iArr2[i31], measuredHeight2 - baseline2);
                            f = f2;
                            z = z14;
                            i4 = i6;
                            i26 = iCombineMeasuredStates;
                            i5 = iMax9;
                        }
                    } else {
                        f = f2;
                        z = z14;
                        i4 = i6;
                        i26 = iCombineMeasuredStates;
                        i5 = iMax9;
                    }
                } else {
                    f = f6;
                    i4 = i27;
                    i5 = iMax8;
                    iMax2 = i25;
                    z = z12;
                }
                i24++;
                i25 = iMax2;
                iMax8 = i5;
                z12 = z;
                i27 = i4;
                f6 = f;
            }
            this.mTotalLength += getPaddingLeft() + getPaddingRight();
            if (iArr[1] != -1 || iArr[0] != -1 || iArr[2] != -1 || iArr[3] != -1) {
                iMax8 = Math.max(iMax8, Math.max(iArr[3], Math.max(iArr[0], Math.max(iArr[1], iArr[2]))) + Math.max(iArr2[3], Math.max(iArr2[0], Math.max(iArr2[1], iArr2[2]))));
            }
            z5 = z12;
            iMax = i25;
            i11 = i26;
            i3 = iMax8;
        }
        if (!z5 && mode2 != 1073741824) {
            i3 = iMax;
        }
        setMeasuredDimension(((-16777216) & i11) | iResolveSizeAndState, View.resolveSizeAndState(Math.max(i3 + getPaddingTop() + getPaddingBottom(), getSuggestedMinimumHeight()), i2, i11 << 16));
        if (z6) {
            forceUniformHeight(virtualChildCount, i);
        }
    }

    int measureNullChild(int i) {
        return 0;
    }

    void measureVertical(int i, int i2) {
        int childrenSkipCount;
        int iMax;
        int i3;
        float f;
        int i4;
        int i5;
        int i6;
        int iCombineMeasuredStates;
        boolean z;
        int i7;
        boolean z2;
        boolean z3;
        int i8;
        int i9;
        boolean z4;
        int iMax2;
        int iMax3;
        int i10;
        this.mTotalLength = 0;
        int i11 = 0;
        int i12 = 0;
        int i13 = 0;
        int i14 = 0;
        boolean z5 = true;
        float f2 = 0.0f;
        int virtualChildCount = getVirtualChildCount();
        int mode = View.MeasureSpec.getMode(i);
        int mode2 = View.MeasureSpec.getMode(i2);
        boolean z6 = false;
        boolean z7 = false;
        int i15 = this.mBaselineAlignedChildIndex;
        boolean z8 = this.mUseLargestChild;
        int iMax4 = Integer.MIN_VALUE;
        int childrenSkipCount2 = 0;
        while (childrenSkipCount2 < virtualChildCount) {
            View virtualChildAt = getVirtualChildAt(childrenSkipCount2);
            if (virtualChildAt == null) {
                this.mTotalLength += measureNullChild(childrenSkipCount2);
                i7 = iMax4;
                z2 = z7;
                z3 = z5;
                i8 = i12;
                i9 = i11;
            } else if (virtualChildAt.getVisibility() != 8) {
                if (hasDividerBeforeChildAt(childrenSkipCount2)) {
                    this.mTotalLength += this.mDividerHeight;
                }
                LayoutParams layoutParams = (LayoutParams) virtualChildAt.getLayoutParams();
                float f3 = f2 + layoutParams.weight;
                if (mode2 == 1073741824 && layoutParams.height == 0 && layoutParams.weight > 0.0f) {
                    int i16 = this.mTotalLength;
                    this.mTotalLength = Math.max(i16, layoutParams.topMargin + i16 + layoutParams.bottomMargin);
                    z7 = true;
                } else {
                    if (layoutParams.height == 0 && layoutParams.weight > 0.0f) {
                        layoutParams.height = -2;
                        i10 = 0;
                    } else {
                        i10 = Integer.MIN_VALUE;
                    }
                    measureChildBeforeLayout(virtualChildAt, childrenSkipCount2, i, 0, i2, f3 == 0.0f ? this.mTotalLength : 0);
                    if (i10 != Integer.MIN_VALUE) {
                        layoutParams.height = i10;
                    }
                    int measuredHeight = virtualChildAt.getMeasuredHeight();
                    int i17 = this.mTotalLength;
                    this.mTotalLength = Math.max(i17, i17 + measuredHeight + layoutParams.topMargin + layoutParams.bottomMargin + getNextLocationOffset(virtualChildAt));
                    if (z8) {
                        iMax4 = Math.max(measuredHeight, iMax4);
                    }
                }
                if (i15 >= 0 && i15 == childrenSkipCount2 + 1) {
                    this.mBaselineChildTop = this.mTotalLength;
                }
                if (childrenSkipCount2 < i15 && layoutParams.weight > 0.0f) {
                    throw new RuntimeException("A child of LinearLayout with index less than mBaselineAlignedChildIndex has weight > 0, which won't work.  Either remove the weight, or don't set mBaselineAlignedChildIndex.");
                }
                boolean z9 = false;
                if (mode != 1073741824 && layoutParams.width == -1) {
                    z4 = true;
                    z9 = true;
                } else {
                    z4 = z6;
                }
                int i18 = layoutParams.leftMargin + layoutParams.rightMargin;
                int measuredWidth = virtualChildAt.getMeasuredWidth() + i18;
                int iMax5 = Math.max(i11, measuredWidth);
                int iCombineMeasuredStates2 = View.combineMeasuredStates(i12, virtualChildAt.getMeasuredState());
                z3 = z5 && layoutParams.width == -1;
                if (layoutParams.weight > 0.0f) {
                    iMax3 = Math.max(i14, !z9 ? measuredWidth : i18);
                    iMax2 = i13;
                } else {
                    if (z9) {
                        measuredWidth = i18;
                    }
                    iMax2 = Math.max(i13, measuredWidth);
                    iMax3 = i14;
                }
                childrenSkipCount2 += getChildrenSkipCount(virtualChildAt, childrenSkipCount2);
                z2 = z7;
                i14 = iMax3;
                i13 = iMax2;
                i9 = iMax5;
                i7 = iMax4;
                i8 = iCombineMeasuredStates2;
                z6 = z4;
                f2 = f3;
            } else {
                childrenSkipCount2 += getChildrenSkipCount(virtualChildAt, childrenSkipCount2);
                i7 = iMax4;
                z2 = z7;
                z3 = z5;
                i8 = i12;
                i9 = i11;
            }
            childrenSkipCount2++;
            iMax4 = i7;
            z7 = z2;
            z5 = z3;
            i12 = i8;
            i11 = i9;
        }
        if (this.mTotalLength > 0 && hasDividerBeforeChildAt(virtualChildCount)) {
            this.mTotalLength += this.mDividerHeight;
        }
        if (z8 && (mode2 == Integer.MIN_VALUE || mode2 == 0)) {
            this.mTotalLength = 0;
            int i19 = 0;
            while (i19 < virtualChildCount) {
                View virtualChildAt2 = getVirtualChildAt(i19);
                if (virtualChildAt2 == null) {
                    this.mTotalLength += measureNullChild(i19);
                    childrenSkipCount = i19;
                } else if (virtualChildAt2.getVisibility() != 8) {
                    LayoutParams layoutParams2 = (LayoutParams) virtualChildAt2.getLayoutParams();
                    int i20 = this.mTotalLength;
                    this.mTotalLength = Math.max(i20, layoutParams2.bottomMargin + i20 + iMax4 + layoutParams2.topMargin + getNextLocationOffset(virtualChildAt2));
                    childrenSkipCount = i19;
                } else {
                    childrenSkipCount = getChildrenSkipCount(virtualChildAt2, i19) + i19;
                }
                i19 = childrenSkipCount + 1;
            }
        }
        this.mTotalLength += getPaddingTop() + getPaddingBottom();
        int iResolveSizeAndState = View.resolveSizeAndState(Math.max(this.mTotalLength, getSuggestedMinimumHeight()), i2, 0);
        int i21 = (16777215 & iResolveSizeAndState) - this.mTotalLength;
        if (!z7 && (i21 == 0 || f2 <= 0.0f)) {
            iMax = Math.max(i13, i14);
            if (z8 && mode2 != 1073741824) {
                int i22 = 0;
                while (true) {
                    int i23 = i22;
                    if (i23 >= virtualChildCount) {
                        break;
                    }
                    View virtualChildAt3 = getVirtualChildAt(i23);
                    if (virtualChildAt3 != null && virtualChildAt3.getVisibility() != 8 && ((LayoutParams) virtualChildAt3.getLayoutParams()).weight > 0.0f) {
                        virtualChildAt3.measure(View.MeasureSpec.makeMeasureSpec(virtualChildAt3.getMeasuredWidth(), 1073741824), View.MeasureSpec.makeMeasureSpec(iMax4, 1073741824));
                    }
                    i22 = i23 + 1;
                }
                i3 = i11;
            } else {
                i3 = i11;
            }
        } else {
            if (this.mWeightSum > 0.0f) {
                f2 = this.mWeightSum;
            }
            this.mTotalLength = 0;
            int i24 = 0;
            boolean z10 = z5;
            int i25 = i13;
            int i26 = i12;
            int i27 = i11;
            float f4 = f2;
            int i28 = i21;
            while (i24 < virtualChildCount) {
                View virtualChildAt4 = getVirtualChildAt(i24);
                if (virtualChildAt4.getVisibility() != 8) {
                    LayoutParams layoutParams3 = (LayoutParams) virtualChildAt4.getLayoutParams();
                    float f5 = layoutParams3.weight;
                    if (f5 > 0.0f) {
                        int i29 = (int) ((i28 * f5) / f4);
                        float f6 = f4 - f5;
                        int i30 = i28 - i29;
                        int childMeasureSpec = getChildMeasureSpec(i, getPaddingLeft() + getPaddingRight() + layoutParams3.leftMargin + layoutParams3.rightMargin, layoutParams3.width);
                        if (layoutParams3.height == 0 && mode2 == 1073741824) {
                            if (i29 <= 0) {
                                i29 = 0;
                            }
                            virtualChildAt4.measure(childMeasureSpec, View.MeasureSpec.makeMeasureSpec(i29, 1073741824));
                        } else {
                            int measuredHeight2 = i29 + virtualChildAt4.getMeasuredHeight();
                            if (measuredHeight2 < 0) {
                                measuredHeight2 = 0;
                            }
                            virtualChildAt4.measure(childMeasureSpec, View.MeasureSpec.makeMeasureSpec(measuredHeight2, 1073741824));
                        }
                        i6 = i30;
                        iCombineMeasuredStates = View.combineMeasuredStates(i26, virtualChildAt4.getMeasuredState() & InputDeviceCompat.SOURCE_ANY);
                        f = f6;
                    } else {
                        f = f4;
                        i6 = i28;
                        iCombineMeasuredStates = i26;
                    }
                    int i31 = layoutParams3.leftMargin + layoutParams3.rightMargin;
                    int measuredWidth2 = virtualChildAt4.getMeasuredWidth() + i31;
                    int iMax6 = Math.max(i27, measuredWidth2);
                    int iMax7 = Math.max(i25, !(mode != 1073741824 && layoutParams3.width == -1) ? measuredWidth2 : i31);
                    z = z10 && layoutParams3.width == -1;
                    int i32 = this.mTotalLength;
                    this.mTotalLength = Math.max(i32, layoutParams3.bottomMargin + virtualChildAt4.getMeasuredHeight() + i32 + layoutParams3.topMargin + getNextLocationOffset(virtualChildAt4));
                    i4 = iMax7;
                    i5 = iMax6;
                } else {
                    f = f4;
                    i4 = i25;
                    i5 = i27;
                    i6 = i28;
                    iCombineMeasuredStates = i26;
                    z = z10;
                }
                i24++;
                z10 = z;
                i25 = i4;
                i27 = i5;
                i26 = iCombineMeasuredStates;
                i28 = i6;
                f4 = f;
            }
            this.mTotalLength += getPaddingTop() + getPaddingBottom();
            z5 = z10;
            iMax = i25;
            i12 = i26;
            i3 = i27;
        }
        if (!z5 && mode != 1073741824) {
            i3 = iMax;
        }
        setMeasuredDimension(View.resolveSizeAndState(Math.max(i3 + getPaddingLeft() + getPaddingRight(), getSuggestedMinimumWidth()), i, i12), iResolveSizeAndState);
        if (z6) {
            forceUniformWidth(virtualChildCount, i2);
        }
    }

    @Override // android.view.View
    protected void onDraw(Canvas canvas) {
        if (this.mDivider != null) {
            if (this.mOrientation != 1) {
                drawDividersHorizontal(canvas);
            } else {
                drawDividersVertical(canvas);
            }
        }
    }

    @Override // android.view.View
    public void onInitializeAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        if (Build.VERSION.SDK_INT < 14) {
            return;
        }
        super.onInitializeAccessibilityEvent(accessibilityEvent);
        accessibilityEvent.setClassName(LinearLayoutCompat.class.getName());
    }

    @Override // android.view.View
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo accessibilityNodeInfo) {
        if (Build.VERSION.SDK_INT < 14) {
            return;
        }
        super.onInitializeAccessibilityNodeInfo(accessibilityNodeInfo);
        accessibilityNodeInfo.setClassName(LinearLayoutCompat.class.getName());
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        if (this.mOrientation != 1) {
            layoutHorizontal(i, i2, i3, i4);
        } else {
            layoutVertical(i, i2, i3, i4);
        }
    }

    @Override // android.view.View
    protected void onMeasure(int i, int i2) {
        if (this.mOrientation != 1) {
            measureHorizontal(i, i2);
        } else {
            measureVertical(i, i2);
        }
    }

    public void setBaselineAligned(boolean z) {
        this.mBaselineAligned = z;
    }

    public void setBaselineAlignedChildIndex(int i) {
        if (i < 0 || i >= getChildCount()) {
            throw new IllegalArgumentException("base aligned child index out of range (0, " + getChildCount() + ")");
        }
        this.mBaselineAlignedChildIndex = i;
    }

    public void setDividerDrawable(Drawable drawable) {
        if (drawable != this.mDivider) {
            this.mDivider = drawable;
            if (drawable == null) {
                this.mDividerWidth = 0;
                this.mDividerHeight = 0;
            } else {
                this.mDividerWidth = drawable.getIntrinsicWidth();
                this.mDividerHeight = drawable.getIntrinsicHeight();
            }
            setWillNotDraw(drawable == null);
            requestLayout();
        }
    }

    public void setDividerPadding(int i) {
        this.mDividerPadding = i;
    }

    public void setGravity(int i) {
        if (this.mGravity == i) {
            return;
        }
        int i2 = (8388615 & i) != 0 ? i : 8388611 | i;
        if ((i2 & 112) == 0) {
            i2 |= 48;
        }
        this.mGravity = i2;
        requestLayout();
    }

    public void setHorizontalGravity(int i) {
        int i2 = i & GravityCompat.RELATIVE_HORIZONTAL_GRAVITY_MASK;
        if ((this.mGravity & GravityCompat.RELATIVE_HORIZONTAL_GRAVITY_MASK) == i2) {
            return;
        }
        this.mGravity = i2 | (this.mGravity & (-8388616));
        requestLayout();
    }

    public void setMeasureWithLargestChildEnabled(boolean z) {
        this.mUseLargestChild = z;
    }

    public void setOrientation(int i) {
        if (this.mOrientation == i) {
            return;
        }
        this.mOrientation = i;
        requestLayout();
    }

    public void setShowDividers(int i) {
        if (i != this.mShowDividers) {
            requestLayout();
        }
        this.mShowDividers = i;
    }

    public void setVerticalGravity(int i) {
        int i2 = i & 112;
        if ((this.mGravity & 112) == i2) {
            return;
        }
        this.mGravity = i2 | (this.mGravity & (-113));
        requestLayout();
    }

    public void setWeightSum(float f) {
        this.mWeightSum = Math.max(0.0f, f);
    }

    @Override // android.view.ViewGroup
    public boolean shouldDelayChildPressedState() {
        return false;
    }
}
