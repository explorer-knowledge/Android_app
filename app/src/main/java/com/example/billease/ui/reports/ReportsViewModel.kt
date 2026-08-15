package com.example.billease.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.billease.data.BillingRepository
import com.example.billease.util.monthBounds
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ReportsViewModel
    @Inject
    constructor(
        private val repository: BillingRepository,
    ) : ViewModel() {
        private val thisMonth = monthBounds()

        val totalBillCount: StateFlow<Int> =
            repository.getTotalBillCount()
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000), 0)

        val totalRevenue: StateFlow<Double> =
            repository.getTotalRevenue()
                .map { it ?: 0.0 }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000), 0.0)

        val billsThisMonth: StateFlow<Int> =
            repository.getBillCountBetween(thisMonth.first, thisMonth.second)
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000), 0)

        val revenueThisMonth: StateFlow<Double> =
            repository.getRevenueBetween(thisMonth.first, thisMonth.second)
                .map { it ?: 0.0 }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000), 0.0)
    }
