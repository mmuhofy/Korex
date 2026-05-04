package com.muhofy.korex.terminal

import android.content.Context
import android.system.ErrnoException
import android.system.Os
import android.system.OsConstants
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileDescriptor
import java.io.FileOutputStream
import java.util.zip.ZipInputStream

private const val TAG = "BootstrapInstaller"

/**
 * Installs the Termux bootstrap environment on first launch.
 *
 * KEY CONSTRAINT — Android API 29+ noexec:
 * context.filesDir (/data/data/<pkg>/files/) is mounted noexec.
 * execve() on binaries extracted there will fail with EACCES/EPERM.
 *
 * SOLUTION — /proc/self/fd trick (fexecve equivalent):
 * Open the target binary with Os.open() to get a FileDescriptor,
 * then pass "/proc/self/fd/<N>" as the shell path to TerminalSession.
 * The kernel resolves the fd path without checking mount noexec flags.
 *
 * This is the same approach used by Termux and other terminal emulators
 * targeting API 29+.
 *
 * Flow:
 * 1. Load libkorex-bootstrap.so via System.loadLibrary()
 * 2. Call native getZip() to get the embedded zip bytes
 * 3. Extract zip to $PREFIX (filesDir/usr)
 * 4. Process SYMLINKS.txt to create symlinks
 * 5. Write stamp file to mark installation complete
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

        // Clean up any previous failed attempt
        staging.deleteRecursively()
        staging.mkdirs()

        onProgress("Loading bootstrap package…", 0)

        val zipBytes = getZip()
        Log.i(TAG, "Bootstrap zip size: ${zipBytes.size} bytes")

        onProgress("Extracting files…", 5)

        val symlinks     = mutableListOf<Pair<String, String>>() // target → link
        var totalEntries = 0
        var processed    = 0

        // First pass — count entries for progress reporting
        ZipInputStream(zipBytes.inputStream()).use { zis ->
            while (zis.nextEntry != null) totalEntries++
        }

        // Second pass — extract files
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
                        // Mark all non-directory files executable.
                        // Note: setExecutable() sets the filesystem bit,
                        // but execve() will still be blocked by noexec mount.
                        // Actual execution is handled via /proc/self/fd trick
                        // in TerminalBridge — see openFdPath().
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
            // renameTo can fail across mount points — fallback to recursive copy
            staging.copyRecursively(prefix, overwrite = true)
            staging.deleteRecursively()
        }

        stampFile(context).writeText("installed")
        onProgress("Done!", 100)
        Log.i(TAG, "Bootstrap installed to ${prefix.absolutePath}")
    }

    /**
     * Opens [binary] with O_RDONLY and returns a "/proc/self/fd/<N>" path string.
     *
     * WHY: filesDir is mounted noexec on API 29+. execve() on a path under
     * filesDir fails with EACCES regardless of file permissions.
     * However, the kernel resolves /proc/self/fd/<N> through the open file
     * descriptor, bypassing the noexec check on the underlying mount point.
     * This is semantically equivalent to fexecve() but works with APIs that
     * accept a path string (like TerminalSession).
     *
     * The returned fd is intentionally NOT closed — it must remain open for
     * the lifetime of the TerminalSession process. Closing it before exec
     * would make the /proc/self/fd path invalid.
     *
     * @throws ErrnoException if the file cannot be opened (e.g. not found)
     */
    @Throws(ErrnoException::class)
    fun openFdPath(binary: File): String {
        val fd: FileDescriptor = Os.open(
            binary.absolutePath,
            OsConstants.O_RDONLY,
            0,
        )
        // FileDescriptor.toString() on Android returns "FileDescriptor[N]"
        // We need just the integer N.
        val fdInt = extractFdInt(fd)
        Log.d(TAG, "Opened fd=$fdInt for ${binary.absolutePath}")
        return "/proc/self/fd/$fdInt"
    }

    /**
     * Extracts the raw int descriptor from a FileDescriptor via reflection.
     * This is necessary because FileDescriptor.getInt$() is package-private
     * in Android's libcore.
     *
     * UNTESTED — verify before use on all target API levels (26–34).
     */
    private fun extractFdInt(fd: FileDescriptor): Int {
        return try {
            val field = FileDescriptor::class.java.getDeclaredField("descriptor")
            field.isAccessible = true
            field.getInt(fd)
        } catch (e: Exception) {
            // Fallback: parse from toString() → "FileDescriptor[12]"
            val str = fd.toString()
            val start = str.indexOf('[')
            val end   = str.indexOf(']')
            if (start != -1 && end != -1) {
                str.substring(start + 1, end).toIntOrNull()
                    ?: throw IllegalStateException("Cannot extract fd int from: $str", e)
            } else {
                throw IllegalStateException("Cannot extract fd int from: $str", e)
            }
        }
    }

    fun prefixDir(context: Context): File =
        File(context.filesDir, "usr")

    private fun stagingDir(context: Context): File =
        File(context.filesDir, "usr-staging")

    private fun stampFile(context: Context): File =
        File(context.filesDir, ".bootstrap-installed")

    /** Checks if this File is a symlink (canonical != absolute). */
    private fun File.isSymlink(): Boolean = runCatching {
        canonicalPath != absolutePath
    }.getOrDefault(false)
}