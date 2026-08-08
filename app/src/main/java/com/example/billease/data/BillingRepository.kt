package com.example.billease.data

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingRepository
    @Inject
    constructor(
        private val personDao: PersonDao,
        private val productDao: ProductDao,
        private val billDao: BillDao,
    ) {
        // Persons
        fun getAllPersons(): Flow<List<Person>> = personDao.getAll()

        fun searchPersons(query: String): Flow<List<Person>> = personDao.search(query)

        fun getPersonById(id: Long): Flow<Person?> = personDao.getById(id)

        suspend fun insertPerson(person: Person) = personDao.insert(person)

        suspend fun updatePerson(person: Person) = personDao.update(person)

        suspend fun deletePerson(person: Person) = personDao.delete(person)

        // Products
        fun getAllProducts(): Flow<List<Product>> = productDao.getAll()

        fun searchProducts(query: String): Flow<List<Product>> = productDao.search(query)

        fun getProductById(id: Long): Flow<Product?> = productDao.getById(id)

        suspend fun insertProduct(product: Product) = productDao.insert(product)

        suspend fun updateProduct(product: Product) = productDao.update(product)

        suspend fun deleteProduct(product: Product) = productDao.delete(product)

        // Bills
        fun getAllBills(): Flow<List<BillWithPerson>> = billDao.getAllBillsWithPerson()

        fun searchBills(query: String): Flow<List<BillWithPerson>> = billDao.searchBillsWithPerson(query)

        fun getBillWithItemsById(id: Long): Flow<BillWithItemsAndPerson?> = billDao.getBillWithItemsAndPersonById(id)

        fun getBillsByPersonId(personId: Long): Flow<List<Bill>> = billDao.getBillsByPersonId(personId)

        fun getBillCountBetween(
            startMillis: Long,
            endMillis: Long,
        ): Flow<Int> = billDao.getBillCountBetween(startMillis, endMillis)

        fun getRevenueBetween(
            startMillis: Long,
            endMillis: Long,
        ): Flow<Double?> = billDao.getRevenueBetween(startMillis, endMillis)

        suspend fun getBillCountForPerson(personId: Long): Int = billDao.getBillCountForPerson(personId)

        suspend fun getBillItemCountForProduct(productId: Long): Int = billDao.getBillItemCountForProduct(productId)

        suspend fun insertBillWithItems(
            bill: Bill,
            items: List<BillItem>,
        ): Long = billDao.insertBillWithItems(bill, items)

        suspend fun updateBillWithItems(
            bill: Bill,
            items: List<BillItem>,
        ) = billDao.updateBillWithItems(bill, items)

        suspend fun deleteBill(bill: Bill) = billDao.deleteBill(bill)
    }
