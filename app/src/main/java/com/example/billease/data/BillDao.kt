package com.example.billease.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

data class BillWithPerson(
    @Embedded val bill: Bill,
    @Relation(
        parentColumn = "personId",
        entityColumn = "id",
    )
    val person: Person,
)

data class BillWithItemsAndPerson(
    @Embedded val bill: Bill,
    @Relation(
        parentColumn = "personId",
        entityColumn = "id",
    )
    val person: Person,
    @Relation(
        parentColumn = "id",
        entityColumn = "billId",
    )
    val items: List<BillItem>,
)

/** One row of the per-product totals report. */
data class ProductTotalRow(
    val productName: String,
    val quantity: Double,
    val revenue: Double,
)

/** One row of the monthly collected-revenue breakdown. [month] is a local "yyyy-MM" key. */
data class MonthlyRevenueRow(
    val month: String,
    val revenue: Double,
)

@Dao
interface BillDao {
    @Query("SELECT COUNT(*) FROM bills")
    fun getTotalBillCount(): Flow<Int>

    // Revenue/"collected" is money actually received: only PAID bills. Outstanding
    // (PENDING + OVERDUE) is tracked separately and shown alongside it on Reports.
    @Query("SELECT SUM(grandTotal) FROM bills WHERE paymentStatus = 'PAID'")
    fun getTotalRevenue(): Flow<Double?>

    @Query("SELECT SUM(grandTotal) FROM bills WHERE paymentStatus != 'PAID'")
    fun getTotalOutstanding(): Flow<Double?>

    @Transaction
    @Query("SELECT * FROM bills ORDER BY billDate DESC LIMIT :limit")
    fun getRecentBillsWithPerson(limit: Int): Flow<List<BillWithPerson>>

    @Transaction
    @Query("SELECT * FROM bills ORDER BY billDate DESC")
    fun getAllBillsWithPerson(): Flow<List<BillWithPerson>>

    @Transaction
    @Query(
        """
        SELECT bills.* FROM bills 
        INNER JOIN persons ON bills.personId = persons.id 
        WHERE bills.billNumber LIKE '%' || :query || '%' OR persons.name LIKE '%' || :query || '%' 
        ORDER BY bills.billDate DESC
    """,
    )
    fun searchBillsWithPerson(query: String): Flow<List<BillWithPerson>>

    @Transaction
    @Query(
        """
        SELECT bills.* FROM bills 
        INNER JOIN persons ON bills.personId = persons.id 
        WHERE (:query = '' OR bills.billNumber LIKE '%' || :query || '%' OR persons.name LIKE '%' || :query || '%')
          AND (:startMillis IS NULL OR bills.billDate >= :startMillis)
          AND (:endExclusiveMillis IS NULL OR bills.billDate < :endExclusiveMillis)
        ORDER BY bills.billDate DESC
    """,
    )
    fun getFilteredBills(
        query: String,
        startMillis: Long?,
        endExclusiveMillis: Long?,
    ): Flow<List<BillWithPerson>>

    @Transaction
    @Query("SELECT * FROM bills WHERE id = :id")
    fun getBillWithItemsAndPersonById(id: Long): Flow<BillWithItemsAndPerson?>

    @Query("SELECT * FROM bills WHERE personId = :personId ORDER BY billDate DESC")
    fun getBillsByPersonId(personId: Long): Flow<List<Bill>>

    @Query("SELECT COUNT(*) FROM bills WHERE billDate >= :startMillis AND billDate <= :endMillis")
    fun getBillCountBetween(
        startMillis: Long,
        endMillis: Long,
    ): Flow<Int>

    @Query(
        """
        SELECT SUM(grandTotal) FROM bills
        WHERE billDate >= :startMillis AND billDate <= :endMillis AND paymentStatus = 'PAID'
    """,
    )
    fun getRevenueBetween(
        startMillis: Long,
        endMillis: Long,
    ): Flow<Double?>

    @Query(
        """
        SELECT SUM(grandTotal) FROM bills
        WHERE billDate >= :startMillis AND billDate <= :endMillis AND paymentStatus != 'PAID'
    """,
    )
    fun getOutstandingBetween(
        startMillis: Long,
        endMillis: Long,
    ): Flow<Double?>

    @Query(
        """
        SELECT strftime('%Y-%m', billDate / 1000, 'unixepoch', 'localtime') AS month,
               COALESCE(SUM(grandTotal), 0) AS revenue
        FROM bills
        WHERE paymentStatus = 'PAID'
        GROUP BY month
        ORDER BY month DESC
    """,
    )
    fun getMonthlyRevenue(): Flow<List<MonthlyRevenueRow>>

    @Query(
        """
        SELECT productNameSnapshot AS productName,
               COALESCE(SUM(quantity), 0) AS quantity,
               COALESCE(SUM(lineTotal), 0) AS revenue
        FROM bill_items
        GROUP BY productNameSnapshot
        ORDER BY revenue DESC
    """,
    )
    fun getProductTotals(): Flow<List<ProductTotalRow>>

    @Query("SELECT COUNT(*) FROM bills WHERE personId = :personId")
    suspend fun getBillCountForPerson(personId: Long): Int

    @Query("SELECT COUNT(*) FROM bill_items WHERE productId = :productId")
    suspend fun getBillItemCountForProduct(productId: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBill(bill: Bill): Long

    @Query("SELECT lastNumber FROM bill_sequences WHERE prefix = :prefix")
    suspend fun getLastBillNumber(prefix: String): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBillSequence(sequence: BillSequence)

    suspend fun peekNextBillNumber(prefix: String): String {
        val nextNumber = (getLastBillNumber(prefix) ?: 0L) + 1L
        return formatBillNumber(prefix, nextNumber)
    }

    @Update
    suspend fun updateBill(bill: Bill)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBillItems(items: List<BillItem>)

    @Query("DELETE FROM bill_items WHERE billId = :billId")
    suspend fun deleteBillItemsByBillId(billId: Long)

    @Delete
    suspend fun deleteBillEntity(bill: Bill)

    @Transaction
    suspend fun deleteBill(bill: Bill) {
        deleteBillItemsByBillId(bill.id)
        deleteBillEntity(bill)
    }

    @Transaction
    suspend fun insertBillWithItems(
        bill: Bill,
        items: List<BillItem>,
        prefix: String,
    ): Long {
        val nextNumber = (getLastBillNumber(prefix) ?: 0L) + 1L
        upsertBillSequence(BillSequence(prefix, nextNumber))
        val billWithNumber = bill.copy(billNumber = formatBillNumber(prefix, nextNumber))
        val billId = insertBill(billWithNumber)
        val itemsWithBillId = items.map { it.copy(billId = billId) }
        insertBillItems(itemsWithBillId)
        return billId
    }

    @Transaction
    suspend fun updateBillWithItems(
        bill: Bill,
        items: List<BillItem>,
    ) {
        updateBill(bill)
        deleteBillItemsByBillId(bill.id)
        val itemsWithBillId = items.map { it.copy(billId = bill.id) }
        insertBillItems(itemsWithBillId)
    }
}
