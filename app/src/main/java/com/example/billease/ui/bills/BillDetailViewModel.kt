package com.example.billease.ui.bills

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.billease.data.BillWithItemsAndPerson
import com.example.billease.data.BillingRepository
import com.example.billease.data.SettingsRepository
import com.example.billease.util.PdfGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class BillDetailViewModel
    @Inject
    constructor(
        repository: BillingRepository,
        private val settingsRepository: SettingsRepository,
        savedStateHandle: SavedStateHandle,
    ) : ViewModel() {
        private val billId: Long = checkNotNull(savedStateHandle.get<Long>("billId"))

        val bill: StateFlow<BillWithItemsAndPerson?> =
            repository.getBillWithItemsById(billId)
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

        suspend fun generatePdf(context: Context): Uri? {
            val currentBill = bill.value ?: return null
            val currentSettings = settingsRepository.appSettingsFlow.first()
            return withContext(Dispatchers.IO) {
                PdfGenerator.generatePdf(context, currentBill, currentSettings)
            }
        }
    }
