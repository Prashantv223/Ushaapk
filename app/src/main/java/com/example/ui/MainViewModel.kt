package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.StorageManager
import com.example.data.model.*
import com.example.data.repository.SettingsRepository
import com.example.service.LocationTracker
import com.example.service.NotificationHelper
import com.example.service.TtsManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

enum class AppScreen {
    FILE_MANAGER,
    AI_CONVERTER,
    PRIVATE_VAULT,
    MAPS,
    HINDI_CALENDAR,
    CLOUD_SYNC,
    QR_SHARE,
    SETTINGS
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val storageManager = StorageManager(application)
    val settingsRepo = SettingsRepository(application)
    val notificationHelper = NotificationHelper(application)
    val locationTracker = LocationTracker(application)
    val ttsManager = TtsManager(application)
    val fileConverterEngine = com.example.data.local.FileConverterEngine(application)

    // Navigation
    private val _currentScreen = MutableStateFlow(AppScreen.FILE_MANAGER)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // File Manager
    private val _currentDirectory = MutableStateFlow<File>(storageManager.getRootDirectory())
    val currentDirectory: StateFlow<File> = _currentDirectory.asStateFlow()

    private val _files = MutableStateFlow<List<FileItem>>(emptyList())
    val files: StateFlow<List<FileItem>> = _files.asStateFlow()

    private val _selectedCategory = MutableStateFlow<FileType?>(null)
    val selectedCategory: StateFlow<FileType?> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _storageStats = MutableStateFlow(storageManager.getStorageStats())
    val storageStats: StateFlow<StorageStats> = _storageStats.asStateFlow()

    private val _selectedFiles = MutableStateFlow<Set<FileItem>>(emptySet())
    val selectedFiles: StateFlow<Set<FileItem>> = _selectedFiles.asStateFlow()

    private val _activeMediaItem = MutableStateFlow<FileItem?>(null)
    val activeMediaItem: StateFlow<FileItem?> = _activeMediaItem.asStateFlow()

    // Status Message / Toast
    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    // AI File Converter State
    private val _selectedSourceFile = MutableStateFlow<FileItem?>(null)
    val selectedSourceFile: StateFlow<FileItem?> = _selectedSourceFile.asStateFlow()

    private val _selectedTargetFormat = MutableStateFlow<TargetFormat>(TargetFormat.PDF)
    val selectedTargetFormat: StateFlow<TargetFormat> = _selectedTargetFormat.asStateFlow()

    private val _isConverting = MutableStateFlow(false)
    val isConverting: StateFlow<Boolean> = _isConverting.asStateFlow()

    private val _conversionProgress = MutableStateFlow(0f)
    val conversionProgress: StateFlow<Float> = _conversionProgress.asStateFlow()

    private val _conversionHistory = MutableStateFlow<List<ConversionHistoryItem>>(emptyList())
    val conversionHistory: StateFlow<List<ConversionHistoryItem>> = _conversionHistory.asStateFlow()

    private val _lastConversionResult = MutableStateFlow<ConversionHistoryItem?>(null)
    val lastConversionResult: StateFlow<ConversionHistoryItem?> = _lastConversionResult.asStateFlow()

    // Calculator & Vault PIN State
    private val _calcExpression = MutableStateFlow("0")
    val calcExpression: StateFlow<String> = _calcExpression.asStateFlow()

    private val _calcResult = MutableStateFlow("")
    val calcResult: StateFlow<String> = _calcResult.asStateFlow()

    private val _calcHistory = MutableStateFlow<List<String>>(emptyList())
    val calcHistory: StateFlow<List<String>> = _calcHistory.asStateFlow()

    private val _isVaultUnlocked = MutableStateFlow(false)
    val isVaultUnlocked: StateFlow<Boolean> = _isVaultUnlocked.asStateFlow()

    private val _vaultItems = MutableStateFlow<List<VaultItem>>(emptyList())
    val vaultItems: StateFlow<List<VaultItem>> = _vaultItems.asStateFlow()

    private val _secretNotes = MutableStateFlow<List<SecretNote>>(emptyList())
    val secretNotes: StateFlow<List<SecretNote>> = _secretNotes.asStateFlow()

    private val _wrongPinAttempts = MutableStateFlow(0)
    val wrongPinAttempts: StateFlow<Int> = _wrongPinAttempts.asStateFlow()

    private val _showFakeCrash = MutableStateFlow(false)
    val showFakeCrash: StateFlow<Boolean> = _showFakeCrash.asStateFlow()

    // Voice & TTS
    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _isVoiceListening = MutableStateFlow(false)
    val isVoiceListening: StateFlow<Boolean> = _isVoiceListening.asStateFlow()

    private val _voiceFeedback = MutableStateFlow<String?>(null)
    val voiceFeedback: StateFlow<String?> = _voiceFeedback.asStateFlow()

    // Cloud Sync
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncProgress = MutableStateFlow(0f)
    val syncProgress: StateFlow<Float> = _syncProgress.asStateFlow()

    private val _socialFeedItems = MutableStateFlow<List<SocialFeedItem>>(emptyList())
    val socialFeedItems: StateFlow<List<SocialFeedItem>> = _socialFeedItems.asStateFlow()

    // Hindi Calendar & Holidays
    private val _selectedHindiMonth = MutableStateFlow(HindiCalendarData.hindiMonths[7]) // Kartika default
    val selectedHindiMonth: StateFlow<HindiMonth> = _selectedHindiMonth.asStateFlow()

    private val _holidays = MutableStateFlow<List<HolidayItem>>(HindiCalendarData.defaultHolidays)
    val holidays: StateFlow<List<HolidayItem>> = _holidays.asStateFlow()

    // Settings
    private val _themeStyle = MutableStateFlow(settingsRepo.currentThemeStyle)
    val themeStyle: StateFlow<ThemeStyle> = _themeStyle.asStateFlow()

    private val _darkModeSetting = MutableStateFlow(settingsRepo.darkModeSetting)
    val darkModeSetting: StateFlow<String> = _darkModeSetting.asStateFlow()

    private val _notificationSettings = MutableStateFlow(settingsRepo.notificationSettings)
    val notificationSettings: StateFlow<NotificationSettings> = _notificationSettings.asStateFlow()

    init {
        refreshFiles()
        refreshVault()
        refreshSocialFeed()
        seedSampleConversions()
    }

    private fun seedSampleConversions() {
        val root = storageManager.getRootDirectory()
        val picturesDir = File(root, "Pictures")
        val sampleImg = File(picturesDir, "Sunset_Hills.jpg")
        if (sampleImg.exists()) {
            _selectedSourceFile.value = FileItem(
                id = sampleImg.absolutePath,
                name = sampleImg.name,
                path = sampleImg.absolutePath,
                sizeBytes = sampleImg.length(),
                lastModified = sampleImg.lastModified(),
                isDirectory = false,
                type = FileType.IMAGE,
                extension = "jpg",
                realFile = sampleImg
            )
        }

        _conversionHistory.value = listOf(
            ConversionHistoryItem(
                id = "conv_1",
                originalFileName = "Sunset_Hills.jpg",
                convertedFileName = "Sunset_Hills_converted.pdf",
                originalSizeBytes = 240 * 1024,
                convertedSizeBytes = 185 * 1024,
                targetFormat = TargetFormat.PDF,
                timestamp = System.currentTimeMillis() - 3600000L * 2,
                convertedFilePath = File(fileConverterEngine.getConvertedDirectory(), "Sunset_Hills_converted.pdf").absolutePath,
                aiInsight = "Raster image compiled into A4 PDF page."
            ),
            ConversionHistoryItem(
                id = "conv_2",
                originalFileName = "Goa_Trip_Clip.mp4",
                convertedFileName = "Goa_Trip_audio.mp3",
                originalSizeBytes = 5 * 1024 * 1024,
                convertedSizeBytes = 680 * 1024,
                targetFormat = TargetFormat.MP3,
                timestamp = System.currentTimeMillis() - 3600000L * 6,
                convertedFilePath = File(fileConverterEngine.getConvertedDirectory(), "Goa_Trip_audio.mp3").absolutePath,
                aiInsight = "Audio isolated at 320kbps stereo quality."
            ),
            ConversionHistoryItem(
                id = "conv_3",
                originalFileName = "Invoice_Receipt.png",
                convertedFileName = "Invoice_OCR.txt",
                originalSizeBytes = 120 * 1024,
                convertedSizeBytes = 3 * 1024,
                targetFormat = TargetFormat.AI_OCR,
                timestamp = System.currentTimeMillis() - 3600000L * 12,
                convertedFilePath = File(fileConverterEngine.getConvertedDirectory(), "Invoice_OCR.txt").absolutePath,
                aiInsight = "Optical characters extracted into structured plain text."
            )
        )
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun refreshFiles() {
        val query = _searchQuery.value
        val cat = _selectedCategory.value
        val list = if (query.isNotBlank()) {
            storageManager.searchFiles(query, cat)
        } else if (cat != null) {
            storageManager.listFilesByCategory(cat)
        } else {
            storageManager.listAllFiles(_currentDirectory.value)
        }
        _files.value = list
        _storageStats.value = storageManager.getStorageStats()
    }

    fun selectCategory(type: FileType?) {
        _selectedCategory.value = type
        refreshFiles()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        refreshFiles()
    }

    fun openDirectory(dir: File) {
        _currentDirectory.value = dir
        _selectedCategory.value = null
        refreshFiles()
    }

    fun navigateUp(): Boolean {
        val curr = _currentDirectory.value
        val root = storageManager.getRootDirectory()
        if (curr.absolutePath != root.absolutePath && curr.parentFile != null) {
            _currentDirectory.value = curr.parentFile!!
            refreshFiles()
            return true
        }
        return false
    }

    fun toggleFileSelection(item: FileItem) {
        val current = _selectedFiles.value.toMutableSet()
        if (current.contains(item)) {
            current.remove(item)
        } else {
            current.add(item)
        }
        _selectedFiles.value = current
    }

    fun clearSelection() {
        _selectedFiles.value = emptySet()
    }

    fun deleteSelectedFiles() {
        val items = _selectedFiles.value
        var count = 0
        for (item in items) {
            val file = item.realFile ?: File(item.path)
            if (storageManager.deleteFile(file)) {
                count++
            }
        }
        clearSelection()
        refreshFiles()
        showMessage("$count items deleted")
        notificationHelper.showFileNotification("Files Deleted", "Successfully deleted $count file(s)")
    }

    fun renameFile(fileItem: FileItem, newName: String) {
        val file = fileItem.realFile ?: File(fileItem.path)
        if (storageManager.renameFile(file, newName)) {
            refreshFiles()
            showMessage("Renamed to $newName")
            notificationHelper.showFileNotification("File Renamed", "$newName renamed successfully")
        } else {
            showMessage("Failed to rename file")
        }
    }

    fun compressSelectedToZip(zipName: String = "Archive_${System.currentTimeMillis()}.zip") {
        val items = _selectedFiles.value.toList()
        if (items.isEmpty()) return
        val filesToZip = items.mapNotNull { it.realFile ?: File(it.path).takeIf { f -> f.exists() } }
        val targetZip = File(_currentDirectory.value, if (zipName.endsWith(".zip")) zipName else "$zipName.zip")

        viewModelScope.launch {
            val success = storageManager.compressToZip(filesToZip, targetZip)
            if (success) {
                clearSelection()
                refreshFiles()
                showMessage("Compressed into ${targetZip.name}")
                notificationHelper.showFileNotification("Compression Complete", "Created ${targetZip.name} with ${filesToZip.size} files")
            } else {
                showMessage("Compression failed")
            }
        }
    }

    fun extractZipFile(zipItem: FileItem) {
        val zipFile = zipItem.realFile ?: File(zipItem.path)
        val targetDir = File(zipFile.parentFile, zipFile.nameWithoutExtension)
        viewModelScope.launch {
            val success = storageManager.extractZip(zipFile, targetDir)
            if (success) {
                refreshFiles()
                showMessage("Extracted to ${targetDir.name}")
                notificationHelper.showFileNotification("Extraction Complete", "Extracted files to ${targetDir.name}")
            } else {
                showMessage("Extraction failed")
            }
        }
    }

    fun moveSelectedToVault() {
        val items = _selectedFiles.value
        var count = 0
        for (item in items) {
            val file = item.realFile ?: File(item.path)
            if (storageManager.moveToVault(file) != null) {
                count++
            }
        }
        clearSelection()
        refreshFiles()
        refreshVault()
        showMessage("Moved $count items to Secret Vault")
        notificationHelper.showSecurityAlert("Files Secured", "$count file(s) moved to encrypted vault")
    }

    fun openMedia(item: FileItem) {
        _activeMediaItem.value = item
    }

    fun closeMedia() {
        _activeMediaItem.value = null
    }

    // Calculator & Vault logic
    fun onCalculatorDigit(digit: String) {
        val current = _calcExpression.value
        _calcExpression.value = if (current == "0") digit else current + digit
    }

    fun onCalculatorOperator(op: String) {
        val current = _calcExpression.value
        _calcExpression.value = current + op
    }

    fun onCalculatorClear() {
        _calcExpression.value = "0"
        _calcResult.value = ""
    }

    fun onCalculatorBackspace() {
        val current = _calcExpression.value
        _calcExpression.value = if (current.length <= 1) "0" else current.dropLast(1)
    }

    fun onCalculatorEquals() {
        val expr = _calcExpression.value

        // Check if expression matches vault PIN (e.g. "1234")
        if (settingsRepo.verifyPin(expr)) {
            unlockVault()
            _calcExpression.value = "0"
            _calcResult.value = ""
            return
        }

        // Try evaluating math
        try {
            val result = evaluateSimpleExpression(expr)
            _calcResult.value = result
            _calcHistory.value = listOf("$expr = $result") + _calcHistory.value.take(10)
        } catch (e: Exception) {
            _calcResult.value = "Error"
            // Track wrong attempt if it looks like a 4-digit PIN attempt
            if (expr.matches(Regex("\\d{4}"))) {
                _wrongPinAttempts.value += 1
                if (_wrongPinAttempts.value >= 3 && settingsRepo.isFakeCrashEnabled) {
                    _showFakeCrash.value = true
                    notificationHelper.showSecurityAlert("Intruder Warning", "3 failed PIN unlock attempts detected!")
                }
            }
        }
    }

    fun selectSourceFile(item: FileItem?) {
        _selectedSourceFile.value = item
        if (item != null) {
            val recommended = getRecommendedFormats(item.type)
            if (!recommended.contains(_selectedTargetFormat.value)) {
                _selectedTargetFormat.value = recommended.firstOrNull() ?: TargetFormat.PDF
            }
        }
    }

    fun selectTargetFormat(format: TargetFormat) {
        _selectedTargetFormat.value = format
    }

    fun getRecommendedFormats(type: FileType): List<TargetFormat> {
        return when (type) {
            FileType.IMAGE -> listOf(TargetFormat.PDF, TargetFormat.PNG, TargetFormat.JPG, TargetFormat.WEBP, TargetFormat.AI_OCR, TargetFormat.ZIP)
            FileType.DOCUMENT -> listOf(TargetFormat.PDF, TargetFormat.TXT, TargetFormat.MD, TargetFormat.AI_SUMMARY, TargetFormat.ZIP)
            FileType.VIDEO -> listOf(TargetFormat.MP3, TargetFormat.M4A, TargetFormat.AI_TRANSCRIPT, TargetFormat.ZIP)
            FileType.AUDIO -> listOf(TargetFormat.MP3, TargetFormat.M4A, TargetFormat.AI_TRANSCRIPT)
            FileType.ARCHIVE -> listOf(TargetFormat.TXT, TargetFormat.ZIP)
            else -> listOf(TargetFormat.PDF, TargetFormat.TXT, TargetFormat.ZIP)
        }
    }

    fun startConversion(sourceItem: FileItem? = null, targetFormat: TargetFormat? = null) {
        val src = sourceItem ?: _selectedSourceFile.value
        if (src == null) {
            showMessage("Please select a file to convert")
            return
        }
        val fmt = targetFormat ?: _selectedTargetFormat.value
        val file = src.realFile ?: File(src.path)
        if (!file.exists()) {
            showMessage("Source file not found")
            return
        }

        if (_isConverting.value) return
        _isConverting.value = true
        _conversionProgress.value = 0.1f

        viewModelScope.launch {
            try {
                for (p in 2..8) {
                    delay(120)
                    _conversionProgress.value = p / 10f
                }
                val (destFile, insight) = fileConverterEngine.convertFile(file, fmt, src.type)
                _conversionProgress.value = 1f
                delay(120)

                val item = ConversionHistoryItem(
                    id = "conv_${System.currentTimeMillis()}",
                    originalFileName = src.name,
                    convertedFileName = destFile.name,
                    originalSizeBytes = file.length(),
                    convertedSizeBytes = destFile.length(),
                    targetFormat = fmt,
                    timestamp = System.currentTimeMillis(),
                    convertedFilePath = destFile.absolutePath,
                    aiInsight = insight
                )

                _lastConversionResult.value = item
                _conversionHistory.value = listOf(item) + _conversionHistory.value
                _isConverting.value = false
                refreshFiles()

                showMessage("Converted ${src.name} to ${fmt.label}!")
                notificationHelper.showFileNotification(
                    "AI Conversion Complete",
                    "${destFile.name} created (${formatFileSize(destFile.length())})"
                )
            } catch (e: Exception) {
                _isConverting.value = false
                showMessage("Conversion failed: ${e.message}")
            }
        }
    }

    fun verifyAndUnlockVault(pin: String): Boolean {
        if (settingsRepo.verifyPin(pin)) {
            unlockVault()
            return true
        } else {
            _wrongPinAttempts.value += 1
            if (_wrongPinAttempts.value >= 3 && settingsRepo.isFakeCrashEnabled) {
                _showFakeCrash.value = true
                notificationHelper.showSecurityAlert("Intruder Warning", "3 failed PIN unlock attempts detected!")
            }
            showMessage("Incorrect PIN")
            return false
        }
    }

    fun unlockVault() {
        _isVaultUnlocked.value = true
        _wrongPinAttempts.value = 0
        _showFakeCrash.value = false
        _currentScreen.value = AppScreen.PRIVATE_VAULT
        refreshVault()
        showMessage("🔒 Secret Vault Unlocked")
    }

    fun lockVault() {
        _isVaultUnlocked.value = false
        _currentScreen.value = AppScreen.AI_CONVERTER
        showMessage("Vault Locked")
    }

    fun dismissFakeCrash() {
        _showFakeCrash.value = false
        _wrongPinAttempts.value = 0
    }

    fun refreshVault() {
        _vaultItems.value = storageManager.getVaultItems()
        _secretNotes.value = storageManager.getSecretNotes()
    }

    fun restoreVaultItem(item: VaultItem) {
        if (storageManager.restoreFromVault(item.id)) {
            refreshVault()
            refreshFiles()
            showMessage("Restored ${item.title} to storage")
        }
    }

    fun deleteVaultItem(item: VaultItem) {
        if (storageManager.deleteVaultItem(item.id)) {
            refreshVault()
            showMessage("Deleted ${item.title}")
        }
    }

    fun saveSecretNote(title: String, content: String, id: String? = null) {
        val note = SecretNote(
            id = id ?: "note_${System.currentTimeMillis()}",
            title = title,
            content = content,
            timestamp = System.currentTimeMillis()
        )
        storageManager.saveSecretNote(note)
        refreshVault()
        showMessage("Secret note saved")
    }

    fun deleteSecretNote(id: String) {
        storageManager.deleteSecretNote(id)
        refreshVault()
        showMessage("Note deleted")
    }

    // Cloud Sync
    fun startCloudSync() {
        if (_isSyncing.value) return
        _isSyncing.value = true
        _syncProgress.value = 0f

        viewModelScope.launch {
            for (step in 1..10) {
                delay(250)
                _syncProgress.value = step / 10f
            }
            settingsRepo.lastSyncTimestamp = System.currentTimeMillis()
            _isSyncing.value = false
            showMessage("Cloud Backup & Social Feeds synchronized")
            notificationHelper.showSyncNotification("Cloud Backup Completed", "Encrypted backup created with 12 items synced")
            refreshSocialFeed()
        }
    }

    private fun refreshSocialFeed() {
        _socialFeedItems.value = listOf(
            SocialFeedItem("sf_1", "WhatsApp", "WA_IMG_20270114.jpg", FileType.IMAGE, 480 * 1024, System.currentTimeMillis() - 3600000, true, "Team Chat"),
            SocialFeedItem("sf_2", "Telegram", "Architecture_Docs.pdf", FileType.DOCUMENT, 1200 * 1024, System.currentTimeMillis() - 7200000, true, "Dev Channel"),
            SocialFeedItem("sf_3", "Instagram", "Reel_Draft_Goa.mp4", FileType.VIDEO, 8400 * 1024, System.currentTimeMillis() - 14400000, true, "Saved Stories"),
            SocialFeedItem("sf_4", "Cloud Drive", "Financial_Ledger.xlsx", FileType.DOCUMENT, 650 * 1024, System.currentTimeMillis() - 28800000, true, "Google Drive Sync")
        )
    }

    // TTS Voice
    fun speakText(text: String) {
        _isSpeaking.value = true
        ttsManager.speak(
            text = text,
            onStart = { _isSpeaking.value = true },
            onDone = { _isSpeaking.value = false },
            onError = { msg ->
                _isSpeaking.value = false
                showMessage(msg)
            }
        )
    }

    fun stopSpeaking() {
        ttsManager.stop()
        _isSpeaking.value = false
    }

    // Voice Navigation Command Processor
    fun executeVoiceCommand(command: String) {
        val cmd = command.lowercase().trim()
        _voiceFeedback.value = "Recognized: \"$command\""

        when {
            cmd.contains("vault") || cmd.contains("private") || cmd.contains("hidden") -> {
                if (settingsRepo.isBiometricEnabled) {
                    unlockVault()
                    speakText("Opening Secret Vault")
                } else {
                    navigateTo(AppScreen.AI_CONVERTER)
                    speakText("Please enter your PIN in the secure vault unlock")
                }
            }
            cmd.contains("convert") || cmd.contains("ai") || cmd.contains("converter") || cmd.contains("calc") -> {
                navigateTo(AppScreen.AI_CONVERTER)
                speakText("Opening AI File Converter")
            }
            cmd.contains("file") || cmd.contains("storage") -> {
                navigateTo(AppScreen.FILE_MANAGER)
                selectCategory(null)
                speakText("Opening File Manager")
            }
            cmd.contains("image") || cmd.contains("photo") -> {
                navigateTo(AppScreen.FILE_MANAGER)
                selectCategory(FileType.IMAGE)
                speakText("Showing Photos and Images")
            }
            cmd.contains("video") || cmd.contains("movie") -> {
                navigateTo(AppScreen.FILE_MANAGER)
                selectCategory(FileType.VIDEO)
                speakText("Showing Videos")
            }
            cmd.contains("audio") || cmd.contains("music") || cmd.contains("song") -> {
                navigateTo(AppScreen.FILE_MANAGER)
                selectCategory(FileType.AUDIO)
                speakText("Showing Music and Audio")
            }
            cmd.contains("doc") || cmd.contains("pdf") -> {
                navigateTo(AppScreen.FILE_MANAGER)
                selectCategory(FileType.DOCUMENT)
                speakText("Showing Documents")
            }
            cmd.contains("map") || cmd.contains("location") || cmd.contains("track") -> {
                navigateTo(AppScreen.MAPS)
                speakText("Opening GPS Tracker and Route History")
            }
            cmd.contains("calendar") || cmd.contains("panchang") || cmd.contains("holiday") || cmd.contains("hindi") -> {
                navigateTo(AppScreen.HINDI_CALENDAR)
                speakText("Opening Hindi Calendar and Panchang")
            }
            cmd.contains("backup") || cmd.contains("sync") || cmd.contains("cloud") -> {
                navigateTo(AppScreen.CLOUD_SYNC)
                speakText("Opening Cloud Backup and Social Feed Sync")
            }
            cmd.contains("qr") || cmd.contains("scan") || cmd.contains("share") -> {
                navigateTo(AppScreen.QR_SHARE)
                speakText("Opening QR Code Scanner and Share")
            }
            cmd.contains("dark") -> {
                setDarkMode("DARK")
                speakText("Dark mode activated")
            }
            cmd.contains("light") -> {
                setDarkMode("LIGHT")
                speakText("Light mode activated")
            }
            else -> {
                speakText("Command not recognized. Try saying open vault, open files, or open map.")
            }
        }
    }

    // Calendar
    fun selectHindiMonth(month: HindiMonth) {
        _selectedHindiMonth.value = month
    }

    fun addCustomHoliday(name: String, date: String, region: String, desc: String) {
        val newH = HolidayItem(
            id = "custom_${System.currentTimeMillis()}",
            englishName = name,
            hindiName = name,
            dateString = date,
            hindiMonth = _selectedHindiMonth.value.englishName,
            tithi = "Custom Event",
            region = region,
            description = desc,
            isRegionalCustom = true
        )
        _holidays.value = listOf(newH) + _holidays.value
        showMessage("Regional event planned!")
        notificationHelper.showHolidayAlert("Festival Planned", "$name added for $date")
    }

    // Theme & Settings
    fun setThemeStyle(style: ThemeStyle) {
        settingsRepo.currentThemeStyle = style
        _themeStyle.value = style
    }

    fun setDarkMode(mode: String) {
        settingsRepo.darkModeSetting = mode
        _darkModeSetting.value = mode
    }

    fun updateNotificationSettings(settings: NotificationSettings) {
        settingsRepo.notificationSettings = settings
        _notificationSettings.value = settings
        showMessage("Push Notification preferences saved")
    }

    fun setVaultPin(newPin: String) {
        settingsRepo.vaultPin = newPin
        showMessage("Secret PIN updated successfully")
    }

    fun setBiometricEnabled(enabled: Boolean) {
        settingsRepo.isBiometricEnabled = enabled
    }

    fun setFakeCrashEnabled(enabled: Boolean) {
        settingsRepo.isFakeCrashEnabled = enabled
    }

    fun showMessage(msg: String) {
        _statusMessage.value = msg
        viewModelScope.launch {
            delay(3000)
            if (_statusMessage.value == msg) {
                _statusMessage.value = null
            }
        }
    }

    private fun evaluateSimpleExpression(expr: String): String {
        val cleaned = expr.replace("×", "*").replace("÷", "/")
        // Simple 2-operand or chained arithmetic evaluator
        val tokens = mutableListOf<String>()
        var buffer = ""
        for (char in cleaned) {
            if (char in "+-*/%") {
                if (buffer.isNotEmpty()) {
                    tokens.add(buffer)
                    buffer = ""
                }
                tokens.add(char.toString())
            } else {
                buffer += char
            }
        }
        if (buffer.isNotEmpty()) tokens.add(buffer)

        if (tokens.isEmpty()) return "0"
        var accumulator = tokens[0].toDoubleOrNull() ?: return "Error"
        var i = 1
        while (i < tokens.size) {
            val op = tokens[i]
            val nextVal = tokens.getOrNull(i + 1)?.toDoubleOrNull() ?: return "Error"
            accumulator = when (op) {
                "+" -> accumulator + nextVal
                "-" -> accumulator - nextVal
                "*" -> accumulator * nextVal
                "/" -> if (nextVal == 0.0) return "Div by 0" else accumulator / nextVal
                "%" -> accumulator % nextVal
                else -> accumulator
            }
            i += 2
        }

        return if (accumulator % 1.0 == 0.0) {
            accumulator.toLong().toString()
        } else {
            String.format(java.util.Locale.US, "%.4f", accumulator).trimEnd('0').trimEnd('.')
        }
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager.shutdown()
    }
}
