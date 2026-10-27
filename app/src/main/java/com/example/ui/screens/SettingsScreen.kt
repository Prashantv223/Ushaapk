package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ThemeStyle
import com.example.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    val currentTheme by viewModel.themeStyle.collectAsState()
    val darkMode by viewModel.darkModeSetting.collectAsState()
    val notifSettings by viewModel.notificationSettings.collectAsState()

    var testTtsText by remember { mutableStateOf("Welcome to CLR File Manager. All files, routes, and secret vault data are encrypted.") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Theme Style Selector
            Text("Custom Themes & Accent Colors", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            val themeOptions = listOf(
                Pair("Slate Cyan", ThemeStyle.SLATE_CYAN),
                Pair("Emerald Vault", ThemeStyle.EMERALD_VAULT),
                Pair("Midnight Blue", ThemeStyle.MIDNIGHT_BLUE),
                Pair("Sunset Amber", ThemeStyle.SUNSET_AMBER),
                Pair("AMOLED Dark", ThemeStyle.AMOLED_DARK)
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(themeOptions) { (name, style) ->
                    val isSelected = currentTheme == style
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier
                            .clickable { viewModel.setThemeStyle(style) }
                            .testTag("theme_card_${style.name}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(getThemePreviewColor(style))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(name, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
            }
        }

        item {
            // Dark Mode Selector
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Appearance Mode", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        listOf("DARK", "LIGHT", "SYSTEM").forEach { mode ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = darkMode == mode,
                                    onClick = { viewModel.setDarkMode(mode) },
                                    modifier = Modifier.testTag("radio_mode_$mode")
                                )
                                Text(mode.lowercase().replaceFirstChar { it.uppercase() }, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }

        item {
            // Push Notification Settings Card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Custom Push Notification Settings", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Personalize priority levels and alerts for files, sync, and security:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Priority Selector
                    Text("Notification Priority:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("HIGH", "DEFAULT", "LOW").forEach { pri ->
                            FilterChip(
                                selected = notifSettings.priorityLevel == pri,
                                onClick = {
                                    viewModel.updateNotificationSettings(notifSettings.copy(priorityLevel = pri))
                                },
                                label = { Text(pri, fontSize = 11.sp) },
                                modifier = Modifier.testTag("notif_pri_$pri")
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                    // Switches
                    NotificationToggleRow("Sound Alerts", "Play ringtone sound on alerts", notifSettings.soundEnabled) {
                        viewModel.updateNotificationSettings(notifSettings.copy(soundEnabled = it))
                    }
                    NotificationToggleRow("Vibration Feedback", "Vibrate device on file change", notifSettings.vibrationEnabled) {
                        viewModel.updateNotificationSettings(notifSettings.copy(vibrationEnabled = it))
                    }
                    NotificationToggleRow("File Operations", "Notify when files are moved or compressed", notifSettings.notifyFileOperations) {
                        viewModel.updateNotificationSettings(notifSettings.copy(notifyFileOperations = it))
                    }
                    NotificationToggleRow("Cloud Sync Status", "Notify upon backup or feed sync completion", notifSettings.notifySyncStatus) {
                        viewModel.updateNotificationSettings(notifSettings.copy(notifySyncStatus = it))
                    }
                    NotificationToggleRow("Security & Intruder Alerts", "Alert when unauthorized PIN is attempted", notifSettings.notifySecurityAlerts) {
                        viewModel.updateNotificationSettings(notifSettings.copy(notifySecurityAlerts = it))
                    }
                    NotificationToggleRow("Daily Panchang & Festival Reminders", "Morning notification for tithi & holidays", notifSettings.notifyDailyHolidays) {
                        viewModel.updateNotificationSettings(notifSettings.copy(notifyDailyHolidays = it))
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = {
                            viewModel.notificationHelper.showFileNotification("CLR Notification Test", "Custom priority is ${notifSettings.priorityLevel}. Audio & alerts active.")
                            viewModel.showMessage("Sent test notification")
                        },
                        modifier = Modifier.fillMaxWidth().testTag("btn_test_notification")
                    ) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Send Test Notification")
                    }
                }
            }
        }

        item {
            // Gemini Flash TTS Tester Card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Gemini Flash TTS & Voice Assistant", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Model: gemini-3.8-flash-tts with system TTS engine fallback.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = testTtsText,
                        onValueChange = { testTtsText = it },
                        modifier = Modifier.fillMaxWidth().testTag("tts_test_input"),
                        maxLines = 3
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { viewModel.speakText(testTtsText) },
                        modifier = Modifier.fillMaxWidth().testTag("btn_test_speak")
                    ) {
                        Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Speak Text Aloud")
                    }
                }
            }
        }

        item {
            // About CLR
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("CLR (Calculator Locker & Resource Manager)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Version 1.0.0 • E2E Encrypted • Built with Jetpack Compose & Material 3", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun NotificationToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

fun getThemePreviewColor(style: ThemeStyle): Color {
    return when (style) {
        ThemeStyle.SLATE_CYAN -> Color(0xFF06B6D4)
        ThemeStyle.EMERALD_VAULT -> Color(0xFF10B981)
        ThemeStyle.MIDNIGHT_BLUE -> Color(0xFF3B82F6)
        ThemeStyle.SUNSET_AMBER -> Color(0xFFF59E0B)
        ThemeStyle.AMOLED_DARK -> Color(0xFFA855F7)
    }
}
