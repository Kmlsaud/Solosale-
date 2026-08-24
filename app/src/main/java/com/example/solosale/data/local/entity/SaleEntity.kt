package com.example.solosale.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class PaymentMethod {
    CASH,
    DIGITAL, // eSewa, Khalti, Fonepay, Card
    CREDIT
}

@Entity(
    tableName = "sales",
    indices = [
        Index(value = ["invoiceNumber"], unique = true),
        Index(value = ["customerId"]),
        Index(value = ["saleDate"])
    ]
)
data class SaleEntity(
    @PrimaryKey(autoGenerate = true)
    val saleId: Long = 0,
    val invoiceNumber: String,
    val customerId: Long? = null,
    val customerName: String = "Walk-in Customer",
    val customerPhone: String = "",
    val userId: Long,
    val subtotal: Double,
    val discount: Double = 0.0,
    val tax: Double = 0.0,
    val grandTotal: Double,
    val paidAmount: Double,
    val dueAmount: Double = 0.0,
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val saleDate: Long = System.currentTimeMillis(),
    val notes: String = ""
)

@Entity(
    tableName = "sale_items",
    foreignKeys = [
        ForeignKey(
            entity = SaleEntity::class,
            parentColumns = ["saleId"],
            childColumns = ["saleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["saleId"]),
        Index(value = ["productId"])
    ]
)
data class SaleItemEntity(
    @PrimaryKey(autoGenerate = true)
    val saleItemId: Long = 0,
    val saleId: Long,
    val productId: Long,
    val productName: String,
    val productCode: String,
    val quantity: Double,
    val unitPrice: Double,
    val purchasePrice: Double = 0.0,
    val discount: Double = 0.0,
    val totalPrice: Double
)
