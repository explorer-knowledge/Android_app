package com.example.billease.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

data class AppSettings(
    val businessName: String,
    val address: String,
    // File path to internal storage copy
    val logoUri: String?,
)

@Singleton
class SettingsRepository
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        private val dataStore = context.dataStore

        private object PreferencesKeys {
            val BUSINESS_NAME = stringPreferencesKey("business_name")
            val ADDRESS = stringPreferencesKey("address")
            val LOGO_URI = stringPreferencesKey("logo_uri")
        }

        val appSettingsFlow: Flow<AppSettings> =
            dataStore.data.map { preferences ->
                AppSettings(
                    businessName = preferences[PreferencesKeys.BUSINESS_NAME] ?: "",
                    address = preferences[PreferencesKeys.ADDRESS] ?: "",
                    logoUri = preferences[PreferencesKeys.LOGO_URI],
                )
            }

        suspend fun updateSettings(
            businessName: String,
            address: String,
            logoUri: String?,
        ) {
            dataStore.edit { preferences ->
                preferences[PreferencesKeys.BUSINESS_NAME] = businessName
                preferences[PreferencesKeys.ADDRESS] = address
                if (logoUri != null) {
                    preferences[PreferencesKeys.LOGO_URI] = logoUri
                } else {
                    preferences.remove(PreferencesKeys.LOGO_URI)
                }
            }
        }
    }
