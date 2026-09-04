package com.example.widget

import android.content.Context
import android.content.Intent
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
            val rawList = runBlocking { db.relativeDao().getAllRelativesOnce() }

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
            "🌸 لم يتصل قط • حان وقت وصله 💚"
        } else {
            val dueMs = relative.lastContactDate + (relative.contactIntervalDays * 86_400_000L)
            if (now >= dueMs) {
                val overdueDays = ((now - dueMs) / 86_400_000L).toInt()
                if (overdueDays <= 0) "🌸 موعد التواصل اليوم"
                else "🔴 متأخر منذ $overdueDays أيام"
            } else {
                "🟢 آخر تواصل: ${DateUtils.formatRelativeTimeExact(relative.lastContactDate)}"
            }
        }
        views.setTextViewText(R.id.widget_item_status, statusText)

        // 1. Fill-in Intent for whole card click (opens Relative Details in App)
        val viewDetailIntent = Intent().apply {
            action = SilaAppWidgetProvider.ACTION_WIDGET_VIEW_DETAIL
            putExtra(SilaAppWidgetProvider.EXTRA_RELATIVE_ID, relative.id)
        }
        views.setOnClickFillInIntent(R.id.widget_item_container, viewDetailIntent)

        // 2. Fill-in Intent for Call button (opens phone dialer)
        val callIntent = Intent().apply {
            action = SilaAppWidgetProvider.ACTION_WIDGET_CALL
            putExtra(SilaAppWidgetProvider.EXTRA_PHONE, relative.phone)
        }
        views.setOnClickFillInIntent(R.id.widget_item_call, callIntent)

        // 3. Fill-in Intent for WhatsApp / Chat button (opens WhatsApp/SMS)
        val chatIntent = Intent().apply {
            action = SilaAppWidgetProvider.ACTION_WIDGET_CHAT
            putExtra(SilaAppWidgetProvider.EXTRA_PHONE, relative.phone)
        }
        views.setOnClickFillInIntent(R.id.widget_item_chat, chatIntent)

        return views
    }

    override fun getLoadingView(): RemoteViews? = null
    override fun getViewTypeCount(): Int = 1
    override fun getItemId(position: Int): Long = relativesList.getOrNull(position)?.id?.toLong() ?: position.toLong()
    override fun hasStableIds(): Boolean = true
}
