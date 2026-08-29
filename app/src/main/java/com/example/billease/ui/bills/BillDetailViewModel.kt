package com.example.billease.ui.bills

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.billease.data.BillDao
import com.example.billease.data.BillWithItemsAndPerson
import com.example.billease.data.SettingsRepository
import com.example.billease.util.PdfGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class BillDetailViewModel
    @Inject
    constructor(
        private val billDao: BillDao,
        private val settingsRepository: SettingsRepository,
        savedStateHandle: SavedStateHandle,
    ) : ViewModel() {
        private val billId: Long = checkNotNull(savedStateHandle.get<Long>("billId"))

        val bill: StateFlow<BillWithItemsAndPerson?> =
            billDao.getBillWithItemsAndPersonById(billId)
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000), null)

        suspend fun generatePdf(context: Context): Uri? {
            val currentBill = bill.value ?: return null
            val currentSettings = settingsRepository.appSettingsFlow.first()
            return withContext(Dispatchers.IO) {
                PdfGenerator.generatePdf(context, currentBill, currentSettings)
            }
        }

        @Suppress("TooGenericExceptionCaught", "SwallowedException")
        fun deleteBill(onResult: (Boolean) -> Unit) {
            val currentBill = bill.value?.bill ?: return
            viewModelScope.launch {
                try {
                    billDao.deleteBill(currentBill)
                    onResult(true)
                } catch (e: Exception) {
                    onResult(false)
                }
            }
        }
    }
