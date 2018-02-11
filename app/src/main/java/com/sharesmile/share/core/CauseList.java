package com.sharesmile.share.core;

import com.google.gson.annotations.SerializedName;
import com.sharesmile.share.core.base.UnObfuscable;
import com.sharesmile.share.home.ExchangeRate;

import java.util.List;

/**
 * Created by Shine on 01/05/16.
 */
public class CauseList implements UnObfuscable {

    private static final String TAG = CauseList.class.getSimpleName();

    @SerializedName("count")
    private int count;

    @SerializedName("next")
    private String next;

    @SerializedName("previous")
    private String previous;

    @SerializedName("overall_impact")
    private double overallImpactInRupees;

    @SerializedName("overall_num_steps")
    private long overallNumSteps;

    @SerializedName("overall_num_runs")
    private long overallNumRuns;

    @SerializedName("exchange_rates")
    private List<ExchangeRate> exchangeRates;

    @SerializedName("results")
    List<CauseData> causes;

    public int getPageNum() {
        return count;
    }

    public List<CauseData> getCauses() {
        return causes;
    }

    public void setCauses(List<CauseData> causes) {
        this.causes = causes;
    }

    public List<ExchangeRate> getExchangeRates() {
        return exchangeRates;
    }

    public void setExchangeRates(List<ExchangeRate> exchangeRates) {
        this.exchangeRates = exchangeRates;
    }

    public double getOverallImpactInRupees() {
        return overallImpactInRupees;
    }

    public long getOverallNumSteps() {
        return overallNumSteps;
    }

    public long getOverallNumRuns() {
        return overallNumRuns;
    }
}