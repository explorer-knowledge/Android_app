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

/** Persisted string values that map to each timeline preset. */
enum class DashboardTimeline(val key: String) {
    TODAY("TODAY"),
    THIS_WEEK("THIS_WEEK"),
    THIS_MONTH("THIS_MONTH"),
    THIS_YEAR("THIS_YEAR"),
    CUSTOM("CUSTOM"),
    ;

    companion object {
        fun fromKey(key: String): DashboardTimeline = entries.firstOrNull { it.key == key } ?: THIS_MONTH
    }
}

data class AppSettings(
    val businessName: String,
    val address: String,
    // File path to internal storage copy
    val logoUri: String?,
    val invoicePrefix: String = "BILL-",
    val currencyCode: String = "INR",
    val dashboardTimeline: DashboardTimeline = DashboardTimeline.THIS_MONTH,
    /** Only non-null when dashboardTimeline == CUSTOM */
    val customTimelineStart: Long? = null,
    val customTimelineEnd: Long? = null,
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
            val DASHBOARD_TIMELINE = stringPreferencesKey("dashboard_timeline")
            val CUSTOM_TIMELINE_START = stringPreferencesKey("custom_timeline_start")
            val CUSTOM_TIMELINE_END = stringPreferencesKey("custom_timeline_end")
        }

        val appSettingsFlow: Flow<AppSettings> =
            dataStore.data.map { preferences ->
                AppSettings(
                    businessName = preferences[PreferencesKeys.BUSINESS_NAME] ?: "",
                    address = preferences[PreferencesKeys.ADDRESS] ?: "",
                    logoUri = preferences[PreferencesKeys.LOGO_URI],
                    invoicePrefix = preferences[PreferencesKeys.INVOICE_PREFIX] ?: "BILL-",
                    currencyCode = preferences[PreferencesKeys.CURRENCY_CODE] ?: "INR",
                    dashboardTimeline =
                        DashboardTimeline.fromKey(
                            preferences[PreferencesKeys.DASHBOARD_TIMELINE] ?: DashboardTimeline.THIS_MONTH.key,
                        ),
                    customTimelineStart = preferences[PreferencesKeys.CUSTOM_TIMELINE_START]?.toLongOrNull(),
                    customTimelineEnd = preferences[PreferencesKeys.CUSTOM_TIMELINE_END]?.toLongOrNull(),
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

        @Suppress("LongParameterList")
        suspend fun updateSettings(
            businessName: String,
            address: String,
            logoUri: String?,
            invoicePrefix: String = "BILL-",
            currencyCode: String = "INR",
            dashboardTimeline: DashboardTimeline = DashboardTimeline.THIS_MONTH,
            customTimelineStart: Long? = null,
            customTimelineEnd: Long? = null,
        ) {
            dataStore.edit { preferences ->
                preferences[PreferencesKeys.BUSINESS_NAME] = businessName
                preferences[PreferencesKeys.ADDRESS] = address
                // A blank prefix would produce bare-number bill numbers ("0001"); the sequence
                // table is still keyed correctly either way, but the display value should never
                // be empty.
                preferences[PreferencesKeys.INVOICE_PREFIX] = invoicePrefix.ifBlank { "BILL-" }
                preferences[PreferencesKeys.CURRENCY_CODE] = currencyCode
                preferences[PreferencesKeys.DASHBOARD_TIMELINE] = dashboardTimeline.key
                if (logoUri != null) {
                    preferences[PreferencesKeys.LOGO_URI] = logoUri
                } else {
                    preferences.remove(PreferencesKeys.LOGO_URI)
                }
                if (customTimelineStart != null) {
                    preferences[PreferencesKeys.CUSTOM_TIMELINE_START] = customTimelineStart.toString()
                } else {
                    preferences.remove(PreferencesKeys.CUSTOM_TIMELINE_START)
                }
                if (customTimelineEnd != null) {
                    preferences[PreferencesKeys.CUSTOM_TIMELINE_END] = customTimelineEnd.toString()
                } else {
                    preferences.remove(PreferencesKeys.CUSTOM_TIMELINE_END)
                }
            }
        }
    }
