package com.example.billease.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.billease.R
import com.example.billease.data.DashboardTimeline
import com.example.billease.ui.components.DateSelectionDialog
import com.example.billease.ui.components.DetailTopAppBar
import com.example.billease.util.SUPPORTED_CURRENCIES
import com.example.billease.util.currencyLabel
import com.example.billease.util.formatDate
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
    val logoLoadFailedMessage = stringResource(R.string.snackbar_logo_load_failed)

    val pickMedia =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                coroutineScope.launch {
                    val newPath = viewModel.copyLogoUri(uri)
                    if (newPath != null) {
                        viewModel.updateLogoPath(newPath)
                    } else {
                        snackbarHostState.showSnackbar(logoLoadFailedMessage)
                    }
                }
            }
        }

    Scaffold(
        topBar = {
            DetailTopAppBar(
                title = stringResource(R.string.settings_title),
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
            // ── Business Identity ─────────────────────────────────────────
            Text(
                stringResource(R.string.business_identity),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 16.dp),
            )

            val businessName = formState.businessName
            val address = formState.address
            val invoicePrefix = formState.invoicePrefix
            val currencyCode = formState.currencyCode
            val logoPath = formState.logoPath

            OutlinedTextField(
                value = businessName,
                onValueChange = viewModel::updateBusinessName,
                label = { Text(stringResource(R.string.business_name)) },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = address,
                onValueChange = viewModel::updateAddress,
                label = { Text(stringResource(R.string.business_address)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = invoicePrefix,
                onValueChange = viewModel::updateInvoicePrefix,
                label = { Text(stringResource(R.string.invoice_prefix)) },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(16.dp))

            CurrencyDropdown(
                currencyCode = currencyCode,
                onCurrencySelected = viewModel::updateCurrencyCode,
            )

            Spacer(Modifier.height(16.dp))

            Text(
                if (logoPath != null) {
                    stringResource(R.string.logo_selected)
                } else {
                    stringResource(R.string.logo_not_selected)
                },
            )

            Spacer(Modifier.height(8.dp))

            OutlinedButton(
                onClick = { pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    if (logoPath != null) {
                        stringResource(R.string.change_logo)
                    } else {
                        stringResource(R.string.select_logo)
                    },
                )
            }

            if (logoPath != null) {
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { viewModel.updateLogoPath(null) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.remove_logo))
                }
            }

            Spacer(Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(Modifier.height(24.dp))

            // ── Dashboard Preferences ─────────────────────────────────────
            Text(
                stringResource(R.string.settings_dashboard_prefs),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 4.dp),
            )
            Text(
                stringResource(R.string.settings_dashboard_prefs_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp),
            )

            TimelineDropdown(
                selected = formState.dashboardTimeline,
                onSelected = { timeline ->
                    viewModel.updateDashboardTimeline(timeline)
                    // Reset custom bounds when switching away from CUSTOM
                    if (timeline != DashboardTimeline.CUSTOM) {
                        viewModel.updateCustomTimelineStart(null)
                        viewModel.updateCustomTimelineEnd(null)
                    }
                },
            )

            // Custom date range inputs — only visible when CUSTOM is chosen
            if (formState.dashboardTimeline == DashboardTimeline.CUSTOM) {
                Spacer(Modifier.height(12.dp))
                CustomTimelineRow(
                    startMillis = formState.customTimelineStart,
                    endMillis = formState.customTimelineEnd,
                    onStartPicked = viewModel::updateCustomTimelineStart,
                    onEndPicked = viewModel::updateCustomTimelineEnd,
                )
            }

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = {
                    viewModel.save()
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.save_settings))
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Currency dropdown (unchanged from original)
// ---------------------------------------------------------------------------

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
            label = { Text(stringResource(R.string.currency)) },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            trailingIcon = {
                TextButton(onClick = { expanded = true }) { Text(stringResource(R.string.select)) }
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

// ---------------------------------------------------------------------------
// Timeline dropdown
// ---------------------------------------------------------------------------

@Composable
private fun TimelineDropdown(
    selected: DashboardTimeline,
    onSelected: (DashboardTimeline) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedTextField(
            value = timelineLabel(selected),
            onValueChange = {},
            label = { Text(stringResource(R.string.settings_home_timeline)) },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            trailingIcon = {
                TextButton(onClick = { expanded = true }) { Text(stringResource(R.string.select)) }
            },
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DashboardTimeline.entries.forEach { timeline ->
                DropdownMenuItem(
                    text = { Text(timelineLabel(timeline)) },
                    onClick = {
                        onSelected(timeline)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun timelineLabel(timeline: DashboardTimeline): String =
    when (timeline) {
        DashboardTimeline.TODAY -> stringResource(R.string.timeline_today)
        DashboardTimeline.THIS_WEEK -> stringResource(R.string.timeline_this_week)
        DashboardTimeline.THIS_MONTH -> stringResource(R.string.this_month)
        DashboardTimeline.THIS_YEAR -> stringResource(R.string.timeline_this_year)
        DashboardTimeline.CUSTOM -> stringResource(R.string.timeline_custom)
    }

// ---------------------------------------------------------------------------
// Custom date range row (only shown when CUSTOM is selected)
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomTimelineRow(
    startMillis: Long?,
    endMillis: Long?,
    onStartPicked: (Long?) -> Unit,
    onEndPicked: (Long?) -> Unit,
) {
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    Row(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = { showStartPicker = true },
            modifier = Modifier.weight(1f),
        ) {
            Text(startMillis?.let { formatDate(it) } ?: stringResource(R.string.from))
        }
        Spacer(Modifier.padding(horizontal = 4.dp))
        OutlinedButton(
            onClick = { showEndPicker = true },
            modifier = Modifier.weight(1f),
        ) {
            Text(endMillis?.let { formatDate(it) } ?: stringResource(R.string.to))
        }
    }

    if (showStartPicker) {
        DateSelectionDialog(
            initialDateMillis = startMillis ?: System.currentTimeMillis(),
            onDateSelected = { onStartPicked(it) },
            onDismiss = { showStartPicker = false },
        )
    }
    if (showEndPicker) {
        DateSelectionDialog(
            initialDateMillis = endMillis ?: System.currentTimeMillis(),
            onDateSelected = { onEndPicked(it) },
            onDismiss = { showEndPicker = false },
        )
    }
}
