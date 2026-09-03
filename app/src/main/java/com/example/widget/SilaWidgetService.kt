package com.example.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.example.R
import com.example.data.AppDatabase
import com.example.data.Relative
import com.example.utils.DateUtils
import kotlinx.coroutines.runBlocking

class SilaWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        val widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        return SilaWidgetFactory(applicationContext, widgetId)
    }
}

class SilaWidgetFactory(
    private val context: Context,
    private val appWidgetId: Int
) : RemoteViewsService.RemoteViewsFactory {

    private var relativesList: List<Relative> = emptyList()
    private var maxCount: Int = 5
    private var sortMode: String = "due"
    private var showStatus: Boolean = true

    override fun onCreate() { loadData() }
    override fun onDataSetChanged() { loadData() }

    private fun loadData() {
        try {
            // Load per-widget settings
            val prefs = context.getSharedPreferences("sila_widget_prefs", Context.MODE_PRIVATE)
            maxCount = prefs.getInt("widget_${appWidgetId}_count", 5)
            sortMode = prefs.getString("widget_${appWidgetId}_sort", "due") ?: "due"
            showStatus = prefs.getBoolean("widget_${appWidgetId}_show_status", true)

            val db = AppDatabase.getDatabase(context)
            val rawList = runBlocking {
                kotlinx.coroutines.withTimeoutOrNull(3000L) {
                    db.relativeDao().getAllRelativesOnce()
                }
            } ?: emptyList()

            val sorted = when (sortMode) {
                "degree" -> rawList.sortedWith(
                    compareBy {
                        when (it.relationshipDegree) {
                            "والدان" -> 1; "أشقاء" -> 2; "أعمام/أخوال" -> 3; else -> 4
                        }
                    }
                )
                "name" -> rawList.sortedBy { it.name }
                else -> rawList.sortedWith(
                    compareBy<Relative> {
                        if (it.lastContactDate == 0L) 0L
                        else it.lastContactDate + (it.contactIntervalDays * 86_400_000L)
                    }
                )
            }

            relativesList = sorted.take(maxCount)
        } catch (e: Exception) {
            e.printStackTrace()
            relativesList = emptyList()
        }
    }

    override fun onDestroy() {
        relativesList = emptyList()
    }

    override fun getCount(): Int = relativesList.size

    override fun getViewAt(position: Int): RemoteViews {
        if (position < 0 || position >= relativesList.size) {
            return RemoteViews(context.packageName, R.layout.sila_widget_item)
        }

        val relative = relativesList[position]
        val views = RemoteViews(context.packageName, R.layout.sila_widget_item)

        views.setTextViewText(R.id.widget_item_name, relative.name)
        views.setTextViewText(R.id.widget_item_degree, relative.relationshipDegree)

        val now = System.currentTimeMillis()
        val statusText = if (showStatus) {
            if (relative.lastContactDate == 0L) {
                "لم يتصل قط • حان وقت وصله 💚"
            } else {
                val dueMs = relative.lastContactDate + (relative.contactIntervalDays * 86_400_000L)
                if (now >= dueMs) {
                    val overdueDays = ((now - dueMs) / 86_400_000L).toInt()
                    if (overdueDays <= 0) "مستحق التواصل اليوم 🌸"
                    else "مستحق منذ $overdueDays أيام 🔴"
                } else {
                    "آخر تواصل: ${DateUtils.formatRelativeTimeExact(relative.lastContactDate)}"
                }
            }
        } else ""
        views.setTextViewText(R.id.widget_item_status, statusText)

        // Fill-in Intent for item click (dials relative's number or opens app)
        val fillInIntent = Intent().apply {
            putExtra("relative_id", relative.id)
            putExtra("phone", relative.phone)
            data = Uri.parse("tel:${relative.phone}")
        }
        views.setOnClickFillInIntent(R.id.widget_item_container, fillInIntent)
        views.setOnClickFillInIntent(R.id.widget_item_call, fillInIntent)

        return views
    }

    override fun getLoadingView(): RemoteViews? = null
    override fun getViewTypeCount(): Int = 1
    override fun getItemId(position: Int): Long = relativesList.getOrNull(position)?.id?.toLong() ?: position.toLong()
    override fun hasStableIds(): Boolean = true
}
