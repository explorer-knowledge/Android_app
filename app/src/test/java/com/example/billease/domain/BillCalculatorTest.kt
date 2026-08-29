package com.example.billease.domain

import com.example.billease.data.Product
import org.junit.Assert.assertEquals
import org.junit.Test

class BillCalculatorTest {
    // ── helpers ──────────────────────────────────────────────────────────────

    private fun product(
        id: Long = 1L,
        name: String = "Item",
        price: Double = 1.0,
        tax: Double = 0.0,
    ) = Product(id = id, name = name, unitPrice = price, unit = "pcs", taxPercent = tax)

    private fun input(
        product: Product,
        qty: Double,
    ) = BillItemInput.fromProduct(product, qty)

    // ── tests ────────────────────────────────────────────────────────────────

    @Test
    fun `empty list with zero discount returns all zeros`() {
        val result = BillCalculator.calculate(emptyList())
        assertEquals(0.0, result.subtotal, 0.001)
        assertEquals(0.0, result.taxTotal, 0.001)
        assertEquals(0.0, result.grandTotal, 0.001)
    }

    @Test
    fun `single item no tax no discount`() {
        val items = listOf(input(product(price = 1.5), qty = 2.0))
        val result = BillCalculator.calculate(items)
        assertEquals(3.0, result.subtotal, 0.001)
        assertEquals(0.0, result.taxTotal, 0.001)
        assertEquals(3.0, result.grandTotal, 0.001)
    }

    @Test
    fun `multiple items with different tax rates`() {
        // product1: 2 × 1.50 = 3.00, tax 10% = 0.30
        // product2: 3 × 2.00 = 6.00, tax  5% = 0.30
        val items =
            listOf(
                input(product(price = 1.5, tax = 10.0), qty = 2.0),
                input(product(price = 2.0, tax = 5.0), qty = 3.0),
            )
        val result = BillCalculator.calculate(items)
        assertEquals(9.0, result.subtotal, 0.001)
        assertEquals(0.6, result.taxTotal, 0.001)
        assertEquals(9.6, result.grandTotal, 0.001)
    }

    @Test
    fun `discount is subtracted from post-tax grand total`() {
        // 2 × 1.50 = 3.00, 10% tax = 0.30 → pre-discount = 3.30, discount = 1.00
        val items = listOf(input(product(price = 1.5, tax = 10.0), qty = 2.0))
        val result = BillCalculator.calculate(items, discount = 1.0)
        assertEquals(3.0, result.subtotal, 0.001)
        assertEquals(0.3, result.taxTotal, 0.001)
        assertEquals(2.3, result.grandTotal, 0.001)
    }

    @Test
    fun `grand total never goes below zero even with excess discount`() {
        val items = listOf(input(product(price = 1.0), qty = 2.0))
        val result = BillCalculator.calculate(items, discount = 100.0)
        assertEquals(2.0, result.subtotal, 0.001)
        assertEquals(0.0, result.taxTotal, 0.001)
        assertEquals(0.0, result.grandTotal, 0.001)
    }

    @Test
    fun `negative discount never increases the grand total`() {
        // 2 x 1.50 = 3.00, no tax; a negative "discount" must not inflate the total.
        val items = listOf(input(product(price = 1.5), qty = 2.0))
        val result = BillCalculator.calculate(items, discount = -50.0)
        assertEquals(3.0, result.subtotal, 0.001)
        assertEquals(3.0, result.grandTotal, 0.001)
    }

    @Test
    fun `exactly 100 percent discount yields a zero grand total`() {
        // 2 x 1.50 = 3.00 + 10% tax = 3.30, discount exactly equals the total.
        val items = listOf(input(product(price = 1.5, tax = 10.0), qty = 2.0))
        val result = BillCalculator.calculate(items, discount = 3.3)
        assertEquals(3.0, result.subtotal, 0.001)
        assertEquals(0.3, result.taxTotal, 0.001)
        assertEquals(0.0, result.grandTotal, 0.001)
    }

    @Test
    fun `NaN inputs are clamped to zero instead of poisoning the total`() {
        val nan = Double.NaN
        val items =
            listOf(
                input(product(price = 1.5), qty = 2.0),
                BillItemInput(
                    productId = 99L,
                    productName = "Broken",
                    unit = "pcs",
                    unitPrice = nan,
                    taxPercent = nan,
                    quantity = nan,
                ),
            )
        val result = BillCalculator.calculate(items, discount = nan)
        assertEquals(3.0, result.subtotal, 0.001)
        assertEquals(0.0, result.taxTotal, 0.001)
        assertEquals(3.0, result.grandTotal, 0.001)
    }

    @Test
    fun `zero quantity item contributes nothing`() {
        val items = listOf(input(product(price = 50.0, tax = 18.0), qty = 0.0))
        val result = BillCalculator.calculate(items)
        assertEquals(0.0, result.subtotal, 0.001)
        assertEquals(0.0, result.taxTotal, 0.001)
        assertEquals(0.0, result.grandTotal, 0.001)
    }

    @Test
    fun `snapshot values are correctly set from product`() {
        val p = product(id = 7L, name = "Widget", price = 9.99, tax = 12.5)
        val item = BillItemInput.fromProduct(p, quantity = 3.0)

        assertEquals(7L, item.productId)
        assertEquals("Widget", item.productName)
        assertEquals("pcs", item.unit)
        assertEquals(9.99, item.unitPrice, 0.001)
        assertEquals(12.5, item.taxPercent, 0.001)
        assertEquals(29.97, item.lineSubtotal, 0.01)
        assertEquals(3.746, item.lineTax, 0.01)
        assertEquals(33.716, item.lineTotal, 0.01)
    }
}
