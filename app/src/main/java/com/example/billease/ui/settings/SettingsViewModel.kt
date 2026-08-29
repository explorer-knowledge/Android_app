package com.example.billease.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.billease.data.AppSettings
import com.example.billease.data.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsFormState(
    val businessName: String = "",
    val address: String = "",
    val logoPath: String? = null,
    val invoicePrefix: String = "BILL-",
    val currencyCode: String = "INR",
)

@HiltViewModel
class SettingsViewModel
    @Inject
    constructor(
        private val repository: SettingsRepository,
    ) : ViewModel() {
        val appSettings: StateFlow<AppSettings> =
            repository.appSettingsFlow
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                    initialValue = AppSettings("", "", null, "BILL-"),
                )

        private val _formState = MutableStateFlow(SettingsFormState())
        val formState: StateFlow<SettingsFormState> = _formState.asStateFlow()
        private var seeded = false

        init {
            viewModelScope.launch {
                repository.appSettingsFlow.collect { settings ->
                    if (!seeded) {
                        _formState.update {
                            SettingsFormState(
                                businessName = settings.businessName,
                                address = settings.address,
                                logoPath = settings.logoUri,
                                invoicePrefix = settings.invoicePrefix,
                                currencyCode = settings.currencyCode,
                            )
                        }
                        seeded = true
                    }
                }
            }
        }

        fun updateBusinessName(value: String) = _formState.update { it.copy(businessName = value) }

        fun updateAddress(value: String) = _formState.update { it.copy(address = value) }

        fun updateInvoicePrefix(value: String) = _formState.update { it.copy(invoicePrefix = value) }

        fun updateCurrencyCode(value: String) = _formState.update { it.copy(currencyCode = value) }

        fun updateLogoPath(value: String?) = _formState.update { it.copy(logoPath = value) }

        fun save() {
            val state = _formState.value
            viewModelScope.launch {
                repository.updateSettings(
                    businessName = state.businessName,
                    address = state.address,
                    logoUri = state.logoPath,
                    invoicePrefix = state.invoicePrefix,
                    currencyCode = state.currencyCode,
                )
            }
        }
    }
