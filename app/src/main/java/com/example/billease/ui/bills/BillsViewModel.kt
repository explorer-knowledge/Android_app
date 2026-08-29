package com.example.billease.ui.bills

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.billease.R
import com.example.billease.data.Bill
import com.example.billease.data.BillDao
import com.example.billease.data.BillWithPerson
import com.example.billease.data.Person
import com.example.billease.data.PersonDao
import com.example.billease.data.Product
import com.example.billease.data.ProductDao
import com.example.billease.util.last7DaysBounds
import com.example.billease.util.monthBounds
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

/** Which quick-date preset is active in the Bills filter. */
enum class DateFilterPreset { ALL, RECENT, THIS_MONTH, CUSTOM }

/**
 * Immutable snapshot of all active filter settings for the Bills list.
 * Custom date range is only consulted when [datePreset] == CUSTOM.
 */
data class BillFilterState(
    val searchQuery: String = "",
    val datePreset: DateFilterPreset = DateFilterPreset.ALL,
    val customStart: Long? = null,
    val customEnd: Long? = null,
    val selectedPerson: Person? = null,
    val selectedProduct: Product? = null,
) {
    val isActive: Boolean
        get() =
            searchQuery.isNotBlank() ||
                datePreset != DateFilterPreset.ALL ||
                selectedPerson != null ||
                selectedProduct != null
}

@HiltViewModel
@Suppress("TooManyFunctions")
class BillsViewModel
    @Inject
    constructor(
        private val billDao: BillDao,
        private val personDao: PersonDao,
        private val productDao: ProductDao,
        @ApplicationContext private val context: Context,
    ) : ViewModel() {
        private val _filter = MutableStateFlow(BillFilterState())
        val filter: StateFlow<BillFilterState> = _filter

        /** Convenience alias kept for backward compat with the screen reading searchQuery. */
        val searchQuery: StateFlow<String> = MutableStateFlow("").also { /* delegated below */ }

        /** All persons — used to populate the Person filter picker. */
        val persons: StateFlow<List<Person>> =
            personDao.getAll()
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

        /** All products — used to populate the Product filter picker. */
        val products: StateFlow<List<Product>> =
            productDao.getAll()
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

        @OptIn(ExperimentalCoroutinesApi::class)
        val bills: StateFlow<List<BillWithPerson>> =
            _filter
                .flatMapLatest { f ->
                    val (start, end) = resolveDateBounds(f)
                    billDao.getFilteredBills(
                        query = f.searchQuery,
                        startMillis = start,
                        endExclusiveMillis = end,
                        personId = f.selectedPerson?.id,
                        productId = f.selectedProduct?.id,
                    )
                }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

        // --- filter update helpers ---

        fun onSearchQueryChange(query: String) {
            _filter.value = _filter.value.copy(searchQuery = query)
        }

        fun onDatePresetChange(preset: DateFilterPreset) {
            _filter.value = _filter.value.copy(datePreset = preset)
        }

        fun onCustomDateRange(
            start: Long?,
            end: Long?,
        ) {
            _filter.value =
                _filter.value.copy(
                    datePreset = if (start != null || end != null) DateFilterPreset.CUSTOM else DateFilterPreset.ALL,
                    customStart = start,
                    customEnd = end,
                )
        }

        fun onPersonFilter(person: Person?) {
            _filter.value = _filter.value.copy(selectedPerson = person)
        }

        fun onProductFilter(product: Product?) {
            _filter.value = _filter.value.copy(selectedProduct = product)
        }

        fun clearAllFilters() {
            _filter.value = BillFilterState()
        }

        // kept for backward compat with DateRangeFilterRow consumers that were
        // referencing this before the refactor; now routes through the unified state
        @OptIn(ExperimentalCoroutinesApi::class)
        val dateRange: StateFlow<Pair<Long?, Long?>> =
            _filter
                .flatMapLatest { f ->
                    kotlinx.coroutines.flow.flowOf(
                        when (f.datePreset) {
                            DateFilterPreset.CUSTOM -> f.customStart to f.customEnd
                            else -> null to null
                        },
                    )
                }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null to null)

        @Deprecated("Use onCustomDateRange / onDatePresetChange instead")
        fun onDateRangeChange(range: Pair<Long?, Long?>) {
            onCustomDateRange(range.first, range.second)
        }

        // --- private helpers ---

        private fun resolveDateBounds(f: BillFilterState): Pair<Long?, Long?> =
            when (f.datePreset) {
                DateFilterPreset.ALL -> null to null
                DateFilterPreset.RECENT -> {
                    val b = last7DaysBounds()
                    b.first to b.second + 1
                }
                DateFilterPreset.THIS_MONTH -> {
                    val b = monthBounds()
                    b.first to b.second + 1
                }
                DateFilterPreset.CUSTOM -> {
                    val start = f.customStart?.let { startOfDayMillis(it) }
                    val end = f.customEnd?.let { endOfDayMillis(it) }
                    start to end
                }
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
                    onResult(context.getString(R.string.msg_bill_deleted))
                } catch (e: Exception) {
                    onResult(context.getString(R.string.error_could_not_delete_bill, e.message))
                }
            }
        }
    }
