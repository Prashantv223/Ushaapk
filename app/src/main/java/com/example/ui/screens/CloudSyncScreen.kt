package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.data.model.SocialFeedItem
import com.example.data.model.formatFileSize
import com.example.ui.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloudSyncScreen(viewModel: MainViewModel) {
    val isSyncing by viewModel.isSyncing.collectAsState()
    val syncProgress by viewModel.syncProgress.collectAsState()
    val socialFeeds by viewModel.socialFeedItems.collectAsState()

    var autoSyncEnabled by remember { mutableStateOf(viewModel.settingsRepo.isCloudAutoSync) }
    var selectedFeedSource by remember { mutableStateOf("All") }
    val sources = listOf("All", "WhatsApp", "Telegram", "Instagram", "Cloud Drive")

    val filteredFeeds = if (selectedFeedSource == "All") {
        socialFeeds
    } else {
        socialFeeds.filter { it.platform.equals(selectedFeedSource, ignoreCase = true) }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            // Encrypted Cloud Storage Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudQueue,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Encrypted Cloud Backup", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("AES-256 E2E Encryption", fontSize = 11.sp, color = Color(0xFF10B981))
                            }
                        }

                        if (isSyncing) {
                            CircularProgressIndicator(
                                progress = { syncProgress },
                                modifier = Modifier.size(28.dp),
                                strokeWidth = 3.dp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "4.2 GB used of 15.0 GB Cloud Quota",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { 0.28f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surface
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Last synced: ${SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(viewModel.settingsRepo.lastSyncTimestamp))}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Button(
                            onClick = { viewModel.startCloudSync() },
                            enabled = !isSyncing,
                            modifier = Modifier.testTag("btn_sync_now")
                        ) {
                            Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isSyncing) "Syncing ${(syncProgress * 100).toInt()}%" else "Backup Now")
                        }
                    }
                }
            }
        }

        item {
            // Auto Sync & Social Feeds Switch Card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Automatic Background Sync", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text("Keep social downloads and modified files synced with your cloud locker", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = autoSyncEnabled,
                        onCheckedChange = {
                            autoSyncEnabled = it
                            viewModel.settingsRepo.isCloudAutoSync = it
                        },
                        modifier = Modifier.testTag("switch_auto_sync")
                    )
                }
            }
        }

        item {
            // Social Media Feed Sync Filter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Social Media Feeds Sync", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("${filteredFeeds.size} items", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                sources.forEach { src ->
                    FilterChip(
                        selected = selectedFeedSource == src,
                        onClick = { selectedFeedSource = src },
                        label = { Text(src, fontSize = 11.sp) }
                    )
                }
            }
        }

        items(filteredFeeds, key = { it.id }) { item ->
            SocialFeedRow(
                item = item,
                onDownload = {
                    viewModel.showMessage("Downloaded ${item.fileName} to local storage")
                    viewModel.notificationHelper.showFileNotification("Download Finished", "${item.fileName} imported from ${item.platform}")
                }
            )
        }
    }
}

@Composable
fun SocialFeedRow(item: SocialFeedItem, onDownload: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(getPlatformColor(item.platform).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (item.platform) {
                        "WhatsApp" -> Icons.Default.Chat
                        "Telegram" -> Icons.Default.Send
                        "Instagram" -> Icons.Default.CameraAlt
                        else -> Icons.Default.Cloud
                    },
                    contentDescription = null,
                    tint = getPlatformColor(item.platform),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(item.fileName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, maxLines = 1)
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${item.platform} • ${item.senderOrSource} • ${formatFileSize(item.sizeBytes)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = onDownload,
                modifier = Modifier.testTag("feed_download_${item.id}")
            ) {
                Icon(Icons.Default.Download, contentDescription = "Download to Storage", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

fun getPlatformColor(platform: String): Color {
    return when (platform) {
        "WhatsApp" -> Color(0xFF25D366)
        "Telegram" -> Color(0xFF0088CC)
        "Instagram" -> Color(0xFFE1306C)
        else -> Color(0xFF4285F4)
    }
}
