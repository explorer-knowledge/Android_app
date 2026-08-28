package com.example.billease.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "bills",
    foreignKeys = [
        ForeignKey(
            entity = Person::class,
            parentColumns = ["id"],
            childColumns = ["personId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [Index("personId"), Index("billNumber", unique = true)],
)
data class Bill(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val billNumber: String,
    val personId: Long,
    // epoch millis
    val billDate: Long,
    val discount: Double = 0.0,
    val notes: String? = null,
    val subtotal: Double,
    val taxTotal: Double,
    val grandTotal: Double,
    val paymentStatus: BillStatus = BillStatus.PENDING,
    val createdAt: Long,
    val updatedAt: Long,
)
