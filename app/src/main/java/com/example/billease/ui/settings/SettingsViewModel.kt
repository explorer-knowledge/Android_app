package com.example.billease.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.billease.data.AppSettings
import com.example.billease.data.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

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
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = AppSettings("", "", null, "BILL-"),
                )

        fun updateSettings(
            businessName: String,
            address: String,
            logoUri: String?,
            invoicePrefix: String = "BILL-",
            currencyCode: String = "INR",
        ) {
            viewModelScope.launch {
                repository.updateSettings(businessName, address, logoUri, invoicePrefix, currencyCode)
            }
        }
    }
