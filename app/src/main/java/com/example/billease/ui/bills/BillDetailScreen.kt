package com.example.billease.ui.bills

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.billease.data.BillItem
import com.example.billease.ui.components.ProfileIconButton
import com.example.billease.util.LocalCurrencyCode
import com.example.billease.util.formatMoney
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: BillDetailViewModel = hiltViewModel(),
) {
    val billData by viewModel.bill.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Bill") },
            text = { Text("Are you sure you want to delete this bill? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteBill { success ->
                            if (success) {
                                onNavigateBack()
                            }
                        }
                    },
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(billData?.bill?.billNumber ?: "Bill Detail") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    billData?.let { data ->
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Bill",
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                        IconButton(onClick = {
                            coroutineScope.launch {
                                val uri = viewModel.generatePdf(context)
                                if (uri != null) {
                                    val intent =
                                        Intent(Intent.ACTION_SEND).apply {
                                            type = "application/pdf"
                                            putExtra(Intent.EXTRA_STREAM, uri)
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                    context.startActivity(Intent.createChooser(intent, "Share Bill"))
                                } else {
                                    snackbarHostState.showSnackbar("Failed to generate PDF")
                                }
                            }
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "Share PDF")
                        }
                        IconButton(onClick = { onNavigateToEdit(data.bill.id) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                    }
                    ProfileIconButton(onClick = onNavigateToSettings)
                },
            )
        },
    ) { padding ->
        val data = billData
        if (data == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val bill = data.bill
        val person = data.person
        val items = data.items

        val dateStr =
            remember(bill.billDate) {
                SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date(bill.billDate))
            }

        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Header card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(bill.billNumber, style = MaterialTheme.typography.headlineSmall)
                        Text(dateStr, style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(4.dp))
                        Text("Bill To", style = MaterialTheme.typography.labelMedium)
                        Text(person.name, style = MaterialTheme.typography.bodyLarge)
                        Text(person.phone, style = MaterialTheme.typography.bodyMedium)
                        if (!person.email.isNullOrBlank()) {
                            Text(person.email, style = MaterialTheme.typography.bodyMedium)
                        }
                        if (!person.address.isNullOrBlank()) {
                            Text(person.address, style = MaterialTheme.typography.bodyMedium)
                        }
                        person.gstNumber?.let { Text("GST: $it", style = MaterialTheme.typography.bodySmall) }
                    }
                }
            }

            // Line items
            item {
                Text("Items", style = MaterialTheme.typography.titleMedium)
            }

            items(items) { item ->
                LineItemDetailRow(item)
            }

            // Totals
            item {
                HorizontalDivider()
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        DetailRow("Subtotal", formatMoney(bill.subtotal, LocalCurrencyCode.current))
                        DetailRow("Tax", formatMoney(bill.taxTotal, LocalCurrencyCode.current))
                        if (bill.discount > 0) {
                            DetailRow("Discount", "- ${formatMoney(bill.discount, LocalCurrencyCode.current)}")
                        }
                        DetailRow("Status", bill.paymentStatus.name)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text("Grand Total", style = MaterialTheme.typography.titleMedium)
                            Text(
                                formatMoney(bill.grandTotal, LocalCurrencyCode.current),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }

            // Notes
            bill.notes?.let { notes ->
                item {
                    Text("Notes", style = MaterialTheme.typography.labelMedium)
                    Text(notes, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun LineItemDetailRow(item: BillItem) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(item.productNameSnapshot, style = MaterialTheme.typography.bodyMedium)
            Text(
                "${item.quantity} ${item.unitSnapshot} × " +
                    formatMoney(item.unitPriceSnapshot, LocalCurrencyCode.current) +
                    if (item.taxPercentSnapshot > 0) " + ${item.taxPercentSnapshot}% tax" else "",
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Text(
            formatMoney(item.lineTotal, LocalCurrencyCode.current),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}
