package com.davesprojects.dm.alarm.notifs

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.Process
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.davesprojects.dm.alarm.R
import com.davesprojects.dm.alarm.util.END_STOPWATCH
import com.davesprojects.dm.alarm.util.STOPWATCH_NOTIFY_ID
import com.davesprojects.dm.alarm.util.formatStopwatchTime
import java.util.*

class StopwatchService : Service() {

    private var failureCounter = 0
    private val timer = Timer()

    private var mBuilder: NotificationCompat.Builder? = null
    private val CHANNEL_ID = "todo.alarm.channel.id.2"

    companion object {
        const val TIME_EXTRA = "timeExtra"
        const val STOPWATCH_UPDATED = "stopwatchUpdated"
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (intent.action != null) {   // allows notification to cancel service
            if (intent.action == END_STOPWATCH) {
                // DO NOT PUT ANYTHING ELSE HERE - COULD MESS WITH SERVICE
                stopMySelf()
            }
            return START_NOT_STICKY
        }

        val time = intent.getDoubleExtra(TIME_EXTRA, 0.0)
        timer.scheduleAtFixedRate(StopwatchTask(time), 0, 1000)
        createNotificationChannel()
        notificationStopwatch(time)
        return START_NOT_STICKY
    }

    private inner class StopwatchTask(private var time: Double) : TimerTask() {
        override fun run() {
            val intent = Intent(STOPWATCH_UPDATED)
            time++
            intent.putExtra(TIME_EXTRA, time)
            sendBroadcast(intent)
            updateNotificationTime(time)
        }
    }

    override fun onDestroy() {
        timer.cancel()
        super.onDestroy()
    }

    private fun updateNotificationTime(time: Double) {
        val notification = getNotification(time, true)
        val notificationManager = getSystemService(
            NotificationManager::class.java
        )
        notificationManager.notify(STOPWATCH_NOTIFY_ID, notification)
    }

    private fun getNotification(time: Double, isSilent: Boolean) : Notification? {
        val iEnd = Intent(baseContext, StopwatchService::class.java)
        iEnd.action = END_STOPWATCH
        val iEndPending = PendingIntent.getService(
            this, 0, iEnd,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val icon = R.drawable.ic_stopwatch
        mBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(icon)
            .setContentTitle("To-Do Alarm")
            .setContentText("Stopwatch: ${time.formatStopwatchTime()}")
            .addAction(R.drawable.ic_stopwatch, "Stop", iEndPending)
            .setPriority(NotificationCompat.FLAG_ONGOING_EVENT)
            .setSilent(isSilent)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        return mBuilder?.build()
    }

    private fun notificationStopwatch(time: Double) {
        val notification = getNotification(time, false)
        startForeground(STOPWATCH_NOTIFY_ID, notification)
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = "todoalarmchannamestopwatch"
            val description = "todoalarmchandescstopwatch"
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
        timer.cancel()
        stopForeground(true)
        stopSelf()

        // the msg below should not display --- verifies service was stopped
        Toast.makeText(
            this@StopwatchService, "Stopping stopwatch",
            Toast.LENGTH_SHORT
        ).show()
        if (failureCounter > 3) Process.killProcess(Process.myPid())
        failureCounter++
    }
}