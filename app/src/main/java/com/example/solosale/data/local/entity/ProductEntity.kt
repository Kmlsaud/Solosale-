package com.example.solosale.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "products",
    indices = [Index(value = ["productCode"], unique = true)]
)
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val productId: Long = 0,
    val productName: String,
    val productCode: String,
    val category: String = "General",
    val purchasePrice: Double,
    val sellingPrice: Double,
    val stockQuantity: Double,
    val minimumStockLevel: Double = 5.0,
    val unit: String = "Pcs", // Pcs, Kg, Ltr, Box, etc.
    val productImage: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val isOutOfStock: Boolean
        get() = stockQuantity <= 0

    val isLowStock: Boolean
        get() = stockQuantity > 0 && stockQuantity <= minimumStockLevel
}
