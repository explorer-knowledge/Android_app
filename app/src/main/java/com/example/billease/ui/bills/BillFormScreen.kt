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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.billease.R
import com.example.billease.data.BillStatus
import com.example.billease.data.Person
import com.example.billease.data.Product
import com.example.billease.ui.components.DateSelectionDialog
import com.example.billease.ui.components.DetailTopAppBar
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

    var showDiscardDialog by rememberSaveable { mutableStateOf(false) }

    BackHandler(enabled = isDirty) {
        showDiscardDialog = true
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text(stringResource(R.string.discard_changes_title)) },
            text = { Text(stringResource(R.string.discard_changes_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDiscardDialog = false
                        onNavigateBack()
                    },
                ) {
                    Text(stringResource(R.string.discard), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text(stringResource(R.string.cancel))
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
            DetailTopAppBar(
                title = stringResource(if (uiState.billId == 0L) R.string.new_bill else R.string.edit_bill),
                onNavigateBack = handleBackNavigation,
            ) {
                ProfileIconButton(onClick = onNavigateToSettings)
            }
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
                    label = { Text(stringResource(R.string.bill_number)) },
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
                    onQuickAddPerson = viewModel::quickAddPerson,
                )
            }

            // Line items header
            item {
                Text(stringResource(R.string.line_items), style = MaterialTheme.typography.titleMedium)
                uiState.lineItemsError?.let { error ->
                    Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
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
                ) { Text(stringResource(R.string.add_line_item)) }
            }

            item { HorizontalDivider() }

            item {
                // Discount
                OutlinedTextField(
                    value = uiState.discountText,
                    onValueChange = viewModel::updateDiscount,
                    label = { Text(stringResource(R.string.discount)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = uiState.discountError != null,
                    supportingText = uiState.discountError?.let { { Text(it) } },
                    singleLine = true,
                )
            }

            item {
                // Notes
                OutlinedTextField(
                    value = uiState.notes,
                    onValueChange = viewModel::updateNotes,
                    label = { Text(stringResource(R.string.notes_optional)) },
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

            uiState.saveError?.let { error ->
                item {
                    Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }

            item {
                Button(
                    onClick = { viewModel.saveBill(onSuccess = onNavigateBack) },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                    enabled = !uiState.isSaving,
                ) { Text(stringResource(R.string.save_bill)) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
        label = { Text(stringResource(R.string.bill_date)) },
        modifier = Modifier.fillMaxWidth(),
        readOnly = true,
        trailingIcon = {
            TextButton(onClick = { showPicker = true }) { Text(stringResource(R.string.change)) }
        },
    )

    if (showPicker) {
        DateSelectionDialog(
            initialDateMillis = currentMillis,
            onDateSelected = onDateSelected,
            onDismiss = { showPicker = false },
        )
    }
}

@Composable
private fun PersonDropdown(
    selectedPerson: Person?,
    allPersons: List<Person>,
    error: String?,
    onPersonSelected: (Person) -> Unit,
    onQuickAddPerson: (String, String, (Boolean) -> Unit) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedTextField(
            value = selectedPerson?.name ?: "",
            onValueChange = {},
            label = { Text(stringResource(R.string.bill_to_person)) },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            isError = error != null,
            supportingText = error?.let { { Text(it) } },
            trailingIcon = {
                TextButton(onClick = { expanded = true }) { Text(stringResource(R.string.select)) }
            },
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            QuickAddPerson(
                allPersons = allPersons,
                onPersonSelected = { person ->
                    onPersonSelected(person)
                    expanded = false
                },
                onQuickAddPerson = onQuickAddPerson,
            )
        }
    }
}

@Composable
private fun QuickAddPerson(
    allPersons: List<Person>,
    onPersonSelected: (Person) -> Unit,
    onQuickAddPerson: (String, String, (Boolean) -> Unit) -> Unit,
) {
    var showForm by remember { mutableStateOf(false) }
    var name by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var nameError by rememberSaveable { mutableStateOf(false) }
    var phoneError by rememberSaveable { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    if (showForm) {
        PersonQuickAddForm(
            name = name,
            phone = phone,
            nameError = nameError,
            phoneError = phoneError,
            isSaving = isSaving,
            onNameChange = {
                name = it
                nameError = false
            },
            onPhoneChange = {
                phone = it
                phoneError = false
            },
            onCancel = { showForm = false },
            onSave = {
                val nameBlank = name.isBlank()
                val phoneBlank = phone.isBlank()
                nameError = nameBlank
                phoneError = phoneBlank
                if (!nameBlank && !phoneBlank) {
                    isSaving = true
                    onQuickAddPerson(name, phone) { success ->
                        isSaving = false
                        if (success) {
                            showForm = false
                            name = ""
                            phone = ""
                        }
                    }
                }
            },
        )
    } else {
        PersonQuickAddList(
            allPersons = allPersons,
            onPersonSelected = onPersonSelected,
            onStartNew = { showForm = true },
        )
    }
}

@Composable
@Suppress("LongParameterList")
private fun PersonQuickAddForm(
    name: String,
    phone: String,
    nameError: Boolean,
    phoneError: Boolean,
    isSaving: Boolean,
    onNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onCancel: () -> Unit,
    onSave: () -> Unit,
) {
    OutlinedTextField(
        value = name,
        onValueChange = onNameChange,
        label = { Text(stringResource(R.string.name)) },
        isError = nameError,
        singleLine = true,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
    )
    if (nameError) {
        Text(
            stringResource(R.string.error_name_required),
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
        )
    }
    OutlinedTextField(
        value = phone,
        onValueChange = onPhoneChange,
        label = { Text(stringResource(R.string.phone)) },
        isError = phoneError,
        singleLine = true,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
    )
    if (phoneError) {
        Text(
            stringResource(R.string.error_phone_required),
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
        )
    }
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedButton(
            onClick = onCancel,
            enabled = !isSaving,
            modifier = Modifier.weight(1f),
        ) {
            Text(stringResource(R.string.cancel))
        }
        Button(
            onClick = onSave,
            enabled = !isSaving,
            modifier = Modifier.weight(1f),
        ) {
            Text(if (isSaving) stringResource(R.string.saving) else stringResource(R.string.save))
        }
    }
}

@Composable
private fun PersonQuickAddList(
    allPersons: List<Person>,
    onPersonSelected: (Person) -> Unit,
    onStartNew: () -> Unit,
) {
    DropdownMenuItem(
        text = { Text(stringResource(R.string.new_person)) },
        onClick = onStartNew,
    )
    if (allPersons.isEmpty()) {
        DropdownMenuItem(text = { Text(stringResource(R.string.no_persons_yet)) }, onClick = {})
    }
    allPersons.forEach { person ->
        DropdownMenuItem(
            text = { Text(stringResource(R.string.person_name_phone, person.name, person.phone)) },
            onClick = {
                onPersonSelected(person)
            },
        )
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
                        label = { Text(stringResource(R.string.product)) },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        isError = row.productError != null,
                        supportingText = row.productError?.let { { Text(it) } },
                        trailingIcon = {
                            TextButton(onClick = { productExpanded = true }) { Text(stringResource(R.string.pick)) }
                        },
                    )
                    DropdownMenu(expanded = productExpanded, onDismissRequest = { productExpanded = false }) {
                        if (allProducts.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.no_products_yet)) },
                                onClick = { productExpanded = false },
                            )
                        }
                        allProducts.forEach { product ->
                            val priceLabel = formatMoney(product.unitPrice, LocalCurrencyCode.current)
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        stringResource(
                                            R.string.product_with_price,
                                            product.name,
                                            priceLabel,
                                            product.unit,
                                        ),
                                    )
                                },
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
                    label = { Text(stringResource(R.string.qty)) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = row.quantityError != null,
                    supportingText = row.quantityError?.let { { Text(it) } },
                    singleLine = true,
                )
                // Live line total
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                    Text(stringResource(R.string.line_total), style = MaterialTheme.typography.labelSmall)
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
                Text(stringResource(R.string.subtotal), style = MaterialTheme.typography.bodyMedium)
                Text(formatMoney(subtotal, LocalCurrencyCode.current), style = MaterialTheme.typography.bodyMedium)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.tax), style = MaterialTheme.typography.bodyMedium)
                Text(formatMoney(taxTotal, LocalCurrencyCode.current), style = MaterialTheme.typography.bodyMedium)
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.grand_total), style = MaterialTheme.typography.titleMedium)
                Text(
                    formatMoney(grandTotal, LocalCurrencyCode.current),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun PaymentStatusPicker(
    currentStatus: BillStatus,
    onStatusSelected: (BillStatus) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val statuses = BillStatus.entries

    Box {
        OutlinedTextField(
            value = currentStatus.name,
            onValueChange = {},
            label = { Text(stringResource(R.string.payment_status)) },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            trailingIcon = {
                TextButton(onClick = { expanded = true }) { Text(stringResource(R.string.change)) }
            },
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            statuses.forEach { status ->
                DropdownMenuItem(
                    text = { Text(status.name) },
                    onClick = {
                        onStatusSelected(status)
                        expanded = false
                    },
                )
            }
        }
    }
}
