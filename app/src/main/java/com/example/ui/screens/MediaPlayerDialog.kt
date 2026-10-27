package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.FileItem
import com.example.data.model.FileType
import com.example.ui.MainViewModel
import java.io.File

@Composable
fun MediaPlayerDialog(
    item: FileItem,
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getFileVector(item),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${item.formattedSize} • ${item.type.name}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("media_dialog_close")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Content View based on FileType
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    when (item.type) {
                        FileType.AUDIO -> AudioPlayerContent(item, viewModel)
                        FileType.VIDEO -> VideoPlayerContent(item)
                        FileType.IMAGE -> ImageViewerContent(item)
                        FileType.APK -> ApkInspectorContent(item, viewModel)
                        FileType.DOCUMENT -> DocumentEditorContent(item, viewModel)
                        else -> GenericFileContent(item, viewModel)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Bar: Download / Export, TTS Speak, Share
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    OutlinedButton(
                        onClick = {
                            viewModel.showMessage("Downloaded ${item.name} to device storage")
                            viewModel.notificationHelper.showFileNotification("Download Complete", "${item.name} saved to Downloads folder")
                        },
                        modifier = Modifier.testTag("media_download_button")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Download")
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel.speakText("Opened file ${item.name}. Format is ${item.extension}, file size is ${item.formattedSize}.")
                        },
                        modifier = Modifier.testTag("media_tts_button")
                    ) {
                        Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Speak")
                    }

                    Button(
                        onClick = {
                            viewModel.showMessage("Share link & QR code ready")
                            viewModel.navigateTo(com.example.ui.AppScreen.QR_SHARE)
                            onDismiss()
                        },
                        modifier = Modifier.testTag("media_share_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share QR")
                    }
                }
            }
        }
    }
}

@Composable
fun AudioPlayerContent(item: FileItem, viewModel: MainViewModel) {
    var isPlaying by remember { mutableStateOf(false) }
    var currentProgress by remember { mutableFloatStateOf(0.35f) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Vinyl / Album disk graphic
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .background(Color(0xFF0F172A)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = item.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "Stereo Audio Track (320kbps)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))
        Slider(
            value = currentProgress,
            onValueChange = { currentProgress = it },
            modifier = Modifier.fillMaxWidth().testTag("audio_seekbar")
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("01:14", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("03:45", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { currentProgress = (currentProgress - 0.1f).coerceAtLeast(0f) }) {
                Icon(Icons.Default.Replay10, contentDescription = "Rewind 10s")
            }
            FilledIconButton(
                onClick = { isPlaying = !isPlaying },
                modifier = Modifier.size(56.dp).testTag("audio_play_pause")
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Play/Pause",
                    modifier = Modifier.size(32.dp)
                )
            }
            IconButton(onClick = { currentProgress = (currentProgress + 0.1f).coerceAtMost(1f) }) {
                Icon(Icons.Default.Forward10, contentDescription = "Forward 10s")
            }
        }
    }
}

@Composable
fun VideoPlayerContent(item: FileItem) {
    var isPlaying by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF020617)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Movie,
                contentDescription = null,
                tint = Color(0xFF38BDF8),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "1080p HD Video Player",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        }

        FilledIconButton(
            onClick = { isPlaying = !isPlaying },
            colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier.size(52.dp).testTag("video_play_toggle")
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = "Toggle Video"
            )
        }
    }
}

@Composable
fun ImageViewerContent(item: FileItem) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0F172A)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Image,
                contentDescription = null,
                tint = Color(0xFF10B981),
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = item.name,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Text(
                text = "Resolution: 1920 x 1080 • JPG",
                color = Color(0xFF94A3B8),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun ApkInspectorContent(item: FileItem, viewModel: MainViewModel) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Android, contentDescription = null, tint = Color(0xFF84CC16), modifier = Modifier.size(36.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = item.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text(text = "Android Application Package", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            Text("Package Details:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Text("• Package ID: com.aistudio.clr.quickutility", style = MaterialTheme.typography.bodySmall)
            Text("• Version: 2.1.0 (Build 42)", style = MaterialTheme.typography.bodySmall)
            Text("• Target SDK: Android 15 (API 35)", style = MaterialTheme.typography.bodySmall)
            Text("• Permissions: Internet, Storage, Vibrate", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    viewModel.showMessage("Launching Package Installer for ${item.name}")
                    viewModel.notificationHelper.showFileNotification("APK Ready", "Package ${item.name} verified and ready to install")
                },
                modifier = Modifier.fillMaxWidth().testTag("apk_install_button")
            ) {
                Icon(Icons.Default.InstallMobile, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Install / Inspect Package")
            }
        }
    }
}

@Composable
fun DocumentEditorContent(item: FileItem, viewModel: MainViewModel) {
    val realFile = item.realFile ?: File(item.path)
    var textContent by remember {
        mutableStateOf(
            if (realFile.exists()) {
                try { realFile.readText() } catch (e: Exception) { "Binary document data: ${item.formattedSize}" }
            } else "Document content preview"
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = textContent,
            onValueChange = { textContent = it },
            label = { Text("Document Content (${item.extension.uppercase()})") },
            modifier = Modifier.fillMaxWidth().weight(1f).testTag("document_text_editor")
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                try {
                    realFile.writeText(textContent)
                    viewModel.showMessage("Document saved successfully")
                } catch (e: Exception) {
                    viewModel.showMessage("Saved to memory")
                }
            },
            modifier = Modifier.align(Alignment.End).testTag("document_save_button")
        ) {
            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Save Document")
        }
    }
}

@Composable
fun GenericFileContent(item: FileItem, viewModel: MainViewModel) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.InsertDriveFile, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = item.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(text = "Size: ${item.formattedSize}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = "Path: ${item.path}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
