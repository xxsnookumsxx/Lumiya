package android.support.v7.widget;

import android.content.Context;
import android.graphics.Rect;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.View;
import android.view.ViewGroup;
import java.util.Arrays;

/* JADX INFO: loaded from: classes.dex */
public class GridLayoutManager extends LinearLayoutManager {
    private static final boolean DEBUG = false;
    public static final int DEFAULT_SPAN_COUNT = -1;
    private static final String TAG = "GridLayoutManager";
    int[] mCachedBorders;
    final Rect mDecorInsets;
    boolean mPendingSpanCountChange;
    final SparseIntArray mPreLayoutSpanIndexCache;
    final SparseIntArray mPreLayoutSpanSizeCache;
    View[] mSet;
    int mSpanCount;
    SpanSizeLookup mSpanSizeLookup;

    public static final class DefaultSpanSizeLookup extends SpanSizeLookup {
        @Override // android.support.v7.widget.GridLayoutManager.SpanSizeLookup
        public int getSpanIndex(int i, int i2) {
            return i % i2;
        }

        @Override // android.support.v7.widget.GridLayoutManager.SpanSizeLookup
        public int getSpanSize(int i) {
            return 1;
        }
    }

    public static class LayoutParams extends RecyclerView.LayoutParams {
        public static final int INVALID_SPAN_ID = -1;
        int mSpanIndex;
        int mSpanSize;

        public LayoutParams(int i, int i2) {
            super(i, i2);
            this.mSpanIndex = -1;
            this.mSpanSize = 0;
        }

        public LayoutParams(Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
            this.mSpanIndex = -1;
            this.mSpanSize = 0;
        }

        public LayoutParams(RecyclerView.LayoutParams layoutParams) {
            super(layoutParams);
            this.mSpanIndex = -1;
            this.mSpanSize = 0;
        }

        public LayoutParams(ViewGroup.LayoutParams layoutParams) {
            super(layoutParams);
            this.mSpanIndex = -1;
            this.mSpanSize = 0;
        }

        public LayoutParams(ViewGroup.MarginLayoutParams marginLayoutParams) {
            super(marginLayoutParams);
            this.mSpanIndex = -1;
            this.mSpanSize = 0;
        }

        public int getSpanIndex() {
            return this.mSpanIndex;
        }

        public int getSpanSize() {
            return this.mSpanSize;
        }
    }

    public static abstract class SpanSizeLookup {
        final SparseIntArray mSpanIndexCache = new SparseIntArray();
        private boolean mCacheSpanIndices = false;

        int findReferenceIndexFromCache(int i) {
            int i2 = 0;
            int size = this.mSpanIndexCache.size() - 1;
            while (i2 <= size) {
                int i3 = (i2 + size) >>> 1;
                if (this.mSpanIndexCache.keyAt(i3) >= i) {
                    size = i3 - 1;
                } else {
                    i2 = i3 + 1;
                }
            }
            int i4 = i2 - 1;
            if (i4 >= 0 && i4 < this.mSpanIndexCache.size()) {
                return this.mSpanIndexCache.keyAt(i4);
            }
            return -1;
        }

        int getCachedSpanIndex(int i, int i2) {
            if (!this.mCacheSpanIndices) {
                return getSpanIndex(i, i2);
            }
            int i3 = this.mSpanIndexCache.get(i, -1);
            if (i3 != -1) {
                return i3;
            }
            int spanIndex = getSpanIndex(i, i2);
            this.mSpanIndexCache.put(i, spanIndex);
            return spanIndex;
        }

        public int getSpanGroupIndex(int i, int i2) {
            int spanSize = getSpanSize(i);
            int i3 = 0;
            int i4 = 0;
            for (int i5 = 0; i5 < i; i5++) {
                int spanSize2 = getSpanSize(i5);
                i4 += spanSize2;
                if (i4 == i2) {
                    i3++;
                    i4 = 0;
                } else if (i4 > i2) {
                    i3++;
                    i4 = spanSize2;
                }
            }
            return i4 + spanSize <= i2 ? i3 : i3 + 1;
        }

        public int getSpanIndex(int i, int i2) {
            int iFindReferenceIndexFromCache;
            int spanSize;
            int i3;
            int spanSize2 = getSpanSize(i);
            if (spanSize2 == i2) {
                return 0;
            }
            if (this.mCacheSpanIndices && this.mSpanIndexCache.size() > 0 && (iFindReferenceIndexFromCache = findReferenceIndexFromCache(i)) >= 0) {
                spanSize = this.mSpanIndexCache.get(iFindReferenceIndexFromCache) + getSpanSize(iFindReferenceIndexFromCache);
                i3 = iFindReferenceIndexFromCache + 1;
            } else {
                i3 = 0;
                spanSize = 0;
            }
            int i4 = spanSize;
            for (int i5 = i3; i5 < i; i5++) {
                int spanSize3 = getSpanSize(i5);
                i4 += spanSize3;
                if (i4 == i2) {
                    i4 = 0;
                } else if (i4 > i2) {
                    i4 = spanSize3;
                }
            }
            if (i4 + spanSize2 > i2) {
                return 0;
            }
            return i4;
        }

        public abstract int getSpanSize(int i);

        public void invalidateSpanIndexCache() {
            this.mSpanIndexCache.clear();
        }

        public boolean isSpanIndexCacheEnabled() {
            return this.mCacheSpanIndices;
        }

        public void setSpanIndexCacheEnabled(boolean z) {
            this.mCacheSpanIndices = z;
        }
    }

    public GridLayoutManager(Context context, int i) {
        super(context);
        this.mPendingSpanCountChange = false;
        this.mSpanCount = -1;
        this.mPreLayoutSpanSizeCache = new SparseIntArray();
        this.mPreLayoutSpanIndexCache = new SparseIntArray();
        this.mSpanSizeLookup = new DefaultSpanSizeLookup();
        this.mDecorInsets = new Rect();
        setSpanCount(i);
    }

    public GridLayoutManager(Context context, int i, int i2, boolean z) {
        super(context, i2, z);
        this.mPendingSpanCountChange = false;
        this.mSpanCount = -1;
        this.mPreLayoutSpanSizeCache = new SparseIntArray();
        this.mPreLayoutSpanIndexCache = new SparseIntArray();
        this.mSpanSizeLookup = new DefaultSpanSizeLookup();
        this.mDecorInsets = new Rect();
        setSpanCount(i);
    }

    public GridLayoutManager(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mPendingSpanCountChange = false;
        this.mSpanCount = -1;
        this.mPreLayoutSpanSizeCache = new SparseIntArray();
        this.mPreLayoutSpanIndexCache = new SparseIntArray();
        this.mSpanSizeLookup = new DefaultSpanSizeLookup();
        this.mDecorInsets = new Rect();
        setSpanCount(getProperties(context, attributeSet, i, i2).spanCount);
    }

    private void assignSpans(RecyclerView.Recycler recycler, RecyclerView.State state, int i, int i2, boolean z) {
        int i3;
        int i4;
        if (z) {
            i3 = 1;
            i4 = 0;
        } else {
            int i5 = i - 1;
            i = -1;
            i4 = i5;
            i3 = -1;
        }
        int i6 = 0;
        for (int i7 = i4; i7 != i; i7 += i3) {
            View view = this.mSet[i7];
            LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
            layoutParams.mSpanSize = getSpanSize(recycler, state, getPosition(view));
            layoutParams.mSpanIndex = i6;
            i6 += layoutParams.mSpanSize;
        }
    }

    private void cachePreLayoutSpanMapping() {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            LayoutParams layoutParams = (LayoutParams) getChildAt(i).getLayoutParams();
            int viewLayoutPosition = layoutParams.getViewLayoutPosition();
            this.mPreLayoutSpanSizeCache.put(viewLayoutPosition, layoutParams.getSpanSize());
            this.mPreLayoutSpanIndexCache.put(viewLayoutPosition, layoutParams.getSpanIndex());
        }
    }

    private void calculateItemBorders(int i) {
        this.mCachedBorders = calculateItemBorders(this.mCachedBorders, this.mSpanCount, i);
    }

    static int[] calculateItemBorders(int[] iArr, int i, int i2) {
        int i3;
        int i4 = 0;
        if (iArr == null || iArr.length != i + 1 || iArr[iArr.length - 1] != i2) {
            iArr = new int[i + 1];
        }
        iArr[0] = 0;
        int i5 = i2 / i;
        int i6 = i2 % i;
        int i7 = 0;
        for (int i8 = 1; i8 <= i; i8++) {
            i4 += i6;
            if (i4 > 0 && i - i4 < i6) {
                i3 = i5 + 1;
                i4 -= i;
            } else {
                i3 = i5;
            }
            i7 += i3;
            iArr[i8] = i7;
        }
        return iArr;
    }

    private void clearPreLayoutSpanMappingCache() {
        this.mPreLayoutSpanSizeCache.clear();
        this.mPreLayoutSpanIndexCache.clear();
    }

    private void ensureAnchorIsInCorrectSpan(RecyclerView.Recycler recycler, RecyclerView.State state, LinearLayoutManager.AnchorInfo anchorInfo, int i) {
        boolean z = i == 1;
        int spanIndex = getSpanIndex(recycler, state, anchorInfo.mPosition);
        if (z) {
            while (spanIndex > 0 && anchorInfo.mPosition > 0) {
                anchorInfo.mPosition--;
                spanIndex = getSpanIndex(recycler, state, anchorInfo.mPosition);
            }
            return;
        }
        int itemCount = state.getItemCount() - 1;
        int i2 = anchorInfo.mPosition;
        int i3 = spanIndex;
        while (i2 < itemCount) {
            int spanIndex2 = getSpanIndex(recycler, state, i2 + 1);
            if (spanIndex2 <= i3) {
                break;
            }
            i2++;
            i3 = spanIndex2;
        }
        anchorInfo.mPosition = i2;
    }

    private void ensureViewSet() {
        if (this.mSet != null && this.mSet.length == this.mSpanCount) {
            return;
        }
        this.mSet = new View[this.mSpanCount];
    }

    private int getSpanGroupIndex(RecyclerView.Recycler recycler, RecyclerView.State state, int i) {
        if (!state.isPreLayout()) {
            return this.mSpanSizeLookup.getSpanGroupIndex(i, this.mSpanCount);
        }
        int iConvertPreLayoutPositionToPostLayout = recycler.convertPreLayoutPositionToPostLayout(i);
        if (iConvertPreLayoutPositionToPostLayout != -1) {
            return this.mSpanSizeLookup.getSpanGroupIndex(iConvertPreLayoutPositionToPostLayout, this.mSpanCount);
        }
        Log.w(TAG, "Cannot find span size for pre layout position. " + i);
        return 0;
    }

    private int getSpanIndex(RecyclerView.Recycler recycler, RecyclerView.State state, int i) {
        if (!state.isPreLayout()) {
            return this.mSpanSizeLookup.getCachedSpanIndex(i, this.mSpanCount);
        }
        int i2 = this.mPreLayoutSpanIndexCache.get(i, -1);
        if (i2 != -1) {
            return i2;
        }
        int iConvertPreLayoutPositionToPostLayout = recycler.convertPreLayoutPositionToPostLayout(i);
        if (iConvertPreLayoutPositionToPostLayout != -1) {
            return this.mSpanSizeLookup.getCachedSpanIndex(iConvertPreLayoutPositionToPostLayout, this.mSpanCount);
        }
        Log.w(TAG, "Cannot find span size for pre layout position. It is not cached, not in the adapter. Pos:" + i);
        return 0;
    }

    private int getSpanSize(RecyclerView.Recycler recycler, RecyclerView.State state, int i) {
        if (!state.isPreLayout()) {
            return this.mSpanSizeLookup.getSpanSize(i);
        }
        int i2 = this.mPreLayoutSpanSizeCache.get(i, -1);
        if (i2 != -1) {
            return i2;
        }
        int iConvertPreLayoutPositionToPostLayout = recycler.convertPreLayoutPositionToPostLayout(i);
        if (iConvertPreLayoutPositionToPostLayout != -1) {
            return this.mSpanSizeLookup.getSpanSize(iConvertPreLayoutPositionToPostLayout);
        }
        Log.w(TAG, "Cannot find span size for pre layout position. It is not cached, not in the adapter. Pos:" + i);
        return 1;
    }

    private void guessMeasurement(float f, int i) {
        calculateItemBorders(Math.max(Math.round(this.mSpanCount * f), i));
    }

    private void measureChild(View view, int i, boolean z) {
        int childMeasureSpec;
        int childMeasureSpec2;
        LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        Rect rect = layoutParams.mDecorInsets;
        int i2 = rect.top + rect.bottom + layoutParams.topMargin + layoutParams.bottomMargin;
        int i3 = layoutParams.rightMargin + rect.right + rect.left + layoutParams.leftMargin;
        int spaceForSpanRange = getSpaceForSpanRange(layoutParams.mSpanIndex, layoutParams.mSpanSize);
        if (this.mOrientation != 1) {
            int childMeasureSpec3 = getChildMeasureSpec(spaceForSpanRange, i, i2, layoutParams.height, false);
            childMeasureSpec = getChildMeasureSpec(this.mOrientationHelper.getTotalSpace(), getWidthMode(), i3, layoutParams.width, true);
            childMeasureSpec2 = childMeasureSpec3;
        } else {
            childMeasureSpec = getChildMeasureSpec(spaceForSpanRange, i, i3, layoutParams.width, false);
            childMeasureSpec2 = getChildMeasureSpec(this.mOrientationHelper.getTotalSpace(), getHeightMode(), i2, layoutParams.height, true);
        }
        measureChildWithDecorationsAndMargin(view, childMeasureSpec, childMeasureSpec2, z);
    }

    private void measureChildWithDecorationsAndMargin(View view, int i, int i2, boolean z) {
        RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) view.getLayoutParams();
        if (!z ? shouldMeasureChild(view, i, i2, layoutParams) : shouldReMeasureChild(view, i, i2, layoutParams)) {
            view.measure(i, i2);
        }
    }

    private void updateMeasurements() {
        calculateItemBorders(getOrientation() != 1 ? (getHeight() - getPaddingBottom()) - getPaddingTop() : (getWidth() - getPaddingRight()) - getPaddingLeft());
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public boolean checkLayoutParams(RecyclerView.LayoutParams layoutParams) {
        return layoutParams instanceof LayoutParams;
    }

    @Override // android.support.v7.widget.LinearLayoutManager
    void collectPrefetchPositionsForLayoutState(RecyclerView.State state, LinearLayoutManager.LayoutState layoutState, RecyclerView.LayoutManager.LayoutPrefetchRegistry layoutPrefetchRegistry) {
        int spanSize = this.mSpanCount;
        for (int i = 0; i < this.mSpanCount && layoutState.hasMore(state) && spanSize > 0; i++) {
            int i2 = layoutState.mCurrentPosition;
            layoutPrefetchRegistry.addPosition(i2, Math.max(0, layoutState.mScrollingOffset));
            spanSize -= this.mSpanSizeLookup.getSpanSize(i2);
            layoutState.mCurrentPosition += layoutState.mItemDirection;
        }
    }

    @Override // android.support.v7.widget.LinearLayoutManager
    View findReferenceChild(RecyclerView.Recycler recycler, RecyclerView.State state, int i, int i2, int i3) {
        View view;
        View view2 = null;
        ensureLayoutState();
        int startAfterPadding = this.mOrientationHelper.getStartAfterPadding();
        int endAfterPadding = this.mOrientationHelper.getEndAfterPadding();
        int i4 = i2 <= i ? -1 : 1;
        View view3 = null;
        while (i != i2) {
            View childAt = getChildAt(i);
            int position = getPosition(childAt);
            if (position < 0 || position >= i3) {
                view = view2;
                childAt = view3;
            } else if (getSpanIndex(recycler, state, position) != 0) {
                view = view2;
                childAt = view3;
            } else if (((RecyclerView.LayoutParams) childAt.getLayoutParams()).isItemRemoved()) {
                if (view3 != null) {
                    view = view2;
                    childAt = view3;
                } else {
                    view = view2;
                }
            } else {
                if (this.mOrientationHelper.getDecoratedStart(childAt) < endAfterPadding && this.mOrientationHelper.getDecoratedEnd(childAt) >= startAfterPadding) {
                    return childAt;
                }
                if (view2 != null) {
                    view = view2;
                    childAt = view3;
                } else {
                    view = childAt;
                    childAt = view3;
                }
            }
            i += i4;
            view2 = view;
            view3 = childAt;
        }
        return view2 == null ? view3 : view2;
    }

    @Override // android.support.v7.widget.LinearLayoutManager, android.support.v7.widget.RecyclerView.LayoutManager
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return this.mOrientation != 0 ? new LayoutParams(-1, -2) : new LayoutParams(-2, -1);
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public RecyclerView.LayoutParams generateLayoutParams(Context context, AttributeSet attributeSet) {
        return new LayoutParams(context, attributeSet);
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public RecyclerView.LayoutParams generateLayoutParams(ViewGroup.LayoutParams layoutParams) {
        return !(layoutParams instanceof ViewGroup.MarginLayoutParams) ? new LayoutParams(layoutParams) : new LayoutParams((ViewGroup.MarginLayoutParams) layoutParams);
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public int getColumnCountForAccessibility(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (this.mOrientation == 1) {
            return this.mSpanCount;
        }
        if (state.getItemCount() >= 1) {
            return getSpanGroupIndex(recycler, state, state.getItemCount() - 1) + 1;
        }
        return 0;
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public int getRowCountForAccessibility(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (this.mOrientation == 0) {
            return this.mSpanCount;
        }
        if (state.getItemCount() >= 1) {
            return getSpanGroupIndex(recycler, state, state.getItemCount() - 1) + 1;
        }
        return 0;
    }

    int getSpaceForSpanRange(int i, int i2) {
        return (this.mOrientation == 1 && isLayoutRTL()) ? this.mCachedBorders[this.mSpanCount - i] - this.mCachedBorders[(this.mSpanCount - i) - i2] : this.mCachedBorders[i + i2] - this.mCachedBorders[i];
    }

    public int getSpanCount() {
        return this.mSpanCount;
    }

    public SpanSizeLookup getSpanSizeLookup() {
        return this.mSpanSizeLookup;
    }

    @Override // android.support.v7.widget.LinearLayoutManager
    void layoutChunk(RecyclerView.Recycler recycler, RecyclerView.State state, LinearLayoutManager.LayoutState layoutState, LinearLayoutManager.LayoutChunkResult layoutChunkResult) {
        int i;
        int i2;
        int i3;
        int i4;
        int childMeasureSpec;
        int iMakeMeasureSpec;
        View next;
        int modeInOther = this.mOrientationHelper.getModeInOther();
        boolean z = modeInOther != 1073741824;
        int i5 = getChildCount() <= 0 ? 0 : this.mCachedBorders[this.mSpanCount];
        if (z) {
            updateMeasurements();
        }
        boolean z2 = layoutState.mItemDirection == 1;
        int i6 = 0;
        int i7 = 0;
        int spanIndex = this.mSpanCount;
        if (!z2) {
            spanIndex = getSpanIndex(recycler, state, layoutState.mCurrentPosition) + getSpanSize(recycler, state, layoutState.mCurrentPosition);
        }
        while (i6 < this.mSpanCount && layoutState.hasMore(state) && spanIndex > 0) {
            int i8 = layoutState.mCurrentPosition;
            int spanSize = getSpanSize(recycler, state, i8);
            if (spanSize > this.mSpanCount) {
                throw new IllegalArgumentException("Item at position " + i8 + " requires " + spanSize + " spans but GridLayoutManager has only " + this.mSpanCount + " spans.");
            }
            spanIndex -= spanSize;
            if (spanIndex < 0 || (next = layoutState.next(recycler)) == null) {
                break;
            }
            i7 += spanSize;
            this.mSet[i6] = next;
            i6++;
        }
        if (i6 == 0) {
            layoutChunkResult.mFinished = true;
            return;
        }
        assignSpans(recycler, state, i6, i7, z2);
        int i9 = 0;
        float f = 0.0f;
        int i10 = 0;
        while (i9 < i6) {
            View view = this.mSet[i9];
            if (layoutState.mScrapList != null) {
                if (z2) {
                    addDisappearingView(view);
                } else {
                    addDisappearingView(view, 0);
                }
            } else if (z2) {
                addView(view);
            } else {
                addView(view, 0);
            }
            calculateItemDecorationsForChild(view, this.mDecorInsets);
            measureChild(view, modeInOther, false);
            int decoratedMeasurement = this.mOrientationHelper.getDecoratedMeasurement(view);
            if (decoratedMeasurement <= i10) {
                decoratedMeasurement = i10;
            }
            float decoratedMeasurementInOther = (this.mOrientationHelper.getDecoratedMeasurementInOther(view) * 1.0f) / ((LayoutParams) view.getLayoutParams()).mSpanSize;
            if (decoratedMeasurementInOther <= f) {
                decoratedMeasurementInOther = f;
            }
            i9++;
            f = decoratedMeasurementInOther;
            i10 = decoratedMeasurement;
        }
        if (z) {
            guessMeasurement(f, i5);
            int i11 = 0;
            for (int i12 = 0; i12 < i6; i12++) {
                View view2 = this.mSet[i12];
                measureChild(view2, 1073741824, true);
                int decoratedMeasurement2 = this.mOrientationHelper.getDecoratedMeasurement(view2);
                if (decoratedMeasurement2 > i11) {
                    i11 = decoratedMeasurement2;
                }
            }
            i = i11;
        } else {
            i = i10;
        }
        for (int i13 = 0; i13 < i6; i13++) {
            View view3 = this.mSet[i13];
            if (this.mOrientationHelper.getDecoratedMeasurement(view3) != i) {
                LayoutParams layoutParams = (LayoutParams) view3.getLayoutParams();
                Rect rect = layoutParams.mDecorInsets;
                int i14 = rect.top + rect.bottom + layoutParams.topMargin + layoutParams.bottomMargin;
                int i15 = rect.right + rect.left + layoutParams.leftMargin + layoutParams.rightMargin;
                int spaceForSpanRange = getSpaceForSpanRange(layoutParams.mSpanIndex, layoutParams.mSpanSize);
                if (this.mOrientation != 1) {
                    childMeasureSpec = View.MeasureSpec.makeMeasureSpec(i - i15, 1073741824);
                    iMakeMeasureSpec = getChildMeasureSpec(spaceForSpanRange, 1073741824, i14, layoutParams.height, false);
                } else {
                    childMeasureSpec = getChildMeasureSpec(spaceForSpanRange, 1073741824, i15, layoutParams.width, false);
                    iMakeMeasureSpec = View.MeasureSpec.makeMeasureSpec(i - i14, 1073741824);
                }
                measureChildWithDecorationsAndMargin(view3, childMeasureSpec, iMakeMeasureSpec, true);
            }
        }
        layoutChunkResult.mConsumed = i;
        int i16 = 0;
        if (this.mOrientation != 1) {
            if (layoutState.mLayoutDirection != -1) {
                int i17 = layoutState.mOffset;
                i3 = i + i17;
                i2 = i17;
                i4 = 0;
            } else {
                int i18 = layoutState.mOffset;
                int i19 = i18 - i;
                i4 = 0;
                i2 = i19;
                i3 = i18;
            }
        } else if (layoutState.mLayoutDirection != -1) {
            int i20 = layoutState.mOffset;
            i16 = i20 + i;
            i3 = 0;
            i4 = i20;
            i2 = 0;
        } else {
            i16 = layoutState.mOffset;
            int i21 = i16 - i;
            i2 = 0;
            i3 = 0;
            i4 = i21;
        }
        int decoratedMeasurementInOther2 = i16;
        int paddingLeft = i3;
        int paddingTop = i4;
        int decoratedMeasurementInOther3 = i2;
        for (int i22 = 0; i22 < i6; i22++) {
            View view4 = this.mSet[i22];
            LayoutParams layoutParams2 = (LayoutParams) view4.getLayoutParams();
            if (this.mOrientation != 1) {
                paddingTop = getPaddingTop() + this.mCachedBorders[layoutParams2.mSpanIndex];
                decoratedMeasurementInOther2 = paddingTop + this.mOrientationHelper.getDecoratedMeasurementInOther(view4);
            } else if (isLayoutRTL()) {
                paddingLeft = getPaddingLeft() + this.mCachedBorders[this.mSpanCount - layoutParams2.mSpanIndex];
                decoratedMeasurementInOther3 = paddingLeft - this.mOrientationHelper.getDecoratedMeasurementInOther(view4);
            } else {
                decoratedMeasurementInOther3 = getPaddingLeft() + this.mCachedBorders[layoutParams2.mSpanIndex];
                paddingLeft = decoratedMeasurementInOther3 + this.mOrientationHelper.getDecoratedMeasurementInOther(view4);
            }
            layoutDecoratedWithMargins(view4, decoratedMeasurementInOther3, paddingTop, paddingLeft, decoratedMeasurementInOther2);
            if (layoutParams2.isItemRemoved() || layoutParams2.isItemChanged()) {
                layoutChunkResult.mIgnoreConsumed = true;
            }
            layoutChunkResult.mFocusable |= view4.hasFocusable();
        }
        Arrays.fill(this.mSet, (Object) null);
    }

    @Override // android.support.v7.widget.LinearLayoutManager
    void onAnchorReady(RecyclerView.Recycler recycler, RecyclerView.State state, LinearLayoutManager.AnchorInfo anchorInfo, int i) {
        super.onAnchorReady(recycler, state, anchorInfo, i);
        updateMeasurements();
        if (state.getItemCount() > 0 && !state.isPreLayout()) {
            ensureAnchorIsInCorrectSpan(recycler, state, anchorInfo, i);
        }
        ensureViewSet();
    }

    @Override // android.support.v7.widget.LinearLayoutManager, android.support.v7.widget.RecyclerView.LayoutManager
    public View onFocusSearchFailed(View view, int i, RecyclerView.Recycler recycler, RecyclerView.State state) {
        int childCount;
        int i2;
        int childCount2;
        int iMin;
        int i3;
        int i4;
        View view2;
        int iMin2;
        View view3;
        View viewFindContainingItemView = findContainingItemView(view);
        if (viewFindContainingItemView == null) {
            return null;
        }
        LayoutParams layoutParams = (LayoutParams) viewFindContainingItemView.getLayoutParams();
        int i5 = layoutParams.mSpanIndex;
        int i6 = layoutParams.mSpanIndex + layoutParams.mSpanSize;
        if (super.onFocusSearchFailed(view, i, recycler, state) == null) {
            return null;
        }
        if ((convertFocusDirectionToLayoutDirection(i) == 1) != this.mShouldReverseLayout) {
            childCount = getChildCount() - 1;
            i2 = -1;
            childCount2 = -1;
        } else {
            childCount = 0;
            i2 = 1;
            childCount2 = getChildCount();
        }
        boolean z = this.mOrientation == 1 && isLayoutRTL();
        View view4 = null;
        int i7 = -1;
        int i8 = 0;
        View view5 = null;
        int i9 = -1;
        int i10 = 0;
        int spanGroupIndex = getSpanGroupIndex(recycler, state, childCount);
        int i11 = childCount;
        while (i11 != childCount2) {
            int spanGroupIndex2 = getSpanGroupIndex(recycler, state, i11);
            View childAt = getChildAt(i11);
            if (childAt == viewFindContainingItemView) {
                break;
            }
            if (!childAt.hasFocusable() || spanGroupIndex2 == spanGroupIndex) {
                LayoutParams layoutParams2 = (LayoutParams) childAt.getLayoutParams();
                int i12 = layoutParams2.mSpanIndex;
                int i13 = layoutParams2.mSpanIndex + layoutParams2.mSpanSize;
                if (childAt.hasFocusable() && i12 == i5 && i13 == i6) {
                    return childAt;
                }
                boolean z2 = false;
                if ((childAt.hasFocusable() && view4 == null) || (!childAt.hasFocusable() && view5 == null)) {
                    z2 = true;
                } else {
                    int iMin3 = Math.min(i13, i6) - Math.max(i12, i5);
                    if (childAt.hasFocusable()) {
                        if (iMin3 > i8) {
                            z2 = true;
                        } else if (iMin3 == i8) {
                            if (z == (i12 > i7)) {
                                z2 = true;
                            }
                        }
                    } else if (view4 == null && isViewPartiallyVisible(childAt, false, true)) {
                        if (iMin3 > i10) {
                            z2 = true;
                        } else if (iMin3 == i10) {
                            if (z == (i12 > i9)) {
                                z2 = true;
                            }
                        }
                    }
                }
                if (!z2) {
                    iMin = i10;
                    i3 = i7;
                    i4 = i9;
                    view2 = view5;
                    iMin2 = i8;
                    view3 = view4;
                } else if (childAt.hasFocusable()) {
                    int i14 = layoutParams2.mSpanIndex;
                    int i15 = i10;
                    i4 = i9;
                    view2 = view5;
                    iMin2 = Math.min(i13, i6) - Math.max(i12, i5);
                    iMin = i15;
                    view3 = childAt;
                    i3 = i14;
                } else {
                    i4 = layoutParams2.mSpanIndex;
                    iMin = Math.min(i13, i6) - Math.max(i12, i5);
                    view2 = childAt;
                    iMin2 = i8;
                    i3 = i7;
                    view3 = view4;
                }
            } else {
                if (view4 != null) {
                    break;
                }
                iMin = i10;
                i3 = i7;
                i4 = i9;
                view2 = view5;
                iMin2 = i8;
                view3 = view4;
            }
            i11 += i2;
            view4 = view3;
            i8 = iMin2;
            i7 = i3;
            view5 = view2;
            i9 = i4;
            i10 = iMin;
        }
        return view4 == null ? view5 : view4;
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public void onInitializeAccessibilityNodeInfoForItem(RecyclerView.Recycler recycler, RecyclerView.State state, View view, AccessibilityNodeInfoCompat accessibilityNodeInfoCompat) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (!(layoutParams instanceof LayoutParams)) {
            super.onInitializeAccessibilityNodeInfoForItem(view, accessibilityNodeInfoCompat);
            return;
        }
        LayoutParams layoutParams2 = (LayoutParams) layoutParams;
        int spanGroupIndex = getSpanGroupIndex(recycler, state, layoutParams2.getViewLayoutPosition());
        if (this.mOrientation != 0) {
            accessibilityNodeInfoCompat.setCollectionItemInfo(AccessibilityNodeInfoCompat.CollectionItemInfoCompat.obtain(spanGroupIndex, 1, layoutParams2.getSpanIndex(), layoutParams2.getSpanSize(), this.mSpanCount > 1 && layoutParams2.getSpanSize() == this.mSpanCount, false));
        } else {
            accessibilityNodeInfoCompat.setCollectionItemInfo(AccessibilityNodeInfoCompat.CollectionItemInfoCompat.obtain(layoutParams2.getSpanIndex(), layoutParams2.getSpanSize(), spanGroupIndex, 1, this.mSpanCount > 1 && layoutParams2.getSpanSize() == this.mSpanCount, false));
        }
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public void onItemsAdded(RecyclerView recyclerView, int i, int i2) {
        this.mSpanSizeLookup.invalidateSpanIndexCache();
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public void onItemsChanged(RecyclerView recyclerView) {
        this.mSpanSizeLookup.invalidateSpanIndexCache();
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public void onItemsMoved(RecyclerView recyclerView, int i, int i2, int i3) {
        this.mSpanSizeLookup.invalidateSpanIndexCache();
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public void onItemsRemoved(RecyclerView recyclerView, int i, int i2) {
        this.mSpanSizeLookup.invalidateSpanIndexCache();
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public void onItemsUpdated(RecyclerView recyclerView, int i, int i2, Object obj) {
        this.mSpanSizeLookup.invalidateSpanIndexCache();
    }

    @Override // android.support.v7.widget.LinearLayoutManager, android.support.v7.widget.RecyclerView.LayoutManager
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (state.isPreLayout()) {
            cachePreLayoutSpanMapping();
        }
        super.onLayoutChildren(recycler, state);
        clearPreLayoutSpanMappingCache();
    }

    @Override // android.support.v7.widget.LinearLayoutManager, android.support.v7.widget.RecyclerView.LayoutManager
    public void onLayoutCompleted(RecyclerView.State state) {
        super.onLayoutCompleted(state);
        this.mPendingSpanCountChange = false;
    }

    @Override // android.support.v7.widget.LinearLayoutManager, android.support.v7.widget.RecyclerView.LayoutManager
    public int scrollHorizontallyBy(int i, RecyclerView.Recycler recycler, RecyclerView.State state) {
        updateMeasurements();
        ensureViewSet();
        return super.scrollHorizontallyBy(i, recycler, state);
    }

    @Override // android.support.v7.widget.LinearLayoutManager, android.support.v7.widget.RecyclerView.LayoutManager
    public int scrollVerticallyBy(int i, RecyclerView.Recycler recycler, RecyclerView.State state) {
        updateMeasurements();
        ensureViewSet();
        return super.scrollVerticallyBy(i, recycler, state);
    }

    @Override // android.support.v7.widget.RecyclerView.LayoutManager
    public void setMeasuredDimension(Rect rect, int i, int i2) {
        int iChooseSize;
        int iChooseSize2;
        if (this.mCachedBorders == null) {
            super.setMeasuredDimension(rect, i, i2);
        }
        int paddingLeft = getPaddingLeft() + getPaddingRight();
        int paddingTop = getPaddingTop() + getPaddingBottom();
        if (this.mOrientation != 1) {
            iChooseSize2 = chooseSize(i, paddingLeft + rect.width(), getMinimumWidth());
            iChooseSize = chooseSize(i2, paddingTop + this.mCachedBorders[this.mCachedBorders.length - 1], getMinimumHeight());
        } else {
            iChooseSize = chooseSize(i2, paddingTop + rect.height(), getMinimumHeight());
            iChooseSize2 = chooseSize(i, paddingLeft + this.mCachedBorders[this.mCachedBorders.length - 1], getMinimumWidth());
        }
        setMeasuredDimension(iChooseSize2, iChooseSize);
    }

    public void setSpanCount(int i) {
        if (i != this.mSpanCount) {
            this.mPendingSpanCountChange = true;
            if (i < 1) {
                throw new IllegalArgumentException("Span count should be at least 1. Provided " + i);
            }
            this.mSpanCount = i;
            this.mSpanSizeLookup.invalidateSpanIndexCache();
            requestLayout();
        }
    }

    public void setSpanSizeLookup(SpanSizeLookup spanSizeLookup) {
        this.mSpanSizeLookup = spanSizeLookup;
    }

    @Override // android.support.v7.widget.LinearLayoutManager
    public void setStackFromEnd(boolean z) {
        if (z) {
            throw new UnsupportedOperationException("GridLayoutManager does not support stack from end. Consider using reverse layout");
        }
        super.setStackFromEnd(false);
    }

    @Override // android.support.v7.widget.LinearLayoutManager, android.support.v7.widget.RecyclerView.LayoutManager
    public boolean supportsPredictiveItemAnimations() {
        return this.mPendingSavedState == null && !this.mPendingSpanCountChange;
    }
}
