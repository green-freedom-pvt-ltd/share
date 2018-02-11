package com.sharesmile.share.profile.stats;

/**
 * Created by ankitmaheshwari on 5/23/17.
 */

public class BarChartEntry {

    private long beginTimeStamp;
    private long endTimeStamp;
    private int impactInRupees;
    private String label;

    public BarChartEntry(long beginTimeStamp, long endTimeStamp, int impactInRupees, String label) {
        this.beginTimeStamp = beginTimeStamp;
        this.endTimeStamp = endTimeStamp;
        this.impactInRupees = impactInRupees;
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

    public String getLabel() {
        return label;
    }
}
