package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppScreen
import com.example.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    viewModel: MainViewModel,
    currentScreen: AppScreen,
    onVoiceClick: () -> Unit
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        ),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "CLR",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = when (currentScreen) {
                            AppScreen.FILE_MANAGER -> "File Manager"
                            AppScreen.AI_CONVERTER -> "AI File Converter"
                            AppScreen.PRIVATE_VAULT -> "🔒 Secret Vault"
                            AppScreen.MAPS -> "GPS Tracker & Routes"
                            AppScreen.HINDI_CALENDAR -> "पंचांग & Calendar"
                            AppScreen.CLOUD_SYNC -> "Cloud Backup & Feeds"
                            AppScreen.QR_SHARE -> "QR Scanner & Share"
                            AppScreen.SETTINGS -> "Preferences"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = when (currentScreen) {
                            AppScreen.FILE_MANAGER -> "Encrypted & Smart Storage"
                            AppScreen.AI_CONVERTER -> "Image • PDF • Video • OCR"
                            AppScreen.PRIVATE_VAULT -> "AES-256 Protected"
                            AppScreen.MAPS -> "Real-time route history"
                            AppScreen.HINDI_CALENDAR -> "Vikram Samvat 2083"
                            AppScreen.CLOUD_SYNC -> "Encrypted cloud hub"
                            AppScreen.QR_SHARE -> "Offline file transfers"
                            AppScreen.SETTINGS -> "Themes, alerts & security"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        actions = {
            // Voice Command Assistant Button
            IconButton(
                onClick = onVoiceClick,
                modifier = Modifier.testTag("top_voice_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Voice Navigation",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // Quick AI Converter / Vault switch
            if (currentScreen != AppScreen.AI_CONVERTER && currentScreen != AppScreen.PRIVATE_VAULT) {
                IconButton(
                    onClick = { viewModel.navigateTo(AppScreen.AI_CONVERTER) },
                    modifier = Modifier.testTag("top_converter_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Transform,
                        contentDescription = "AI File Converter",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
            } else if (currentScreen == AppScreen.PRIVATE_VAULT) {
                IconButton(
                    onClick = { viewModel.lockVault() },
                    modifier = Modifier.testTag("top_lock_vault_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Lock Vault",
                        tint = Color(0xFFEF4444)
                    )
                }
            }

            // Cloud Sync icon
            if (currentScreen != AppScreen.CLOUD_SYNC) {
                IconButton(
                    onClick = { viewModel.navigateTo(AppScreen.CLOUD_SYNC) },
                    modifier = Modifier.testTag("top_cloud_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudSync,
                        contentDescription = "Cloud Backup",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    )
}

@Composable
fun AppBottomNav(
    currentScreen: AppScreen,
    onSelectScreen: (AppScreen) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    ) {
        NavigationBarItem(
            selected = currentScreen == AppScreen.FILE_MANAGER,
            onClick = { onSelectScreen(AppScreen.FILE_MANAGER) },
            icon = { Icon(Icons.Default.Folder, contentDescription = "Files") },
            label = { Text("Files", fontSize = 11.sp) },
            modifier = Modifier.testTag("nav_files")
        )
        NavigationBarItem(
            selected = currentScreen == AppScreen.AI_CONVERTER || currentScreen == AppScreen.PRIVATE_VAULT,
            onClick = { onSelectScreen(AppScreen.AI_CONVERTER) },
            icon = {
                Icon(
                    if (currentScreen == AppScreen.PRIVATE_VAULT) Icons.Default.LockOpen else Icons.Default.Transform,
                    contentDescription = "AI Converter"
                )
            },
            label = { Text(if (currentScreen == AppScreen.PRIVATE_VAULT) "Vault" else "AI Convert", fontSize = 11.sp) },
            modifier = Modifier.testTag("nav_ai_converter")
        )
        NavigationBarItem(
            selected = currentScreen == AppScreen.MAPS,
            onClick = { onSelectScreen(AppScreen.MAPS) },
            icon = { Icon(Icons.Default.Map, contentDescription = "Maps") },
            label = { Text("Map", fontSize = 11.sp) },
            modifier = Modifier.testTag("nav_maps")
        )
        NavigationBarItem(
            selected = currentScreen == AppScreen.HINDI_CALENDAR,
            onClick = { onSelectScreen(AppScreen.HINDI_CALENDAR) },
            icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Hindi Calendar") },
            label = { Text("पंचांग", fontSize = 11.sp) },
            modifier = Modifier.testTag("nav_calendar")
        )
        NavigationBarItem(
            selected = currentScreen == AppScreen.QR_SHARE,
            onClick = { onSelectScreen(AppScreen.QR_SHARE) },
            icon = { Icon(Icons.Default.QrCode, contentDescription = "QR Share") },
            label = { Text("QR", fontSize = 11.sp) },
            modifier = Modifier.testTag("nav_qr")
        )
        NavigationBarItem(
            selected = currentScreen == AppScreen.SETTINGS,
            onClick = { onSelectScreen(AppScreen.SETTINGS) },
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            label = { Text("Settings", fontSize = 11.sp) },
            modifier = Modifier.testTag("nav_settings")
        )
    }
}
