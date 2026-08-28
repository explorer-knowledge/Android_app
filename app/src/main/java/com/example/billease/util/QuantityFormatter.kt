package com.example.billease.util

/** Renders a Double without a trailing ".0" when it has no fractional part (e.g. "2", not "2.0"). */
fun formatQuantity(value: Double): String =
    if (value == value.toLong().toDouble()) {
        value.toLong().toString()
    } else {
        value.toString()
    }

/** Same integral-safe rendering as [formatQuantity], with a trailing "%" (e.g. "18%", not "18.0%"). */
fun formatPercent(value: Double): String = "${formatQuantity(value)}%"
