package com.davesprojects.dm.alarm.ui

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import androidx.fragment.app.Fragment
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.davesprojects.dm.alarm.R
import java.lang.Exception

class TimerFrag : Fragment() {

    private lateinit var countDownTimer: CountDownTimer
    private var initialCountDown: Long = 60000 // milliseconds
    private var countDownInterval: Long = 1000
    private var timeLeft = 60
    private var startOrPause = "Start"

    private lateinit var timeLeftTV: TextView
    private lateinit var btnStartPause: Button
    private lateinit var btnRestart: Button
    private lateinit var editTextHr: EditText
    private lateinit var editTextMin: EditText
    private lateinit var editTextSec: EditText

    private var pausedTime : Long = 0
    private var timerPaused = false

    private lateinit var con : Context


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.timer_layout, container, false)
        con = view.context

        btnStartPause = view.findViewById(R.id.buttonTimerStartPause)
        btnRestart = view.findViewById(R.id.buttonTimerReStart)
        timeLeftTV = view.findViewById(R.id.textViewTimerLeft)
        editTextHr = view.findViewById(R.id.editTextHr)
        editTextMin = view.findViewById(R.id.editTextMin)
        editTextSec = view.findViewById(R.id.editTextSec)

        btnStartPause.setOnClickListener { v->
            if (btnStartPause.text.toString().equals("Start")) {
                btnStartPause.text = "Pause"
                if (!timerPaused) {
                    startTimer()
                } else {
                    restoreAfterPause()
                }
            } else if (btnStartPause.text.toString().equals("Pause")) {
                btnStartPause.text = "Start"
                pauseTimer()
            }

        }

        btnRestart.setOnClickListener { v->
            restartTimer()
        }


        //return inflater.inflate(R.layout.timer_layout, container, false)
        return view
    }

    private fun restartTimer() {
        countDownTimer.cancel()
        timerPaused = false
        btnStartPause.text = "Pause"
        startTimer()
    }

    private fun startTimer() {
        countDownTimer = object : CountDownTimer(convertEditTextLong(), countDownInterval) {
            override fun onTick(millis: Long) {
                timeLeft = millis.toInt() / 1000

                //timeLeftTV.text = timeLeft.toString()
                timeLeftTV.text = convertLongToTV(timeLeft)
            }

            override fun onFinish() {
                endTimer()
            }
        }

        countDownTimer.start()
        timerPaused = false
    }

    private fun pauseTimer() {
        Toast.makeText(context, "Timer Paused", Toast.LENGTH_LONG).show()
        countDownTimer.cancel()
        timerPaused = true
    }

    private fun restoreAfterPause() {
        countDownTimer = object : CountDownTimer(timeLeft.toLong() * 1000, countDownInterval) {
            override fun onTick(millis: Long) {
                timeLeft = millis.toInt() / 1000

                //timeLeftTV.text = timeLeft.toString()
                timeLeftTV.text = convertLongToTV(timeLeft)
            }

            override fun onFinish() {
                endTimer()
            }
        }

        countDownTimer.start()
        timerPaused = false
    }

    private fun endTimer() {
        timerPaused = false

        createNotificationChannel()
        // toast
        Toast.makeText(context, "Timer Complete", Toast.LENGTH_LONG).show()

        btnStartPause.text = "Start"

        val builder = NotificationCompat.Builder(con, "TODOALARM")
                .setSmallIcon(R.drawable.ic_timer)
                .setContentTitle(getString(R.string.timer_title))
                .setContentText("Timer")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)

        with(NotificationManagerCompat.from(con)) {
            // notificationId is a unique int for each notification that you must define
            notify(119, builder.build())
        }
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "test notify"
            val descriptionText = "notifictaion description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("TODOALARM", name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                    con.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun convertEditTextLong() : Long {
        val hrs = editTextHr.text.toString()
        val mins = editTextMin.text.toString()
        val secs = editTextSec.text.toString()
        var numHrs = 0
        var numMins = 0
        var numSecs = 0

        if (!hrs.equals("")) {
            numHrs = hrs.toInt()
        }
        if (!mins.equals("")) {
            numMins = mins.toInt()
        }
        if (!secs.equals("")) {
            numSecs = secs.toInt()
        }

        val total = ((numHrs * 60 * 60) + (numMins * 60) + (numSecs)) * 1000

        return total.toLong()
    }

    private fun convertLongToTV(timePass: Int) : String {
        var time = timePass
        if (time >= 3600) {
            val hrs = time / 3600
            time -= (hrs*3600)
            val mins = time / 60
            time -= (mins*60)

            var minsStr = mins.toString()
            if (mins < 10) {
                minsStr = "0" + minsStr
            }

            var secsStr = time.toString()
            if (time < 10) {
                secsStr = "0" + secsStr
            }

            return hrs.toString() + ":" + minsStr + ":" + secsStr
        } else if (time >= 60) {
            val mins = time / 60
            time -= (mins*60)

            var timeStr = time.toString()
            if (time < 10) {
                timeStr = "0" + timeStr
            }

            return mins.toString() + ":" + timeStr
        } else {
            return time.toString()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        try {
            countDownTimer.cancel()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onResume() {
        super.onResume()
        if (activity != null) {
            activity!!.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    override fun onPause() {
        super.onPause()
        if (activity != null) {
            activity!!.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
        }
    }
}