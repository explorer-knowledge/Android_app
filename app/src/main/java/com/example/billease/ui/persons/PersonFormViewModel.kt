package com.example.billease.ui.persons

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.billease.R
import com.example.billease.data.Person
import com.example.billease.data.PersonDao
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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
    val saveError: String? = null,
)

@HiltViewModel
class PersonFormViewModel
    @Inject
    constructor(
        private val personDao: PersonDao,
        @ApplicationContext private val context: Context,
        savedStateHandle: SavedStateHandle,
    ) : ViewModel() {
        private val personId: Long = savedStateHandle.get<Long>("personId") ?: -1L
        private val _uiState = MutableStateFlow(PersonFormState())
        val uiState: StateFlow<PersonFormState> = _uiState.asStateFlow()

        private var isEditMode = personId != -1L

        init {
            if (isEditMode) {
                viewModelScope.launch {
                    personDao.getById(personId).first()?.let {
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
                _uiState.update { it.copy(nameError = context.getString(R.string.error_name_required)) }
                hasError = true
            }
            if (currentState.phone.isBlank()) {
                _uiState.update { it.copy(phoneError = context.getString(R.string.error_phone_required)) }
                hasError = true
            }

            if (hasError) return

            _uiState.update { it.copy(saveError = null) }
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

                try {
                    if (isEditMode) {
                        personDao.update(person)
                    } else {
                        personDao.insert(person)
                    }
                    onSuccess()
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(saveError = context.getString(R.string.error_could_not_save_person, e.message))
                    }
                }
            }
        }
    }
