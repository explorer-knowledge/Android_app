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
            .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
            // v1 predates the ForeignKey/RESTRICT constraints added in v2 (would require a full
            // table recreation to migrate, not a plain ALTER TABLE) and has no real-world
            // installs to preserve. Every version from v2 onward has a real migration above -
            // this fallback intentionally does NOT cover them, so a missing migration on a
            // future schema bump fails loudly (IllegalStateException) instead of silently
            // wiping user data.
            .fallbackToDestructiveMigrationFrom(1)
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

@Suppress("MagicNumber")
val MIGRATION_3_4 =
    object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("CREATE TABLE IF NOT EXISTS bill_sequences (prefix TEXT NOT NULL, lastNumber INTEGER NOT NULL, PRIMARY KEY(prefix))")
        }
    }

@Suppress("MagicNumber")
val MIGRATION_4_5 =
    object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Historical bill_items predate unitSnapshot; there's no source to backfill the
            // real unit from (the Product row may since have changed or been deleted), so
            // existing rows get '' - PdfGenerator/BillDetailScreen already render a blank
            // unitSnapshot by omitting it rather than showing a stray value.
            db.execSQL("ALTER TABLE bill_items ADD COLUMN unitSnapshot TEXT NOT NULL DEFAULT ''")
        }
    }

@Suppress("MagicNumber")
val MIGRATION_5_6 =
    object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_bills_billNumber ON bills(billNumber)")
        }
    }
