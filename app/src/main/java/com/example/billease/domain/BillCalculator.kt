package com.example.billease.domain

/**
 * Input for a single line item. Values are snapshot-style so the calculator
 * is decoupled from live Product data — use [BillItemInput.fromProduct] when
 * building a new bill, or construct directly from saved BillItem snapshot
 * columns when recalculating an existing one.
 */
data class BillItemInput(
    val productId: Long,
    val productName: String,     // snapshot
    val unitPrice: Double,       // snapshot
    val taxPercent: Double,      // snapshot
    val quantity: Double,
) {
    companion object {
        fun fromProduct(
            product: com.example.billease.data.Product,
            quantity: Double,
        ) = BillItemInput(
            productId = product.id,
            productName = product.name,
            unitPrice = product.unitPrice,
            taxPercent = product.taxPercent,
            quantity = quantity,
        )
    }

    /** Pre-tax line total. */
    val lineSubtotal: Double get() = unitPrice * quantity

    /** Tax amount for this line. */
    val lineTax: Double get() = lineSubtotal * (taxPercent / 100.0)

    /** Total including tax for this line. */
    val lineTotal: Double get() = lineSubtotal + lineTax
}

data class BillCalculationResult(
    val subtotal: Double,   // sum of all lineSubtotals
    val taxTotal: Double,   // sum of all lineTaxes
    val grandTotal: Double, // subtotal + taxTotal - discount (≥ 0)
)

/**
 * Pure domain class — no Android or Room imports.
 * Decision: discount is applied to the post-tax grand total, not per-line.
 */
object BillCalculator {

    fun calculate(items: List<BillItemInput>, discount: Double = 0.0): BillCalculationResult {
        val subtotal = items.sumOf { it.lineSubtotal }
        val taxTotal = items.sumOf { it.lineTax }
        val grandTotal = maxOf(0.0, subtotal + taxTotal - discount)
        return BillCalculationResult(subtotal = subtotal, taxTotal = taxTotal, grandTotal = grandTotal)
    }
}
