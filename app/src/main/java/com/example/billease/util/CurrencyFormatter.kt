package com.example.billease.util

import java.text.NumberFormat
import java.util.Locale

fun formatMoney(amount: Double): String = NumberFormat.getCurrencyInstance(Locale.getDefault()).format(amount)
