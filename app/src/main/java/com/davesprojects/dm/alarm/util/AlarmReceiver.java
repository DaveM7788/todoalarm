package com.davesprojects.dm.alarm.util;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.util.Log;

import com.davesprojects.dm.alarm.ui.WakeUpActivity;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.d("newData", "Alarm Receiver");

        SharedPreferences.Editor prefEditor = context.getSharedPreferences("Preferences",
                Context.MODE_PRIVATE).edit();
        prefEditor.putBoolean("alarmOnOff", true);
        prefEditor.apply();


        /*
        Intent intentIntermediate = new Intent();
        intentIntermediate.setClassName("com.davesprojects.dm.alarm", "com.davesprojects.dm.alarm.Intermediate");
        intentIntermediate.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intentIntermediate);
        */



        // start the intermediate frag once the alarm turns on ; )
        /*
        Intent intentTodo = new Intent();
        intentTodo.setClassName("com.davesprojects.dm.alarm", "com.davesprojects.dm.alarm.ui.MainActivity");
        intentTodo.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // the extra code below lets the Main Activity know it is being started from
        // the AlarmReceiver class.  It will wake up the user and then open a fragment to
        // automatically speak out the TodoList for this case
        String fromAlarm = "fromAlarm";
        intentTodo.putExtra("FROM_ALARM", fromAlarm);
        Log.d("newData", "starting activity");
        Bundle bundle = intent.getExtras();
        if (Build.VERSION.SDK_INT >= 16) {
            context.startActivity(intentTodo, bundle);
        }

         */

        Intent i = new Intent(context, WakeUpActivity.class);
        //i.setClassName("com.davesprojects.dm.alarm", "com.davesprojects.dm.alarm.ui.WakeUpActivity");
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
        // flag new task required when calling startActivity in a Receiver
        // flag no history prevents user from clicking back button into the WakeUpActivity
        context.startActivity(i);
    }
}
