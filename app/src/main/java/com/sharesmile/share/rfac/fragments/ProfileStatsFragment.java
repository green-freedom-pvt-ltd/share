package com.sharesmile.share.rfac.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sharesmile.share.MainApplication;
import com.sharesmile.share.R;
import com.sharesmile.share.core.BaseFragment;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.rfac.adapters.ProfileStatsViewAdapter;
import com.sharesmile.share.utils.SharedPrefsManager;
import com.sharesmile.share.views.CircularImageView;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ankitmaheshwari on 4/28/17.
 */

public class ProfileStatsFragment extends BaseFragment {

    private static final String TAG = "ProfileStatsFragment";

    @BindView(R.id.img_profile_stats)
    CircularImageView imageView;

    @BindView(R.id.tv_profile_name)
    TextView name;

    @BindView(R.id.tv_level_min)
    TextView levelMinDist;

    @BindView(R.id.tv_level_max)
    TextView levelMaxDist;

    @BindView(R.id.tv_level_num)
    TextView levelNum;

    @BindView(R.id.level_progress_bar)
    View levelProgressBar;

    @BindView(R.id.stats_view_pager)
    ViewPager viewPager;

    @BindView(R.id.bt_see_runs)
    View runHistoryButton;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_profile_stats, null);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init();
    }

    private void init(){
        String url = SharedPrefsManager.getInstance().getString(Constants.PREF_USER_IMAGE);
        Picasso.with(getActivity()).load(url).placeholder(R.drawable.placeholder_profile).into(imageView);
        name.setText(MainApplication.getInstance().getUserDetails().getFirstName());
        viewPager.setAdapter(new ProfileStatsViewAdapter(getChildFragmentManager()));
    }

}
