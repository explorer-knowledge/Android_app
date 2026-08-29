package com.example.billease.util

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import androidx.core.content.FileProvider
import com.example.billease.R
import com.example.billease.data.AppSettings
import com.example.billease.data.BillWithItemsAndPerson
import java.io.File
import java.io.FileOutputStream

object PdfGenerator {
    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN_LEFT = 50f
    private const val MARGIN_RIGHT = 545f
    private const val MARGIN_BOTTOM = 50f
    private const val MAX_Y = PAGE_HEIGHT - MARGIN_BOTTOM

    fun generatePdf(
        context: Context,
        data: BillWithItemsAndPerson,
        settings: AppSettings,
    ): Uri? {
        val pdfDocument = PdfDocument()
        var pageNumber = 1

        fun openPage(): PdfDocument.Page {
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
            pageNumber++
            return pdfDocument.startPage(pageInfo)
        }

        var page = openPage()
        var canvas = page.canvas
        var yPos = 50f

        val paint =
            Paint().apply {
                color = Color.BLACK
                textSize = 12f
                isAntiAlias = true
            }

        fun checkPageBreak(requiredSpace: Float) {
            if (yPos + requiredSpace > MAX_Y) {
                pdfDocument.finishPage(page)
                page = openPage()
                canvas = page.canvas
                yPos = 50f
            }
        }

        // Business Logo
        if (!settings.logoUri.isNullOrBlank()) {
            val bitmap = BitmapFactory.decodeFile(settings.logoUri)
            if (bitmap != null) {
                val targetHeight = 80
                val ratio = targetHeight.toFloat() / bitmap.height
                val targetWidth = (bitmap.width * ratio).toInt()
                canvas.drawBitmap(
                    bitmap,
                    null,
                    Rect(MARGIN_LEFT.toInt(), yPos.toInt(), (MARGIN_LEFT + targetWidth).toInt(), (yPos + targetHeight).toInt()),
                    paint,
                )
                yPos += 90f
            }
        }

        // Business Info / Title
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 20f
        val bName = settings.businessName.ifBlank { context.getString(R.string.pdf_invoice_title) }
        canvas.drawText(bName, MARGIN_LEFT, yPos, paint)
        yPos += 20f

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 12f
        if (settings.address.isNotBlank()) {
            val addressLines = settings.address.split("\n")
            checkPageBreak(addressLines.size * 15f)
            addressLines.forEach {
                canvas.drawText(it, MARGIN_LEFT, yPos, paint)
                yPos += 15f
            }
            yPos += 10f
        }

        // Bill Info
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(context.getString(R.string.pdf_bill_no, data.bill.billNumber), MARGIN_LEFT, yPos, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        val dateStr = formatDate(data.bill.billDate)
        canvas.drawText(context.getString(R.string.pdf_date, dateStr), MARGIN_LEFT, yPos + 15f, paint)

        yPos += 45f

        // Bill To
        checkPageBreak(
            45f +
                (if (data.person.email.isNullOrBlank()) 0f else 15f) +
                data.person.address.orEmpty().split("\n").size * 15f +
                (if (data.person.gstNumber.isNullOrBlank()) 0f else 15f),
        )
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(context.getString(R.string.pdf_bill_to), MARGIN_LEFT, yPos, paint)
        yPos += 15f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText(data.person.name, MARGIN_LEFT, yPos, paint)
        yPos += 15f
        canvas.drawText(data.person.phone, MARGIN_LEFT, yPos, paint)
        yPos += 15f
        if (!data.person.email.isNullOrBlank()) {
            canvas.drawText(data.person.email, MARGIN_LEFT, yPos, paint)
            yPos += 15f
        }
        if (!data.person.address.isNullOrBlank()) {
            data.person.address.split("\n").forEach {
                canvas.drawText(it, MARGIN_LEFT, yPos, paint)
                yPos += 15f
            }
        }
        if (!data.person.gstNumber.isNullOrBlank()) {
            canvas.drawText(context.getString(R.string.gst_label, data.person.gstNumber), MARGIN_LEFT, yPos, paint)
            yPos += 15f
        }

        yPos += 30f

        // Table Header
        checkPageBreak(30f)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(context.getString(R.string.item), MARGIN_LEFT, yPos, paint)
        canvas.drawText(context.getString(R.string.qty), MARGIN_LEFT + 250f, yPos, paint)
        canvas.drawText(context.getString(R.string.price), MARGIN_LEFT + 320f, yPos, paint)
        canvas.drawText(context.getString(R.string.tax), MARGIN_LEFT + 390f, yPos, paint)
        val totalLabel = context.getString(R.string.total)
        canvas.drawText(totalLabel, MARGIN_RIGHT - paint.measureText(totalLabel), yPos, paint)

        yPos += 10f
        canvas.drawLine(MARGIN_LEFT, yPos, MARGIN_RIGHT, yPos, paint)
        yPos += 20f

        // Table Items
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        for (item in data.items) {
            checkPageBreak(20f)

            // Truncate long product names
            val maxNameWidth = 240f // up to 250f (margin_left + 250 is Qty)
            val nameText =
                TextUtils.ellipsize(
                    item.productNameSnapshot,
                    android.text.TextPaint(paint),
                    maxNameWidth,
                    TextUtils.TruncateAt.END,
                ).toString()

            val quantityText =
                if (item.unitSnapshot.isBlank()) {
                    formatQuantity(item.quantity)
                } else {
                    "${formatQuantity(item.quantity)} ${item.unitSnapshot}"
                }
            canvas.drawText(nameText, MARGIN_LEFT, yPos, paint)
            canvas.drawText(quantityText, MARGIN_LEFT + 250f, yPos, paint)
            canvas.drawText(formatMoney(item.unitPriceSnapshot, settings.currencyCode), MARGIN_LEFT + 320f, yPos, paint)
            canvas.drawText(formatPercent(item.taxPercentSnapshot), MARGIN_LEFT + 390f, yPos, paint)

            val totalStr = formatMoney(item.lineTotal, settings.currencyCode)
            canvas.drawText(totalStr, MARGIN_RIGHT - paint.measureText(totalStr), yPos, paint)
            yPos += 20f
        }

        checkPageBreak(120f) // enough space for totals

        yPos += 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawLine(MARGIN_LEFT, yPos, MARGIN_RIGHT, yPos, paint)
        yPos += 20f

        // Totals
        val subtotalStr = formatMoney(data.bill.subtotal, settings.currencyCode)
        val taxTotalStr = formatMoney(data.bill.taxTotal, settings.currencyCode)
        val discountStr = formatMoney(data.bill.discount, settings.currencyCode)
        val grandTotalStr = formatMoney(data.bill.grandTotal, settings.currencyCode)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText(context.getString(R.string.pdf_subtotal), MARGIN_LEFT + 350f, yPos, paint)
        canvas.drawText(subtotalStr, MARGIN_RIGHT - paint.measureText(subtotalStr), yPos, paint)
        yPos += 20f

        canvas.drawText(context.getString(R.string.pdf_tax), MARGIN_LEFT + 350f, yPos, paint)
        canvas.drawText(taxTotalStr, MARGIN_RIGHT - paint.measureText(taxTotalStr), yPos, paint)
        yPos += 20f

        if (data.bill.discount > 0) {
            canvas.drawText(context.getString(R.string.pdf_discount), MARGIN_LEFT + 350f, yPos, paint)
            val minusDiscount = context.getString(R.string.pdf_minus_amount, discountStr)
            canvas.drawText(minusDiscount, MARGIN_RIGHT - paint.measureText(minusDiscount), yPos, paint)
            yPos += 20f
        }

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(context.getString(R.string.pdf_grand_total), MARGIN_LEFT + 350f, yPos, paint)
        canvas.drawText(grandTotalStr, MARGIN_RIGHT - paint.measureText(grandTotalStr), yPos, paint)

        yPos += 40f

        // Notes
        if (!data.bill.notes.isNullOrBlank()) {
            val noteLines = data.bill.notes.split("\n")
            checkPageBreak(noteLines.size * 15f + 20f)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText(context.getString(R.string.pdf_notes), MARGIN_LEFT, yPos, paint)
            yPos += 15f
            noteLines.forEach { line ->
                canvas.drawText(line, MARGIN_LEFT, yPos, paint)
                yPos += 15f
            }
        }

        pdfDocument.finishPage(page)

        val pdfsDir = File(context.cacheDir, "pdfs")
        if (!pdfsDir.exists()) {
            pdfsDir.mkdirs()
        }
        val file = File(pdfsDir, "Bill_${data.bill.billNumber}.pdf")

        return try {
            FileOutputStream(file).use { pdfDocument.writeTo(it) }
            pdfDocument.close()
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file,
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write PDF ${data.bill.billNumber}", e)
            pdfDocument.close()
            null
        }
    }

    private const val TAG = "PdfGenerator"
}
