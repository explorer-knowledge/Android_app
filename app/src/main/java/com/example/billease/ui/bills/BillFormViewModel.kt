package com.example.billease.ui.bills

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.billease.data.Bill
import com.example.billease.data.BillItem
import com.example.billease.data.BillingRepository
import com.example.billease.data.Person
import com.example.billease.data.Product
import com.example.billease.data.SettingsRepository
import com.example.billease.domain.BillCalculator
import com.example.billease.domain.BillItemInput
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/** One editable row in the bill form. */
data class LineItemFormState(
    val product: Product? = null,
    val quantityText: String = "1",
    val quantityError: String? = null,
    val productError: String? = null,
    val lineTotal: Double = 0.0,
) {
    val quantity: Double get() = quantityText.toDoubleOrNull() ?: 0.0
}

data class BillFormUiState(
    // 0 = new bill
    val billId: Long = 0L,
    val billNumber: String = "",
    val personId: Long = -1L,
    val selectedPerson: Person? = null,
    val discountText: String = "0",
    val notes: String = "",
    val billDateMillis: Long = System.currentTimeMillis(),
    val lineItems: List<LineItemFormState> = listOf(LineItemFormState()),
    val allPersons: List<Person> = emptyList(),
    val allProducts: List<Product> = emptyList(),
    // live totals
    val subtotal: Double = 0.0,
    val taxTotal: Double = 0.0,
    val grandTotal: Double = 0.0,
    // validation
    val personError: String? = null,
    val lineItemsError: String? = null,
    val isSaving: Boolean = false,
)

@HiltViewModel
@Suppress("TooManyFunctions")
class BillFormViewModel
    @Inject
    constructor(
        private val repository: BillingRepository,
        private val settingsRepository: SettingsRepository,
        savedStateHandle: SavedStateHandle,
    ) : ViewModel() {
        private val billId: Long = savedStateHandle.get<Long>("billId") ?: -1L
        private val isEditMode: Boolean = billId != -1L

        private val _uiState = MutableStateFlow(BillFormUiState())
        val uiState: StateFlow<BillFormUiState> = _uiState.asStateFlow()

        private val _isDirty = MutableStateFlow(false)
        val isDirty: StateFlow<Boolean> = _isDirty.asStateFlow()

        val allPersons: StateFlow<List<Person>> =
            repository.getAllPersons()
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

        val allProducts: StateFlow<List<Product>> =
            repository.getAllProducts()
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

        init {
            viewModelScope.launch {
                if (isEditMode) {
                    repository.getBillWithItemsById(billId).first()?.let { billData ->
                        val items =
                            billData.items.map { item ->
                                LineItemFormState(
                                    product =
                                        Product(
                                            id = item.productId,
                                            name = item.productNameSnapshot,
                                            unitPrice = item.unitPriceSnapshot,
                                            unit = "",
                                            taxPercent = item.taxPercentSnapshot,
                                        ),
                                    quantityText =
                                        item.quantity.let {
                                            if (it == it.toLong().toDouble()) {
                                                it.toLong().toString()
                                            } else {
                                                it.toString()
                                            }
                                        },
                                )
                            }
                        _uiState.update {
                            it.copy(
                                billId = billData.bill.id,
                                billNumber = billData.bill.billNumber,
                                personId = billData.bill.personId,
                                selectedPerson = billData.person,
                                discountText =
                                    billData.bill.discount.let { d ->
                                        if (d == d.toLong().toDouble()) d.toLong().toString() else d.toString()
                                    },
                                notes = billData.bill.notes ?: "",
                                billDateMillis = billData.bill.billDate,
                                lineItems = items,
                            )
                        }
                        recalculate()
                    }
                } else {
                    val settings = settingsRepository.appSettingsFlow.first()
                    _uiState.update { it.copy(billNumber = generateBillNumber(settings.invoicePrefix)) }
                }
            }
        }

        private fun generateBillNumber(prefix: String): String {
            val sdf = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault())
            return "${prefix}${sdf.format(Date())}"
        }

        fun selectPerson(person: Person) {
            _uiState.update { it.copy(selectedPerson = person, personId = person.id, personError = null) }
            _isDirty.value = true
        }

        fun updateDiscount(text: String) {
            _uiState.update { it.copy(discountText = text) }
            _isDirty.value = true
            recalculate()
        }

        fun updateNotes(notes: String) {
            _uiState.update { it.copy(notes = notes) }
            _isDirty.value = true
        }

        fun updateBillDate(millis: Long) {
            _uiState.update { it.copy(billDateMillis = millis) }
            _isDirty.value = true
        }

        fun updateLineItemProduct(
            index: Int,
            product: Product,
        ) {
            _uiState.update { state ->
                val items = state.lineItems.toMutableList()
                items[index] = items[index].copy(product = product, productError = null)
                state.copy(lineItems = items, lineItemsError = null)
            }
            _isDirty.value = true
            recalculate()
        }

        fun updateLineItemQuantity(
            index: Int,
            text: String,
        ) {
            _uiState.update { state ->
                val items = state.lineItems.toMutableList()
                items[index] = items[index].copy(quantityText = text, quantityError = null)
                state.copy(lineItems = items)
            }
            _isDirty.value = true
            recalculate()
        }

        fun addLineItem() {
            _uiState.update { it.copy(lineItems = it.lineItems + LineItemFormState()) }
            _isDirty.value = true
        }

        fun removeLineItem(index: Int) {
            if (_uiState.value.lineItems.size <= 1) return
            _uiState.update { state ->
                state.copy(lineItems = state.lineItems.toMutableList().also { it.removeAt(index) })
            }
            _isDirty.value = true
            recalculate()
        }

        private fun recalculate() {
            val state = _uiState.value
            val updatedLineItems =
                state.lineItems.map { row ->
                    val product = row.product
                    val qty = row.quantity
                    if (product != null && qty > 0) {
                        val input = BillItemInput.fromProduct(product, qty)
                        row.copy(lineTotal = input.lineTotal)
                    } else {
                        row.copy(lineTotal = 0.0)
                    }
                }
            val inputs =
                updatedLineItems.mapNotNull { row ->
                    val product = row.product ?: return@mapNotNull null
                    if (row.quantity <= 0) return@mapNotNull null
                    BillItemInput.fromProduct(product, row.quantity)
                }
            val discount = state.discountText.toDoubleOrNull() ?: 0.0
            val result = BillCalculator.calculate(inputs, discount)
            _uiState.update {
                it.copy(
                    lineItems = updatedLineItems,
                    subtotal = result.subtotal,
                    taxTotal = result.taxTotal,
                    grandTotal = result.grandTotal,
                )
            }
        }

        fun saveBill(onSuccess: () -> Unit) {
            val state = _uiState.value
            var valid = true

            if (state.selectedPerson == null) {
                _uiState.update { it.copy(personError = "Select a person") }
                valid = false
            }

            // Validate each line item
            val updatedItems =
                state.lineItems.mapIndexed { _, row ->
                    var r = row
                    if (r.product == null) r = r.copy(productError = "Select a product")
                    val qty = r.quantityText.toDoubleOrNull()
                    if (qty == null || qty <= 0) r = r.copy(quantityError = "Enter a valid quantity (> 0)")
                    r
                }
            _uiState.update { it.copy(lineItems = updatedItems) }

            if (updatedItems.any { it.productError != null || it.quantityError != null }) {
                _uiState.update { it.copy(lineItemsError = "Fix errors above") }
                valid = false
            }

            if (!valid) return

            _uiState.update { it.copy(isSaving = true) }
            viewModelScope.launch {
                try {
                    val person = state.selectedPerson!!
                    val discount = state.discountText.toDoubleOrNull() ?: 0.0
                    val billInputs =
                        updatedItems.map { row ->
                            BillItemInput.fromProduct(row.product!!, row.quantity)
                        }
                    val result = BillCalculator.calculate(billInputs, discount)
                    val now = System.currentTimeMillis()

                    val bill =
                        Bill(
                            id = if (isEditMode) billId else 0L,
                            billNumber = state.billNumber,
                            personId = person.id,
                            billDate = state.billDateMillis,
                            discount = discount,
                            notes = state.notes.takeIf { it.isNotBlank() },
                            subtotal = result.subtotal,
                            taxTotal = result.taxTotal,
                            grandTotal = result.grandTotal,
                            createdAt = if (isEditMode) now else now,
                            updatedAt = now,
                        )

                    val billItems =
                        billInputs.map { input ->
                            // DAO sets this via insertBillWithItems
                            BillItem(
                                billId = 0L,
                                productId = input.productId,
                                productNameSnapshot = input.productName,
                                unitPriceSnapshot = input.unitPrice,
                                taxPercentSnapshot = input.taxPercent,
                                quantity = input.quantity,
                                lineTotal = input.lineTotal,
                            )
                        }

                    if (isEditMode) {
                        repository.updateBillWithItems(bill, billItems)
                    } else {
                        repository.insertBillWithItems(bill, billItems)
                    }
                    onSuccess()
                } finally {
                    _uiState.update { it.copy(isSaving = false) }
                }
            }
        }
    }
