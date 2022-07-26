package com.davesprojects.dm.alarm.ui

import android.content.ContentUris
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.davesprojects.dm.alarm.R
import android.content.Intent
import android.database.Cursor
import android.provider.CalendarContract
import android.speech.tts.TextToSpeech
import android.view.View
import android.widget.Toast
import com.davesprojects.dm.alarm.db.DBHelper
import com.davesprojects.dm.alarm.notifs.MusicAlarmSoundService
import com.davesprojects.dm.alarm.util.AlarmHandler
import java.lang.Exception
import java.lang.StringBuilder
import java.util.*

class WakeUpIntermediate : AppCompatActivity(), TextToSpeech.OnInitListener {

    lateinit var btnContinue: Button
    lateinit var btnContinueKeepSong: Button
    var tts: TextToSpeech? = null
    private var dbH: DBHelper? = null
    var speakToDoList = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.intermediate_new_android)

        val prefs = getSharedPreferences("Preferences", MODE_PRIVATE)

        btnContinue = findViewById(R.id.btnContinue)
        btnContinue.setOnClickListener {
            killAlarmMusicService()
        }

        btnContinueKeepSong = findViewById(R.id.btnContinueKeepPlaying)
        if (prefs.contains("wakeUpToSong")) {
            val wakeUpToSong = prefs.getBoolean("wakeUpToSong", false)
            if (wakeUpToSong) {
                btnContinueKeepSong.visibility = View.VISIBLE
                btnContinueKeepSong.setOnClickListener {
                    intentToMainActivity()
                }
            }
        }

        if (prefs.contains("alarmOnOff")) {
            val state = prefs.getBoolean("alarmOnOff", false)
            if (state) {
                val prefEditor = getSharedPreferences(
                    "Preferences",
                    MODE_PRIVATE
                ).edit()
                prefEditor.putBoolean("alarmOnOff", false)
                prefEditor.apply()
            }
        }

        if (prefs.contains("todoSpeak")) {
            speakToDoList = prefs.getBoolean("todoSpeak", false)
            // if speakToDoList = true, stopRingtone() will call function to speak to do list before
            // finishing
            if (tts == null) {
                tts = TextToSpeech(this, this)
            }
        }

        dbH = DBHelper(this)
    }

    private fun killAlarmMusicService() {
        try {
            val iEnd = Intent(baseContext, MusicAlarmSoundService::class.java)
            iEnd.action = "EndService"
            baseContext.startService(iEnd)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        intentToMainActivity()
    }

    private fun intentToMainActivity() {
        setNextAlarm()

        if (speakToDoList) {
            var nameOfUser = ""
            val prefs = getSharedPreferences("Preferences", MODE_PRIVATE)
            if (prefs.contains("nameOfUser")) {
                nameOfUser = prefs.getString("nameOfUser", "").toString()
            }
            speakAllTasks(nameOfUser)
        }

        val i = Intent(this, MainActivity::class.java)
        i.putExtra("goodmorning", "fromwakeup")
        startActivity(i)
    }

    private fun setNextAlarm() {
        val alarmHandler = AlarmHandler(this)
        alarmHandler.findNextAlarm()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result: Int? = tts?.setLanguage(Locale.ENGLISH)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "Please enable english language", Toast.LENGTH_SHORT).show()
            }
        } else {
            // notify user of the error, and keep running the app
            Toast.makeText(this, "Audio unavailable due to TextToSpeech error", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun speakTask(sayThis: String?) {
        // speak out the message, api >= 21 requires extra parameter
        val utteranceId = this.hashCode().toString() + ""
        tts?.speak(sayThis, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun speakAllTasks(nameOfUser: String) {
        // gather info from the task database
        /* if called from onInit via AlarmReceiver, add the greeting.  This is ugly but required.
        This is done because tts doesn't wait until after it's finished speaking to speak another
        message. The first message will be skipped. There is a way around this with an utterance
        listener, but it adds a lot of ugly boilerplate.
         */
        var counter = 0
        val allTasks = StringBuilder(
            "Good Morning, " + nameOfUser + "! Your to do list for today " +
                    "is as follows."
        )
        val cursor = dbH!!.allData
        while (cursor.moveToNext()) {
            val idx = cursor.getColumnIndex("DESCTASK")
            counter++
            allTasks.append(" Task ").append(counter).append(". ").append(cursor.getString(idx))
                .append(" .")
        }
        val calTasks = getCalendarTasks()
        if (calTasks.size > 0) {
            for (task: String? in calTasks) {
                counter++
                allTasks.append(" Task ").append(counter).append(". ").append(task).append(" .")
            }
        }
        cursor.close()
        dbH?.close()
        speakTask(allTasks.toString())
    }

    fun getCalendarTasks(): ArrayList<String> {
        val INSTANCE_PROJECTION = arrayOf(
            CalendarContract.Instances.EVENT_ID,  // 0
            CalendarContract.Instances.BEGIN,  // 1
            CalendarContract.Instances.TITLE,  // 2
            CalendarContract.Instances.DTSTART,
            CalendarContract.Instances.START_MINUTE
        )

        // other indexes not used
        val PROJECTION_TITLE_INDEX = 2
        val PROJECTION_STARTMINUTE_INDEX = 4
        val beginTime = Calendar.getInstance()
        val date = beginTime[Calendar.DAY_OF_MONTH]
        val year = beginTime[Calendar.YEAR]
        val month = beginTime[Calendar.MONTH]
        beginTime[year, month] = date
        val endTime = Calendar.getInstance()
        endTime[year, month] = date + 1
        val startMillis = beginTime.timeInMillis
        val endMillis = endTime.timeInMillis

        // Construct the query with the desired date range.
        val builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
        ContentUris.appendId(builder, startMillis)
        ContentUris.appendId(builder, endMillis)
        val cur: Cursor?
        val cr = contentResolver
        cur = cr?.query(
            builder.build(),
            INSTANCE_PROJECTION,
            null,
            null,
            null
        )
        var title: String
        var startmin: String
        val calTasksForToday = ArrayList<String>()
        if (cur != null) {
            while (cur.moveToNext()) {
                // Get the field values
                title = cur.getString(PROJECTION_TITLE_INDEX)
                startmin = cur.getString(PROJECTION_STARTMINUTE_INDEX)
                calTasksForToday.add(convertMinsSinceMidnightToTime(startmin) + " - " + title)
            }
            cur.close()
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
        return (hour).toString() + ":" + minutesStr + " " + amOrPM
    }

    // in case user accidentally hits the home button instead of ok, STOP the ALARM! ; )
    override fun onDestroy() {
        super.onDestroy()

        // tts can consume a lot of resources if left on
        tts?.stop()
        tts?.shutdown()
        if (dbH != null) dbH?.close()
    }
}