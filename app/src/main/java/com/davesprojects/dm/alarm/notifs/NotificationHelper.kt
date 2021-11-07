package com.davesprojects.dm.alarm.notifs

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.Ringtone
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService
import com.davesprojects.dm.alarm.R
import com.davesprojects.dm.alarm.ui.MainActivity
import com.davesprojects.dm.alarm.ui.WakeUpActivity
import android.media.RingtoneManager
import android.net.Uri
import android.view.View
import android.widget.Button
import java.lang.Exception


class NotificationHelper {

    var wakeUpSongDat = ""
    var ringtone: Ringtone? = null
    var mp: MediaPlayer? = null
    val CHANNEL_ID = "todo.alarm.channel.id.1"
    val notificationId = 106
    val defaultSoundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
    val vibrationPat = longArrayOf(100, 200, 300, 400)
    var wakeUpToSong = false
    var currentVolume = 0

    fun alarmNotification(context: Context) {
        createNotificationChannel(context)
        // Create an explicit intent for an Activity in your app
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val deleteIntent = Intent(context, MainActivity::class.java).apply {
            println("dp-77 delete intent pending intent")
            stopAllSounds(context)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }


        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val pendingIntentDelete: PendingIntent = PendingIntent.getActivity(context, 0, deleteIntent, PendingIntent.FLAG_IMMUTABLE)
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_alarm_blue)
            .setContentTitle("To-Do Alarm")
            .setContentText("Good morning ... or evening : )")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            //.setContentIntent(pendingIntent)
            .setVibrate(vibrationPat)
            .setSound(defaultSoundUri)
            .setFullScreenIntent(pendingIntent, true)
            .setDeleteIntent(pendingIntentDelete)


        with(NotificationManagerCompat.from(context)) {
            // notificationId is a unique int for each notification that you must define
            notify(notificationId, builder.build())
            // playAlarm(context)
        }
    }

    fun playAlarm(context: Context) {
        val prefs: SharedPreferences = context.getSharedPreferences("Preferences", Context.MODE_PRIVATE)
        if (prefs.contains("wakeUpToSong")) {
            wakeUpToSong = prefs.getBoolean("wakeUpToSong", false)
        }
        if (prefs.contains("song")) {
            wakeUpSongDat = prefs.getString("song", "").toString()
        } else {
            wakeUpToSong = false
        }
        if (wakeUpSongDat.isNotEmpty()) {
            if (wakeUpSongDat == "") {
                wakeUpToSong = false
            }
        }
        //ringtoneStop = findViewById<Button>(R.id.ringtoneEnd)
        //ringtoneStop.setOnClickListener(View.OnClickListener { stopRingtone() })
        if (wakeUpToSong) {
            try {
                val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager?
                currentVolume = audio!!.getStreamVolume(AudioManager.STREAM_MUSIC)
                val maxVolume = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                audio.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0)
                mp = MediaPlayer()
                mp?.reset()
                mp?.setAudioStreamType(AudioManager.STREAM_MUSIC)
                mp?.setDataSource(wakeUpSongDat)
                mp?.prepare()
                if (mp?.isPlaying == false) {
                    mp?.start()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            // play an alarm sound! Wake up ; )
            // ensure ringer volume is maxed. Otherwise alarm won't make any sound if the
            // user has the phone on vibrate
            val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager?
            currentVolume = audio!!.getStreamVolume(AudioManager.STREAM_RING)
            val maxVolume = audio.getStreamMaxVolume(AudioManager.STREAM_RING)
            audio.setStreamVolume(AudioManager.STREAM_RING, maxVolume, 0)
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ringtone = RingtoneManager.getRingtone(context, uri)
            ringtone?.play()
        }
    }

    private fun createNotificationChannel(context: Context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.channel_name)
            val descriptionText = context.getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
                vibrationPattern = vibrationPat
                setSound(defaultSoundUri, null)
                //lockscreenVisibility = 1
            }

            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun stopAllSounds(context: Context) {
        println("dp-77 stop all sounds working???")
        if (ringtone != null) {
            if (ringtone!!.isPlaying) {
                // return ringtone sound to previous level before activity began
                val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager?
                audio!!.setStreamVolume(AudioManager.STREAM_RING, currentVolume, 0)
                ringtone!!.stop()
            }
        }
        if (mp != null) {
            val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager?
            audio!!.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0)
            mp!!.stop()
            mp!!.release()
        }
    }

    // notification cancel === stop music
    // notification stop sound button === stop music
}