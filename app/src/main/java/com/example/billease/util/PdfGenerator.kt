package com.example.billease.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.billease.data.BillWithItemsAndPerson
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfGenerator {

    fun generatePdf(context: Context, data: BillWithItemsAndPerson): Uri? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size at 72 PPI
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        drawBill(canvas, data)

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
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }

    private fun drawBill(canvas: Canvas, data: BillWithItemsAndPerson) {
        val paint = Paint().apply {
            color = Color.BLACK
            textSize = 12f
            isAntiAlias = true
        }

        var yPos = 50f
        val leftMargin = 50f
        val rightMargin = 545f

        // Title
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 24f
        canvas.drawText("INVOICE", leftMargin, yPos, paint)
        yPos += 30f

        // Bill Info
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 12f
        canvas.drawText("Bill No: ${data.bill.billNumber}", leftMargin, yPos, paint)
        val dateStr = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(data.bill.billDate))
        canvas.drawText("Date: $dateStr", leftMargin, yPos + 15f, paint)
        
        yPos += 45f

        // Bill To
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Bill To:", leftMargin, yPos, paint)
        yPos += 15f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText(data.person.name, leftMargin, yPos, paint)
        yPos += 15f
        canvas.drawText(data.person.phone, leftMargin, yPos, paint)
        yPos += 15f
        if (!data.person.gstNumber.isNullOrBlank()) {
            canvas.drawText("GST: ${data.person.gstNumber}", leftMargin, yPos, paint)
            yPos += 15f
        }

        yPos += 30f

        // Table Header
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Item", leftMargin, yPos, paint)
        canvas.drawText("Qty", leftMargin + 250f, yPos, paint)
        canvas.drawText("Price", leftMargin + 320f, yPos, paint)
        canvas.drawText("Tax", leftMargin + 390f, yPos, paint)
        canvas.drawText("Total", rightMargin - paint.measureText("Total"), yPos, paint)
        
        yPos += 10f
        canvas.drawLine(leftMargin, yPos, rightMargin, yPos, paint)
        yPos += 20f

        // Table Items
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        for (item in data.items) {
            canvas.drawText(item.productNameSnapshot, leftMargin, yPos, paint)
            canvas.drawText(item.quantity.toString(), leftMargin + 250f, yPos, paint)
            canvas.drawText(String.format(Locale.getDefault(), "%.2f", item.unitPriceSnapshot), leftMargin + 320f, yPos, paint)
            canvas.drawText(String.format(Locale.getDefault(), "%.1f%%", item.taxPercentSnapshot), leftMargin + 390f, yPos, paint)
            
            val totalStr = String.format(Locale.getDefault(), "%.2f", item.lineTotal)
            canvas.drawText(totalStr, rightMargin - paint.measureText(totalStr), yPos, paint)
            yPos += 20f
        }

        yPos += 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawLine(leftMargin, yPos, rightMargin, yPos, paint)
        yPos += 20f

        // Totals
        val subtotalStr = String.format(Locale.getDefault(), "%.2f", data.bill.subtotal)
        val taxTotalStr = String.format(Locale.getDefault(), "%.2f", data.bill.taxTotal)
        val discountStr = String.format(Locale.getDefault(), "%.2f", data.bill.discount)
        val grandTotalStr = String.format(Locale.getDefault(), "%.2f", data.bill.grandTotal)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Subtotal:", leftMargin + 350f, yPos, paint)
        canvas.drawText(subtotalStr, rightMargin - paint.measureText(subtotalStr), yPos, paint)
        yPos += 20f

        canvas.drawText("Tax:", leftMargin + 350f, yPos, paint)
        canvas.drawText(taxTotalStr, rightMargin - paint.measureText(taxTotalStr), yPos, paint)
        yPos += 20f

        if (data.bill.discount > 0) {
            canvas.drawText("Discount:", leftMargin + 350f, yPos, paint)
            canvas.drawText("-$discountStr", rightMargin - paint.measureText("-$discountStr"), yPos, paint)
            yPos += 20f
        }

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Grand Total:", leftMargin + 350f, yPos, paint)
        canvas.drawText(grandTotalStr, rightMargin - paint.measureText(grandTotalStr), yPos, paint)
        
        yPos += 40f
        
        // Notes
        if (!data.bill.notes.isNullOrBlank()) {
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("Notes:", leftMargin, yPos, paint)
            yPos += 15f
            data.bill.notes.split("\n").forEach { line ->
                canvas.drawText(line, leftMargin, yPos, paint)
                yPos += 15f
            }
        }
    }
}
