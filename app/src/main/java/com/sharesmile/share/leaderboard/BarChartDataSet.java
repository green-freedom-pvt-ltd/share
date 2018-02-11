package com.sharesmile.share.leaderboard;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.github.mikephil.charting.data.BarEntry;
import com.sharesmile.share.core.application.MainApplication;
import com.sharesmile.share.WorkoutDao;
import com.sharesmile.share.utils.DateUtil;
import com.sharesmile.share.core.Logger;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ankitmaheshwari on 5/23/17.
 */

public class BarChartDataSet {

    private static final String TAG = "BarChartDataSet";

    private Map<Integer, BarChartEntry> entries;
    private List<BarEntry> barEntries;

    public static final int TYPE_WEEKLY = 1;

    public BarChartDataSet(int type){
        switch (type){
            case TYPE_WEEKLY:
                initLastWeekDataSet();
                break;
        }
    }

    public BarChartEntry getBarChartEntry(int index){
        return entries.get(index);
    }

    public BarEntry getBarEntry(int index){
        return new BarEntry(index, getBarChartEntry(index).getImpactInRupees());
    }

    public String getLabelForIndex(int index){
        return entries.get(index).getLabel();
    }

    public Map<Integer, BarChartEntry> getBarChartEntries(){
        return entries;
    }

    public List<BarEntry> getBarEntries(){
        return barEntries;
    }

    private void initLastWeekDataSet(){
        long currentTimeStampMillis = DateUtil.getServerTimeInMillis();
        entries = new HashMap<>();

        Calendar today = Calendar.getInstance();
        today.setTimeInMillis(currentTimeStampMillis);
        int thisDayOfLastWeek = today.get(Calendar.DAY_OF_WEEK); // SUNDAY:1 MONDAY:2 and so on
        int index = 6;
        long begin = getEpochForBeginningOfDay(today);
        long end = currentTimeStampMillis;
        int impact = getImpactInInterval(begin, end);
        entries.put(index, new BarChartEntry(begin, end, impact, getDayLabel(thisDayOfLastWeek)));
        int dayOfWeek = thisDayOfLastWeek;
        while (index >= 0){
            BarChartEntry prevInterval = entries.get(index);
            int prevDayOfWeek = (dayOfWeek - 1) == 0 ? 7 : (dayOfWeek - 1);
            begin = prevInterval.getBeginTimeStamp() - 86400000;
            end = prevInterval.getBeginTimeStamp();
            impact = getImpactInInterval(begin, end);
            entries.put(index-1, new BarChartEntry(begin, end, impact, getDayLabel(prevDayOfWeek)));
            index--;
            dayOfWeek = prevDayOfWeek;
        }
        barEntries = new ArrayList<>(7);
        for (int i=0; i<7; i++){
            barEntries.add(i,new BarEntry(i, entries.get(i).getImpactInRupees()));
        }
    }

    private int getImpactInInterval(long beginTsMillis, long endTsMillis){
        SQLiteDatabase database = MainApplication.getInstance().getDbWrapper().getDaoSession().getDatabase();
        // Calculate amount_raised in interval
        Cursor cursor = database.rawQuery("SELECT "
                + "SUM(" +WorkoutDao.Properties.RunAmount.columnName + ") "
                + "FROM " + WorkoutDao.TABLENAME + " where "
                + WorkoutDao.Properties.IsValidRun.columnName + " is 1" +
                " and " + WorkoutDao.Properties.BeginTimeStamp.columnName + " between "
                + beginTsMillis + " and " + endTsMillis, new String []{});
        cursor.moveToFirst();
        int amountRaised = Math.round(cursor.getFloat(0));

        // Let's try with "Date" column also
        Cursor cursorDate = database.rawQuery("SELECT "
                + "SUM(" +WorkoutDao.Properties.RunAmount.columnName + ") "
                + "FROM " + WorkoutDao.TABLENAME + " where "
                + WorkoutDao.Properties.IsValidRun.columnName + " is 1" +
                " and " + WorkoutDao.Properties.Date.columnName + " between "
                + beginTsMillis + " and " + endTsMillis, new String []{});
        cursorDate.moveToFirst();
        int amountRaisedWithDateColumn = Math.round(cursorDate.getFloat(0));

        if (amountRaisedWithDateColumn > amountRaised){
            amountRaised = amountRaisedWithDateColumn;
        }

        Logger.d(TAG, "Amount raised between " + beginTsMillis + " and " + endTsMillis + " is " + amountRaised);
        return amountRaised;
    }

    /**
     * Get time elapsed, in millis, since the beginning of input day
     * @param day Calendar instance
     * @return
     */
    private long getMillisElapsedSinceBeginningOfDay(Calendar day){
        long hour = day.get(Calendar.HOUR_OF_DAY);
        long minute = day.get(Calendar.MINUTE);
        long secs = day.get(Calendar.SECOND);
        long millis = day.get(Calendar.MILLISECOND);
        return hour*3600000 + minute*60000 + secs*1000 + millis;
    }

    private long getEpochForBeginningOfDay(Calendar day){
        long currentTs = day.getTimeInMillis();
        long millisElapsedSinceBeginning = getMillisElapsedSinceBeginningOfDay(day);
        return currentTs - millisElapsedSinceBeginning;
    }

    public static final String getDayLabel(int dayOfWeek){
        switch (dayOfWeek){
            case 1:
                return "SUN";
            case 2:
                return "MON";
            case 3:
                return "TUE";
            case 4:
                return "WED";
            case 5:
                return "THU";
            case 6:
                return "FRI";
            case 7:
                return "SAT";
            default:
                throw new IllegalArgumentException("Invalid value for dayOfWeek: " + dayOfWeek);
        }
    }

}
