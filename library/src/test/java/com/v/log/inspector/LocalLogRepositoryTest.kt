package com.v.log.inspector

import com.v.log.logger.Logger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class LocalLogRepositoryTest {

    @Test
    fun `parse local log file content into entries`() {
        val content = """
            brand,model,33,WIFI,1.0.0
            ============================================================INFO===========================================================>
            2026-04-20 10:00:00.000,12,main,INFO,DemoTag,MainActivity,onCreate
            first line
            second line
            ============================================================ERROR===========================================================>
            2026-04-20 10:01:00.000,23,Render,ERROR,CrashTag,MainActivity,render
            crash detail
        """.trimIndent()

        val entries = LocalLogParser.parse(content, 0L)

        assertEquals(2, entries.size)
        assertEquals(Logger.INFO, entries[0].level)
        assertEquals("DemoTag", entries[0].tag)
        assertEquals("main", entries[0].thread)
        assertEquals("first line\nsecond line", entries[0].message)
        assertEquals(Logger.ERROR, entries[1].level)
        assertEquals("CrashTag", entries[1].tag)
        assertEquals("crash detail", entries[1].message)
    }

    @Test
    fun `list and delete local log files`() {
        val tempDir = createTempDir(prefix = "vlog-local-test")
        val newer = File(tempDir, "2026-04-20.log").apply {
            writeText("demo")
            setLastModified(200L)
        }
        val older = File(tempDir, "2026-04-19.log").apply {
            writeText("demo")
            setLastModified(100L)
        }
        File(tempDir, "ignore.txt").writeText("skip")

        val files = LocalLogRepository.listLogFiles(
            listOf(older.absolutePath, newer.absolutePath, File(tempDir, "ignore.txt").absolutePath)
        )

        assertEquals(listOf(newer.name, older.name), files.map { it.name })

        val deletedCount = LocalLogRepository.deleteAll(files)

        assertEquals(2, deletedCount)
        assertTrue(!newer.exists())
        assertTrue(!older.exists())
    }
}
