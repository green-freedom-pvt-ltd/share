package com.sharesmile.share.rfac.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.sharesmile.share.R;
import com.sharesmile.share.core.BaseFragment;


/**
 * Created by apurvgandhwani on 3/26/2016.
 */
public class ProfileFragment extends BaseFragment {


    public static TabLayout profile_tabLayout;
    public static ViewPager profile_viewPager;
    public static int int_items = 2;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /**
         *Inflate tab_layout and setup Views.
         */
        View v = inflater.inflate(R.layout.fragment_drawer_profile, null);
        profile_tabLayout = (TabLayout) v.findViewById(R.id.profile_tabs);
        profile_viewPager = (ViewPager) v.findViewById(R.id.profile_viewpager);
        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        /**
         *Set an Apater for the View Pager
         */
        profile_viewPager.setAdapter(new MyAdapter(getChildFragmentManager()));
        profile_tabLayout.post(new Runnable() {
            @Override
            public void run() {
                profile_tabLayout.setupWithViewPager(profile_viewPager);
            }
        });

        return v;
    }

    class MyAdapter extends FragmentPagerAdapter {

        public MyAdapter(FragmentManager fm) {
            super(fm);
        }

        /**
         * Return fragment with respect to Position .
         */

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new ProfileGeneralFragment();
                case 1:
                    return new ProfileHistoryFragment();

            }
            return null;
        }

        @Override
        public int getCount() {

            return int_items;

        }

        /**
         * This method returns the title of the tab according to the position.
         */

        @Override
        public CharSequence getPageTitle(int position) {

            switch (position) {
                case 0:
                    return "GENERAL";
                case 1:
                    return "HISTORY";

            }
            return null;
        }
    }
}


