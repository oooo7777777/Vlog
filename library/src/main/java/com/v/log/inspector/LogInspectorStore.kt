package com.v.log.inspector

import com.v.log.util.LogUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.CopyOnWriteArraySet

object LogInspectorStore {

    const val TAB_V_LOG = "V_LOG"
    private const val MAX_ENTRIES = 500
    private const val MAX_MESSAGE_LENGTH = 16 * 1024
    private const val DEFAULT_PREVIEW_LENGTH = 120
    private val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.CHINA)
    private val entries = ArrayList<LogEntry>(MAX_ENTRIES)
    private val listeners = CopyOnWriteArraySet<() -> Unit>()
    @Volatile
    private var enabled = false
    @Volatile
    private var previewLength = DEFAULT_PREVIEW_LENGTH

    @Synchronized
    fun setEnabled(enable: Boolean) {
        enabled = enable
        if (!enable) {
            entries.clear()
        }
        dispatchChanged()
    }

    fun isEnabled(): Boolean = enabled

    fun setPreviewLength(length: Int) {
        previewLength = length.coerceAtLeast(40)
    }

    fun getPreviewLength(): Int = previewLength

    fun buildTabs(entries: List<LogEntry>): List<LogTabItem> {
        val tabs = ArrayList<LogTabItem>()
        tabs.add(LogTabItem(TAB_V_LOG, TAB_V_LOG))
        val customTabs = LinkedHashSet<String>()
        entries.forEach { entry ->
            val custom = entry.tag.takeIf { it.isNotBlank() } ?: return@forEach
            if (custom != TAB_V_LOG) {
                customTabs.add(custom)
            }
        }
        customTabs.forEach { tabs.add(LogTabItem(it, it)) }
        return tabs
    }

    @Synchronized
    fun add(priority: Int, tag: String, thread: String, message: String) {
        if (!enabled) return
        if (entries.size >= MAX_ENTRIES) {
            entries.removeAt(0)
        }
        val sanitizedTag = sanitizeTag(tag)
        entries.add(
            LogEntry(
                timestamp = System.currentTimeMillis(),
                level = priority,
                levelName = LogUtils.logLevel(priority),
                tag = sanitizedTag,
                thread = thread,
                message = clipMessage(message)
            )
        )
        dispatchChanged()
    }

    @Synchronized
    fun snapshot(): List<LogEntry> = entries.toList()

    @Synchronized
    fun clear() {
        entries.clear()
        dispatchChanged()
    }

    fun registerListener(listener: () -> Unit) {
        listeners.add(listener)
    }

    fun unregisterListener(listener: () -> Unit) {
        listeners.remove(listener)
    }

    fun formatTime(timestamp: Long): String = timeFormat.format(Date(timestamp))

    fun previewMessage(message: String): String {
        if (message.length <= previewLength) return message
        return message.take(previewLength).trimEnd() + "..."
    }

    fun sanitizeDisplayTag(tag: String): String = sanitizeTag(tag)

    private fun clipMessage(message: String): String {
        if (message.length <= MAX_MESSAGE_LENGTH) return message
        val remain = message.length - MAX_MESSAGE_LENGTH
        return message.substring(0, MAX_MESSAGE_LENGTH) +
                "\n[log inspector truncated $remain chars]"
    }

    private fun sanitizeTag(tag: String): String {
        val prefix = "V_LOG ["
        return if (tag.startsWith(prefix) && tag.endsWith("]") && tag.length > prefix.length + 1) {
            tag.substring(prefix.length, tag.length - 1)
        } else {
            tag
        }
    }

    private fun dispatchChanged() {
        listeners.forEach { it.invoke() }
    }

    data class LogTabItem(
        val key: String,
        val label: String
    )
}
