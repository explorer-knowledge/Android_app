package com.example.billease.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Per-prefix counter for auto-generated bill numbers (e.g. `BILL-0001`, `BILL-0002`). */
@Entity(tableName = "bill_sequences")
data class BillSequence(
    @PrimaryKey val prefix: String,
    val lastNumber: Long,
)

fun formatBillNumber(
    prefix: String,
    number: Long,
): String = "$prefix${number.toString().padStart(4, '0')}"
