package com.example.billease.ui.bills

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.billease.data.Person
import com.example.billease.data.Product
import com.example.billease.ui.components.ProfileIconButton
import com.example.billease.util.LocalCurrencyCode
import com.example.billease.util.formatDate
import com.example.billease.util.formatMoney

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillFormScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: BillFormViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val allPersons by viewModel.allPersons.collectAsState()
    val allProducts by viewModel.allProducts.collectAsState()
    val isDirty by viewModel.isDirty.collectAsState()

    var showDiscardDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = isDirty) {
        showDiscardDialog = true
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("Discard changes?") },
            text = { Text("You have unsaved changes. Go back and lose them?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDiscardDialog = false
                        onNavigateBack()
                    },
                ) {
                    Text("Discard", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }

    val handleBackNavigation = {
        if (isDirty) {
            showDiscardDialog = true
        } else {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.billId == 0L) "New Bill" else "Edit Bill") },
                navigationIcon = {
                    IconButton(onClick = handleBackNavigation) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = { ProfileIconButton(onClick = onNavigateToSettings) },
            )
        },
    ) { padding ->
        if (uiState.isSaving) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Spacer(Modifier.height(4.dp))
                // Bill number (read-only)
                OutlinedTextField(
                    value = uiState.billNumber,
                    onValueChange = {},
                    label = { Text("Bill Number") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    singleLine = true,
                )
            }

            item {
                BillDateField(
                    currentMillis = uiState.billDateMillis,
                    onDateSelected = viewModel::updateBillDate,
                )
            }

            item {
                // Person picker
                PersonDropdown(
                    selectedPerson = uiState.selectedPerson,
                    allPersons = allPersons,
                    error = uiState.personError,
                    onPersonSelected = viewModel::selectPerson,
                )
            }

            // Line items header
            item {
                Text("Line Items", style = MaterialTheme.typography.titleMedium)
                if (uiState.lineItemsError != null) {
                    Text(uiState.lineItemsError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }

            itemsIndexed(uiState.lineItems) { index, row ->
                LineItemRow(
                    row = row,
                    allProducts = allProducts,
                    onProductSelected = { viewModel.updateLineItemProduct(index, it) },
                    onQuantityChange = { viewModel.updateLineItemQuantity(index, it) },
                    onRemove = { viewModel.removeLineItem(index) },
                    canRemove = uiState.lineItems.size > 1,
                )
            }

            item {
                OutlinedButton(
                    onClick = viewModel::addLineItem,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("+ Add Line Item") }
            }

            item { HorizontalDivider() }

            item {
                // Discount
                OutlinedTextField(
                    value = uiState.discountText,
                    onValueChange = viewModel::updateDiscount,
                    label = { Text("Discount (₹)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                )
            }

            item {
                // Notes
                OutlinedTextField(
                    value = uiState.notes,
                    onValueChange = viewModel::updateNotes,
                    label = { Text("Notes (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                )
            }

            item {
                PaymentStatusPicker(
                    currentStatus = uiState.paymentStatus,
                    onStatusSelected = viewModel::updatePaymentStatus,
                )
            }

            item {
                // Totals summary
                TotalsSummaryCard(
                    subtotal = uiState.subtotal,
                    taxTotal = uiState.taxTotal,
                    grandTotal = uiState.grandTotal,
                )
            }

            item {
                Button(
                    onClick = { viewModel.saveBill(onSuccess = onNavigateBack) },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                    enabled = !uiState.isSaving,
                ) { Text("Save Bill") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("FunctionNaming")
@Composable
private fun BillDateField(
    currentMillis: Long,
    onDateSelected: (Long) -> Unit,
) {
    var showPicker by remember { mutableStateOf(false) }
    val dateStr =
        remember(currentMillis) {
            formatDate(currentMillis)
        }

    OutlinedTextField(
        value = dateStr,
        onValueChange = {},
        label = { Text("Bill Date") },
        modifier = Modifier.fillMaxWidth(),
        readOnly = true,
        trailingIcon = {
            TextButton(onClick = { showPicker = true }) { Text("Change") }
        },
    )

    if (showPicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = currentMillis)
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { onDateSelected(it) }
                    showPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Cancel") }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Suppress("FunctionNaming")
@Composable
private fun PersonDropdown(
    selectedPerson: Person?,
    allPersons: List<Person>,
    error: String?,
    onPersonSelected: (Person) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedTextField(
            value = selectedPerson?.name ?: "",
            onValueChange = {},
            label = { Text("Bill To (Person)") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            isError = error != null,
            supportingText = error?.let { { Text(it) } },
            trailingIcon = {
                TextButton(onClick = { expanded = true }) { Text("Select") }
            },
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            if (allPersons.isEmpty()) {
                DropdownMenuItem(text = { Text("No persons — add one first") }, onClick = { expanded = false })
            }
            allPersons.forEach { person ->
                DropdownMenuItem(
                    text = { Text("${person.name} · ${person.phone}") },
                    onClick = {
                        onPersonSelected(person)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun LineItemRow(
    row: LineItemFormState,
    allProducts: List<Product>,
    onProductSelected: (Product) -> Unit,
    onQuantityChange: (String) -> Unit,
    onRemove: () -> Unit,
    canRemove: Boolean,
) {
    var productExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Product picker
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = row.product?.name ?: "",
                        onValueChange = {},
                        label = { Text("Product") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        isError = row.productError != null,
                        supportingText = row.productError?.let { { Text(it) } },
                        trailingIcon = {
                            TextButton(onClick = { productExpanded = true }) { Text("Pick") }
                        },
                    )
                    DropdownMenu(expanded = productExpanded, onDismissRequest = { productExpanded = false }) {
                        if (allProducts.isEmpty()) {
                            DropdownMenuItem(text = { Text("No products — add one first") }, onClick = { productExpanded = false })
                        }
                        allProducts.forEach { product ->
                            DropdownMenuItem(
                                text = { Text("${product.name} · ₹${product.unitPrice}/${product.unit}") },
                                onClick = {
                                    onProductSelected(product)
                                    productExpanded = false
                                },
                            )
                        }
                    }
                }
                if (canRemove) {
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = onRemove) {
                        Icon(Icons.Default.Delete, contentDescription = "Remove line")
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = row.quantityText,
                    onValueChange = onQuantityChange,
                    label = { Text("Qty") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = row.quantityError != null,
                    supportingText = row.quantityError?.let { { Text(it) } },
                    singleLine = true,
                )
                // Live line total
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                    Text("Line Total", style = MaterialTheme.typography.labelSmall)
                    Text(
                        formatMoney(row.lineTotal, LocalCurrencyCode.current),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun TotalsSummaryCard(
    subtotal: Double,
    taxTotal: Double,
    grandTotal: Double,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Subtotal", style = MaterialTheme.typography.bodyMedium)
                Text(formatMoney(subtotal, LocalCurrencyCode.current), style = MaterialTheme.typography.bodyMedium)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Tax", style = MaterialTheme.typography.bodyMedium)
                Text(formatMoney(taxTotal, LocalCurrencyCode.current), style = MaterialTheme.typography.bodyMedium)
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Grand Total", style = MaterialTheme.typography.titleMedium)
                Text(
                    formatMoney(grandTotal, LocalCurrencyCode.current),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Suppress("FunctionNaming")
@Composable
private fun PaymentStatusPicker(
    currentStatus: String,
    onStatusSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val statuses = listOf("PENDING", "PAID", "OVERDUE")

    Box {
        OutlinedTextField(
            value = currentStatus,
            onValueChange = {},
            label = { Text("Payment Status") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            trailingIcon = {
                TextButton(onClick = { expanded = true }) { Text("Change") }
            },
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            statuses.forEach { status ->
                DropdownMenuItem(
                    text = { Text(status) },
                    onClick = {
                        onStatusSelected(status)
                        expanded = false
                    },
                )
            }
        }
    }
}
