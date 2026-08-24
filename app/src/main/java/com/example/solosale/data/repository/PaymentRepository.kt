package com.example.solosale.data.repository

import androidx.room.withTransaction
import com.example.solosale.data.local.AppDatabase
import com.example.solosale.data.local.entity.PaymentEntity
import com.example.solosale.data.local.entity.PaymentMethod
import com.example.solosale.data.local.entity.StockAdjustmentEntity
import kotlinx.coroutines.flow.Flow

class PaymentRepository(
    private val database: AppDatabase
) {
    private val paymentDao = database.paymentDao()
    private val saleDao = database.saleDao()

    val allPayments: Flow<List<PaymentEntity>> = paymentDao.getAllPayments()

    fun getPaymentsForCustomer(customerId: Long): Flow<List<PaymentEntity>> =
        paymentDao.getPaymentsForCustomer(customerId)

    suspend fun collectDuePayment(
        customerId: Long,
        amountToPay: Double,
        paymentMethod: PaymentMethod,
        notes: String
    ): Result<Unit> {
        if (amountToPay <= 0) {
            return Result.failure(IllegalArgumentException("Payment amount must be greater than 0"))
        }

        return try {
            database.withTransaction {
                val unpaidSales = saleDao.getUnpaidSalesForCustomer(customerId)
                val totalDue = unpaidSales.sumOf { it.dueAmount }

                if (amountToPay > totalDue + 0.01) {
                    throw IllegalArgumentException("Payment (Rs. $amountToPay) cannot exceed remaining due amount (Rs. $totalDue)")
                }

                var remainingPayment = amountToPay

                // Apply payment to oldest unpaid sales first
                for (sale in unpaidSales) {
                    if (remainingPayment <= 0) break
                    val payableForThisSale = minOf(remainingPayment, sale.dueAmount)
                    saleDao.applyPaymentToSale(sale.saleId, payableForThisSale)

                    val payment = PaymentEntity(
                        customerId = customerId,
                        saleId = sale.saleId,
                        amount = payableForThisSale,
                        paymentMethod = paymentMethod,
                        paymentDate = System.currentTimeMillis(),
                        notes = if (notes.isNotBlank()) "$notes (For Inv ${sale.invoiceNumber})" else "Due payment for Invoice ${sale.invoiceNumber}"
                    )
                    paymentDao.insertPayment(payment)

                    remainingPayment -= payableForThisSale
                }

                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class InventoryRepository(
    private val database: AppDatabase
) {
    private val productDao = database.productDao()
    private val stockAdjustmentDao = database.stockAdjustmentDao()

    val allAdjustments: Flow<List<StockAdjustmentEntity>> = stockAdjustmentDao.getAllAdjustments()

    fun getAdjustmentsForProduct(productId: Long): Flow<List<StockAdjustmentEntity>> =
        stockAdjustmentDao.getAdjustmentsForProduct(productId)

    suspend fun adjustStock(
        productId: Long,
        adjustmentQuantity: Double, // e.g. -5 for damaged or +10 for found stock
        reason: String
    ): Result<Unit> {
        if (reason.isBlank()) {
            return Result.failure(IllegalArgumentException("Adjustment reason is required"))
        }

        return try {
            database.withTransaction {
                val product = productDao.getProductById(productId)
                    ?: throw IllegalStateException("Product not found")

                val newStock = product.stockQuantity + adjustmentQuantity
                if (newStock < 0) {
                    throw IllegalArgumentException("Resulting stock cannot be negative (Current: ${product.stockQuantity})")
                }

                // 1. Update product stock
                productDao.setStock(productId, newStock)

                // 2. Insert stock adjustment log
                val adjustment = StockAdjustmentEntity(
                    productId = productId,
                    productName = product.productName,
                    previousStock = product.stockQuantity,
                    adjustmentQty = adjustmentQuantity,
                    newStock = newStock,
                    reason = reason,
                    date = System.currentTimeMillis()
                )
                stockAdjustmentDao.insertAdjustment(adjustment)

                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
