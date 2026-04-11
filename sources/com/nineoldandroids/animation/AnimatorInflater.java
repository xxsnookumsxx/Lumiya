package com.nineoldandroids.animation;

import android.R;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.util.Xml;
import android.view.animation.AnimationUtils;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/* JADX INFO: loaded from: classes.dex */
public class AnimatorInflater {
    private static final int AnimatorSet_ordering = 0;
    private static final int Animator_duration = 1;
    private static final int Animator_interpolator = 0;
    private static final int Animator_repeatCount = 3;
    private static final int Animator_repeatMode = 4;
    private static final int Animator_startOffset = 2;
    private static final int Animator_valueFrom = 5;
    private static final int Animator_valueTo = 6;
    private static final int Animator_valueType = 7;
    private static final int PropertyAnimator_propertyName = 0;
    private static final int TOGETHER = 0;
    private static final int VALUE_TYPE_FLOAT = 0;
    private static final int[] AnimatorSet = {R.attr.ordering};
    private static final int[] PropertyAnimator = {R.attr.propertyName};
    private static final int[] Animator = {R.attr.interpolator, R.attr.duration, R.attr.startOffset, R.attr.repeatCount, R.attr.repeatMode, R.attr.valueFrom, R.attr.valueTo, R.attr.valueType};

    private static Animator createAnimatorFromXml(Context context, XmlPullParser xmlPullParser) throws XmlPullParserException, IOException {
        return createAnimatorFromXml(context, xmlPullParser, Xml.asAttributeSet(xmlPullParser), null, 0);
    }

    /* JADX WARN: Code restructure failed: missing block: B:35:0x009f, code lost:
    
        if (r2 == null) goto L8;
     */
    /* JADX WARN: Code restructure failed: missing block: B:36:0x00a1, code lost:
    
        r3 = new com.nineoldandroids.animation.Animator[r2.size()];
        r4 = r2.iterator();
     */
    /* JADX WARN: Code restructure failed: missing block: B:38:0x00af, code lost:
    
        if (r4.hasNext() != false) goto L41;
     */
    /* JADX WARN: Code restructure failed: missing block: B:39:0x00b1, code lost:
    
        if (r13 == 0) goto L42;
     */
    /* JADX WARN: Code restructure failed: missing block: B:40:0x00b3, code lost:
    
        r12.playSequentially(r3);
     */
    /* JADX WARN: Code restructure failed: missing block: B:41:0x00b8, code lost:
    
        r3[r5] = (com.nineoldandroids.animation.Animator) r4.next();
        r5 = r5 + 1;
     */
    /* JADX WARN: Code restructure failed: missing block: B:42:0x00c4, code lost:
    
        r12.playTogether(r3);
     */
    /* JADX WARN: Code restructure failed: missing block: B:7:0x0012, code lost:
    
        if (r12 != null) goto L35;
     */
    /* JADX WARN: Code restructure failed: missing block: B:8:0x0014, code lost:
    
        return r1;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    private static com.nineoldandroids.animation.Animator createAnimatorFromXml(android.content.Context r9, org.xmlpull.v1.XmlPullParser r10, android.util.AttributeSet r11, com.nineoldandroids.animation.AnimatorSet r12, int r13) throws org.xmlpull.v1.XmlPullParserException, java.io.IOException {
        /*
            Method dump skipped, instruction units count: 201
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: com.nineoldandroids.animation.AnimatorInflater.createAnimatorFromXml(android.content.Context, org.xmlpull.v1.XmlPullParser, android.util.AttributeSet, com.nineoldandroids.animation.AnimatorSet, int):com.nineoldandroids.animation.Animator");
    }

    public static Animator loadAnimator(Context context, int i) throws Resources.NotFoundException {
        XmlResourceParser animation = null;
        try {
            try {
                animation = context.getResources().getAnimation(i);
                return createAnimatorFromXml(context, animation);
            } catch (IOException e) {
                Resources.NotFoundException notFoundException = new Resources.NotFoundException("Can't load animation resource ID #0x" + Integer.toHexString(i));
                notFoundException.initCause(e);
                throw notFoundException;
            } catch (XmlPullParserException e2) {
                Resources.NotFoundException notFoundException2 = new Resources.NotFoundException("Can't load animation resource ID #0x" + Integer.toHexString(i));
                notFoundException2.initCause(e2);
                throw notFoundException2;
            }
        } finally {
            if (animation != null) {
                animation.close();
            }
        }
    }

    private static ValueAnimator loadAnimator(Context context, AttributeSet attributeSet, ValueAnimator valueAnimator) throws Resources.NotFoundException {
        TypedArray typedArrayObtainStyledAttributes = context.obtainStyledAttributes(attributeSet, Animator);
        long j = typedArrayObtainStyledAttributes.getInt(1, 0);
        long j2 = typedArrayObtainStyledAttributes.getInt(2, 0);
        int i = typedArrayObtainStyledAttributes.getInt(7, 0);
        if (valueAnimator == null) {
            valueAnimator = new ValueAnimator();
        }
        boolean z = i == 0;
        TypedValue typedValuePeekValue = typedArrayObtainStyledAttributes.peekValue(5);
        boolean z2 = typedValuePeekValue != null;
        int i2 = !z2 ? 0 : typedValuePeekValue.type;
        TypedValue typedValuePeekValue2 = typedArrayObtainStyledAttributes.peekValue(6);
        boolean z3 = typedValuePeekValue2 != null;
        int i3 = !z3 ? 0 : typedValuePeekValue2.type;
        if ((z2 && i2 >= 28 && i2 <= 31) || (z3 && i3 >= 28 && i3 <= 31)) {
            z = false;
            valueAnimator.setEvaluator(new ArgbEvaluator());
        }
        if (z) {
            if (z2) {
                float f = i2 != 5 ? typedArrayObtainStyledAttributes.getFloat(5, 0.0f) : typedArrayObtainStyledAttributes.getDimension(5, 0.0f);
                if (z3) {
                    valueAnimator.setFloatValues(f, i3 != 5 ? typedArrayObtainStyledAttributes.getFloat(6, 0.0f) : typedArrayObtainStyledAttributes.getDimension(6, 0.0f));
                } else {
                    valueAnimator.setFloatValues(f);
                }
            } else {
                valueAnimator.setFloatValues(i3 != 5 ? typedArrayObtainStyledAttributes.getFloat(6, 0.0f) : typedArrayObtainStyledAttributes.getDimension(6, 0.0f));
            }
        } else if (z2) {
            int color = i2 != 5 ? (i2 >= 28 && i2 <= 31) ? typedArrayObtainStyledAttributes.getColor(5, 0) : typedArrayObtainStyledAttributes.getInt(5, 0) : (int) typedArrayObtainStyledAttributes.getDimension(5, 0.0f);
            if (z3) {
                valueAnimator.setIntValues(color, i3 != 5 ? (i3 >= 28 && i3 <= 31) ? typedArrayObtainStyledAttributes.getColor(6, 0) : typedArrayObtainStyledAttributes.getInt(6, 0) : (int) typedArrayObtainStyledAttributes.getDimension(6, 0.0f));
            } else {
                valueAnimator.setIntValues(color);
            }
        } else if (z3) {
            valueAnimator.setIntValues(i3 != 5 ? (i3 >= 28 && i3 <= 31) ? typedArrayObtainStyledAttributes.getColor(6, 0) : typedArrayObtainStyledAttributes.getInt(6, 0) : (int) typedArrayObtainStyledAttributes.getDimension(6, 0.0f));
        }
        valueAnimator.setDuration(j);
        valueAnimator.setStartDelay(j2);
        if (typedArrayObtainStyledAttributes.hasValue(3)) {
            valueAnimator.setRepeatCount(typedArrayObtainStyledAttributes.getInt(3, 0));
        }
        if (typedArrayObtainStyledAttributes.hasValue(4)) {
            valueAnimator.setRepeatMode(typedArrayObtainStyledAttributes.getInt(4, 1));
        }
        int resourceId = typedArrayObtainStyledAttributes.getResourceId(0, 0);
        if (resourceId > 0) {
            valueAnimator.setInterpolator(AnimationUtils.loadInterpolator(context, resourceId));
        }
        typedArrayObtainStyledAttributes.recycle();
        return valueAnimator;
    }

    private static ObjectAnimator loadObjectAnimator(Context context, AttributeSet attributeSet) throws Resources.NotFoundException {
        ObjectAnimator objectAnimator = new ObjectAnimator();
        loadAnimator(context, attributeSet, objectAnimator);
        TypedArray typedArrayObtainStyledAttributes = context.obtainStyledAttributes(attributeSet, PropertyAnimator);
        objectAnimator.setPropertyName(typedArrayObtainStyledAttributes.getString(0));
        typedArrayObtainStyledAttributes.recycle();
        return objectAnimator;
    }
}
