package com.termux

import android.database.Cursor
import android.database.MatrixCursor
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.provider.DocumentsContract.Document
import android.provider.DocumentsContract.Root
import android.provider.DocumentsProvider
import android.webkit.MimeTypeMap
import com.termux.R
import java.io.File
import java.io.FileNotFoundException

/**
 * Exposes the Korex home directory (~/) in the Android Files app left drawer.
 *
 * Users can browse, open, and share files from:
 *   - ~/.korex/snippets.zsh
 *   - ~/.korex/ (themes, notes, bookmarks etc. — future phases)
 *   - ~/  (home directory root)
 *
 * Authority: com.termux.documents
 * Registered in AndroidManifest.xml as a DocumentsProvider.
 */
class KorexDocumentsProvider : DocumentsProvider() {

    companion object {
        private const val ROOT_ID     = "korex-home"
        private const val AUTHORITY   = "com.termux.documents"

        private val ROOT_PROJECTION = arrayOf(
            Root.COLUMN_ROOT_ID,
            Root.COLUMN_TITLE,
            Root.COLUMN_SUMMARY,
            Root.COLUMN_DOCUMENT_ID,
            Root.COLUMN_FLAGS,
            Root.COLUMN_ICON,
            Root.COLUMN_MIME_TYPES,
        )

        private val DOCUMENT_PROJECTION = arrayOf(
            Document.COLUMN_DOCUMENT_ID,
            Document.COLUMN_DISPLAY_NAME,
            Document.COLUMN_MIME_TYPE,
            Document.COLUMN_SIZE,
            Document.COLUMN_LAST_MODIFIED,
            Document.COLUMN_FLAGS,
        )
    }

    // Root is filesDir/home — same as TerminalBridge.homeDir()
    private val homeDir: File get() = File(context!!.filesDir, "home")

    override fun onCreate(): Boolean = true

    override fun queryRoots(projection: Array<out String>?): Cursor {
        val cursor = MatrixCursor(projection ?: ROOT_PROJECTION)
        homeDir.mkdirs()
        cursor.newRow().apply {
            add(Root.COLUMN_ROOT_ID,     ROOT_ID)
            add(Root.COLUMN_TITLE,       "Korex")
            add(Root.COLUMN_SUMMARY,     "Terminal home directory")
            add(Root.COLUMN_DOCUMENT_ID, ROOT_ID)
            add(Root.COLUMN_FLAGS,       Root.FLAG_SUPPORTS_CREATE or Root.FLAG_LOCAL_ONLY)
            add(Root.COLUMN_ICON,        R.mipmap.ic_launcher)
            add(Root.COLUMN_MIME_TYPES,  "*/*")
        }
        return cursor
    }

    override fun queryDocument(documentId: String, projection: Array<out String>?): Cursor {
        val cursor = MatrixCursor(projection ?: DOCUMENT_PROJECTION)
        addDocumentRow(cursor, resolveFile(documentId))
        return cursor
    }

    override fun queryChildDocuments(
        parentDocumentId: String,
        projection: Array<out String>?,
        sortOrder: String?,
    ): Cursor {
        val cursor = MatrixCursor(projection ?: DOCUMENT_PROJECTION)
        val parent = resolveFile(parentDocumentId)
        parent.listFiles()
            ?.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
            ?.forEach { addDocumentRow(cursor, it) }
        return cursor
    }

    override fun openDocument(
        documentId: String,
        mode: String,
        signal: CancellationSignal?,
    ): ParcelFileDescriptor {
        val file = resolveFile(documentId)
        val accessMode = ParcelFileDescriptor.parseMode(mode)
        return ParcelFileDescriptor.open(file, accessMode)
            ?: throw FileNotFoundException("Cannot open: $documentId")
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    /**
     * Converts a documentId to a File.
     * ROOT_ID → homeDir
     * Everything else → homeDir + relative path encoded after ROOT_ID prefix
     */
    private fun resolveFile(documentId: String): File {
        if (documentId == ROOT_ID) return homeDir
        // documentId format: "korex-home/relative/path"
        val relative = documentId.removePrefix("$ROOT_ID/")
        val file = File(homeDir, relative).canonicalFile
        // Security: ensure resolved path is inside homeDir
        if (!file.path.startsWith(homeDir.canonicalPath)) {
            throw SecurityException("Path traversal attempt: $documentId")
        }
        return file
    }

    private fun fileToDocumentId(file: File): String {
        val relative = file.canonicalPath.removePrefix(homeDir.canonicalPath).trimStart('/')
        return if (relative.isEmpty()) ROOT_ID else "$ROOT_ID/$relative"
    }

    private fun addDocumentRow(cursor: MatrixCursor, file: File) {
        val mimeType = if (file.isDirectory) Document.MIME_TYPE_DIR else getMimeType(file)
        val flags = if (file.isDirectory) {
            Document.FLAG_DIR_SUPPORTS_CREATE
        } else {
            Document.FLAG_SUPPORTS_WRITE or Document.FLAG_SUPPORTS_DELETE
        }
        cursor.newRow().apply {
            add(Document.COLUMN_DOCUMENT_ID,   fileToDocumentId(file))
            add(Document.COLUMN_DISPLAY_NAME,  file.name)
            add(Document.COLUMN_MIME_TYPE,     mimeType)
            add(Document.COLUMN_SIZE,          if (file.isFile) file.length() else null)
            add(Document.COLUMN_LAST_MODIFIED, file.lastModified())
            add(Document.COLUMN_FLAGS,         flags)
        }
    }

    private fun getMimeType(file: File): String {
        val ext = file.extension.lowercase()
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)
            ?: "application/octet-stream"
    }
}