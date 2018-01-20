package com.sharesmile.share.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Shine on 14/05/16.
 */
public class DateUtil {

    public static String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static String DEFAULT_DATE_FORMAT_DATE_ONLY = "yyyy-MM-dd";
    public static String USER_FORMAT_DATE = "dd-MMM-yyyy hh:mm a";
    public static String USER_FORMAT_DATE_DATE_ONLY = "dd-MMM-yyyy";
    public static String MILLISEC_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
    public static String MILLISEC_DATE_FORMAT_TZ = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    public static String HH_MM_AMPM = "hh:mm a";


    public static String getCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(getServerTimeInMillis());
        Date date = calendar.getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
        return simpleDateFormat.format(date);

    }

    public static String getDefaultFormattedDate(Date date) {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
        return simpleDateFormat.format(date);

    }

    public static String getMillisecFormattedDate(Date date) {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(MILLISEC_DATE_FORMAT);
        return simpleDateFormat.format(date);

    }

    public static String getCustomFormattedDate(Date date) {
        return getCustomFormattedDate(date, USER_FORMAT_DATE);

    }

    public static String getCustomFormattedDate(Date date, String format) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        return simpleDateFormat.format(date);
    }

    public static Date getDefaultFormattedDate(String dateString) {
        return getFormattedDate(dateString, DEFAULT_DATE_FORMAT);
    }

    public static Date getDate(){
        return new Date(getServerTimeInMillis());
    }

    public static long getServerTimeInMillis(){
        return ServerTimeKeeper.getServerTimeStampInMillis();
    }

    public static long getSystemTimeForServerTime(long serverTimeStamp){
        long delta = System.currentTimeMillis() - getServerTimeInMillis();
        return serverTimeStamp + delta;
    }

    public static Date getFormattedDate(String dateString, String format) {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        try {
            return simpleDateFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}
