package com.sharesmile.share.home.homescreen;

import android.support.v4.view.ViewPager;
import android.view.View;

import com.sharesmile.share.core.Logger;

/**
 * Created by Shine on 30/04/16.
 */
public class ViewPagerTransformer implements ViewPager.PageTransformer {
    public static final float MIN_SCALE = 0.85f;

    private static final String TAG = "ViewPagerTransformer";

    @Override
    public void transformPage(View page, float position) {
        Logger.d(TAG, "transformPage: " + position);
        if (position < -1) {
            page.setScaleY(MIN_SCALE);
        } else if (position <= 1) {
            if (position == 0) {
                page.setScaleY(1);
            } else {
                float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position - 0.18285715f));
                page.setScaleY(scaleFactor);
            }
        } else {
            page.setScaleY(MIN_SCALE);
        }
    }
}
