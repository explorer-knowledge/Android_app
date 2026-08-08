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
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("personId")]
)
data class Bill(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val billNumber: String,
    val personId: Long,
    val billDate: Long,          // epoch millis
    val discount: Double = 0.0,
    val notes: String? = null,
    val subtotal: Double,
    val taxTotal: Double,
    val grandTotal: Double,
    val createdAt: Long,
    val updatedAt: Long
)
