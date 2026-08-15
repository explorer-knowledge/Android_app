package com.example.billease.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.billease.data.AppDatabase
import com.example.billease.data.BillDao
import com.example.billease.data.PersonDao
import com.example.billease.data.ProductDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "billease_database",
        )
            .addMigrations(MIGRATION_2_3)
            // Pre-release: destructive migration acceptable while there is no shipped user data.
            // Remove this before the first production release and replace with proper Migrations.
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun providePersonDao(database: AppDatabase): PersonDao = database.personDao()

    @Provides
    fun provideProductDao(database: AppDatabase): ProductDao = database.productDao()

    @Provides
    fun provideBillDao(database: AppDatabase): BillDao = database.billDao()
}

@Suppress("MagicNumber")
val MIGRATION_2_3 =
    object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE bills ADD COLUMN paymentStatus TEXT NOT NULL DEFAULT 'PENDING'")
        }
    }
