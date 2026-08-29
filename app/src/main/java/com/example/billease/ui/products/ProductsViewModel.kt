package com.example.billease.ui.products

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.billease.R
import com.example.billease.data.BillDao
import com.example.billease.data.Product
import com.example.billease.data.ProductDao
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
        private val productDao: ProductDao,
        private val billDao: BillDao,
        @ApplicationContext private val context: Context,
    ) : ViewModel() {
        private val _searchQuery = MutableStateFlow("")
        val searchQuery: StateFlow<String> = _searchQuery

        @OptIn(ExperimentalCoroutinesApi::class)
        val products =
            _searchQuery.flatMapLatest { query ->
                if (query.isBlank()) {
                    productDao.getAll()
                } else {
                    productDao.search(query)
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                initialValue = emptyList(),
            )

        fun onSearchQueryChange(query: String) {
            _searchQuery.value = query
        }

        // Decision: Prevent deletion if product is referenced in existing bills.
        fun deleteProduct(
            product: Product,
            onResult: (String) -> Unit,
        ) {
            viewModelScope.launch {
                try {
                    val count = billDao.getBillItemCountForProduct(product.id)
                    if (count > 0) {
                        onResult(context.getString(R.string.error_cannot_delete_product_used))
                    } else {
                        productDao.delete(product)
                        onResult(context.getString(R.string.msg_product_deleted))
                    }
                } catch (e: Exception) {
                    onResult(context.getString(R.string.error_could_not_delete_product, e.message))
                }
            }
        }
    }
