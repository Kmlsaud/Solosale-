package com.example.solosale.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.solosale.data.local.entity.ProductEntity
import com.example.solosale.data.repository.InventoryRepository
import com.example.solosale.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class StockFilterOption(val label: String) {
    ALL("All Products"),
    IN_STOCK("In Stock"),
    LOW_STOCK("Low Stock"),
    OUT_OF_STOCK("Out of Stock")
}

class ProductViewModel(
    private val productRepository: ProductRepository,
    private val inventoryRepository: InventoryRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>("All")
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _stockFilter = MutableStateFlow(StockFilterOption.ALL)
    val stockFilter = _stockFilter.asStateFlow()

    val categories: StateFlow<List<String>> = productRepository.categories.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val products: StateFlow<List<ProductEntity>> = combine(
        productRepository.allProducts,
        _searchQuery,
        _selectedCategory,
        _stockFilter
    ) { all, query, category, filter ->
        all.filter { prod ->
            val matchesQuery = query.isBlank() ||
                    prod.productName.contains(query, ignoreCase = true) ||
                    prod.productCode.contains(query, ignoreCase = true)
            val matchesCategory = category == "All" || category == null || prod.category.equals(category, ignoreCase = true)
            val matchesStock = when (filter) {
                StockFilterOption.ALL -> true
                StockFilterOption.IN_STOCK -> prod.stockQuantity > prod.minimumStockLevel
                StockFilterOption.LOW_STOCK -> prod.stockQuantity > 0 && prod.stockQuantity <= prod.minimumStockLevel
                StockFilterOption.OUT_OF_STOCK -> prod.stockQuantity <= 0
            }
            matchesQuery && matchesCategory && matchesStock
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _operationStatus = MutableStateFlow<String?>(null)
    val operationStatus = _operationStatus.asStateFlow()

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onCategorySelect(category: String?) {
        _selectedCategory.value = category
    }

    fun onStockFilterSelect(filter: StockFilterOption) {
        _stockFilter.value = filter
    }

    fun saveProduct(
        productId: Long = 0,
        name: String,
        code: String,
        category: String,
        purchasePrice: Double,
        sellingPrice: Double,
        stock: Double,
        minStock: Double,
        unit: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val product = ProductEntity(
                productId = productId,
                productName = name.trim(),
                productCode = code.trim().uppercase(),
                category = if (category.isNotBlank()) category.trim() else "General",
                purchasePrice = purchasePrice,
                sellingPrice = sellingPrice,
                stockQuantity = stock,
                minimumStockLevel = minStock,
                unit = if (unit.isNotBlank()) unit.trim() else "Pcs"
            )

            val result = if (productId == 0L) {
                productRepository.insertProduct(product)
            } else {
                productRepository.updateProduct(product)
            }

            if (result.isSuccess) {
                _operationStatus.value = "Product saved successfully"
                onSuccess()
            } else {
                _operationStatus.value = result.exceptionOrNull()?.message ?: "Failed to save product"
            }
        }
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            productRepository.deleteProduct(product)
            _operationStatus.value = "Product deleted"
        }
    }

    fun adjustStock(productId: Long, qtyChange: Double, reason: String, onDone: () -> Unit) {
        viewModelScope.launch {
            val result = inventoryRepository.adjustStock(productId, qtyChange, reason)
            if (result.isSuccess) {
                _operationStatus.value = "Stock adjusted"
                onDone()
            } else {
                _operationStatus.value = result.exceptionOrNull()?.message ?: "Failed to adjust stock"
            }
        }
    }

    fun clearStatus() {
        _operationStatus.value = null
    }

    suspend fun getProduct(id: Long): ProductEntity? = productRepository.getProductById(id)
}
