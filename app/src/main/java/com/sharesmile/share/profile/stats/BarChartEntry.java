package com.sharesmile.share.profile.stats;

/**
 * Created by ankitmaheshwari on 5/23/17.
 */

public class BarChartEntry {

    private long beginTimeStamp;
    private long endTimeStamp;
    private int impactInRupees;
    private double distance;
    private int count;
    private String label;

    public BarChartEntry(long beginTimeStamp, long endTimeStamp, BarChartDataSet.BarChartSetData impactInRupees, String label) {
        this.beginTimeStamp = beginTimeStamp;
        this.endTimeStamp = endTimeStamp;
        this.impactInRupees = impactInRupees.impact;
        this.distance = impactInRupees.runs;
        this.count = impactInRupees.count;
        this.label = label;
    }

    public long getBeginTimeStamp() {
        return beginTimeStamp;
    }

    public long getEndTimeStamp() {
        return endTimeStamp;
    }

    public int getImpactInRupees() {
        return impactInRupees;
    }

    public double getDistance() {
        return distance;
    }

    public int getCount() {
        return count;
    }

    public String getLabel() {
        return label;
    }
}
