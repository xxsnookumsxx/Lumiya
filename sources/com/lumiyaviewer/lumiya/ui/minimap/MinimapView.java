package com.lumiyaviewer.lumiya.ui.minimap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.WindowManager;
import com.google.common.base.Objects;
import com.lumiyaviewer.lumiya.slproto.modules.SLMinimap;
import com.lumiyaviewer.lumiya.slproto.types.ImmutableVector;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;

/* JADX INFO: loaded from: classes.dex */
public class MinimapView extends View {
    private static final float USER_MARK_TOUCH_SLACK = 50.0f;
    private int activePointerId;
    private float actualZoomFactor;
    private final Rect bitmapDstRect;
    private final Paint bitmapPaint;
    private final Rect bitmapSrcRect;
    private final Point displaySize;
    private Rect lastDrawRect;
    private float mapOffsetX;
    private float mapOffsetY;

    @Nullable
    private Bitmap minimapBitmap;
    private OnUserClickListener onUserClickListener;
    private float prevTouchX;
    private float prevTouchY;
    private final ScaleGestureDetector scaleGestureDetector;
    private final ScaleGestureDetector.OnScaleGestureListener scaleGestureListener;

    @Nullable
    private UUID selectedUser;

    @Nullable
    private SLMinimap.UserLocations userLocations;
    private final Paint userMarkPaint;

    interface OnUserClickListener {
        void onUserClick(UUID uuid);
    }

    public MinimapView(Context context) {
        super(context);
        this.onUserClickListener = null;
        this.actualZoomFactor = 1.0f;
        this.prevTouchX = 0.0f;
        this.prevTouchY = 0.0f;
        this.activePointerId = -1;
        this.mapOffsetX = 0.0f;
        this.mapOffsetY = 0.0f;
        this.selectedUser = null;
        this.lastDrawRect = null;
        this.displaySize = new Point();
        this.userMarkPaint = new Paint();
        this.bitmapPaint = new Paint();
        this.bitmapSrcRect = new Rect();
        this.bitmapDstRect = new Rect();
        this.scaleGestureListener = new ScaleGestureDetector.SimpleOnScaleGestureListener() { // from class: com.lumiyaviewer.lumiya.ui.minimap.MinimapView.1
            @Override // android.view.ScaleGestureDetector.SimpleOnScaleGestureListener, android.view.ScaleGestureDetector.OnScaleGestureListener
            public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
                MinimapView.this.actualZoomFactor = Math.min(Math.max(MinimapView.this.actualZoomFactor * scaleGestureDetector.getScaleFactor(), 1.0f), 5.0f);
                MinimapView.this.invalidate();
                return true;
            }
        };
        this.scaleGestureDetector = new ScaleGestureDetector(context, this.scaleGestureListener);
    }

    public MinimapView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.onUserClickListener = null;
        this.actualZoomFactor = 1.0f;
        this.prevTouchX = 0.0f;
        this.prevTouchY = 0.0f;
        this.activePointerId = -1;
        this.mapOffsetX = 0.0f;
        this.mapOffsetY = 0.0f;
        this.selectedUser = null;
        this.lastDrawRect = null;
        this.displaySize = new Point();
        this.userMarkPaint = new Paint();
        this.bitmapPaint = new Paint();
        this.bitmapSrcRect = new Rect();
        this.bitmapDstRect = new Rect();
        this.scaleGestureListener = new ScaleGestureDetector.SimpleOnScaleGestureListener() { // from class: com.lumiyaviewer.lumiya.ui.minimap.MinimapView.1
            @Override // android.view.ScaleGestureDetector.SimpleOnScaleGestureListener, android.view.ScaleGestureDetector.OnScaleGestureListener
            public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
                MinimapView.this.actualZoomFactor = Math.min(Math.max(MinimapView.this.actualZoomFactor * scaleGestureDetector.getScaleFactor(), 1.0f), 5.0f);
                MinimapView.this.invalidate();
                return true;
            }
        };
        this.scaleGestureDetector = new ScaleGestureDetector(context, this.scaleGestureListener);
    }

    public MinimapView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.onUserClickListener = null;
        this.actualZoomFactor = 1.0f;
        this.prevTouchX = 0.0f;
        this.prevTouchY = 0.0f;
        this.activePointerId = -1;
        this.mapOffsetX = 0.0f;
        this.mapOffsetY = 0.0f;
        this.selectedUser = null;
        this.lastDrawRect = null;
        this.displaySize = new Point();
        this.userMarkPaint = new Paint();
        this.bitmapPaint = new Paint();
        this.bitmapSrcRect = new Rect();
        this.bitmapDstRect = new Rect();
        this.scaleGestureListener = new ScaleGestureDetector.SimpleOnScaleGestureListener() { // from class: com.lumiyaviewer.lumiya.ui.minimap.MinimapView.1
            @Override // android.view.ScaleGestureDetector.SimpleOnScaleGestureListener, android.view.ScaleGestureDetector.OnScaleGestureListener
            public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
                MinimapView.this.actualZoomFactor = Math.min(Math.max(MinimapView.this.actualZoomFactor * scaleGestureDetector.getScaleFactor(), 1.0f), 5.0f);
                MinimapView.this.invalidate();
                return true;
            }
        };
        this.scaleGestureDetector = new ScaleGestureDetector(context, this.scaleGestureListener);
    }

    private void drawUserMark(ImmutableVector immutableVector, Canvas canvas, Paint paint, Rect rect, boolean z, float f, boolean z2) {
        float x = immutableVector.getX();
        float fWidth = rect.left + ((x / 256.0f) * rect.width());
        float y = rect.top + (((256.0f - immutableVector.getY()) / 256.0f) * rect.width());
        if (z) {
            paint.setARGB(255, 255, 255, 0);
        } else {
            paint.setARGB(255, 0, 255, 0);
        }
        paint.setStrokeWidth(0.0f);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        canvas.drawCircle(fWidth, y, 5.0f, paint);
        paint.setARGB(255, 128, 255, 128);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(fWidth, y, 5.0f, paint);
        if (z && (!Float.isNaN(f))) {
            float fCos = (float) (Math.cos(f) * 20.0d);
            float fSin = (float) (Math.sin(f) * 20.0d);
            float fCos2 = (float) ((Math.cos(f) * 15.0d) - (Math.sin(f) * (-5.0d)));
            float fCos3 = (float) ((Math.cos(f) * (-5.0d)) + (Math.sin(f) * 15.0d));
            float fCos4 = (float) ((Math.cos(f) * 15.0d) - (Math.sin(f) * 5.0d));
            float fCos5 = (float) ((Math.cos(f) * 5.0d) + (Math.sin(f) * 15.0d));
            paint.setStrokeWidth(3.0f);
            canvas.drawLine(fWidth, y, fWidth + fCos, y - fSin, paint);
            canvas.drawLine(fWidth + fCos, y - fSin, fCos2 + fWidth, y - fCos3, paint);
            canvas.drawLine(fWidth + fCos, y - fSin, fWidth + fCos4, y - fCos5, paint);
        }
        if (z2) {
            paint.setStrokeWidth(2.0f);
            paint.setARGB(255, 255, 255, 0);
            canvas.drawCircle(fWidth, y, 10.0f, paint);
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:21:0x009f  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    private void handleTouch(float r10, float r11) {
        /*
            r9 = this;
            r8 = 1132462080(0x43800000, float:256.0)
            r0 = 0
            com.lumiyaviewer.lumiya.slproto.modules.SLMinimap$UserLocations r1 = r9.userLocations
            if (r1 == 0) goto L9e
            android.graphics.Rect r1 = r9.lastDrawRect
            if (r1 == 0) goto L9e
            android.content.res.Resources r1 = r9.getResources()
            android.util.DisplayMetrics r1 = r1.getDisplayMetrics()
            r2 = 1
            r3 = 1112014848(0x42480000, float:50.0)
            float r4 = android.util.TypedValue.applyDimension(r2, r3, r1)
            r1 = 0
            com.lumiyaviewer.lumiya.slproto.modules.SLMinimap$UserLocations r2 = r9.userLocations
            java.util.Map<java.util.UUID, com.lumiyaviewer.lumiya.slproto.modules.SLMinimap$UserLocation> r2 = r2.userPositions
            java.util.Set r2 = r2.entrySet()
            java.util.Iterator r5 = r2.iterator()
            r2 = r0
            r3 = r1
        L29:
            boolean r0 = r5.hasNext()
            if (r0 == 0) goto L92
            java.lang.Object r0 = r5.next()
            java.util.Map$Entry r0 = (java.util.Map.Entry) r0
            java.lang.Object r1 = r0.getValue()
            com.lumiyaviewer.lumiya.slproto.modules.SLMinimap$UserLocation r1 = (com.lumiyaviewer.lumiya.slproto.modules.SLMinimap.UserLocation) r1
            com.lumiyaviewer.lumiya.slproto.types.ImmutableVector r1 = r1.location
            float r6 = r1.getX()
            float r6 = r6 / r8
            android.graphics.Rect r7 = r9.lastDrawRect
            int r7 = r7.width()
            float r7 = (float) r7
            float r6 = r6 * r7
            android.graphics.Rect r7 = r9.lastDrawRect
            int r7 = r7.left
            float r7 = (float) r7
            float r6 = r6 + r7
            float r1 = r1.getY()
            float r1 = r8 - r1
            float r1 = r1 / r8
            android.graphics.Rect r7 = r9.lastDrawRect
            int r7 = r7.width()
            float r7 = (float) r7
            float r1 = r1 * r7
            android.graphics.Rect r7 = r9.lastDrawRect
            int r7 = r7.top
            float r7 = (float) r7
            float r1 = r1 + r7
            float r6 = r6 - r10
            float r6 = java.lang.Math.abs(r6)
            float r1 = r1 - r11
            float r1 = java.lang.Math.abs(r1)
            float r6 = r6 * r6
            float r1 = r1 * r1
            float r1 = r1 + r6
            double r6 = (double) r1
            double r6 = java.lang.Math.sqrt(r6)
            float r1 = (float) r6
            int r6 = (r1 > r4 ? 1 : (r1 == r4 ? 0 : -1))
            if (r6 >= 0) goto L9f
            if (r2 != 0) goto L87
            java.lang.Object r0 = r0.getKey()
            java.util.UUID r0 = (java.util.UUID) r0
        L84:
            r2 = r0
            r3 = r1
            goto L29
        L87:
            int r6 = (r1 > r3 ? 1 : (r1 == r3 ? 0 : -1))
            if (r6 >= 0) goto L9f
            java.lang.Object r0 = r0.getKey()
            java.util.UUID r0 = (java.util.UUID) r0
            goto L84
        L92:
            r9.setSelectedUser(r2)
            com.lumiyaviewer.lumiya.ui.minimap.MinimapView$OnUserClickListener r0 = r9.onUserClickListener
            if (r0 == 0) goto L9e
            com.lumiyaviewer.lumiya.ui.minimap.MinimapView$OnUserClickListener r0 = r9.onUserClickListener
            r0.onUserClick(r2)
        L9e:
            return
        L9f:
            r0 = r2
            r1 = r3
            goto L84
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lumiyaviewer.lumiya.ui.minimap.MinimapView.handleTouch(float, float):void");
    }

    @Override // android.view.View
    protected void onDraw(Canvas canvas) {
        if (this.minimapBitmap != null) {
            int width = getWidth();
            int height = getHeight();
            int iRound = Math.round(Math.min(width, height) * this.actualZoomFactor);
            int i = width / 2;
            int i2 = height / 2;
            if (iRound <= width) {
                this.mapOffsetX = 0.0f;
            }
            if (iRound <= height) {
                this.mapOffsetY = 0.0f;
            }
            int i3 = (i - (iRound / 2)) + ((int) this.mapOffsetX);
            if (i3 > 0 && iRound > width) {
                this.mapOffsetX = (iRound / 2) - i;
                i3 = (i - (iRound / 2)) + ((int) this.mapOffsetX);
            }
            if (i3 + iRound <= width && iRound > width) {
                this.mapOffsetX = ((width - iRound) - i) + (iRound / 2);
                i3 = (i - (iRound / 2)) + ((int) this.mapOffsetX);
            }
            int i4 = (i2 - (iRound / 2)) + ((int) this.mapOffsetY);
            if (i4 > 0 && iRound > height) {
                this.mapOffsetY = (iRound / 2) - i2;
                i4 = (i2 - (iRound / 2)) + ((int) this.mapOffsetY);
            }
            if (i4 + iRound <= height && iRound > height) {
                this.mapOffsetY = ((height - iRound) - i2) + (iRound / 2);
                i4 = (i2 - (iRound / 2)) + ((int) this.mapOffsetY);
            }
            this.bitmapDstRect.set(i3, i4, i3 + iRound, iRound + i4);
            this.bitmapSrcRect.set(0, 0, this.minimapBitmap.getWidth(), this.minimapBitmap.getHeight());
            canvas.drawBitmap(this.minimapBitmap, this.bitmapSrcRect, this.bitmapDstRect, this.bitmapPaint);
            if (this.userLocations != null) {
                Iterator<T> it = this.userLocations.userPositions.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry entry = (Map.Entry) it.next();
                    drawUserMark(((SLMinimap.UserLocation) entry.getValue()).location, canvas, this.userMarkPaint, this.bitmapDstRect, false, Float.NaN, Objects.equal(this.selectedUser, entry.getKey()));
                }
                ImmutableVector immutableVector = this.userLocations.myAvatarPosition;
                if (immutableVector != null) {
                    drawUserMark(immutableVector, canvas, this.userMarkPaint, this.bitmapDstRect, true, this.userLocations.myAvatarHeading, false);
                }
            }
            if (this.lastDrawRect == null) {
                this.lastDrawRect = new Rect(this.bitmapDstRect);
            } else {
                this.lastDrawRect.set(this.bitmapDstRect);
            }
        }
    }

    @Override // android.view.View
    protected void onMeasure(int i, int i2) {
        Display defaultDisplay = ((WindowManager) getContext().getSystemService("window")).getDefaultDisplay();
        if (Build.VERSION.SDK_INT >= 13) {
            defaultDisplay.getSize(this.displaySize);
        } else {
            this.displaySize.set(defaultDisplay.getWidth(), defaultDisplay.getHeight());
        }
        int iMin = Math.min(this.displaySize.x, this.displaySize.y);
        if (View.MeasureSpec.getMode(i) != 0) {
            iMin = Math.min(iMin, View.MeasureSpec.getSize(i));
        }
        if (View.MeasureSpec.getMode(i2) != 0) {
            iMin = Math.min(iMin, View.MeasureSpec.getSize(i2));
        }
        setMeasuredDimension(iMin, iMin);
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        this.scaleGestureDetector.onTouchEvent(motionEvent);
        switch (motionEvent.getActionMasked()) {
            case 0:
                this.activePointerId = motionEvent.getPointerId(0);
                this.prevTouchX = motionEvent.getX();
                this.prevTouchY = motionEvent.getY();
                handleTouch(this.prevTouchX, this.prevTouchY);
                return true;
            case 1:
                this.activePointerId = -1;
                return true;
            case 2:
                int iFindPointerIndex = motionEvent.findPointerIndex(this.activePointerId);
                float x = motionEvent.getX(iFindPointerIndex);
                float y = motionEvent.getY(iFindPointerIndex);
                if (!this.scaleGestureDetector.isInProgress()) {
                    float f = x - this.prevTouchX;
                    float f2 = y - this.prevTouchY;
                    this.mapOffsetX = f + this.mapOffsetX;
                    this.mapOffsetY += f2;
                    invalidate();
                }
                this.prevTouchX = x;
                this.prevTouchY = y;
                return true;
            case 3:
                this.activePointerId = -1;
                return true;
            case 4:
            case 5:
            default:
                return true;
            case 6:
                int actionIndex = motionEvent.getActionIndex();
                if (motionEvent.getPointerId(actionIndex) == this.activePointerId) {
                    int i = actionIndex == 0 ? 1 : 0;
                    this.prevTouchX = motionEvent.getX(i);
                    this.prevTouchY = motionEvent.getY(i);
                    this.activePointerId = motionEvent.getPointerId(i);
                }
                return true;
        }
    }

    void setMinimapBitmap(@Nullable SLMinimap.MinimapBitmap minimapBitmap) {
        if (minimapBitmap == null) {
            if (this.minimapBitmap != null) {
                this.minimapBitmap.recycle();
                this.minimapBitmap = null;
            }
        } else if (this.minimapBitmap == null) {
            this.minimapBitmap = minimapBitmap.makeBitmap();
        } else {
            minimapBitmap.updateBitmap(this.minimapBitmap);
        }
        invalidate();
    }

    void setOnUserClickListener(OnUserClickListener onUserClickListener) {
        this.onUserClickListener = onUserClickListener;
    }

    void setSelectedUser(@Nullable UUID uuid) {
        if (Objects.equal(uuid, this.selectedUser)) {
            return;
        }
        this.selectedUser = uuid;
        invalidate();
    }

    void setUserLocations(@Nullable SLMinimap.UserLocations userLocations) {
        this.userLocations = userLocations;
        invalidate();
    }
}
