package com.example

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast

/**
 * Handles notification action buttons (📞 Call, 💬 WhatsApp) directly
 * from the notification shade — without opening the app first.
 *
 * Actions:
 *   ACTION_CALL       → opens phone dialer with the relative's number
 *   ACTION_WHATSAPP   → opens WhatsApp chat with the relative's number
 */
class NotificationActionReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_CALL = "com.example.silatrahim.ACTION_CALL"
        const val ACTION_WHATSAPP = "com.example.silatrahim.ACTION_WHATSAPP"
        const val EXTRA_PHONE = "extra_phone"
        const val EXTRA_RELATIVE_NAME = "extra_relative_name"
        const val EXTRA_RELATIVE_ID = "extra_relative_id"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val phone = intent.getStringExtra(EXTRA_PHONE) ?: return
        val relativeName = intent.getStringExtra(EXTRA_RELATIVE_NAME) ?: ""
        val lang = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
            .getString("selected_language", "ar") ?: "ar"

        when (intent.action) {
            ACTION_CALL -> openDialer(context, phone)
            ACTION_WHATSAPP -> openWhatsApp(context, phone, lang)
        }
    }

    private fun openDialer(context: Context, phone: String) {
        val dialIntent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phone")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(dialIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun openWhatsApp(context: Context, phone: String, lang: String) {
        // Normalize: strip spaces/dashes, ensure + prefix for international
        val normalized = phone.replace("[^\\d+]".toRegex(), "")

        val waIntent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://wa.me/$normalized")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val isWhatsAppInstalled = try {
            context.packageManager.getPackageInfo("com.whatsapp", PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }

        if (isWhatsAppInstalled) {
            try {
                context.startActivity(waIntent)
            } catch (e: Exception) {
                // Fallback to generic browser link
                try {
                    context.startActivity(waIntent)
                } catch (e2: Exception) {
                    openDialer(context, phone)
                }
            }
        } else {
            // WhatsApp not installed → fallback to dialer
            val toastMsg = if (lang == "en") "WhatsApp is not installed, opening dialer instead"
            else "واتساب غير مثبت، سيتم فتح الاتصال بدلاً منه"
            Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show()
            openDialer(context, phone)
        }
    }
}
