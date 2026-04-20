package com.v.log.inspector

data class LogEntry(
    val timestamp: Long,
    val level: Int,
    val levelName: String,
    val tag: String,
    val thread: String,
    val message: String
)
