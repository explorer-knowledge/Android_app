package com.example.billease.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Person::class, Product::class, Bill::class, BillItem::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun personDao(): PersonDao
    abstract fun productDao(): ProductDao
    abstract fun billDao(): BillDao
}
