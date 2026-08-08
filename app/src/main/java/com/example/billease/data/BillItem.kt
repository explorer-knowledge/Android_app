package com.example.billease.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "bill_items",
    foreignKeys = [
        ForeignKey(
            entity = Bill::class,
            parentColumns = ["id"],
            childColumns = ["billId"],
            onDelete = ForeignKey.RESTRICT,
        ),
        ForeignKey(
            entity = Product::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [Index("billId"), Index("productId")],
)
data class BillItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val billId: Long,
    val productId: Long,
    val productNameSnapshot: String,
    val quantity: Double,
    val unitPriceSnapshot: Double,
    val taxPercentSnapshot: Double,
    val lineTotal: Double,
)
