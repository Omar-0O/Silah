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

class SilaAppWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_WIDGET_CALL) {
            val phone = intent.getStringExtra("phone") ?: ""
            if (phone.isNotBlank()) {
                try {
                    val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:$phone")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(dialIntent)
                } catch (e: Exception) {
                    try {
                        val mainIntent = Intent(context, MainActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(mainIntent)
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }
                }
            } else {
                try {
                    val mainIntent = Intent(context, MainActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(mainIntent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    companion object {
        const val ACTION_WIDGET_CALL = "com.example.silah.ACTION_WIDGET_CALL"

        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.sila_widget)

            // Setup Header & Empty state click to open main app
            val mainIntent = Intent(context, MainActivity::class.java)
            val mainPendingIntent = PendingIntent.getActivity(
                context, 0, mainIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_header, mainPendingIntent)
            views.setOnClickPendingIntent(R.id.widget_empty, mainPendingIntent)

            // Bind RemoteViewsAdapter to ListView
            val adapterIntent = Intent(context, SilaWidgetService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            views.setRemoteAdapter(R.id.widget_list, adapterIntent)
            views.setEmptyView(R.id.widget_list, R.id.widget_empty)

            // Setup Broadcast PendingIntent template for ListView item clicks
            val broadcastIntent = Intent(context, SilaAppWidgetProvider::class.java).apply {
                action = ACTION_WIDGET_CALL
            }
            val callPendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                broadcastIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
            views.setPendingIntentTemplate(R.id.widget_list, callPendingIntent)

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
}
