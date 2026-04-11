package android.support.graphics.drawable;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.Keyframe;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.os.Build;
import android.support.annotation.AnimatorRes;
import android.support.annotation.RestrictTo;
import android.support.v4.content.res.TypedArrayUtils;
import android.support.v4.graphics.PathParser;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.util.Xml;
import android.view.InflateException;
import com.google.vr.cardboard.VrSettingsProviderContract;
import java.io.IOException;
import java.util.ArrayList;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/* JADX INFO: loaded from: classes.dex */
@RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
public class AnimatorInflaterCompat {
    private static final boolean DBG_ANIMATOR_INFLATER = false;
    private static final int MAX_NUM_POINTS = 100;
    private static final String TAG = "AnimatorInflater";
    private static final int TOGETHER = 0;
    private static final int VALUE_TYPE_COLOR = 3;
    private static final int VALUE_TYPE_FLOAT = 0;
    private static final int VALUE_TYPE_INT = 1;
    private static final int VALUE_TYPE_PATH = 2;
    private static final int VALUE_TYPE_UNDEFINED = 4;

    private static class PathDataEvaluator implements TypeEvaluator<PathParser.PathDataNode[]> {
        private PathParser.PathDataNode[] mNodeArray;

        private PathDataEvaluator() {
        }

        PathDataEvaluator(PathParser.PathDataNode[] pathDataNodeArr) {
            this.mNodeArray = pathDataNodeArr;
        }

        @Override // android.animation.TypeEvaluator
        public PathParser.PathDataNode[] evaluate(float f, PathParser.PathDataNode[] pathDataNodeArr, PathParser.PathDataNode[] pathDataNodeArr2) {
            if (!PathParser.canMorph(pathDataNodeArr, pathDataNodeArr2)) {
                throw new IllegalArgumentException("Can't interpolate between two incompatible pathData");
            }
            if (this.mNodeArray == null || !PathParser.canMorph(this.mNodeArray, pathDataNodeArr)) {
                this.mNodeArray = PathParser.deepCopyNodes(pathDataNodeArr);
            }
            for (int i = 0; i < pathDataNodeArr.length; i++) {
                this.mNodeArray[i].interpolatePathDataNode(pathDataNodeArr[i], pathDataNodeArr2[i], f);
            }
            return this.mNodeArray;
        }
    }

    private static Animator createAnimatorFromXml(Context context, Resources resources, Resources.Theme theme, XmlPullParser xmlPullParser, float f) throws XmlPullParserException, IOException {
        return createAnimatorFromXml(context, resources, theme, xmlPullParser, Xml.asAttributeSet(xmlPullParser), null, 0, f);
    }

    /* JADX WARN: Code restructure failed: missing block: B:41:0x00f4, code lost:
    
        if (r13 == null) goto L8;
     */
    /* JADX WARN: Code restructure failed: missing block: B:42:0x00f6, code lost:
    
        r8 = new android.animation.Animator[r13.size()];
        r9 = r13.iterator();
        r6 = 0;
     */
    /* JADX WARN: Code restructure failed: missing block: B:44:0x0106, code lost:
    
        if (r9.hasNext() != false) goto L47;
     */
    /* JADX WARN: Code restructure failed: missing block: B:45:0x0108, code lost:
    
        if (r23 == 0) goto L48;
     */
    /* JADX WARN: Code restructure failed: missing block: B:46:0x010a, code lost:
    
        r22.playSequentially(r8);
     */
    /* JADX WARN: Code restructure failed: missing block: B:47:0x0111, code lost:
    
        r8[r6] = (android.animation.Animator) r9.next();
        r6 = r6 + 1;
     */
    /* JADX WARN: Code restructure failed: missing block: B:48:0x011d, code lost:
    
        r22.playTogether(r8);
     */
    /* JADX WARN: Code restructure failed: missing block: B:7:0x0011, code lost:
    
        if (r22 != null) goto L41;
     */
    /* JADX WARN: Code restructure failed: missing block: B:8:0x0013, code lost:
    
        return r5;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    private static android.animation.Animator createAnimatorFromXml(android.content.Context r17, android.content.res.Resources r18, android.content.res.Resources.Theme r19, org.xmlpull.v1.XmlPullParser r20, android.util.AttributeSet r21, android.animation.AnimatorSet r22, int r23, float r24) throws org.xmlpull.v1.XmlPullParserException, java.io.IOException {
        /*
            Method dump skipped, instruction units count: 292
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.graphics.drawable.AnimatorInflaterCompat.createAnimatorFromXml(android.content.Context, android.content.res.Resources, android.content.res.Resources$Theme, org.xmlpull.v1.XmlPullParser, android.util.AttributeSet, android.animation.AnimatorSet, int, float):android.animation.Animator");
    }

    private static Keyframe createNewKeyframe(Keyframe keyframe, float f) {
        return keyframe.getType() != Float.TYPE ? keyframe.getType() != Integer.TYPE ? Keyframe.ofObject(f) : Keyframe.ofInt(f) : Keyframe.ofFloat(f);
    }

    private static void distributeKeyframes(Keyframe[] keyframeArr, float f, int i, int i2) {
        float f2 = f / ((i2 - i) + 2);
        while (i <= i2) {
            keyframeArr[i].setFraction(keyframeArr[i - 1].getFraction() + f2);
            i++;
        }
    }

    private static void dumpKeyframes(Object[] objArr, String str) {
        if (objArr == null || objArr.length == 0) {
            return;
        }
        Log.d(TAG, str);
        int length = objArr.length;
        for (int i = 0; i < length; i++) {
            Keyframe keyframe = (Keyframe) objArr[i];
            Log.d(TAG, "Keyframe " + i + ": fraction " + (keyframe.getFraction() < 0.0f ? "null" : Float.valueOf(keyframe.getFraction())) + ", , value : " + (!keyframe.hasValue() ? "null" : keyframe.getValue()));
        }
    }

    private static PropertyValuesHolder getPVH(TypedArray typedArray, int i, int i2, int i3, String str) {
        PropertyValuesHolder propertyValuesHolderOfFloat;
        TypedValue typedValuePeekValue = typedArray.peekValue(i2);
        boolean z = typedValuePeekValue != null;
        int i4 = !z ? 0 : typedValuePeekValue.type;
        TypedValue typedValuePeekValue2 = typedArray.peekValue(i3);
        boolean z2 = typedValuePeekValue2 != null;
        int i5 = !z2 ? 0 : typedValuePeekValue2.type;
        if (i == 4) {
            i = ((z && isColorType(i4)) || (z2 && isColorType(i5))) ? 3 : 0;
        }
        boolean z3 = i == 0;
        if (i == 2) {
            String string = typedArray.getString(i2);
            String string2 = typedArray.getString(i3);
            PathParser.PathDataNode[] pathDataNodeArrCreateNodesFromPathData = PathParser.createNodesFromPathData(string);
            PathParser.PathDataNode[] pathDataNodeArrCreateNodesFromPathData2 = PathParser.createNodesFromPathData(string2);
            if (pathDataNodeArrCreateNodesFromPathData == null && pathDataNodeArrCreateNodesFromPathData2 == null) {
                return null;
            }
            if (pathDataNodeArrCreateNodesFromPathData == null) {
                if (pathDataNodeArrCreateNodesFromPathData2 == null) {
                    return null;
                }
                return PropertyValuesHolder.ofObject(str, new PathDataEvaluator(), pathDataNodeArrCreateNodesFromPathData2);
            }
            PathDataEvaluator pathDataEvaluator = new PathDataEvaluator();
            if (pathDataNodeArrCreateNodesFromPathData2 == null) {
                return PropertyValuesHolder.ofObject(str, pathDataEvaluator, pathDataNodeArrCreateNodesFromPathData);
            }
            if (PathParser.canMorph(pathDataNodeArrCreateNodesFromPathData, pathDataNodeArrCreateNodesFromPathData2)) {
                return PropertyValuesHolder.ofObject(str, pathDataEvaluator, pathDataNodeArrCreateNodesFromPathData, pathDataNodeArrCreateNodesFromPathData2);
            }
            throw new InflateException(" Can't morph from " + string + " to " + string2);
        }
        ArgbEvaluator argbEvaluator = i != 3 ? null : ArgbEvaluator.getInstance();
        if (z3) {
            if (z) {
                float f = i4 != 5 ? typedArray.getFloat(i2, 0.0f) : typedArray.getDimension(i2, 0.0f);
                if (z2) {
                    propertyValuesHolderOfFloat = PropertyValuesHolder.ofFloat(str, f, i5 != 5 ? typedArray.getFloat(i3, 0.0f) : typedArray.getDimension(i3, 0.0f));
                } else {
                    propertyValuesHolderOfFloat = PropertyValuesHolder.ofFloat(str, f);
                }
            } else {
                propertyValuesHolderOfFloat = PropertyValuesHolder.ofFloat(str, i5 != 5 ? typedArray.getFloat(i3, 0.0f) : typedArray.getDimension(i3, 0.0f));
            }
        } else if (z) {
            int i6 = i4 != 5 ? !isColorType(i4) ? typedArray.getInt(i2, 0) : typedArray.getColor(i2, 0) : (int) typedArray.getDimension(i2, 0.0f);
            if (z2) {
                propertyValuesHolderOfFloat = PropertyValuesHolder.ofInt(str, i6, i5 != 5 ? !isColorType(i5) ? typedArray.getInt(i3, 0) : typedArray.getColor(i3, 0) : (int) typedArray.getDimension(i3, 0.0f));
            } else {
                propertyValuesHolderOfFloat = PropertyValuesHolder.ofInt(str, i6);
            }
        } else if (z2) {
            propertyValuesHolderOfFloat = PropertyValuesHolder.ofInt(str, i5 != 5 ? !isColorType(i5) ? typedArray.getInt(i3, 0) : typedArray.getColor(i3, 0) : (int) typedArray.getDimension(i3, 0.0f));
        } else {
            propertyValuesHolderOfFloat = null;
        }
        if (propertyValuesHolderOfFloat == null || argbEvaluator == null) {
            return propertyValuesHolderOfFloat;
        }
        propertyValuesHolderOfFloat.setEvaluator(argbEvaluator);
        return propertyValuesHolderOfFloat;
    }

    private static int inferValueTypeFromValues(TypedArray typedArray, int i, int i2) {
        TypedValue typedValuePeekValue = typedArray.peekValue(i);
        boolean z = typedValuePeekValue != null;
        int i3 = !z ? 0 : typedValuePeekValue.type;
        TypedValue typedValuePeekValue2 = typedArray.peekValue(i2);
        boolean z2 = typedValuePeekValue2 != null;
        return ((z && isColorType(i3)) || (z2 && isColorType(!z2 ? 0 : typedValuePeekValue2.type))) ? 3 : 0;
    }

    private static int inferValueTypeOfKeyframe(Resources resources, Resources.Theme theme, AttributeSet attributeSet, XmlPullParser xmlPullParser) {
        int i = 0;
        TypedArray typedArrayObtainAttributes = TypedArrayUtils.obtainAttributes(resources, theme, attributeSet, AndroidResources.STYLEABLE_KEYFRAME);
        TypedValue typedValuePeekNamedValue = TypedArrayUtils.peekNamedValue(typedArrayObtainAttributes, xmlPullParser, VrSettingsProviderContract.SETTING_VALUE_KEY, 0);
        if ((typedValuePeekNamedValue != null) && isColorType(typedValuePeekNamedValue.type)) {
            i = 3;
        }
        typedArrayObtainAttributes.recycle();
        return i;
    }

    private static boolean isColorType(int i) {
        return i >= 28 && i <= 31;
    }

    public static Animator loadAnimator(Context context, @AnimatorRes int i) throws Resources.NotFoundException {
        return Build.VERSION.SDK_INT < 24 ? loadAnimator(context, context.getResources(), context.getTheme(), i) : AnimatorInflater.loadAnimator(context, i);
    }

    public static Animator loadAnimator(Context context, Resources resources, Resources.Theme theme, @AnimatorRes int i) throws Resources.NotFoundException {
        return loadAnimator(context, resources, theme, i, 1.0f);
    }

    public static Animator loadAnimator(Context context, Resources resources, Resources.Theme theme, @AnimatorRes int i, float f) throws Resources.NotFoundException {
        XmlResourceParser animation = null;
        try {
            try {
                try {
                    animation = resources.getAnimation(i);
                    return createAnimatorFromXml(context, resources, theme, animation, f);
                } catch (XmlPullParserException e) {
                    Resources.NotFoundException notFoundException = new Resources.NotFoundException("Can't load animation resource ID #0x" + Integer.toHexString(i));
                    notFoundException.initCause(e);
                    throw notFoundException;
                }
            } catch (IOException e2) {
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

    private static ValueAnimator loadAnimator(Context context, Resources resources, Resources.Theme theme, AttributeSet attributeSet, ValueAnimator valueAnimator, float f, XmlPullParser xmlPullParser) throws Resources.NotFoundException {
        TypedArray typedArrayObtainAttributes = TypedArrayUtils.obtainAttributes(resources, theme, attributeSet, AndroidResources.STYLEABLE_ANIMATOR);
        TypedArray typedArrayObtainAttributes2 = TypedArrayUtils.obtainAttributes(resources, theme, attributeSet, AndroidResources.STYLEABLE_PROPERTY_ANIMATOR);
        if (valueAnimator == null) {
            valueAnimator = new ValueAnimator();
        }
        parseAnimatorFromTypeArray(valueAnimator, typedArrayObtainAttributes, typedArrayObtainAttributes2, f, xmlPullParser);
        int namedResourceId = TypedArrayUtils.getNamedResourceId(typedArrayObtainAttributes, xmlPullParser, "interpolator", 0, 0);
        if (namedResourceId > 0) {
            valueAnimator.setInterpolator(AnimationUtilsCompat.loadInterpolator(context, namedResourceId));
        }
        typedArrayObtainAttributes.recycle();
        if (typedArrayObtainAttributes2 != null) {
            typedArrayObtainAttributes2.recycle();
        }
        return valueAnimator;
    }

    private static Keyframe loadKeyframe(Context context, Resources resources, Resources.Theme theme, AttributeSet attributeSet, int i, XmlPullParser xmlPullParser) throws XmlPullParserException, IOException {
        Keyframe keyframeOfInt;
        TypedArray typedArrayObtainAttributes = TypedArrayUtils.obtainAttributes(resources, theme, attributeSet, AndroidResources.STYLEABLE_KEYFRAME);
        float namedFloat = TypedArrayUtils.getNamedFloat(typedArrayObtainAttributes, xmlPullParser, "fraction", 3, -1.0f);
        TypedValue typedValuePeekNamedValue = TypedArrayUtils.peekNamedValue(typedArrayObtainAttributes, xmlPullParser, VrSettingsProviderContract.SETTING_VALUE_KEY, 0);
        boolean z = typedValuePeekNamedValue != null;
        if (i == 4) {
            i = (z && isColorType(typedValuePeekNamedValue.type)) ? 3 : 0;
        }
        if (z) {
            switch (i) {
                case 0:
                    keyframeOfInt = Keyframe.ofFloat(namedFloat, TypedArrayUtils.getNamedFloat(typedArrayObtainAttributes, xmlPullParser, VrSettingsProviderContract.SETTING_VALUE_KEY, 0, 0.0f));
                    break;
                case 1:
                case 3:
                    keyframeOfInt = Keyframe.ofInt(namedFloat, TypedArrayUtils.getNamedInt(typedArrayObtainAttributes, xmlPullParser, VrSettingsProviderContract.SETTING_VALUE_KEY, 0, 0));
                    break;
                case 2:
                default:
                    keyframeOfInt = null;
                    break;
            }
        } else {
            keyframeOfInt = i != 0 ? Keyframe.ofInt(namedFloat) : Keyframe.ofFloat(namedFloat);
        }
        int namedResourceId = TypedArrayUtils.getNamedResourceId(typedArrayObtainAttributes, xmlPullParser, "interpolator", 1, 0);
        if (namedResourceId > 0) {
            keyframeOfInt.setInterpolator(AnimationUtilsCompat.loadInterpolator(context, namedResourceId));
        }
        typedArrayObtainAttributes.recycle();
        return keyframeOfInt;
    }

    private static ObjectAnimator loadObjectAnimator(Context context, Resources resources, Resources.Theme theme, AttributeSet attributeSet, float f, XmlPullParser xmlPullParser) throws Resources.NotFoundException {
        ObjectAnimator objectAnimator = new ObjectAnimator();
        loadAnimator(context, resources, theme, attributeSet, objectAnimator, f, xmlPullParser);
        return objectAnimator;
    }

    private static PropertyValuesHolder loadPvh(Context context, Resources resources, Resources.Theme theme, XmlPullParser xmlPullParser, String str, int i) throws XmlPullParserException, IOException {
        ArrayList arrayList;
        int size;
        int i2;
        ArrayList arrayList2 = null;
        while (true) {
            arrayList = arrayList2;
            int next = xmlPullParser.next();
            if (next == 3 || next == 1) {
                break;
            }
            if (xmlPullParser.getName().equals("keyframe")) {
                int iInferValueTypeOfKeyframe = i != 4 ? i : inferValueTypeOfKeyframe(resources, theme, Xml.asAttributeSet(xmlPullParser), xmlPullParser);
                Keyframe keyframeLoadKeyframe = loadKeyframe(context, resources, theme, Xml.asAttributeSet(xmlPullParser), iInferValueTypeOfKeyframe, xmlPullParser);
                if (keyframeLoadKeyframe != null) {
                    if (arrayList == null) {
                        arrayList = new ArrayList();
                    }
                    arrayList.add(keyframeLoadKeyframe);
                }
                xmlPullParser.next();
                arrayList2 = arrayList;
                i = iInferValueTypeOfKeyframe;
            } else {
                arrayList2 = arrayList;
            }
        }
        if (arrayList == null || (size = arrayList.size()) <= 0) {
            return null;
        }
        Keyframe keyframe = (Keyframe) arrayList.get(0);
        Keyframe keyframe2 = (Keyframe) arrayList.get(size - 1);
        float fraction = keyframe2.getFraction();
        if (fraction >= 1.0f) {
            i2 = size;
        } else if (fraction < 0.0f) {
            keyframe2.setFraction(1.0f);
            i2 = size;
        } else {
            arrayList.add(arrayList.size(), createNewKeyframe(keyframe2, 1.0f));
            i2 = size + 1;
        }
        float fraction2 = keyframe.getFraction();
        if (fraction2 != 0.0f) {
            if (fraction2 < 0.0f) {
                keyframe.setFraction(0.0f);
            } else {
                arrayList.add(0, createNewKeyframe(keyframe, 0.0f));
                i2++;
            }
        }
        Keyframe[] keyframeArr = new Keyframe[i2];
        arrayList.toArray(keyframeArr);
        for (int i3 = 0; i3 < i2; i3++) {
            Keyframe keyframe3 = keyframeArr[i3];
            if (keyframe3.getFraction() < 0.0f) {
                if (i3 == 0) {
                    keyframe3.setFraction(0.0f);
                } else if (i3 != i2 - 1) {
                    int i4 = i3;
                    for (int i5 = i3 + 1; i5 < i2 - 1 && keyframeArr[i5].getFraction() < 0.0f; i5++) {
                        i4 = i5;
                    }
                    distributeKeyframes(keyframeArr, keyframeArr[i4 + 1].getFraction() - keyframeArr[i3 - 1].getFraction(), i3, i4);
                } else {
                    keyframe3.setFraction(1.0f);
                }
            }
        }
        PropertyValuesHolder propertyValuesHolderOfKeyframe = PropertyValuesHolder.ofKeyframe(str, keyframeArr);
        if (i != 3) {
            return propertyValuesHolderOfKeyframe;
        }
        propertyValuesHolderOfKeyframe.setEvaluator(ArgbEvaluator.getInstance());
        return propertyValuesHolderOfKeyframe;
    }

    private static PropertyValuesHolder[] loadValues(Context context, Resources resources, Resources.Theme theme, XmlPullParser xmlPullParser, AttributeSet attributeSet) throws XmlPullParserException, IOException {
        ArrayList arrayList = null;
        while (true) {
            int eventType = xmlPullParser.getEventType();
            if (eventType == 3 || eventType == 1) {
                break;
            }
            if (eventType == 2) {
                if (xmlPullParser.getName().equals("propertyValuesHolder")) {
                    TypedArray typedArrayObtainAttributes = TypedArrayUtils.obtainAttributes(resources, theme, attributeSet, AndroidResources.STYLEABLE_PROPERTY_VALUES_HOLDER);
                    String namedString = TypedArrayUtils.getNamedString(typedArrayObtainAttributes, xmlPullParser, "propertyName", 3);
                    int namedInt = TypedArrayUtils.getNamedInt(typedArrayObtainAttributes, xmlPullParser, "valueType", 2, 4);
                    PropertyValuesHolder propertyValuesHolderLoadPvh = loadPvh(context, resources, theme, xmlPullParser, namedString, namedInt);
                    if (propertyValuesHolderLoadPvh == null) {
                        propertyValuesHolderLoadPvh = getPVH(typedArrayObtainAttributes, namedInt, 0, 1, namedString);
                    }
                    if (propertyValuesHolderLoadPvh != null) {
                        if (arrayList == null) {
                            arrayList = new ArrayList();
                        }
                        arrayList.add(propertyValuesHolderLoadPvh);
                    }
                    typedArrayObtainAttributes.recycle();
                }
                xmlPullParser.next();
                arrayList = arrayList;
            } else {
                xmlPullParser.next();
            }
        }
        if (arrayList == null) {
            return null;
        }
        int size = arrayList.size();
        PropertyValuesHolder[] propertyValuesHolderArr = new PropertyValuesHolder[size];
        for (int i = 0; i < size; i++) {
            propertyValuesHolderArr[i] = (PropertyValuesHolder) arrayList.get(i);
        }
        return propertyValuesHolderArr;
    }

    private static void parseAnimatorFromTypeArray(ValueAnimator valueAnimator, TypedArray typedArray, TypedArray typedArray2, float f, XmlPullParser xmlPullParser) {
        long namedInt = TypedArrayUtils.getNamedInt(typedArray, xmlPullParser, "duration", 1, 300);
        long namedInt2 = TypedArrayUtils.getNamedInt(typedArray, xmlPullParser, "startOffset", 2, 0);
        int namedInt3 = TypedArrayUtils.getNamedInt(typedArray, xmlPullParser, "valueType", 7, 4);
        if (TypedArrayUtils.hasAttribute(xmlPullParser, "valueFrom") && TypedArrayUtils.hasAttribute(xmlPullParser, "valueTo")) {
            if (namedInt3 == 4) {
                namedInt3 = inferValueTypeFromValues(typedArray, 5, 6);
            }
            PropertyValuesHolder pvh = getPVH(typedArray, namedInt3, 5, 6, "");
            if (pvh != null) {
                valueAnimator.setValues(pvh);
            }
        }
        valueAnimator.setDuration(namedInt);
        valueAnimator.setStartDelay(namedInt2);
        valueAnimator.setRepeatCount(TypedArrayUtils.getNamedInt(typedArray, xmlPullParser, "repeatCount", 3, 0));
        valueAnimator.setRepeatMode(TypedArrayUtils.getNamedInt(typedArray, xmlPullParser, "repeatMode", 4, 1));
        if (typedArray2 == null) {
            return;
        }
        setupObjectAnimator(valueAnimator, typedArray2, namedInt3, f, xmlPullParser);
    }

    private static void setupObjectAnimator(ValueAnimator valueAnimator, TypedArray typedArray, int i, float f, XmlPullParser xmlPullParser) {
        ObjectAnimator objectAnimator = (ObjectAnimator) valueAnimator;
        String namedString = TypedArrayUtils.getNamedString(typedArray, xmlPullParser, "pathData", 1);
        if (namedString == null) {
            objectAnimator.setPropertyName(TypedArrayUtils.getNamedString(typedArray, xmlPullParser, "propertyName", 0));
            return;
        }
        String namedString2 = TypedArrayUtils.getNamedString(typedArray, xmlPullParser, "propertyXName", 2);
        String namedString3 = TypedArrayUtils.getNamedString(typedArray, xmlPullParser, "propertyYName", 3);
        if (i != 2) {
        }
        if (namedString2 == null && namedString3 == null) {
            throw new InflateException(typedArray.getPositionDescription() + " propertyXName or propertyYName is needed for PathData");
        }
        setupPathMotion(PathParser.createPathFromPathData(namedString), objectAnimator, 0.5f * f, namedString2, namedString3);
    }

    private static void setupPathMotion(Path path, ObjectAnimator objectAnimator, float f, String str, String str2) {
        int i;
        PathMeasure pathMeasure = new PathMeasure(path, false);
        float length = 0.0f;
        ArrayList arrayList = new ArrayList();
        arrayList.add(Float.valueOf(0.0f));
        do {
            length += pathMeasure.getLength();
            arrayList.add(Float.valueOf(length));
        } while (pathMeasure.nextContour());
        PathMeasure pathMeasure2 = new PathMeasure(path, false);
        int iMin = Math.min(100, ((int) (length / f)) + 1);
        float[] fArr = new float[iMin];
        float[] fArr2 = new float[iMin];
        float[] fArr3 = new float[2];
        int i2 = 0;
        float f2 = length / (iMin - 1);
        int i3 = 0;
        float fFloatValue = 0.0f;
        while (i3 < iMin) {
            pathMeasure2.getPosTan(fFloatValue, fArr3, null);
            pathMeasure2.getPosTan(fFloatValue, fArr3, null);
            fArr[i3] = fArr3[0];
            fArr2[i3] = fArr3[1];
            float f3 = fFloatValue + f2;
            if (i2 + 1 >= arrayList.size()) {
                fFloatValue = f3;
                i = i2;
            } else if (f3 > ((Float) arrayList.get(i2 + 1)).floatValue()) {
                fFloatValue = f3 - ((Float) arrayList.get(i2 + 1)).floatValue();
                i = i2 + 1;
                pathMeasure2.nextContour();
            } else {
                fFloatValue = f3;
                i = i2;
            }
            i3++;
            i2 = i;
        }
        PropertyValuesHolder propertyValuesHolderOfFloat = str != null ? PropertyValuesHolder.ofFloat(str, fArr) : null;
        PropertyValuesHolder propertyValuesHolderOfFloat2 = str2 != null ? PropertyValuesHolder.ofFloat(str2, fArr2) : null;
        if (propertyValuesHolderOfFloat == null) {
            objectAnimator.setValues(propertyValuesHolderOfFloat2);
        } else if (propertyValuesHolderOfFloat2 != null) {
            objectAnimator.setValues(propertyValuesHolderOfFloat, propertyValuesHolderOfFloat2);
        } else {
            objectAnimator.setValues(propertyValuesHolderOfFloat);
        }
    }
}
