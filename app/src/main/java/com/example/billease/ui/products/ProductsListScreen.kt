package com.example.billease.ui.products

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.billease.data.Product
import com.example.billease.ui.components.ConfirmDeleteDialog
import com.example.billease.ui.components.DismissableSnackbar
import com.example.billease.ui.components.EmptyState
import com.example.billease.ui.components.ProfileIconButton
import com.example.billease.ui.components.SearchField
import com.example.billease.util.LocalCurrencyCode
import com.example.billease.util.formatMoney

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsListScreen(
    onNavigateToProductForm: (Long?) -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: ProductsViewModel = hiltViewModel(),
) {
    val products by viewModel.products.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var showDeleteDialog by remember { mutableStateOf<Product?>(null) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Products") },
                actions = { ProfileIconButton(onClick = onNavigateToSettings) },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onNavigateToProductForm(null) }) {
                Icon(Icons.Default.Add, contentDescription = "Add Product")
            }
        },
        snackbarHost = {
            snackbarMessage?.let {
                DismissableSnackbar(message = it, onDismiss = { snackbarMessage = null })
            }
        },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding),
        ) {
            SearchField(
                query = searchQuery,
                onQueryChange = viewModel::onSearchQueryChange,
                placeholder = "Search by name",
            )

            if (products.isEmpty()) {
                EmptyState(
                    icon = Icons.Outlined.ShoppingCart,
                    title = "No products found",
                    subtitle = "Tap + to add a new product.",
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(products) { product ->
                        ProductListItem(
                            product = product,
                            onClick = { onNavigateToProductForm(product.id) },
                            onDeleteClick = { showDeleteDialog = product },
                        )
                    }
                }
            }
        }
    }

    showDeleteDialog?.let { product ->
        ConfirmDeleteDialog(
            title = "Delete Product",
            message = "Are you sure you want to delete ${product.name}?",
            onConfirm = {
                viewModel.deleteProduct(product) { message ->
                    snackbarMessage = message
                    showDeleteDialog = null
                }
            },
            onDismiss = { showDeleteDialog = null },
        )
    }
}

@Composable
fun ProductListItem(
    product: Product,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(text = product.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "${formatMoney(product.unitPrice, LocalCurrencyCode.current)} / ${product.unit}",
                    style = MaterialTheme.typography.bodyMedium,
                )
                if (product.taxPercent > 0) {
                    Text(text = "Tax: ${product.taxPercent}%", style = MaterialTheme.typography.bodySmall)
                }
            }
            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Product")
            }
        }
    }
}
