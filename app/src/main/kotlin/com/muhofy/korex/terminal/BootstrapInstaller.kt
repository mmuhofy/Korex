package com.muhofy.korex.terminal

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipInputStream

private const val TAG = "BootstrapInstaller"

/**
 * Installs the bootstrap environment on first launch.
 *
 * Flow:
 * 1. Load libtermux-bootstrap.so via System.loadLibrary()
 * 2. Call native getZip() to get the zip bytes
 * 3. Extract zip to $PREFIX (filesDir/usr)
 * 4. Process SYMLINKS.txt to create symlinks
 * 5. Mark installation complete via a stamp file
 */
object BootstrapInstaller {

    private external fun getZip(): ByteArray

    val isLoaded: Boolean by lazy {
        try {
            System.loadLibrary("korex-bootstrap")
            true
        } catch (e: UnsatisfiedLinkError) {
            Log.e(TAG, "Failed to load libkorex-bootstrap.so", e)
            false
        }
    }

    fun isInstalled(context: Context): Boolean {
        return stampFile(context).exists()
    }

    /**
     * Installs bootstrap. Reports progress via [onProgress] (0..100).
     * Throws on failure.
     */
    suspend fun install(
        context: Context,
        onProgress: (message: String, percent: Int) -> Unit,
    ) = withContext(Dispatchers.IO) {
        val prefix = prefixDir(context)
        val staging = stagingDir(context)

        // Clean up any previous failed attempt
        staging.deleteRecursively()
        staging.mkdirs()

        onProgress("Loading bootstrap package…", 0)

        val zipBytes = getZip()
        Log.i(TAG, "Bootstrap zip size: ${zipBytes.size} bytes")

        onProgress("Extracting files…", 5)

        val symlinks = mutableListOf<Pair<String, String>>() // target -> link
        var totalEntries = 0
        var processed = 0

        // First pass — count entries for progress
        ZipInputStream(zipBytes.inputStream()).use { zis ->
            while (zis.nextEntry != null) totalEntries++
        }

        // Second pass — extract
        ZipInputStream(zipBytes.inputStream()).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                val name = entry.name

                if (name == "SYMLINKS.txt") {
                    val content = zis.readBytes().toString(Charsets.UTF_8)
                    content.lines().forEach { line ->
                        val parts = line.split("←")
                        if (parts.size == 2) {
                            symlinks.add(parts[0].trim() to parts[1].trim())
                        }
                    }
                } else {
                    val outFile = File(staging, name)
                    if (entry.isDirectory) {
                        outFile.mkdirs()
                    } else {
                        outFile.parentFile?.mkdirs()
                        FileOutputStream(outFile).use { out ->
                            zis.copyTo(out)
                        }
                        // Set executable bit for all non-text files
                        outFile.setExecutable(true, false)
                        outFile.setReadable(true, false)
                        outFile.setWritable(true, false)
                    }
                }

                processed++
                val percent = 5 + (processed * 80 / totalEntries.coerceAtLeast(1))
                onProgress("Extracting files… ($processed/$totalEntries)", percent)
                entry = zis.nextEntry
            }
        }

        onProgress("Creating symlinks…", 85)

        symlinks.forEach { (target, link) ->
            try {
                val linkFile = File(staging, link)
                linkFile.parentFile?.mkdirs()
                if (linkFile.exists() || isSymlink(linkFile)) linkFile.delete()
                Os.symlink(target, linkFile.absolutePath)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to create symlink $link -> $target", e)
            }
        }

        onProgress("Finalizing…", 95)

        // Move staging to real prefix
        prefix.deleteRecursively()
        if (!staging.renameTo(prefix)) {
            // renameTo can fail across mount points — fallback to copy
            staging.copyRecursively(prefix, overwrite = true)
            staging.deleteRecursively()
        }

        // Write stamp file
        stampFile(context).writeText("installed")

        onProgress("Done!", 100)
        Log.i(TAG, "Bootstrap installed to ${prefix.absolutePath}")
    }

    fun prefixDir(context: Context): File =
        File(context.filesDir, "usr")

    private fun stagingDir(context: Context): File =
        File(context.filesDir, "usr-staging")

    private fun stampFile(context: Context): File =
        File(context.filesDir, ".bootstrap-installed")

    private fun isSymlink(file: File): Boolean {
        return try {
            file.canonicalPath != file.absolutePath
        } catch (e: Exception) {
            false
        }
    }
}

// Needed for Os.symlink
private object Os {
    fun symlink(target: String, linkPath: String) {
        android.system.Os.symlink(target, linkPath)
    }
}