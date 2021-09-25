package com.davesprojects.dm.alarm.model

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.provider.CalendarContract
import com.davesprojects.dm.alarm.util.PermissionsHelper
import java.lang.Exception
import java.util.*

class CalEventHelp(private val con: Context) {

    private var calIds: Array<String>

    init {
        val prefsF = con.getSharedPreferences("Preferences", Context.MODE_PRIVATE)
        calIds = if (prefsF.contains("calIdsToAdd")) {
            prefsF.getString("calIdsToAdd", "")?.split(",".toRegex())?.toTypedArray() ?: emptyArray()
        } else {
            emptyArray()
        }
    }

    fun getTodayCalEvents(considerCalOpt: Boolean): ArrayList<String> {
        return getCalEventsForADay(considerCalOpt, null)
    }

    fun getCalEventsForADay(considerCalOpt: Boolean, whichDay: Date?): ArrayList<String> {
        val INSTANCE_PROJECTION = arrayOf(
                CalendarContract.Instances.EVENT_ID,  // 0
                CalendarContract.Instances.BEGIN,  // 1
                CalendarContract.Instances.TITLE,  // 2
                CalendarContract.Instances.DTSTART,
                CalendarContract.Instances.START_MINUTE,
                CalendarContract.Instances.CALENDAR_ID
        )

        // other indexes not used
        val PROJECTION_TITLE_INDEX = 2
        val PROJECTION_STARTMINUTE_INDEX = 4
        val PROJECTION_ID_INDEX = 5

        val beginTime = Calendar.getInstance()
        var date = 0
        var month = 0
        var year = 0
        whichDay?.let {
            date = it.date
            month = it.month
            year = it.year + 1900
        } ?: kotlin.run {
            // get calendar events for today
            date = beginTime[Calendar.DAY_OF_MONTH]
            year = beginTime[Calendar.YEAR]
            month = beginTime[Calendar.MONTH]
        }
        beginTime[year, month, date, 0] = 0
        val endTime = Calendar.getInstance()
        endTime[year, month, date + 1, 0] = 0
        val startMillis = beginTime.timeInMillis
        val endMillis = endTime.timeInMillis

        // Construct the query with the desired date range.
        val builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
        ContentUris.appendId(builder, startMillis)
        ContentUris.appendId(builder, endMillis)
        var cursor: Cursor? = null
        val cr = con.contentResolver

        try {
            cursor = cr.query(builder.build(),
                    INSTANCE_PROJECTION,
                    null,
                    null,
                    null)
        } catch (e: Exception) { }

        val calTasksForToday = ArrayList<String>()
        cursor?.let {
            while (cursor.moveToNext()) {
                // Get the field values
                val title = cursor.getString(PROJECTION_TITLE_INDEX)
                val startmin = cursor.getString(PROJECTION_STARTMINUTE_INDEX)
                val calIdOne = cursor.getString(PROJECTION_ID_INDEX)
                if (considerCalOpt) {
                    for (id in calIds) {
                        if (id == calIdOne) {
                            calTasksForToday.add(convertMinsSinceMidnightToTime(startmin) + " - " + title)
                            break
                        }
                    }
                } else {
                    calTasksForToday.add(convertMinsSinceMidnightToTime(startmin) + " - " + title)
                }
            }
            cursor.close()
        }
        return calTasksForToday
    }

    private fun convertMinsSinceMidnightToTime(startmin: String): String {
        val startminCalc = startmin.toInt()
        var hour = startminCalc / 60
        val minutes = startminCalc % 60
        var amOrPM = "AM"
        var minutesStr = minutes.toString()
        if (hour >= 12) {
            amOrPM = "PM"
            if (hour > 12) {
                hour -= 12 // prevents 8:30 PM being shown as 20:30 PM
            }
        }
        if (minutes < 10) {
            minutesStr = "0$minutesStr"
        }
        return "$hour:$minutesStr $amOrPM"
    }
}