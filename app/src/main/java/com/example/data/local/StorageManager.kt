package com.example.data.local

import android.content.Context
import com.example.data.model.*
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class StorageManager(private val context: Context) {

    private val rootDir: File = File(context.filesDir, "CLR_Files").apply { if (!exists()) mkdirs() }
    private val vaultDir: File = File(context.filesDir, ".clr_vault").apply { if (!exists()) mkdirs() }
    private val notesFile: File = File(context.filesDir, ".vault_notes.json")

    init {
        initializeSampleFiles()
    }

    fun getRootDirectory(): File = rootDir
    fun getVaultDirectory(): File = vaultDir

    private fun initializeSampleFiles() {
        val marker = File(context.filesDir, ".init_done")
        if (marker.exists()) return

        try {
            // Folders
            val imagesDir = File(rootDir, "Pictures").apply { mkdirs() }
            val videosDir = File(rootDir, "Videos").apply { mkdirs() }
            val musicDir = File(rootDir, "Music").apply { mkdirs() }
            val docsDir = File(rootDir, "Documents").apply { mkdirs() }
            val downloadsDir = File(rootDir, "Downloads").apply { mkdirs() }
            val apksDir = File(rootDir, "APKs").apply { mkdirs() }

            // Sample image 1 (PNG bytes)
            createDummyFile(File(imagesDir, "Sunset_Hills.jpg"), 240 * 1024, "Image file header content")
            createDummyFile(File(imagesDir, "Invoice_Receipt.png"), 120 * 1024, "Invoice PNG content")
            createDummyFile(File(imagesDir, "Project_Wireframe.jpg"), 350 * 1024, "Design mockup")

            // Sample audio
            createDummyFile(File(musicDir, "Acoustic_Breeze.mp3"), 1024 * 1024 * 3, "MP3 Audio sample stream data")
            createDummyFile(File(musicDir, "Voice_Meeting_Notes.m4a"), 512 * 1024, "Voice recording")

            // Sample video
            createDummyFile(File(videosDir, "Goa_Trip_Clip.mp4"), 1024 * 1024 * 5, "MP4 Video stream")

            // Sample documents
            File(docsDir, "Business_Plan_2027.txt").writeText("CLR Business Strategy & Encrypted Backup Plan.\nConfidential file management guidelines.")
            File(docsDir, "Regional_Holiday_Planner.txt").writeText("North India festival schedule:\n- Diwali: Lakshmi Puja\n- Holi: Festival of colors\n- Chhath Puja: Sun worship")
            createDummyFile(File(docsDir, "Tax_Statement.pdf"), 450 * 1024, "%PDF-1.4 Dummy PDF Content")

            // Sample APK
            createDummyFile(File(apksDir, "QuickUtility_v2.1.apk"), 1024 * 1024 * 8, "PK APK dummy archive")

            // Sample Archive
            val sampleZip = File(downloadsDir, "Project_Assets.zip")
            compressToZip(listOf(File(docsDir, "Business_Plan_2027.txt")), sampleZip)

            // Initial Vault items
            val secretPhoto = File(vaultDir, "vault_private_id.jpg")
            createDummyFile(secretPhoto, 180 * 1024, "Encrypted private photo")

            marker.createNewFile()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createDummyFile(file: File, sizeBytes: Long, contentHint: String) {
        if (file.exists()) return
        file.parentFile?.mkdirs()
        FileOutputStream(file).use { out ->
            val header = contentHint.toByteArray()
            out.write(header)
            var written = header.size.toLong()
            val buffer = ByteArray(4096) { 0x5A.toByte() }
            while (written < sizeBytes) {
                val toWrite = minOf(buffer.size.toLong(), sizeBytes - written).toInt()
                out.write(buffer, 0, toWrite)
                written += toWrite
            }
        }
    }

    fun listAllFiles(currentFolder: File = rootDir): List<FileItem> {
        val target = if (currentFolder.exists() && currentFolder.isDirectory) currentFolder else rootDir
        val files = target.listFiles() ?: return emptyList()
        return files.map { fileToFileItem(it) }
            .sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
    }

    fun listFilesByCategory(type: FileType): List<FileItem> {
        val result = mutableListOf<FileItem>()
        fun scan(dir: File) {
            val list = dir.listFiles() ?: return
            for (f in list) {
                if (f.isDirectory) {
                    scan(f)
                } else {
                    val item = fileToFileItem(f)
                    if (item.type == type) {
                        result.add(item)
                    }
                }
            }
        }
        scan(rootDir)
        return result.sortedByDescending { it.lastModified }
    }

    fun searchFiles(query: String, filterType: FileType? = null): List<FileItem> {
        val result = mutableListOf<FileItem>()
        val q = query.trim().lowercase()
        fun scan(dir: File) {
            val list = dir.listFiles() ?: return
            for (f in list) {
                val item = fileToFileItem(f)
                val matchesQuery = q.isEmpty() || item.name.lowercase().contains(q)
                val matchesType = filterType == null || item.type == filterType
                if (matchesQuery && matchesType && !item.isDirectory) {
                    result.add(item)
                }
                if (f.isDirectory) {
                    scan(f)
                }
            }
        }
        scan(rootDir)
        return result
    }

    fun getStorageStats(): StorageStats {
        var totalImage = 0L
        var totalVideo = 0L
        var totalAudio = 0L
        var totalDoc = 0L
        var totalApk = 0L
        var totalArchive = 0L
        var totalOther = 0L
        var usedBytes = 0L

        fun calculate(dir: File) {
            val files = dir.listFiles() ?: return
            for (f in files) {
                if (f.isDirectory) {
                    calculate(f)
                } else {
                    val size = f.length()
                    usedBytes += size
                    when (detectFileType(f.name, false)) {
                        FileType.IMAGE -> totalImage += size
                        FileType.VIDEO -> totalVideo += size
                        FileType.AUDIO -> totalAudio += size
                        FileType.DOCUMENT -> totalDoc += size
                        FileType.APK -> totalApk += size
                        FileType.ARCHIVE -> totalArchive += size
                        FileType.OTHER -> totalOther += size
                    }
                }
            }
        }
        calculate(rootDir)

        val totalDeviceStorage = 64L * 1024 * 1024 * 1024 // 64 GB simulated device size
        val simulatedUsed = 18L * 1024 * 1024 * 1024 + usedBytes
        val freeBytes = totalDeviceStorage - simulatedUsed

        return StorageStats(
            totalBytes = totalDeviceStorage,
            usedBytes = simulatedUsed,
            freeBytes = freeBytes,
            imageBytes = totalImage + (3.2 * 1024 * 1024 * 1024).toLong(),
            videoBytes = totalVideo + (8.5 * 1024 * 1024 * 1024).toLong(),
            audioBytes = totalAudio + (1.4 * 1024 * 1024 * 1024).toLong(),
            docBytes = totalDoc + (750 * 1024 * 1024).toLong(),
            apkBytes = totalApk + (2.1 * 1024 * 1024 * 1024).toLong(),
            archiveBytes = totalArchive + (1.1 * 1024 * 1024 * 1024).toLong(),
            otherBytes = totalOther + (1.0 * 1024 * 1024 * 1024).toLong()
        )
    }

    fun moveFile(source: File, targetDirectory: File): Boolean {
        if (!source.exists() || !targetDirectory.exists() || !targetDirectory.isDirectory) return false
        val dest = File(targetDirectory, source.name)
        return source.renameTo(dest)
    }

    fun copyFile(source: File, targetDirectory: File): Boolean {
        if (!source.exists() || !targetDirectory.exists() || !targetDirectory.isDirectory) return false
        val dest = File(targetDirectory, if (File(targetDirectory, source.name).exists()) "Copy_${source.name}" else source.name)
        return try {
            source.copyTo(dest, overwrite = true)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun renameFile(file: File, newName: String): Boolean {
        if (!file.exists() || newName.isBlank()) return false
        val parent = file.parentFile ?: return false
        val dest = File(parent, newName)
        return file.renameTo(dest)
    }

    fun deleteFile(file: File): Boolean {
        return if (file.isDirectory) {
            file.deleteRecursively()
        } else {
            file.delete()
        }
    }

    fun createFolder(parent: File, name: String): Boolean {
        val folder = File(parent, name)
        return folder.mkdirs()
    }

    fun compressToZip(files: List<File>, destinationZip: File): Boolean {
        return try {
            destinationZip.parentFile?.mkdirs()
            ZipOutputStream(BufferedOutputStream(FileOutputStream(destinationZip))).use { out ->
                val buffer = ByteArray(4096)
                for (file in files) {
                    addFileToZip(file, "", out, buffer)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun addFileToZip(file: File, parentPath: String, out: ZipOutputStream, buffer: ByteArray) {
        val entryName = if (parentPath.isEmpty()) file.name else "$parentPath/${file.name}"
        if (file.isDirectory) {
            val children = file.listFiles() ?: return
            for (child in children) {
                addFileToZip(child, entryName, out, buffer)
            }
        } else {
            FileInputStream(file).use { fi ->
                BufferedInputStream(fi).use { origin ->
                    val entry = ZipEntry(entryName)
                    out.putNextEntry(entry)
                    var count: Int
                    while (origin.read(buffer, 0, buffer.size).also { count = it } != -1) {
                        out.write(buffer, 0, count)
                    }
                    out.closeEntry()
                }
            }
        }
    }

    fun extractZip(zipFile: File, targetDirectory: File): Boolean {
        return try {
            targetDirectory.mkdirs()
            ZipInputStream(BufferedInputStream(FileInputStream(zipFile))).use { zis ->
                var ze: ZipEntry? = zis.nextEntry
                val buffer = ByteArray(4096)
                while (ze != null) {
                    val file = File(targetDirectory, ze.name)
                    if (ze.isDirectory) {
                        file.mkdirs()
                    } else {
                        file.parentFile?.mkdirs()
                        FileOutputStream(file).use { fos ->
                            var count: Int
                            while (zis.read(buffer).also { count = it } != -1) {
                                fos.write(buffer, 0, count)
                            }
                        }
                    }
                    zis.closeEntry()
                    ze = zis.nextEntry
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Vault Operations
    fun moveToVault(file: File): VaultItem? {
        if (!file.exists()) return null
        val encryptedFileName = "enc_${System.currentTimeMillis()}_${file.name}"
        val target = File(vaultDir, encryptedFileName)
        val success = file.renameTo(target)
        if (!success) return null

        val type = when (detectFileType(file.name, false)) {
            FileType.IMAGE -> VaultType.PHOTO
            FileType.VIDEO -> VaultType.VIDEO
            FileType.DOCUMENT -> VaultType.DOCUMENT
            else -> VaultType.DOCUMENT
        }

        return VaultItem(
            id = target.name,
            title = file.name,
            originalName = file.name,
            type = type,
            dateEncrypted = System.currentTimeMillis(),
            sizeBytes = target.length(),
            previewPath = target.absolutePath
        )
    }

    fun getVaultItems(): List<VaultItem> {
        val files = vaultDir.listFiles() ?: return emptyList()
        return files.map { file ->
            val cleanName = file.name.removePrefix("enc_").substringAfter("_", file.name)
            val type = when {
                cleanName.endsWith(".jpg", true) || cleanName.endsWith(".png", true) -> VaultType.PHOTO
                cleanName.endsWith(".mp4", true) || cleanName.endsWith(".mkv", true) -> VaultType.VIDEO
                else -> VaultType.DOCUMENT
            }
            VaultItem(
                id = file.name,
                title = cleanName,
                originalName = cleanName,
                type = type,
                dateEncrypted = file.lastModified(),
                sizeBytes = file.length(),
                previewPath = file.absolutePath
            )
        }.sortedByDescending { it.dateEncrypted }
    }

    fun restoreFromVault(vaultId: String): Boolean {
        val file = File(vaultDir, vaultId)
        if (!file.exists()) return false
        val cleanName = file.name.removePrefix("enc_").substringAfter("_", file.name)
        val restored = File(rootDir, cleanName)
        return file.renameTo(restored)
    }

    fun deleteVaultItem(vaultId: String): Boolean {
        val file = File(vaultDir, vaultId)
        return file.delete()
    }

    // Secret Notes
    fun getSecretNotes(): List<SecretNote> {
        if (!notesFile.exists()) return defaultNotes()
        return try {
            val jsonStr = notesFile.readText()
            val array = org.json.JSONArray(jsonStr)
            val list = mutableListOf<SecretNote>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    SecretNote(
                        id = obj.getString("id"),
                        title = obj.getString("title"),
                        content = obj.getString("content"),
                        timestamp = obj.getLong("timestamp"),
                        category = obj.optString("category", "Personal")
                    )
                )
            }
            list
        } catch (e: Exception) {
            defaultNotes()
        }
    }

    fun saveSecretNote(note: SecretNote) {
        val existing = getSecretNotes().toMutableList()
        val index = existing.indexOfFirst { it.id == note.id }
        if (index >= 0) {
            existing[index] = note
        } else {
            existing.add(0, note)
        }
        val array = org.json.JSONArray()
        for (item in existing) {
            val obj = org.json.JSONObject().apply {
                put("id", item.id)
                put("title", item.title)
                put("content", item.content)
                put("timestamp", item.timestamp)
                put("category", item.category)
            }
            array.put(obj)
        }
        notesFile.writeText(array.toString())
    }

    fun deleteSecretNote(id: String) {
        val existing = getSecretNotes().filter { it.id != id }
        val array = org.json.JSONArray()
        for (item in existing) {
            val obj = org.json.JSONObject().apply {
                put("id", item.id)
                put("title", item.title)
                put("content", item.content)
                put("timestamp", item.timestamp)
                put("category", item.category)
            }
            array.put(obj)
        }
        notesFile.writeText(array.toString())
    }

    private fun defaultNotes(): List<SecretNote> = listOf(
        SecretNote(
            id = "note_1",
            title = "Bank & Locker Codes",
            content = "Locker Safe 1: 8492-B\nATM Backup PIN: ****\nPan Card Number: ABCDE1234F",
            timestamp = System.currentTimeMillis() - 86400000L * 2,
            category = "Finance"
        ),
        SecretNote(
            id = "note_2",
            title = "Cloud Master Keys",
            content = "AES-256 Recovery Seed: velvet-orchid-928-sapphire-zenith\nVault Backup Server: clr-secure-drive.io",
            timestamp = System.currentTimeMillis() - 86400000L * 5,
            category = "Security"
        )
    )

    private fun fileToFileItem(file: File): FileItem {
        val isDir = file.isDirectory
        val ext = file.extension.lowercase()
        return FileItem(
            id = file.absolutePath,
            name = file.name,
            path = file.absolutePath,
            sizeBytes = if (isDir) getDirectorySize(file) else file.length(),
            lastModified = file.lastModified(),
            isDirectory = isDir,
            type = detectFileType(file.name, isDir),
            isVault = file.parentFile?.absolutePath == vaultDir.absolutePath,
            extension = ext,
            realFile = file
        )
    }

    private fun getDirectorySize(dir: File): Long {
        var size = 0L
        val list = dir.listFiles() ?: return 0L
        for (f in list) {
            size += if (f.isDirectory) getDirectorySize(f) else f.length()
        }
        return size
    }

    private fun detectFileType(fileName: String, isDir: Boolean): FileType {
        if (isDir) return FileType.OTHER
        val ext = fileName.substringAfterLast('.', "").lowercase()
        return when (ext) {
            "jpg", "jpeg", "png", "webp", "gif", "svg", "bmp" -> FileType.IMAGE
            "mp4", "mkv", "webm", "avi", "3gp", "mov" -> FileType.VIDEO
            "mp3", "m4a", "wav", "aac", "flac", "ogg" -> FileType.AUDIO
            "pdf", "doc", "docx", "txt", "xlsx", "csv", "ppt" -> FileType.DOCUMENT
            "apk" -> FileType.APK
            "zip", "rar", "7z", "tar", "gz" -> FileType.ARCHIVE
            else -> FileType.OTHER
        }
    }
}
