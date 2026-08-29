package com.example.billease.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.billease.data.BillingRepository
import com.example.billease.data.MonthlyRevenueRow
import com.example.billease.data.ProductTotalRow
import com.example.billease.util.currentMonthBoundsFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ReportsViewModel
    @Inject
    constructor(
        private val repository: BillingRepository,
    ) : ViewModel() {
        val totalBillCount: StateFlow<Int> =
            repository.getTotalBillCount()
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000), 0)

        val totalRevenue: StateFlow<Double> =
            repository.getTotalRevenue()
                .map { it ?: 0.0 }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000), 0.0)

        val totalOutstanding: StateFlow<Double> =
            repository.getTotalOutstanding()
                .map { it ?: 0.0 }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000), 0.0)

        val monthlyRevenue: StateFlow<List<MonthlyRevenueRow>> =
            repository.getMonthlyRevenue()
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000), emptyList())

        val productTotals: StateFlow<List<ProductTotalRow>> =
            repository.getProductTotals()
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000), emptyList())

        @OptIn(ExperimentalCoroutinesApi::class)
        val billsThisMonth: StateFlow<Int> =
            currentMonthBoundsFlow()
                .flatMapLatest { bounds ->
                    repository.getBillCountBetween(bounds.first, bounds.second)
                }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000), 0)

        @OptIn(ExperimentalCoroutinesApi::class)
        val revenueThisMonth: StateFlow<Double> =
            currentMonthBoundsFlow()
                .flatMapLatest { bounds ->
                    repository.getRevenueBetween(bounds.first, bounds.second)
                }
                .map { it ?: 0.0 }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000), 0.0)

        @OptIn(ExperimentalCoroutinesApi::class)
        val outstandingThisMonth: StateFlow<Double> =
            currentMonthBoundsFlow()
                .flatMapLatest { bounds ->
                    repository.getOutstandingBetween(bounds.first, bounds.second)
                }
                .map { it ?: 0.0 }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000), 0.0)
    }
