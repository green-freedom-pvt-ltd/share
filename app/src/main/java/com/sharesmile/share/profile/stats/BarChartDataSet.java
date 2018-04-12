package com.sharesmile.share.profile.stats;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.github.mikephil.charting.data.BarEntry;
import com.sharesmile.share.Workout;
import com.sharesmile.share.core.application.MainApplication;
import com.sharesmile.share.WorkoutDao;
import com.sharesmile.share.utils.DateUtil;
import com.sharesmile.share.core.Logger;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by ankitmaheshwari on 5/23/17.
 */

public class BarChartDataSet {

    private static final String TAG = "BarChartDataSet";

    private Map<Integer, BarChartEntry> entries;
    private List<BarEntry> barEntries;

    public static final int TYPE_DAILY = 1;
    public static final int TYPE_WEEKLY = 2;
    public static final int TYPE_MONTHLY = 3;
    private long days = 0;
    private int weeks = 0;
    private int months = 0;
    private int type = 0;

    public BarChartDataSet(int type) {
        this.type = type;
        switch (type) {
            case TYPE_DAILY:
                days = 0;
                initDailyDataSet();
                break;
            case TYPE_WEEKLY:
                weeks = 0;
                initWeeklyDataSet();
                break;
            case TYPE_MONTHLY:
                months = 0;
                initMonthlyDataSet();
                break;
        }
    }

    public BarChartEntry getBarChartEntry(int index) {
        return entries.get(index);
    }

    public BarEntry getBarEntry(int index) {
        return new BarEntry(index, getBarChartEntry(index).getImpactInRupees());
    }

    public String getLabelForIndex(int index) {
        return entries.get(index).getLabel();
    }

    public Map<Integer, BarChartEntry> getBarChartEntries() {
        return entries;
    }

    public List<BarEntry> getBarEntries() {
        return barEntries;
    }

    private void initDailyDataSet() {
        long currentTimeStampMillis = DateUtil.getServerTimeInMillis();
        entries = new HashMap<>();

        Calendar today = Calendar.getInstance();
        today.setTimeInMillis(currentTimeStampMillis);

        int thisDayOfLastWeek = today.get(Calendar.DAY_OF_WEEK); // SUNDAY:1 MONDAY:2 and so on
        long firstRun = getFirstRun();
        days = TimeUnit.DAYS.convert(currentTimeStampMillis - firstRun, TimeUnit.MILLISECONDS);
        int index = (int) (days - 1);
        long begin = getEpochForBeginningOfDay(today);
        long end = currentTimeStampMillis;
        BarChartSetData impact = getImpactInInterval(begin, end);
        entries.put(index, new BarChartEntry(begin, end, impact, getDayLabel(begin)));
        int dayOfWeek = thisDayOfLastWeek;
        while (index >= 0) {
            BarChartEntry prevInterval = entries.get(index);
            int prevDayOfWeek = (dayOfWeek - 1) == 0 ? 7 : (dayOfWeek - 1);
            begin = prevInterval.getBeginTimeStamp() - 86400000;
            end = prevInterval.getBeginTimeStamp();
            impact = getImpactInInterval(begin, end);

            entries.put(index - 1, new BarChartEntry(begin, end, impact, getDayLabel(begin)));
            index--;
            dayOfWeek = prevDayOfWeek;
        }
        barEntries = new ArrayList<>(7);
        for (int i = 0; i < days; i++) {
            barEntries.add(i, new BarEntry(i, entries.get(i).getImpactInRupees()));
        }
    }

    private void initWeeklyDataSet() {
        long currentTimeStampMillis = DateUtil.getServerTimeInMillis();
        entries = new HashMap<>();

        Calendar today = Calendar.getInstance();
        today.setTimeInMillis(currentTimeStampMillis);

        int thisDayOfLastWeek = today.get(Calendar.DAY_OF_WEEK); // SUNDAY:1 MONDAY:2 and so on
        long firstRun = getFirstRun();
        days = TimeUnit.DAYS.convert(currentTimeStampMillis - firstRun, TimeUnit.MILLISECONDS);

        weeks = getWeeksBetween(firstRun, currentTimeStampMillis);
        int index = (int) (weeks - 1);
        long begin = getEpochForBeginningOfWeek(today);
        long end = currentTimeStampMillis;
        BarChartSetData impact = getImpactInInterval(begin, end);
        entries.put(index, new BarChartEntry(begin, end, impact, getDayLabel(thisDayOfLastWeek)));
        int dayOfWeek = thisDayOfLastWeek;

        while (index >= 0) {
            BarChartEntry prevInterval = entries.get(index);
            int prevDayOfWeek = (dayOfWeek - 1) == 0 ? 7 : (dayOfWeek - 1);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(prevInterval.getBeginTimeStamp());
            begin = getEpochForBeginningOfWeek(calendar) - (86400000 * 7);
            end = prevInterval.getBeginTimeStamp();
            impact = getImpactInInterval(begin, end);
            entries.put(index - 1, new BarChartEntry(begin, end, impact, getDayLabel(prevDayOfWeek)));
            index--;
            dayOfWeek = prevDayOfWeek;
        }
        barEntries = new ArrayList<>(7);
        for (int i = 0; i < weeks; i++) {
            barEntries.add(i, new BarEntry(i, entries.get(i).getImpactInRupees()));
        }
    }

    private int getWeeksBetween(long milliStart, long milliEnd) {
        Calendar calendarStart = Calendar.getInstance();
        calendarStart.setTimeInMillis(milliStart);
        calendarStart.set(Calendar.HOUR_OF_DAY, 0);
        calendarStart.set(Calendar.MINUTE, 0);
        calendarStart.set(Calendar.SECOND, 0);
        calendarStart.set(Calendar.MILLISECOND, 0);

        Calendar calendarEnd = Calendar.getInstance();
        calendarEnd.setTimeInMillis(milliEnd);
        calendarEnd.set(Calendar.HOUR_OF_DAY, 0);
        calendarEnd.set(Calendar.MINUTE, 0);
        calendarEnd.set(Calendar.SECOND, 0);
        calendarEnd.set(Calendar.MILLISECOND, 0);

        while (calendarStart.before(calendarEnd)) {
            if (calendarStart.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
                weeks++;
            calendarStart.add(Calendar.DATE, 1);
        }
        if (calendarStart.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            weeks++;
        }
        return weeks;
    }

    private void initMonthlyDataSet() {
        long currentTimeStampMillis = DateUtil.getServerTimeInMillis();
        entries = new HashMap<>();

        Calendar today = Calendar.getInstance();
        today.setTimeInMillis(currentTimeStampMillis);

        int thisDayOfLastWeek = today.get(Calendar.DAY_OF_WEEK); // SUNDAY:1 MONDAY:2 and so on
        long firstRun = getFirstRun();
        days = TimeUnit.DAYS.convert(currentTimeStampMillis - firstRun, TimeUnit.MILLISECONDS);

        months = getMonthsBetween(firstRun, currentTimeStampMillis);
        int index = (int) (months - 1);
        long begin = getEpochForBeginningOfMonth(today);
        long end = currentTimeStampMillis;
        BarChartSetData impact = getImpactInInterval(begin, end);
        entries.put(index, new BarChartEntry(begin, end, impact, getDayLabel(thisDayOfLastWeek)));
        int dayOfWeek = thisDayOfLastWeek;

        while (index >= 0) {
            BarChartEntry prevInterval = entries.get(index);
            int prevDayOfWeek = (dayOfWeek - 1) == 0 ? 7 : (dayOfWeek - 1);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(prevInterval.getBeginTimeStamp());
            calendar.add(Calendar.MILLISECOND, -1);
            int days = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            begin = getEpochForBeginningOfMonth(calendar);
            end = calendar.getTimeInMillis();
            impact = getImpactInInterval(begin, end);
            entries.put(index - 1, new BarChartEntry(begin, end, impact, getDayLabel(prevDayOfWeek)));
            index--;
            dayOfWeek = prevDayOfWeek;
        }
        barEntries = new ArrayList<>(7);
        for (int i = 0; i < months; i++) {
            barEntries.add(i, new BarEntry(i, entries.get(i).getImpactInRupees()));
        }
    }

    private int getMonthsBetween(long milliStart, long milliEnd) {
        Calendar calendarStart = Calendar.getInstance();
        calendarStart.setTimeInMillis(milliStart);
        calendarStart.set(Calendar.HOUR_OF_DAY, 0);
        calendarStart.set(Calendar.MINUTE, 0);
        calendarStart.set(Calendar.SECOND, 0);
        calendarStart.set(Calendar.MILLISECOND, 0);

        Calendar calendarEnd = Calendar.getInstance();
        calendarEnd.setTimeInMillis(milliEnd);
        calendarEnd.set(Calendar.HOUR_OF_DAY, 0);
        calendarEnd.set(Calendar.MINUTE, 0);
        calendarEnd.set(Calendar.SECOND, 0);
        calendarEnd.set(Calendar.MILLISECOND, 0);

        if (calendarStart.get(Calendar.DAY_OF_MONTH) >= 1) {
            months++;
            calendarStart.set(Calendar.DAY_OF_MONTH, 1);
            calendarStart.add(Calendar.MONTH, 1);
        }
        while (calendarStart.before(calendarEnd)) {
            months++;
            calendarStart.add(Calendar.MONTH, 1);
        }
        return months;
    }

    public class BarChartSetData {
        public int impact;
        public int count;
        public double runs;
    }

    private BarChartSetData getImpactInInterval(long beginTsMillis, long endTsMillis) {
        SQLiteDatabase database = MainApplication.getInstance().getDbWrapper().getDaoSession().getDatabase();
        // Calculate amount_raised in interval
        Cursor cursor = database.rawQuery("SELECT "
                + "SUM(" + WorkoutDao.Properties.RunAmount.columnName + ") AS " + WorkoutDao.Properties.RunAmount.columnName
                + ", SUM(" + WorkoutDao.Properties.Distance.columnName + ") AS " + WorkoutDao.Properties.Distance.columnName
                + ", COUNT(" + WorkoutDao.Properties.Distance.columnName + ") AS countOfRows "
                + " FROM " + WorkoutDao.TABLENAME + " where "
                + WorkoutDao.Properties.IsValidRun.columnName + " is 1" +
                " and " + WorkoutDao.Properties.BeginTimeStamp.columnName + " between "
                + beginTsMillis + " and " + endTsMillis, new String[]{});
        cursor.moveToFirst();
        BarChartSetData barChartSetData = new BarChartSetData();
        barChartSetData.impact = Math.round(cursor.getFloat(cursor.getColumnIndex(WorkoutDao.Properties.RunAmount.columnName)));
        barChartSetData.runs = cursor.getFloat(cursor.getColumnIndex(WorkoutDao.Properties.Distance.columnName));
        barChartSetData.count = Math.round(cursor.getFloat(cursor.getColumnIndex("countOfRows")));


        // Let's try with "Date" column also
        Cursor cursorDate = database.rawQuery("SELECT "
                + "SUM(" + WorkoutDao.Properties.RunAmount.columnName + ") AS " + WorkoutDao.Properties.RunAmount.columnName
                + ", SUM(" + WorkoutDao.Properties.Distance.columnName + ") AS " + WorkoutDao.Properties.Distance.columnName
                + ", COUNT(" + WorkoutDao.Properties.Distance.columnName + ") AS countOfRows "
                + "FROM " + WorkoutDao.TABLENAME + " where "
                + WorkoutDao.Properties.IsValidRun.columnName + " is 1" +
                " and " + WorkoutDao.Properties.Date.columnName + " between "
                + beginTsMillis + " and " + endTsMillis, new String[]{});
        cursorDate.moveToFirst();
        BarChartSetData barChartSetDataDate = new BarChartSetData();
        barChartSetDataDate.impact = Math.round(cursorDate.getFloat(cursorDate.getColumnIndex(WorkoutDao.Properties.RunAmount.columnName)));
        barChartSetDataDate.runs = cursorDate.getFloat(cursorDate.getColumnIndex(WorkoutDao.Properties.Distance.columnName));
        barChartSetDataDate.count = Math.round(cursorDate.getFloat(cursorDate.getColumnIndex("countOfRows")));

        if (barChartSetDataDate.impact > barChartSetData.impact) {
            barChartSetData.impact = barChartSetDataDate.impact;
            barChartSetData.runs = barChartSetDataDate.runs;
            barChartSetData.count = barChartSetDataDate.count;
        }


        Logger.d(TAG, "Amount raised between " + beginTsMillis + " and " + endTsMillis + " is " + barChartSetData.impact + " with " + barChartSetData.runs + "kms , " + barChartSetData.count + " rows");
        return barChartSetData;
    }

    public long getNoOfValuesInXAxis() {
        switch (type) {
            case TYPE_DAILY:
                return days;
            case TYPE_WEEKLY:
                return weeks;
            case TYPE_MONTHLY:
                return months;
            default:
                return 1;
        }

    }

    private long getFirstRun() {
        SQLiteDatabase database = MainApplication.getInstance().getDbWrapper().getDaoSession().getDatabase();
        // Calculate amount_raised in interval
        Cursor cursor = database.rawQuery("SELECT "
                + "MIN(" + WorkoutDao.Properties.BeginTimeStamp.columnName + ") "
                + "FROM " + WorkoutDao.TABLENAME, null);
        cursor.moveToFirst();
        Logger.d(TAG, "getFirstRun : " + cursor.getLong(0));
        return cursor.getLong(0);
    }

    /**
     * Get time elapsed, in millis, since the beginning of input day
     *
     * @param day Calendar instance
     * @return
     */
    private long getMillisElapsedSinceBeginningOfDay(Calendar day) {
        long hour = day.get(Calendar.HOUR_OF_DAY);
        long minute = day.get(Calendar.MINUTE);
        long secs = day.get(Calendar.SECOND);
        long millis = day.get(Calendar.MILLISECOND);
        return hour * 3600000 + minute * 60000 + secs * 1000 + millis;
    }

    private long getMillisElapsedSinceBeginningOfWeek(Calendar day) {
        long days = day.get(Calendar.DAY_OF_WEEK) - 1;
        long hour = day.get(Calendar.HOUR_OF_DAY);
        long minute = day.get(Calendar.MINUTE);
        long secs = day.get(Calendar.SECOND);
        long millis = day.get(Calendar.MILLISECOND);
        return days * 86400000 + hour * 3600000 + minute * 60000 + secs * 1000 + millis;
    }

    private long getMillisElapsedSinceBeginningOfMonth(Calendar day) {
        long days = day.get(Calendar.DAY_OF_MONTH) - 1;
        long hour = day.get(Calendar.HOUR_OF_DAY);
        long minute = day.get(Calendar.MINUTE);
        long secs = day.get(Calendar.SECOND);
        long millis = day.get(Calendar.MILLISECOND);
        return days * 86400000 + hour * 3600000 + minute * 60000 + secs * 1000 + millis;
    }

    private long getEpochForBeginningOfDay(Calendar day) {
        long currentTs = day.getTimeInMillis();
        long millisElapsedSinceBeginning = getMillisElapsedSinceBeginningOfDay(day);
        return currentTs - millisElapsedSinceBeginning;
    }

    private long getEpochForBeginningOfWeek(Calendar day) {
        long currentTs = day.getTimeInMillis();
        long millisElapsedSinceBeginning = getMillisElapsedSinceBeginningOfWeek(day);
        return currentTs - millisElapsedSinceBeginning;
    }

    private long getEpochForBeginningOfMonth(Calendar day) {
        long currentTs = day.getTimeInMillis();
        long millisElapsedSinceBeginning = getMillisElapsedSinceBeginningOfMonth(day);
        return currentTs - millisElapsedSinceBeginning;
    }

    public static final String getDayLabel(long dayOfWeek) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE");
        Date date = new Date(dayOfWeek);
        return simpleDateFormat.format(date).toUpperCase();
        /*switch (dayOfWeek){
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
        }*/
    }

}
