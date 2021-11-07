package com.davesprojects.dm.alarm.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.RingtoneManager
import android.os.Build
import androidx.core.content.ContextCompat.getSystemService
import com.davesprojects.dm.alarm.notifs.MusicAlarmSoundService
import com.davesprojects.dm.alarm.notifs.NotificationHelper
import com.davesprojects.dm.alarm.ui.MainActivity
import com.davesprojects.dm.alarm.ui.WakeUpActivity
import kotlin.math.abs

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        println("dp-77 onReceive running")

        /* on Android 12 it is possible to directly trigger this receiver from the new alarm
        icon. Clicking the icon seems to call the intent directly to trigger AlarmReceiver.
        We need to prevent the alarm from instantly going off by comparing the current time
        against the alarm time that was set during the pending intent creation
        */
        if (Build.VERSION.SDK_INT >= 31) {
            val extrasIntent = intent.extras
            val timeFromIntent = extrasIntent?.getLong(UsefulConstants.TIME_FROM_INTENT, 0)
            timeFromIntent?.let {
                val currentTime = System.currentTimeMillis()
                val diff = it - currentTime
                if (diff < UsefulConstants.THRESH_TIMES && diff > 0) {
                    //launchWakeUpActivity(context)
                    handleAlarmNotify(context)
                } else if (diff < 0 && abs(diff) < UsefulConstants.SLIGHT_BEFORE_THRESH) {
                    //launchWakeUpActivity(context)
                    handleAlarmNotify(context)
                } else {
                    val i = Intent(context, MainActivity::class.java)
                    i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(i)
                }
            }
        } else {
            launchWakeUpActivity(context)
        }
    }

    private fun launchWakeUpActivity(context: Context) {
        println("dp-77 launch wake up running")
        val prefEditor = context.getSharedPreferences(
            "Preferences",
            Context.MODE_PRIVATE
        ).edit()
        prefEditor.putBoolean("alarmOnOff", true)
        prefEditor.apply()

        val i = Intent(context, WakeUpActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_HISTORY
        // flag new task required when calling startActivity in a Receiver
        // flag no history prevents user from clicking back button into the WakeUpActivity


        // context.startActivity(i)

        //playWake(context)
        //handleAlarmNotify(context)
    }

    private fun playWake(context: Context) {

        // play an alarm sound! Wake up ; )
        // ensure ringer volume is maxed. Otherwise alarm won't make any sound if the
        // user has the phone on vibrate
        val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        // currentVolume = audio.getStreamVolume(AudioManager.STREAM_RING)
        val maxVolume = audio.getStreamMaxVolume(AudioManager.STREAM_RING)
        audio.setStreamVolume(AudioManager.STREAM_RING, maxVolume, 0)

        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        val ringtone = RingtoneManager.getRingtone(context, uri)
        //ringtone.play()
    }

    private fun handleAlarmNotify(context: Context) {
        //val notifyHelper = NotificationHelper()
        //notifyHelper.alarmNotification(context)
        val serviceIntent = Intent(context, MusicAlarmSoundService::class.java)
        context.startService(serviceIntent);
    }
}