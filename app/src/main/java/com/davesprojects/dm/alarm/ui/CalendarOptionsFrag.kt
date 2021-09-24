package com.davesprojects.dm.alarm.ui

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.CalendarContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CheckedTextView
import android.widget.ListView
import androidx.fragment.app.Fragment
import com.davesprojects.dm.alarm.R

class CalendarOptionsFrag : Fragment() {

    private lateinit var con: Context
    private lateinit var myView: View
    private lateinit var calListView: ListView
    private lateinit var prefs: SharedPreferences
    private lateinit var calendarIdsToSave: MutableList<String>
    private var allCalendarsData = mutableListOf<CalOption>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        myView = inflater.inflate(R.layout.calendar_options, container, false)
        con = myView.context
        prefs = con.getSharedPreferences("Preferences", Context.MODE_PRIVATE)
        calendarIdsToSave = mutableListOf()

        calListView = myView.findViewById(R.id.listCalOptions)
        calListView.choiceMode = ListView.CHOICE_MODE_MULTIPLE
        val arrayAdapter = ArrayAdapter<String>(con, android.R.layout.simple_list_item_multiple_choice, findAvailCalendars())
        calListView.adapter = arrayAdapter
        findWhichCalendarsToCheckInit()

        calListView.onItemClickListener = object: AdapterView.OnItemClickListener {
            override fun onItemClick(p0: AdapterView<*>?, checkTextView: View?, position: Int, p3: Long) {

                val checked = (checkTextView as? CheckedTextView)?.isChecked
                val calId = allCalendarsData.getOrNull(position)?.calid

                if (checked == true) {
                    if (calId != null && !calendarIdsToSave.contains(calId)) calendarIdsToSave.add(calId)
                } else {
                    if (calendarIdsToSave.contains(calId)) calendarIdsToSave.remove(calId)
                }
            }

        }

        return myView
    }

    private fun findAvailCalendars() : List<String> {
        // Run query
        // gets the id of the default calendar
        val allCalendars = mutableListOf<String>()
        val projection = arrayOf(
                CalendarContract.Calendars._ID,
                CalendarContract.Calendars.NAME,
                CalendarContract.Calendars.ACCOUNT_NAME
        )

        try {
            val calCursor = con.contentResolver.query(CalendarContract.Calendars.CONTENT_URI,
                    projection,
                    CalendarContract.Calendars.VISIBLE + " = 1",
                    null,
                    CalendarContract.Calendars._ID + " ASC")

            if (calCursor?.moveToFirst() == true) {
                do {
                    val display = "${calCursor.getString(0)} - ${calCursor.getString(1)}"
                    allCalendars.add(display)
                    allCalendarsData.add(CalOption(calCursor.getString(0), display))
                } while (calCursor.moveToNext())
            }
            calCursor?.close()
        } catch (e: SecurityException) {
            e.printStackTrace()
        }

        return allCalendars
    }

    private fun findWhichCalendarsToCheckInit() {
        if (prefs.contains("calIdsToAdd")) {
            val temp = prefs.getString("calIdsToAdd", "")?.split(",")
            calendarIdsToSave = temp?.toMutableList() ?: mutableListOf()
        }

        for (id in calendarIdsToSave) {
            allCalendarsData.forEachIndexed { index, calOption ->
                if (calOption.calid == id) calListView.setItemChecked(index, true)
            }
        }
    }

    // save as string = csv
    private fun setCalendarsToUseOnTodo() {
        val allIds = StringBuilder()
        for (id in calendarIdsToSave) {
            allIds.append(id).append(",")
        }
        val asString = allIds.toString().removeSuffix(",")

        val prefEditor = con.getSharedPreferences("Preferences", Context.MODE_PRIVATE).edit()
        prefEditor.putString("calIdsToAdd", asString)
        prefEditor.apply()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        setCalendarsToUseOnTodo()
    }

    data class CalOption(var calid: String, var display: String) {}
}