package com.sharesmile.share.rfac.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.sharesmile.share.R;
import com.sharesmile.share.core.BaseFragment;
import com.sharesmile.share.rfac.CustomBarChartRenderer;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ankitmaheshwari on 4/28/17.
 */

public class ProfileStatsFragment extends BaseFragment {

    @BindView(R.id.chart)
    BarChart barChart;

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
        setUpBarChart();
    }

    private void setUpBarChart(){

        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(2, 10));
        entries.add(new BarEntry(4, 15));
        entries.add(new BarEntry(6, 23));
        entries.add(new BarEntry(8, 28));
        entries.add(new BarEntry(10, 5));
        entries.add(new BarEntry(12, 12));
        entries.add(new BarEntry(14, 19));

        BarDataSet dataSet = new BarDataSet(entries, "LastWeek");
        dataSet.setColor(R.color.neon_red);
        dataSet.setValueTextColor(R.color.orange);

        BarData data = new BarData(dataSet);
        barChart.setData(data);
        barChart.setDrawGridBackground(false);
        barChart.getXAxis().setDrawGridLines(false);
        barChart.getAxisLeft().setSpaceBottom(30);
        barChart.getAxisLeft().setDrawGridLines(false);
        barChart.getAxisLeft().setDrawAxisLine(false);

        barChart.setDrawBorders(false);

        barChart.setRenderer(new CustomBarChartRenderer(barChart, barChart.getAnimator(),
                barChart.getViewPortHandler()));


        barChart.invalidate();
    }
}
