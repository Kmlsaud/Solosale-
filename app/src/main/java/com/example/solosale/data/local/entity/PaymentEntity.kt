package com.example.solosale.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "payments",
    indices = [
        Index(value = ["customerId"]),
        Index(value = ["saleId"]),
        Index(value = ["paymentDate"])
    ]
)
data class PaymentEntity(
    @PrimaryKey(autoGenerate = true)
    val paymentId: Long = 0,
    val customerId: Long,
    val saleId: Long? = null,
    val amount: Double,
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val paymentDate: Long = System.currentTimeMillis(),
    val notes: String = ""
)

@Entity(
    tableName = "stock_adjustments",
    indices = [
        Index(value = ["productId"]),
        Index(value = ["date"])
    ]
)
data class StockAdjustmentEntity(
    @PrimaryKey(autoGenerate = true)
    val adjustmentId: Long = 0,
    val productId: Long,
    val productName: String,
    val previousStock: Double,
    val adjustmentQty: Double, // can be positive (+5) or negative (-3)
    val newStock: Double,
    val reason: String, // Damaged, Expired, Found Extra, Internal Use, Audit
    val date: Long = System.currentTimeMillis()
)

@Entity(tableName = "business_settings")
data class BusinessSettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    val businessName: String = "SoloSale Store",
    val ownerName: String = "Store Owner",
    val address: String = "Kathmandu, Nepal",
    val phone: String = "+977-9800000000",
    val email: String = "info@solosale.np",
    val panVatNumber: String = "123456789",
    val invoicePrefix: String = "INV-",
    val startingInvoiceNumber: Long = 1001,
    val taxPercentage: Double = 13.0, // Standard VAT in Nepal 13%
    val isTaxEnabled: Boolean = false,
    val currencySymbol: String = "Rs.",
    val footerNote: String = "Thank you for doing business with us!",
    val showLogo: Boolean = true
) {
    val panNumber: String get() = panVatNumber
    val invoiceFooterText: String get() = footerNote
}
