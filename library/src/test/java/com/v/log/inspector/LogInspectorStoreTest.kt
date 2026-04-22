package com.v.log.inspector

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class LogInspectorStoreTest {

    @Before
    fun setUp() {
        LogInspectorStore.setEnabled(true)
        LogInspectorStore.clear()
    }

    @Test
    fun add_sanitizesStoredTag() {
        LogInspectorStore.add(android.util.Log.DEBUG, "V_LOG [Order]", "main", "message")

        val entry = LogInspectorStore.snapshot().single()
        assertEquals("Order", entry.tag)
    }

    @Test
    fun buildTabs_keepsDefaultAndAppendsDistinctTags() {
        val entries = listOf(
            LogEntry(0L, 1, "DEBUG", "V_LOG", "main", "a"),
            LogEntry(1L, 1, "DEBUG", "Order", "main", "b"),
            LogEntry(2L, 1, "DEBUG", "Profile", "main", "c"),
            LogEntry(3L, 1, "DEBUG", "Order", "main", "d")
        )

        val tabs = LogInspectorStore.buildTabs(entries)

        assertEquals(listOf("V_LOG", "Order", "Profile"), tabs.map { it.key })
    }
}
