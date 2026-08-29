package com.example.billease.ui.persons

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.billease.R
import com.example.billease.data.BillDao
import com.example.billease.data.Person
import com.example.billease.data.PersonDao
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
        private val personDao: PersonDao,
        private val billDao: BillDao,
        @ApplicationContext private val context: Context,
    ) : ViewModel() {
        private val _searchQuery = MutableStateFlow("")
        val searchQuery: StateFlow<String> = _searchQuery

        @OptIn(ExperimentalCoroutinesApi::class)
        val persons =
            _searchQuery.flatMapLatest { query ->
                if (query.isBlank()) {
                    personDao.getAll()
                } else {
                    personDao.search(query)
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                initialValue = emptyList(),
            )

        fun onSearchQueryChange(query: String) {
            _searchQuery.value = query
        }

        // Decision: Prevent deletion if person has existing bills.
        fun deletePerson(
            person: Person,
            onResult: (String) -> Unit,
        ) {
            viewModelScope.launch {
                try {
                    val billCount = billDao.getBillCountForPerson(person.id)
                    if (billCount > 0) {
                        onResult(context.getString(R.string.error_cannot_delete_person_used))
                    } else {
                        personDao.delete(person)
                        onResult(context.getString(R.string.msg_person_deleted))
                    }
                } catch (e: Exception) {
                    onResult(context.getString(R.string.error_could_not_delete_person, e.message))
                }
            }
        }
    }
