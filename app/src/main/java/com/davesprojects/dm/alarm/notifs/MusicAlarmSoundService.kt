package com.davesprojects.dm.alarm.notifs

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.Process
import android.widget.Toast

import android.app.NotificationManager

import android.app.NotificationChannel

import android.os.Build

import android.app.PendingIntent
import android.content.Context
import android.content.SharedPreferences
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC
import com.davesprojects.dm.alarm.ui.WakeUpIntermediate
import java.lang.Exception

import android.graphics.BitmapFactory
import com.davesprojects.dm.alarm.R

class MusicAlarmSoundService : Service() {

    private var mBuilder: NotificationCompat.Builder? = null
    private var wakeUpSongDat = ""
    private var ringtone: Ringtone? = null
    private var mp: MediaPlayer? = null
    private val CHANNEL_ID = "todo.alarm.channel.id.1"
    private val defaultSoundUri: Uri = RingtoneManager.getDefaultUri(
        RingtoneManager.TYPE_NOTIFICATION
    )
    private val vibrationPat = longArrayOf(100, 200, 300, 400, 400, 300, 400)
    private var wakeUpToSong = false
    private var currentVolume = 0
    private var failureCounter = 0

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (intent.action != null) {   // allows notification to cancel service
            if (intent.action == "EndService") {
                // DO NOT PUT ANYTHING ELSE HERE - COULD MESS WITH SERVICE
                stopMySelf()
            }
            return START_NOT_STICKY
        }
        startupSequence()
        return START_NOT_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        // NOTE: for some reason turning off/on bluetooth triggers onCreate for this service
        // probably related to this bug - https://issuetracker.google.com/issues/209930562?pli=1
    }

    private fun startupSequence() {
        createNotificationChannel()
        notifyMaker()
        // startupSequence gets called by onStartCommand. which can get called multiple times
        // so let's stopAllSounds just in case there are any weird situations
        stopAllSounds(baseContext)
        playAlarm(baseContext)
    }

    fun stopMySelf() {
        stopForeground(true)
        stopSelf()
        stopAllSounds(baseContext)

        // the msg below should not display --- verifies service was stopped
        Toast.makeText(this@MusicAlarmSoundService, "Stopping alarm sounds $failureCounter", Toast.LENGTH_SHORT)
            .show()
        if (failureCounter > 3) Process.killProcess(Process.myPid())
        failureCounter++
    }

    private fun notifyMaker() {
        // allows clicking on notification to open app
        val i = Intent(baseContext, WakeUpIntermediate::class.java)
        i.putExtra("goodmorning", "fromwakeup")
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_HISTORY
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0, i,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val iEnd = Intent(baseContext, MusicAlarmSoundService::class.java)
        iEnd.action = "EndService"
        val iEndPending = PendingIntent.getService(
            this, 0, iEnd,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val icon = R.drawable.ic_notification_todo_alarm
        val iconBitmap = BitmapFactory.decodeResource(
            baseContext.resources,
            R.mipmap.ic_launcher
        )
        mBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(icon)
            .setContentTitle("To-Do Alarm")
            .setContentText("Good morning ... or evening")
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_alarm_blue, "Stop", iEndPending)
            .addAction(R.drawable.ic_alarm_blue, "Open", pendingIntent)
            .setPriority(NotificationCompat.FLAG_ONGOING_EVENT)
            .setOngoing(true)
            .setVibrate(vibrationPat)
            .setSound(defaultSoundUri)
            .setVisibility(VISIBILITY_PUBLIC)
            .setStyle(NotificationCompat.BigPictureStyle().bigPicture(iconBitmap))
        startForeground(7, mBuilder?.build())
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = "todoalarmchanname"
            val description = "todoalarmchandesc"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            channel.description = description
            channel.enableVibration(true)
            channel.vibrationPattern = vibrationPat
            channel.setSound(defaultSoundUri, null)
            channel.lockscreenVisibility = VISIBILITY_PUBLIC
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun playAlarm(context: Context) {
        val prefs: SharedPreferences = context.getSharedPreferences(
            "Preferences", Context.MODE_PRIVATE
        )
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

    private fun stopAllSounds(context: Context) {
        ringtone?.let {
            if (it.isPlaying) {
                // return ringtone sound to previous level before activity began
                val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager?
                audio?.setStreamVolume(AudioManager.STREAM_RING, currentVolume, 0)
                ringtone?.stop()
            }
        }
        mp?.let {
            val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager?
            audio?.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0)
            it.stop()
            it.release()
        }
    }
}