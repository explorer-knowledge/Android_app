package com.example.billease.ui.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.billease.data.BillingRepository
import com.example.billease.data.Product
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductsViewModel
    @Inject
    constructor(
        private val repository: BillingRepository,
    ) : ViewModel() {
        private val _searchQuery = MutableStateFlow("")
        val searchQuery: StateFlow<String> = _searchQuery

        @OptIn(ExperimentalCoroutinesApi::class)
        val products =
            _searchQuery.flatMapLatest { query ->
                if (query.isBlank()) {
                    repository.getAllProducts()
                } else {
                    repository.searchProducts(query)
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList(),
            )

        fun onSearchQueryChange(query: String) {
            _searchQuery.value = query
        }

        // Decision: Prevent deletion if product is referenced in existing bills.
        fun deleteProduct(
            product: Product,
            onResult: (Boolean, String) -> Unit,
        ) {
            viewModelScope.launch {
                val count = repository.getBillItemCountForProduct(product.id)
                if (count > 0) {
                    onResult(false, "Cannot delete product used in existing bills.")
                } else {
                    repository.deleteProduct(product)
                    onResult(true, "Product deleted successfully.")
                }
            }
        }
    }
