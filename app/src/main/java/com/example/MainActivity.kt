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
            val selectedFont by viewModel.selectedFont.collectAsState()
            val backupResult by viewModel.backupResult.collectAsState()

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

            // Connect launchers to ViewModel triggers
            LaunchedEffect(Unit) {
                viewModel.setExportLauncher {
                    exportLauncher.launch(viewModel.suggestedBackupName())
                }
                viewModel.setImportLauncher {
                    importLauncher.launch(arrayOf("application/json", "*/*"))
                }
            }

            // Show Toast on backup/restore result
            LaunchedEffect(backupResult) {
                backupResult?.let { result ->
                    Toast.makeText(applicationContext, result.message, Toast.LENGTH_LONG).show()
                    viewModel.clearBackupResult()
                }
            }

            MyApplicationTheme(darkTheme = isDarkMode, fontName = selectedFont) {
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
