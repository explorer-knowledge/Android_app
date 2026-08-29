package com.example.billease.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.billease.data.BillDao
import com.example.billease.data.BillWithPerson
import com.example.billease.data.DashboardTimeline
import com.example.billease.data.PersonDao
import com.example.billease.data.SettingsRepository
import com.example.billease.util.monthBounds
import com.example.billease.util.thisWeekBounds
import com.example.billease.util.thisYearBounds
import com.example.billease.util.todayBounds
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

private const val RECENT_BILL_LIMIT = 5

@HiltViewModel
class HomeViewModel
    @Inject
    constructor(
        private val personDao: PersonDao,
        private val billDao: BillDao,
        settingsRepository: SettingsRepository,
    ) : ViewModel() {
        private val _homeSearchQuery = MutableStateFlow("")
        val homeSearchQuery: StateFlow<String> = _homeSearchQuery

        fun onHomeSearchChange(query: String) {
            _homeSearchQuery.value = query
        }

        val customerCount: StateFlow<Int> =
            personDao.getCount()
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                    initialValue = 0,
                )

        val totalBills: StateFlow<Int> =
            billDao.getTotalBillCount()
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                    initialValue = 0,
                )

        /** Emits the active settings so the UI can drive timeline-aware queries. */
        private val settingsFlow =
            settingsRepository.appSettingsFlow
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                    initialValue = com.example.billease.data.AppSettings("", "", null),
                )

        /** The label shown in the Hero card — reflects the currently selected timeline. */
        val dashboardTimelineLabel: StateFlow<DashboardTimeline> =
            settingsFlow
                .map { it.dashboardTimeline }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                    initialValue = DashboardTimeline.THIS_MONTH,
                )

        @OptIn(ExperimentalCoroutinesApi::class)
        val revenueThisMonth: StateFlow<Double> =
            settingsFlow
                .flatMapLatest { settings ->
                    val (start, end) = resolveTimelineBounds(settings)
                    billDao.getRevenueBetween(start, end)
                }
                .map { it ?: 0.0 }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                    initialValue = 0.0,
                )

        // Expose a limited list of recent bills for the dashboard
        @OptIn(ExperimentalCoroutinesApi::class)
        val recentBills: StateFlow<List<BillWithPerson>> =
            _homeSearchQuery
                .flatMapLatest { query ->
                    if (query.isBlank()) {
                        billDao.getRecentBillsWithPerson(RECENT_BILL_LIMIT)
                    } else {
                        billDao.searchBillsWithPerson(query)
                    }
                }
                .map { it.take(RECENT_BILL_LIMIT) }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                    initialValue = emptyList(),
                )

        val businessNameInitial: StateFlow<String> =
            settingsFlow
                .map { settings ->
                    settings.businessName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "B"
                }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                    initialValue = "B",
                )

        private fun resolveTimelineBounds(settings: com.example.billease.data.AppSettings): Pair<Long, Long> =
            when (settings.dashboardTimeline) {
                DashboardTimeline.TODAY -> todayBounds()
                DashboardTimeline.THIS_WEEK -> thisWeekBounds()
                DashboardTimeline.THIS_MONTH -> monthBounds()
                DashboardTimeline.THIS_YEAR -> thisYearBounds()
                DashboardTimeline.CUSTOM -> {
                    val start = settings.customTimelineStart ?: monthBounds().first
                    val end = settings.customTimelineEnd ?: monthBounds().second
                    start to end
                }
            }
    }
