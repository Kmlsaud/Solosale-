package com.example.solosale.data.repository

import com.example.solosale.data.local.dao.CustomerDao
import com.example.solosale.data.local.dao.PaymentDao
import com.example.solosale.data.local.dao.SaleDao
import com.example.solosale.data.local.entity.CustomerEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class CustomerWithStats(
    val customer: CustomerEntity,
    val totalPurchases: Double,
    val totalPaid: Double,
    val totalDue: Double
)

class CustomerRepository(
    private val customerDao: CustomerDao,
    private val saleDao: SaleDao,
    private val paymentDao: PaymentDao
) {
    val allCustomers: Flow<List<CustomerEntity>> = customerDao.getAllCustomers()
    val totalCustomersCount: Flow<Int> = customerDao.getCustomerCount()

    fun searchCustomers(query: String): Flow<List<CustomerEntity>> = customerDao.searchCustomers(query)

    suspend fun getCustomerById(id: Long): CustomerEntity? = customerDao.getCustomerById(id)

    suspend fun insertCustomer(customer: CustomerEntity): Long = customerDao.insertCustomer(customer)

    suspend fun updateCustomer(customer: CustomerEntity) = customerDao.updateCustomer(customer)

    suspend fun deleteCustomer(customer: CustomerEntity) = customerDao.deleteCustomer(customer)

    fun getCustomerWithStats(customerId: Long): Flow<CustomerWithStats?> {
        return combine(
            saleDao.getSalesForCustomer(customerId),
            paymentDao.getPaymentsForCustomer(customerId)
        ) { sales, payments ->
            val customer = customerDao.getCustomerById(customerId) ?: return@combine null
            val totalPurchases = sales.sumOf { it.grandTotal }
            val totalDue = sales.sumOf { it.dueAmount }
            val totalPaid = totalPurchases - totalDue
            CustomerWithStats(
                customer = customer,
                totalPurchases = totalPurchases,
                totalPaid = totalPaid,
                totalDue = totalDue
            )
        }
    }
}
