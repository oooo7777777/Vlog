package com.v.log.inspector

fun LogEntry.asClipboardText(): String {
    return buildString {
        append("time=").append(LogInspectorStore.formatTime(timestamp)).append('\n')
        append("level=").append(levelName).append('\n')
        append("tag=").append(tag).append('\n')
        append("thread=").append(thread).append('\n')
        append("message=").append(message.normalizedForExport())
    }
}

private fun String.normalizedForExport(): String {
    val lines = lineSequence().toList()
    if (lines.isEmpty()) return this
    val start = lines.indexOfFirst { it.isNotBlank() }.let { if (it == -1) 0 else it }
    val end = lines.indexOfLast { it.isNotBlank() }.let { if (it == -1) lines.lastIndex else it }
    return lines.subList(start, end + 1).joinToString("\n")
}
