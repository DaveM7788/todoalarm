package com.davesprojects.dm.alarm.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionsHelper {
    Context con;
    Activity activity;

    public PermissionsHelper(Context c, Activity a) {
        con = c;
        activity = a;
    }

    public void checkPermissions() {
        if (ContextCompat.checkSelfPermission(con, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_CALENDAR}, 1);
        }

        if (ContextCompat.checkSelfPermission(con, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 2);
        }
    }
}
