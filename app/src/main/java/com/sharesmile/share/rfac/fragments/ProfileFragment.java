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
import android.widget.ImageView;
import android.widget.TextView;

import com.sharesmile.share.MainApplication;
import com.sharesmile.share.R;
import com.sharesmile.share.WorkoutDao;
import com.sharesmile.share.core.BaseFragment;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.utils.SharedPrefsManager;
import com.sharesmile.share.views.CircularImageView;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Created by apurvgandhwani on 3/26/2016.
 */
public class ProfileFragment extends BaseFragment {


    public static TabLayout profile_tabLayout;
    public static ViewPager profile_viewPager;
    public static int int_items = 2;

    @BindView(R.id.img_profile)
    CircularImageView mProfileImage;

    @BindView(R.id.tv_profile_name)
    TextView mName;

    @BindView(R.id.impact_summary)
    TextView mImpactSummary;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /**
         *Inflate tab_layout and setup Views.
         */
        View v = inflater.inflate(R.layout.fragment_drawer_profile, null);
        ButterKnife.bind(this, v);
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
        getFragmentController().updateToolBar(getString(R.string.profile), true);
        displayUserInfo();
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        boolean hasRun = SharedPrefsManager.getInstance().getBoolean(Constants.PREF_HAS_RUN, false);
        if (hasRun) {
            profile_viewPager.setCurrentItem(1);
            int totalRun = SharedPrefsManager.getInstance().getInt(Constants.PREF_TOTAL_RUN, 0);
            int totalImpact = SharedPrefsManager.getInstance().getInt(Constants.PREF_TOTAL_IMPACT, 0);
            mImpactSummary.setText(getString(R.string.impact_summary, totalImpact, totalRun));
        } else {
            mImpactSummary.setText(getString(R.string.begin_impact));
        }

    }

    private void displayUserInfo() {
        SharedPrefsManager prefsManager = SharedPrefsManager.getInstance();
        String url = prefsManager.getString(Constants.PREF_USER_IMAGE);
        Picasso.with(getActivity()).load(url).placeholder(R.drawable.placeholder_profile).into(mProfileImage);
        String name = prefsManager.getString(Constants.PREF_USER_NAME);
        mName.setText(name);
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


