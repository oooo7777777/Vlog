package com.v.log.inspector

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.v.log.R
import com.v.log.VLog

class LocalLogFilesActivity : AppCompatActivity() {

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, LocalLogFilesActivity::class.java)
        }
    }

    private lateinit var adapter: LocalLogFilesAdapter
    private lateinit var tvSummary: TextView
    private lateinit var tvEmpty: TextView
    private lateinit var btnDeleteAll: TextView
    private lateinit var listLogs: ListView
    private var localLogFiles: List<LocalLogFile> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LogShareExporter.cleanupStaleFiles(this)
        setContentView(R.layout.vlog_activity_local_log_files)
        title = getString(R.string.vlog_local_files_title)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        tvSummary = findViewById(R.id.tvSummary)
        tvEmpty = findViewById(R.id.tvEmpty)
        btnDeleteAll = findViewById(R.id.btnDeleteAll)
        listLogs = findViewById(R.id.listLogs)

        adapter = LocalLogFilesAdapter(this) { file ->
            startActivity(LogViewerActivity.createIntent(this, file.absolutePath))
        }
        listLogs.adapter = adapter

        btnDeleteAll.setOnClickListener { deleteAllLocalFiles() }

        render()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        render()
    }

    private fun render() {
        localLogFiles = LocalLogRepository.listLogFiles(VLog.getFilesAll())
        adapter.submit(localLogFiles)

        tvSummary.text = getString(R.string.vlog_local_files_summary, localLogFiles.size)
        val empty = localLogFiles.isEmpty()
        tvEmpty.visibility = if (empty) View.VISIBLE else View.GONE
        listLogs.visibility = if (empty) View.GONE else View.VISIBLE
        btnDeleteAll.visibility = if (empty) View.GONE else View.VISIBLE
    }

    private fun deleteAllLocalFiles() {
        val deletedCount = LocalLogRepository.deleteAll(localLogFiles)
        Toast.makeText(
            this,
            getString(R.string.vlog_local_files_deleted, deletedCount),
            Toast.LENGTH_SHORT
        ).show()
        render()
    }
}
