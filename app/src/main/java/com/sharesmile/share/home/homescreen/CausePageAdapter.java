package com.sharesmile.share.home.homescreen;

import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.sharesmile.share.core.Logger;
import com.sharesmile.share.core.cause.model.CauseData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Shine on 01/05/16.
 */
public class CausePageAdapter extends FragmentStatePagerAdapter {

    private static final String TAG = "CausePageAdapter";

    private ArrayList<CauseData> mData = new ArrayList<>();

    public CausePageAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        Logger.d(TAG, "getItem #" + i);
        CauseData causeData = mData.get(i);

        Fragment frag;
        if (causeData.getId() != -1)
        {
         frag = CauseSwipeFragment.getInstance(causeData);
        }else
        {
            frag = SMCSwipeFragment.getInstance();
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
        return mData.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "OBJECT " + (position + 1);
    }


    public void setData(List<CauseData> causes) {
        Logger.d(TAG, "setData");
        mData.clear();
        mData.addAll(causes);
        notifyDataSetChanged();

    }

    public CauseData getItemAtPosition(int position) {
        return mData.get(position);
    }
}
