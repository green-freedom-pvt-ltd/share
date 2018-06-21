package com.sharesmile.share.profile;

import android.content.AsyncTaskLoader;
import android.content.Context;

import com.sharesmile.share.profile.stats.BarChartDataSet;

public class SetUpBarChartDataLoader extends AsyncTaskLoader<SetUpBarChartDataLoader.MyStatsBarChart>{

    MyStatsBarChart myStatsBarChart;

    public SetUpBarChartDataLoader(Context context) {
        super(context);

    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        if(myStatsBarChart!=null)
            deliverResult(myStatsBarChart);
        else
        {
            forceLoad();
        }
    }

    @Override
    public SetUpBarChartDataLoader.MyStatsBarChart loadInBackground() {
        myStatsBarChart = new MyStatsBarChart();
        myStatsBarChart.barChartDataSetDaily = new BarChartDataSet(BarChartDataSet.TYPE_DAILY);
        myStatsBarChart.barChartDataSetWeekly = new BarChartDataSet(BarChartDataSet.TYPE_WEEKLY);
        myStatsBarChart.barChartDataSetMonthly = new BarChartDataSet(BarChartDataSet.TYPE_MONTHLY);
        return myStatsBarChart;
    }

    @Override
    public void deliverResult(MyStatsBarChart data) {
        myStatsBarChart = data;
        super.deliverResult(data);
    }

    class MyStatsBarChart {
        BarChartDataSet barChartDataSetDaily;
        BarChartDataSet barChartDataSetWeekly;
        BarChartDataSet barChartDataSetMonthly;
    }
}

