package com.guardianai.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

// Simple foreground service scaffold. Integrate model and sensor collection later.
class FallDetectionService : Service() {

    companion object {
        const val CHANNEL_ID = "GuardianAI_FallChannel"
        const val NOTIF_ID = 1001
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIF_ID, buildNotification("Monitoring for falls..."))

        // TODO: Initialize model, sensors, and feature pipeline here
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Keep running
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cleanup model and sensors
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(CHANNEL_ID, "Fall Detection", NotificationManager.IMPORTANCE_LOW)
            nm.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(content: String): Notification {
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Guardian AI")
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setSound(defaultSoundUri)
            .build()
    }
}

// Simple data class for events
data class FallEvent(val timestamp: Long, val probability: Float)
