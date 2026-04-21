package com.v.log.inspector

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.core.content.FileProvider
import java.io.File

object LogShareExporter {

    private const val SHARE_DIR = "vlog_share"
    private const val LIST_FILE_NAME = "inspector_logs.txt"
    private const val DETAIL_FILE_NAME = "inspector_log_detail.txt"
    private const val LOCAL_FILE_NAME = "local_log_share.txt"
    private const val DELETE_DELAY_MS = 2 * 60 * 1000L

    fun cleanupStaleFiles(context: Context) {
        shareDir(context).listFiles()?.forEach { file ->
            val expired = System.currentTimeMillis() - file.lastModified() >= DELETE_DELAY_MS
            if (expired) {
                file.delete()
            }
        }
    }

    fun shareEntriesAsTextFile(context: Context, entries: List<LogEntry>) {
        if (entries.isEmpty()) return
        val content = entries.joinToString("\n\n") { it.asClipboardText() }
        val file = writeTempFile(context, LIST_FILE_NAME, content)
        shareFile(context, file, "text/plain")
    }

    fun shareEntryAsTextFile(context: Context, entry: LogEntry) {
        val file = writeTempFile(context, DETAIL_FILE_NAME, entry.asClipboardText())
        shareFile(context, file, "text/plain")
    }

    fun shareExistingFile(context: Context, file: File) {
        if (!file.exists() || !file.isFile) return
        val exported = writeTempFile(context, LOCAL_FILE_NAME, file.readText())
        shareFile(context, exported, "text/plain")
    }

    private fun writeTempFile(context: Context, fileName: String, content: String): File {
        cleanupStaleFiles(context)
        val dir = shareDir(context)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        dir.listFiles()?.forEach { it.delete() }
        return File(dir, fileName).apply {
            writeText(content)
        }
    }

    private fun shareFile(context: Context, file: File, mimeType: String) {
        val uri = file.toShareUri(context)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            clipData = android.content.ClipData.newUri(context.contentResolver, file.name, uri)
        }
        scheduleDelete(file)
        context.startActivity(Intent.createChooser(intent, context.getString(com.v.log.R.string.vlog_share_title)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    private fun scheduleDelete(file: File) {
        Handler(Looper.getMainLooper()).postDelayed({
            if (file.exists()) {
                file.delete()
            }
        }, DELETE_DELAY_MS)
    }

    private fun File.toShareUri(context: Context): Uri {
        return FileProvider.getUriForFile(
            context,
            context.packageName + ".vlog.fileprovider",
            this
        )
    }

    private fun shareDir(context: Context): File = File(context.cacheDir, SHARE_DIR)
}
