package com.example.solosale.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.solosale.data.local.entity.CustomerEntity
import com.example.solosale.data.local.entity.SupplierEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers ORDER BY customerName ASC")
    fun getAllCustomers(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE customerId = :customerId LIMIT 1")
    suspend fun getCustomerById(customerId: Long): CustomerEntity?

    @Query("SELECT * FROM customers WHERE customerName LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%' ORDER BY customerName ASC")
    fun searchCustomers(query: String): Flow<List<CustomerEntity>>

    @Query("SELECT COUNT(*) FROM customers")
    fun getCustomerCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity): Long

    @Update
    suspend fun updateCustomer(customer: CustomerEntity)

    @Delete
    suspend fun deleteCustomer(customer: CustomerEntity)
}

@Dao
interface SupplierDao {
    @Query("SELECT * FROM suppliers ORDER BY supplierName ASC")
    fun getAllSuppliers(): Flow<List<SupplierEntity>>

    @Query("SELECT * FROM suppliers WHERE supplierId = :supplierId LIMIT 1")
    suspend fun getSupplierById(supplierId: Long): SupplierEntity?

    @Query("SELECT * FROM suppliers WHERE supplierName LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%' ORDER BY supplierName ASC")
    fun searchSuppliers(query: String): Flow<List<SupplierEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplier(supplier: SupplierEntity): Long

    @Update
    suspend fun updateSupplier(supplier: SupplierEntity)

    @Delete
    suspend fun deleteSupplier(supplier: SupplierEntity)
}
