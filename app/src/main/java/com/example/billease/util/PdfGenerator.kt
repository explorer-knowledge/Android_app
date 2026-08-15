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
import com.example.billease.data.AppSettings
import com.example.billease.data.BillWithItemsAndPerson
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

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
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()

        var page = pdfDocument.startPage(pageInfo)
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
                page = pdfDocument.startPage(pageInfo)
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
        val bName = settings.businessName.ifBlank { "INVOICE" }
        canvas.drawText(bName, MARGIN_LEFT, yPos, paint)
        yPos += 20f

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 12f
        if (settings.address.isNotBlank()) {
            settings.address.split("\n").forEach {
                canvas.drawText(it, MARGIN_LEFT, yPos, paint)
                yPos += 15f
            }
            yPos += 10f
        }

        // Bill Info
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Bill No: ${data.bill.billNumber}", MARGIN_LEFT, yPos, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        val dateStr = formatDate(data.bill.billDate)
        canvas.drawText("Date: $dateStr", MARGIN_LEFT, yPos + 15f, paint)

        yPos += 45f

        // Bill To
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Bill To:", MARGIN_LEFT, yPos, paint)
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
            canvas.drawText("GST: ${data.person.gstNumber}", MARGIN_LEFT, yPos, paint)
            yPos += 15f
        }

        yPos += 30f

        // Table Header
        checkPageBreak(30f)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Item", MARGIN_LEFT, yPos, paint)
        canvas.drawText("Qty", MARGIN_LEFT + 250f, yPos, paint)
        canvas.drawText("Price", MARGIN_LEFT + 320f, yPos, paint)
        canvas.drawText("Tax", MARGIN_LEFT + 390f, yPos, paint)
        canvas.drawText("Total", MARGIN_RIGHT - paint.measureText("Total"), yPos, paint)

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

            canvas.drawText(nameText, MARGIN_LEFT, yPos, paint)
            canvas.drawText(
                if (item.unitSnapshot.isBlank()) item.quantity.toString() else "${item.quantity} ${item.unitSnapshot}",
                MARGIN_LEFT + 250f,
                yPos,
                paint,
            )
            canvas.drawText(formatMoney(item.unitPriceSnapshot, settings.currencyCode), MARGIN_LEFT + 320f, yPos, paint)
            canvas.drawText(String.format(Locale.getDefault(), "%.1f%%", item.taxPercentSnapshot), MARGIN_LEFT + 390f, yPos, paint)

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
        canvas.drawText("Subtotal:", MARGIN_LEFT + 350f, yPos, paint)
        canvas.drawText(subtotalStr, MARGIN_RIGHT - paint.measureText(subtotalStr), yPos, paint)
        yPos += 20f

        canvas.drawText("Tax:", MARGIN_LEFT + 350f, yPos, paint)
        canvas.drawText(taxTotalStr, MARGIN_RIGHT - paint.measureText(taxTotalStr), yPos, paint)
        yPos += 20f

        if (data.bill.discount > 0) {
            canvas.drawText("Discount:", MARGIN_LEFT + 350f, yPos, paint)
            canvas.drawText("-$discountStr", MARGIN_RIGHT - paint.measureText("-$discountStr"), yPos, paint)
            yPos += 20f
        }

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Grand Total:", MARGIN_LEFT + 350f, yPos, paint)
        canvas.drawText(grandTotalStr, MARGIN_RIGHT - paint.measureText(grandTotalStr), yPos, paint)

        yPos += 40f

        // Notes
        if (!data.bill.notes.isNullOrBlank()) {
            val noteLines = data.bill.notes.split("\n")
            checkPageBreak(noteLines.size * 15f + 20f)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("Notes:", MARGIN_LEFT, yPos, paint)
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
            pdfDocument.writeTo(FileOutputStream(file))
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
