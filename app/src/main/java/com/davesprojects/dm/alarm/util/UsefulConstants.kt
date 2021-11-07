package com.davesprojects.dm.alarm.util

class UsefulConstants {
    companion object {
        const val TIME_FROM_INTENT = "FROMTODOALARMORSYS"
        const val THRESH_TIMES: Long = (1000 * 60 * 10).toLong()  // within 10 minutes
        const val SLIGHT_BEFORE_THRESH = (1000 * 60 * 1.5).toLong()  // 1:30 before
    }
}