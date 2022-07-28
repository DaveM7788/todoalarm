package com.davesprojects.dm.alarm.ui

import android.content.*
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import com.davesprojects.dm.alarm.R
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.davesprojects.dm.alarm.notifs.StopwatchService
import com.davesprojects.dm.alarm.util.STOPWATCH_FRAG
import com.davesprojects.dm.alarm.util.backstackHandler
import com.davesprojects.dm.alarm.util.formatStopwatchTime

/**
 * Created by blue on 12/30/2017.
 */
class StopwatchFrag : Fragment()  {

    private var timerStarted = false
    private lateinit var serviceIntent: Intent
    private var time = 0.0

    private lateinit var startStopButton: Button
    lateinit var resetButton: Button
    lateinit var timeTV: TextView
    private lateinit var myView: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        myView = inflater.inflate(R.layout.stopwatch, container, false)
        requireContext().backstackHandler(STOPWATCH_FRAG)

        startStopButton = myView.findViewById(R.id.buttonStartStop)
        startStopButton.setOnClickListener { startStopTimer() }
        resetButton = myView.findViewById(R.id.buttonRestart)
        resetButton.setOnClickListener { resetTimer() }
        timeTV = myView.findViewById(R.id.stopWatchTV)

        serviceIntent = Intent(context, StopwatchService::class.java)
        requireContext().registerReceiver(
            updateTime, IntentFilter(StopwatchService.STOPWATCH_UPDATED)
        )

        return myView
    }

    private fun resetTimer() {
        stopTimer()
        time = 0.0
        timeTV.text = time.formatStopwatchTime()
    }

    private fun startStopTimer() {
        if (timerStarted)
            stopTimer()
        else
            startTimer()
    }

    private fun startTimer() {
        serviceIntent.putExtra(StopwatchService.TIME_EXTRA, time)
        requireContext().startService(serviceIntent)
        startStopButton.text = "Stop"
        timerStarted = true
    }

    private fun stopTimer() {
        requireActivity().stopService(serviceIntent)
        startStopButton.text = "Start"
        timerStarted = false
    }

    private val updateTime: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            time = intent.getDoubleExtra(StopwatchService.TIME_EXTRA, 0.0)
            timeTV.text = time.formatStopwatchTime()
            startStopButton.text = "Stop"
            timerStarted = true
        }
    }
}