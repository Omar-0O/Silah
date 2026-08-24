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

import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {

    private val viewModel: RelativeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                    if (!callLogGranted) {
                        ungrantedPermissions.add(android.Manifest.permission.READ_CALL_LOG)
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
}
