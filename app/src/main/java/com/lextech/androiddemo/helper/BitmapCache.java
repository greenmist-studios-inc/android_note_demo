package com.lextech.androiddemo.helper;

import android.graphics.Bitmap;

/**
 * Just stores a bitmap to be used later.
 * This is a workaround to be able to share bitmap between activities for transition.
 */
public class BitmapCache {

    private Bitmap bitmap;

    public Bitmap getBitmap() {
        Bitmap b = bitmap;
        bitmap = null;
        return b;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
}
