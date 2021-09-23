package com.davesprojects.dm.alarm.util;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.davesprojects.dm.alarm.ui.WakeUpActivity;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, Intent intent) {

        SharedPreferences.Editor prefEditor = context.getSharedPreferences("Preferences",
                Context.MODE_PRIVATE).edit();
        prefEditor.putBoolean("alarmOnOff", true);
        prefEditor.apply();

        Intent i = new Intent(context, WakeUpActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
        // flag new task required when calling startActivity in a Receiver
        // flag no history prevents user from clicking back button into the WakeUpActivity
        context.startActivity(i);
    }
}
