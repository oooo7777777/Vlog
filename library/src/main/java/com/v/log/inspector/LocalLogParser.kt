package com.v.log.inspector

import com.v.log.logger.Logger
import java.text.SimpleDateFormat
import java.util.Locale

object LocalLogParser {

    private val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.CHINA)
    private const val ENTRY_SEPARATOR_PREFIX = "============================================================"
    private val headerRegex = Regex(
        "^(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3}),(\\d+),([^,]*),(VERBOSE|DEBUG|INFO|WARN|ERROR|ASSERT),([^,]*)(?:,.*)?$"
    )

    fun isHeaderLine(line: String): Boolean = headerRegex.matches(line)

    fun isSeparatorLine(line: String): Boolean = line.startsWith(ENTRY_SEPARATOR_PREFIX)

    fun parse(content: String, fallbackTimestamp: Long): List<LogEntry> {
        if (content.isBlank()) return emptyList()
        val entries = ArrayList<LogEntry>()
        var currentHeader: ParsedHeader? = null
        val messageLines = ArrayList<String>()

        fun flushEntry() {
            val header = currentHeader ?: return
            val displayTag = LogInspectorStore.sanitizeDisplayTag(header.tag)
            entries.add(
                LogEntry(
                    timestamp = header.timestamp,
                    level = header.level,
                    levelName = header.levelName,
                    tag = displayTag,
                    thread = header.thread,
                    message = messageLines.joinToString("\n").trimEnd()
                )
            )
            currentHeader = null
            messageLines.clear()
        }

        content.lineSequence().forEach { rawLine ->
            val line = rawLine.removeSuffix("\r")
            if (line.startsWith(ENTRY_SEPARATOR_PREFIX)) {
                flushEntry()
                return@forEach
            }
            val parsedHeader = parseHeader(line, fallbackTimestamp)
            if (parsedHeader != null && currentHeader == null) {
                currentHeader = parsedHeader
                return@forEach
            }
            if (currentHeader != null) {
                messageLines.add(line)
            }
        }
        flushEntry()
        return entries
    }

    fun parseSingle(content: String, fallbackTimestamp: Long): LogEntry? {
        return parse(content, fallbackTimestamp).firstOrNull()
    }

    private fun parseHeader(line: String, fallbackTimestamp: Long): ParsedHeader? {
        val match = headerRegex.matchEntire(line) ?: return null
        val levelName = match.groupValues[4]
        val timestamp = runCatching { timeFormat.parse(match.groupValues[1])?.time ?: fallbackTimestamp }
            .getOrDefault(fallbackTimestamp)
        return ParsedHeader(
            timestamp = timestamp,
            level = mapLevel(levelName),
            levelName = levelName,
            thread = match.groupValues[3],
            tag = match.groupValues[5]
        )
    }

    private fun mapLevel(levelName: String): Int {
        return when (levelName) {
            "DEBUG" -> Logger.DEBUG
            "INFO" -> Logger.INFO
            "WARN" -> Logger.WARN
            "ERROR" -> Logger.ERROR
            "VERBOSE" -> Logger.DEBUG
            "ASSERT" -> Logger.ERROR
            else -> Logger.INFO
        }
    }

    private data class ParsedHeader(
        val timestamp: Long,
        val level: Int,
        val levelName: String,
        val thread: String,
        val tag: String
    )
}
