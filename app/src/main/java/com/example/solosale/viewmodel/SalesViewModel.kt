package com.example.solosale.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.solosale.data.local.entity.BusinessSettingsEntity
import com.example.solosale.data.local.entity.CustomerEntity
import com.example.solosale.data.local.entity.PaymentMethod
import com.example.solosale.data.local.entity.ProductEntity
import com.example.solosale.data.local.entity.SaleEntity
import com.example.solosale.data.repository.CartItem
import com.example.solosale.data.repository.CustomerRepository
import com.example.solosale.data.repository.ProductRepository
import com.example.solosale.data.repository.SaleWithItems
import com.example.solosale.data.repository.SalesRepository
import com.example.solosale.data.repository.SettingsRepository
import com.example.solosale.utils.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SalesViewModel(
    private val salesRepository: SalesRepository,
    private val productRepository: ProductRepository,
    private val customerRepository: CustomerRepository,
    private val settingsRepository: SettingsRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    val allSales: StateFlow<List<SaleEntity>> = salesRepository.allSales.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allCustomers: StateFlow<List<CustomerEntity>> = customerRepository.allCustomers.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val settings: StateFlow<BusinessSettingsEntity> = settingsRepository.settingsFlow
        .combine(MutableStateFlow(BusinessSettingsEntity())) { saved, default ->
            saved ?: default
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = BusinessSettingsEntity()
        )

    // POS State
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems = _cartItems.asStateFlow()

    private val _selectedCustomer = MutableStateFlow<CustomerEntity?>(null)
    val selectedCustomer = _selectedCustomer.asStateFlow()

    private val _customCustomerName = MutableStateFlow("")
    val customCustomerName = _customCustomerName.asStateFlow()

    private val _customCustomerPhone = MutableStateFlow("")
    val customCustomerPhone = _customCustomerPhone.asStateFlow()

    private val _discountAmount = MutableStateFlow(0.0)
    val discountAmount = _discountAmount.asStateFlow()

    private val _isTaxApplied = MutableStateFlow(false)
    val isTaxApplied = _isTaxApplied.asStateFlow()

    private val _paymentMethod = MutableStateFlow(PaymentMethod.CASH)
    val paymentMethod = _paymentMethod.asStateFlow()

    private val _paidAmountInput = MutableStateFlow<String>("")
    val paidAmountInput = _paidAmountInput.asStateFlow()

    private val _notes = MutableStateFlow("")
    val notes = _notes.asStateFlow()

    private val _productSearch = MutableStateFlow("")
    val productSearch = _productSearch.asStateFlow()

    val availableProducts: StateFlow<List<ProductEntity>> = combine(
        productRepository.allProducts,
        _productSearch
    ) { products, query ->
        if (query.isBlank()) products else products.filter {
            it.productName.contains(query, ignoreCase = true) || it.productCode.contains(query, ignoreCase = true)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _checkoutError = MutableStateFlow<String?>(null)
    val checkoutError = _checkoutError.asStateFlow()

    private val _createdSaleId = MutableStateFlow<Long?>(null)
    val createdSaleId = _createdSaleId.asStateFlow()

    fun onProductSearchChange(query: String) {
        _productSearch.value = query
    }

    fun addToCart(product: ProductEntity, qty: Double = 1.0) {
        val currentList = _cartItems.value.toMutableList()
        val existingIndex = currentList.indexOfFirst { it.productId == product.productId }

        if (existingIndex >= 0) {
            val existing = currentList[existingIndex]
            val newQty = existing.quantity + qty
            if (newQty <= product.stockQuantity) {
                currentList[existingIndex] = existing.copy(quantity = newQty)
            } else {
                _checkoutError.value = "Cannot exceed available stock (${product.stockQuantity})"
                return
            }
        } else {
            if (qty <= product.stockQuantity) {
                currentList.add(
                    CartItem(
                        productId = product.productId,
                        productName = product.productName,
                        productCode = product.productCode,
                        unitPrice = product.sellingPrice,
                        purchasePrice = product.purchasePrice,
                        quantity = qty,
                        unit = product.unit,
                        availableStock = product.stockQuantity
                    )
                )
            } else {
                _checkoutError.value = "Product is out of stock or insufficient stock"
                return
            }
        }
        _cartItems.value = currentList
    }

    fun updateCartItemQuantity(productId: Long, newQty: Double) {
        if (newQty <= 0) {
            removeFromCart(productId)
            return
        }
        val currentList = _cartItems.value.toMutableList()
        val index = currentList.indexOfFirst { it.productId == productId }
        if (index >= 0) {
            val item = currentList[index]
            if (newQty <= item.availableStock) {
                currentList[index] = item.copy(quantity = newQty)
                _cartItems.value = currentList
            } else {
                _checkoutError.value = "Stock limit reached (${item.availableStock})"
            }
        }
    }

    fun removeFromCart(productId: Long) {
        _cartItems.value = _cartItems.value.filter { it.productId != productId }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        _selectedCustomer.value = null
        _customCustomerName.value = ""
        _customCustomerPhone.value = ""
        _discountAmount.value = 0.0
        _isTaxApplied.value = false
        _paidAmountInput.value = ""
        _notes.value = ""
        _paymentMethod.value = PaymentMethod.CASH
        _checkoutError.value = null
    }

    fun selectCustomer(customer: CustomerEntity?) {
        _selectedCustomer.value = customer
        if (customer != null) {
            _customCustomerName.value = customer.customerName
            _customCustomerPhone.value = customer.phone
        }
    }

    fun setCustomCustomer(name: String, phone: String) {
        _selectedCustomer.value = null
        _customCustomerName.value = name
        _customCustomerPhone.value = phone
    }

    fun setDiscount(amount: Double) {
        _discountAmount.value = amount.coerceAtLeast(0.0)
    }

    fun toggleTax(apply: Boolean) {
        _isTaxApplied.value = apply
    }

    fun setPaymentMethod(method: PaymentMethod) {
        _paymentMethod.value = method
    }

    fun setPaidAmountInput(input: String) {
        _paidAmountInput.value = input
    }

    fun setNotes(n: String) {
        _notes.value = n
    }

    fun checkout(onSuccess: (Long) -> Unit) {
        viewModelScope.launch {
            _checkoutError.value = null
            val items = _cartItems.value
            if (items.isEmpty()) {
                _checkoutError.value = "Please add items to cart before checkout"
                return@launch
            }

            val userId = tokenManager.userIdFlow.firstOrNull() ?: 1L
            val invoiceNum = salesRepository.generateNextInvoiceNumber()
            val subtotal = items.sumOf { it.totalPrice }
            val currentSettings = settings.value
            val taxRate = if (_isTaxApplied.value) currentSettings.taxPercentage else 0.0
            val taxAmount = if (_isTaxApplied.value) (subtotal - _discountAmount.value) * (taxRate / 100.0) else 0.0
            val grandTotal = (subtotal - _discountAmount.value + taxAmount).coerceAtLeast(0.0)

            val parsedPaid = when (_paymentMethod.value) {
                PaymentMethod.CREDIT -> _paidAmountInput.value.toDoubleOrNull() ?: 0.0
                PaymentMethod.CASH, PaymentMethod.DIGITAL -> {
                    if (_paidAmountInput.value.isNotBlank()) {
                        _paidAmountInput.value.toDoubleOrNull() ?: grandTotal
                    } else {
                        grandTotal
                    }
                }
            }

            val custName = _selectedCustomer.value?.customerName ?: _customCustomerName.value
            val custPhone = _selectedCustomer.value?.phone ?: _customCustomerPhone.value
            val custId = _selectedCustomer.value?.customerId

            val result = salesRepository.createSale(
                invoiceNumber = invoiceNum,
                customerId = custId,
                customerName = custName,
                customerPhone = custPhone,
                userId = userId,
                items = items,
                discount = _discountAmount.value,
                isTaxEnabled = _isTaxApplied.value,
                taxPercentage = taxRate,
                paidAmount = parsedPaid,
                paymentMethod = _paymentMethod.value,
                notes = _notes.value
            )

            if (result.isSuccess) {
                val saleId = result.getOrThrow()
                _createdSaleId.value = saleId
                clearCart()
                onSuccess(saleId)
            } else {
                _checkoutError.value = result.exceptionOrNull()?.message ?: "Sale creation failed"
            }
        }
    }

    suspend fun getSaleDetails(saleId: Long): SaleWithItems? {
        return salesRepository.getSaleWithItems(saleId)
    }

    fun clearError() {
        _checkoutError.value = null
    }
}
