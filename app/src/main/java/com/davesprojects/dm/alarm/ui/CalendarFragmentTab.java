package com.davesprojects.dm.alarm.ui;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.CalendarContract;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.davesprojects.dm.alarm.R;
import com.davesprojects.dm.alarm.model.TodayCalEvents;
import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class CalendarFragmentTab extends Fragment implements View.OnClickListener {

    Context con;
    View myView;

    static CompactCalendarView compactCalendarView;

    private ListView mTaskListView;
    private ArrayAdapter<String> mAdapter;
    boolean okToDeleteTasks;
    TextView textMonth;

    ArrayList<String> titles = new ArrayList<>();
    ArrayList<Long> times = new ArrayList<>();
    ArrayList<Long> eventIds = new ArrayList<>();

    int timezoneDiff = 0;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.calendar_tab, container, false);
        con = myView.getContext();

        textMonth = myView.findViewById(R.id.textMonth);
        setInitialMonthTxt();

        compactCalendarView = myView.findViewById(R.id.compactcalendar_view);
        // Set first day of week to Monday, defaults to Monday so calling setFirstDayOfWeek is not necessary
        // Use constants provided by Java Calendar class
        compactCalendarView.setFirstDayOfWeek(Calendar.SUNDAY);
        compactCalendarView.setLocale(TimeZone.getDefault(), Locale.getDefault());

        //Toast.makeText(con, TimeZone.getDefault().getDisplayName(), Toast.LENGTH_SHORT).show();


        // define a listener to receive callbacks when certain events happen.
        compactCalendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
                //List<Event> events = compactCalendarView.getEvents(dateClicked);
                //populateListView(events);
                setLVForToday(dateClicked);
            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                int month = firstDayOfNewMonth.getMonth();
                int year = firstDayOfNewMonth.getYear();
                textMonth.setText(monthIntToString(month) + " " + (year + 1900));
            }
        });
        timeDiffUTCDefaultTZ();
        new AsyncTaskCal().execute(0L);


        mTaskListView = myView.findViewById(R.id.list_cal);
        // set it as the adapter of the ListView instance
        mTaskListView.setAdapter(mAdapter);

        mTaskListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String eventTitle = String.valueOf(mTaskListView.getItemAtPosition(position));
                if (eventTitle.contains("-")) {
                    // events actually displayed as Time - Event Title
                    // find first index of -
                    // sub string out the text after -
                    // find the text in titles array list, the position of it will match the event id position
                    int needle = titles.indexOf(eventTitle);

                    Long eventId = eventIds.get(needle);

                    Uri uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId);
                    Intent intent = new Intent(Intent.ACTION_VIEW)
                            .setData(uri)
                            .putExtra(CalendarContract.Events.TITLE, false);
                    // startActivity(intent);
                    startActivityForResult(intent, 199);  // see OnActivityResult
                }
            }
        });



        return myView;
    }

    private void timeDiffUTCDefaultTZ() {
        TimeZone tz = TimeZone.getDefault();
        timezoneDiff = tz.getRawOffset() * -1;
        //Toast.makeText(con, "raw off" + tz.getRawOffset(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        // will reset list view to correct value after opening the calendar activity
        setInitialListView();
    }

    public void setLVForToday(Date dateClicked) {
        List<Event> events = compactCalendarView.getEvents(dateClicked);
        populateListView(events);
    }

    // refresh calendar events after user clicks on an item in the cal task list view
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 199) {
            compactCalendarView.removeAllEvents();
            mAdapter.clear();
            mAdapter.notifyDataSetChanged();
            new AsyncTaskCal().execute(0L);
        }
    }

    public void setInitialMonthTxt() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);

        textMonth.setText(monthIntToString(month) + " " + (year));
    }

    public String monthIntToString(int month) {
        String mstr;
        switch(month) {
            case 0: mstr = "JAN"; break;
            case 1: mstr = "FEB"; break;
            case 2: mstr = "MAR"; break;
            case 3: mstr = "APR"; break;
            case 4: mstr = "MAY"; break;
            case 5: mstr = "JUN"; break;
            case 6: mstr = "JUL"; break;
            case 7: mstr = "AUG"; break;
            case 8: mstr = "SEP"; break;
            case 9: mstr = "OCT"; break;
            case 10: mstr = "NOV"; break;
            case 11: mstr = "DEC"; break;
            default: mstr = "Invalid"; break;
        }
        return mstr;
    }

    public void setInitialListView() {
        ArrayList<String> calTasksForToday = new TodayCalEvents(con).getTodayCalEvents();
        if (calTasksForToday.size() == 0) {
            calTasksForToday.add("No Calendar Events");
        }

        if (mAdapter == null) {
            mAdapter = new ArrayAdapter<>(con, android.R.layout.simple_list_item_activated_1, calTasksForToday);
            mTaskListView.setAdapter(mAdapter);
        } else {
            mAdapter.clear();
            mAdapter.addAll(calTasksForToday);
            mAdapter.notifyDataSetChanged();
        }
    }

    public void populateListView(List<Event> events) {
        ArrayList<String> oneDayEvents = new ArrayList<>();

        if (events.size() > 0) {
            for (int i = 0; i < events.size(); i++) {
                Event temp = events.get(i);
                oneDayEvents.add(temp.getData().toString());

                temp.getTimeInMillis();
            }
        } else {
            oneDayEvents.add("No Calendar Events");
        }

        if (mAdapter == null) {
            mAdapter = new ArrayAdapter<>(con, android.R.layout.simple_list_item_activated_1, oneDayEvents);
            mTaskListView.setAdapter(mAdapter);
        } else {
            mAdapter.clear();
            mAdapter.addAll(oneDayEvents);
            mAdapter.notifyDataSetChanged();
        }
    }


    @Override
    public void onClick(View view) {

    }

    public void calendarRun() {
        // Run query
        // gets the id of the default calendar
        long id = 0;

        String[] projection = new String[]{
                CalendarContract.Calendars._ID,
                CalendarContract.Calendars.NAME,
                CalendarContract.Calendars.ACCOUNT_NAME,
                CalendarContract.Calendars.ACCOUNT_TYPE};

        try {
            Cursor calCursor = con.getContentResolver().
                    query(CalendarContract.Calendars.CONTENT_URI,
                            projection,
                            CalendarContract.Calendars.VISIBLE + " = 1",
                            null,
                            CalendarContract.Calendars._ID + " ASC");
            if (calCursor.moveToFirst()) {
                do {
                    id = calCursor.getLong(0);
                } while (calCursor.moveToNext());
            }
            calCursor.close();
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        getCalendarEvents(id);
    }

    public int[] getCalendarTimeRange() {
        int[] range = new int[3];
        Calendar cal = Calendar.getInstance();
        int date = cal.get(Calendar.DAY_OF_MONTH);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);

        range[0] = date;
        range[1] = month;
        range[2] = year;

        return range;
    }

    public void getCalendarEvents(long calID) {
        final String DEBUG_TAG = "MyActivity";
        final String[] INSTANCE_PROJECTION = new String[] {
                CalendarContract.Instances.EVENT_ID,      // 0
                CalendarContract.Instances.BEGIN,         // 1
                CalendarContract.Instances.TITLE,          // 2
                CalendarContract.Instances.DTSTART,        // 3
                CalendarContract.Instances.START_MINUTE,   // 4
                CalendarContract.Instances.EVENT_TIMEZONE  // 5
        };

        final int PROJECTION_ID_INDEX = 0;
        final int PROJECTION_BEGIN_INDEX = 1;
        final int PROJECTION_TITLE_INDEX = 2;
        final int PROJECTION_DTSTART_INDEX = 3;
        final int PROJECTION_STARTMINUTE_INDEX = 4;
        final int PROJECTION_TZ = 5;

        int[] timeRange = getCalendarTimeRange();


        Calendar beginTime = Calendar.getInstance();
        //beginTime.set(2018, 9, 01, 8, 0);
        beginTime.set(timeRange[2], timeRange[1] - 1, timeRange[0]);
        long startMillis = beginTime.getTimeInMillis();
        Calendar endTime = Calendar.getInstance();
        //endTime.set(2018, 10, 24, 8, 0);
        // show 6 months out
        endTime.set(timeRange[2], timeRange[1] + 6, timeRange[0]);
        long endMillis = endTime.getTimeInMillis();

        Cursor cur;
        ContentResolver cr = con.getContentResolver();

        // Construct the query with the desired date range.
        Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(builder, startMillis);
        ContentUris.appendId(builder, endMillis);

        // Submit the query
        cur =  cr.query(builder.build(),
                INSTANCE_PROJECTION,
                null,
                null,
                null);

        String title;
        long eventID = 0;
        long beginVal = 0;
        //String dtstart = "";
        String startmin;
        String tz = "";

        titles.clear();
        times.clear();
        eventIds.clear();
        while (cur.moveToNext()) {
            // Get the field values
            eventID = cur.getLong(PROJECTION_ID_INDEX);
            beginVal = cur.getLong(PROJECTION_BEGIN_INDEX);
            title = cur.getString(PROJECTION_TITLE_INDEX);
            //dtstart = cur.getString(PROJECTION_DTSTART_INDEX);
            startmin = cur.getString(PROJECTION_STARTMINUTE_INDEX);
            tz = cur.getString(PROJECTION_TZ);

            // google calendar defaults holidays to UTC which cause the events to show up
            // a day early. the code below fixes this adding the rawOffSet between UTC and
            // the system default time zone (calendarView set as default timezone)

            // beginVal is not actually epoch time???
            if (tz.equals("UTC")) {
                beginVal += (timezoneDiff);
            }

            titles.add(convertMinsSinceMidnightToTime(startmin) + " - " + (title));
            times.add(beginVal);
            eventIds.add(eventID);
        }
        cur.close();
        populateCalendarWithEvents(titles, times);
    }

    public String convertMinsSinceMidnightToTime(String startmin) {
        int startminCalc = Integer.valueOf(startmin);
        int hour = startminCalc / 60;
        int minutes = startminCalc % 60;
        String amOrPM = "AM";
        String minutesStr = String.valueOf(minutes);

        if (hour >= 12) {
            amOrPM = "PM";
            if (hour > 12) {
                hour -= 12;  // prevents 8:30 PM being shown as 20:30 PM
            }
        }

        if (hour == 0) {
            hour = 12;  // prevents 12:04 AM being shown as 0:04 AM
        }

        if (minutes < 10) {
            minutesStr = "0" + minutesStr;
        }

        return (hour) + ":" + minutesStr + " " + amOrPM;
    }

    public void populateCalendarWithEvents(ArrayList<String> titles, ArrayList<Long> times) {
        List<Event> eventsCal = new ArrayList<>();
        for(int i = 0; i < titles.size(); i++) {
            eventsCal.add(new Event(Color.BLUE, times.get(i), titles.get(i)));
        }
        compactCalendarView.addEvents(eventsCal);
    }

    // ---------------------------------------
    public class AsyncTaskCal extends AsyncTask<Long, Long, List<CalendarContract.Events>> {
        @Override
        protected List<CalendarContract.Events> doInBackground(Long... longs) {
            calendarRun();
            return null;
        }
    }
}
