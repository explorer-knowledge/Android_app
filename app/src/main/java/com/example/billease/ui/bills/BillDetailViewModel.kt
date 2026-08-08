package com.example.billease.ui.bills

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.billease.data.BillWithItemsAndPerson
import com.example.billease.data.BillingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class BillDetailViewModel @Inject constructor(
    repository: BillingRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val billId: Long = checkNotNull(savedStateHandle.get<Long>("billId"))

    val bill: StateFlow<BillWithItemsAndPerson?> = repository.getBillWithItemsById(billId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
