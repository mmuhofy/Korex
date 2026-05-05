package com.korexx.terminal

import android.content.Context
import android.system.Os
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipInputStream

private const val TAG = "BootstrapInstaller"

/**
 * Installs the Termux bootstrap environment on first launch.
 *
 * The bootstrap zip is embedded in libkorex-bootstrap.so via JNI/Assembly
 * and extracted to filesDir/usr (PREFIX) on first run.
 *
 * Shell binaries (bash, zsh) are NOT executed from filesDir — they are
 * shipped as libkorex-*.so in jniLibs/ and run from nativeLibraryDir,
 * which is always exec-able on all API levels. See TerminalBridge.
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

    fun isInstalled(context: Context): Boolean = stampFile(context).exists()

    /**
     * Installs bootstrap to filesDir/usr.
     * Reports progress via [onProgress] (0..100).
     * Throws on failure — caller must handle and show error UI.
     */
    suspend fun install(
        context: Context,
        onProgress: (message: String, percent: Int) -> Unit,
    ) = withContext(Dispatchers.IO) {
        val prefix  = prefixDir(context)
        val staging = stagingDir(context)

        staging.deleteRecursively()
        staging.mkdirs()

        onProgress("Loading bootstrap package…", 0)

        val zipBytes = getZip()
        Log.i(TAG, "Bootstrap zip size: ${zipBytes.size} bytes")

        onProgress("Extracting files…", 5)

        val symlinks     = mutableListOf<Pair<String, String>>()
        var totalEntries = 0
        var processed    = 0

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
                        FileOutputStream(outFile).use { out -> zis.copyTo(out) }
                        outFile.setExecutable(true, false)
                        outFile.setReadable(true, false)
                        outFile.setWritable(true, false)
                    }
                }

                processed++
                val percent = 5 + (processed * 80 / totalEntries.coerceAtLeast(1))
                onProgress("Extracting… ($processed/$totalEntries)", percent)
                entry = zis.nextEntry
            }
        }

        onProgress("Creating symlinks…", 85)

        symlinks.forEach { (target, link) ->
            runCatching {
                val linkFile = File(staging, link)
                linkFile.parentFile?.mkdirs()
                if (linkFile.exists() || linkFile.isSymlink()) linkFile.delete()
                Os.symlink(target, linkFile.absolutePath)
            }.onFailure { e ->
                Log.w(TAG, "Symlink failed: $link → $target", e)
            }
        }

        onProgress("Finalizing…", 95)

        prefix.deleteRecursively()
        if (!staging.renameTo(prefix)) {
            staging.copyRecursively(prefix, overwrite = true)
            staging.deleteRecursively()
        }

        stampFile(context).writeText("installed")
        onProgress("Done!", 100)
        Log.i(TAG, "Bootstrap installed to ${prefix.absolutePath}")
    }

    fun prefixDir(context: Context): File = File(context.filesDir, "usr")

    private fun stagingDir(context: Context): File =
        File(context.filesDir, "usr-staging")

    private fun stampFile(context: Context): File =
        File(context.filesDir, ".bootstrap-installed")

    private fun File.isSymlink(): Boolean = runCatching {
        canonicalPath != absolutePath
    }.getOrDefault(false)
}