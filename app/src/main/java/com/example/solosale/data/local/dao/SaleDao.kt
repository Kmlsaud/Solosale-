package com.example.solosale.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.solosale.data.local.entity.SaleEntity
import com.example.solosale.data.local.entity.SaleItemEntity
import kotlinx.coroutines.flow.Flow

data class TopProductSummary(
    val productId: Long,
    val productName: String,
    val totalQtySold: Double,
    val totalRevenue: Double
)

data class SalesSummary(
    val totalSalesCount: Int,
    val totalRevenue: Double,
    val totalDue: Double,
    val totalPaid: Double
)

@Dao
interface SaleDao {
    @Query("SELECT * FROM sales ORDER BY saleDate DESC")
    fun getAllSales(): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales WHERE saleId = :saleId LIMIT 1")
    suspend fun getSaleById(saleId: Long): SaleEntity?

    @Query("SELECT * FROM sales WHERE invoiceNumber = :invoiceNumber LIMIT 1")
    suspend fun getSaleByInvoiceNumber(invoiceNumber: String): SaleEntity?

    @Query("SELECT * FROM sales WHERE saleDate >= :startDate AND saleDate <= :endDate ORDER BY saleDate DESC")
    fun getSalesBetweenDates(startDate: Long, endDate: Long): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales WHERE saleDate >= :startDate AND saleDate <= :endDate ORDER BY saleDate DESC")
    suspend fun getSalesBetweenDatesDirect(startDate: Long, endDate: Long): List<SaleEntity>

    @Query("SELECT * FROM sales WHERE customerId = :customerId ORDER BY saleDate DESC")
    fun getSalesForCustomer(customerId: Long): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales WHERE dueAmount > 0 ORDER BY saleDate DESC")
    fun getSalesWithDue(): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales WHERE customerId = :customerId AND dueAmount > 0 ORDER BY saleDate ASC")
    suspend fun getUnpaidSalesForCustomer(customerId: Long): List<SaleEntity>

    @Query("SELECT COUNT(*) FROM sales")
    fun getTotalSalesCount(): Flow<Int>

    @Query("SELECT COALESCE(SUM(grandTotal), 0.0) FROM sales WHERE saleDate >= :startDate AND saleDate <= :endDate")
    fun getRevenueBetween(startDate: Long, endDate: Long): Flow<Double>

    @Query("SELECT COALESCE(SUM(grandTotal), 0.0) FROM sales")
    fun getTotalRevenue(): Flow<Double>

    @Query("SELECT COALESCE(SUM(dueAmount), 0.0) FROM sales")
    fun getTotalDue(): Flow<Double>

    @Query("SELECT * FROM sales ORDER BY saleDate DESC LIMIT :limit")
    fun getRecentSales(limit: Int = 10): Flow<List<SaleEntity>>

    @Query("SELECT MAX(saleId) FROM sales")
    suspend fun getLastSaleId(): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: SaleEntity): Long

    @Update
    suspend fun updateSale(sale: SaleEntity)

    @Delete
    suspend fun deleteSale(sale: SaleEntity)

    @Query("UPDATE sales SET paidAmount = paidAmount + :paidAmountAdd, dueAmount = dueAmount - :paidAmountAdd WHERE saleId = :saleId")
    suspend fun applyPaymentToSale(saleId: Long, paidAmountAdd: Double)
}

@Dao
interface SaleItemDao {
    @Query("SELECT * FROM sale_items WHERE saleId = :saleId")
    fun getItemsForSale(saleId: Long): Flow<List<SaleItemEntity>>

    @Query("SELECT * FROM sale_items WHERE saleId = :saleId")
    suspend fun getItemsForSaleDirect(saleId: Long): List<SaleItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaleItems(items: List<SaleItemEntity>)

    @Query("""
        SELECT si.productId, si.productName, SUM(si.quantity) as totalQtySold, SUM(si.totalPrice) as totalRevenue
        FROM sale_items si
        INNER JOIN sales s ON si.saleId = s.saleId
        WHERE s.saleDate >= :startDate AND s.saleDate <= :endDate
        GROUP BY si.productId, si.productName
        ORDER BY totalQtySold DESC
        LIMIT :limit
    """)
    fun getTopSellingProducts(startDate: Long, endDate: Long, limit: Int = 5): Flow<List<TopProductSummary>>
}
