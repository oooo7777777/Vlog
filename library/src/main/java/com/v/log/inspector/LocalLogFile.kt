package com.v.log.inspector

data class LocalLogFile(
    val absolutePath: String,
    val name: String,
    val lastModified: Long,
    val sizeBytes: Long
)
