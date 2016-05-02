package com.sharesmile.share;

import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;

import com.google.gson.Gson;
import com.sharesmile.share.utils.Logger;

/**
 * Created by Shine on 30/04/16.
 */
public class ViewPagerTransformer implements ViewPager.PageTransformer {
    private static final float MIN_SCALE = 0.75f;

    @Override
    public void transformPage(View page, float position) {
        Logger.d("Anshul","Position"+" "+position);

            Log.e("pos",new Gson().toJson(position));
            if (position < -1) {
            //    float scaleFactor = Math.min(MIN_SCALE, 1 - Math.abs(p
                page.setScaleY(0.9f);
             //   page.setAlpha(1);
            } else if (position <= 1) {
                float scaleFactor = Math.max(0.9f, 1 - Math.abs(position - 0.14285715f));
              //  page.setScaleX(scaleFactor);
                Log.e("scale",new Gson().toJson(scaleFactor));
                page.setScaleY(scaleFactor);
               // page.setAlpha(scaleFactor);
            } else {
                page.setScaleY(0.9f);
                //page.setAlpha(1);
            }



    }
}
