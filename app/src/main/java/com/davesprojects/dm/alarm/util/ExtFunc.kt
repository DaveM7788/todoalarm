package com.davesprojects.dm.alarm.util

import android.content.Context
import kotlin.math.roundToInt

fun Context.backstackHandler(fragString: String) {
    // for back stack
    val prefEditor = this.getSharedPreferences(
        "Preferences",
        Context.MODE_PRIVATE
    ).edit()
    prefEditor.putString("lastFrag", fragString)
    prefEditor.apply()
}

fun Double.formatStopwatchTime() : String {
    val resultInt = this.roundToInt()
    val hours = resultInt % 86400 / 3600
    val minutes = resultInt % 86400 % 3600 / 60
    val seconds = resultInt % 86400 % 3600 % 60

    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}