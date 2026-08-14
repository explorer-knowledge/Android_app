package com.example.billease.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.billease.data.BillingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar
import javax.inject.Inject

@Suppress("MagicNumber")
@HiltViewModel
class ReportsViewModel
    @Inject
    constructor(
        private val repository: BillingRepository,
    ) : ViewModel() {
        private val monthBounds = getMonthBounds()

        val totalBillCount: StateFlow<Int> =
            repository.getTotalBillCount()
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

        val totalRevenue: StateFlow<Double> =
            repository.getTotalRevenue()
                .map { it ?: 0.0 }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

        val billsThisMonth: StateFlow<Int> =
            repository.getBillCountBetween(monthBounds.first, monthBounds.second)
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

        val revenueThisMonth: StateFlow<Double> =
            repository.getRevenueBetween(monthBounds.first, monthBounds.second)
                .map { it ?: 0.0 }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

        private fun getMonthBounds(): Pair<Long, Long> {
            val start =
                Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
            val end =
                Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                    add(Calendar.MONTH, 1)
                    add(Calendar.MILLISECOND, -1)
                }.timeInMillis
            return Pair(start, end)
        }
    }
