package com.sharesmile.share.rfac.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.sharesmile.share.rfac.fragments.ProfileStatsViewFragment;

/**
 * Created by ankitmaheshwari on 5/18/17.
 */

public class ProfileStatsViewAdapter extends FragmentStatePagerAdapter {

    public ProfileStatsViewAdapter(FragmentManager manager){
        super(manager);
    }

    @Override
    public Fragment getItem(int position) {
        return ProfileStatsViewFragment.getInstance(0);
    }

    @Override
    public int getCount() {
        return 1;
    }
}
