package com.example.solosale.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.solosale.data.local.entity.ProductEntity
import com.example.solosale.data.local.entity.PurchaseEntity
import com.example.solosale.data.local.entity.SupplierEntity
import com.example.solosale.data.repository.ProductRepository
import com.example.solosale.data.repository.PurchaseItemInput
import com.example.solosale.data.repository.PurchaseRepository
import com.example.solosale.data.repository.PurchaseWithItems
import com.example.solosale.data.repository.SupplierRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PurchaseViewModel(
    private val purchaseRepository: PurchaseRepository,
    private val supplierRepository: SupplierRepository,
    private val productRepository: ProductRepository
) : ViewModel() {

    val allPurchases: StateFlow<List<PurchaseEntity>> = purchaseRepository.allPurchases.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allSuppliers: StateFlow<List<SupplierEntity>> = supplierRepository.allSuppliers.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allProducts: StateFlow<List<ProductEntity>> = productRepository.allProducts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage = _statusMessage.asStateFlow()

    fun createPurchase(
        supplier: SupplierEntity?,
        customSupplierName: String,
        billNumber: String,
        items: List<PurchaseItemInput>,
        paidAmount: Double,
        notes: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            if (items.isEmpty()) {
                _statusMessage.value = "Please add at least one product to purchase"
                return@launch
            }

            val supplierName = supplier?.supplierName ?: customSupplierName

            val result = purchaseRepository.createPurchase(
                supplierId = supplier?.supplierId,
                supplierName = supplierName,
                billNumber = billNumber.trim(),
                items = items,
                paidAmount = paidAmount,
                notes = notes.trim()
            )

            if (result.isSuccess) {
                _statusMessage.value = "Purchase recorded and stock updated!"
                onSuccess()
            } else {
                _statusMessage.value = result.exceptionOrNull()?.message ?: "Failed to save purchase"
            }
        }
    }

    fun saveSupplier(
        supplierId: Long = 0,
        name: String,
        phone: String,
        address: String,
        email: String,
        contactPerson: String,
        onSuccess: () -> Unit
    ) {
        if (name.isBlank()) {
            _statusMessage.value = "Supplier name is required"
            return
        }
        viewModelScope.launch {
            val entity = SupplierEntity(
                supplierId = supplierId,
                supplierName = name.trim(),
                phone = phone.trim(),
                address = address.trim(),
                email = email.trim(),
                contactPerson = contactPerson.trim()
            )
            if (supplierId == 0L) {
                supplierRepository.insertSupplier(entity)
                _statusMessage.value = "Supplier added"
            } else {
                supplierRepository.updateSupplier(entity)
                _statusMessage.value = "Supplier updated"
            }
            onSuccess()
        }
    }

    suspend fun getPurchaseDetails(purchaseId: Long): PurchaseWithItems? {
        return purchaseRepository.getPurchaseWithItems(purchaseId)
    }

    fun clearStatus() {
        _statusMessage.value = null
    }
}
