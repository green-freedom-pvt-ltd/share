package com.sharesmile.share.rfac.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.sharesmile.share.rfac.fragments.OnBoardingFragment;

/**
 * Created by Shine on 25/06/16.
 */
public class OnBoardingAdapter extends FragmentStatePagerAdapter {

    public OnBoardingAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    @Override
    public Fragment getItem(int position) {
        return OnBoardingFragment.getInstance(position);
    }

    @Override
    public int getCount() {
        return 4;
    }
}
