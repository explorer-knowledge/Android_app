package com.example.billease.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.billease.data.BillDao
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
        private val billDao: BillDao,
    ) : ViewModel() {
        val totalBillCount: StateFlow<Int> =
            billDao.getTotalBillCount()
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000), 0)

        val totalRevenue: StateFlow<Double> =
            billDao.getTotalRevenue()
                .map { it ?: 0.0 }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000), 0.0)

        val totalOutstanding: StateFlow<Double> =
            billDao.getTotalOutstanding()
                .map { it ?: 0.0 }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000), 0.0)

        val monthlyRevenue: StateFlow<List<MonthlyRevenueRow>> =
            billDao.getMonthlyRevenue()
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000), emptyList())

        val productTotals: StateFlow<List<ProductTotalRow>> =
            billDao.getProductTotals()
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000), emptyList())

        @OptIn(ExperimentalCoroutinesApi::class)
        val billsThisMonth: StateFlow<Int> =
            currentMonthBoundsFlow()
                .flatMapLatest { bounds ->
                    billDao.getBillCountBetween(bounds.first, bounds.second)
                }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000), 0)

        @OptIn(ExperimentalCoroutinesApi::class)
        val revenueThisMonth: StateFlow<Double> =
            currentMonthBoundsFlow()
                .flatMapLatest { bounds ->
                    billDao.getRevenueBetween(bounds.first, bounds.second)
                }
                .map { it ?: 0.0 }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000), 0.0)

        @OptIn(ExperimentalCoroutinesApi::class)
        val outstandingThisMonth: StateFlow<Double> =
            currentMonthBoundsFlow()
                .flatMapLatest { bounds ->
                    billDao.getOutstandingBetween(bounds.first, bounds.second)
                }
                .map { it ?: 0.0 }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000), 0.0)
    }
