package com.example.solosale.data.repository

import com.example.solosale.data.local.dao.ProductDao
import com.example.solosale.data.local.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

class ProductRepository(private val productDao: ProductDao) {

    val allProducts: Flow<List<ProductEntity>> = productDao.getAllProducts()
    val lowStockProducts: Flow<List<ProductEntity>> = productDao.getLowStockProducts()
    val outOfStockProducts: Flow<List<ProductEntity>> = productDao.getOutOfStockProducts()
    val totalProductsCount: Flow<Int> = productDao.getTotalProductsCount()
    val lowStockCount: Flow<Int> = productDao.getLowStockCount()
    val outOfStockCount: Flow<Int> = productDao.getOutOfStockCount()
    val categories: Flow<List<String>> = productDao.getCategories()

    fun searchProducts(query: String): Flow<List<ProductEntity>> = productDao.searchProducts(query)

    suspend fun getProductById(id: Long): ProductEntity? = productDao.getProductById(id)

    suspend fun getProductByCode(code: String): ProductEntity? = productDao.getProductByCode(code)

    suspend fun insertProduct(product: ProductEntity): Result<Long> {
        return try {
            if (product.productName.isBlank()) {
                return Result.failure(IllegalArgumentException("Product name cannot be empty"))
            }
            if (product.sellingPrice < 0 || product.purchasePrice < 0) {
                return Result.failure(IllegalArgumentException("Prices cannot be negative"))
            }
            val existing = productDao.getProductByCode(product.productCode)
            if (existing != null && existing.productId != product.productId) {
                return Result.failure(IllegalArgumentException("Product code '${product.productCode}' already exists"))
            }
            val id = productDao.insertProduct(product)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProduct(product: ProductEntity): Result<Unit> {
        return try {
            productDao.updateProduct(product.copy(updatedAt = System.currentTimeMillis()))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteProduct(product: ProductEntity): Result<Unit> {
        return try {
            productDao.deleteProduct(product)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
