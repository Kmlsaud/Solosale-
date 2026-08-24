package com.example.solosale.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "purchases",
    indices = [
        Index(value = ["supplierId"]),
        Index(value = ["purchaseDate"])
    ]
)
data class PurchaseEntity(
    @PrimaryKey(autoGenerate = true)
    val purchaseId: Long = 0,
    val supplierId: Long? = null,
    val supplierName: String = "General Supplier",
    val billNumber: String = "",
    val purchaseDate: Long = System.currentTimeMillis(),
    val totalAmount: Double,
    val paidAmount: Double = 0.0,
    val notes: String = ""
)

@Entity(
    tableName = "purchase_items",
    foreignKeys = [
        ForeignKey(
            entity = PurchaseEntity::class,
            parentColumns = ["purchaseId"],
            childColumns = ["purchaseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["purchaseId"]),
        Index(value = ["productId"])
    ]
)
data class PurchaseItemEntity(
    @PrimaryKey(autoGenerate = true)
    val purchaseItemId: Long = 0,
    val purchaseId: Long,
    val productId: Long,
    val productName: String,
    val quantity: Double,
    val purchasePrice: Double,
    val totalPrice: Double
)
