package com.v.log.inspector

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.v.log.R

class LocalLogFilesAdapter(
    context: Context,
    private val onClick: (LocalLogFile) -> Unit
) : BaseAdapter() {

    private val inflater = LayoutInflater.from(context)
    private val appContext = context.applicationContext
    private val density = context.resources.displayMetrics.density
    private val items = ArrayList<LocalLogFile>()

    fun submit(entries: List<LocalLogFile>) {
        items.clear()
        items.addAll(entries)
        notifyDataSetChanged()
    }

    override fun getCount(): Int = items.size

    override fun getItem(position: Int): LocalLogFile = items[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: inflater.inflate(R.layout.vlog_item_local_log_file, parent, false)
        val item = getItem(position)

        view.findViewById<TextView>(R.id.tvFileName).apply {
            text = item.name
            setTextColor(Color.parseColor("#18212F"))
        }
        view.findViewById<TextView>(R.id.tvFileMeta).apply {
            text = appContext.getString(
                R.string.vlog_local_file_meta,
                LogInspectorStore.formatTime(item.lastModified),
                Formatter.formatShortFileSize(appContext, item.sizeBytes)
            )
            setTextColor(Color.parseColor("#667085"))
        }
        view.findViewById<TextView>(R.id.tvOpenAction).apply {
            background = pillDrawable(Color.parseColor("#EEF4FF"))
            setTextColor(Color.parseColor("#1D4ED8"))
        }
        view.setOnClickListener { onClick(item) }
        return view
    }

    private fun pillDrawable(fillColor: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 999f * density
            setColor(fillColor)
        }
    }
}
