package com.example.billease.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val unitPrice: Double,
    val unit: String,
    val taxPercent: Double = 0.0,
    val description: String? = null
)
