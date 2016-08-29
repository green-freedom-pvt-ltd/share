package com.sharesmile.share;

import android.support.v4.view.ViewPager;
import android.view.View;

/**
 * Created by Shine on 30/04/16.
 */
public class ViewPagerTransformer implements ViewPager.PageTransformer {
    private static final float MIN_SCALE = 0.75f;

    @Override
    public void transformPage(View page, float position) {
        if (position < -1) {
            page.setScaleY(0.9f);
        } else if (position <= 1) {
            if (position == 0) {
                page.setScaleY(1);
            } else {
                float scaleFactor = Math.max(0.9f, 1 - Math.abs(position - 0.14285715f));
                page.setScaleY(scaleFactor);
            }
        } else {
            page.setScaleY(0.9f);
        }

    }
}
