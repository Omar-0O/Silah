package com.example

import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.screens.AppNavigation
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.RelativeViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: RelativeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsState()
            val selectedLanguage by viewModel.selectedLanguage.collectAsState()
            val backupResult by viewModel.backupResult.collectAsState()

            // ── Export backup (SAF) ────────────────────────────────────────
            val exportLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.CreateDocument("application/json")
            ) { uri ->
                uri?.let { viewModel.exportBackup(applicationContext, it) }
            }

            // ── Import backup (SAF) ────────────────────────────────────────
            val importLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.OpenDocument()
            ) { uri ->
                uri?.let { viewModel.importBackup(applicationContext, it) }
            }

            // ── Native Contact Picker (no READ_CONTACTS needed) ───────────
            val contactPickerLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.PickContact()
            ) { contactUri: Uri? ->
                contactUri?.let { uri ->
                    // Resolve display name + phone from the picked contact
                    val name = resolveContactName(uri)
                    val phone = resolveContactPhone(uri)
                    if (name.isNotBlank() || phone.isNotBlank()) {
                        viewModel.onContactPicked(name, phone)
                    }
                }
            }

            // ── Notification permission (Android 13+) ──────────────────────
            val notifPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { /* granted or not — we'll still show a reminder next time */ }

            LaunchedEffect(Unit) {
                // Request notification permission on first launch (Android 13+)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    notifPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }

                // Wire up launchers in ViewModel
                viewModel.setExportLauncher { exportLauncher.launch(viewModel.suggestedBackupName()) }
                viewModel.setImportLauncher { importLauncher.launch(arrayOf("application/json", "*/*")) }
                viewModel.setContactPickerLauncher { contactPickerLauncher.launch(null) }
            }

            // Show backup/restore result toast
            LaunchedEffect(backupResult) {
                backupResult?.let { result ->
                    Toast.makeText(applicationContext, result.message, Toast.LENGTH_LONG).show()
                    viewModel.clearBackupResult()
                }
            }

            // Handle deep-link from notification "mark_contacted" action
            val intentAction = intent?.getStringExtra("action")
            val intentRelativeId = intent?.getIntExtra("relative_id", -1) ?: -1
            LaunchedEffect(intentAction, intentRelativeId) {
                if (intentAction == "mark_contacted" && intentRelativeId != -1) {
                    viewModel.showMarkContactedPrompt(intentRelativeId)
                }
            }

            MyApplicationTheme(darkTheme = isDarkMode, fontName = "Almarai") {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(viewModel = viewModel)
                }
            }
        }
    }

    /** Resolve display name from contact URI returned by native contact picker. */
    private fun resolveContactName(uri: Uri): String {
        return try {
            contentResolver.query(
                uri,
                arrayOf(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY),
                null, null, null
            )?.use { cursor ->
                if (cursor.moveToFirst()) cursor.getString(0) ?: "" else ""
            } ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    /** Resolve first phone number from contact URI. */
    private fun resolveContactPhone(uri: Uri): String {
        return try {
            // Get contact ID first
            val contactId = contentResolver.query(
                uri,
                arrayOf(ContactsContract.Contacts._ID),
                null, null, null
            )?.use { cursor ->
                if (cursor.moveToFirst()) cursor.getString(0) else null
            } ?: return ""

            // Then query phone numbers
            contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                arrayOf(contactId),
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) cursor.getString(0) ?: "" else ""
            } ?: ""
        } catch (e: Exception) {
            ""
        }
    }
}
