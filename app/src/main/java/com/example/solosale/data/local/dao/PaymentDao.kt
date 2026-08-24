package com.example.solosale.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.solosale.data.local.entity.BusinessSettingsEntity
import com.example.solosale.data.local.entity.PaymentEntity
import com.example.solosale.data.local.entity.StockAdjustmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments ORDER BY paymentDate DESC")
    fun getAllPayments(): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE customerId = :customerId ORDER BY paymentDate DESC")
    fun getPaymentsForCustomer(customerId: Long): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE saleId = :saleId ORDER BY paymentDate DESC")
    fun getPaymentsForSale(saleId: Long): Flow<List<PaymentEntity>>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM payments WHERE customerId = :customerId")
    fun getTotalPaidByCustomer(customerId: Long): Flow<Double>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentEntity): Long
}

@Dao
interface StockAdjustmentDao {
    @Query("SELECT * FROM stock_adjustments ORDER BY date DESC")
    fun getAllAdjustments(): Flow<List<StockAdjustmentEntity>>

    @Query("SELECT * FROM stock_adjustments WHERE productId = :productId ORDER BY date DESC")
    fun getAdjustmentsForProduct(productId: Long): Flow<List<StockAdjustmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdjustment(adjustment: StockAdjustmentEntity): Long
}

@Dao
interface SettingsDao {
    @Query("SELECT * FROM business_settings WHERE id = 1 LIMIT 1")
    fun getSettingsFlow(): Flow<BusinessSettingsEntity?>

    @Query("SELECT * FROM business_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettings(): BusinessSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(settings: BusinessSettingsEntity)
}
