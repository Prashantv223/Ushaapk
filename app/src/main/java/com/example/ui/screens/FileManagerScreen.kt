package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import com.example.data.model.FileItem
import com.example.data.model.FileType
import com.example.data.model.formatFileSize
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileManagerScreen(viewModel: MainViewModel) {
    val files by viewModel.files.collectAsState()
    val currentDir by viewModel.currentDirectory.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val storageStats by viewModel.storageStats.collectAsState()
    val selectedFiles by viewModel.selectedFiles.collectAsState()

    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf<FileItem?>(null) }
    var showDetailsDialog by remember { mutableStateOf<FileItem?>(null) }
    var showMoveDialog by remember { mutableStateOf(false) }
    var showCompressDialog by remember { mutableStateOf(false) }
    var folderNameInput by remember { mutableStateOf("") }
    var renameInput by remember { mutableStateOf("") }
    var zipNameInput by remember { mutableStateOf("Archive_${System.currentTimeMillis() % 10000}.zip") }

    val isRoot = currentDir.absolutePath == viewModel.storageManager.getRootDirectory().absolutePath

    Scaffold(
        floatingActionButton = {
            if (selectedFiles.isEmpty()) {
                FloatingActionButton(
                    onClick = { showCreateFolderDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("fab_create_folder")
                ) {
                    Icon(Icons.Default.CreateNewFolder, contentDescription = "New Folder")
                }
            }
        },
        bottomBar = {
            // Multi-selection action bottom bar
            AnimatedVisibility(
                visible = selectedFiles.isNotEmpty(),
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    tonalElevation = 8.dp,
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${selectedFiles.size} selected",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Compress ZIP
                            IconButton(
                                onClick = { showCompressDialog = true },
                                modifier = Modifier.testTag("action_compress")
                            ) {
                                Icon(Icons.Default.Archive, contentDescription = "Compress to ZIP", tint = MaterialTheme.colorScheme.primary)
                            }
                            // Move to Vault
                            IconButton(
                                onClick = { viewModel.moveSelectedToVault() },
                                modifier = Modifier.testTag("action_move_vault")
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = "Move to Secret Vault", tint = Color(0xFFF59E0B))
                            }
                            // Delete
                            IconButton(
                                onClick = { viewModel.deleteSelectedFiles() },
                                modifier = Modifier.testTag("action_delete")
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                            }
                            // Clear
                            IconButton(
                                onClick = { viewModel.clearSelection() },
                                modifier = Modifier.testTag("action_clear_selection")
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Clear Selection")
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Storage Overview Card
                StorageOverviewCard(storageStats)
            }

            item {
                // Category Selector Filter Cards
                CategoryRow(
                    selectedCategory = selectedCategory,
                    onSelectCategory = { viewModel.selectCategory(it) }
                )
            }

            item {
                // Search Field & Quick Actions
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Search files, videos, documents...", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("file_search_field")
                )
            }

            item {
                // Breadcrumb path and navigation bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (!isRoot) {
                            IconButton(
                                onClick = { viewModel.navigateUp() },
                                modifier = Modifier.size(36.dp).testTag("button_nav_up")
                            ) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Up")
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = if (isRoot) "📁 Internal Storage" else "📂 " + currentDir.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Text(
                        text = "${files.size} items",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (files.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = null,
                                modifier = Modifier.size(54.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (searchQuery.isNotEmpty()) "No files matching \"$searchQuery\"" else "Folder is empty",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(files, key = { it.id }) { item ->
                    val isSelected = selectedFiles.contains(item)
                    FileItemRow(
                        item = item,
                        isSelected = isSelected,
                        onItemClick = {
                            if (selectedFiles.isNotEmpty()) {
                                viewModel.toggleFileSelection(item)
                            } else {
                                if (item.isDirectory) {
                                    val f = item.realFile ?: File(item.path)
                                    viewModel.openDirectory(f)
                                } else {
                                    viewModel.openMedia(item)
                                }
                            }
                        },
                        onLongClick = {
                            viewModel.toggleFileSelection(item)
                        },
                        onRename = {
                            renameInput = item.name
                            showRenameDialog = item
                        },
                        onDelete = {
                            val f = item.realFile ?: File(item.path)
                            viewModel.storageManager.deleteFile(f)
                            viewModel.refreshFiles()
                        },
                        onExtract = {
                            viewModel.extractZipFile(item)
                        },
                        onMoveToVault = {
                            val f = item.realFile ?: File(item.path)
                            viewModel.storageManager.moveToVault(f)
                            viewModel.refreshFiles()
                            viewModel.refreshVault()
                            viewModel.showMessage("Moved ${item.name} to Secret Vault")
                        },
                        onDetails = {
                            showDetailsDialog = item
                        },
                        onSpeakDetails = {
                            viewModel.speakText("File ${item.name}, size ${item.formattedSize}, modified on ${item.formattedDate}")
                        }
                    )
                }
            }
        }
    }

    // Dialog: Create Folder
    if (showCreateFolderDialog) {
        AlertDialog(
            onDismissRequest = { showCreateFolderDialog = false },
            title = { Text("Create New Folder") },
            text = {
                OutlinedTextField(
                    value = folderNameInput,
                    onValueChange = { folderNameInput = it },
                    placeholder = { Text("Folder Name") },
                    singleLine = true,
                    modifier = Modifier.testTag("create_folder_input")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (folderNameInput.isNotBlank()) {
                            viewModel.storageManager.createFolder(currentDir, folderNameInput.trim())
                            folderNameInput = ""
                            showCreateFolderDialog = false
                            viewModel.refreshFiles()
                        }
                    },
                    modifier = Modifier.testTag("create_folder_confirm")
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateFolderDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Dialog: Rename
    showRenameDialog?.let { targetItem ->
        AlertDialog(
            onDismissRequest = { showRenameDialog = null },
            title = { Text("Rename File") },
            text = {
                OutlinedTextField(
                    value = renameInput,
                    onValueChange = { renameInput = it },
                    singleLine = true,
                    modifier = Modifier.testTag("rename_input")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (renameInput.isNotBlank()) {
                            viewModel.renameFile(targetItem, renameInput.trim())
                            showRenameDialog = null
                        }
                    },
                    modifier = Modifier.testTag("rename_confirm")
                ) {
                    Text("Rename")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = null }) { Text("Cancel") }
            }
        )
    }

    // Dialog: Compress ZIP
    if (showCompressDialog) {
        AlertDialog(
            onDismissRequest = { showCompressDialog = false },
            title = { Text("Compress into ZIP") },
            text = {
                Column {
                    Text("Compressing ${selectedFiles.size} selected items into an archive:", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = zipNameInput,
                        onValueChange = { zipNameInput = it },
                        label = { Text("Archive Name") },
                        singleLine = true,
                        modifier = Modifier.testTag("compress_name_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.compressSelectedToZip(zipNameInput.trim())
                        showCompressDialog = false
                    },
                    modifier = Modifier.testTag("compress_confirm_btn")
                ) {
                    Text("Compress")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCompressDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Dialog: File Details & TTS
    showDetailsDialog?.let { fileItem ->
        AlertDialog(
            onDismissRequest = { showDetailsDialog = null },
            title = { Text(fileItem.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Path: ${fileItem.path}", style = MaterialTheme.typography.bodySmall)
                    Text("Size: ${fileItem.formattedSize} (${fileItem.sizeBytes} bytes)", style = MaterialTheme.typography.bodySmall)
                    Text("Modified: ${fileItem.formattedDate}", style = MaterialTheme.typography.bodySmall)
                    Text("Type: ${fileItem.type.name}", style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.speakText("Details for ${fileItem.name}: Size is ${fileItem.formattedSize}, modified ${fileItem.formattedDate}")
                    }
                ) {
                    Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Speak")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDetailsDialog = null }) { Text("Close") }
            }
        )
    }
}

@Composable
fun StorageOverviewCard(stats: com.example.data.model.StorageStats) {
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
                    Icon(Icons.Default.SdStorage, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Device Storage",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "${formatFileSize(stats.usedBytes)} / ${formatFileSize(stats.totalBytes)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            val usedFraction = (stats.usedBytes.toFloat() / stats.totalBytes.toFloat()).coerceIn(0f, 1f)
            LinearProgressIndicator(
                progress = { usedFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surface
            )

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StorageStatPill("Images", formatFileSize(stats.imageBytes), Color(0xFF10B981))
                StorageStatPill("Videos", formatFileSize(stats.videoBytes), Color(0xFF3B82F6))
                StorageStatPill("Audio", formatFileSize(stats.audioBytes), Color(0xFFF59E0B))
                StorageStatPill("Docs", formatFileSize(stats.docBytes), Color(0xFFA855F7))
            }
        }
    }
}

@Composable
fun StorageStatPill(label: String, size: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Column {
            Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(size, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun CategoryRow(
    selectedCategory: FileType?,
    onSelectCategory: (FileType?) -> Unit
) {
    val categories = listOf(
        Pair("All", null),
        Pair("Images", FileType.IMAGE),
        Pair("Videos", FileType.VIDEO),
        Pair("Audio", FileType.AUDIO),
        Pair("Docs", FileType.DOCUMENT),
        Pair("APKs", FileType.APK),
        Pair("Archives", FileType.ARCHIVE)
    )

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { (name, type) ->
            val isSelected = selectedCategory == type
            FilterChip(
                selected = isSelected,
                onClick = { onSelectCategory(if (isSelected && type != null) null else type) },
                label = { Text(name, fontSize = 12.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = getCategoryIcon(type),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            )
        }
    }
}

fun getCategoryIcon(type: FileType?): ImageVector {
    return when (type) {
        FileType.IMAGE -> Icons.Default.Image
        FileType.VIDEO -> Icons.Default.Videocam
        FileType.AUDIO -> Icons.Default.AudioFile
        FileType.DOCUMENT -> Icons.Default.Description
        FileType.APK -> Icons.Default.Android
        FileType.ARCHIVE -> Icons.Default.Archive
        else -> Icons.Default.Folder
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun FileItemRow(
    item: FileItem,
    isSelected: Boolean,
    onItemClick: () -> Unit,
    onLongClick: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onExtract: () -> Unit,
    onMoveToVault: () -> Unit,
    onDetails: () -> Unit,
    onSpeakDetails: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onItemClick,
                onLongClick = onLongClick
            )
            .testTag("file_item_${item.name}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(getFileColor(item).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getFileVector(item),
                    contentDescription = null,
                    tint = getFileColor(item),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (item.isDirectory) "Folder" else "${item.formattedSize} • ${item.formattedDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 3-dots Menu
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Options")
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Details & Speak") },
                        leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
                        onClick = {
                            menuExpanded = false
                            onDetails()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Rename") },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                        onClick = {
                            menuExpanded = false
                            onRename()
                        }
                    )
                    if (item.type == FileType.ARCHIVE) {
                        DropdownMenuItem(
                            text = { Text("Extract ZIP") },
                            leadingIcon = { Icon(Icons.Default.FolderZip, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onExtract()
                            }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("Move to Secret Vault") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFF59E0B)) },
                        onClick = {
                            menuExpanded = false
                            onMoveToVault()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}

fun getFileVector(item: FileItem): ImageVector {
    if (item.isDirectory) return Icons.Default.Folder
    return when (item.type) {
        FileType.IMAGE -> Icons.Default.Image
        FileType.VIDEO -> Icons.Default.PlayCircle
        FileType.AUDIO -> Icons.Default.MusicNote
        FileType.DOCUMENT -> Icons.Default.Description
        FileType.APK -> Icons.Default.Android
        FileType.ARCHIVE -> Icons.Default.Archive
        else -> Icons.Default.InsertDriveFile
    }
}

fun getFileColor(item: FileItem): Color {
    if (item.isDirectory) return Color(0xFF38BDF8)
    return when (item.type) {
        FileType.IMAGE -> Color(0xFF10B981)
        FileType.VIDEO -> Color(0xFF3B82F6)
        FileType.AUDIO -> Color(0xFFF59E0B)
        FileType.DOCUMENT -> Color(0xFFA855F7)
        FileType.APK -> Color(0xFF84CC16)
        FileType.ARCHIVE -> Color(0xFFEC4899)
        else -> Color(0xFF64748B)
    }
}
