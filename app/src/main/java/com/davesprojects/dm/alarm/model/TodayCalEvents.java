package com.davesprojects.dm.alarm.model;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

public class TodayCalEvents {
    private Context con;
    private String[] calIds;

    public TodayCalEvents(Context c) {
        con = c;

        SharedPreferences prefsF = con.getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        if (prefsF.contains("calIdsToAdd")) {
            calIds = prefsF.getString("calIdsToAdd", "").split(",");
        }
    }

    public ArrayList<String> getTodayCalEvents(boolean considerCalOpt) {
        final String[] INSTANCE_PROJECTION = new String[] {
                CalendarContract.Instances.EVENT_ID,      // 0
                CalendarContract.Instances.BEGIN,         // 1
                CalendarContract.Instances.TITLE,          // 2
                CalendarContract.Instances.DTSTART,
                CalendarContract.Instances.START_MINUTE,
                CalendarContract.Instances.CALENDAR_ID
        };

        // other indexes not used
        final int PROJECTION_TITLE_INDEX = 2;
        final int PROJECTION_STARTMINUTE_INDEX = 4;
        final int PROJECTION_ID_INDEX = 5;

        Calendar beginTime = Calendar.getInstance();
        int date = beginTime.get(Calendar.DAY_OF_MONTH);
        int year = beginTime.get(Calendar.YEAR);
        int month = beginTime.get(Calendar.MONTH);
        beginTime.set(year, month, date, 0, 0);
        Calendar endTime = Calendar.getInstance();
        endTime.set(year, month, date + 1, 0, 0);
        long startMillis = beginTime.getTimeInMillis();
        long endMillis = endTime.getTimeInMillis();

        // Construct the query with the desired date range.
        Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(builder, startMillis);
        ContentUris.appendId(builder, endMillis);

        Cursor cur;
        ContentResolver cr = con.getContentResolver();
        cur =  cr.query(builder.build(),
                INSTANCE_PROJECTION,
                null,
                null,
                null);

        String title;
        String startmin;

        ArrayList<String> calTasksForToday = new ArrayList<>();

        if (cur != null) {
            while (cur.moveToNext()) {
                // Get the field values
                title = cur.getString(PROJECTION_TITLE_INDEX);
                startmin = cur.getString(PROJECTION_STARTMINUTE_INDEX);
                String calIdOne = cur.getString(PROJECTION_ID_INDEX);
                if (considerCalOpt) {
                    for (String s : calIds) {
                        if (s.equals(calIdOne)) {
                            calTasksForToday.add(convertMinsSinceMidnightToTime(startmin) + " - " + title);
                            break;
                        }
                    }
                } else {
                    calTasksForToday.add(convertMinsSinceMidnightToTime(startmin) + " - " + title);
                }
            }
            cur.close();
        }

        return calTasksForToday;
    }

    private String convertMinsSinceMidnightToTime(String startmin) {
        int startminCalc = Integer.parseInt(startmin);
        int hour = startminCalc / 60;
        int minutes = startminCalc % 60;
        String amOrPM = "AM";
        String minutesStr = String.valueOf(minutes);

        if (hour >= 12) {
            amOrPM = "PM";
            if (hour > 12) {
                hour -= 12;  // prevents 8:30 PM being shown as 20:30 PM
            }
        }

        if (minutes < 10) {
            minutesStr = "0" + minutesStr;
        }

        return (hour) + ":" + minutesStr + " " + amOrPM;
    }
}
