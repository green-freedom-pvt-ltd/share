package com.sharesmile.share.rfac.fragments;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.sharesmile.share.MainApplication;
import com.sharesmile.share.R;
import com.sharesmile.share.Workout;
import com.sharesmile.share.WorkoutDao;
import com.sharesmile.share.core.BaseFragment;
import com.sharesmile.share.rfac.CustomBarChartRenderer;
import com.sharesmile.share.utils.DateUtil;
import com.sharesmile.share.utils.Logger;
import com.sharesmile.share.utils.SharedPrefsManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.sharesmile.share.core.Constants.PREF_TOTAL_CALORIES;
import static com.sharesmile.share.core.Constants.PREF_TOTAL_IMPACT;
import static com.sharesmile.share.core.Constants.PREF_WORKOUT_LIFETIME_DISTANCE;

/**
 * Created by ankitmaheshwari on 5/18/17.
 */

public class ProfileStatsViewFragment extends BaseFragment implements View.OnClickListener{

    private static final String TAG = "ProfileStatsViewFragment";
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

    @BindView(R.id.tv_impact)
    TextView impactInRupees;

    @BindView(R.id.cal_not_available_container)
    View caloriesNotAvailableContainer;

    @BindView(R.id.cal_available_container)
    View caloriesAvailableContainer;

    @BindView(R.id.tv_cal)
    TextView calories;

    @BindView(R.id.tv_distance)
    TextView distance;

    int position;

    private int totalAmountRaised;
    private double totalDistance;
    private double totalCalories;

    public static ProfileStatsViewFragment getInstance(int position) {
        ProfileStatsViewFragment fragment = new ProfileStatsViewFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(POSITION, position);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arg = getArguments();
        position = arg.getInt(POSITION);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_profile_stats_view, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (position == POSITION_WEEKLY){
            setUpWeeklyStats();
        }else if (position == POSITION_ALL_TIME){
            setUpAllTimeStats();
        }

        displayStats();
        setUpBarChart();
    }

    private void setUpWeeklyStats(){
        toTheRight.setText("All Time >");
        duration.setText("Last 7 Days");
        long currentTimeStampMillis = DateUtil.getServerTimeInMillis();

        Calendar today = Calendar.getInstance();
        today.setTimeInMillis(currentTimeStampMillis);
        int thisDayOfLastWeek = today.get(Calendar.DAY_OF_WEEK); // SUNDAY:1 MONDAY:2 and so on

        long timeStamp6DaysBefore = currentTimeStampMillis - 518400000;
        Calendar firstDay = Calendar.getInstance();
        firstDay.setTimeInMillis(timeStamp6DaysBefore);
        int firstDayOfLastWeek = firstDay.get(Calendar.DAY_OF_WEEK);

        long beginningOfFirstDayTsMillis = timeStamp6DaysBefore - getMillisElapsedSinceBeginningOfDay(firstDay);

        // So the time period is beginningOfFirstDayTsMillis to currentTimeStampMillis

        Logger.d(TAG, "setUpWeeklyStats: beginningOfFirstDayTsMillis = " + beginningOfFirstDayTsMillis + ", and currentTimeStampMillis = " + currentTimeStampMillis);
        Logger.d(TAG, "setUpWeeklyStats: thisDayOfLastWeek = " + thisDayOfLastWeek + ", and firstDayOfLastWeek = " + firstDayOfLastWeek);

        WorkoutDao workoutDao = MainApplication.getInstance().getDbWrapper().getWorkoutDao();
        List<Workout> workouts = workoutDao.queryBuilder().where(WorkoutDao.Properties.IsValidRun.eq(true))
                .orderDesc(WorkoutDao.Properties.Distance).limit(10).build().list();

        for (Workout workout : workouts){
            Logger.d(TAG, "WorkoutId: " + workout.getId() + ", distance: " + workout.getDistance()
                    + ", beginTimeStamp = " + workout.getBeginTimeStamp() + ", endTimeStamp: " + workout.getEndTimeStamp());
        }

        SQLiteDatabase database = MainApplication.getInstance().getDbWrapper().getDaoSession().getDatabase();
        // Calculate total_amount_raised, total_distance, total_steps, total_recorded_time, and total_calories
        Cursor cursor = database.rawQuery("SELECT "
                + "SUM(" +WorkoutDao.Properties.RunAmount.columnName + "), "
                + "SUM("+ WorkoutDao.Properties.Distance.columnName +"), "
                + "SUM("+ WorkoutDao.Properties.Calories.columnName +") "
                + "FROM " + WorkoutDao.TABLENAME + " where "
                + WorkoutDao.Properties.IsValidRun.columnName + " is 1" +
                " and " + WorkoutDao.Properties.BeginTimeStamp.columnName + " between "
                + beginningOfFirstDayTsMillis + " and " + currentTimeStampMillis, new String []{});
        cursor.moveToFirst();
        totalAmountRaised = Math.round(cursor.getFloat(0));
        totalDistance = cursor.getDouble(1);
        totalCalories = cursor.getDouble(2);

        Logger.d(TAG, "Fetched Weekly stats from DB: totalAmountRaised: " + totalAmountRaised
                + ", totalDistance: " + totalDistance + ", totalCalories: " + totalCalories);

    }



    private long getMillisElapsedSinceBeginningOfDay(Calendar day){
        long hour = day.get(Calendar.HOUR_OF_DAY);
        long minute = day.get(Calendar.MINUTE);
        long secs = day.get(Calendar.SECOND);
        long millis = day.get(Calendar.MILLISECOND);
        return hour*3600000 + minute*60000 + secs*1000 + millis;
    }



    private void setUpAllTimeStats(){
        toTheLeft.setText("< Last 7 Days");
        duration.setText("All Time");
        totalAmountRaised = SharedPrefsManager.getInstance().getInt(PREF_TOTAL_IMPACT);
        totalCalories = SharedPrefsManager.getInstance().getLong(PREF_TOTAL_CALORIES);
        totalDistance = SharedPrefsManager.getInstance().getLong(PREF_WORKOUT_LIFETIME_DISTANCE);
        Logger.d(TAG, "Fetched All time stats from Preferences: totalAmountRaised: " + totalAmountRaised
                + ", totalDistance: " + totalDistance + ", totalCalories: " + totalCalories);
    }

    private void displayStats(){
        impactInRupees.setText("\u20B9 " + totalAmountRaised);
        resolveCaloriesContainer();
        distance.setText(String.valueOf(totalDistance));
    }

    private void resolveCaloriesContainer(){
        if (MainApplication.getInstance().getUserDetails().getBodyWeight() > 0f){
            // Weight set, so calories must be available
            caloriesAvailableContainer.setVisibility(View.VISIBLE);
            caloriesNotAvailableContainer.setVisibility(View.GONE);
            calories.setText(String.valueOf(totalCalories));
        }else {
            // Weight not set yet
            caloriesAvailableContainer.setVisibility(View.GONE);
            caloriesNotAvailableContainer.setVisibility(View.VISIBLE);
            caloriesNotAvailableContainer.setOnClickListener(this);
        }
    }

    private void setUpBarChart(){

        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, 10));
        entries.add(new BarEntry(1, 15));
        entries.add(new BarEntry(2, 23));
        entries.add(new BarEntry(3, 28));
        entries.add(new BarEntry(4, 5));
        entries.add(new BarEntry(5, 12));
        entries.add(new BarEntry(6, 19));

        BarDataSet dataSet = new BarDataSet(entries, "Stats");
        dataSet.setColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
        dataSet.setValueTextColor(ContextCompat.getColor(getContext(), R.color.greyish_brown));

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

        yAxis.setSpaceBottom(25);
        yAxis.setLabelCount(3, true);

        yAxis.setCenterAxisLabels(true);
        yAxis.setGridColor(ContextCompat.getColor(getContext(), R.color.warm_grey));
        yAxis.setDrawAxisLine(false);
        yAxis.setGridLineWidth(0.5f);

        yAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                if (value > 0){
                    return "\u20B9 " + Math.round(value);
                }else {
                    return "";
                }
            }
        });

        XAxis xAxis = barChart.getXAxis();

        xAxis.setDrawGridLines(false);
        xAxis.setAxisLineColor(ContextCompat.getColor(getContext(), R.color.warm_grey));
        xAxis.setAxisLineWidth(1.5f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        List<String> list = Arrays.asList("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun");
        IAxisValueFormatter valueFormatter = new IndexAxisValueFormatter(list);
        xAxis.setValueFormatter(valueFormatter);

        barChart.setDrawBorders(false);

        barChart.setRenderer(new CustomBarChartRenderer(barChart, barChart.getAnimator(),
                barChart.getViewPortHandler()));


        barChart.invalidate();
        barChart.setTouchEnabled(false);
//        barChart.getData().setHighlightEnabled(false);
//        barChart.setPinchZoom(false);
//        barChart.setDoubleTapToZoomEnabled(false);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.cal_not_available_container:
                // TODO: Open WeightInputDialog

                break;
        }
    }
}
