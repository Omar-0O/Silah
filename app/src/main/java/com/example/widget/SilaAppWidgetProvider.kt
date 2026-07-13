package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SilaAppWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.sila_widget)

            // Setup click to open app
            val mainIntent = Intent(context, MainActivity::class.java)
            val mainPendingIntent = PendingIntent.getActivity(
                context, 0, mainIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_container, mainPendingIntent)

            // Fetch the most urgent contact from Room database asynchronously
            val database = AppDatabase.getDatabase(context.applicationContext, CoroutineScope(Dispatchers.IO))
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val relativeDao = database.relativeDao()
                    // Get all relatives from the flow
                    val list = relativeDao.getAllRelatives().first()
                    
                    if (list.isNotEmpty()) {
                        // Find the one that has been contacted least recently or not at all
                        val urgent = list.minByOrNull { it.lastContactDate } ?: list[0]
                        views.setTextViewText(R.id.widget_title, "صلة الرَّحم ✨")
                        views.setTextViewText(R.id.widget_contact_name, urgent.name)
                        views.setTextViewText(R.id.widget_contact_status, "حان وقت وصله اليوم")

                        // Quick Call Intent
                        val callIntent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:${urgent.phone}")
                        }
                        val callPendingIntent = PendingIntent.getActivity(
                            context, urgent.id, callIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        views.setOnClickPendingIntent(R.id.widget_call_button, callPendingIntent)
                    } else {
                        views.setTextViewText(R.id.widget_title, "صلة الرَّحم ✨")
                        views.setTextViewText(R.id.widget_contact_name, "أضف أقاربك للبدء")
                        views.setTextViewText(R.id.widget_contact_status, "ابدأ بصلة رحمك اليوم")
                    }
                } catch (e: Exception) {
                    views.setTextViewText(R.id.widget_title, "صلة الرَّحم ✨")
                    views.setTextViewText(R.id.widget_contact_name, "تواصل مع عائلتك")
                    views.setTextViewText(R.id.widget_contact_status, "اضغط لفتح التطبيق")
                } finally {
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            }
        }
    }
}
