package com.example.solosale.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true)
    val customerId: Long = 0,
    val customerName: String,
    val phone: String = "",
    val address: String = "",
    val email: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "suppliers")
data class SupplierEntity(
    @PrimaryKey(autoGenerate = true)
    val supplierId: Long = 0,
    val supplierName: String,
    val phone: String = "",
    val address: String = "",
    val email: String = "",
    val contactPerson: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
