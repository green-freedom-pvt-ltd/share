package com.sharesmile.share.profile.history;

import java.util.Calendar;

/**
 * Created by ankitmaheshwari on 6/15/17.
 */

public class RunHistoryDateHeaderItem extends RunHistoryItem {

    private Calendar calendar;
    private float impactInDay;
    private float caloriesInDay;

    public RunHistoryDateHeaderItem(){
    }

    public Calendar getCalendar() {
        return calendar;
    }

    public void setCalendar(Calendar calendar) {
        this.calendar = calendar;
    }

    public float getImpactInDay() {
        return impactInDay;
    }

    public void setImpactInDay(float impactInDay) {
        this.impactInDay = impactInDay;
    }

    public float getCaloriesInDay() {
        return caloriesInDay;
    }

    public void setCaloriesInDay(float caloriesInDay) {
        this.caloriesInDay = caloriesInDay;
    }

    @Override
    public int getType() {
        return DATE_HEADER;
    }
}
