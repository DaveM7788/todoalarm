package com.davesprojects.dm.alarm.ui;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.Nullable;

import com.davesprojects.dm.alarm.model.AlarmDP;
import com.davesprojects.dm.alarm.util.AlarmHandler;
import com.davesprojects.dm.alarm.util.AlarmReceiver;
import com.davesprojects.dm.alarm.R;
import com.davesprojects.dm.alarm.adapters.RecyclerAdapterAlarms;
import com.davesprojects.dm.alarm.db.DBHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.os.Handler;

import java.util.ArrayList;
import java.util.Calendar;



public class AlarmFrag extends Fragment implements View.OnClickListener {
    Context con;
    View myView;
    TextView nextAlarmTV;
    FloatingActionButton fab;

    public RecyclerView recyclerView;
    public RecyclerView.LayoutManager layoutManager;
    public RecyclerView.Adapter adapter;
    ArrayList<AlarmDP> alarms;
    private DBHelper dbH;
    AlarmHandler alarmHandler;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.alarms_recycler_main, container, false);
        con = myView.getContext();

        alarmHandler = new AlarmHandler(myView.getContext());

        // for back stack
        SharedPreferences.Editor prefEditor = con.getSharedPreferences("Preferences",
                Context.MODE_PRIVATE).edit();
        prefEditor.putString("lastFrag", "AlarmFrag");
        prefEditor.apply();

        alarms = dbAlarmRefresh();

        nextAlarmTV = myView.findViewById(R.id.nextAlarmTV);

        recyclerView = myView.findViewById(R.id.recycler_view);
        adapter = new RecyclerAdapterAlarms(con, alarms, nextAlarmTV);
        layoutManager = new LinearLayoutManager(con);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        fab = myView.findViewById(R.id.fAB);
        fab.setOnClickListener(this);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 || dy < 0 && fab.isShown()) {
                    fab.hide();
                }
            }

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {

                if (newState == RecyclerView.SCROLL_STATE_IDLE){
                    // wait for 1.1 seconds
                    final Handler aniHandler = new Handler();
                    aniHandler.postDelayed(new Runnable(){
                        public void run(){
                            fab.show();
                        }
                    }, 1100);
                }
                super.onScrollStateChanged(recyclerView, newState);
            }

        });

        return myView;
    }

    @Override
    public void onResume() {
        super.onResume();
        String nextAlarmTime = alarmHandler.findNextAlarm();
        nextAlarmTV.setText(nextAlarmTime);
    }

    public ArrayList<AlarmDP> dbAlarmRefresh() {
        dbH = new DBHelper(con);
        ArrayList<String> alarmId = new ArrayList<>();
        ArrayList<String> alarmTimes = new ArrayList<>();
        ArrayList<String> alarmDays = new ArrayList<>();
        ArrayList<String> onOffStates = new ArrayList<>();
        Cursor cursorRep = dbH.getAllDataAlarms();

        while (cursorRep.moveToNext()) {
            int id = cursorRep.getColumnIndex("ID3");
            int idr = cursorRep.getColumnIndex("PRTIME");
            int idd = cursorRep.getColumnIndex("DAYS");
            int ioo = cursorRep.getColumnIndex("ONOFF");
            alarmId.add(cursorRep.getString(id));
            alarmTimes.add(cursorRep.getString(idr));
            alarmDays.add(cursorRep.getString(idd));
            onOffStates.add(cursorRep.getString(ioo));
        }

        dbH.close();
        return AlarmDP.createAlarmDPList(alarmId, alarmTimes, alarmDays, onOffStates);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.fAB) {
            addAlarm();
        }
    }

    public void addAlarm() {
        final int[] daysState = {0, 0, 0, 0, 0, 0, 0};

        View checkBoxView = View.inflate(con, R.layout.checkbox_alarm, null);
        final TextView sun = checkBoxView.findViewById(R.id.sun);
        final TextView mon = checkBoxView.findViewById(R.id.mon);
        final TextView tue = checkBoxView.findViewById(R.id.tue);
        final TextView wed = checkBoxView.findViewById(R.id.wed);
        final TextView thur = checkBoxView.findViewById(R.id.thur);
        final TextView fri = checkBoxView.findViewById(R.id.fri);
        final TextView sat = checkBoxView.findViewById(R.id.sat);

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

        AlertDialog dialog = new AlertDialog.Builder(con)
                .setTitle("Add a New Alarm")
                .setMessage("Please enter the alarm info")
                .setView(checkBoxView)
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
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
                            if (timeHour == 0) {
                                timeHourStr = "12";  // want 12:04 AM not 0:04AM
                            }
                            amPM = " AM";
                        }

                        // need to turn into a String Builder
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
                            String setTime = (timeHour) + "," + (timeMinute);

                            String alarmSetString = timeHourStr + ":" + timeMinStr + amPM;
                            dbH.insertAlarmData(setTime, alarmSetString, militaryTime, realDayStr, "on");
                            dbH.close();

                            alarms = dbAlarmRefresh();
                            adapter = new RecyclerAdapterAlarms(con, alarms, nextAlarmTV);
                            recyclerView.setAdapter(adapter);

                            turnOnAlarm();
                        } else {
                            Toast.makeText(myView.getContext(),
                                    "Error: Select at least one day for the alarm", Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
        dialog.show();
    }

    public void turnOnAlarm() {
        String tv = alarmHandler.findNextAlarm();
        nextAlarmTV.setText(tv);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dbH != null) {
            dbH.close();
        }
    }
}
