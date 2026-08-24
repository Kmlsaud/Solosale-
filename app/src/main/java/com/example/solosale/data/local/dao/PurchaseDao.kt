package com.example.solosale.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.solosale.data.local.entity.PurchaseEntity
import com.example.solosale.data.local.entity.PurchaseItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PurchaseDao {
    @Query("SELECT * FROM purchases ORDER BY purchaseDate DESC")
    fun getAllPurchases(): Flow<List<PurchaseEntity>>

    @Query("SELECT * FROM purchases WHERE purchaseId = :purchaseId LIMIT 1")
    suspend fun getPurchaseById(purchaseId: Long): PurchaseEntity?

    @Query("SELECT * FROM purchases WHERE supplierId = :supplierId ORDER BY purchaseDate DESC")
    fun getPurchasesForSupplier(supplierId: Long): Flow<List<PurchaseEntity>>

    @Query("SELECT COALESCE(SUM(totalAmount), 0.0) FROM purchases")
    fun getTotalPurchaseAmount(): Flow<Double>

    @Query("SELECT COALESCE(SUM(totalAmount), 0.0) FROM purchases WHERE purchaseDate >= :startDate AND purchaseDate <= :endDate")
    fun getPurchaseAmountBetween(startDate: Long, endDate: Long): Flow<Double>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchase(purchase: PurchaseEntity): Long

    @Update
    suspend fun updatePurchase(purchase: PurchaseEntity)

    @Delete
    suspend fun deletePurchase(purchase: PurchaseEntity)
}

@Dao
interface PurchaseItemDao {
    @Query("SELECT * FROM purchase_items WHERE purchaseId = :purchaseId")
    fun getItemsForPurchase(purchaseId: Long): Flow<List<PurchaseItemEntity>>

    @Query("SELECT * FROM purchase_items WHERE purchaseId = :purchaseId")
    suspend fun getItemsForPurchaseDirect(purchaseId: Long): List<PurchaseItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchaseItems(items: List<PurchaseItemEntity>)
}
