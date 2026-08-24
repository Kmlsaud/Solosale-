package com.example.solosale.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.solosale.data.local.entity.CustomerEntity
import com.example.solosale.data.local.entity.PaymentEntity
import com.example.solosale.data.local.entity.PaymentMethod
import com.example.solosale.data.local.entity.SaleEntity
import com.example.solosale.data.repository.CustomerRepository
import com.example.solosale.data.repository.CustomerWithStats
import com.example.solosale.data.repository.PaymentRepository
import com.example.solosale.data.repository.SalesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CustomerViewModel(
    private val customerRepository: CustomerRepository,
    private val salesRepository: SalesRepository,
    private val paymentRepository: PaymentRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    val customersWithStats: StateFlow<List<CustomerWithStats>> = combine(
        customerRepository.allCustomers,
        salesRepository.allSales,
        paymentRepository.allPayments,
        _searchQuery
    ) { customers, sales, payments, query ->
        customers
            .filter {
                query.isBlank() ||
                        it.customerName.contains(query, ignoreCase = true) ||
                        it.phone.contains(query, ignoreCase = true)
            }
            .map { customer ->
                val custSales = sales.filter { it.customerId == customer.customerId }
                val totalPurchases = custSales.sumOf { it.grandTotal }
                val totalDue = custSales.sumOf { it.dueAmount }
                val totalPaid = totalPurchases - totalDue
                CustomerWithStats(
                    customer = customer,
                    totalPurchases = totalPurchases,
                    totalPaid = totalPaid,
                    totalDue = totalDue
                )
            }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage = _statusMessage.asStateFlow()

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun saveCustomer(
        customerId: Long = 0,
        name: String,
        phone: String,
        address: String,
        email: String,
        onSuccess: () -> Unit
    ) {
        if (name.isBlank()) {
            _statusMessage.value = "Customer name is required"
            return
        }
        viewModelScope.launch {
            val entity = CustomerEntity(
                customerId = customerId,
                customerName = name.trim(),
                phone = phone.trim(),
                address = address.trim(),
                email = email.trim()
            )
            if (customerId == 0L) {
                customerRepository.insertCustomer(entity)
                _statusMessage.value = "Customer added successfully"
            } else {
                customerRepository.updateCustomer(entity)
                _statusMessage.value = "Customer updated"
            }
            onSuccess()
        }
    }

    fun deleteCustomer(customer: CustomerEntity) {
        viewModelScope.launch {
            customerRepository.deleteCustomer(customer)
            _statusMessage.value = "Customer deleted"
        }
    }

    fun getCustomerSales(customerId: Long): StateFlow<List<SaleEntity>> =
        salesRepository.getSalesForCustomer(customerId).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun getCustomerPayments(customerId: Long): StateFlow<List<PaymentEntity>> =
        paymentRepository.getPaymentsForCustomer(customerId).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun collectDue(customerId: Long, amount: Double, method: PaymentMethod, notes: String, onDone: () -> Unit) {
        viewModelScope.launch {
            val res = paymentRepository.collectDuePayment(customerId, amount, method, notes)
            if (res.isSuccess) {
                _statusMessage.value = "Due payment recorded successfully"
                onDone()
            } else {
                _statusMessage.value = res.exceptionOrNull()?.message ?: "Failed to record payment"
            }
        }
    }

    fun clearStatus() {
        _statusMessage.value = null
    }
}

class PaymentViewModel(
    private val paymentRepository: PaymentRepository,
    private val salesRepository: SalesRepository
) : ViewModel() {

    val allPayments: StateFlow<List<PaymentEntity>> = paymentRepository.allPayments.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val salesWithDue: StateFlow<List<SaleEntity>> = salesRepository.salesWithDue.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val totalDue: StateFlow<Double> = salesRepository.totalDue.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )
}
