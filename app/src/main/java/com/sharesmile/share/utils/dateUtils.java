package com.sharesmile.share.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Shine on 14/05/16.
 */
public class DateUtils {

    public static String DEFAULT_DATE_FORMAT = "yyyy-MM-dd hh:mm:ss";

    public static String getCurrentDate() {

        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
        return simpleDateFormat.format(date);

    }

    public static String getDefaultFormattedDate(Date date) {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
        return simpleDateFormat.format(date);

    }

    public static Date getDefaultFormattedDate(String dateString) {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
        try {
           return simpleDateFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }

    }
}
