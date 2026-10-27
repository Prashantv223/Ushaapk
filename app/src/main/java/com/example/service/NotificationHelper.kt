package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.repository.SettingsRepository

class NotificationHelper(private val context: Context) {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val settingsRepo = SettingsRepository(context)

    companion object {
        const val CHANNEL_FILES = "clr_files_channel"
        const val CHANNEL_SYNC = "clr_sync_channel"
        const val CHANNEL_SECURITY = "clr_security_channel"
        const val CHANNEL_HOLIDAYS = "clr_holidays_channel"
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val fileChannel = NotificationChannel(
                CHANNEL_FILES,
                "File Operations",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for file moves, compression, deletions"
            }

            val syncChannel = NotificationChannel(
                CHANNEL_SYNC,
                "Cloud Sync & Backup",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Status of encrypted backup and cloud synchronization"
            }

            val securityChannel = NotificationChannel(
                CHANNEL_SECURITY,
                "Vault & Security Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts for PIN attempts and vault security"
            }

            val holidayChannel = NotificationChannel(
                CHANNEL_HOLIDAYS,
                "Hindi Calendar & Festivals",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Daily Panchang tithi and festival reminders"
            }

            notificationManager.createNotificationChannels(
                listOf(fileChannel, syncChannel, securityChannel, holidayChannel)
            )
        }
    }

    fun showFileNotification(title: String, message: String, notificationId: Int = 101) {
        val settings = settingsRepo.notificationSettings
        if (!settings.notifyFileOperations) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val priority = when (settings.priorityLevel) {
            "HIGH" -> NotificationCompat.PRIORITY_HIGH
            "LOW" -> NotificationCompat.PRIORITY_LOW
            else -> NotificationCompat.PRIORITY_DEFAULT
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_FILES)
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(priority)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (!settings.vibrationEnabled) {
            builder.setVibrate(longArrayOf(0))
        }

        try {
            notificationManager.notify(notificationId, builder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun showSyncNotification(title: String, message: String, notificationId: Int = 102) {
        val settings = settingsRepo.notificationSettings
        if (!settings.notifySyncStatus) return

        val builder = NotificationCompat.Builder(context, CHANNEL_SYNC)
            .setSmallIcon(android.R.drawable.stat_sys_upload_done)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        try {
            notificationManager.notify(notificationId, builder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun showSecurityAlert(title: String, message: String, notificationId: Int = 103) {
        val settings = settingsRepo.notificationSettings
        if (!settings.notifySecurityAlerts) return

        val builder = NotificationCompat.Builder(context, CHANNEL_SECURITY)
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentTitle("⚠️ $title")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        try {
            notificationManager.notify(notificationId, builder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun showHolidayAlert(title: String, message: String, notificationId: Int = 104) {
        val settings = settingsRepo.notificationSettings
        if (!settings.notifyDailyHolidays) return

        val builder = NotificationCompat.Builder(context, CHANNEL_HOLIDAYS)
            .setSmallIcon(android.R.drawable.ic_menu_my_calendar)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)

        try {
            notificationManager.notify(notificationId, builder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
