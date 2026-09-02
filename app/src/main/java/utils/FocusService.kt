package com.example.taskmanager.utils

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.*
import androidx.core.app.NotificationCompat
import com.example.taskmanager.MainActivity
import java.util.Locale

class FocusService : Service() {

    private var countDownTimer: CountDownTimer? = null
    private var timeLeftInMillis: Long = 0
    private var isTimerRunning = false
    private val binder = LocalBinder()

    companion object {
        const val CHANNEL_ID = "focus_mode_channel"
        const val NOTIFICATION_ID = 2
        var isServiceRunning = false
        var currentTimeLeft: Long = 1500000L
    }

    inner class LocalBinder : Binder() {
        fun getService(): FocusService = this@FocusService
    }

    override fun onBind(intent: Intent): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        when (action) {
            "START" -> {
                val duration = intent.getLongExtra("duration", 1500000L)
                startTimer(duration)
            }
            "STOP" -> stopTimer()
        }
        return START_STICKY
    }

    private fun startTimer(duration: Long) {
        // Cancel any existing timer first
        countDownTimer?.cancel()
        
        timeLeftInMillis = duration
        currentTimeLeft = duration
        isTimerRunning = true
        isServiceRunning = true
        
        createNotificationChannel()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, buildNotification("Timer Started"), ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTIFICATION_ID, buildNotification("Timer Started"))
        }

        countDownTimer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                currentTimeLeft = millisUntilFinished
                updateNotification()
                
                val intent = Intent("FOCUS_TIMER_UPDATE").apply {
                    setPackage(packageName)
                    putExtra("timeLeft", timeLeftInMillis)
                }
                sendBroadcast(intent)
            }

            override fun onFinish() {
                isTimerRunning = false
                isServiceRunning = false
                updateNotification("Focus Session Finished! 🎉")
                stopForeground(false)
                
                val intent = Intent("FOCUS_TIMER_FINISHED")
                sendBroadcast(intent)
            }
        }.start()
    }

    private fun stopTimer() {
        countDownTimer?.cancel()
        isTimerRunning = false
        isServiceRunning = false
        stopForeground(true)
        stopSelf()
    }

    private fun updateNotification(content: String? = null) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, buildNotification(content))
    }

    private fun buildNotification(content: String?): Notification {
        val minutes = (timeLeftInMillis / 1000) / 60
        val seconds = (timeLeftInMillis / 1000) % 60
        val timeStr = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

        val mainIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, mainIntent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Focus Mode Active")
            .setContentText(content ?: "Time remaining: $timeStr")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID, "Focus Mode Timer",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    override fun onDestroy() {
        countDownTimer?.cancel()
        super.onDestroy()
    }
}