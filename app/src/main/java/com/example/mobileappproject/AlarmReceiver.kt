package com.example.mobileappproject

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
import androidx.core.app.NotificationCompat

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        val alarmId = intent.getIntExtra("alarmId", -1)

        // 알림 표시
        showNotification(context, "알람이 울립니다!")

        // 진동 발생
        triggerVibration(context)

        Toast.makeText(context, "알람이 울립니다!", Toast.LENGTH_LONG).show()
    }

    private fun triggerVibration(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator.vibrate(android.os.VibrationEffect.createOneShot(1000, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(1000)
        }
    }

    private fun showNotification(context: Context, message: String) {
        val channelId = "alarm_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "알람 채널", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "알람 알림 채널"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("알람 발생!")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(0, notification)
    }
}