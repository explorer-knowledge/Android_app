package com.example.billease.ui.bills

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.billease.data.Bill
import com.example.billease.data.BillWithPerson
import com.example.billease.data.BillingRepository
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
class BillsViewModel @Inject constructor(
    private val repository: BillingRepository,
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    @OptIn(ExperimentalCoroutinesApi::class)
    val bills: StateFlow<List<BillWithPerson>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) repository.getAllBills()
            else repository.searchBills(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun deleteBill(bill: Bill, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                repository.deleteBill(bill)
                onResult(true, "Bill deleted.")
            } catch (e: Exception) {
                onResult(false, "Could not delete bill: ${e.message}")
            }
        }
    }
}
