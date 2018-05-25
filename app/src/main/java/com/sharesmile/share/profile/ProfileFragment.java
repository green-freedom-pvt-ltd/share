package com.sharesmile.share.profile;

import android.graphics.Bitmap;
import android.graphics.LinearGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.sharesmile.share.AchievedBadge;
import com.sharesmile.share.AchievedBadgeDao;
import com.sharesmile.share.R;
import com.sharesmile.share.analytics.events.AnalyticsEvent;
import com.sharesmile.share.analytics.events.Event;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.core.Logger;
import com.sharesmile.share.core.ShareImageLoader;
import com.sharesmile.share.core.SharedPrefsManager;
import com.sharesmile.share.core.application.MainApplication;
import com.sharesmile.share.core.base.BaseFragment;
import com.sharesmile.share.core.event.UpdateEvent;
import com.sharesmile.share.core.sync.SyncHelper;
import com.sharesmile.share.home.homescreen.OnboardingOverlay;
import com.sharesmile.share.home.settings.UnitsManager;
import com.sharesmile.share.network.NetworkUtils;
import com.sharesmile.share.profile.badges.AchievedBadgeProgressFragment;
import com.sharesmile.share.profile.badges.adapter.AchievementsAdapter;
import com.sharesmile.share.profile.history.ProfileHistoryFragment;
import com.sharesmile.share.profile.stats.BarChartDataSet;
import com.sharesmile.share.profile.stats.BarChartEntry;
import com.sharesmile.share.profile.streak.StreakFragment;
import com.sharesmile.share.utils.Utils;
import com.sharesmile.share.views.CircularImageView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import Models.Level;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;

import static com.sharesmile.share.core.Constants.NAVIGATION_DRAWER;
import static com.sharesmile.share.core.Constants.PREF_TOTAL_IMPACT;
import static com.sharesmile.share.core.Constants.PREF_TOTAL_RUN;
import static com.sharesmile.share.core.Constants.PREF_WORKOUT_LIFETIME_DISTANCE;
import static com.sharesmile.share.core.Constants.PROFILE_SCREEN;

/**
 * Created by ankitmaheshwari on 4/28/17.
 */

public class ProfileFragment extends BaseFragment {

    private static final String TAG = "ProfileFragment";

    @BindView(R.id.img_profile_stats)
    CircularImageView imageView;

    @BindView(R.id.tv_profile_name)
    TextView name;

    @BindView(R.id.img_profile_stats_2)
    CircularImageView imageView2;

    @BindView(R.id.tv_profile_name_2)
    TextView name2;

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

    @BindView(R.id.chart_daily)
    BarChart barChartDaily;
    @BindView(R.id.chart_weekly)
    BarChart barChartWeekly;
    @BindView(R.id.chart_monthly)
    BarChart barChartMonthly;

    @BindView(R.id.tv_streak)
    TextView streakValue;

    @BindView(R.id.tv_stats_impact)
    TextView statsImpact;

    @BindView(R.id.tv_stats_kms)
    TextView statsKms;

    @BindView(R.id.tv_stats_workout)
    TextView statsWorkout;

    @BindView(R.id.stats_layout)
    LinearLayout stats_layout;

    @BindView(R.id.tv_my_stats_daily)
    TextView myStatsDaily;

    @BindView(R.id.tv_my_stats_weekly)
    TextView myStatsWeekly;

    @BindView(R.id.tv_my_stats_monthly)
    TextView myStatsMonthly;

    @BindView(R.id.progress_bar_stats_graph)
    ProgressBar progressBarStatsGraph;

    @BindView(R.id.workout_layout)
    NestedScrollView workoutLayout;

    @BindView(R.id.no_workout_layout)
    LinearLayout noWorkoutLayout;
    @BindView(R.id.btn_lets_run)
    LinearLayout letsGo;
    @BindView(R.id.overlay_layout)
    LinearLayout overlayLayout;

    @BindView(R.id.rv_achievements)
    RecyclerView achievementsRecylerView;

    @BindView(R.id.rv_charity_overview)
    RecyclerView charityOverviewRecyclerView;

    AchievementsAdapter achievementsAdapter;

    Rect scrollBounds;

    private int totalAmountRaised;
    private int numRuns;
    private double totalDistance;
    private BarChartDataSet barChartDataSetDaily;
    private BarChartDataSet barChartDataSetWeekly;
    private BarChartDataSet barChartDataSetMonthly;
    private SetUpBarChartAsync setUpBarChartAsync;
    public MaterialTapTargetPrompt materialTapTargetPrompt;

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
        long workoutCount = MainApplication.getInstance().getUsersWorkoutCount();
        if(workoutCount>0)
        {
            incrementProfileScreenVisitCount();
        }
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
                if(SharedPrefsManager.getInstance().getInt(PREF_TOTAL_IMPACT)>0) {
                    Bitmap toShare = Utils.getBitmapFromLiveView(sharableContainer);
                    Utils.share(getContext(), Utils.getLocalBitmapUri(toShare, getContext()),
                            getString(R.string.share_stats));
                    AnalyticsEvent.create(Event.ON_CLICK_PROFILE_SHARE)
                            .buildAndDispatch();
                }else
                {
                    MainApplication.showToast(getResources().getString(R.string.no_workout_profile_txt_toast));
                }
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

            setupToolbar();

            // Level and Level's progress
            int lifeTimeImpact = SharedPrefsManager.getInstance().getInt(PREF_TOTAL_IMPACT);

            if(lifeTimeImpact==0)
            {
                workoutLayout.setVisibility(View.GONE);
                noWorkoutLayout.setVisibility(View.VISIBLE);
                ShareImageLoader.getInstance().loadImage(url, imageView2,
                        ContextCompat.getDrawable(getContext(), R.drawable.placeholder_profile));
                // Name of user
                name2.setText(MainApplication.getInstance().getUserDetails().getFullName());
            }else {

                workoutLayout.setVisibility(View.VISIBLE);
                noWorkoutLayout.setVisibility(View.GONE);
                ShareImageLoader.getInstance().loadImage(url, imageView,
                        ContextCompat.getDrawable(getContext(), R.drawable.placeholder_profile));
                // Name of user
                name.setText(MainApplication.getInstance().getUserDetails().getFullName());
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
                streakValue.setText(MainApplication.getInstance().getUserDetails().getStreakCount() + "");
                setUpAllTimeStats();
                int height = (int) getResources().getDimension(R.dimen.super_large_text);
                Shader textShader = new LinearGradient(0, 0, 0, height, new int[]{0xff04cbfd, 0xff33f373},
                        new float[]{0, 1}, Shader.TileMode.CLAMP);
                impactInRupees.getPaint().setShader(textShader);

                displayStats();
                configBarChart();
                setUpBarChartAsync = new SetUpBarChartAsync();
                setUpBarChartAsync.execute();
                prepareStreakOnboardingOverlays();
            }
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false);
            achievementsRecylerView.setLayoutManager(linearLayoutManager);
            List<AchievedBadge> achievedBadges =MainApplication.getInstance().getDbWrapper().getAchievedBadgeDao().queryBuilder()
                    .where(AchievedBadgeDao.Properties.BadgeIdAchieved.notEq(-1)).list();
            if(achievedBadges!=null && achievedBadges.size()>0) {
                achievementsAdapter = new AchievementsAdapter(achievedBadges);
                achievementsRecylerView.setAdapter(achievementsAdapter);
            }
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
    @OnClick(R.id.btn_lets_run)
    void letsGo()
    {
        goBack();
    }

    @OnClick({R.id.tv_my_stats_daily, R.id.tv_my_stats_weekly, R.id.tv_my_stats_monthly})
    void onMyStatsClick(View view) {
        int type;
        String stats = "";

        switch (view.getId()) {
            case R.id.tv_my_stats_daily:
                type = BarChartDataSet.TYPE_DAILY;
                stats = "type_daily";
                break;
            case R.id.tv_my_stats_weekly:
                type = BarChartDataSet.TYPE_WEEKLY;
                stats = "type_weekly";
                break;
            case R.id.tv_my_stats_monthly:
                type = BarChartDataSet.TYPE_MONTHLY;
                stats = "type_monthly";
                break;
            default:
                type = BarChartDataSet.TYPE_DAILY;
        }
        AnalyticsEvent.create(Event.ON_SET_BIRTTHDAY)
                .put("stats_type", stats)
                .buildAndDispatch();
        if (setUpBarChartAsync.getStatus() != AsyncTask.Status.RUNNING) {
            showChart(type);
        } else {
            setUpBarChartAsync.setType(type);
            setBG(type);
        }
    }

    private void showChart(int type) {
        setBG(type);
        barChartDaily.setVisibility(View.INVISIBLE);
        barChartWeekly.setVisibility(View.INVISIBLE);
        barChartMonthly.setVisibility(View.INVISIBLE);
        try {
            switch (type) {
                case BarChartDataSet.TYPE_DAILY:
                    barChartDaily.setVisibility(View.VISIBLE);
                    barChartDaily.highlightValue(barChartDataSetDaily.getBarEntries().size() - 1, 0);
                    break;
                case BarChartDataSet.TYPE_WEEKLY:
                    barChartWeekly.setVisibility(View.VISIBLE);
                    barChartWeekly.highlightValue(barChartDataSetWeekly.getBarEntries().size() - 1, 0);
                    break;
                case BarChartDataSet.TYPE_MONTHLY:
                    barChartMonthly.setVisibility(View.VISIBLE);
                    barChartMonthly.highlightValue(barChartDataSetMonthly.getBarEntries().size() - 1, 0);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        scrollBounds = new Rect();
        workoutLayout.getHitRect(scrollBounds);
        workoutLayout.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if(runHistoryButton!=null)
            {
                if(runHistoryButton.getLocalVisibleRect(scrollBounds))
                {
                    int sh = scrollBounds.height();
                    int bh = runHistoryButton.getHeight();
                    if(!runHistoryButton.getLocalVisibleRect(scrollBounds) ||
                            sh < bh)
                    {
                        Logger.d(TAG,"PARTIALLY VISIBLE");
                    }else
                    {
                        Logger.d(TAG,"VISIBLE");
                        prepareStatsOnboardingOverlays();
                    }
                }else
                {
                    Logger.d(TAG,"NOT VISIBLE");
                }
            }
        });
    }

    private void setUpAllTimeStats() {
        totalAmountRaised = SharedPrefsManager.getInstance().getInt(PREF_TOTAL_IMPACT);
        numRuns = SharedPrefsManager.getInstance().getInt(PREF_TOTAL_RUN);
        totalDistance = SharedPrefsManager.getInstance().getLong(PREF_WORKOUT_LIFETIME_DISTANCE);
        Logger.d(TAG, "Fetched All time stats from Preferences: totalAmountRaised: " + totalAmountRaised
                + ", totalDistance: " + totalDistance + ", numRuns: " + numRuns);
    }

    private void displayStats() {
        impactInRupees.setText(UnitsManager.formatRupeeToMyCurrency(totalAmountRaised));

    }

    class SetUpBarChartAsync extends AsyncTask<Void, Void, Void> {

        int type = BarChartDataSet.TYPE_DAILY;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBarStatsGraph.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... voids) {

            barChartDataSetDaily = new BarChartDataSet(BarChartDataSet.TYPE_DAILY);
            barChartDataSetWeekly = new BarChartDataSet(BarChartDataSet.TYPE_WEEKLY);
            barChartDataSetMonthly = new BarChartDataSet(BarChartDataSet.TYPE_MONTHLY);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (isVisible()) {
                BarDataSet dataSet;
                dataSet = new BarDataSet(barChartDataSetDaily.getBarEntries(), "Stats");
                dataSet.setColor(ContextCompat.getColor(getContext(), R.color.bright_sky_blue));
                dataSet.setValueTextColor(ContextCompat.getColor(getContext(), R.color.greyish_brown));
                IValueFormatter intValueFormatter = new IValueFormatter() {
                    @Override
                    public String getFormattedValue(float value, Entry entry, int dataSetIndex,
                                                    ViewPortHandler viewPortHandler) {
                        int val = Math.round(value);
                        if (val > 0) {
                            return UnitsManager.formatRupeeToMyCurrency(val);
                        } else {
                            return "";
                        }
                    }
                };

                dataSet.setValueFormatter(intValueFormatter);
                BarData dataDaily = new BarData(dataSet);

                barChartDaily.setData(dataDaily);
                barChartDaily.post(new Runnable() {
                    @Override
                    public void run() {
                        int width = layoutProfileStats.getWidth();
                        int size = barChartDataSetDaily.getNoOfValuesInXAxis() >= 7 ? 7 : (int) barChartDataSetDaily.getNoOfValuesInXAxis();
                        long scale = barChartDataSetDaily.getNoOfValuesInXAxis() / size;
//                        barChartDaily.fitScreen();
                        barChartDaily.zoom(scale, 1, width * scale, 0);
//                        barChartDaily.highlightValue(barChartDataSetDaily.getBarEntries().size() - 1, 0);
                        barChartDaily.invalidate();
                    }
                });
                BarDataSet dataSetWeekly;
                dataSetWeekly = new BarDataSet(barChartDataSetWeekly.getBarEntries(), "Stats");
                dataSetWeekly.setColor(ContextCompat.getColor(getContext(), R.color.bright_sky_blue));
                dataSetWeekly.setValueTextColor(ContextCompat.getColor(getContext(), R.color.greyish_brown));
                dataSetWeekly.setValueFormatter(intValueFormatter);
                BarData dataWeekly = new BarData(dataSetWeekly);
                barChartWeekly.setData(dataWeekly);
                barChartWeekly.post(new Runnable() {
                    @Override
                    public void run() {
                        int width = layoutProfileStats.getWidth();
                        float size = barChartDataSetWeekly.getNoOfValuesInXAxis() >= 7 ? 7 : (int) barChartDataSetWeekly.getNoOfValuesInXAxis();
                        float scale = (barChartDataSetWeekly.getNoOfValuesInXAxis()) / size;
//                        barChartWeekly.fitScreen();
                        barChartWeekly.zoom(scale, 1, width * scale, 0);
//                        barChartWeekly.highlightValue(barChartDataSetWeekly.getBarEntries().size() - 1, 0);
                        barChartWeekly.invalidate();
                    }
                });

                BarDataSet dataSetMonthly;
                dataSetMonthly = new BarDataSet(barChartDataSetMonthly.getBarEntries(), "Stats");
                dataSetMonthly.setColor(ContextCompat.getColor(getContext(), R.color.bright_sky_blue));
                dataSetMonthly.setValueTextColor(ContextCompat.getColor(getContext(), R.color.greyish_brown));
                dataSetMonthly.setValueFormatter(intValueFormatter);
                BarData dataMonthly = new BarData(dataSetMonthly);

                barChartMonthly.setData(dataMonthly);
                barChartMonthly.post(new Runnable() {
                    @Override
                    public void run() {
                        int width = layoutProfileStats.getWidth();
                        int size = barChartDataSetMonthly.getNoOfValuesInXAxis() >= 7 ? 7 : (int) barChartDataSetMonthly.getNoOfValuesInXAxis();
                        long scale = barChartDataSetMonthly.getNoOfValuesInXAxis() / size;
//                        barChartMonthly.fitScreen();
                        barChartMonthly.zoom(scale, 1, width * scale, 0);
//                        barChartMonthly.highlightValue(barChartDataSetMonthly.getBarEntries().size() - 1, 0);
                        barChartMonthly.invalidate();
                    }
                });
                showChart(type);
                progressBarStatsGraph.setVisibility(View.GONE);
            }
        }

        private void setType(int type) {
            this.type = type;
        }
    }

    private void configBarChart() {
        //daily
        barChartDaily.setDrawGridBackground(false);
        barChartDaily.getAxisRight().setEnabled(false);
        barChartDaily.getAxisRight().setDrawGridLines(false);
        Description descDaily = new Description();
        descDaily.setText("");
        barChartDaily.setDescription(descDaily);
        barChartDaily.getLegend().setEnabled(false);

        YAxis yAxisDaily = barChartDaily.getAxisLeft();

        yAxisDaily.setSpaceBottom(4);
        yAxisDaily.setLabelCount(3, true);

        yAxisDaily.setAxisMinimum(0);
        yAxisDaily.setGridColor(ContextCompat.getColor(getContext(), R.color.black_5));
        yAxisDaily.setDrawAxisLine(false);
        yAxisDaily.setGridLineWidth(1f);

        yAxisDaily.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                if (value > 10) {
                    return UnitsManager.formatRupeeToMyCurrency(value);
                } else if (value > 0) {
                    // Earlier only one decimal was being shown
                    return UnitsManager.formatRupeeToMyCurrency(value);
                } else {
                    return "";
                }
            }
        });

        XAxis xAxisDaily = barChartDaily.getXAxis();

        xAxisDaily.setDrawGridLines(false);
        xAxisDaily.setAxisLineColor(ContextCompat.getColor(getContext(), R.color.black_5));
        xAxisDaily.setAxisLineWidth(1.0f);
        xAxisDaily.setGranularity(1f);
        xAxisDaily.setPosition(XAxis.XAxisPosition.BOTTOM);
        IAxisValueFormatter valueFormatterDaily = new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                int index = (int) value;
                return barChartDataSetDaily.getLabelForIndex(index)/*value+""*/;
            }
        };
        xAxisDaily.setValueFormatter(valueFormatterDaily);
        barChartDaily.setDrawBorders(false);
        barChartDaily.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry entry, Highlight highlight) {
                statsImpact.setText(getContext().getString(R.string.rupee_symbol) + " " + ((int) entry.getY()));
                BarChartEntry barChartEntry = barChartDataSetDaily.getBarChartEntry((int) entry.getX());
                statsWorkout.setText(barChartEntry.getCount() + "");
                statsKms.setText(Utils.formatToKmsWithTwoDecimal((float) (barChartEntry.getDistance() * 1000)) + "");
            }

            @Override
            public void onNothingSelected() {

            }
        });
        barChartDaily.setPinchZoom(false);
        barChartDaily.setDoubleTapToZoomEnabled(false);
        barChartDaily.setScaleEnabled(false);
        //weekly
        barChartWeekly.setDrawGridBackground(false);
        barChartWeekly.getAxisRight().setEnabled(false);
        barChartWeekly.getAxisRight().setDrawGridLines(false);
        Description descWeekly = new Description();
        descWeekly.setText("");
        barChartWeekly.setDescription(descWeekly);
        barChartWeekly.getLegend().setEnabled(false);

        YAxis yAxisWeekly = barChartWeekly.getAxisLeft();

        yAxisWeekly.setSpaceBottom(4);
        yAxisWeekly.setLabelCount(3, true);

        yAxisWeekly.setAxisMinimum(0);
        yAxisWeekly.setGridColor(ContextCompat.getColor(getContext(), R.color.black_5));
        yAxisWeekly.setDrawAxisLine(false);
        yAxisWeekly.setGridLineWidth(1f);

        yAxisWeekly.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                if (value > 10) {
                    return UnitsManager.formatRupeeToMyCurrency(value);
                } else if (value > 0) {
                    // Earlier only one decimal was being shown
                    return UnitsManager.formatRupeeToMyCurrency(value);
                } else {
                    return "";
                }
            }
        });

        XAxis xAxisWeekly = barChartWeekly.getXAxis();

        xAxisWeekly.setDrawGridLines(false);
        xAxisWeekly.setAxisLineColor(ContextCompat.getColor(getContext(), R.color.black_5));
        xAxisWeekly.setAxisLineWidth(1.0f);
        xAxisWeekly.setGranularity(1f);
        xAxisWeekly.setPosition(XAxis.XAxisPosition.BOTTOM);
        IAxisValueFormatter valueFormatterWeekly = new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                int index = (int) value;
                return barChartDataSetWeekly.getLabelForIndex(index)/*value+""*/;
            }
        };
        xAxisWeekly.setValueFormatter(valueFormatterWeekly);
        barChartWeekly.setDrawBorders(false);
        barChartWeekly.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry entry, Highlight highlight) {
                statsImpact.setText(getContext().getString(R.string.rupee_symbol) + " " + ((int) entry.getY()));
                BarChartEntry barChartEntry = barChartDataSetWeekly.getBarChartEntry((int) entry.getX());
                statsWorkout.setText(barChartEntry.getCount() + "");
                statsKms.setText(Utils.formatToKmsWithTwoDecimal((float) (barChartEntry.getDistance() * 1000)) + "");
            }

            @Override
            public void onNothingSelected() {

            }
        });
        barChartWeekly.setPinchZoom(false);
        barChartWeekly.setDoubleTapToZoomEnabled(false);
        barChartWeekly.setScaleEnabled(false);
        //monthly
        barChartMonthly.setDrawGridBackground(false);
        barChartMonthly.getAxisRight().setEnabled(false);
        barChartMonthly.getAxisRight().setDrawGridLines(false);
        Description desc = new Description();
        desc.setText("");
        barChartMonthly.setDescription(desc);
        barChartMonthly.getLegend().setEnabled(false);

        YAxis yAxisMonthly = barChartMonthly.getAxisLeft();

        yAxisMonthly.setSpaceBottom(4);
        yAxisMonthly.setLabelCount(3, true);

        yAxisMonthly.setAxisMinimum(0);
        yAxisMonthly.setGridColor(ContextCompat.getColor(getContext(), R.color.black_5));
        yAxisMonthly.setDrawAxisLine(false);
        yAxisMonthly.setGridLineWidth(1f);

        yAxisMonthly.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                if (value > 10) {
                    return UnitsManager.formatRupeeToMyCurrency(value);
                } else if (value > 0) {
                    // Earlier only one decimal was being shown
                    return UnitsManager.formatRupeeToMyCurrency(value);
                } else {
                    return "";
                }
            }
        });

        XAxis xAxisMonthly = barChartMonthly.getXAxis();

        xAxisMonthly.setDrawGridLines(false);
        xAxisMonthly.setAxisLineColor(ContextCompat.getColor(getContext(), R.color.black_5));
        xAxisMonthly.setAxisLineWidth(1.0f);
        xAxisMonthly.setGranularity(1f);
        xAxisMonthly.setPosition(XAxis.XAxisPosition.BOTTOM);
        IAxisValueFormatter valueFormatterMonthly = new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                int index = (int) value;
                return barChartDataSetMonthly.getLabelForIndex(index)/*value+""*/;
            }
        };
        xAxisMonthly.setValueFormatter(valueFormatterMonthly);
        barChartMonthly.setDrawBorders(false);
        barChartMonthly.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry entry, Highlight highlight) {
                statsImpact.setText(getContext().getString(R.string.rupee_symbol) + " " + ((int) entry.getY()));
                BarChartEntry barChartEntry = barChartDataSetMonthly.getBarChartEntry((int) entry.getX());
                statsWorkout.setText(barChartEntry.getCount() + "");
                statsKms.setText(Utils.formatToKmsWithTwoDecimal((float) (barChartEntry.getDistance() * 1000)) + "");
            }

            @Override
            public void onNothingSelected() {

            }
        });
        barChartMonthly.setPinchZoom(false);
        barChartMonthly.setDoubleTapToZoomEnabled(false);
        barChartMonthly.setScaleEnabled(false);
    }

    private void setBG(int type) {
        myStatsDaily.setBackgroundResource(R.drawable.bg_my_stats_daily);
        myStatsDaily.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
        myStatsWeekly.setBackgroundResource(R.drawable.bg_my_stats_weekly);
        myStatsWeekly.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
        myStatsMonthly.setBackgroundResource(R.drawable.bg_my_stats_monthly);
        myStatsMonthly.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
        switch (type) {
            case BarChartDataSet.TYPE_DAILY:
                myStatsDaily.setBackgroundResource(R.drawable.bg_my_stats_daily_hover);
                myStatsDaily.setTextColor(getResources().getColor(R.color.white));
                break;
            case BarChartDataSet.TYPE_WEEKLY:
                myStatsWeekly.setBackgroundResource(R.drawable.bg_my_stats_weekly_hover);
                myStatsWeekly.setTextColor(getResources().getColor(R.color.white));
                break;
            case BarChartDataSet.TYPE_MONTHLY:
                myStatsMonthly.setBackgroundResource(R.drawable.bg_my_stats_monthly_hover);
                myStatsMonthly.setTextColor(getResources().getColor(R.color.white));
                break;
        }
    }

    private void setupToolbar() {
        setHasOptionsMenu(true);
        setToolbarTitle(getResources().getString(R.string.profile));
    }

    @OnClick(R.id.tv_streak)
    void streakClick() {
        getFragmentController().replaceFragment(StreakFragment.newInstance(Constants.FROM_PROFILE_FOR_STREAK), true);
        AnalyticsEvent.create(Event.ON_CLICK_STREAK_ICON)
                .buildAndDispatch();
    }

    OverlayStatsRunnable overlayStatsRunnable;
    OverlayStreakRunnable overlayStreakRunnable;

    private void prepareStreakOnboardingOverlays() {
        if (OnboardingOverlay.STREAK_COUNT.isEligibleForDisplay(getProfileOpenCount(), 0) && overlayStreakRunnable == null) {
            overlayStreakRunnable = new OverlayStreakRunnable();
            MainApplication.getMainThreadHandler().postDelayed(overlayStreakRunnable, OnboardingOverlay.STREAK_COUNT.getDelayInMillis());
        }
    }

    private void prepareStatsOnboardingOverlays() {
        long workoutCount = MainApplication.getInstance().getUsersWorkoutCount();
        int profileScreenVisit = getProfileOpenCount();

        if (OnboardingOverlay.MY_STATS.isEligibleForDisplay(profileScreenVisit, workoutCount>0?1:2) && overlayStatsRunnable==null) {
            overlayStatsRunnable = new OverlayStatsRunnable();
            MainApplication.getMainThreadHandler().postDelayed(overlayStatsRunnable, OnboardingOverlay.STREAK_COUNT.getDelayInMillis());
        }
    }

    private void incrementProfileScreenVisitCount() {
        int launchCount = getProfileOpenCount();
        SharedPrefsManager.getInstance().setInt(Constants.PREF_SCREEN_LAUNCH_COUNT_PREFIX + PROFILE_SCREEN, ++launchCount);
    }

    public int getProfileOpenCount() {
        return SharedPrefsManager.getInstance().getInt(Constants.PREF_SCREEN_LAUNCH_COUNT_PREFIX + PROFILE_SCREEN);
    }
    public class OverlayStreakRunnable implements Runnable {
        private boolean cancelled;
        @Override
        public void run() {
            if (!cancelled && isVisible()) {
                // This is a hack to get hold of the anchor view for help center menu item
                materialTapTargetPrompt = Utils.setOverlay(OnboardingOverlay.STREAK_COUNT,
                        streakValue,
                        getActivity(),
                        true, false,false);
                materialTapTargetPrompt.show();
            }
        }
        public void cancel() {
            cancelled = true;
        }
    }

    public class OverlayStatsRunnable implements Runnable {
        private boolean cancelled;
        @Override
        public void run() {
            if (!cancelled && isVisible()) {
                // This is a hack to get hold of the anchor view for help center menu item

                materialTapTargetPrompt = Utils.setOverlay(OnboardingOverlay.MY_STATS,
                        overlayLayout,
                        getActivity(),
                        true, false,true);
                materialTapTargetPrompt.show();
            }
        }
        public void cancel() {
            cancelled = true;
        }
    }

    @OnClick(R.id.see_all_badges)
    public void onClickSeeAll()
    {
        getFragmentController().replaceFragment(new AchievedBadgeProgressFragment(), true);
    }
}
