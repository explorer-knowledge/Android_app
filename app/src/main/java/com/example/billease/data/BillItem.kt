package com.example.billease.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bill_items")
data class BillItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val billId: Long,
    val productId: Long,
    val productNameSnapshot: String,
    val quantity: Double,
    val unitPriceSnapshot: Double,
    val taxPercentSnapshot: Double,
    val lineTotal: Double
)
