package com.sharesmile.share.rfac.adapters;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.sharesmile.share.core.Logger;

/**
 * Created by ankitmaheshwari on 9/3/17.
 */

public abstract class BannerPagerAdapter extends PagerAdapter {

    private static final String TAG = "BannerPagerAdapter";

    abstract int getNumPages();

    protected abstract View getItemView(int position, ViewGroup container);

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public int getCount() {
        return getNumPages();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Logger.d(TAG, "instantiateItem: " + position);
        View carouselItem = getItemView(position, container);
        container.addView(carouselItem);
        return carouselItem;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }
}
