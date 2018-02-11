package com.sharesmile.share.rfac.fragments;

import android.graphics.LinearGradient;
import android.graphics.Shader;
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
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.sharesmile.share.R;
import com.sharesmile.share.core.base.BaseFragment;
import com.sharesmile.share.home.settings.UnitsManager;
import com.sharesmile.share.rfac.CustomBarChartRenderer;
import com.sharesmile.share.rfac.models.BarChartDataSet;
import com.sharesmile.share.core.Logger;
import com.sharesmile.share.core.SharedPrefsManager;
import com.sharesmile.share.utils.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.sharesmile.share.core.Constants.PREF_TOTAL_IMPACT;
import static com.sharesmile.share.core.Constants.PREF_TOTAL_RUN;
import static com.sharesmile.share.core.Constants.PREF_WORKOUT_LIFETIME_DISTANCE;
import static com.sharesmile.share.rfac.models.BarChartDataSet.TYPE_WEEKLY;

/**
 * Created by ankitmaheshwari on 5/18/17.
 */

public class ProfileStatsViewFragment extends BaseFragment {

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

    @BindView(R.id.tv_total_runs)
    TextView totalRuns;

    @BindView(R.id.tv_km_distance)
    TextView distance;

    @BindView(R.id.tv_distance_unit)
    TextView distanceUnit;

    int position;

    private int totalAmountRaised;
    private int numRuns;
    private double totalDistance;
    private BarChartDataSet barChartDataSet;

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
            setUpAllTimeStats();
        }else if (position == POSITION_ALL_TIME){
            setUpAllTimeStats();
        }

        int height = (int) getResources().getDimension(R.dimen.super_large_text);
        Shader textShader=new LinearGradient(0, 0, 0, height, new int[]{0xff04cbfd,0xff33f373},
                new float[]{0, 1}, Shader.TileMode.CLAMP);
        impactInRupees.getPaint().setShader(textShader);

        displayStats();
        setUpBarChart();
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
        barChart.setTouchEnabled(false);
//        barChart.getData().setHighlightEnabled(false);
//        barChart.setPinchZoom(false);
//        barChart.setDoubleTapToZoomEnabled(false);
    }

}
