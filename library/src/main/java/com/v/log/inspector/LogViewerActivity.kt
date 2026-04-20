package com.v.log.inspector

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ListView
import android.widget.ArrayAdapter
import android.widget.AdapterView
import android.widget.Spinner
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.v.log.R
import com.v.log.logger.Logger
import java.io.File
import kotlin.concurrent.thread

class LogViewerActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_LOCAL_LOG_FILE_PATH = "extra_local_log_file_path"

        fun createIntent(context: Context, localLogFilePath: String? = null): Intent {
            return Intent(context, LogViewerActivity::class.java).apply {
                localLogFilePath?.let { putExtra(EXTRA_LOCAL_LOG_FILE_PATH, it) }
            }
        }
    }

    private lateinit var adapter: LogViewerAdapter
    private lateinit var etFilter: EditText
    private lateinit var spinnerLevel: Spinner
    private var allLogs: List<LogEntry> = emptyList()
    private var selectedLevel: Int? = null
    private var localLogFilePath: String? = null
    private val listener: () -> Unit = { runOnUiThread { render() } }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.vlog_activity_log_viewer)
        localLogFilePath = intent.getStringExtra(EXTRA_LOCAL_LOG_FILE_PATH)
        title = viewerTitle()

        etFilter = findViewById(R.id.etFilter)
        spinnerLevel = findViewById(R.id.spinnerLevel)
        etFilter.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                applyFilter()
                true
            } else {
                false
            }
        }
        etFilter.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyFilter()
            }

            override fun afterTextChanged(s: Editable?) = Unit
        })
        spinnerLevel.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf(
                getString(R.string.vlog_level_all),
                getString(R.string.vlog_level_debug),
                getString(R.string.vlog_level_info),
                getString(R.string.vlog_level_warn),
                getString(R.string.vlog_level_error)
            )
        )
        spinnerLevel.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                selectedLevel = when (position) {
                    1 -> Logger.DEBUG
                    2 -> Logger.INFO
                    3 -> Logger.WARN
                    4 -> Logger.ERROR
                    else -> null
                }
                applyFilter()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }

        adapter = LogViewerAdapter(this) { copyEntry(it) }
        findViewById<ListView>(R.id.listLogs).adapter = adapter

        render()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.vlog_menu_log_viewer, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.action_clear)?.isVisible = !isViewingLocalFile()
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_local_files -> {
                startActivity(LocalLogFilesActivity.createIntent(this))
                true
            }

            R.id.action_clear -> {
                LogInspectorStore.clear()
                LogInspectorNotifier.update(applicationContext)
                true
            }

            R.id.action_share -> {
                shareLogs(adapter.currentItems())
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onStart() {
        super.onStart()
        if (!isViewingLocalFile()) {
            LogInspectorStore.registerListener(listener)
        }
    }

    override fun onStop() {
        if (!isViewingLocalFile()) {
            LogInspectorStore.unregisterListener(listener)
        }
        super.onStop()
    }

    private fun render() {
        if (isViewingLocalFile()) {
            loadLocalLogs()
        } else {
            allLogs = LogInspectorStore.snapshot().asReversed()
            applyFilter()
        }
    }

    private fun applyFilter() {
        val keyword = etFilter.text.toString().trim()
        val logs = allLogs.filter { entry ->
            val timeText = LogInspectorStore.formatTime(entry.timestamp)
            val keywordMatched = keyword.isEmpty() ||
                    entry.tag.contains(keyword, ignoreCase = true) ||
                    entry.thread.contains(keyword, ignoreCase = true) ||
                    timeText.contains(keyword, ignoreCase = true) ||
                    entry.message.contains(keyword, ignoreCase = true)
            val levelMatched = selectedLevel == null || entry.level == selectedLevel
            keywordMatched && levelMatched
        }
        adapter.submit(logs)
    }

    private fun copyEntry(entry: LogEntry) {
        val text = buildString {
            append("time=").append(LogInspectorStore.formatTime(entry.timestamp)).append('\n')
            append("level=").append(entry.levelName).append('\n')
            append("tag=").append(entry.tag).append('\n')
            append("thread=").append(entry.thread).append('\n')
            append("message=").append(entry.message)
        }
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("vlog-entry", text))
        Toast.makeText(this, R.string.vlog_copied, Toast.LENGTH_SHORT).show()
    }

    private fun shareLogs(entries: List<LogEntry>) {
        if (entries.isEmpty()) {
            Toast.makeText(this, R.string.vlog_nothing_to_share, Toast.LENGTH_SHORT).show()
            return
        }
        val content = entries.joinToString("\n\n") { entry ->
            buildString {
                append(LogInspectorStore.formatTime(entry.timestamp)).append('\n')
                append(entry.levelName).append('\n')
                append(entry.tag).append('\n')
                append(entry.thread).append('\n')
                append(entry.message)
            }
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, content)
        }
        startActivity(Intent.createChooser(intent, getString(R.string.vlog_share_title)))
    }

    private fun isViewingLocalFile(): Boolean = !localLogFilePath.isNullOrEmpty()

    private fun viewerTitle(): String {
        val localFileName = localLogFilePath?.let { File(it).name }
        return if (localFileName.isNullOrEmpty()) {
            getString(R.string.vlog_viewer_title)
        } else {
            getString(R.string.vlog_viewer_title_local, localFileName)
        }
    }

    private fun loadLocalLogs() {
        val path = localLogFilePath ?: return
        adapter.submit(emptyList())
        allLogs = emptyList()
        thread {
            val entries = LocalLogRepository.readEntries(File(path)).asReversed()
            runOnUiThread {
                if (isFinishing || isDestroyed) return@runOnUiThread
                allLogs = entries
                applyFilter()
                if (entries.isEmpty()) {
                    Toast.makeText(this, R.string.vlog_local_file_no_entries, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
