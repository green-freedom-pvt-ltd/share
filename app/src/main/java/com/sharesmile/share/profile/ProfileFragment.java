package com.sharesmile.share.profile;

import android.graphics.Bitmap;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.sharesmile.share.core.event.UpdateEvent;
import com.sharesmile.share.core.application.MainApplication;
import com.sharesmile.share.R;
import com.sharesmile.share.analytics.events.AnalyticsEvent;
import com.sharesmile.share.analytics.events.Event;
import com.sharesmile.share.core.base.BaseFragment;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.home.settings.UnitsManager;
import com.sharesmile.share.network.NetworkUtils;
import com.sharesmile.share.core.sync.SyncHelper;
import com.sharesmile.share.core.Logger;
import com.sharesmile.share.core.ShareImageLoader;
import com.sharesmile.share.core.SharedPrefsManager;
import com.sharesmile.share.profile.history.ProfileHistoryFragment;
import com.sharesmile.share.profile.stats.BarChartDataSet;
import com.sharesmile.share.profile.stats.CustomBarChartRenderer;
import com.sharesmile.share.profile.streak.StreakFragment;
import com.sharesmile.share.utils.Utils;
import com.sharesmile.share.views.CircularImageView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import Models.Level;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.sharesmile.share.core.Constants.PREF_TOTAL_IMPACT;
import static com.sharesmile.share.core.Constants.PREF_TOTAL_RUN;
import static com.sharesmile.share.core.Constants.PREF_WORKOUT_LIFETIME_DISTANCE;
import static com.sharesmile.share.profile.stats.BarChartDataSet.TYPE_WEEKLY;

/**
 * Created by ankitmaheshwari on 4/28/17.
 */

public class ProfileFragment extends BaseFragment {

    private static final String TAG = "ProfileFragment";

    @BindView(R.id.img_profile_stats)
    CircularImageView imageView;

    @BindView(R.id.tv_profile_name)
    TextView name;

    @BindView(R.id.tv_level)
    TextView levelDist;

    @BindView(R.id.tv_level_num)
    TextView levelNum;

    @BindView(R.id.level_progress_bar)
    View levelProgressBar;

    @BindView(R.id.stats_sharable_container)
    View sharableContainer;

    @BindView(R.id.bt_see_runs)
    View runHistoryButton;

    @BindView(R.id.profile_progress_bar)
    ProgressBar progressBar;

    @BindView(R.id.ll_profile_stats)
    View layoutProfileStats;

    @BindView(R.id.tv_impact)
    TextView impactInRupees;
    private static final String POSITION = "position";

    private static final int POSITION_WEEKLY = 0;
    private static final int POSITION_ALL_TIME = 1;

    @BindView(R.id.chart)
    BarChart barChart;

    @BindView(R.id.tv_to_left)
    TextView toTheLeft;

    @BindView(R.id.tv_duration)
    TextView duration;

    @BindView(R.id.tv_to_right)
    TextView toTheRight;

    @BindView(R.id.tv_total_runs)
    TextView totalRuns;

    @BindView(R.id.tv_km_distance)
    TextView distance;

    @BindView(R.id.tv_distance_unit)
    TextView distanceUnit;

    @BindView(R.id.tv_streak)
    TextView streakValue;


    int position;

    private int totalAmountRaised;
    private int numRuns;
    private double totalDistance;
    private BarChartDataSet barChartDataSet;


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
            case R.id.item_share_profile:
                Bitmap toShare = Utils.getBitmapFromLiveView(sharableContainer);
                Utils.share(getContext(), Utils.getLocalBitmapUri(toShare, getContext()),
                        getString(R.string.share_stats));
                AnalyticsEvent.create(Event.ON_CLICK_PROFILE_SHARE)
                        .buildAndDispatch();
                return true;
            case R.id.item_edit_profile:
                getFragmentController().replaceFragment(new EditProfileFragment(), true);
                AnalyticsEvent.create(Event.ON_CLICK_EDIT_PROFILE).buildAndDispatch();
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
    public void onEvent(UpdateEvent.RunDataUpdated runDataUpdated) {
        initUi();
    }

    private void initUi() {
        // Setting Profile Picture
        boolean isWorkoutDataUpToDate =
                SharedPrefsManager.getInstance().getBoolean(Constants.PREF_IS_WORKOUT_DATA_UP_TO_DATE_IN_DB, false);
        Logger.d(TAG, "initUi: isWorkoutDataUpToDate = " + isWorkoutDataUpToDate);

        if (isWorkoutDataUpToDate) {
            hideProgressDialog();
            String url = MainApplication.getInstance().getUserDetails().getSocialThumb();
            ShareImageLoader.getInstance().loadImage(url, imageView,
                    ContextCompat.getDrawable(getContext(), R.drawable.placeholder_profile));
            // Name of user
            name.setText(MainApplication.getInstance().getUserDetails().getFullName());

            // Level and Level's progress
            int lifeTimeImpact = SharedPrefsManager.getInstance().getInt(PREF_TOTAL_IMPACT);
            Level level = Utils.getLevel(lifeTimeImpact);
            levelDist.setText(UnitsManager.formatRupeeToMyCurrency(level.getMinImpact()) + "/" + UnitsManager.formatRupeeToMyCurrency(level.getMaxImpact()));
            levelNum.setText("Level " + level.getLevel());
            float progressPercent =
                    ((float) (lifeTimeImpact - level.getMinImpact())) / (level.getMaxImpact() - level.getMinImpact());
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) levelProgressBar.getLayoutParams();
            params.weight = progressPercent;
            levelProgressBar.setLayoutParams(params);

//            viewPager.setAdapter(new ProfileStatsViewAdapter(getChildFragmentManager()));
            if (SharedPrefsManager.getInstance().getInt(Constants.PREF_TOTAL_RUN) > 0) {
                runHistoryButton.setVisibility(View.VISIBLE);
                runHistoryButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getFragmentController().replaceFragment(ProfileHistoryFragment.newInstance(false), true);
                        AnalyticsEvent.create(Event.ON_CLICK_SEE_WORKOUTS).buildAndDispatch();
                    }
                });
            } else {
                runHistoryButton.setVisibility(View.GONE);
            }
            //streak value
            streakValue.setText(MainApplication.getInstance().getUserDetails().getStreakCount()+"");

            setupToolbar();

            setUpAllTimeStats();
            int height = (int) getResources().getDimension(R.dimen.super_large_text);
            Shader textShader = new LinearGradient(0, 0, 0, height, new int[]{0xff04cbfd, 0xff33f373},
                    new float[]{0, 1}, Shader.TileMode.CLAMP);
            impactInRupees.getPaint().setShader(textShader);

            displayStats();
            setUpBarChart();
        } else if (NetworkUtils.isNetworkConnected(MainApplication.getContext())) {
            // Need to force refresh Workout Data
            Logger.e(TAG, "Must fetch historical run data before");
            SyncHelper.forceRefreshEntireWorkoutHistory();
            showProgressDialog();
        } else {
            layoutProfileStats.setVisibility(View.GONE);
            MainApplication.showToast("Please check your internet connection");
        }
    }

    private void setUpAllTimeStats(){
        toTheLeft.setVisibility(View.GONE);
        toTheRight.setVisibility(View.GONE);
        duration.setText(R.string.profile_stats_all_time);
        totalAmountRaised = SharedPrefsManager.getInstance().getInt(PREF_TOTAL_IMPACT);
        numRuns = SharedPrefsManager.getInstance().getInt(PREF_TOTAL_RUN);
        totalDistance = SharedPrefsManager.getInstance().getLong(PREF_WORKOUT_LIFETIME_DISTANCE);
        Logger.d(TAG, "Fetched All time stats from Preferences: totalAmountRaised: " + totalAmountRaised
                + ", totalDistance: " + totalDistance + ", numRuns: " + numRuns);
    }

    private void displayStats(){
        impactInRupees.setText(UnitsManager.formatRupeeToMyCurrency(totalAmountRaised));
        totalRuns.setText(numRuns + "");
        String totalDistanceString;
        double totalDistInMyUnits = UnitsManager.isImperial() ? totalDistance*0.621 : totalDistance;
        if (totalDistInMyUnits > 100){
            totalDistanceString = String.valueOf(Math.round(totalDistInMyUnits));
        } else if (totalDistInMyUnits % 1 == 0){
            totalDistanceString = String.valueOf(totalDistInMyUnits);
        } else {
            totalDistanceString = String.valueOf(Utils.formatWithOneDecimal(totalDistInMyUnits));
        }
        distance.setText(String.valueOf(totalDistanceString));
        distanceUnit.setText(UnitsManager.getDistanceLabel());
    }

    private void setUpBarChart(){
        barChartDataSet = new BarChartDataSet(TYPE_WEEKLY);

        BarDataSet dataSet = new BarDataSet(barChartDataSet.getBarEntries(), "Stats");
        dataSet.setColor(ContextCompat.getColor(getContext(), R.color.bright_sky_blue));
        dataSet.setValueTextColor(ContextCompat.getColor(getContext(), R.color.greyish_brown));

        IValueFormatter intValueFormatter = new IValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex,
                                            ViewPortHandler viewPortHandler) {
                int val = Math.round(value);
                if (val > 0){
                    return UnitsManager.formatRupeeToMyCurrency(val);
                }else {
                    return "";
                }
            }
        };

        dataSet.setValueFormatter(intValueFormatter);
        BarData data = new BarData(dataSet);
        barChart.setData(data);
        barChart.setDrawGridBackground(false);


        barChart.getAxisRight().setEnabled(false);
        barChart.getAxisRight().setDrawGridLines(false);
        Description desc = new Description();
        desc.setText("");
        barChart.setDescription(desc);
        barChart.getLegend().setEnabled(false);

        YAxis yAxis = barChart.getAxisLeft();

        yAxis.setSpaceBottom(4);
        yAxis.setLabelCount(3, true);

        yAxis.setAxisMinimum(0);
        yAxis.setGridColor(ContextCompat.getColor(getContext(), R.color.black_5));
        yAxis.setDrawAxisLine(false);
        yAxis.setGridLineWidth(1f);

        yAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                if (value > 10){
                    return UnitsManager.formatRupeeToMyCurrency(value);
                }
                else if (value > 0){
                    // Earlier only one decimal was being shown
                    return UnitsManager.formatRupeeToMyCurrency(value);
                }else {
                    return "";
                }
            }
        });

        XAxis xAxis = barChart.getXAxis();

        xAxis.setDrawGridLines(false);
        xAxis.setAxisLineColor(ContextCompat.getColor(getContext(), R.color.black_5));
        xAxis.setAxisLineWidth(1.0f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        IAxisValueFormatter valueFormatter = new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                int index = (int) value;
                return barChartDataSet.getLabelForIndex(index);
            }
        } ;
        xAxis.setValueFormatter(valueFormatter);
        barChart.setDrawBorders(false);
        barChart.setRenderer(new CustomBarChartRenderer(barChart, barChart.getAnimator(),
                barChart.getViewPortHandler()));
        barChart.invalidate();
        Handler handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                float width = barChart.getWidth();
                float scale = ((width*31)/7)/width;
                barChart.zoom(scale,1,barChart.getWidth()*scale,0);
                barChart.setScaleEnabled(false);
            }
        };
        handler.postDelayed(null,100);
//        barChart.setTouchEnabled(false);
//        barChart.getData().setHighlightEnabled(false);
//        barChart.setPinchZoom(false);
//        barChart.setDoubleTapToZoomEnabled(false);
    }
    private void setupToolbar() {
        setHasOptionsMenu(true);
        setToolbarTitle(getResources().getString(R.string.profile));
    }

    @OnClick(R.id.tv_streak)
    void streakClick()
    {
        getFragmentController().replaceFragment(StreakFragment.newInstance(Constants.FROM_PROFILE_FOR_STREAK), true);
    }
}
