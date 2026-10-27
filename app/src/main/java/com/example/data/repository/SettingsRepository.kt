package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.NotificationSettings
import com.example.data.model.ThemeStyle

class SettingsRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("clr_app_prefs", Context.MODE_PRIVATE)

    var vaultPin: String
        get() = prefs.getString("vault_pin", "1234") ?: "1234"
        set(value) = prefs.edit().putString("vault_pin", value).apply()

    var isBiometricEnabled: Boolean
        get() = prefs.getBoolean("biometric_enabled", true)
        set(value) = prefs.edit().putBoolean("biometric_enabled", value).apply()

    var isFakeCrashEnabled: Boolean
        get() = prefs.getBoolean("fake_crash_enabled", false)
        set(value) = prefs.edit().putBoolean("fake_crash_enabled", value).apply()

    var currentThemeStyle: ThemeStyle
        get() {
            val name = prefs.getString("theme_style", ThemeStyle.SLATE_CYAN.name)
            return try {
                ThemeStyle.valueOf(name ?: ThemeStyle.SLATE_CYAN.name)
            } catch (e: Exception) {
                ThemeStyle.SLATE_CYAN
            }
        }
        set(value) = prefs.edit().putString("theme_style", value.name).apply()

    var darkModeSetting: String // "SYSTEM", "DARK", "LIGHT"
        get() = prefs.getString("dark_mode", "DARK") ?: "DARK"
        set(value) = prefs.edit().putString("dark_mode", value).apply()

    var isHindiLanguage: Boolean
        get() = prefs.getBoolean("hindi_lang", false)
        set(value) = prefs.edit().putBoolean("hindi_lang", value).apply()

    var isCloudAutoSync: Boolean
        get() = prefs.getBoolean("cloud_auto_sync", true)
        set(value) = prefs.edit().putBoolean("cloud_auto_sync", value).apply()

    var lastSyncTimestamp: Long
        get() = prefs.getLong("last_sync_time", System.currentTimeMillis() - 3600000L * 4)
        set(value) = prefs.edit().putLong("last_sync_time", value).apply()

    var notificationSettings: NotificationSettings
        get() {
            return NotificationSettings(
                soundEnabled = prefs.getBoolean("notif_sound", true),
                vibrationEnabled = prefs.getBoolean("notif_vibrate", true),
                priorityLevel = prefs.getString("notif_priority", "HIGH") ?: "HIGH",
                notifyFileOperations = prefs.getBoolean("notif_file_op", true),
                notifySyncStatus = prefs.getBoolean("notif_sync", true),
                notifySecurityAlerts = prefs.getBoolean("notif_security", true),
                notifyDailyHolidays = prefs.getBoolean("notif_holiday", true)
            )
        }
        set(value) {
            prefs.edit()
                .putBoolean("notif_sound", value.soundEnabled)
                .putBoolean("notif_vibrate", value.vibrationEnabled)
                .putString("notif_priority", value.priorityLevel)
                .putBoolean("notif_file_op", value.notifyFileOperations)
                .putBoolean("notif_sync", value.notifySyncStatus)
                .putBoolean("notif_security", value.notifySecurityAlerts)
                .putBoolean("notif_holiday", value.notifyDailyHolidays)
                .apply()
        }

    fun verifyPin(pin: String): Boolean {
        return pin == vaultPin
    }
}
