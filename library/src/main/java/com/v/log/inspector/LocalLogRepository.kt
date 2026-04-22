package com.v.log.inspector

import java.io.File
import java.io.RandomAccessFile
import kotlin.math.max

object LocalLogRepository {

    private const val DEFAULT_PAGE_SIZE = 100

    fun listLogFiles(paths: List<String>?): List<LocalLogFile> {
        if (paths.isNullOrEmpty()) return emptyList()
        return paths
            .asSequence()
            .map { File(it) }
            .filter { it.exists() && it.isFile && it.name.endsWith(".log", ignoreCase = true) }
            .map {
                LocalLogFile(
                    absolutePath = it.absolutePath,
                    name = it.name,
                    lastModified = it.lastModified(),
                    sizeBytes = it.length()
                )
            }
            .sortedByDescending { it.lastModified }
            .toList()
    }

    fun readEntries(file: File): List<LogEntry> {
        if (!file.exists() || !file.isFile) return emptyList()
        return LocalLogParser.parse(file.readText(), file.lastModified())
    }

    fun openPagedReader(file: File, pageSize: Int = DEFAULT_PAGE_SIZE): PagedReader? {
        if (!file.exists() || !file.isFile) return null
        val segments = scanSegments(file)
        if (segments.isNotEmpty()) {
            return PagedReader(file, segments, pageSize)
        }
        val fallbackEntries = readEntries(file).asReversed()
        return if (fallbackEntries.isEmpty()) null else PagedReader(fallbackEntries, pageSize)
    }

    fun deleteAll(files: List<LocalLogFile>): Int {
        var deletedCount = 0
        files.forEach { logFile ->
            val file = File(logFile.absolutePath)
            if (file.exists() && file.delete()) {
                deletedCount++
            }
        }
        return deletedCount
    }

    private fun scanSegments(file: File): List<EntrySegment> {
        val segments = ArrayList<EntrySegment>()
        RandomAccessFile(file, "r").use { raf ->
            var entryStart: Long? = null
            while (true) {
                val lineStart = raf.filePointer
                val line = raf.readLine() ?: break
                if (entryStart == null && LocalLogParser.isHeaderLine(line)) {
                    entryStart = lineStart
                    continue
                }
                if (entryStart != null && LocalLogParser.isSeparatorLine(line)) {
                    segments.add(EntrySegment(entryStart, raf.filePointer))
                    entryStart = null
                }
            }
            if (entryStart != null && entryStart < raf.length()) {
                segments.add(EntrySegment(entryStart, raf.length()))
            }
        }
        return segments
    }

    class PagedReader internal constructor(
        private val file: File,
        private val segments: List<EntrySegment>,
        private val pageSize: Int
    ) {
        private var fallbackEntries: List<LogEntry>? = null
        private var nextIndexExclusive = segments.size

        internal constructor(
            entries: List<LogEntry>,
            pageSize: Int
        ) : this(File(""), emptyList(), pageSize) {
            fallbackEntries = entries
            nextIndexExclusive = entries.size
        }

        @Synchronized
        fun hasMore(): Boolean = nextIndexExclusive > 0

        @Synchronized
        fun readNextPage(): List<LogEntry> {
            if (nextIndexExclusive <= 0) return emptyList()
            val fallback = fallbackEntries
            if (fallback != null) {
                val fromIndex = max(0, nextIndexExclusive - pageSize)
                val page = fallback.subList(fromIndex, nextIndexExclusive)
                nextIndexExclusive = fromIndex
                return page
            }
            val fromIndex = max(0, nextIndexExclusive - pageSize)
            val pageSegments = segments.subList(fromIndex, nextIndexExclusive).asReversed()
            nextIndexExclusive = fromIndex
            return RandomAccessFile(file, "r").use { raf ->
                pageSegments.mapNotNull { segment ->
                    raf.seek(segment.startOffset)
                    val size = (segment.endOffset - segment.startOffset).toInt()
                    if (size <= 0) return@mapNotNull null
                    val bytes = ByteArray(size)
                    raf.readFully(bytes)
                    LocalLogParser.parseSingle(String(bytes, Charsets.UTF_8), file.lastModified())
                }
            }
        }
    }

    internal data class EntrySegment(
        val startOffset: Long,
        val endOffset: Long
    )
}
