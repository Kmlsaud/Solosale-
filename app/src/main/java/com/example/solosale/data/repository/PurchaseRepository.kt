package com.example.solosale.data.repository

import androidx.room.withTransaction
import com.example.solosale.data.local.AppDatabase
import com.example.solosale.data.local.dao.PurchaseDao
import com.example.solosale.data.local.dao.PurchaseItemDao
import com.example.solosale.data.local.dao.SupplierDao
import com.example.solosale.data.local.entity.PurchaseEntity
import com.example.solosale.data.local.entity.PurchaseItemEntity
import com.example.solosale.data.local.entity.SupplierEntity
import kotlinx.coroutines.flow.Flow

data class PurchaseItemInput(
    val productId: Long,
    val productName: String,
    val quantity: Double,
    val purchasePrice: Double
) {
    val totalPrice: Double
        get() = quantity * purchasePrice
}

data class PurchaseWithItems(
    val purchase: PurchaseEntity,
    val items: List<PurchaseItemEntity>
)

class PurchaseRepository(
    private val database: AppDatabase
) {
    private val purchaseDao: PurchaseDao = database.purchaseDao()
    private val purchaseItemDao: PurchaseItemDao = database.purchaseItemDao()
    private val productDao = database.productDao()

    val allPurchases: Flow<List<PurchaseEntity>> = purchaseDao.getAllPurchases()
    val totalPurchaseAmount: Flow<Double> = purchaseDao.getTotalPurchaseAmount()

    fun getPurchasesForSupplier(supplierId: Long): Flow<List<PurchaseEntity>> =
        purchaseDao.getPurchasesForSupplier(supplierId)

    fun getItemsForPurchase(purchaseId: Long): Flow<List<PurchaseItemEntity>> =
        purchaseItemDao.getItemsForPurchase(purchaseId)

    suspend fun getPurchaseWithItems(purchaseId: Long): PurchaseWithItems? {
        val purchase = purchaseDao.getPurchaseById(purchaseId) ?: return null
        val items = purchaseItemDao.getItemsForPurchaseDirect(purchaseId)
        return PurchaseWithItems(purchase, items)
    }

    suspend fun createPurchase(
        supplierId: Long?,
        supplierName: String,
        billNumber: String,
        items: List<PurchaseItemInput>,
        paidAmount: Double,
        notes: String
    ): Result<Long> {
        if (items.isEmpty()) {
            return Result.failure(IllegalArgumentException("Purchase items cannot be empty"))
        }

        val totalAmount = items.sumOf { it.totalPrice }

        return try {
            database.withTransaction {
                // 1. Insert Purchase
                val purchase = PurchaseEntity(
                    supplierId = supplierId,
                    supplierName = if (supplierName.isNotBlank()) supplierName else "General Supplier",
                    billNumber = billNumber,
                    purchaseDate = System.currentTimeMillis(),
                    totalAmount = totalAmount,
                    paidAmount = paidAmount,
                    notes = notes
                )
                val purchaseId = purchaseDao.insertPurchase(purchase)

                // 2. Insert Items
                val itemEntities = items.map {
                    PurchaseItemEntity(
                        purchaseId = purchaseId,
                        productId = it.productId,
                        productName = it.productName,
                        quantity = it.quantity,
                        purchasePrice = it.purchasePrice,
                        totalPrice = it.totalPrice
                    )
                }
                purchaseItemDao.insertPurchaseItems(itemEntities)

                // 3. Update stock for each product (+ Quantity)
                for (item in items) {
                    productDao.updateStock(item.productId, item.quantity)
                    // Also update purchase price on product if changed
                    val currentProduct = productDao.getProductById(item.productId)
                    if (currentProduct != null && currentProduct.purchasePrice != item.purchasePrice) {
                        productDao.updateProduct(currentProduct.copy(purchasePrice = item.purchasePrice))
                    }
                }

                Result.success(purchaseId)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class SupplierRepository(private val supplierDao: SupplierDao) {
    val allSuppliers: Flow<List<SupplierEntity>> = supplierDao.getAllSuppliers()

    fun searchSuppliers(query: String): Flow<List<SupplierEntity>> = supplierDao.searchSuppliers(query)

    suspend fun getSupplierById(id: Long): SupplierEntity? = supplierDao.getSupplierById(id)

    suspend fun insertSupplier(supplier: SupplierEntity): Long = supplierDao.insertSupplier(supplier)

    suspend fun updateSupplier(supplier: SupplierEntity) = supplierDao.updateSupplier(supplier)

    suspend fun deleteSupplier(supplier: SupplierEntity) = supplierDao.deleteSupplier(supplier)
}
