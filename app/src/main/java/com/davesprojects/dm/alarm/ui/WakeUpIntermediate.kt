package com.davesprojects.dm.alarm.ui

import android.media.Ringtone
import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.davesprojects.dm.alarm.R

class WakeUpIntermediate : AppCompatActivity() {

    lateinit var btnContinue: Button
    lateinit var btnContinueKeepSong: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.intermediate_new_android)

        btnContinue = findViewById(R.id.btnContinue)
        btnContinue.setOnClickListener { killAlarmMusicService() }
    }

    private fun killAlarmMusicService() {

    }
}