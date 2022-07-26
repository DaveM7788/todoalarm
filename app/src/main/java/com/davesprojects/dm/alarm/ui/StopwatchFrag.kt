package com.davesprojects.dm.alarm.ui

import android.content.Context
import android.content.Intent
import android.widget.Chronometer
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import com.davesprojects.dm.alarm.R
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.SystemClock
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import com.davesprojects.dm.alarm.notifs.MusicAlarmSoundService
import com.davesprojects.dm.alarm.notifs.StopwatchService

/**
 * Created by blue on 12/30/2017.
 */
class StopwatchFrag : Fragment(), View.OnClickListener {
    lateinit var con: Context
    lateinit var myView: View
    lateinit var timerChrono: Chronometer
    lateinit var buttonTimer: Button
    lateinit var buttonResumeTimer: Button
    var chronoOn = false
    var paused = false
    private var lastPause: Long = 0
    var wasRestored = false
    var restoreTime: Long = 0
    var pauseText: CharSequence? = null

    // SystemClock.elapsedRealtime = returns time since system was booted
    // chrono.getBase   = return the base time as set by setBase(long)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        myView = inflater.inflate(R.layout.stopwatch, container, false)
        con = requireContext()

        // for back stack
        val prefEditor = con.getSharedPreferences(
            "Preferences",
            Context.MODE_PRIVATE
        ).edit()
        prefEditor.putString("lastFrag", "StopwatchFrag")
        prefEditor.apply()
        timerChrono = myView.findViewById(R.id.stopWatchChrono)
        buttonTimer = myView.findViewById(R.id.buttonStopwatch)
        buttonTimer.setOnClickListener(View.OnClickListener { firstButtonStopWatch() })
        buttonResumeTimer = myView.findViewById(R.id.buttonResumeStopWatch)
        buttonResumeTimer.setOnClickListener(View.OnClickListener { restartStopWatch() })
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("CHRONO")) {
                restoreTime = savedInstanceState.getLong("CHRONO")
                paused = savedInstanceState.getBoolean("CHRONOPAUSED")
                wasRestored = true
                if (!paused) {
                    chronoOn = true
                    buttonTimer.setText("Pause")
                    timerChrono.setBase(restoreTime)
                    timerChrono.start()
                } else {
                    lastPause = savedInstanceState.getLong("LASTPAUSE")
                    pauseText = savedInstanceState.getCharSequence("CHRONOTEXT")
                    timerChrono.setText(pauseText)
                    buttonTimer.setText("Resume")
                }
            }
        }
        return myView
    }

    override fun onClick(view: View) {
        // required override
    }

    private fun foregroundService() {
        val serviceIntent = Intent(context, StopwatchService::class.java)
        if (Build.VERSION.SDK_INT >= 26) con.startForegroundService(serviceIntent)
        else con.startService(serviceIntent)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // handle screen rotation (which normally resets chrono = bad!)
        outState.putLong("CHRONO", timerChrono.base)
        outState.putBoolean("CHRONOPAUSED", paused)
        outState.putLong("LASTPAUSE", lastPause)
        outState.putCharSequence("CHRONOTEXT", timerChrono.text)
    }

    fun firstButtonStopWatch() {
        if (!chronoOn) {
            if (!paused) {
                startStopWatch()
            } else {
                resumeStopWatch()
            }
        } else {
            pauseStopWatch()
        }
    }

    fun startStopWatch() {
        chronoOn = true
        buttonTimer.text = "Pause"
        timerChrono.base = SystemClock.elapsedRealtime()
        timerChrono.start()
    }

    fun restartStopWatch() {
        chronoOn = true
        paused = false
        wasRestored = false
        timerChrono.base = SystemClock.elapsedRealtime()
        timerChrono.start()
        buttonTimer.text = "Pause"
    }

    fun pauseStopWatch() {
        buttonTimer.text = "Resume"
        lastPause = SystemClock.elapsedRealtime()
        timerChrono.stop()
        paused = true
        chronoOn = false
    }

    fun resumeStopWatch() {
        buttonTimer.text = "Pause"
        chronoOn = true
        paused = false
        if (!wasRestored) {
            timerChrono.base = timerChrono!!.base + SystemClock.elapsedRealtime() - lastPause
        } else {
            timerChrono.base = restoreTime + SystemClock.elapsedRealtime() - lastPause
            wasRestored = false
        }
        timerChrono.start()
    }

    // screen rotation calls onCreate resets chrono
    // prevent screen rotation just for this fragment
    override fun onResume() {
        super.onResume()
        if (activity != null) {
            requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    override fun onPause() {
        super.onPause()
        if (activity != null) {
            requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
        }
    }
}