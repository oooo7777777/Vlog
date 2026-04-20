package com.v.log.inspector

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.v.log.R
import com.v.log.logger.Logger

class LogViewerAdapter(
    context: Context,
    private val onClick: (LogEntry) -> Unit
) : BaseAdapter() {

    private val inflater = LayoutInflater.from(context)
    private val density = context.resources.displayMetrics.density
    private val items = ArrayList<LogEntry>()

    fun submit(entries: List<LogEntry>) {
        items.clear()
        items.addAll(entries)
        notifyDataSetChanged()
    }

    fun currentItems(): List<LogEntry> = items.toList()

    override fun getCount(): Int = items.size

    override fun getItem(position: Int): LogEntry = items[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: inflater.inflate(R.layout.vlog_item_log_entry, parent, false)
        val entry = getItem(position)
        val accentColor = levelColor(entry.level)
        val titleColor = Color.parseColor("#18212F")
        val bodyColor = Color.parseColor("#344054")
        val mutedColor = Color.parseColor("#667085")
        val chipTextColor = darken(accentColor, 0.18f)
        val chipBackgroundColor = mixWithWhite(accentColor, 0.86f)

        view.findViewById<View>(R.id.logCard).background = outlinedCardDrawable(accentColor)
        view.findViewById<TextView>(R.id.tvLevel).apply {
            text = entry.levelName
            setTextColor(chipTextColor)
            background = pillDrawable(chipBackgroundColor)
        }
        view.findViewById<TextView>(R.id.tvTime).apply {
            text = LogInspectorStore.formatTime(entry.timestamp)
            setTextColor(mutedColor)
        }
        view.findViewById<TextView>(R.id.tvTag).apply {
            text = entry.tag
            setTextColor(titleColor)
        }
        view.findViewById<TextView>(R.id.tvThread).apply {
            text = "Thread: ${entry.thread}"
            setTextColor(mutedColor)
            background = pillDrawable(Color.parseColor("#F2F4F7"))
        }
        view.findViewById<TextView>(R.id.tvMessage).apply {
            text = entry.message
            setTextColor(bodyColor)
        }
        view.setOnClickListener { onClick(entry) }
        return view
    }

    private fun levelColor(level: Int): Int {
        return when (level) {
            Logger.DEBUG -> Color.parseColor("#1565C0")
            Logger.INFO -> Color.parseColor("#2E7D32")
            Logger.WARN -> Color.parseColor("#EF6C00")
            Logger.ERROR -> Color.parseColor("#C62828")
            else -> Color.parseColor("#546E7A")
        }
    }

    private fun pillDrawable(fillColor: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 999f * density
            setColor(fillColor)
        }
    }

    private fun outlinedCardDrawable(strokeColor: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 18f * density
            setColor(Color.parseColor("#FCFCFD"))
            setStroke((1.2f * density).toInt().coerceAtLeast(1), mixWithWhite(strokeColor, 0.28f))
        }
    }

    private fun mixWithWhite(color: Int, whiteRatio: Float): Int {
        val ratio = whiteRatio.coerceIn(0f, 1f)
        val red = (Color.red(color) * (1 - ratio) + 255 * ratio).toInt()
        val green = (Color.green(color) * (1 - ratio) + 255 * ratio).toInt()
        val blue = (Color.blue(color) * (1 - ratio) + 255 * ratio).toInt()
        return Color.rgb(red, green, blue)
    }

    private fun darken(color: Int, ratio: Float): Int {
        val factor = (1f - ratio.coerceIn(0f, 1f))
        return Color.rgb(
            (Color.red(color) * factor).toInt(),
            (Color.green(color) * factor).toInt(),
            (Color.blue(color) * factor).toInt()
        )
    }
}
