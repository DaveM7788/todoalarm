package com.davesprojects.dm.alarm.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.davesprojects.dm.alarm.R
import com.davesprojects.dm.alarm.notifs.TimerService
import com.davesprojects.dm.alarm.util.TIMER_FRAG
import com.davesprojects.dm.alarm.util.backstackHandler
import com.davesprojects.dm.alarm.util.formatStopwatchTime

class TimerFrag : Fragment() {

    private lateinit var timeLeftTV: TextView
    private lateinit var btnStartPause: Button
    private lateinit var btnRestart: Button
    private lateinit var editTextHr: EditText
    private lateinit var editTextMin: EditText
    private lateinit var editTextSec: EditText

    private lateinit var con : Context


    private lateinit var serviceIntent: Intent
    private var time = 0.0
    private var timerStarted = false


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.timer_layout, container, false)
        requireContext().backstackHandler(TIMER_FRAG)
        con = view.context

        btnStartPause = view.findViewById(R.id.buttonTimerStartPause)
        btnRestart = view.findViewById(R.id.buttonTimerReStart)
        timeLeftTV = view.findViewById(R.id.textViewTimerLeft)
        editTextHr = view.findViewById(R.id.editTextHr)
        editTextMin = view.findViewById(R.id.editTextMin)
        editTextSec = view.findViewById(R.id.editTextSec)

        btnStartPause.setOnClickListener { startStopCountdown() }
        btnRestart.setOnClickListener { resetCountdown() }

        serviceIntent = Intent(context, TimerService::class.java)
        requireContext().registerReceiver(
            updateCountdown, IntentFilter(TimerService.TIMER_UPDATED)
        )

        return view
    }

    private val updateCountdown: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            time = intent.getDoubleExtra(TimerService.TIMER_EXTRA, 0.0)
            timeLeftTV.text = "Time Left: ${time.formatStopwatchTime()}"
            if (time <= 0.9) {
                btnStartPause.text = "Start"
                timerStarted = false
            }
        }
    }

    private fun resetCountdown() {
        stopCountdown()
        time = 0.0
        timeLeftTV.text = time.formatStopwatchTime()
        startCountdown()
    }

    private fun startStopCountdown() {
        if (timerStarted) {
            stopCountdown()
        }
        else {
            startCountdown()
        }
    }

    private fun startCountdown() {
        val timeStart = getTotalSecondsRequired()
        serviceIntent.putExtra(TimerService.TIMER_EXTRA, timeStart)
        requireContext().startService(serviceIntent)
        btnStartPause.text = "Stop"
        timerStarted = true
    }

    private fun stopCountdown() {
        requireActivity().stopService(serviceIntent)
        btnStartPause.text = "Start"
        timerStarted = false
    }

    private fun getTotalSecondsRequired() : Int {
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

        val total = ((numHrs * 60 * 60) + (numMins * 60) + (numSecs))

        return total
    }
}