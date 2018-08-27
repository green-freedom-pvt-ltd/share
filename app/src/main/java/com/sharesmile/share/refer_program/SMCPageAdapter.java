package com.sharesmile.share.refer_program;

import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.sharesmile.share.core.Logger;
import com.sharesmile.share.home.homescreen.CauseSwipeFragment;

/**
 * Created by Shine on 01/05/16.
 */
public class SMCPageAdapter extends FragmentStatePagerAdapter {

    private static final String TAG = "SMCPageAdapter";

    public SMCPageAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        Logger.d(TAG, "getItem #" + i);
        Fragment frag = null;
        if (i == 0) {
            frag = SMC0Fragment.getInstance();
        } else if (i == 1) {
            frag = SMC1Fragment.getInstance();
        }
        return frag;
    }

    @Override
    public int getItemPosition(Object object) {
        CauseSwipeFragment fragment = (CauseSwipeFragment) object;
        if (fragment.isCompleted()) {
            // Figure out the position of fragment
            return super.getItemPosition(object);
        } else {
            return POSITION_NONE;
        }
    }

    @Override
    public Parcelable saveState() {
        return null;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "OBJECT " + (position + 1);
    }
}

