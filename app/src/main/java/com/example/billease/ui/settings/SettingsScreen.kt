package com.example.billease.ui.settings

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("LongMethod", "FunctionNaming")
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val settings by viewModel.appSettings.collectAsState()

    var businessName by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var logoPath by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(settings) {
        businessName = settings.businessName
        address = settings.address
        logoPath = settings.logoUri
    }

    val pickMedia =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                val newPath = copyUriToInternalStorage(context, uri)
                if (newPath != null) {
                    logoPath = newPath
                }
            }
        }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
        ) {
            Text("Business Identity", modifier = Modifier.padding(bottom = 16.dp))

            OutlinedTextField(
                value = businessName,
                onValueChange = { businessName = it },
                label = { Text("Business Name") },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Business Address") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
            )

            Spacer(Modifier.height(16.dp))

            Text("Logo: ${if (logoPath != null) "Selected" else "Not selected"}")

            Spacer(Modifier.height(8.dp))

            OutlinedButton(
                onClick = { pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (logoPath != null) "Change Logo" else "Select Logo")
            }

            if (logoPath != null) {
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { logoPath = null },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Remove Logo")
                }
            }

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = {
                    viewModel.updateSettings(businessName, address, logoPath)
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Save Settings")
            }
        }
    }
}

fun copyUriToInternalStorage(
    context: Context,
    uri: Uri,
): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val file = File(context.filesDir, "business_logo.jpg")
        val outputStream = FileOutputStream(file)
        inputStream.copyTo(outputStream)
        inputStream.close()
        outputStream.close()
        file.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
