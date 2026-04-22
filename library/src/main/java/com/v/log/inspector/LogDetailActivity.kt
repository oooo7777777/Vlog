package com.v.log.inspector

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.v.log.R
import com.v.log.logger.Logger

class LogDetailActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_TIMESTAMP = "extra_timestamp"
        private const val EXTRA_LEVEL = "extra_level"
        private const val EXTRA_LEVEL_NAME = "extra_level_name"
        private const val EXTRA_TAG = "extra_tag"
        private const val EXTRA_THREAD = "extra_thread"
        private const val EXTRA_MESSAGE = "extra_message"

        fun createIntent(context: Context, entry: LogEntry): Intent {
            return Intent(context, LogDetailActivity::class.java).apply {
                putExtra(EXTRA_TIMESTAMP, entry.timestamp)
                putExtra(EXTRA_LEVEL, entry.level)
                putExtra(EXTRA_LEVEL_NAME, entry.levelName)
                putExtra(EXTRA_TAG, entry.tag)
                putExtra(EXTRA_THREAD, entry.thread)
                putExtra(EXTRA_MESSAGE, entry.message)
            }
        }
    }

    private lateinit var entry: LogEntry

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LogShareExporter.cleanupStaleFiles(this)
        setContentView(R.layout.vlog_activity_log_detail)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = getString(R.string.vlog_detail_title)
        entry = LogEntry(
            timestamp = intent.getLongExtra(EXTRA_TIMESTAMP, System.currentTimeMillis()),
            level = intent.getIntExtra(EXTRA_LEVEL, 0),
            levelName = intent.getStringExtra(EXTRA_LEVEL_NAME).orEmpty(),
            tag = intent.getStringExtra(EXTRA_TAG).orEmpty(),
            thread = intent.getStringExtra(EXTRA_THREAD).orEmpty(),
            message = intent.getStringExtra(EXTRA_MESSAGE).orEmpty()
        )

        bindView()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.vlog_menu_log_detail, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }

            R.id.action_copy -> {
                copyEntry()
                true
            }

            R.id.action_share -> {
                shareEntry()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun bindView() {
        val levelColor = levelBorderColor(entry.level)
        findViewById<android.widget.TextView>(R.id.tvTimeValue).text = LogInspectorStore.formatTime(entry.timestamp)
        findViewById<android.widget.TextView>(R.id.tvLevelValue).apply {
            text = entry.levelName
            setTextColor(levelColor)
        }
        findViewById<android.widget.TextView>(R.id.tvTagValue).text = entry.tag
        findViewById<android.widget.TextView>(R.id.tvThreadValue).text = entry.thread
        findViewById<android.widget.TextView>(R.id.tvMessageValue).apply {
            text = entry.message
            setTextColor(Color.parseColor("#000000"))
        }
    }

    private fun copyEntry() {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("vlog-entry", entry.asClipboardText()))
        Toast.makeText(this, R.string.vlog_copied, Toast.LENGTH_SHORT).show()
    }

    private fun shareEntry() {
        LogShareExporter.shareEntryAsTextFile(this, entry)
    }

    private fun levelBorderColor(level: Int): Int {
        val accent = when (level) {
            Logger.VERBOSE -> Color.parseColor("#6D28D9")
            Logger.DEBUG -> Color.parseColor("#1565C0")
            Logger.INFO -> Color.parseColor("#2E7D32")
            Logger.WARN -> Color.parseColor("#EF6C00")
            Logger.ERROR -> Color.parseColor("#C62828")
            Logger.ASSERT -> Color.parseColor("#111827")
            else -> Color.parseColor("#546E7A")
        }
        return mixWithWhite(accent, 0.28f)
    }

    private fun mixWithWhite(color: Int, whiteRatio: Float): Int {
        val ratio = whiteRatio.coerceIn(0f, 1f)
        val red = (Color.red(color) * (1 - ratio) + 255 * ratio).toInt()
        val green = (Color.green(color) * (1 - ratio) + 255 * ratio).toInt()
        val blue = (Color.blue(color) * (1 - ratio) + 255 * ratio).toInt()
        return Color.rgb(red, green, blue)
    }
}
