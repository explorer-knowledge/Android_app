package com.example.billease.ui.bills

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.billease.data.Bill
import com.example.billease.data.BillDao
import com.example.billease.data.BillWithPerson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class BillsViewModel
    @Inject
    constructor(
        private val billDao: BillDao,
    ) : ViewModel() {
        private val _searchQuery = MutableStateFlow("")
        val searchQuery: StateFlow<String> = _searchQuery

        private val _dateRange = MutableStateFlow<Pair<Long?, Long?>>(null to null)
        val dateRange: StateFlow<Pair<Long?, Long?>> = _dateRange

        @OptIn(ExperimentalCoroutinesApi::class)
        val bills: StateFlow<List<BillWithPerson>> =
            combine(_searchQuery, _dateRange) { query, range -> query to range }
                .flatMapLatest { (query, range) ->
                    // DatePicker returns UTC-midnight; normalize to local-day boundaries
                    val start = range.first?.let { startOfDayMillis(it) }
                    val endExclusive = range.second?.let { endOfDayMillis(it) }
                    billDao.getFilteredBills(query, start, endExclusive)
                }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000), emptyList())

        fun onSearchQueryChange(query: String) {
            _searchQuery.value = query
        }

        fun onDateRangeChange(range: Pair<Long?, Long?>) {
            _dateRange.value = range
        }

        @Suppress("MagicNumber")
        private fun startOfDayMillis(dayMillis: Long): Long =
            Calendar.getInstance().apply {
                timeInMillis = dayMillis
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

        @Suppress("MagicNumber")
        private fun endOfDayMillis(dayMillis: Long): Long =
            Calendar.getInstance().apply {
                timeInMillis = dayMillis
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.timeInMillis

        fun deleteBill(
            bill: Bill,
            onResult: (String) -> Unit,
        ) {
            viewModelScope.launch {
                try {
                    billDao.deleteBill(bill)
                    onResult("Bill deleted.")
                } catch (e: Exception) {
                    onResult("Could not delete bill: ${e.message}")
                }
            }
        }
    }
