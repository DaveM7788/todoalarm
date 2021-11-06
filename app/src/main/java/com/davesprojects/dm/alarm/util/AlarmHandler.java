package com.davesprojects.dm.alarm.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;

import com.davesprojects.dm.alarm.db.DBHelper;

import java.util.ArrayList;
import java.util.Calendar;

// Alarm Handler is responsible for finding the next alarm to go off and turning it on
// all alarms are stored in the sqlite database
public class AlarmHandler {
    Context con;
    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;

    public AlarmHandler(Context a) {
        con = a;
    }

    private int transDayToNum(String day) {
        int toRet;
        switch(day) {
            case "Su": toRet = 1; break;
            case "Mo": toRet = 2; break;
            case "Tu": toRet = 3; break;
            case "We": toRet = 4; break;
            case "Th": toRet = 5; break;
            case "Fr": toRet = 6; break;
            case "Sa": toRet = 7; break;
            default: toRet = 0;
        }
        return toRet;
    }

    // m = 7, always for 7 days in a week
    private int mod(int x, int m) {
        return (x % m + m ) % m;
    }

    private int daysBetween(int start, int end) {
        return mod(end - start,7);
    }

    private void deleteAlarmIntent() {
        // cancel alarms created during this activity
        if (alarmManager != null) {
            // recreate the pending intent to cancel it.  This is required any time the user
            // tries to cancel an alarm after they left the original alarm creation activity
            versionSetPendingIntentAlarm();
            alarmManager.cancel(pendingIntent);
        }

        if (pendingIntent != null) {
            pendingIntent.cancel();
        }
    }

    private int minsSinceMidnight(int milTime) {
        double time = milTime / 100d;
        int hours = (int) Math.floor(time);
        int minutes = milTime % 100;
        return (hours * 60) + minutes;
    }

    public String findNextAlarm() {
        DBHelper dbH = new DBHelper(con);
        ArrayList<String> alarmId = new ArrayList<>();
        ArrayList<String> alarmMilTimes = new ArrayList<>();
        ArrayList<String> alarmDays = new ArrayList<>();
        ArrayList<String> onOffStates = new ArrayList<>();
        ArrayList<String> alarmNormTimes = new ArrayList<>();
        Cursor cursorRep = dbH.getAllDataAlarms();
        while (cursorRep.moveToNext()) {
            int id = cursorRep.getColumnIndex("ID3");
            int idr = cursorRep.getColumnIndex("MILTIME");
            int idd = cursorRep.getColumnIndex("DAYS");
            int ioo = cursorRep.getColumnIndex("ONOFF");
            int ian = cursorRep.getColumnIndex("PRTIME");

            // only care about the alarms set to on
            if (cursorRep.getString(ioo).equals("on")) {
                alarmId.add(cursorRep.getString(id));
                alarmMilTimes.add(cursorRep.getString(idr));
                alarmDays.add(cursorRep.getString(idd));
                onOffStates.add(cursorRep.getString(ioo));
                alarmNormTimes.add(cursorRep.getString(ian)); // ex. 8:08 pm instead of 2008
            }
        }
        cursorRep.close();
        dbH.close();

        if (alarmId.size() == 0) {
            deleteAlarmIntent();
            return "No Active Alarms";
        }

        Calendar cal = Calendar.getInstance();
        int day = cal.get(Calendar.DAY_OF_WEEK);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        // fix issue with minute < 10 causing time of 165 instead of 1605
        String minuteMil;
        if (minute < 10) {
            minuteMil = "0" + (minute);
        } else {
            minuteMil = String.valueOf(minute);
        }

        int milTimeCurrent = Integer.valueOf((hour) + minuteMil);

        int min = 1440 * 7 + 1;  // 1440 minutes in a day
        for (int i = 0; i < alarmDays.size(); i++) {  // loop through each db entry
            String entryTime = alarmMilTimes.get(i);
            String[] splitDays = alarmDays.get(i).split(" ");
            for (int j = 0; j < splitDays.length; j++) {
                int multiplier = daysBetween(day, transDayToNum(splitDays[j]));

                // mil time of 1910 - 1810 = 100 (need to consider minutes in a day until a military time)
                int minutes = minsSinceMidnight(Integer.valueOf(entryTime)) - minsSinceMidnight(milTimeCurrent);

                // if statement for alarms scheduled for current day for a past time
                if (multiplier == 0 && minutes < 0) {
                    multiplier = 7; // forward one week
                }

                // compute time difference in minutes
                // (full days between * 1440 mins per day) + (entry time in mins - current time in mins)
                int thisDifference = (multiplier * 1440) + minutes;

                if (thisDifference < min && thisDifference > 0) {
                    min = thisDifference;
                }
            }
        }

        // actually sets alarm using Android API
        setNextAlarm(min);
        return tempGetAlarm();
    }

    private String tempGetAlarm() {
        long time = 0;
        if (Build.VERSION.SDK_INT >= 21) {
            try {
                AlarmManager.AlarmClockInfo info = alarmManager.getNextAlarmClock();
                time = info.getTriggerTime();  // guarantees correct time to Android Alarm API level
            } catch (NullPointerException ignored) {
            }
        }

        Calendar current = Calendar.getInstance();
        long diff = time - current.getTimeInMillis();
        diff /= 1000; // to seconds
        diff /= 60; // to minutes
        return convertNextAlarmAwayTime((int)diff);
    }

    private String convertNextAlarmAwayTime(int m) {
        int days;
        int hours;
        int mins;
        int remaining;

        days = m / 1440;
        remaining = m - (days * 1440);

        hours = remaining / 60;
        mins = remaining - (hours * 60);

        String toRet;
        if (days > 0) {
            toRet = "Next Alarm: " + (days) + " days, " + (hours) +
                    " hrs, " + (mins) + " mins away";
        }
        else if (hours > 0) {
            toRet = "Next Alarm: " + (hours) + " hrs, " + (mins) + " mins away";
        }
        else {
            toRet = "Next Alarm: " + (mins) + " mins away";
        }

        return toRet;
    }

    private void versionSetPendingIntentAlarm() {
        Intent intent = new Intent(con, AlarmReceiver.class);
        if (Build.VERSION.SDK_INT >= 23) {
            pendingIntent = PendingIntent.getBroadcast(con, 116, intent, PendingIntent.FLAG_IMMUTABLE);
        } else {
            pendingIntent = PendingIntent.getBroadcast(con, 116, intent, 0);
        }
    }

    private void setNextAlarm(int time) {
        if (time != 0) {
            //Intent intent = new Intent(con, AlarmReceiver.class);
            versionSetPendingIntentAlarm();
            alarmManager = (AlarmManager) con.getSystemService(Context.ALARM_SERVICE);
            // IntentFilter iF = new IntentFilter(NEXT_ALARM_CLOCK_CHANGED);

            // time must converted from minutes to milliseconds to use setAlarmClock() method
            long t1 = System.currentTimeMillis();  // returns time since UTC (Midnight Jan 1 1970) in millisecond
            long t2 = (time * 60 * 1000) + t1;

            if (alarmManager != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    alarmManager.setAlarmClock(new AlarmManager.AlarmClockInfo(t2, pendingIntent), pendingIntent);
                }
                else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, t2, pendingIntent);
                }
                else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, t2, pendingIntent);
                }
            }
        }
    }
}
