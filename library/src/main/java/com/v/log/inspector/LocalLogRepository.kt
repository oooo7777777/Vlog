package com.v.log.inspector

import java.io.File

object LocalLogRepository {

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
}
