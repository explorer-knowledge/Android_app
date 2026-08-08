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

@Dao
interface BillDao {
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
    @Query("SELECT * FROM bills WHERE id = :id")
    fun getBillWithItemsAndPersonById(id: Long): Flow<BillWithItemsAndPerson?>

    @Query("SELECT * FROM bills WHERE personId = :personId ORDER BY billDate DESC")
    fun getBillsByPersonId(personId: Long): Flow<List<Bill>>

    @Query("SELECT COUNT(*) FROM bills WHERE billDate >= :startMillis AND billDate <= :endMillis")
    fun getBillCountBetween(startMillis: Long, endMillis: Long): Flow<Int>

    @Query("SELECT SUM(grandTotal) FROM bills WHERE billDate >= :startMillis AND billDate <= :endMillis")
    fun getRevenueBetween(startMillis: Long, endMillis: Long): Flow<Double?>

    @Query("SELECT COUNT(*) FROM bills WHERE personId = :personId")
    suspend fun getBillCountForPerson(personId: Long): Int

    @Query("SELECT COUNT(*) FROM bill_items WHERE productId = :productId")
    suspend fun getBillItemCountForProduct(productId: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBill(bill: Bill): Long

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
    ): Long {
        val billId = insertBill(bill)
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
