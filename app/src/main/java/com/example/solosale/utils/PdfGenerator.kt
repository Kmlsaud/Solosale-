package com.example.solosale.utils

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import com.example.solosale.data.local.entity.BusinessSettingsEntity
import com.example.solosale.data.local.entity.SaleEntity
import com.example.solosale.data.local.entity.SaleItemEntity
import java.io.File
import java.io.FileOutputStream

object PdfGenerator {

    fun generateInvoicePdf(
        context: Context,
        sale: SaleEntity,
        items: List<SaleItemEntity>,
        settings: BusinessSettingsEntity
    ): File? {
        val pdfDocument = PdfDocument()
        val pageWidth = 595 // Standard A4 width in points (72 dpi)
        val pageHeight = 842 // Standard A4 height in points
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paint = Paint().apply {
            isAntiAlias = true
        }

        // Header Background Banner
        paint.color = Color.rgb(13, 71, 161) // Deep Royal Blue
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), 90f, paint)

        // Business Name & Title
        paint.color = Color.WHITE
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 22f
        canvas.drawText(settings.businessName.uppercase(), 40f, 42f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 12f
        canvas.drawText("TAX INVOICE / SALES BILL", 40f, 62f, paint)

        // Invoice Number & Date (Right Header)
        paint.textAlign = Paint.Align.RIGHT
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("INVOICE: ${sale.invoiceNumber}", (pageWidth - 40).toFloat(), 42f, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Date: ${DateUtils.formatDateTime(sale.saleDate)}", (pageWidth - 40).toFloat(), 62f, paint)

        // Reset Alignment
        paint.textAlign = Paint.Align.LEFT

        var currentY = 115f

        // Business & Customer Info Grid
        // Left Column: Business Details
        paint.color = Color.rgb(33, 33, 33)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 11f
        canvas.drawText("BILLED FROM:", 40f, currentY, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = Color.rgb(97, 97, 97)
        paint.textSize = 10f
        canvas.drawText(settings.businessName, 40f, currentY + 16f, paint)
        canvas.drawText(settings.address, 40f, currentY + 30f, paint)
        canvas.drawText("Phone: ${settings.phone}", 40f, currentY + 44f, paint)
        if (settings.panVatNumber.isNotBlank()) {
            canvas.drawText("PAN/VAT: ${settings.panVatNumber}", 40f, currentY + 58f, paint)
        }

        // Right Column: Customer Details
        paint.color = Color.rgb(33, 33, 33)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 11f
        canvas.drawText("BILLED TO:", 340f, currentY, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = Color.rgb(97, 97, 97)
        paint.textSize = 10f
        canvas.drawText(sale.customerName, 340f, currentY + 16f, paint)
        if (sale.customerPhone.isNotBlank()) {
            canvas.drawText("Phone: ${sale.customerPhone}", 340f, currentY + 30f, paint)
        }
        canvas.drawText("Payment Method: ${sale.paymentMethod.name}", 340f, currentY + 44f, paint)

        currentY += 80f

        // Table Header
        paint.color = Color.rgb(238, 242, 246)
        canvas.drawRect(40f, currentY, (pageWidth - 40).toFloat(), currentY + 28f, paint)

        paint.color = Color.rgb(13, 71, 161)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 10f
        canvas.drawText("#", 50f, currentY + 18f, paint)
        canvas.drawText("ITEM DESCRIPTION", 80f, currentY + 18f, paint)

        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("QTY", 360f, currentY + 18f, paint)
        canvas.drawText("RATE", 440f, currentY + 18f, paint)
        canvas.drawText("AMOUNT", (pageWidth - 50).toFloat(), currentY + 18f, paint)

        currentY += 34f

        // Table Rows
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        val linePaint = Paint().apply {
            color = Color.rgb(224, 224, 224)
            strokeWidth = 0.8f
        }

        items.forEachIndexed { index, item ->
            paint.textAlign = Paint.Align.LEFT
            paint.color = Color.rgb(66, 66, 66)
            paint.textSize = 9.5f

            canvas.drawText("${index + 1}", 50f, currentY + 14f, paint)

            val displayItemName = if (item.productCode.isNotBlank()) "${item.productName} (${item.productCode})" else item.productName
            val truncatedName = if (displayItemName.length > 38) displayItemName.take(35) + "..." else displayItemName
            canvas.drawText(truncatedName, 80f, currentY + 14f, paint)

            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText("${item.quantity}", 360f, currentY + 14f, paint)
            canvas.drawText(CurrencyUtils.formatNpr(item.unitPrice, settings.currencySymbol), 440f, currentY + 14f, paint)
            canvas.drawText(CurrencyUtils.formatNpr(item.totalPrice, settings.currencySymbol), (pageWidth - 50).toFloat(), currentY + 14f, paint)

            currentY += 22f
            canvas.drawLine(40f, currentY, (pageWidth - 40).toFloat(), currentY, linePaint)
            currentY += 6f
        }

        currentY += 15f

        // Calculations & Totals (Right Aligned Box)
        val summaryX = 350f
        val summaryValX = (pageWidth - 50).toFloat()

        paint.textAlign = Paint.Align.LEFT
        paint.color = Color.rgb(97, 97, 97)
        paint.textSize = 10f

        canvas.drawText("Subtotal:", summaryX, currentY, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(CurrencyUtils.formatNpr(sale.subtotal, settings.currencySymbol), summaryValX, currentY, paint)

        if (sale.discount > 0) {
            currentY += 16f
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText("Discount:", summaryX, currentY, paint)
            paint.textAlign = Paint.Align.RIGHT
            paint.color = Color.rgb(198, 40, 40)
            canvas.drawText("- " + CurrencyUtils.formatNpr(sale.discount, settings.currencySymbol), summaryValX, currentY, paint)
            paint.color = Color.rgb(97, 97, 97)
        }

        if (sale.tax > 0) {
            currentY += 16f
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText("Tax/VAT (${settings.taxPercentage}%):", summaryX, currentY, paint)
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText("+ " + CurrencyUtils.formatNpr(sale.tax, settings.currencySymbol), summaryValX, currentY, paint)
        }

        currentY += 20f
        // Grand Total Box
        paint.color = Color.rgb(232, 245, 233)
        canvas.drawRect(summaryX - 10f, currentY - 14f, (pageWidth - 40).toFloat(), currentY + 16f, paint)

        paint.color = Color.rgb(27, 94, 32)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 12f
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("GRAND TOTAL:", summaryX, currentY + 4f, paint)

        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(CurrencyUtils.formatNpr(sale.grandTotal, settings.currencySymbol), summaryValX, currentY + 4f, paint)

        currentY += 30f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 10f
        paint.textAlign = Paint.Align.LEFT
        paint.color = Color.rgb(33, 33, 33)
        canvas.drawText("Paid Amount:", summaryX, currentY, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(CurrencyUtils.formatNpr(sale.paidAmount, settings.currencySymbol), summaryValX, currentY, paint)

        if (sale.dueAmount > 0) {
            currentY += 16f
            paint.textAlign = Paint.Align.LEFT
            paint.color = Color.rgb(198, 40, 40)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("Due / Balance:", summaryX, currentY, paint)
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText(CurrencyUtils.formatNpr(sale.dueAmount, settings.currencySymbol), summaryValX, currentY, paint)
        }

        // Footer Note
        val footerY = (pageHeight - 60).toFloat()
        paint.textAlign = Paint.Align.CENTER
        paint.color = Color.rgb(117, 117, 117)
        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        canvas.drawText(settings.footerNote, (pageWidth / 2).toFloat(), footerY, paint)
        canvas.drawText("Generated by SoloSale POS • Software for Smart Businesses", (pageWidth / 2).toFloat(), footerY + 16f, paint)

        pdfDocument.finishPage(page)

        return try {
            val invoicesDir = File(context.cacheDir, "invoices")
            if (!invoicesDir.exists()) invoicesDir.mkdirs()
            val file = File(invoicesDir, "Invoice_${sale.invoiceNumber}.pdf")
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            pdfDocument.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }

    fun sharePdf(context: Context, file: File) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share Invoice PDF"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
