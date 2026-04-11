package com.lumiyaviewer.lumiya.res.text;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

/* JADX INFO: loaded from: classes.dex */
public class DrawableTextBitmap {
    private final float baselineOffset;
    private final Bitmap bitmap;

    DrawableTextBitmap(DrawableTextParams drawableTextParams, int i) {
        Paint paint = new Paint();
        Rect rect = new Rect();
        paint.setTextSize(i);
        paint.setAntiAlias(true);
        paint.setTextAlign(Paint.Align.CENTER);
        String[] strArrSplit = drawableTextParams.text().split("\n");
        int length = strArrSplit.length;
        int i2 = 0;
        int iWidth = 1;
        while (i2 < length) {
            String str = strArrSplit[i2];
            paint.getTextBounds(str, 0, str.length(), rect);
            i2++;
            iWidth = rect.width() > iWidth ? rect.width() : iWidth;
        }
        float fDescent = paint.descent() - paint.ascent();
        int iRound = Math.round((strArrSplit.length * fDescent) + 1.0f);
        int i3 = drawableTextParams.backgroundColor() != 0 ? i : 0;
        int i4 = drawableTextParams.backgroundColor() != 0 ? i / 2 : 0;
        int i5 = 1;
        while (i5 < iWidth + i3 && i5 < 512) {
            i5 <<= 1;
        }
        int i6 = 1;
        while (i6 < iRound + i4 && i6 < 256) {
            i6 <<= 1;
        }
        int iMax = Math.max(i5, i6);
        this.bitmap = Bitmap.createBitmap(iMax, iMax, drawableTextParams.backgroundColor() == 0 ? Bitmap.Config.ALPHA_8 : Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(this.bitmap);
        if (drawableTextParams.backgroundColor() != 0) {
            paint.setColor(drawableTextParams.backgroundColor());
            int i7 = iWidth + i3;
            int i8 = iRound + i4;
            canvas.drawRect((iMax - i7) / 2, (iMax - i8) / 2, iMax - ((iMax - i7) / 2), iMax - ((iMax - i8) / 2), paint);
        }
        paint.setARGB(255, 255, 255, 255);
        int iDescent = (iMax - iRound) / 2;
        for (String str2 : strArrSplit) {
            canvas.drawText(str2, iMax / 2, iDescent - paint.ascent(), paint);
            iDescent = (int) (iDescent + (paint.descent() - paint.ascent()));
        }
        this.baselineOffset = (iRound + fDescent) / iMax;
    }

    public float getBaselineOffset() {
        return this.baselineOffset;
    }

    public Bitmap getBitmap() {
        return this.bitmap;
    }
}
