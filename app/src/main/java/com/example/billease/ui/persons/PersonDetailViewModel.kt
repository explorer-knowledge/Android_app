package com.example.billease.ui.persons

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.billease.data.Bill
import com.example.billease.data.BillingRepository
import com.example.billease.data.Person
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class PersonDetailViewModel
    @Inject
    constructor(
        repository: BillingRepository,
        savedStateHandle: SavedStateHandle,
    ) : ViewModel() {
        private val personId: Long = checkNotNull(savedStateHandle.get<Long>("personId"))

        val person: StateFlow<Person?> =
            repository.getPersonById(personId)
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = null,
                )

        val bills: StateFlow<List<Bill>> =
            repository.getBillsByPersonId(personId)
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = emptyList(),
                )
    }
