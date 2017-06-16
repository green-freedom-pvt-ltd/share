package com.sharesmile.share.rfac.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sharesmile.share.Events.DBEvent;
import com.sharesmile.share.MainApplication;
import com.sharesmile.share.R;
import com.sharesmile.share.core.BaseFragment;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.network.NetworkUtils;
import com.sharesmile.share.rfac.adapters.ProfileStatsViewAdapter;
import com.sharesmile.share.sync.SyncHelper;
import com.sharesmile.share.utils.Logger;
import com.sharesmile.share.utils.ShareImageLoader;
import com.sharesmile.share.utils.SharedPrefsManager;
import com.sharesmile.share.utils.Utils;
import com.sharesmile.share.views.CircularImageView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import Models.Level;
import butterknife.BindView;
import butterknife.ButterKnife;

import static com.sharesmile.share.core.Constants.PREF_WORKOUT_LIFETIME_DISTANCE;

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

    @BindView(R.id.profile_progress_bar)
    ProgressBar progressBar;

    @BindView(R.id.ll_profile_stats)
    View layoutProfileStats;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_profile_stats, null);
        ButterKnife.bind(this, v);
        EventBus.getDefault().register(this);
        return v;
    }

    @Override
    public void onDestroyView() {
        EventBus.getDefault().unregister(this);
        super.onDestroyView();
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
                getFragmentController().replaceFragment(new ProfileGeneralFragment(), true);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showProgressDialog() {
        progressBar.setVisibility(View.VISIBLE);
        layoutProfileStats.setVisibility(View.GONE);
    }

    private void hideProgressDialog() {
        progressBar.setVisibility(View.GONE);
        layoutProfileStats.setVisibility(View.VISIBLE);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(DBEvent.RunDataUpdated runDataUpdated) {
        initUi();
    }

    private void initUi(){
        // Setting Profile Picture
        boolean isWorkoutDataUpToDate =
                SharedPrefsManager.getInstance().getBoolean(Constants.PREF_IS_WORKOUT_DATA_UP_TO_DATE_IN_DB, false);

        if (isWorkoutDataUpToDate){
            hideProgressDialog();
            String url = MainApplication.getInstance().getUserDetails().getSocialThumb();
            ShareImageLoader.getInstance().loadImage(url, imageView,
                    ContextCompat.getDrawable(getContext(), R.drawable.placeholder_profile));
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
            if (SharedPrefsManager.getInstance().getInt(Constants.PREF_TOTAL_RUN) > 0){
                runHistoryButton.setVisibility(View.VISIBLE);
                runHistoryButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getFragmentController().replaceFragment(new ProfileHistoryFragment(), true);
                    }
                });
            }else {
                runHistoryButton.setVisibility(View.GONE);
            }
            setupToolbar();
        }else if (NetworkUtils.isNetworkConnected(MainApplication.getContext())){
            // Need to force refresh Workout Data
            Logger.e(TAG, "Must fetch historical run data before");
            SyncHelper.forceRefreshEntireWorkoutHistory();
            showProgressDialog();
        }else {
            layoutProfileStats.setVisibility(View.GONE);
            MainApplication.showToast("Please check your internet connection");
        }
    }

    private void setupToolbar() {
        setHasOptionsMenu(true);
        setToolbarTitle(getResources().getString(R.string.profile));
    }
}
