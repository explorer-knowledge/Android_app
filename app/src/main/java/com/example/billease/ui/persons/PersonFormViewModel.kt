package com.example.billease.ui.persons

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.billease.data.BillingRepository
import com.example.billease.data.Person
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PersonFormState(
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val gstNumber: String = "",
    val nameError: String? = null,
    val phoneError: String? = null,
)

@HiltViewModel
class PersonFormViewModel
    @Inject
    constructor(
        private val repository: BillingRepository,
        savedStateHandle: SavedStateHandle,
    ) : ViewModel() {
        private val personId: Long = savedStateHandle.get<Long>("personId") ?: -1L
        private val _uiState = MutableStateFlow(PersonFormState())
        val uiState: StateFlow<PersonFormState> = _uiState.asStateFlow()

        private var isEditMode = personId != -1L

        init {
            if (isEditMode) {
                viewModelScope.launch {
                    repository.getPersonById(personId).collect { person ->
                        person?.let {
                            _uiState.value =
                                PersonFormState(
                                    name = it.name,
                                    phone = it.phone,
                                    email = it.email ?: "",
                                    address = it.address ?: "",
                                    gstNumber = it.gstNumber ?: "",
                                )
                        }
                    }
                }
            }
        }

        fun updateName(name: String) {
            _uiState.update { it.copy(name = name, nameError = null) }
        }

        fun updatePhone(phone: String) {
            _uiState.update { it.copy(phone = phone, phoneError = null) }
        }

        fun updateEmail(email: String) {
            _uiState.update { it.copy(email = email) }
        }

        fun updateAddress(address: String) {
            _uiState.update { it.copy(address = address) }
        }

        fun updateGstNumber(gstNumber: String) {
            _uiState.update { it.copy(gstNumber = gstNumber) }
        }

        fun savePerson(onSuccess: () -> Unit) {
            val currentState = _uiState.value
            var hasError = false

            if (currentState.name.isBlank()) {
                _uiState.update { it.copy(nameError = "Name cannot be empty") }
                hasError = true
            }
            if (currentState.phone.isBlank()) {
                _uiState.update { it.copy(phoneError = "Phone cannot be empty") }
                hasError = true
            }

            if (hasError) return

            viewModelScope.launch {
                val person =
                    Person(
                        id = if (isEditMode) personId else 0L,
                        name = currentState.name,
                        phone = currentState.phone,
                        email = currentState.email.takeIf { it.isNotBlank() },
                        address = currentState.address.takeIf { it.isNotBlank() },
                        gstNumber = currentState.gstNumber.takeIf { it.isNotBlank() },
                    )

                if (isEditMode) {
                    repository.updatePerson(person)
                } else {
                    repository.insertPerson(person)
                }
                onSuccess()
            }
        }
    }
