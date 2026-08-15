package com.example.billease.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.billease.data.BillWithPerson
import com.example.billease.data.BillingRepository
import com.example.billease.data.SettingsRepository
import com.example.billease.util.monthBounds
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@Suppress("MagicNumber")
@HiltViewModel
class HomeViewModel
    @Inject
    constructor(
        private val repository: BillingRepository,
        settingsRepository: SettingsRepository,
    ) : ViewModel() {
        private val thisMonth = monthBounds()

        private val _homeSearchQuery = MutableStateFlow("")
        val homeSearchQuery: StateFlow<String> = _homeSearchQuery

        fun onHomeSearchChange(query: String) {
            _homeSearchQuery.value = query
        }

        val customerCount: StateFlow<Int> =
            repository.getPersonCount()
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = 0,
                )

        val totalBills: StateFlow<Int> =
            repository.getTotalBillCount()
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = 0,
                )

        val revenueThisMonth: StateFlow<Double> =
            repository.getRevenueBetween(thisMonth.first, thisMonth.second)
                // Room returns null for SUM() on empty sets, so map it to 0.0
                .map { it ?: 0.0 }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = 0.0,
                )

        // Expose a limited list of recent bills for the dashboard
        @OptIn(ExperimentalCoroutinesApi::class)
        val recentBills: StateFlow<List<BillWithPerson>> =
            _homeSearchQuery
                .flatMapLatest { query ->
                    if (query.isBlank()) {
                        repository.getAllBills().map { it.take(5) }
                    } else {
                        repository.searchBills(query)
                    }
                }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = emptyList(),
                )

        val businessNameInitial: StateFlow<String> =
            settingsRepository.appSettingsFlow
                .map { settings ->
                    settings.businessName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "B"
                }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = "B",
                )
    }
