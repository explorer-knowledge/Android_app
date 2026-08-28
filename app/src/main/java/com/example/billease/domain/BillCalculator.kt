package com.example.billease.domain

/**
 * Input for a single line item. Values are snapshot-style so the calculator
 * is decoupled from live Product data — use [BillItemInput.fromProduct] when
 * building a new bill, or construct directly from saved BillItem snapshot
 * columns when recalculating an existing one.
 */
data class BillItemInput(
    val productId: Long,
    // snapshot
    val productName: String,
    // snapshot
    val unit: String,
    // snapshot
    val unitPrice: Double,
    // snapshot
    val taxPercent: Double,
    val quantity: Double,
) {
    companion object {
        fun fromProduct(
            product: com.example.billease.data.Product,
            quantity: Double,
        ) = BillItemInput(
            productId = product.id,
            productName = product.name,
            unit = product.unit,
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
    // sum of all lineSubtotals
    val subtotal: Double,
    // sum of all lineTaxes
    val taxTotal: Double,
    // subtotal + taxTotal - discount (≥ 0)
    val grandTotal: Double,
)

/**
 * Pure domain class — no Android or Room imports.
 * Decision: discount is applied to the post-tax grand total, not per-line.
 */
object BillCalculator {
    fun calculate(
        items: List<BillItemInput>,
        discount: Double = 0.0,
    ): BillCalculationResult {
        val subtotal = items.sumOf { it.lineSubtotal }
        val taxTotal = items.sumOf { it.lineTax }
        // Defense-in-depth: a negative discount must never increase the total.
        // Callers (BillFormViewModel) reject negative input before it gets here,
        // but this keeps the invariant even if a future caller doesn't validate.
        val safeDiscount = maxOf(0.0, discount)
        val grandTotal = maxOf(0.0, subtotal + taxTotal - safeDiscount)
        return BillCalculationResult(subtotal = subtotal, taxTotal = taxTotal, grandTotal = grandTotal)
    }
}
