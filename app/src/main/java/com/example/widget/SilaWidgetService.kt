package com.example.widget

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
        return SilaWidgetFactory(applicationContext)
    }
}

class SilaWidgetFactory(private val context: Context) : RemoteViewsService.RemoteViewsFactory {

    private var relativesList: List<Relative> = emptyList()

    override fun onCreate() {
        loadData()
    }

    override fun onDataSetChanged() {
        loadData()
    }

    private fun loadData() {
        try {
            val db = AppDatabase.getDatabase(context)
            val rawList = runBlocking {
                kotlinx.coroutines.withTimeoutOrNull(3000L) {
                    db.relativeDao().getAllRelativesOnce()
                }
            } ?: emptyList()

            // Sort from highest urgency (most overdue / smallest due threshold) to lowest
            relativesList = rawList.sortedWith(
                compareBy<Relative> { relative ->
                    if (relative.lastContactDate == 0L) 0L
                    else relative.lastContactDate + (relative.contactIntervalDays * 86_400_000L)
                }.thenBy { relative ->
                    when (relative.relationshipDegree) {
                        "والدان" -> 1
                        "أشقاء" -> 2
                        "أعمام/أخوال" -> 3
                        else -> 4
                    }
                }.thenBy { it.name }
            )
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
        val statusText = if (relative.lastContactDate == 0L) {
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
