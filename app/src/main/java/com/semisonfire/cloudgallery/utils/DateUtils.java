package com.semisonfire.cloudgallery.utils;


import android.text.format.DateFormat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateUtils {

    public static final String ONLY_DATE_FORMAT = "dd.MM.yyyy";
    public static final String DEFAULT_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    public static String getCurrentDate() {
        long millis = System.currentTimeMillis();
        return getDateString(new Date(millis), DEFAULT_FORMAT);
    }

    public static String getDateString(String date, String format) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DEFAULT_FORMAT, Locale.getDefault());
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            Date newDate = simpleDateFormat.parse(date);
            return getDateString(newDate, format);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getDateString(Date date, String format) {
        return DateFormat.format(format, date).toString();
    }
}
