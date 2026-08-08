package com.example.billease.ui.persons

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.billease.data.BillingRepository
import com.example.billease.data.Person
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PersonsViewModel
    @Inject
    constructor(
        private val repository: BillingRepository,
    ) : ViewModel() {
        private val _searchQuery = MutableStateFlow("")
        val searchQuery: StateFlow<String> = _searchQuery

        @OptIn(ExperimentalCoroutinesApi::class)
        val persons =
            _searchQuery.flatMapLatest { query ->
                if (query.isBlank()) {
                    repository.getAllPersons()
                } else {
                    repository.searchPersons(query)
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList(),
            )

        fun onSearchQueryChange(query: String) {
            _searchQuery.value = query
        }

        // Decision: Prevent deletion if person has existing bills.
        fun deletePerson(
            person: Person,
            onResult: (Boolean, String) -> Unit,
        ) {
            viewModelScope.launch {
                val billCount = repository.getBillCountForPerson(person.id)
                if (billCount > 0) {
                    onResult(false, "Cannot delete person with existing bills.")
                } else {
                    repository.deletePerson(person)
                    onResult(true, "Person deleted successfully.")
                }
            }
        }
    }
