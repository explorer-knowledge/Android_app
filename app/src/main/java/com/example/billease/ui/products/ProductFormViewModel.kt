package com.example.billease.ui.products

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.billease.data.BillingRepository
import com.example.billease.data.Product
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProductFormState(
    val name: String = "",
    val unitPrice: String = "",
    val unit: String = "",
    val taxPercent: String = "",
    val description: String = "",
    val nameError: String? = null,
    val unitPriceError: String? = null,
    val unitError: String? = null,
    val taxPercentError: String? = null
)

@HiltViewModel
class ProductFormViewModel @Inject constructor(
    private val repository: BillingRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val productId: Long = savedStateHandle.get<Long>("productId") ?: -1L
    private val _uiState = MutableStateFlow(ProductFormState())
    val uiState: StateFlow<ProductFormState> = _uiState.asStateFlow()

    private var isEditMode = productId != -1L

    init {
        if (isEditMode) {
            viewModelScope.launch {
                repository.getProductById(productId).collect { product ->
                    product?.let {
                        _uiState.value = ProductFormState(
                            name = it.name,
                            unitPrice = it.unitPrice.toString(),
                            unit = it.unit,
                            taxPercent = it.taxPercent.toString(),
                            description = it.description ?: ""
                        )
                    }
                }
            }
        }
    }

    fun updateName(name: String) {
        _uiState.update { it.copy(name = name, nameError = null) }
    }

    fun updateUnitPrice(price: String) {
        _uiState.update { it.copy(unitPrice = price, unitPriceError = null) }
    }

    fun updateUnit(unit: String) {
        _uiState.update { it.copy(unit = unit, unitError = null) }
    }

    fun updateTaxPercent(tax: String) {
        _uiState.update { it.copy(taxPercent = tax, taxPercentError = null) }
    }

    fun updateDescription(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun saveProduct(onSuccess: () -> Unit) {
        val currentState = _uiState.value
        var hasError = false

        if (currentState.name.isBlank()) {
            _uiState.update { it.copy(nameError = "Name cannot be empty") }
            hasError = true
        }
        
        val price = currentState.unitPrice.toDoubleOrNull()
        if (price == null || price <= 0) {
            _uiState.update { it.copy(unitPriceError = "Enter a valid positive price") }
            hasError = true
        }

        if (currentState.unit.isBlank()) {
            _uiState.update { it.copy(unitError = "Unit cannot be empty") }
            hasError = true
        }
        
        val tax = if (currentState.taxPercent.isBlank()) 0.0 else currentState.taxPercent.toDoubleOrNull()
        if (tax == null || tax < 0) {
            _uiState.update { it.copy(taxPercentError = "Enter a valid tax percentage") }
            hasError = true
        }

        if (hasError) return

        viewModelScope.launch {
            val product = Product(
                id = if (isEditMode) productId else 0L,
                name = currentState.name,
                unitPrice = price!!,
                unit = currentState.unit,
                taxPercent = tax!!,
                description = currentState.description.takeIf { it.isNotBlank() }
            )

            if (isEditMode) {
                repository.updateProduct(product)
            } else {
                repository.insertProduct(product)
            }
            onSuccess()
        }
    }
}
