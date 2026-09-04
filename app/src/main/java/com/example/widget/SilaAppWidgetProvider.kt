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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SilaAppWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_WIDGET_CALL = "com.example.silatrahim.ACTION_WIDGET_CALL"
        const val ACTION_WIDGET_CHAT = "com.example.silatrahim.ACTION_WIDGET_CHAT"
        const val ACTION_WIDGET_VIEW_DETAIL = "com.example.silatrahim.ACTION_WIDGET_VIEW_DETAIL"
        const val ACTION_WIDGET_REFRESH = "com.example.silatrahim.ACTION_WIDGET_REFRESH"

        const val EXTRA_PHONE = "extra_phone"
        const val EXTRA_RELATIVE_ID = "extra_relative_id"

        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.sila_widget)

            // 1. Header & Empty state click to open main app
            val mainIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val mainPendingIntent = PendingIntent.getActivity(
                context, 0, mainIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_header, mainPendingIntent)
            views.setOnClickPendingIntent(R.id.widget_empty, mainPendingIntent)

            // 2. Quick Add Button click -> opens MainActivity with open_add_dialog
            val addIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("open_add_dialog", true)
            }
            val addPendingIntent = PendingIntent.getActivity(
                context, 101, addIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_add, addPendingIntent)

            // 3. Quick Refresh Button click -> triggers manual refresh
            val refreshIntent = Intent(context, SilaAppWidgetProvider::class.java).apply {
                action = ACTION_WIDGET_REFRESH
            }
            val refreshPendingIntent = PendingIntent.getBroadcast(
                context, 102, refreshIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_refresh, refreshPendingIntent)

            // 4. Bind RemoteViewsAdapter to ListView
            val adapterIntent = Intent(context, SilaWidgetService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            views.setRemoteAdapter(R.id.widget_list, adapterIntent)
            views.setEmptyView(R.id.widget_list, R.id.widget_empty)

            // 5. Setup PendingIntent template for ListView item clicks (Call, Chat, View Detail)
            val listTemplateIntent = Intent(context, SilaAppWidgetProvider::class.java)
            val listTemplatePendingIntent = PendingIntent.getBroadcast(
                context,
                103,
                listTemplateIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
            views.setPendingIntentTemplate(R.id.widget_list, listTemplatePendingIntent)

            // 6. Update due relatives count badge asynchronously
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getDatabase(context)
                    val relatives = db.relativeDao().getAllRelativesOnce()
                    val now = System.currentTimeMillis()
                    val dueCount = relatives.count { r ->
                        if (r.lastContactDate == 0L) true
                        else (now - r.lastContactDate) >= (r.contactIntervalDays * 86_400_000L)
                    }
                    val badgeText = if (dueCount > 0) "$dueCount في الانتظار 🌸" else "الكل متواصل 💚"
                    val badgeUpdateViews = RemoteViews(context.packageName, R.layout.sila_widget)
                    badgeUpdateViews.setTextViewText(R.id.widget_badge, badgeText)
                    appWidgetManager.partiallyUpdateAppWidget(appWidgetId, badgeUpdateViews)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        /**
         * Automatically triggers a fresh update to all Sila widgets on the home screen
         */
        fun triggerWidgetUpdate(context: Context) {
            try {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val componentName = ComponentName(context, SilaAppWidgetProvider::class.java)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

                // Refresh adapter data in ListView
                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list)

                for (id in appWidgetIds) {
                    updateAppWidget(context, appWidgetManager, id)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val action = intent.action ?: return

        when (action) {
            ACTION_WIDGET_CALL -> {
                val phone = intent.getStringExtra(EXTRA_PHONE) ?: ""
                if (phone.isNotBlank()) {
                    val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(dialIntent)
                }
            }

            ACTION_WIDGET_CHAT -> {
                val phone = intent.getStringExtra(EXTRA_PHONE) ?: ""
                if (phone.isNotBlank()) {
                    val cleanPhone = phone.replace("+", "").replace(" ", "").replace("-", "")
                    val waIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone")).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    try {
                        context.startActivity(waIntent)
                    } catch (e: Exception) {
                        val smsIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$phone")).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        try {
                            context.startActivity(smsIntent)
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                        }
                    }
                }
            }

            ACTION_WIDGET_VIEW_DETAIL -> {
                val relativeId = intent.getIntExtra(EXTRA_RELATIVE_ID, -1)
                val mainIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    if (relativeId != -1) {
                        putExtra("relative_id", relativeId)
                    }
                }
                context.startActivity(mainIntent)
            }

            ACTION_WIDGET_REFRESH -> {
                triggerWidgetUpdate(context)
            }
        }
    }
}
