package com.example.data.model

import java.io.File

enum class FileType {
    IMAGE, VIDEO, AUDIO, DOCUMENT, APK, ARCHIVE, OTHER
}

data class FileItem(
    val id: String,
    val name: String,
    val path: String,
    val sizeBytes: Long,
    val lastModified: Long,
    val isDirectory: Boolean,
    val type: FileType,
    val isVault: Boolean = false,
    val extension: String = "",
    val realFile: File? = null
) {
    val formattedSize: String
        get() = formatFileSize(sizeBytes)

    val formattedDate: String
        get() {
            val sdf = java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault())
            return sdf.format(java.util.Date(lastModified))
        }
}

fun formatFileSize(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
    val group = digitGroups.coerceIn(0, units.size - 1)
    val value = bytes / Math.pow(1024.0, group.toDouble())
    return String.format(java.util.Locale.US, "%.1f %s", value, units[group])
}

data class StorageStats(
    val totalBytes: Long,
    val usedBytes: Long,
    val freeBytes: Long,
    val imageBytes: Long,
    val videoBytes: Long,
    val audioBytes: Long,
    val docBytes: Long,
    val apkBytes: Long,
    val archiveBytes: Long,
    val otherBytes: Long
)

enum class VaultType {
    PHOTO, VIDEO, DOCUMENT, NOTE
}

data class VaultItem(
    val id: String,
    val title: String,
    val originalName: String,
    val type: VaultType,
    val dateEncrypted: Long,
    val sizeBytes: Long,
    val noteContent: String = "",
    val previewPath: String? = null
)

data class SecretNote(
    val id: String,
    val title: String,
    val content: String,
    val timestamp: Long,
    val category: String = "Personal"
)

data class RoutePoint(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double = 0.0,
    val speed: Float = 0f,
    val timestamp: Long = System.currentTimeMillis()
)

data class RouteTrack(
    val id: String,
    val name: String,
    val startTime: Long,
    val endTime: Long,
    val points: List<RoutePoint>,
    val totalDistanceMeters: Double,
    val averageSpeedKmh: Double
)

data class HindiMonth(
    val englishName: String,
    val hindiName: String,
    val season: String,
    val gregorianApprox: String
)

data class HolidayItem(
    val id: String,
    val englishName: String,
    val hindiName: String,
    val dateString: String,
    val hindiMonth: String,
    val tithi: String,
    val region: String, // "National", "North", "South", "East", "West"
    val description: String,
    val isRegionalCustom: Boolean = false
)

data class SocialFeedItem(
    val id: String,
    val platform: String, // "WhatsApp", "Telegram", "Instagram", "Cloud Drive"
    val fileName: String,
    val fileType: FileType,
    val sizeBytes: Long,
    val syncTimestamp: Long,
    val isAutoSynced: Boolean = true,
    val senderOrSource: String
)

enum class ThemeStyle {
    SLATE_CYAN,
    EMERALD_VAULT,
    MIDNIGHT_BLUE,
    SUNSET_AMBER,
    AMOLED_DARK
}

data class NotificationSettings(
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val priorityLevel: String = "HIGH", // "HIGH", "DEFAULT", "LOW"
    val notifyFileOperations: Boolean = true,
    val notifySyncStatus: Boolean = true,
    val notifySecurityAlerts: Boolean = true,
    val notifyDailyHolidays: Boolean = true
)

enum class TargetFormat(val label: String, val extension: String, val category: String) {
    PDF("PDF Document", "pdf", "Document"),
    PNG("PNG Image", "png", "Image"),
    JPG("JPEG Image", "jpg", "Image"),
    WEBP("WebP Compressed", "webp", "Image"),
    MP3("MP3 Audio (Extract)", "mp3", "Audio"),
    M4A("M4A Voice Track", "m4a", "Audio"),
    TXT("Plain Text", "txt", "Document"),
    MD("Markdown Document", "md", "Document"),
    ZIP("ZIP Compression", "zip", "Archive"),
    AI_OCR("AI Text Scanner (OCR)", "txt", "AI Smart"),
    AI_SUMMARY("AI Executive Summary", "txt", "AI Smart"),
    AI_TRANSCRIPT("AI Audio Transcription", "txt", "AI Smart")
}

data class ConversionHistoryItem(
    val id: String,
    val originalFileName: String,
    val convertedFileName: String,
    val originalSizeBytes: Long,
    val convertedSizeBytes: Long,
    val targetFormat: TargetFormat,
    val timestamp: Long,
    val convertedFilePath: String,
    val aiInsight: String = ""
) {
    val reductionPercentage: Int
        get() {
            if (originalSizeBytes <= 0) return 0
            val diff = originalSizeBytes - convertedSizeBytes
            return ((diff.toDouble() / originalSizeBytes.toDouble()) * 100).toInt().coerceAtLeast(0)
        }
}
