package com.sharesmile.share.rfac.adapters;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by ankitmaheshwari on 9/3/17.
 */

public abstract class BannerPagerAdapter extends PagerAdapter {

    abstract int getNumPages();

    protected abstract View getItemView(int position, ViewGroup container);

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
        View carouselItem = getItemView(position, container);
        container.addView(carouselItem);
        return carouselItem;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }
}
