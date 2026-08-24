package com.example.solosale.data.repository

import androidx.room.withTransaction
import com.example.solosale.data.local.AppDatabase
import com.example.solosale.data.local.dao.SaleDao
import com.example.solosale.data.local.dao.SaleItemDao
import com.example.solosale.data.local.dao.TopProductSummary
import com.example.solosale.data.local.entity.PaymentEntity
import com.example.solosale.data.local.entity.PaymentMethod
import com.example.solosale.data.local.entity.SaleEntity
import com.example.solosale.data.local.entity.SaleItemEntity
import com.example.solosale.utils.DateUtils
import com.example.solosale.utils.InvoiceNumberGenerator
import kotlinx.coroutines.flow.Flow

data class SaleWithItems(
    val sale: SaleEntity,
    val items: List<SaleItemEntity>
)

data class CartItem(
    val productId: Long,
    val productName: String,
    val productCode: String,
    val unitPrice: Double,
    val purchasePrice: Double,
    val quantity: Double,
    val unit: String = "Pcs",
    val availableStock: Double
) {
    val totalPrice: Double
        get() = quantity * unitPrice
}

class SalesRepository(
    private val database: AppDatabase
) {
    private val saleDao: SaleDao = database.saleDao()
    private val saleItemDao: SaleItemDao = database.saleItemDao()
    private val productDao = database.productDao()
    private val paymentDao = database.paymentDao()
    private val settingsDao = database.settingsDao()

    val allSales: Flow<List<SaleEntity>> = saleDao.getAllSales()
    val totalSalesCount: Flow<Int> = saleDao.getTotalSalesCount()
    val totalRevenue: Flow<Double> = saleDao.getTotalRevenue()
    val totalDue: Flow<Double> = saleDao.getTotalDue()
    val recentSales: Flow<List<SaleEntity>> = saleDao.getRecentSales(10)
    val salesWithDue: Flow<List<SaleEntity>> = saleDao.getSalesWithDue()

    fun getSalesForCustomer(customerId: Long): Flow<List<SaleEntity>> = saleDao.getSalesForCustomer(customerId)

    fun getItemsForSale(saleId: Long): Flow<List<SaleItemEntity>> = saleItemDao.getItemsForSale(saleId)

    suspend fun getSaleWithItems(saleId: Long): SaleWithItems? {
        val sale = saleDao.getSaleById(saleId) ?: return null
        val items = saleItemDao.getItemsForSaleDirect(saleId)
        return SaleWithItems(sale, items)
    }

    suspend fun generateNextInvoiceNumber(): String {
        val settings = settingsDao.getSettings()
        val prefix = settings?.invoicePrefix ?: "INV-"
        val startNum = settings?.startingInvoiceNumber ?: 1001
        val lastId = saleDao.getLastSaleId() ?: 0
        return InvoiceNumberGenerator.generate(prefix, lastId, startNum)
    }

    suspend fun createSale(
        invoiceNumber: String,
        customerId: Long?,
        customerName: String,
        customerPhone: String,
        userId: Long,
        items: List<CartItem>,
        discount: Double,
        isTaxEnabled: Boolean,
        taxPercentage: Double,
        paidAmount: Double,
        paymentMethod: PaymentMethod,
        notes: String = ""
    ): Result<Long> {
        if (items.isEmpty()) {
            return Result.failure(IllegalArgumentException("Cart cannot be empty"))
        }

        // Validate customer for credit sales
        if (paymentMethod == PaymentMethod.CREDIT && (customerId == null || customerName.isBlank())) {
            return Result.failure(IllegalArgumentException("Customer is required for credit sales"))
        }

        // Calculate totals
        val subtotal = items.sumOf { it.totalPrice }
        val taxAmount = if (isTaxEnabled) ((subtotal - discount) * (taxPercentage / 100.0)).coerceAtLeast(0.0) else 0.0
        val grandTotal = (subtotal - discount + taxAmount).coerceAtLeast(0.0)
        val actualPaid = paidAmount.coerceAtMost(grandTotal).coerceAtLeast(0.0)
        val dueAmount = (grandTotal - actualPaid).coerceAtLeast(0.0)

        return try {
            database.withTransaction {
                // 1. Check stock availability
                for (item in items) {
                    val product = productDao.getProductById(item.productId)
                        ?: throw IllegalStateException("Product ${item.productName} not found")
                    if (product.stockQuantity < item.quantity) {
                        throw IllegalStateException("Insufficient stock for ${item.productName}. Available: ${product.stockQuantity}")
                    }
                }

                // 2. Insert Sale
                val saleEntity = SaleEntity(
                    invoiceNumber = invoiceNumber,
                    customerId = customerId,
                    customerName = if (customerName.isNotBlank()) customerName else "Walk-in Customer",
                    customerPhone = customerPhone,
                    userId = userId,
                    subtotal = subtotal,
                    discount = discount,
                    tax = taxAmount,
                    grandTotal = grandTotal,
                    paidAmount = actualPaid,
                    dueAmount = dueAmount,
                    paymentMethod = paymentMethod,
                    saleDate = System.currentTimeMillis(),
                    notes = notes
                )
                val saleId = saleDao.insertSale(saleEntity)

                // 3. Insert Sale Items
                val itemEntities = items.map {
                    SaleItemEntity(
                        saleId = saleId,
                        productId = it.productId,
                        productName = it.productName,
                        productCode = it.productCode,
                        quantity = it.quantity,
                        unitPrice = it.unitPrice,
                        purchasePrice = it.purchasePrice,
                        discount = 0.0,
                        totalPrice = it.totalPrice
                    )
                }
                saleItemDao.insertSaleItems(itemEntities)

                // 4. Update Product Stock (- Quantity)
                for (item in items) {
                    productDao.updateStock(item.productId, -item.quantity)
                }

                // 5. If paidAmount > 0 and customerId != null, record payment
                if (actualPaid > 0 && customerId != null) {
                    val payment = PaymentEntity(
                        customerId = customerId,
                        saleId = saleId,
                        amount = actualPaid,
                        paymentMethod = paymentMethod,
                        paymentDate = System.currentTimeMillis(),
                        notes = "Payment for Invoice $invoiceNumber"
                    )
                    paymentDao.insertPayment(payment)
                }

                Result.success(saleId)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getTodayRevenue(): Flow<Double> {
        val start = DateUtils.getStartOfDay()
        val end = DateUtils.getEndOfDay()
        return saleDao.getRevenueBetween(start, end)
    }

    fun getTopSellingProducts(limit: Int = 5): Flow<List<TopProductSummary>> {
        val start = DateUtils.getStartOfMonth()
        val end = DateUtils.getEndOfDay()
        return saleItemDao.getTopSellingProducts(start, end, limit)
    }
}
