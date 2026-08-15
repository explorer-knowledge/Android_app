package com.example.billease.util

import androidx.compose.runtime.staticCompositionLocalOf
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

/** Currencies offered in the Settings dropdown, in display order. */
val SUPPORTED_CURRENCIES =
    listOf("INR", "USD", "EUR", "GBP", "JPY", "AED", "AUD", "CAD", "SGD", "SAR", "CHF", "CNY")

/** The currency selected in Settings, available to every composable. */
val LocalCurrencyCode = staticCompositionLocalOf { "INR" }

fun formatMoney(
    amount: Double,
    currencyCode: String,
): String {
    val format = NumberFormat.getCurrencyInstance(Locale.getDefault())
    format.currency = Currency.getInstance(currencyCode)
    return format.format(amount)
}

/** e.g. "USD ($)" for the Settings dropdown labels. */
fun currencyLabel(code: String): String = "$code (${Currency.getInstance(code).symbol})"
