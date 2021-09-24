package com.davesprojects.dm.alarm.ui

import android.widget.ArrayAdapter
import android.widget.TextView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import com.davesprojects.dm.alarm.R
import com.github.sundeepk.compactcalendarview.CompactCalendarView.CompactCalendarViewListener
import android.widget.AdapterView.OnItemClickListener
import android.content.ContentUris
import android.provider.CalendarContract
import android.content.Intent
import com.davesprojects.dm.alarm.model.TodayCalEvents
import android.content.Context
import android.database.Cursor
import android.graphics.Color
import android.view.View
import android.widget.ListView
import androidx.fragment.app.Fragment
import com.github.sundeepk.compactcalendarview.CompactCalendarView
import com.github.sundeepk.compactcalendarview.domain.Event
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class CalendarFragmentTab : Fragment(), View.OnClickListener {

    companion object {
        lateinit var compactCalendarView: CompactCalendarView
    }

    private lateinit var con: Context
    private lateinit var myView: View
    private lateinit var mTaskListView: ListView
    private var mAdapter: ArrayAdapter<String>? = null
    private lateinit var textMonth: TextView
    private var titles = ArrayList<String>()
    private var times = ArrayList<Long>()
    private var eventIds = ArrayList<Long>()
    private var timezoneDiff = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        myView = inflater.inflate(R.layout.calendar_tab, container, false)
        con = myView.context
        textMonth = myView.findViewById(R.id.textMonth)
        setInitialMonthTxt()
        compactCalendarView = myView.findViewById(R.id.compactcalendar_view)
        // Set first day of week to Monday, defaults to Monday so calling setFirstDayOfWeek is not necessary
        // Use constants provided by Java Calendar class
        compactCalendarView.setFirstDayOfWeek(Calendar.SUNDAY)
        compactCalendarView.setLocale(TimeZone.getDefault(), Locale.getDefault())


        // define a listener to receive callbacks when certain events happen.
        compactCalendarView.setListener(object : CompactCalendarViewListener {
            override fun onDayClick(dateClicked: Date) {
                setLVForToday(dateClicked)
            }

            override fun onMonthScroll(firstDayOfNewMonth: Date) {
                val month = firstDayOfNewMonth.month
                val year = firstDayOfNewMonth.year
                textMonth.text = getString(R.string.cal_month_top, monthIntToString(month), (year + 1900).toString())
            }
        })
        timeDiffUTCDefaultTZ()

        CoroutineScope(Dispatchers.IO).launch {
            calendarRun()
        }

        mTaskListView = myView.findViewById(R.id.list_cal)
        setInitialListView()
        // set it as the adapter of the ListView instance
        mTaskListView.adapter = mAdapter
        mTaskListView.setOnItemClickListener(OnItemClickListener { _, _, position, _ ->
            val eventTitle = mTaskListView.getItemAtPosition(position).toString()
            if (eventTitle.contains("-")) {
                // events actually displayed as Time - Event Title
                // find first index of -
                // sub string out the text after -
                // find the text in titles array list, the position of it will match the event id position
                val needle = titles.indexOf(eventTitle)
                val eventId = eventIds[needle]
                val uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId)
                val intent = Intent(Intent.ACTION_VIEW)
                        .setData(uri)
                        .putExtra(CalendarContract.Events.TITLE, false)
                startActivityForResult(intent, 199) // see OnActivityResult
            }
        })
        return myView
    }

    private fun timeDiffUTCDefaultTZ() {
        val tz = TimeZone.getDefault()
        timezoneDiff = tz.rawOffset * -1
    }

    override fun onResume() {
        super.onResume()
        // will reset list view to correct value after opening the calendar activity
        setInitialListView()
    }

    fun setLVForToday(dateClicked: Date?) {
        val events = compactCalendarView.getEvents(dateClicked)
        populateListView(events)
    }

    // refresh calendar events after user clicks on an item in the cal task list view
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 199) {
            compactCalendarView.removeAllEvents()
            mAdapter?.clear()
            mAdapter?.notifyDataSetChanged()
            calendarRun()
        }
    }

    private fun setInitialMonthTxt() {
        val cal = Calendar.getInstance()
        val year = cal[Calendar.YEAR]
        val month = cal[Calendar.MONTH]
        textMonth.text = getString(R.string.cal_month_top, monthIntToString(month), (year + 1900).toString())
    }

    fun monthIntToString(month: Int): String {
        return when (month) {
            0 -> "JAN"
            1 -> "FEB"
            2 -> "MAR"
            3 -> "APR"
            4 -> "MAY"
            5 -> "JUN"
            6 -> "JUL"
            7 -> "AUG"
            8 -> "SEP"
            9 -> "OCT"
            10 -> "NOV"
            11 -> "DEC"
            else -> "Invalid"
        }
    }

    private fun setInitialListView() {
        val calTasksForToday = TodayCalEvents(con).getTodayCalEvents(false)
        if (calTasksForToday.size == 0) {
            calTasksForToday.add("No Calendar Events")
        }
        if (mAdapter == null) {
            mAdapter = ArrayAdapter(con, android.R.layout.simple_list_item_activated_1, calTasksForToday)
            mTaskListView.adapter = mAdapter
        } else {
            mAdapter?.clear()
            mAdapter?.addAll(calTasksForToday)
            mAdapter?.notifyDataSetChanged()
        }
    }

    private fun populateListView(events: List<Event>) {
        val oneDayEvents = ArrayList<String>()
        if (events.isNotEmpty()) {
            for (i in events.indices) {
                val temp = events[i]
                oneDayEvents.add(temp.data.toString())
                temp.timeInMillis
            }
        } else {
            oneDayEvents.add("No Calendar Events")
        }
        if (mAdapter == null) {
            mAdapter = ArrayAdapter(con, android.R.layout.simple_list_item_activated_1, oneDayEvents)
            mTaskListView.adapter = mAdapter
        } else {
            mAdapter?.clear()
            mAdapter?.addAll(oneDayEvents)
            mAdapter?.notifyDataSetChanged()
        }
    }

    override fun onClick(view: View) {}

    private fun calendarRun() {
        // Run query
        // gets the id of the default calendar
        var id: Long = 0
        val projection = arrayOf(
                CalendarContract.Calendars._ID,
                CalendarContract.Calendars.NAME,
                CalendarContract.Calendars.ACCOUNT_NAME,
                CalendarContract.Calendars.ACCOUNT_TYPE)
        try {
            val calCursor = con.contentResolver.query(CalendarContract.Calendars.CONTENT_URI,
                    projection,
                    CalendarContract.Calendars.VISIBLE + " = 1",
                    null,
                    CalendarContract.Calendars._ID + " ASC")
            if (calCursor!!.moveToFirst()) {
                do {
                    id = calCursor.getLong(0)
                } while (calCursor.moveToNext())
            }
            calCursor.close()
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
        getCalendarEvents(id)
    }

    private val calendarTimeRange: IntArray
        get() {
            val range = IntArray(3)
            val cal = Calendar.getInstance()
            val date = cal[Calendar.DAY_OF_MONTH]
            val year = cal[Calendar.YEAR]
            val month = cal[Calendar.MONTH]
            range[0] = date
            range[1] = month
            range[2] = year
            return range
        }

    private fun getCalendarEvents(calID: Long) {
        val INSTANCE_PROJECTION = arrayOf(
                CalendarContract.Instances.EVENT_ID,  // 0
                CalendarContract.Instances.BEGIN,  // 1
                CalendarContract.Instances.TITLE,  // 2
                CalendarContract.Instances.DTSTART,  // 3
                CalendarContract.Instances.START_MINUTE,  // 4
                CalendarContract.Instances.EVENT_TIMEZONE // 5
        )
        val PROJECTION_ID_INDEX = 0
        val PROJECTION_BEGIN_INDEX = 1
        val PROJECTION_TITLE_INDEX = 2
        val PROJECTION_DTSTART_INDEX = 3
        val PROJECTION_STARTMINUTE_INDEX = 4
        val PROJECTION_TZ = 5
        val timeRange = calendarTimeRange
        val beginTime = Calendar.getInstance()
        beginTime[timeRange[2], timeRange[1] - 1] = timeRange[0]
        val startMillis = beginTime.timeInMillis
        val endTime = Calendar.getInstance()
        // show 6 months out
        endTime[timeRange[2], timeRange[1] + 6] = timeRange[0]
        val endMillis = endTime.timeInMillis
        val contentResolver = con.contentResolver

        // Construct the query with the desired date range.
        val builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
        ContentUris.appendId(builder, startMillis)
        ContentUris.appendId(builder, endMillis)

        // Submit the query
        val cur: Cursor? = contentResolver.query(builder.build(),
                INSTANCE_PROJECTION,
                null,
                null,
                null)

        var title: String
        var eventID: Long = 0
        var beginVal: Long = 0
        var startmin: String?
        var tz = ""
        titles.clear()
        times.clear()
        eventIds.clear()
        while (cur?.moveToNext() == true) {
            // Get the field values
            eventID = cur.getLong(PROJECTION_ID_INDEX)
            beginVal = cur.getLong(PROJECTION_BEGIN_INDEX)
            title = cur.getString(PROJECTION_TITLE_INDEX)
            //dtstart = cur.getString(PROJECTION_DTSTART_INDEX);
            startmin = cur.getString(PROJECTION_STARTMINUTE_INDEX)
            tz = cur.getString(PROJECTION_TZ)

            // google calendar defaults holidays to UTC which cause the events to show up
            // a day early. the code below fixes this adding the rawOffSet between UTC and
            // the system default time zone (calendarView set as default timezone)

            // beginVal is not actually epoch time???
            if (tz == "UTC") {
                beginVal += timezoneDiff.toLong()
            }
            titles.add(convertMinsSinceMidnightToTime(startmin) + " - " + title)
            times.add(beginVal)
            eventIds.add(eventID)
        }
        cur?.close()
        populateCalendarWithEvents(titles, times)
    }

    private fun convertMinsSinceMidnightToTime(startmin: String?): String {
        val startminCalc = Integer.valueOf(startmin)
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
        if (hour == 0) {
            hour = 12 // prevents 12:04 AM being shown as 0:04 AM
        }
        if (minutes < 10) {
            minutesStr = "0$minutesStr"
        }
        return "$hour:$minutesStr $amOrPM"
    }

    private fun populateCalendarWithEvents(titles: ArrayList<String>, times: ArrayList<Long>) {
        val eventsCal: MutableList<Event> = ArrayList()
        for (i in titles.indices) {
            eventsCal.add(Event(Color.BLUE, times[i], titles[i]))
        }
        compactCalendarView.addEvents(eventsCal)
    }
}