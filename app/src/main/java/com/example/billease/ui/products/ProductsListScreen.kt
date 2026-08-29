package com.example.billease.ui.products

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.billease.R
import com.example.billease.data.Product
import com.example.billease.ui.components.ConfirmDeleteDialog
import com.example.billease.ui.components.DismissableSnackbar
import com.example.billease.ui.components.EmptyState
import com.example.billease.ui.components.ListItemCard
import com.example.billease.ui.components.ScreenHeader
import com.example.billease.util.LocalCurrencyCode
import com.example.billease.util.formatMoney
import com.example.billease.util.formatPercent

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
            ScreenHeader(
                heading = stringResource(R.string.products_heading),
                query = searchQuery,
                onQueryChange = viewModel::onSearchQueryChange,
                onNavigateToSettings = onNavigateToSettings,
            )

            if (products.isEmpty()) {
                EmptyState(
                    icon = Icons.Outlined.ShoppingCart,
                    title =
                        if (searchQuery.isNotBlank()) {
                            stringResource(R.string.no_products_match, searchQuery)
                        } else {
                            stringResource(R.string.no_products_found)
                        },
                    subtitle =
                        if (searchQuery.isNotBlank()) {
                            stringResource(R.string.try_different_search)
                        } else {
                            stringResource(R.string.tap_to_add_product)
                        },
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
            title = stringResource(R.string.delete_product_title),
            message = stringResource(R.string.confirm_delete_product, product.name),
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
    ListItemCard(
        onClick = onClick,
        onDeleteClick = onDeleteClick,
        deleteContentDescription = "Delete Product",
    ) {
        Text(text = product.name, style = MaterialTheme.typography.titleMedium)
        Text(
            text =
                stringResource(
                    R.string.product_price_per_unit,
                    formatMoney(product.unitPrice, LocalCurrencyCode.current),
                    product.unit,
                ),
            style = MaterialTheme.typography.bodyMedium,
        )
        if (product.taxPercent > 0) {
            Text(
                text = stringResource(R.string.tax_label_value, formatPercent(product.taxPercent)),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
