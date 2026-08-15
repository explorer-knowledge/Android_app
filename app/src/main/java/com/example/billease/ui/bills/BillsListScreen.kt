package com.example.billease.ui.bills

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.billease.data.BillWithPerson
import com.example.billease.ui.components.ProfileIconButton
import com.example.billease.util.formatMoney
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillsListScreen(
    onNavigateToBillDetail: (Long) -> Unit,
    onNavigateToBillForm: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: BillsViewModel = hiltViewModel(),
) {
    val bills by viewModel.bills.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val dateRange by viewModel.dateRange.collectAsState()

    var pendingDelete by remember { mutableStateOf<BillWithPerson?>(null) }
    var snackbarMsg by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bills") },
                actions = { ProfileIconButton(onClick = onNavigateToSettings) },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToBillForm) {
                Icon(Icons.Default.Add, contentDescription = "New Bill")
            }
        },
        snackbarHost = {
            snackbarMsg?.let { msg ->
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = { TextButton(onClick = { snackbarMsg = null }) { Text("OK") } },
                ) { Text(msg) }
            }
        },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding),
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                placeholder = { Text("Search bill number or person") },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                singleLine = true,
            )

            DateRangeFilterRow(
                range = dateRange,
                onRangeChange = viewModel::onDateRangeChange,
            )

            if (bills.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.List,
                            contentDescription = "No bills",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No bills yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap + to create one.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        )
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(bills) { bwp ->
                        BillListItem(
                            billWithPerson = bwp,
                            onClick = { onNavigateToBillDetail(bwp.bill.id) },
                            onDeleteClick = { pendingDelete = bwp },
                        )
                    }
                }
            }
        }
    }

    pendingDelete?.let { bwp ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete Bill") },
            text = { Text("Delete ${bwp.bill.billNumber}? This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteBill(bwp.bill) { msg ->
                        snackbarMsg = msg
                    }
                    pendingDelete = null
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Cancel") }
            },
        )
    }
}

@Suppress("MagicNumber", "LongMethod", "FunctionNaming")
@Composable
private fun BillListItem(
    billWithPerson: BillWithPerson,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    val bill = billWithPerson.bill
    val person = billWithPerson.person
    val dateStr =
        remember(bill.billDate) {
            SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(bill.billDate))
        }

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(bill.billNumber, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.width(8.dp))
                    val statusText = bill.paymentStatus
                    val (statusColor, statusBg) =
                        when (statusText.uppercase()) {
                            "PAID" -> Color(0xFF15803D) to Color(0xFFDCFCE7)
                            "OVERDUE" -> Color(0xFFB91C1C) to Color(0xFFFEE2E2)
                            else -> Color(0xFFB45309) to Color(0xFFFEF3C7)
                        }
                    Box(
                        modifier =
                            Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(statusBg)
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = statusText,
                            color = statusColor,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(person.name, style = MaterialTheme.typography.bodyMedium)
                Text(dateStr, style = MaterialTheme.typography.bodySmall)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatMoney(bill.grandTotal),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                IconButton(onClick = onDeleteClick) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("FunctionNaming")
@Composable
private fun DateRangeFilterRow(
    range: Pair<Long?, Long?>,
    onRangeChange: (Pair<Long?, Long?>) -> Unit,
) {
    val start = range.first
    val end = range.second

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FilterDateField(
            millis = start,
            otherMillis = end,
            isStart = true,
            onPicked = { picked -> onRangeChange(picked to end) },
            modifier = Modifier.weight(1f),
        )
        FilterDateField(
            millis = end,
            otherMillis = start,
            isStart = false,
            onPicked = { picked -> onRangeChange(start to picked) },
            modifier = Modifier.weight(1f),
        )
        if (start != null || end != null) {
            TextButton(onClick = { onRangeChange(null to null) }) { Text("Clear") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("FunctionNaming", "MagicNumber")
@Composable
private fun FilterDateField(
    millis: Long?,
    otherMillis: Long?,
    isStart: Boolean,
    onPicked: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showPicker by remember { mutableStateOf(false) }
    val label = if (isStart) "From" else "To"
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val text = millis?.let { dateFormat.format(Date(it)) } ?: label

    OutlinedButton(onClick = { showPicker = true }, modifier = modifier) {
        Text(text, maxLines = 1)
    }

    if (showPicker) {
        val pickerState =
            rememberDatePickerState(
                initialSelectedDateMillis = millis ?: otherMillis ?: System.currentTimeMillis(),
            )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let(onPicked)
                    showPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Cancel") }
            },
        ) {
            DatePicker(state = pickerState)
        }
    }
}
