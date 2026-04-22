package com.v.log.inspector

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.Rect
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ArrayAdapter
import android.widget.AdapterView
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.Spinner
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.v.log.R
import com.v.log.logger.Logger
import java.io.File
import kotlin.concurrent.thread

class LogViewerActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_LOCAL_LOG_FILE_PATH = "extra_local_log_file_path"
        private const val LOCAL_PAGE_SIZE = 100
        private const val LOCAL_LOAD_MORE_THRESHOLD = 15

        fun createIntent(context: Context, localLogFilePath: String? = null): Intent {
            return Intent(context, LogViewerActivity::class.java).apply {
                localLogFilePath?.let { putExtra(EXTRA_LOCAL_LOG_FILE_PATH, it) }
            }
        }
    }

    private lateinit var adapter: LogViewerAdapter
    private lateinit var etFilter: EditText
    private lateinit var spinnerLevel: Spinner
    private lateinit var scrollTabs: HorizontalScrollView
    private lateinit var layoutTabs: LinearLayout
    private lateinit var listLogs: RecyclerView
    private var allLogs: List<LogEntry> = emptyList()
    private var selectedLevel: Int? = null
    private var selectedTabKey: String = LogInspectorStore.TAB_V_LOG
    private var localLogFilePath: String? = null
    private var showFullMessage = false
    private var localReader: LocalLogRepository.PagedReader? = null
    private var localLoadedLogs = ArrayList<LogEntry>()
    private var isLocalInitializing = false
    private var isLocalLoadingMore = false
    private var currentLocalFilePath: String? = null
    private val listener: () -> Unit = { runOnUiThread { render() } }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LogShareExporter.cleanupStaleFiles(this)
        setContentView(R.layout.vlog_activity_log_viewer)
        applyIntentState(intent)
        installTitleClickToggle()

        etFilter = findViewById(R.id.etFilter)
        spinnerLevel = findViewById(R.id.spinnerLevel)
        scrollTabs = findViewById(R.id.scrollTabs)
        layoutTabs = findViewById(R.id.layoutTabs)
        listLogs = findViewById(R.id.listLogs)
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
                getString(R.string.vlog_level_verbose),
                getString(R.string.vlog_level_debug),
                getString(R.string.vlog_level_info),
                getString(R.string.vlog_level_warn),
                getString(R.string.vlog_level_error),
                getString(R.string.vlog_level_assert)
            )
        )
        spinnerLevel.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                selectedLevel = when (position) {
                    1 -> Logger.VERBOSE
                    2 -> Logger.DEBUG
                    3 -> Logger.INFO
                    4 -> Logger.WARN
                    5 -> Logger.ERROR
                    6 -> Logger.ASSERT
                    else -> null
                }
                applyFilter()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }

        adapter = LogViewerAdapter(
            this,
            onClick = { openDetail(it) },
            onLongClick = { copyEntry(it) }
        )
        adapter.setShowFullMessage(showFullMessage)
        listLogs.layoutManager = LinearLayoutManager(this)
        listLogs.adapter = adapter
        listLogs.setHasFixedSize(false)
        if (listLogs.itemDecorationCount == 0) {
            listLogs.addItemDecoration(VerticalSpaceItemDecoration((8 * resources.displayMetrics.density).toInt()))
        }
        listLogs.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (!isViewingLocalFile() || dy <= 0 || isLocalInitializing || isLocalLoadingMore) return
                val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return
                val lastVisible = layoutManager.findLastVisibleItemPosition()
                if (lastVisible >= adapter.itemCount - LOCAL_LOAD_MORE_THRESHOLD) {
                    loadNextLocalPage()
                }
            }
        })

        render()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent == null) return
        setIntent(intent)
        applyIntentState(intent)
        invalidateOptionsMenu()
        render()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.vlog_menu_log_viewer, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.action_local_files)?.isVisible = !isViewingLocalFile()
        menu.findItem(R.id.action_clear)?.isVisible = !isViewingLocalFile()
        menu.findItem(R.id.action_toggle_message_mode)?.title = getString(
            if (showFullMessage) R.string.vlog_show_preview_mode else R.string.vlog_show_full_mode
        )
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }

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

            R.id.action_toggle_message_mode -> {
                showFullMessage = !showFullMessage
                adapter.setShowFullMessage(showFullMessage)
                invalidateOptionsMenu()
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
            ensureLocalLogs()
        } else {
            allLogs = LogInspectorStore.snapshot().asReversed()
            applyFilter()
        }
    }

    private fun applyFilter() {
        val keyword = etFilter.text.toString().trim()
        val baseFilteredLogs = allLogs.filter { entry ->
            val timeText = LogInspectorStore.formatTime(entry.timestamp)
            val keywordMatched = keyword.isEmpty() ||
                    entry.tag.contains(keyword, ignoreCase = true) ||
                    entry.thread.contains(keyword, ignoreCase = true) ||
                    timeText.contains(keyword, ignoreCase = true) ||
                    entry.message.contains(keyword, ignoreCase = true)
            val levelMatched = selectedLevel == null || entry.level == selectedLevel
            keywordMatched && levelMatched
        }
        val availableTabs = LogInspectorStore.buildTabs(baseFilteredLogs)
        if (availableTabs.none { it.key == selectedTabKey }) {
            selectedTabKey = LogInspectorStore.TAB_V_LOG
        }
        renderTabs(availableTabs)
        val logs = baseFilteredLogs.filter { entry ->
            LogInspectorStore.matchesTab(entry.tag, selectedTabKey)
        }
        adapter.submit(logs)
    }

    private fun copyEntry(entry: LogEntry) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("vlog-entry", entry.asClipboardText()))
        Toast.makeText(this, R.string.vlog_copied, Toast.LENGTH_SHORT).show()
    }

    private fun openDetail(entry: LogEntry) {
        startActivity(LogDetailActivity.createIntent(this, entry))
    }

    private fun shareLogs(entries: List<LogEntry>) {
        if (entries.isEmpty()) {
            Toast.makeText(this, R.string.vlog_nothing_to_share, Toast.LENGTH_SHORT).show()
            return
        }
        if (isViewingLocalFile()) {
            val localFile = File(localLogFilePath.orEmpty())
            if (localFile.exists()) {
                LogShareExporter.shareExistingFile(this, localFile)
                return
            }
        }
        LogShareExporter.shareEntriesAsTextFile(this, entries)
    }

    private fun isViewingLocalFile(): Boolean = !localLogFilePath.isNullOrEmpty()

    private fun applyIntentState(intent: Intent) {
        localLogFilePath = intent.getStringExtra(EXTRA_LOCAL_LOG_FILE_PATH)
        title = viewerTitle()
        supportActionBar?.setDisplayHomeAsUpEnabled(isViewingLocalFile())
    }

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
        currentLocalFilePath = path
        localReader = null
        localLoadedLogs = ArrayList()
        adapter.submit(emptyList())
        allLogs = emptyList()
        isLocalInitializing = true
        thread {
            val logFile = File(path)
            val reader = LocalLogRepository.openPagedReader(logFile, LOCAL_PAGE_SIZE)
            var entries = reader?.readNextPage().orEmpty()
            if (entries.isEmpty() && logFile.length() > 0L) {
                entries = LocalLogRepository.readEntries(logFile).asReversed()
            }
            val hasMore = reader?.hasMore() == true
            runOnUiThread {
                if (isFinishing || isDestroyed) return@runOnUiThread
                isLocalInitializing = false
                localReader = reader
                localLoadedLogs.addAll(entries)
                allLogs = localLoadedLogs.toList()
                applyFilter()
                if (entries.isEmpty() && !hasMore) {
                    Toast.makeText(this, R.string.vlog_local_file_no_entries, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun ensureLocalLogs() {
        val path = localLogFilePath ?: return
        if (currentLocalFilePath != path || localReader == null && localLoadedLogs.isEmpty() && !isLocalInitializing) {
            loadLocalLogs()
            return
        }
        applyFilter()
    }

    private fun loadNextLocalPage() {
        val reader = localReader ?: return
        if (!reader.hasMore()) return
        isLocalLoadingMore = true
        thread {
            val nextEntries = reader.readNextPage()
            runOnUiThread {
                if (isFinishing || isDestroyed) return@runOnUiThread
                isLocalLoadingMore = false
                if (nextEntries.isEmpty()) return@runOnUiThread
                localLoadedLogs.addAll(nextEntries)
                allLogs = localLoadedLogs.toList()
                applyFilter()
            }
        }
    }

    private fun renderTabs(tabs: List<LogInspectorStore.LogTabItem>) {
        layoutTabs.removeAllViews()
        val marginEnd = (8 * resources.displayMetrics.density).toInt()
        tabs.forEach { tab ->
            val tabView = TextView(this).apply {
                text = tab.label
                textSize = 13f
                setPadding(
                    (16 * resources.displayMetrics.density).toInt(),
                    (8 * resources.displayMetrics.density).toInt(),
                    (16 * resources.displayMetrics.density).toInt(),
                    (8 * resources.displayMetrics.density).toInt()
                )
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    this.marginEnd = marginEnd
                }
                background = tabBackground(selected = tab.key == selectedTabKey)
                setTextColor(if (tab.key == selectedTabKey) Color.parseColor("#FFFFFF") else Color.parseColor("#344054"))
                setOnClickListener {
                    if (selectedTabKey == tab.key) return@setOnClickListener
                    selectedTabKey = tab.key
                    applyFilter()
                }
            }
            layoutTabs.addView(tabView)
        }
        scrollTabs.visibility = if (tabs.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun tabBackground(selected: Boolean): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 999f * resources.displayMetrics.density
            if (selected) {
                setColor(Color.parseColor("#18212F"))
            } else {
                setColor(Color.parseColor("#FFFFFF"))
                setStroke(
                    (1 * resources.displayMetrics.density).toInt().coerceAtLeast(1),
                    Color.parseColor("#D0D5DD")
                )
            }
        }
    }

    private fun installTitleClickToggle() {
        window.decorView.post {
            val titleView = findTitleView(window.decorView, viewerTitle()) ?: return@post
            titleView.isClickable = true
            titleView.isFocusable = true
            titleView.setOnClickListener {
                toggleListPosition()
            }
        }
    }

    private fun toggleListPosition() {
        val layoutManager = listLogs.layoutManager as? LinearLayoutManager ?: return
        val firstCompletelyVisible = layoutManager.findFirstCompletelyVisibleItemPosition()
        if (firstCompletelyVisible <= 0) {
            scrollToBottom()
        } else {
            layoutManager.scrollToPositionWithOffset(0, 0)
        }
    }

    private fun scrollToBottom() {
        val layoutManager = listLogs.layoutManager as? LinearLayoutManager ?: return
        if (!isViewingLocalFile()) {
            val lastIndex = adapter.itemCount - 1
            if (lastIndex >= 0) {
                layoutManager.scrollToPositionWithOffset(lastIndex, 0)
            }
            return
        }
        val reader = localReader
        if (reader == null || !reader.hasMore()) {
            val lastIndex = adapter.itemCount - 1
            if (lastIndex >= 0) {
                layoutManager.scrollToPositionWithOffset(lastIndex, 0)
            }
            return
        }
        if (isLocalInitializing || isLocalLoadingMore) return
        isLocalLoadingMore = true
        thread {
            val appended = ArrayList<LogEntry>()
            while (reader.hasMore()) {
                appended.addAll(reader.readNextPage())
            }
            runOnUiThread {
                if (isFinishing || isDestroyed) return@runOnUiThread
                isLocalLoadingMore = false
                if (appended.isNotEmpty()) {
                    localLoadedLogs.addAll(appended)
                    allLogs = localLoadedLogs.toList()
                    applyFilter()
                }
                val lastIndex = adapter.itemCount - 1
                if (lastIndex >= 0) {
                    layoutManager.scrollToPositionWithOffset(lastIndex, 0)
                }
            }
        }
    }

    private fun findTitleView(view: View, titleText: String): TextView? {
        if (view is TextView && view.text?.toString() == titleText) {
            return view
        }
        if (view is ViewGroup) {
            for (index in 0 until view.childCount) {
                val matched = findTitleView(view.getChildAt(index), titleText)
                if (matched != null) return matched
            }
        }
        return null
    }

    private class VerticalSpaceItemDecoration(
        private val spacePx: Int
    ) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            val position = parent.getChildAdapterPosition(view)
            if (position == RecyclerView.NO_POSITION) return
            if (position > 0) {
                outRect.top = spacePx
            }
        }
    }
}
