package com.davesprojects.dm.alarm.util;

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import androidx.appcompat.app.AlertDialog;
import android.view.View;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.davesprojects.dm.alarm.R;
import com.davesprojects.dm.alarm.adapters.RecyclerAdapterAlarms;
import com.davesprojects.dm.alarm.db.DBHelper;
import com.davesprojects.dm.alarm.model.AlarmDP;
import com.davesprojects.dm.alarm.util.AlarmHandler;

import java.util.ArrayList;
import java.util.Calendar;

public class AlarmUpdate {
    private Context con;
    private DBHelper dbH;

    public String fieldTime;
    public String fieldOnOff;
    public String fieldDays;

    public ArrayList<AlarmDP> alarmDPS;
    public RecyclerAdapterAlarms recyclerAdapterAlarms;
    public int positionField;
    public String alarmIdField;

    public TextView nextAlarmTV;

    public AlarmUpdate(Context context, ArrayList<AlarmDP> alarms, RecyclerAdapterAlarms adapter, int pos, String alarmId, TextView nextAlarm) {
        con = context;
        dbH = new DBHelper(con);
        alarmDPS = alarms;
        recyclerAdapterAlarms = adapter;
        positionField = pos;
        alarmIdField = alarmId;
        nextAlarmTV = nextAlarm;
    }

    private int[] formatTime(String time) {
        int[] retHourMinAM = new int[2];
        String[] hoursMins = time.split(",");
        retHourMinAM[0] = Integer.valueOf(hoursMins[0]);
        retHourMinAM[1] = Integer.valueOf(hoursMins[1]);

        return retHourMinAM;
    }

    public String[] updateAlarm(final String alarmId) {
        // display checkbox with correct id info
        final String[] returnNewData = new String[2];

        String days = "";
        String time = "";

        Cursor cursor = dbH.getOneAlarm(alarmId);
        while (cursor.moveToNext()) {
            int idd = cursor.getColumnIndex("DAYS");
            int idt = cursor.getColumnIndex("TIME");
            time = cursor.getString(idt);
            days = cursor.getString(idd);
        }
        cursor.close();
        dbH.close();


        final int[] daysState = {0, 0, 0, 0, 0, 0, 0};
        View checkBoxView = View.inflate(con, R.layout.checkbox_alarm, null);
        final TextView sun = checkBoxView.findViewById(R.id.sun);
        final TextView mon = checkBoxView.findViewById(R.id.mon);
        final TextView tue = checkBoxView.findViewById(R.id.tue);
        final TextView wed = checkBoxView.findViewById(R.id.wed);
        final TextView thur = checkBoxView.findViewById(R.id.thur);
        final TextView fri = checkBoxView.findViewById(R.id.fri);
        final TextView sat = checkBoxView.findViewById(R.id.sat);



        String[] daysArr = days.split(" ");
        for (int i = 0; i < daysArr.length; i++) {
            switch (daysArr[i]) {
                case "Su": daysState[0] = 1; sun.setTextColor(Color.RED); break;
                case "Mo": daysState[1] = 1; mon.setTextColor(Color.RED); break;
                case "Tu": daysState[2] = 1; tue.setTextColor(Color.RED); break;
                case "We": daysState[3] = 1; wed.setTextColor(Color.RED); break;
                case "Th": daysState[4] = 1; thur.setTextColor(Color.RED); break;
                case "Fr": daysState[5] = 1; fri.setTextColor(Color.RED); break;
                case "Sa": daysState[6] = 1; sat.setTextColor(Color.RED); break;
                default: break;
            }
        }

        sun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int state = daysState[0];
                if (state == 0) {
                    sun.setTextColor(Color.RED);
                    daysState[0] = 1;
                } else {
                    sun.setTextColor(Color.GRAY);
                    daysState[0] = 0;
                }
            }
        });
        mon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int state = daysState[1];
                if (state == 0) {
                    mon.setTextColor(Color.RED);
                    daysState[1] = 1;
                } else {
                    mon.setTextColor(Color.GRAY);
                    daysState[1] = 0;
                }
            }
        });
        tue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int state = daysState[2];
                if (state == 0) {
                    tue.setTextColor(Color.RED);
                    daysState[2] = 1;
                } else {
                    tue.setTextColor(Color.GRAY);
                    daysState[2] = 0;
                }
            }
        });
        wed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int state = daysState[3];
                if (state == 0) {
                    wed.setTextColor(Color.RED);
                    daysState[3] = 1;
                } else {
                    wed.setTextColor(Color.GRAY);
                    daysState[3] = 0;
                }
            }
        });
        thur.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int state = daysState[4];
                if (state == 0) {
                    thur.setTextColor(Color.RED);
                    daysState[4] = 1;
                } else {
                    thur.setTextColor(Color.GRAY);
                    daysState[4] = 0;
                }
            }
        });
        fri.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int state = daysState[5];
                if (state == 0) {
                    fri.setTextColor(Color.RED);
                    daysState[5] = 1;
                } else {
                    fri.setTextColor(Color.GRAY);
                    daysState[5] = 0;
                }
            }
        });
        sat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int state = daysState[6];
                if (state == 0) {
                    sat.setTextColor(Color.RED);
                    daysState[6] = 1;
                } else {
                    sat.setTextColor(Color.GRAY);
                    daysState[6] = 0;
                }
            }
        });

        final TimePicker checkTimePicker = checkBoxView.findViewById(R.id.time_select);
        int[] timePickerData = formatTime(time);
        if (Build.VERSION.SDK_INT >= 23) {
            checkTimePicker.setHour(timePickerData[0]);  // auto handle AM PM and military time
            checkTimePicker.setMinute(timePickerData[1]);
        }

        AlertDialog dialog = new AlertDialog.Builder(con)
                .setTitle("Update Alarm")
                .setMessage("Please update the alarm info")
                .setView(checkBoxView)
                .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(Calendar.HOUR_OF_DAY, checkTimePicker.getCurrentHour());
                        calendar.set(Calendar.MINUTE, checkTimePicker.getCurrentMinute());

                        int timeHour = checkTimePicker.getCurrentHour();
                        int timeMinute = checkTimePicker.getCurrentMinute();
                        String timeHourStr = String.valueOf(timeHour);
                        String timeMinStr = String.valueOf(timeMinute);

                        if (timeMinute < 10) {
                            timeMinStr = "0" + timeMinute;
                        }

                        String militaryTime = timeHourStr + timeMinStr;

                        // android uses military time as default, convert to normal time for user interface
                        String amPM;
                        if (timeHour >= 12) {
                            amPM = " PM";
                            if (timeHour == 12) {
                                timeHourStr = "12";
                            } else {
                                timeHourStr = String.valueOf(timeHour - 12);
                            }
                        } else {
                            amPM = " AM";
                        }

                        StringBuilder days = new StringBuilder();
                        for (int i = 0; i < daysState.length; ++i) {
                            if (daysState[i] == 1) {
                                // alarm scheduled to go off on that day
                                switch (i) {
                                    case 0: days.append("Su "); break;
                                    case 1: days.append("Mo "); break;
                                    case 2: days.append("Tu "); break;
                                    case 3: days.append("We "); break;
                                    case 4: days.append("Th "); break;
                                    case 5: days.append("Fr "); break;
                                    case 6: days.append("Sa "); break;
                                    default: break;
                                }
                            }
                        }

                        // remove last char which is a |space|
                        String realDayStr = days.toString();
                        if (realDayStr.length() > 0) {
                            realDayStr = realDayStr.substring(0, days.length() - 1);
                            String setTime = timeHour + "," + timeMinute;

                            String alarmSetString = timeHourStr + ":" + timeMinStr + amPM;
                            dbH.updateOneAlarm(alarmId, setTime, alarmSetString, militaryTime, realDayStr, "on");
                            dbH.close();

                            fieldDays = realDayStr;
                            fieldOnOff = "on";
                            fieldTime = alarmSetString;

                            returnNewData[0] = alarmSetString;
                            returnNewData[1] = realDayStr;
                        } else {
                            Toast.makeText(con,
                                    "Error: Select at least one day for the alarm", Toast.LENGTH_LONG).show();
                        }

                        // check if update causes a new "Most Recent Alarm"
                        AlarmHandler aH = new AlarmHandler(con);
                        String tv = aH.findNextAlarm();
                        nextAlarmTV.setText(tv);

                        AlarmDP newAlarmInfo = new AlarmDP(alarmIdField, returnNewData[0], returnNewData[1], "on");
                        alarmDPS.set(positionField, newAlarmInfo);
                        recyclerAdapterAlarms.notifyItemChanged(positionField);

                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
        dialog.show();

        return returnNewData;
    }
}
