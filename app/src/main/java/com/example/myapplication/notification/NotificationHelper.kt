package com.example.myapplication.notification

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.myapplication.MainActivity
import com.example.myapplication.R

object NotificationHelper {

    const val CHANNEL_ID = "fdfs_alert_channel"
    const val CHANNEL_NAME = "FDFS Booking Alerts"
    const val NOTIFICATION_ID = 1001

    private var activeVibrator: Vibrator? = null

    fun createNotificationChannel(context: Context) {
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
            description = "Urgent alerts when FDFS First Day First Show movie booking opens"
            enableVibration(true)
            vibrationPattern = create30SecondVibrationPattern()
        }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    private fun create30SecondVibrationPattern(): LongArray {
        val patternList = mutableListOf<Long>(0)
        var totalMs = 0L
        while (totalMs < 30000) {
            patternList.add(1000L) // vibrate 1 sec
            patternList.add(500L)  // pause 0.5 sec
            totalMs += 1500L
        }
        return patternList.toLongArray()
    }

    @SuppressLint("MissingPermission")
    fun triggerBookingAlert(
        context: Context,
        movieTitle: String,
        cinemaName: String,
        bookingUrl: String,
        vibrationEnabled: Boolean = true
    ) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val url = if (bookingUrl.isNotBlank()) bookingUrl else "https://www.google.com/search?q=${Uri.encode("$movieTitle $cinemaName booking")}"
        val bookIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val bookPendingIntent = PendingIntent.getActivity(
            context,
            1,
            bookIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("🚨 FDFS BOOKING OPEN: $movieTitle!")
            .setContentText("Morning show tickets are now OPEN at $cinemaName. Tap to book instantly!")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("🚨 FDFS FIRST DAY FIRST SHOW ALERT!\n\nMovie: $movieTitle\nCinema: $cinemaName\nStatus: BOOKING OPEN NOW!\n\nTap 'BOOK NOW' to grab your morning show seats!")
            )
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_launcher_foreground, "BOOK NOW", bookPendingIntent)

        if (vibrationEnabled) {
            val pattern = create30SecondVibrationPattern()
            builder.setVibrate(pattern)
            start30SecondVibration(context, pattern)
        }

        try {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(NOTIFICATION_ID, builder.build())
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    @SuppressLint("MissingPermission")
    fun start30SecondVibration(context: Context, pattern: LongArray = create30SecondVibrationPattern()) {
        try {
            stopVibration()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                val vibrator = vibratorManager.defaultVibrator
                activeVibrator = vibrator
                val effect = VibrationEffect.createWaveform(pattern, -1)
                vibrator.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                activeVibrator = vibrator
                val effect = VibrationEffect.createWaveform(pattern, -1)
                vibrator.vibrate(effect)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("MissingPermission")
    fun stopVibration() {
        try {
            activeVibrator?.cancel()
            activeVibrator = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
