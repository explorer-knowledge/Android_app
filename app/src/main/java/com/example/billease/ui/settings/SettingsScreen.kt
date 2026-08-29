package com.example.billease.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.billease.ui.components.DetailTopAppBar
import com.example.billease.util.SUPPORTED_CURRENCIES
import com.example.billease.util.currencyLabel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("LongMethod")
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val formState by viewModel.formState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val pickMedia =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                coroutineScope.launch {
                    val newPath = viewModel.copyLogoUri(uri)
                    if (newPath != null) {
                        viewModel.updateLogoPath(newPath)
                    } else {
                        snackbarHostState.showSnackbar("Could not load that image. Please try another.")
                    }
                }
            }
        }

    Scaffold(
        topBar = {
            DetailTopAppBar(
                title = "Settings",
                onNavigateBack = onNavigateBack,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
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

            val businessName = formState.businessName
            val address = formState.address
            val invoicePrefix = formState.invoicePrefix
            val currencyCode = formState.currencyCode
            val logoPath = formState.logoPath

            OutlinedTextField(
                value = businessName,
                onValueChange = viewModel::updateBusinessName,
                label = { Text("Business Name") },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = address,
                onValueChange = viewModel::updateAddress,
                label = { Text("Business Address") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = invoicePrefix,
                onValueChange = viewModel::updateInvoicePrefix,
                label = { Text("Invoice Prefix") },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(16.dp))

            CurrencyDropdown(
                currencyCode = currencyCode,
                onCurrencySelected = viewModel::updateCurrencyCode,
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
                    onClick = { viewModel.updateLogoPath(null) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Remove Logo")
                }
            }

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = {
                    viewModel.save()
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Save Settings")
            }
        }
    }
}

@Composable
private fun CurrencyDropdown(
    currencyCode: String,
    onCurrencySelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedTextField(
            value = currencyLabel(currencyCode),
            onValueChange = {},
            label = { Text("Currency") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            trailingIcon = {
                TextButton(onClick = { expanded = true }) { Text("Select") }
            },
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            SUPPORTED_CURRENCIES.forEach { code ->
                DropdownMenuItem(
                    text = { Text(currencyLabel(code)) },
                    onClick = {
                        onCurrencySelected(code)
                        expanded = false
                    },
                )
            }
        }
    }
}
