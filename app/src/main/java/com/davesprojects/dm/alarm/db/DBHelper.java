package com.davesprojects.dm.alarm.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 9;
    public static final String DATABASE_NAME = "TASK2.db";
    public static final String TABLE_NAME = "task_table";
    public static final String COL_1 = "ID";
    public static final String COL_2 = "DESCTASK";

    public static final String TABLE2_NAME = "repeating";
    public static final String COL_3 = "DAYS";
    public static final String COL_4 = "DATEGO";

    public static final String TABLE3_NAME = "alarms";
    public static final String COL_TIME = "TIME";
    public static final String COL_PRETTYTIME = "PRTIME";
    public static final String COL_MILTIME = "MILTIME";
    public static final String COL_DAYS = "DAYS";
    public static final String COL_ONOFF = "ONOFF";

    public static final String TABLE4_NAME = "calendarTodayTasksAddedAlready";
    public static final String COL_DESCTASK = "DESCTASK";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME +
                " (ID INTEGER PRIMARY KEY AUTOINCREMENT, DESCTASK TEXT)");

        db.execSQL("create table " + TABLE2_NAME +
                " (ID2 INTEGER PRIMARY KEY AUTOINCREMENT, DAYS INTEGER, DATEGO TEXT)");

        db.execSQL("create table " + TABLE3_NAME +
                " (ID3 INTEGER PRIMARY KEY AUTOINCREMENT, TIME TEXT, PRTIME TEXT, MILTIME TEXT, DAYS TEXT, ONOFF TEXT)");

        db.execSQL("create table " + TABLE4_NAME +
                " (ID4 INTEGER PRIMARY KEY AUTOINCREMENT, DESCTASK TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE2_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE3_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE4_NAME);
        onCreate(db);
    }

    public boolean insertData(String description) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2, description);
        long result = db.insert(TABLE_NAME, null, contentValues);

        if(result == -1) {
            return false;
        } else {
            return true;
        }
    }

    public boolean insertDataRep(String description, int days, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2, description);
        contentValues.put(COL_3, days);
        contentValues.put(COL_4, date);
        long result = db.insert(TABLE2_NAME, null, contentValues);

        if(result == -1) {
            return false;
        } else {
            return true;
        }
    }

    public Cursor getAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery("select * from " + TABLE_NAME, null);
    }

    public Cursor getAllDataRep() {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery("select * from " + TABLE2_NAME, null);
    }

    public Cursor getAllDataAlarms() {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery("select * from " + TABLE3_NAME, null);
    }

    public Cursor getOneAlarm(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("ID3", id);
        return db.rawQuery("SELECT * FROM " + TABLE3_NAME + " WHERE ID3=?", new String[]{id});
    }

    public boolean updateOneAlarm(String id, String time, String prettytime, String miltime, String days, String onoff) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_TIME, time);
        contentValues.put(COL_PRETTYTIME, prettytime);
        contentValues.put(COL_MILTIME, miltime);
        contentValues.put(COL_DAYS, days);
        contentValues.put(COL_ONOFF, onoff);
        int result = db.update(TABLE3_NAME, contentValues, "ID3 = ?", new String[] {id});

        if (result == -1) {
            return false;
        } else {
            return true;
        }
    }

    public boolean updateData(String id, String description) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_1, id);
        contentValues.put(COL_2, description);
        db.update(TABLE_NAME, contentValues, "ID = ?", new String[] {id});
        return true;
    }

    public boolean updateDataRep(String id, String description, int days, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("ID2", id);
        contentValues.put(COL_2, description);
        contentValues.put(COL_3, days);
        contentValues.put(COL_4, date);
        db.update(TABLE2_NAME, contentValues, "ID2 = ?", new String[] {id});
        return true;
    }

    public Integer deleteData(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME, "ID = ?", new String[] {id});
    }

    public Integer deleteDataRep(String taskToDel) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE2_NAME, "DESC = ?", new String[] {taskToDel});
    }


    // Table 3 Methods
    // insert
    public boolean insertAlarmData(String time, String prettytime, String miltime, String days, String onoff) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_TIME, time);
        contentValues.put(COL_PRETTYTIME, prettytime);
        contentValues.put(COL_MILTIME, miltime);
        contentValues.put(COL_DAYS, days);
        contentValues.put(COL_ONOFF, onoff);
        long result = db.insert(TABLE3_NAME, null, contentValues);

        if (result == -1) {
            return false;
        } else {
            return true;
        }
    }

    // delete
    public Integer deleteAlarmData(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE3_NAME, "ID3 = ?", new String[] {id});
    }

    // change alarm state
    public Integer changeAlarmState(String id, String state) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_ONOFF, state);
        return db.update(TABLE3_NAME, contentValues, "ID3 = ?", new String[] {id});
    }



    // Table 4 Methods
    public boolean insertCalendarTodayTasks(String task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_DESCTASK, task);
        long result = db.insert(TABLE4_NAME, null, contentValues);

        if (result == -1) {
            return false;
        } else {
            return true;
        }
    }

    public Cursor getAllCalendarTodayTasks() {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery("select * from " + TABLE4_NAME, null);
    }

    public Integer refreshCalendarTasksDB() {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE4_NAME, null, null); //deletes all rows
    }
}
