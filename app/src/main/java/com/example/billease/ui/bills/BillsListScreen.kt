package com.example.billease.ui.bills

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.billease.R
import com.example.billease.data.BillWithPerson
import com.example.billease.data.Person
import com.example.billease.data.Product
import com.example.billease.ui.components.ConfirmDeleteDialog
import com.example.billease.ui.components.DateSelectionDialog
import com.example.billease.ui.components.DismissableSnackbar
import com.example.billease.ui.components.EmptyState
import com.example.billease.ui.components.ScreenHeader
import com.example.billease.ui.components.StatusBadge
import com.example.billease.util.LocalCurrencyCode
import com.example.billease.util.formatDate
import com.example.billease.util.formatMoney

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillsListScreen(
    onNavigateToBillDetail: (Long) -> Unit,
    onNavigateToBillForm: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: BillsViewModel = hiltViewModel(),
) {
    val bills by viewModel.bills.collectAsState()
    val filter by viewModel.filter.collectAsState()
    val persons by viewModel.persons.collectAsState()
    val products by viewModel.products.collectAsState()

    var pendingDelete by remember { mutableStateOf<BillWithPerson?>(null) }
    var snackbarMsg by remember { mutableStateOf<String?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToBillForm) {
                Icon(Icons.Default.Add, contentDescription = "New Bill")
            }
        },
        snackbarHost = {
            snackbarMsg?.let { msg ->
                DismissableSnackbar(
                    message = msg,
                    onDismiss = { snackbarMsg = null },
                    dismissLabel = stringResource(R.string.ok),
                )
            }
        },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding),
        ) {
            ScreenHeader(
                heading = stringResource(R.string.bills_heading),
                query = filter.searchQuery,
                onQueryChange = viewModel::onSearchQueryChange,
                onNavigateToSettings = onNavigateToSettings,
            )

            BillFilterRow(
                filter = filter,
                persons = persons,
                products = products,
                onDatePreset = viewModel::onDatePresetChange,
                onCustomRange = viewModel::onCustomDateRange,
                onPersonFilter = viewModel::onPersonFilter,
                onProductFilter = viewModel::onProductFilter,
                onClearAll = viewModel::clearAllFilters,
            )

            val emptyTitle =
                stringResource(if (filter.isActive) R.string.no_bills_match else R.string.no_bills_yet)
            val emptySubtitle =
                stringResource(if (filter.isActive) R.string.try_clearing_filters else R.string.tap_to_create_bill)
            if (bills.isEmpty()) {
                EmptyState(
                    icon = Icons.AutoMirrored.Outlined.List,
                    title = emptyTitle,
                    subtitle = emptySubtitle,
                )
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
        ConfirmDeleteDialog(
            title = stringResource(R.string.delete_bill_title),
            message = stringResource(R.string.confirm_delete_bill, bwp.bill.billNumber),
            onConfirm = {
                viewModel.deleteBill(bwp.bill) { msg ->
                    snackbarMsg = msg
                }
                pendingDelete = null
            },
            onDismiss = { pendingDelete = null },
        )
    }
}

// ---------------------------------------------------------------------------
// Filter chip row
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BillFilterRow(
    filter: BillFilterState,
    persons: List<Person>,
    products: List<Product>,
    onDatePreset: (DateFilterPreset) -> Unit,
    onCustomRange: (Long?, Long?) -> Unit,
    onPersonFilter: (Person?) -> Unit,
    onProductFilter: (Product?) -> Unit,
    onClearAll: () -> Unit,
) {
    var showDateMenu by remember { mutableStateOf(false) }
    var showPersonPicker by remember { mutableStateOf(false) }
    var showProductPicker by remember { mutableStateOf(false) }
    var showCustomStart by remember { mutableStateOf(false) }
    var showCustomEnd by remember { mutableStateOf(false) }

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // ── Date filter chip ──────────────────────────────────────────────
        item {
            val dateLabel =
                when (filter.datePreset) {
                    DateFilterPreset.ALL -> stringResource(R.string.filter_date)
                    DateFilterPreset.RECENT -> stringResource(R.string.filter_date_recent)
                    DateFilterPreset.THIS_MONTH -> stringResource(R.string.this_month)
                    DateFilterPreset.CUSTOM -> {
                        val s = filter.customStart?.let { formatDate(it) } ?: "?"
                        val e = filter.customEnd?.let { formatDate(it) } ?: "?"
                        "$s – $e"
                    }
                }
            FilterChip(
                selected = filter.datePreset != DateFilterPreset.ALL,
                onClick = { showDateMenu = true },
                label = { Text(dateLabel) },
                trailingIcon = {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                },
            )
            DropdownMenu(expanded = showDateMenu, onDismissRequest = { showDateMenu = false }) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.filter_all)) },
                    onClick = {
                        onDatePreset(DateFilterPreset.ALL)
                        showDateMenu = false
                    },
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.filter_date_recent)) },
                    onClick = {
                        onDatePreset(DateFilterPreset.RECENT)
                        showDateMenu = false
                    },
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.this_month)) },
                    onClick = {
                        onDatePreset(DateFilterPreset.THIS_MONTH)
                        showDateMenu = false
                    },
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.filter_date_custom)) },
                    onClick = {
                        showDateMenu = false
                        showCustomStart = true
                    },
                )
            }
        }

        // ── Person filter chip ────────────────────────────────────────────
        item {
            FilterChip(
                selected = filter.selectedPerson != null,
                onClick = { showPersonPicker = true },
                label = {
                    Text(filter.selectedPerson?.name ?: stringResource(R.string.filter_person))
                },
                trailingIcon = {
                    if (filter.selectedPerson != null) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = "Clear person filter",
                            modifier = Modifier.clickable { onPersonFilter(null) },
                        )
                    } else {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                    }
                },
            )
        }

        // ── Product filter chip ───────────────────────────────────────────
        item {
            FilterChip(
                selected = filter.selectedProduct != null,
                onClick = { showProductPicker = true },
                label = {
                    Text(filter.selectedProduct?.name ?: stringResource(R.string.filter_product))
                },
                trailingIcon = {
                    if (filter.selectedProduct != null) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = "Clear product filter",
                            modifier = Modifier.clickable { onProductFilter(null) },
                        )
                    } else {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                    }
                },
            )
        }

        // ── Clear all (only when any filter is active) ────────────────────
        if (filter.isActive) {
            item {
                TextButton(onClick = onClearAll) {
                    Text(stringResource(R.string.clear))
                }
            }
        }
    }

    // Person picker dialog
    if (showPersonPicker) {
        PickerDialog(
            title = stringResource(R.string.filter_person),
            items = persons,
            itemLabel = { it.name },
            onSelect = {
                onPersonFilter(it)
                showPersonPicker = false
            },
            onDismiss = { showPersonPicker = false },
        )
    }

    // Product picker dialog
    if (showProductPicker) {
        PickerDialog(
            title = stringResource(R.string.filter_product),
            items = products,
            itemLabel = { it.name },
            onSelect = {
                onProductFilter(it)
                showProductPicker = false
            },
            onDismiss = { showProductPicker = false },
        )
    }

    // Custom date range — two sequential date pickers
    if (showCustomStart) {
        DateSelectionDialog(
            initialDateMillis = filter.customStart ?: System.currentTimeMillis(),
            onDateSelected = { start ->
                onCustomRange(start, filter.customEnd)
                showCustomStart = false
                showCustomEnd = true
            },
            onDismiss = { showCustomStart = false },
        )
    }
    if (showCustomEnd) {
        DateSelectionDialog(
            initialDateMillis = filter.customEnd ?: System.currentTimeMillis(),
            onDateSelected = { end ->
                onCustomRange(filter.customStart, end)
                showCustomEnd = false
            },
            onDismiss = { showCustomEnd = false },
        )
    }
}

@Composable
private fun <T> PickerDialog(
    title: String,
    items: List<T>,
    itemLabel: (T) -> String,
    onSelect: (T) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            LazyColumn {
                items(items) { item ->
                    Text(
                        text = itemLabel(item),
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(item) }
                                .padding(vertical = 12.dp, horizontal = 4.dp),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
    )
}

// ---------------------------------------------------------------------------
// Bill list item
// ---------------------------------------------------------------------------

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
            formatDate(bill.billDate)
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
                    StatusBadge(bill.paymentStatus)
                }
                Spacer(Modifier.height(4.dp))
                Text(person.name, style = MaterialTheme.typography.bodyMedium)
                Text(dateStr, style = MaterialTheme.typography.bodySmall)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatMoney(bill.grandTotal, LocalCurrencyCode.current),
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
