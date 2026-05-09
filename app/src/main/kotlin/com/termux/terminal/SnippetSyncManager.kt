package com.termux.terminal

import android.content.Context
import android.util.Log
import com.termux.data.snippet.SnippetEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG            = "SnippetSyncManager"
private const val KOREX_DIR      = ".korex"
private const val SNIPPETS_FILE  = "snippets.zsh"
private const val SOURCE_MARKER  = "# korex-snippets"

// All rc files that should source snippets.zsh
private val RC_FILES = listOf(".zshrc", ".bashrc")

/**
 * Syncs the in-app snippet list to ~/.korex/snippets.zsh as shell aliases,
 * then hot-reloads the file in every live terminal session so aliases are
 * immediately available without restarting.
 *
 * Flow:
 *   1. Write all snippets as `alias name='command'` to ~/.korex/snippets.zsh
 *   2. Ensure every rc file (zshrc, bashrc) sources the file on startup
 *   3. Send `source ~/.korex/snippets.zsh` to every live TerminalBridge
 *      so the current session picks up changes instantly
 *
 * Alias name derivation:
 *   "Git Push Main" → git_push_main
 */
@Singleton
class SnippetSyncManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val homeDir      get() = File(context.filesDir, "home")
    private val korexDir     get() = File(homeDir, KOREX_DIR)
    private val snippetsFile get() = File(korexDir, SNIPPETS_FILE)

    /**
     * Rewrites snippets.zsh and hot-reloads in all live sessions.
     * [getBridges] is a lambda so we don't hold a reference to SessionManager
     * (avoids circular DI dependency).
     */
    suspend fun sync(
        snippets: List<SnippetEntity>,
        getBridges: () -> Collection<TerminalBridge> = { emptyList() },
    ) = withContext(Dispatchers.IO) {
        runCatching {
            // ── 1. Write snippets file ────────────────────────────────────
            korexDir.mkdirs()
            val content = buildSnippetsFile(snippets)
            snippetsFile.writeText(content, Charsets.UTF_8)
            Log.i(TAG, "Wrote ${snippets.size} snippets → ${snippetsFile.absolutePath}")

            // ── 2. Ensure rc files source snippets.zsh ────────────────────
            RC_FILES.forEach { ensureRcSourced(it) }

            // ── 3. Hot-reload in every live terminal ──────────────────────
            val sourceCmd = "source \"${snippetsFile.absolutePath}\"\n"
            getBridges().forEach { bridge ->
                runCatching { bridge.write(sourceCmd) }
                    .onFailure { Log.w(TAG, "Failed to hot-reload in bridge: ${it.message}") }
            }
            Log.i(TAG, "Hot-reloaded snippets in ${getBridges().size} session(s)")

        }.onFailure { e ->
            Log.e(TAG, "Snippet sync failed", e)
        }
    }

    // ── Private helpers ──────────────────────────────────────────────────

    private fun buildSnippetsFile(snippets: List<SnippetEntity>): String = buildString {
        appendLine("# Korex snippets — auto-generated, do not edit manually")
        appendLine("# Manage snippets via the Korex app")
        appendLine()
        for (snippet in snippets) {
            val name = toAliasName(snippet.title)
            if (name.isBlank()) continue
            val cmd = snippet.command.replace("'", "'\\''") // escape single quotes
            appendLine("# ${snippet.title}")
            appendLine("alias $name='$cmd'")
            appendLine()
        }
    }

    /**
     * Adds `source ~/.korex/snippets.zsh` to [rcFileName] if not already present.
     * Creates the file if missing.
     *
     * Uses the absolute path (not ~/) to avoid expansion issues across shells.
     */
    private fun ensureRcSourced(rcFileName: String) {
        val rcFile = File(homeDir, rcFileName)
        val sourceLine = buildSourceLine()

        if (!rcFile.exists()) {
            rcFile.writeText("$sourceLine\n", Charsets.UTF_8)
            Log.i(TAG, "Created $rcFileName with source line")
            return
        }

        val content = rcFile.readText(Charsets.UTF_8)
        if (SOURCE_MARKER !in content) {
            rcFile.appendText("\n$sourceLine\n", Charsets.UTF_8)
            Log.i(TAG, "Appended source line to $rcFileName")
        } else {
            Log.d(TAG, "$rcFileName already sources snippets — skipping")
        }
    }

    /**
     * Builds the source line using the absolute path so it works
     * regardless of how $HOME is set in the shell environment.
     */
    private fun buildSourceLine(): String {
        val absPath = snippetsFile.absolutePath
        return "[ -f \"$absPath\" ] && source \"$absPath\"  $SOURCE_MARKER"
    }

    /**
     * "Git Push Main" → "git_push_main"
     * Strips non-alphanumeric, lowercases, replaces spaces with underscores.
     */
    private fun toAliasName(title: String): String =
        title.trim()
            .lowercase()
            .replace(Regex("\\s+"), "_")
            .replace(Regex("[^a-z0-9_]"), "")
            .take(32)
}