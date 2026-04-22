package com.v.log.inspector

import com.v.log.logger.Logger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LogInspectorStoreTest {

    @Test
    fun `build tabs normalizes whitespace and removes duplicate tags`() {
        val tabs = LogInspectorStore.buildTabs(
            listOf(
                entry(tag = " Home "),
                entry(tag = "Home"),
                entry(tag = "Settings")
            )
        )

        assertEquals(
            listOf(LogInspectorStore.TAB_V_LOG, "Home", "Settings"),
            tabs.map { it.key }
        )
    }

    @Test
    fun `matches tab compares normalized tag values`() {
        assertTrue(LogInspectorStore.matchesTab(" Home ", "Home"))
        assertTrue(LogInspectorStore.matchesTab("V_LOG [Home]", "Home"))
    }

    private fun entry(tag: String): LogEntry {
        return LogEntry(
            timestamp = 0L,
            level = Logger.INFO,
            levelName = "INFO",
            tag = tag,
            thread = "main",
            message = "message"
        )
    }
}
