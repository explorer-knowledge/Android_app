package com.example.billease.data

import android.content.Context
import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

data class AppSettings(
    val businessName: String,
    val address: String,
    // File path to internal storage copy
    val logoUri: String?,
    val invoicePrefix: String = "BILL-",
    val currencyCode: String = "INR",
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
            val INVOICE_PREFIX = stringPreferencesKey("invoice_prefix")
            val CURRENCY_CODE = stringPreferencesKey("currency_code")
        }

        val appSettingsFlow: Flow<AppSettings> =
            dataStore.data.map { preferences ->
                AppSettings(
                    businessName = preferences[PreferencesKeys.BUSINESS_NAME] ?: "",
                    address = preferences[PreferencesKeys.ADDRESS] ?: "",
                    logoUri = preferences[PreferencesKeys.LOGO_URI],
                    invoicePrefix = preferences[PreferencesKeys.INVOICE_PREFIX] ?: "BILL-",
                    currencyCode = preferences[PreferencesKeys.CURRENCY_CODE] ?: "INR",
                )
            }

        // Copies a picked image into app-private storage and returns its absolute path,
        // or null on failure. Runs on Dispatchers.IO; both streams are closed with .use{}
        // so a mid-copy exception can't leak handles.
        suspend fun copyLogoToInternalStorage(uri: Uri): String? =
            withContext(Dispatchers.IO) {
                try {
                    val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null
                    val file = File(context.filesDir, "business_logo.jpg")
                    inputStream.use { input ->
                        FileOutputStream(file).use { output ->
                            input.copyTo(output)
                        }
                    }
                    file.absolutePath
                } catch (e: Exception) {
                    null
                }
            }

        suspend fun updateSettings(
            businessName: String,
            address: String,
            logoUri: String?,
            invoicePrefix: String = "BILL-",
            currencyCode: String = "INR",
        ) {
            dataStore.edit { preferences ->
                preferences[PreferencesKeys.BUSINESS_NAME] = businessName
                preferences[PreferencesKeys.ADDRESS] = address
                // A blank prefix would produce bare-number bill numbers ("0001"); the sequence
                // table is still keyed correctly either way, but the display value should never
                // be empty.
                preferences[PreferencesKeys.INVOICE_PREFIX] = invoicePrefix.ifBlank { "BILL-" }
                preferences[PreferencesKeys.CURRENCY_CODE] = currencyCode
                if (logoUri != null) {
                    preferences[PreferencesKeys.LOGO_URI] = logoUri
                } else {
                    preferences.remove(PreferencesKeys.LOGO_URI)
                }
            }
        }
    }
