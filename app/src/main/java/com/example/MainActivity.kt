package com.example

import android.os.Bundle
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.screens.AppNavigation
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.RelativeViewModel

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import android.net.Uri
import android.provider.ContactsContract

class MainActivity : ComponentActivity() {

    private val viewModel: RelativeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleNotificationOrWidgetIntent(intent)
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            android.util.Log.e("SILAH_CRASH", "Uncaught exception in thread ${thread.name}", throwable)
        }
        enableEdgeToEdge()
        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsState()
            val selectedLanguage by viewModel.selectedLanguage.collectAsState()
            val backupResult by viewModel.backupResult.collectAsState()
            val layoutDirection = if (selectedLanguage == "en") LayoutDirection.Ltr else LayoutDirection.Rtl

            // ── Export: Opens Save-File dialog (SAF) ──────────────────────
            val exportLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.CreateDocument("application/json")
            ) { uri ->
                uri?.let { viewModel.exportBackup(applicationContext, it) }
            }

            // ── Import: Opens Open-File dialog (SAF) ──────────────────────
            val importLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.OpenDocument()
            ) { uri ->
                uri?.let { viewModel.importBackup(applicationContext, it) }
            }

            // ── Native Contact Picker (no broad READ_CONTACTS needed) ───────────
            val contactPickerLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    result.data?.data?.let { uri ->
                        val (name, phone) = resolveContactData(uri)
                        if (name.isNotBlank() || phone.isNotBlank()) {
                            viewModel.onContactPicked(name, phone)
                        }
                    }
                }
            }

            // ── Immediate Permissions Launcher (Contacts + Call Log) ──────
            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestMultiplePermissions()
            ) { permissions ->
                val callLogGranted = permissions[android.Manifest.permission.READ_CALL_LOG] ?: false
                if (callLogGranted) {
                    viewModel.syncCallLogsWithRelatives(applicationContext)
                }
            }

            // Connect launchers and request startup permissions if not already granted
            LaunchedEffect(Unit) {
                try {
                    val callLogGranted = ContextCompat.checkSelfPermission(this@MainActivity, android.Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED
                    if (callLogGranted) {
                        try {
                            viewModel.syncCallLogsWithRelatives(applicationContext)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    val ungrantedPermissions = mutableListOf<String>()
                    if (ContextCompat.checkSelfPermission(this@MainActivity, android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                        ungrantedPermissions.add(android.Manifest.permission.READ_CONTACTS)
                    }
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(this@MainActivity, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                            ungrantedPermissions.add(android.Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }

                    if (ungrantedPermissions.isNotEmpty()) {
                        try {
                            permissionLauncher.launch(ungrantedPermissions.toTypedArray())
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    viewModel.setExportLauncher {
                        try {
                            exportLauncher.launch(viewModel.suggestedBackupName())
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    viewModel.setImportLauncher {
                        try {
                            importLauncher.launch(arrayOf("application/json", "*/*"))
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    viewModel.setContactPickerLauncher {
                        try {
                            val intent = Intent(
                                Intent.ACTION_PICK,
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI
                            )
                            contactPickerLauncher.launch(intent)
                        } catch (e: Exception) {
                            try {
                                val fallbackIntent = Intent(
                                    Intent.ACTION_PICK,
                                    ContactsContract.Contacts.CONTENT_URI
                                )
                                contactPickerLauncher.launch(fallbackIntent)
                            } catch (e2: Exception) {
                                e2.printStackTrace()
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // Show Toast on backup/restore result
            LaunchedEffect(backupResult) {
                backupResult?.let { result ->
                    Toast.makeText(applicationContext, result.message, Toast.LENGTH_LONG).show()
                    viewModel.clearBackupResult()
                }
            }

            CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
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
    }

    override fun onStart() {
        super.onStart()
        viewModel.checkPendingNotifiedRelatives()
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNotificationOrWidgetIntent(intent)
        viewModel.checkPendingNotifiedRelatives()
    }

    private fun handleNotificationOrWidgetIntent(intent: android.content.Intent?) {
        val relativeId = intent?.getIntExtra("relative_id", -1) ?: -1
        if (relativeId != -1) {
            viewModel.selectRelativeById(relativeId)
        }
        if (intent?.getBooleanExtra("open_add_dialog", false) == true) {
            viewModel.openAddRelativeDialog()
        }
    }

    /** Resolve display name and phone number from contact URI returned by native contact picker. */
    private fun resolveContactData(uri: Uri): Pair<String, String> {
        var name = ""
        var phone = ""

        // 1. Direct query on the returned URI (works directly for Phone.CONTENT_URI and Data URIs without broad permissions)
        try {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameCols = arrayOf(
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
                        ContactsContract.Contacts.DISPLAY_NAME
                    )
                    for (col in nameCols) {
                        val idx = cursor.getColumnIndex(col)
                        if (idx >= 0) {
                            val candidate = cursor.getString(idx)?.trim()
                            if (!candidate.isNullOrBlank()) {
                                name = candidate
                                break
                            }
                        }
                    }

                    val phoneCols = arrayOf(
                        ContactsContract.CommonDataKinds.Phone.NUMBER,
                        "data1",
                        ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER
                    )
                    for (col in phoneCols) {
                        val idx = cursor.getColumnIndex(col)
                        if (idx >= 0) {
                            val candidate = cursor.getString(idx)?.trim()
                            if (!candidate.isNullOrBlank()) {
                                phone = candidate
                                break
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("SilahContact", "Error querying contact URI directly", e)
        }

        // 2. If phone is still empty, and uri might be a Contact URI: query Data subdirectory under that contact URI
        if (phone.isBlank()) {
            try {
                val dataUri = Uri.withAppendedPath(uri, ContactsContract.Contacts.Data.CONTENT_DIRECTORY)
                contentResolver.query(
                    dataUri,
                    arrayOf(
                        ContactsContract.CommonDataKinds.Phone.NUMBER,
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
                    ),
                    "${ContactsContract.Data.MIMETYPE} = ?",
                    arrayOf(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE),
                    null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val numIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                        if (numIdx >= 0 && phone.isBlank()) {
                            phone = cursor.getString(numIdx)?.trim() ?: ""
                        }
                        val nameIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                        if (nameIdx >= 0 && name.isBlank()) {
                            name = cursor.getString(nameIdx)?.trim() ?: ""
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("SilahContact", "Error querying contact data subdirectory", e)
            }
        }

        // 3. Fallback: If still empty, try resolving ID and querying CommonDataKinds.Phone (if permission allows)
        if (phone.isBlank()) {
            try {
                val contactId = contentResolver.query(
                    uri,
                    arrayOf(ContactsContract.Contacts._ID),
                    null, null, null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val idIdx = cursor.getColumnIndex(ContactsContract.Contacts._ID)
                        if (idIdx >= 0) cursor.getString(idIdx) else cursor.getString(0)
                    } else null
                }

                if (!contactId.isNullOrBlank()) {
                    contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        arrayOf(
                            ContactsContract.CommonDataKinds.Phone.NUMBER,
                            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
                        ),
                        "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                        arrayOf(contactId),
                        null
                    )?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            val numIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                            if (numIdx >= 0 && phone.isBlank()) {
                                phone = cursor.getString(numIdx)?.trim() ?: ""
                            }
                            val nameIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                            if (nameIdx >= 0 && name.isBlank()) {
                                name = cursor.getString(nameIdx)?.trim() ?: ""
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("SilahContact", "Error querying Phone.CONTENT_URI fallback", e)
            }
        }

        return Pair(name.trim(), phone.trim())
    }
}
