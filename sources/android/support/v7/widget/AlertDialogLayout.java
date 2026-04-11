package android.support.v7.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.appcompat.R;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/* JADX INFO: loaded from: classes.dex */
@RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
public class AlertDialogLayout extends LinearLayoutCompat {
    public AlertDialogLayout(@Nullable Context context) {
        super(context);
    }

    public AlertDialogLayout(@Nullable Context context, @Nullable AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    private void forceUniformWidth(int i, int i2) {
        int iMakeMeasureSpec = View.MeasureSpec.makeMeasureSpec(getMeasuredWidth(), 1073741824);
        for (int i3 = 0; i3 < i; i3++) {
            View childAt = getChildAt(i3);
            if (childAt.getVisibility() != 8) {
                LinearLayoutCompat.LayoutParams layoutParams = (LinearLayoutCompat.LayoutParams) childAt.getLayoutParams();
                if (layoutParams.width == -1) {
                    int i4 = layoutParams.height;
                    layoutParams.height = childAt.getMeasuredHeight();
                    measureChildWithMargins(childAt, iMakeMeasureSpec, 0, i2, 0);
                    layoutParams.height = i4;
                }
            }
        }
    }

    private static int resolveMinimumHeight(View view) {
        int minimumHeight = ViewCompat.getMinimumHeight(view);
        if (minimumHeight > 0) {
            return minimumHeight;
        }
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            if (viewGroup.getChildCount() == 1) {
                return resolveMinimumHeight(viewGroup.getChildAt(0));
            }
        }
        return 0;
    }

    private void setChildFrame(View view, int i, int i2, int i3, int i4) {
        view.layout(i, i2, i + i3, i2 + i4);
    }

    private boolean tryOnMeasure(int i, int i2) {
        int iCombineMeasuredStates;
        int i3;
        int measuredHeight;
        int iCombineMeasuredStates2;
        int i4;
        int iCombineMeasuredStates3;
        int i5;
        int i6;
        int i7;
        int iCombineMeasuredStates4;
        int measuredHeight2;
        int iCombineMeasuredStates5;
        View view;
        View view2;
        View view3 = null;
        View view4 = null;
        View view5 = null;
        int childCount = getChildCount();
        int i8 = 0;
        while (i8 < childCount) {
            View childAt = getChildAt(i8);
            if (childAt.getVisibility() != 8) {
                int id = childAt.getId();
                if (id == R.id.topPanel) {
                    View view6 = view5;
                    view = view4;
                    view2 = childAt;
                    childAt = view6;
                } else if (id == R.id.buttonPanel) {
                    view2 = view3;
                    childAt = view5;
                    view = childAt;
                } else {
                    if ((id != R.id.contentPanel && id != R.id.customPanel) || view5 != null) {
                        return false;
                    }
                    view = view4;
                    view2 = view3;
                }
            } else {
                childAt = view5;
                view = view4;
                view2 = view3;
            }
            i8++;
            view3 = view2;
            view4 = view;
            view5 = childAt;
        }
        int mode = View.MeasureSpec.getMode(i2);
        int size = View.MeasureSpec.getSize(i2);
        int mode2 = View.MeasureSpec.getMode(i);
        int paddingTop = getPaddingTop() + getPaddingBottom();
        if (view3 == null) {
            iCombineMeasuredStates = 0;
        } else {
            view3.measure(i, 0);
            paddingTop += view3.getMeasuredHeight();
            iCombineMeasuredStates = View.combineMeasuredStates(0, view3.getMeasuredState());
        }
        if (view4 == null) {
            i3 = paddingTop;
            measuredHeight = 0;
            iCombineMeasuredStates2 = iCombineMeasuredStates;
            i4 = 0;
        } else {
            view4.measure(i, 0);
            int iResolveMinimumHeight = resolveMinimumHeight(view4);
            i3 = paddingTop + iResolveMinimumHeight;
            measuredHeight = view4.getMeasuredHeight() - iResolveMinimumHeight;
            iCombineMeasuredStates2 = View.combineMeasuredStates(iCombineMeasuredStates, view4.getMeasuredState());
            i4 = iResolveMinimumHeight;
        }
        if (view5 == null) {
            iCombineMeasuredStates3 = iCombineMeasuredStates2;
            i5 = i3;
            i6 = 0;
        } else {
            view5.measure(i, mode != 0 ? View.MeasureSpec.makeMeasureSpec(Math.max(0, size - i3), mode) : 0);
            int measuredHeight3 = view5.getMeasuredHeight();
            int i9 = i3 + measuredHeight3;
            iCombineMeasuredStates3 = View.combineMeasuredStates(iCombineMeasuredStates2, view5.getMeasuredState());
            i5 = i9;
            i6 = measuredHeight3;
        }
        int i10 = size - i5;
        if (view4 == null) {
            measuredHeight2 = i5;
            iCombineMeasuredStates4 = iCombineMeasuredStates3;
        } else {
            int i11 = i5 - i4;
            int iMin = Math.min(i10, measuredHeight);
            if (iMin <= 0) {
                i7 = i10;
            } else {
                i7 = i10 - iMin;
                i4 += iMin;
            }
            view4.measure(i, View.MeasureSpec.makeMeasureSpec(i4, 1073741824));
            int measuredHeight4 = i11 + view4.getMeasuredHeight();
            iCombineMeasuredStates4 = View.combineMeasuredStates(iCombineMeasuredStates3, view4.getMeasuredState());
            i10 = i7;
            measuredHeight2 = measuredHeight4;
        }
        if (view5 != null && i10 > 0) {
            view5.measure(i, View.MeasureSpec.makeMeasureSpec(i6 + i10, mode));
            measuredHeight2 = (measuredHeight2 - i6) + view5.getMeasuredHeight();
            iCombineMeasuredStates5 = View.combineMeasuredStates(iCombineMeasuredStates4, view5.getMeasuredState());
        } else {
            iCombineMeasuredStates5 = iCombineMeasuredStates4;
        }
        int iMax = 0;
        for (int i12 = 0; i12 < childCount; i12++) {
            View childAt2 = getChildAt(i12);
            if (childAt2.getVisibility() != 8) {
                iMax = Math.max(iMax, childAt2.getMeasuredWidth());
            }
        }
        setMeasuredDimension(View.resolveSizeAndState(iMax + getPaddingLeft() + getPaddingRight(), i, iCombineMeasuredStates5), View.resolveSizeAndState(measuredHeight2, i2, 0));
        if (mode2 == 1073741824) {
            return true;
        }
        forceUniformWidth(childCount, i2);
        return true;
    }

    @Override // android.support.v7.widget.LinearLayoutCompat, android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        int paddingTop;
        int i5;
        int paddingLeft = getPaddingLeft();
        int i6 = i3 - i;
        int paddingRight = i6 - getPaddingRight();
        int paddingRight2 = (i6 - paddingLeft) - getPaddingRight();
        int measuredHeight = getMeasuredHeight();
        int childCount = getChildCount();
        int gravity = getGravity();
        int i7 = gravity & 112;
        int i8 = gravity & GravityCompat.RELATIVE_HORIZONTAL_GRAVITY_MASK;
        switch (i7) {
            case 16:
                paddingTop = (((i4 - i2) - measuredHeight) / 2) + getPaddingTop();
                break;
            case 48:
            default:
                paddingTop = getPaddingTop();
                break;
            case 80:
                paddingTop = ((getPaddingTop() + i4) - i2) - measuredHeight;
                break;
        }
        Drawable dividerDrawable = getDividerDrawable();
        int intrinsicHeight = dividerDrawable != null ? dividerDrawable.getIntrinsicHeight() : 0;
        int i9 = paddingTop;
        for (int i10 = 0; i10 < childCount; i10++) {
            View childAt = getChildAt(i10);
            if (childAt != null && childAt.getVisibility() != 8) {
                int measuredWidth = childAt.getMeasuredWidth();
                int measuredHeight2 = childAt.getMeasuredHeight();
                LinearLayoutCompat.LayoutParams layoutParams = (LinearLayoutCompat.LayoutParams) childAt.getLayoutParams();
                int i11 = layoutParams.gravity;
                if (i11 < 0) {
                    i11 = i8;
                }
                switch (GravityCompat.getAbsoluteGravity(i11, ViewCompat.getLayoutDirection(this)) & 7) {
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
                int i12 = layoutParams.topMargin + (!hasDividerBeforeChildAt(i10) ? i9 : i9 + intrinsicHeight);
                setChildFrame(childAt, i5, i12, measuredWidth, measuredHeight2);
                i9 = i12 + layoutParams.bottomMargin + measuredHeight2;
            }
        }
    }

    @Override // android.support.v7.widget.LinearLayoutCompat, android.view.View
    protected void onMeasure(int i, int i2) {
        if (tryOnMeasure(i, i2)) {
            return;
        }
        super.onMeasure(i, i2);
    }
}
