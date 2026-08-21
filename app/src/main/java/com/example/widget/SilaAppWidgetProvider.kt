package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.AppDatabase
import com.example.utils.DateUtils
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

            // Setup click to open main app
            val mainIntent = Intent(context, MainActivity::class.java)
            val mainPendingIntent = PendingIntent.getActivity(
                context, 0, mainIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_container, mainPendingIntent)

            // Fetch relatives from Room database & find the most urgent relative to contact
            val database = AppDatabase.getDatabase(context.applicationContext, CoroutineScope(Dispatchers.IO))
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val relativeDao = database.relativeDao()
                    val list = relativeDao.getAllRelatives().first()

                    if (list.isNotEmpty()) {
                        // Calculate overdue urgency score: (lastContactDate - intervalMs)
                        // Lower value means most overdue / urgent!
                        val urgent = list.minByOrNull { relative ->
                            val intervalMs = relative.contactIntervalDays * 24L * 60L * 60L * 1000L
                            if (relative.lastContactDate == 0L) 0L else (relative.lastContactDate + intervalMs)
                        } ?: list[0]

                        views.setTextViewText(R.id.widget_title, "صِلَةِ ✨")
                        views.setTextViewText(R.id.widget_contact_name, urgent.name)

                        val statusText = if (urgent.lastContactDate == 0L) {
                            "لم يتصل قط • حان وقت وصله 💚"
                        } else {
                            "آخر تواصل: ${DateUtils.formatRelativeTimeExact(urgent.lastContactDate)}"
                        }
                        views.setTextViewText(R.id.widget_contact_status, statusText)

                        // Direct Call Intent from HomeScreen Widget
                        val callIntent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:${urgent.phone}")
                        }
                        val callPendingIntent = PendingIntent.getActivity(
                            context, urgent.id, callIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        views.setOnClickPendingIntent(R.id.widget_call_button, callPendingIntent)
                    } else {
                        views.setTextViewText(R.id.widget_title, "صِلَةِ ✨")
                        views.setTextViewText(R.id.widget_contact_name, "أضف أقاربك للبدء")
                        views.setTextViewText(R.id.widget_contact_status, "ابدأ بصلة رحمك اليوم 💚")
                    }
                } catch (e: Exception) {
                    views.setTextViewText(R.id.widget_title, "صِلَةِ ✨")
                    views.setTextViewText(R.id.widget_contact_name, "تواصل مع عائلتك")
                    views.setTextViewText(R.id.widget_contact_status, "اضغط لفتح التطبيق")
                } finally {
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            }
        }

        /**
         * Automatically triggers a fresh update to all Sila widgets on the home screen
         */
        fun triggerWidgetUpdate(context: Context) {
            try {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val componentName = ComponentName(context, SilaAppWidgetProvider::class.java)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
                for (id in appWidgetIds) {
                    updateAppWidget(context, appWidgetManager, id)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
