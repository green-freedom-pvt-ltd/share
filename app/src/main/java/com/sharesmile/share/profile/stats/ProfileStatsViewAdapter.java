package com.sharesmile.share.profile.stats;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by ankitmaheshwari on 5/18/17.
 */

public class ProfileStatsViewAdapter extends FragmentPagerAdapter {

    public ProfileStatsViewAdapter(FragmentManager manager){
        super(manager);
    }

    @Override
    public Fragment getItem(int position) {
        return ProfileStatsViewFragment.getInstance(position);
    }

    @Override
    public int getCount() {
        return 1;
    }
}
