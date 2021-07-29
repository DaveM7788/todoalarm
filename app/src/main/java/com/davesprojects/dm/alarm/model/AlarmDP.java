package com.davesprojects.dm.alarm.model;

import java.util.ArrayList;


public class AlarmDP {
    private String id;
    private String times;
    private String days;
    private String onoff;

    public AlarmDP(String mId, String mDesc, String mDays, String mOnOff) {
        id = mId;
        times = mDesc;
        days = mDays;
        onoff = mOnOff;
    }

    public String getId() {
        return id;
    }

    public String getTime() {
        return times;
    }

    public String getDays() {
        return days;
    }

    public String getOnoff() {
        return onoff;
    }

    public static ArrayList<AlarmDP> createAlarmDPList(ArrayList<String> ids, ArrayList<String> times, ArrayList<String> days, ArrayList<String> onoffs) {
        ArrayList<AlarmDP> alarms = new ArrayList<>();
        for (int i = 0; i <= ids.size() - 1; i++) {
            alarms.add(new AlarmDP(ids.get(i), times.get(i), days.get(i), onoffs.get(i)));
        }
        return alarms;
    }
}
