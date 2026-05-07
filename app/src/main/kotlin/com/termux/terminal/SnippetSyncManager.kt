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

private const val TAG = "SnippetSyncManager"

// Paths relative to filesDir/home (~)
private const val KOREX_DIR          = ".korex"
private const val SNIPPETS_FILE      = "snippets.zsh"
private const val ZSHRC_FILE         = ".zshrc"
private const val BASHRC_FILE        = ".bashrc"
private const val SOURCE_MARKER      = "# korex-snippets"
private const val SOURCE_LINE        = "[ -f ~/.$KOREX_DIR/$SNIPPETS_FILE ] && source ~/.$KOREX_DIR/$SNIPPETS_FILE  $SOURCE_MARKER"

/**
 * Syncs the in-app snippet list to ~/.korex/snippets.zsh as shell aliases.
 *
 * Flow:
 * 1. Writes all snippets as `alias name='command'` to ~/.korex/snippets.zsh
 * 2. Ensures ~/.zshrc and ~/.bashrc source this file (one-time setup)
 *
 * Alias name is derived from snippet title:
 *   - lowercased
 *   - spaces → underscores
 *   - non-alphanumeric/underscore chars stripped
 *   - truncated to 32 chars
 *
 * Example:
 *   title = "Git Push Main"  →  alias git_push_main='git push origin main'
 */
@Singleton
class SnippetSyncManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val homeDir    get() = File(context.filesDir, "home")
    private val korexDir   get() = File(homeDir, KOREX_DIR)
    private val snippetsFile get() = File(korexDir, SNIPPETS_FILE)

    /**
     * Rewrites ~/.korex/snippets.zsh with the full current snippet list.
     * Also ensures rc files source it.
     * Safe to call on every add/update/delete.
     */
    suspend fun sync(snippets: List<SnippetEntity>) = withContext(Dispatchers.IO) {
        runCatching {
            korexDir.mkdirs()

            val content = buildSnippetsFile(snippets)
            snippetsFile.writeText(content, Charsets.UTF_8)
            Log.i(TAG, "Wrote ${snippets.size} snippets to ${snippetsFile.absolutePath}")

            ensureRcSourced(ZSHRC_FILE)
            ensureRcSourced(BASHRC_FILE)

        }.onFailure { e ->
            Log.e(TAG, "Failed to sync snippets", e)
        }
    }

    /**
     * Builds the full snippets.zsh content from the snippet list.
     */
    private fun buildSnippetsFile(snippets: List<SnippetEntity>): String {
        val sb = StringBuilder()
        sb.appendLine("# Korex snippets — auto-generated, do not edit manually")
        sb.appendLine("# Add/edit snippets via the Korex app")
        sb.appendLine()

        for (snippet in snippets) {
            val aliasName = toAliasName(snippet.title)
            if (aliasName.isBlank()) continue

            // Escape single quotes in command: ' → '\''
            val escapedCmd = snippet.command.replace("'", "'\\''")
            sb.appendLine("# ${snippet.title}")
            sb.appendLine("alias $aliasName='$escapedCmd'")
            sb.appendLine()
        }

        return sb.toString()
    }

    /**
     * Ensures the rc file sources snippets.zsh.
     * Adds the source line only if not already present.
     * Creates the rc file if it doesn't exist.
     */
    private fun ensureRcSourced(rcFileName: String) {
        val rcFile = File(homeDir, rcFileName)

        if (!rcFile.exists()) {
            rcFile.writeText("$SOURCE_LINE\n", Charsets.UTF_8)
            Log.i(TAG, "Created $rcFileName with source line")
            return
        }

        val content = rcFile.readText(Charsets.UTF_8)
        if (SOURCE_MARKER !in content) {
            rcFile.appendText("\n$SOURCE_LINE\n", Charsets.UTF_8)
            Log.i(TAG, "Added source line to $rcFileName")
        }
    }

    /**
     * Converts a snippet title to a valid shell alias name.
     * "Git Push Main" → "git_push_main"
     */
    private fun toAliasName(title: String): String =
        title
            .trim()
            .lowercase()
            .replace(Regex("\\s+"), "_")
            .replace(Regex("[^a-z0-9_]"), "")
            .take(32)
}