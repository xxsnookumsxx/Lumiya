package uk.co.senab.photoview.gestures;

import android.view.MotionEvent;

/* JADX INFO: loaded from: classes.dex */
public interface GestureDetector {
    boolean isDragging();

    boolean isScaling();

    boolean onTouchEvent(MotionEvent motionEvent);

    void setOnGestureListener(OnGestureListener onGestureListener);
}
