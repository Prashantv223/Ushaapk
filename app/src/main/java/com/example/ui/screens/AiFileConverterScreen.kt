package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.MainViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiFileConverterScreen(viewModel: MainViewModel) {
    val selectedSource by viewModel.selectedSourceFile.collectAsState()
    val selectedFormat by viewModel.selectedTargetFormat.collectAsState()
    val isConverting by viewModel.isConverting.collectAsState()
    val progress by viewModel.conversionProgress.collectAsState()
    val history by viewModel.conversionHistory.collectAsState()
    val lastResult by viewModel.lastConversionResult.collectAsState()
    val allFiles by viewModel.files.collectAsState()
    val showFakeCrash by viewModel.showFakeCrash.collectAsState()

    var showFilePickerSheet by remember { mutableStateOf(false) }
    var showVaultPinDialog by remember { mutableStateOf(false) }
    var vaultPinInput by remember { mutableStateOf("") }
    var filePickerFilter by remember { mutableStateOf<FileType?>(null) }

    // Fake Crash Decoy Dialog
    if (showFakeCrash) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissFakeCrash() },
            title = {
                Text(
                    text = "Converter Service Stopped",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            },
            text = {
                Column {
                    Text(
                        text = "Unfortunately, AI File Converter process has stopped due to an unauthorized access attempt.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "(Decoy active. Tap OK to restore)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.dismissFakeCrash() },
                    modifier = Modifier.testTag("dismiss_fake_crash_btn")
                ) {
                    Text("OK")
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Card with AI branding & Secret Vault Access
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "AI File Converter",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Image • PDF • Video • Audio • OCR",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Discreet Secret Vault access button
                    IconButton(
                        onClick = { showVaultPinDialog = true },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                            .testTag("converter_vault_lock_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Secret Vault Access",
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Step 1: Selected File Preview Card
        item {
            Text(
                text = "1. Choose File to Convert",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                selectedSource?.let { getFileColor(it).copy(alpha = 0.15f) }
                                    ?: MaterialTheme.colorScheme.primaryContainer
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = selectedSource?.let { getFileVector(it) } ?: Icons.Default.UploadFile,
                            contentDescription = null,
                            tint = selectedSource?.let { getFileColor(it) } ?: MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = selectedSource?.name ?: "No file selected",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = selectedSource?.let { "${it.formattedSize} • ${it.type.name}" } ?: "Tap Choose to select an image, video, PDF or doc",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = {
                            viewModel.refreshFiles()
                            showFilePickerSheet = true
                        },
                        modifier = Modifier.testTag("btn_select_file")
                    ) {
                        Text("Select")
                    }
                }
            }
        }

        // Step 2: Target Format Selection
        item {
            Text(
                text = "2. Select Target Format & AI Feature",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            // AI Smart Superpowers Row
            Text(
                text = "✨ AI Powered Conversions",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val aiFormats = listOf(
                    TargetFormat.AI_OCR,
                    TargetFormat.AI_SUMMARY,
                    TargetFormat.AI_TRANSCRIPT
                )
                items(aiFormats) { fmt ->
                    FormatBadgeChip(
                        format = fmt,
                        isSelected = selectedFormat == fmt,
                        isAi = true,
                        onClick = { viewModel.selectTargetFormat(fmt) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Standard Media & Document Formats",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))

            // Target Formats Grid/Row
            val standardFormats = listOf(
                TargetFormat.PDF,
                TargetFormat.PNG,
                TargetFormat.JPG,
                TargetFormat.WEBP,
                TargetFormat.MP3,
                TargetFormat.M4A,
                TargetFormat.TXT,
                TargetFormat.MD,
                TargetFormat.ZIP
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(standardFormats) { fmt ->
                    FormatBadgeChip(
                        format = fmt,
                        isSelected = selectedFormat == fmt,
                        isAi = false,
                        onClick = { viewModel.selectTargetFormat(fmt) }
                    )
                }
            }
        }

        // Step 3: Convert Action & Live Progress
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Convert to: ${selectedFormat.label}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Output: .${selectedFormat.extension} (${selectedFormat.category})",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = { viewModel.startConversion() },
                            enabled = !isConverting && selectedSource != null,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.testTag("btn_start_conversion")
                        ) {
                            Icon(Icons.Default.Transform, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isConverting) "Converting..." else "Convert Now")
                        }
                    }

                    // Progress indicator if converting
                    if (isConverting) {
                        Spacer(modifier = Modifier.height(14.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("AI Engine processing...", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                            Text("${(progress * 100).toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Latest Conversion Result Card
        lastResult?.let { res ->
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
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
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Conversion Complete", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            }
                            if (res.reductionPercentage > 0) {
                                Surface(
                                    color = Color(0xFF10B981).copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "${res.reductionPercentage}% smaller",
                                        color = Color(0xFF10B981),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = res.convertedFileName,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "${formatFileSize(res.originalSizeBytes)} ➔ ${formatFileSize(res.convertedSizeBytes)}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = res.aiInsight,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    val f = File(res.convertedFilePath)
                                    val item = FileItem(
                                        id = f.absolutePath,
                                        name = f.name,
                                        path = f.absolutePath,
                                        sizeBytes = f.length(),
                                        lastModified = f.lastModified(),
                                        isDirectory = false,
                                        type = when (res.targetFormat) {
                                            TargetFormat.PDF, TargetFormat.TXT, TargetFormat.MD -> FileType.DOCUMENT
                                            TargetFormat.PNG, TargetFormat.JPG, TargetFormat.WEBP -> FileType.IMAGE
                                            TargetFormat.MP3, TargetFormat.M4A -> FileType.AUDIO
                                            TargetFormat.ZIP -> FileType.ARCHIVE
                                            else -> FileType.DOCUMENT
                                        },
                                        extension = res.targetFormat.extension,
                                        realFile = f
                                    )
                                    viewModel.openMedia(item)
                                },
                                modifier = Modifier.testTag("btn_view_converted")
                            ) {
                                Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Preview")
                            }

                            Button(
                                onClick = {
                                    val f = File(res.convertedFilePath)
                                    viewModel.storageManager.moveToVault(f)
                                    viewModel.showMessage("Saved to Secret Vault")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                                modifier = Modifier.testTag("btn_vault_save_converted")
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Send to Vault")
                            }
                        }
                    }
                }
            }
        }

        // Conversion History
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Conversion History (${history.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        items(history, key = { it.id }) { item ->
            ConversionHistoryRow(
                item = item,
                onOpen = {
                    val f = File(item.convertedFilePath)
                    val fileItem = FileItem(
                        id = f.absolutePath,
                        name = f.name,
                        path = f.absolutePath,
                        sizeBytes = f.length(),
                        lastModified = f.lastModified(),
                        isDirectory = false,
                        type = when (item.targetFormat) {
                            TargetFormat.PDF, TargetFormat.TXT, TargetFormat.MD -> FileType.DOCUMENT
                            TargetFormat.PNG, TargetFormat.JPG, TargetFormat.WEBP -> FileType.IMAGE
                            TargetFormat.MP3, TargetFormat.M4A -> FileType.AUDIO
                            TargetFormat.ZIP -> FileType.ARCHIVE
                            else -> FileType.DOCUMENT
                        },
                        extension = item.targetFormat.extension,
                        realFile = f
                    )
                    viewModel.openMedia(fileItem)
                },
                onSpeak = {
                    viewModel.speakText("Conversion details for ${item.convertedFileName}. Converted to ${item.targetFormat.label}. ${item.aiInsight}")
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }

    // Modal Bottom Sheet: Select File to Convert
    if (showFilePickerSheet) {
        ModalBottomSheet(onDismissRequest = { showFilePickerSheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Choose File to Convert",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { showFilePickerSheet = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Filter chips
                val filterTypes = listOf(
                    Pair("All", null),
                    Pair("Images", FileType.IMAGE),
                    Pair("Docs/PDF", FileType.DOCUMENT),
                    Pair("Videos", FileType.VIDEO),
                    Pair("Audio", FileType.AUDIO)
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filterTypes) { (label, type) ->
                        FilterChip(
                            selected = filePickerFilter == type,
                            onClick = { filePickerFilter = type },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                val filtered = allFiles.filter { !it.isDirectory && (filePickerFilter == null || it.type == filePickerFilter) }
                if (filtered.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(30.dp), contentAlignment = Alignment.Center) {
                        Text("No matching files found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 350.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filtered) { item ->
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.selectSourceFile(item)
                                        showFilePickerSheet = false
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = getFileVector(item),
                                        contentDescription = null,
                                        tint = getFileColor(item),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.name, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text("${item.formattedSize} • ${item.type.name}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog: Secret Vault PIN Unlock
    if (showVaultPinDialog) {
        AlertDialog(
            onDismissRequest = {
                showVaultPinDialog = false
                vaultPinInput = ""
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFF59E0B))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Unlock Secret Vault")
                }
            },
            text = {
                Column {
                    Text("Enter secret vault PIN to access encrypted files and hidden media:", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = vaultPinInput,
                        onValueChange = { if (it.all { c -> c.isDigit() }) vaultPinInput = it },
                        placeholder = { Text("Enter PIN (e.g. 1234)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("vault_unlock_pin_input")
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    if (viewModel.settingsRepo.isBiometricEnabled) {
                        OutlinedButton(
                            onClick = {
                                viewModel.unlockVault()
                                showVaultPinDialog = false
                                vaultPinInput = ""
                            },
                            modifier = Modifier.fillMaxWidth().testTag("vault_quick_biometric_btn")
                        ) {
                            Icon(Icons.Default.Fingerprint, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Biometric Fingerprint")
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val success = viewModel.verifyAndUnlockVault(vaultPinInput.trim())
                        if (success) {
                            showVaultPinDialog = false
                            vaultPinInput = ""
                        }
                    },
                    modifier = Modifier.testTag("vault_pin_confirm_btn")
                ) {
                    Text("Unlock")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showVaultPinDialog = false
                    vaultPinInput = ""
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun FormatBadgeChip(
    format: TargetFormat,
    isSelected: Boolean,
    isAi: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        modifier = Modifier.testTag("format_chip_${format.name}")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isAi) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Column {
                Text(
                    text = format.label,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
                Text(
                    text = ".${format.extension}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ConversionHistoryRow(
    item: ConversionHistoryItem,
    onOpen: () -> Unit,
    onSpeak: () -> Unit
) {
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
                    .size(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.targetFormat.extension.uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.convertedFileName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "From: ${item.originalFileName} • ${formatFileSize(item.convertedSizeBytes)}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(item.timestamp)),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onSpeak, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.VolumeUp, contentDescription = "Speak Details", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            }
            IconButton(onClick = onOpen, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Visibility, contentDescription = "Preview", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            }
        }
    }
}
