package com.v.log.inspector

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.v.log.R
import com.v.log.logger.Logger
import java.util.HashMap

class LogViewerAdapter(
    context: Context,
    private val onClick: (LogEntry) -> Unit,
    private val onLongClick: (LogEntry) -> Unit
) : RecyclerView.Adapter<LogViewerAdapter.ViewHolder>() {

    private companion object {
        private val TITLE_COLOR = Color.parseColor("#18212F")
        private val BODY_COLOR = Color.parseColor("#344054")
        private val MUTED_COLOR = Color.parseColor("#667085")
        private val THREAD_PILL_COLOR = Color.parseColor("#F2F4F7")
        private val CARD_FILL_COLOR = Color.parseColor("#FCFCFD")
    }

    private val inflater = LayoutInflater.from(context)
    private val density = context.resources.displayMetrics.density
    private val items = ArrayList<LogEntry>()
    private val threadBackground = pillDrawable(THREAD_PILL_COLOR)
    private val cardBackgrounds = HashMap<Int, GradientDrawable>()
    private var showFullMessage = false

    fun submit(entries: List<LogEntry>) {
        items.clear()
        items.addAll(entries)
        notifyDataSetChanged()
    }

    fun setShowFullMessage(show: Boolean) {
        if (showFullMessage == show) return
        showFullMessage = show
        notifyDataSetChanged()
    }

    fun currentItems(): List<LogEntry> = items.toList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.vlog_item_log_entry, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry = items[position]
        holder.bind(entry)
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(root: View) : RecyclerView.ViewHolder(root) {
        private val logCard: View = root.findViewById(R.id.logCard)
        private val tvTime: TextView = root.findViewById(R.id.tvTime)
        private val tvTag: TextView = root.findViewById(R.id.tvTag)
        private val tvThread: TextView = root.findViewById(R.id.tvThread)
        private val tvMessage: TextView = root.findViewById(R.id.tvMessage)

        fun bind(entry: LogEntry) {
            val accentColor = levelColor(entry.level)
            logCard.background = cardBackgrounds.getOrPut(entry.level) {
                outlinedCardDrawable(accentColor)
            }
            tvTime.apply {
                text = LogInspectorStore.formatTime(entry.timestamp)
                setTextColor(MUTED_COLOR)
            }
            tvTag.apply {
                text = entry.tag
                setTextColor(TITLE_COLOR)
            }
            tvThread.apply {
                text = "Thread: ${entry.thread}"
                setTextColor(MUTED_COLOR)
                background = threadBackground
            }
            tvMessage.apply {
                text = displayMessage(entry)
                setTextColor(BODY_COLOR)
            }
            itemView.setOnClickListener { onClick(entry) }
            itemView.setOnLongClickListener {
                onLongClick(entry)
                true
            }
        }
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
            setColor(CARD_FILL_COLOR)
            setStroke((1.2f * density).toInt().coerceAtLeast(1), mixWithWhite(strokeColor, 0.28f))
        }
    }

    private fun displayMessage(entry: LogEntry): String {
        return if (showFullMessage) entry.message else LogInspectorStore.previewMessage(entry.message)
    }

    private fun mixWithWhite(color: Int, whiteRatio: Float): Int {
        val ratio = whiteRatio.coerceIn(0f, 1f)
        val red = (Color.red(color) * (1 - ratio) + 255 * ratio).toInt()
        val green = (Color.green(color) * (1 - ratio) + 255 * ratio).toInt()
        val blue = (Color.blue(color) * (1 - ratio) + 255 * ratio).toInt()
        return Color.rgb(red, green, blue)
    }
}
