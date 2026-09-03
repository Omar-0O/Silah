package com.example.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.widget.*
import com.example.R

/**
 * Shown when the user adds the Silah widget to their home screen.
 * Lets them choose: how many relatives to show, sort order, and status visibility.
 */
class SilaWidgetConfigActivity : Activity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sila_widget_config)

        // Default RESULT_CANCELED — if user presses back, no widget is added
        setResult(RESULT_CANCELED)

        appWidgetId = intent.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        // Load saved prefs for this widget
        val prefs = getSharedPreferences("sila_widget_prefs", MODE_PRIVATE)
        val savedCount = prefs.getInt("widget_${appWidgetId}_count", 5)
        val savedSort = prefs.getString("widget_${appWidgetId}_sort", "due") ?: "due"
        val savedShowStatus = prefs.getBoolean("widget_${appWidgetId}_show_status", true)

        // Count spinner
        val spinner = findViewById<Spinner>(R.id.spinner_count)
        val countOptions = listOf("3", "5", "7", "10")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, countOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.setSelection(countOptions.indexOf(savedCount.toString()).coerceAtLeast(0))

        // Sort radio
        val rgSort = findViewById<RadioGroup>(R.id.rg_sort)
        when (savedSort) {
            "due" -> rgSort.check(R.id.rb_sort_due)
            "degree" -> rgSort.check(R.id.rb_sort_degree)
            "name" -> rgSort.check(R.id.rb_sort_name)
        }

        // Show status toggle
        val switchStatus = findViewById<Switch>(R.id.switch_show_status)
        switchStatus.isChecked = savedShowStatus

        // Save button
        findViewById<Button>(R.id.btn_save_widget).setOnClickListener {
            val selectedCount = countOptions[spinner.selectedItemPosition].toInt()
            val selectedSort = when (rgSort.checkedRadioButtonId) {
                R.id.rb_sort_degree -> "degree"
                R.id.rb_sort_name -> "name"
                else -> "due"
            }
            val showStatus = switchStatus.isChecked

            // Persist settings
            prefs.edit().apply {
                putInt("widget_${appWidgetId}_count", selectedCount)
                putString("widget_${appWidgetId}_sort", selectedSort)
                putBoolean("widget_${appWidgetId}_show_status", showStatus)
                apply()
            }

            // Trigger widget update
            val appWidgetManager = AppWidgetManager.getInstance(this)
            SilaAppWidgetProvider.updateAppWidget(this, appWidgetManager, appWidgetId)

            val resultValue = Intent().apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            setResult(RESULT_OK, resultValue)
            finish()
        }
    }
}
