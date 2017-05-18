package com.sharesmile.share.rfac.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import com.sharesmile.share.R;
import com.sharesmile.share.core.BaseFragment;
import com.sharesmile.share.rfac.CustomBarChartRenderer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ankitmaheshwari on 5/18/17.
 */

public class ProfileStatsViewFragment extends BaseFragment {

    private static final String TAG = "ProfileStatsViewFragment";
    private static final String POSITION = "position";

    @BindView(R.id.chart)
    BarChart barChart;

    int position;

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
        setUpBarChart();
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

        BarDataSet dataSet = new BarDataSet(entries, "LastWeek");
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
    }


}
