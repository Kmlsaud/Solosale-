package com.example.solosale.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.solosale.data.local.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY productName ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE productId = :productId LIMIT 1")
    suspend fun getProductById(productId: Long): ProductEntity?

    @Query("SELECT * FROM products WHERE productCode = :code LIMIT 1")
    suspend fun getProductByCode(code: String): ProductEntity?

    @Query("SELECT * FROM products WHERE productName LIKE '%' || :query || '%' OR productCode LIKE '%' || :query || '%' ORDER BY productName ASC")
    fun searchProducts(query: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE stockQuantity <= minimumStockLevel AND stockQuantity > 0")
    fun getLowStockProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE stockQuantity <= 0")
    fun getOutOfStockProducts(): Flow<List<ProductEntity>>

    @Query("SELECT COUNT(*) FROM products")
    fun getTotalProductsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM products WHERE stockQuantity <= minimumStockLevel AND stockQuantity > 0")
    fun getLowStockCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM products WHERE stockQuantity <= 0")
    fun getOutOfStockCount(): Flow<Int>

    @Query("SELECT DISTINCT category FROM products ORDER BY category ASC")
    fun getCategories(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    @Query("UPDATE products SET stockQuantity = stockQuantity + :quantityChange, updatedAt = :timestamp WHERE productId = :productId")
    suspend fun updateStock(productId: Long, quantityChange: Double, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE products SET stockQuantity = :newStock, updatedAt = :timestamp WHERE productId = :productId")
    suspend fun setStock(productId: Long, newStock: Double, timestamp: Long = System.currentTimeMillis())
}
