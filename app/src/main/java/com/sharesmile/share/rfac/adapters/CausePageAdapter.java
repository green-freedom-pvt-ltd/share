package com.sharesmile.share.rfac.adapters;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.SparseArray;

import com.sharesmile.share.rfac.fragments.CauseSwipeFragment;
import com.sharesmile.share.rfac.models.CauseData;
import com.sharesmile.share.rfac.models.CauseList;

import java.util.ArrayList;

/**
 * Created by Shine on 01/05/16.
 */
public class CausePageAdapter extends FragmentStatePagerAdapter {
    SparseArray<Fragment> registeredFragments = new SparseArray<Fragment>();
    private ArrayList<CauseData> mData = new ArrayList<>();

    public CausePageAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        return CauseSwipeFragment.getInstance(mData.get(i));
    }


    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "OBJECT " + (position + 1);
    }

    public Fragment getRegisteredFragment(int position) {
        return registeredFragments.get(position);
    }


    public void addData(CauseList causesList) {
        mData.addAll(causesList.getCauses());
        notifyDataSetChanged();
    }
}
