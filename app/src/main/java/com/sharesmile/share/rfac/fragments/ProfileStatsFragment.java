package com.sharesmile.share.rfac.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sharesmile.share.MainApplication;
import com.sharesmile.share.R;
import com.sharesmile.share.core.BaseFragment;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.rfac.adapters.ProfileStatsViewAdapter;
import com.sharesmile.share.utils.SharedPrefsManager;
import com.sharesmile.share.utils.Utils;
import com.sharesmile.share.views.CircularImageView;
import com.squareup.picasso.Picasso;

import Models.Level;
import butterknife.BindView;
import butterknife.ButterKnife;

import static com.sharesmile.share.R.id.bt_see_runs;
import static com.sharesmile.share.core.Constants.PREF_WORKOUT_LIFETIME_DISTANCE;

/**
 * Created by ankitmaheshwari on 4/28/17.
 */

public class ProfileStatsFragment extends BaseFragment implements View.OnClickListener{

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

    @BindView(bt_see_runs)
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
        initUi();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_profile, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_edit_profile:
                //TODO: Open Edit Profile Fragment
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initUi(){
        // Setting Profile Picture
        String url = SharedPrefsManager.getInstance().getString(Constants.PREF_USER_IMAGE);
        Picasso.with(getActivity()).load(url).placeholder(R.drawable.placeholder_profile).into(imageView);

        // Name of user
        name.setText(MainApplication.getInstance().getUserDetails().getFirstName());

        // Level and Level's progress
        long lifetimeDistance = SharedPrefsManager.getInstance().getLong(PREF_WORKOUT_LIFETIME_DISTANCE);
        Level level = Utils.getLevel(Float.parseFloat(lifetimeDistance+""));
        levelMinDist.setText(level.getMinKm() + "km");
        levelMaxDist.setText(level.getMaxKm() + "km");
        levelNum.setText("Level " + level.getLevel());
        float progressPercent = ((float)(lifetimeDistance - level.getMinKm())) / (level.getMaxKm() - level.getMinKm());
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) levelProgressBar.getLayoutParams();
        params.weight = progressPercent;
        levelProgressBar.setLayoutParams(params);

        viewPager.setAdapter(new ProfileStatsViewAdapter(getChildFragmentManager()));

        runHistoryButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.bt_see_runs){
            //TODO: Show Run History Screen
        }
    }
}
