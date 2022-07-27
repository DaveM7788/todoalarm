package com.davesprojects.dm.alarm.notifs

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.os.Process
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.davesprojects.dm.alarm.R
import com.davesprojects.dm.alarm.util.END_TIMER
import com.davesprojects.dm.alarm.util.TIMER_NOTIFY_ID
import com.davesprojects.dm.alarm.util.formatStopwatchTime

class TimerService : Service() {

    private var failureCounter = 0
    private var secondsLeft = 0
    private var timer : CountDownTimer? = null

    private var mBuilder: NotificationCompat.Builder? = null
    private val CHANNEL_ID = "todo.alarm.channel.id.3"

    companion object {
        const val TIMER_EXTRA = "timerExtra"
        const val TIMER_UPDATED = "timerUpdated"
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (intent.action != null) {   // allows notification to cancel service
            if (intent.action == END_TIMER) {
                // DO NOT PUT ANYTHING ELSE HERE - COULD MESS WITH SERVICE
                stopMySelf()
            }
            return START_NOT_STICKY
        }

        val time = intent.getIntExtra(TIMER_EXTRA, 0)
        secondsLeft = time
        timer = object : CountDownTimer((time * 1000).toLong(), 1000) {
            override fun onTick(p0: Long) {
                val intentTick = Intent(TIMER_UPDATED)
                secondsLeft--
                intentTick.putExtra(TIMER_EXTRA, secondsLeft.toDouble())
                sendBroadcast(intentTick)
                updateNotificationTime(secondsLeft.toDouble())
            }

            override fun onFinish() {
                stopMySelf()
            }
        }
        timer?.start()
        createNotificationChannel()
        notificationTimer(time.toDouble())
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        timer?.cancel()
        super.onDestroy()
    }

    private fun updateNotificationTime(time: Double) {
        val isSilent = time >= 4.0
        val notification = getNotification(time, isSilent)
        val notificationManager = getSystemService(
            NotificationManager::class.java
        )
        notificationManager.notify(TIMER_NOTIFY_ID, notification)
    }

    private fun getNotification(time: Double, isSilent: Boolean) : Notification? {
        val iEnd = Intent(baseContext, TimerService::class.java)
        iEnd.action = END_TIMER
        val iEndPending = PendingIntent.getService(
            this, 0, iEnd,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val icon = R.drawable.ic_timer
        mBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(icon)
            .setContentTitle("To-Do Alarm")
            .setContentText("Countdown Timer: ${time.formatStopwatchTime()}")
            .addAction(R.drawable.ic_timer, "Stop", iEndPending)
            .setPriority(NotificationCompat.FLAG_ONGOING_EVENT)
            .setSilent(isSilent)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        return mBuilder?.build()
    }

    private fun notificationTimer(time: Double) {
        val notification = getNotification(time, false)
        startForeground(TIMER_NOTIFY_ID, notification)
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = "todoalarmchannametimer"
            val description = "todoalarmchandesctimer"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            channel.description = description
            channel.lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun stopMySelf() {
        timer?.cancel()
        stopForeground(true)
        stopSelf()

        // the msg below should not display --- verifies service was stopped
        Toast.makeText(
            this@TimerService, "Stopping timer",
            Toast.LENGTH_SHORT
        ).show()
        if (failureCounter > 3) Process.killProcess(Process.myPid())
        failureCounter++
    }
}