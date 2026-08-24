package com.example.solosale.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.solosale.data.local.entity.ProductEntity
import com.example.solosale.ui.components.AppBottomBar
import com.example.solosale.ui.components.AppSearchBar
import com.example.solosale.ui.components.AppTopBar
import com.example.solosale.ui.components.EmptyState
import com.example.solosale.ui.components.ProductCard
import com.example.solosale.viewmodel.ProductViewModel
import com.example.solosale.viewmodel.StockFilterOption

@Composable
fun ProductsScreen(
    productViewModel: ProductViewModel,
    onMenuClick: () -> Unit,
    onNavigateToAddProduct: () -> Unit,
    onNavigateToEditProduct: (Long) -> Unit,
    onNavigateBottom: (String) -> Unit
) {
    val products by productViewModel.products.collectAsState()
    val searchQuery by productViewModel.searchQuery.collectAsState()
    val categories by productViewModel.categories.collectAsState()
    val selectedCategory by productViewModel.selectedCategory.collectAsState()
    val stockFilter by productViewModel.stockFilter.collectAsState()
    val statusMessage by productViewModel.operationStatus.collectAsState()

    var productToDelete by remember { mutableStateOf<ProductEntity?>(null) }
    var productToAdjust by remember { mutableStateOf<ProductEntity?>(null) }
    var adjustQtyInput by remember { mutableStateOf("") }
    var adjustReasonInput by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(statusMessage) {
        statusMessage?.let {
            snackbarHostState.showSnackbar(it)
            productViewModel.clearStatus()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AppTopBar(
                title = "Products Catalog",
                onMenuClick = onMenuClick
            )
        },
        bottomBar = {
            AppBottomBar(currentRoute = "products", onNavigate = onNavigateBottom)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddProduct,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_product_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Product")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Search Bar
            AppSearchBar(
                query = searchQuery,
                onQueryChange = { productViewModel.onSearchQueryChange(it) },
                placeholder = "Search products by name or code...",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            // Stock Filter Chips
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(StockFilterOption.values()) { option ->
                    FilterChip(
                        selected = stockFilter == option,
                        onClick = { productViewModel.onStockFilterSelect(option) },
                        label = { Text(option.label) }
                    )
                }
            }

            // Categories Filter Chips
            if (categories.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategory == "All",
                            onClick = { productViewModel.onCategorySelect("All") },
                            label = { Text("All Categories") }
                        )
                    }
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { productViewModel.onCategorySelect(cat) },
                            label = { Text(cat) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (products.isEmpty()) {
                EmptyState(
                    title = "No Products Found",
                    message = "Tap the + button to add products to your inventory.",
                    icon = Icons.Default.Inventory2,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(products, key = { it.productId }) { product ->
                        ProductCard(
                            product = product,
                            onClick = { onNavigateToEditProduct(product.productId) },
                            onEdit = { onNavigateToEditProduct(product.productId) },
                            onDelete = { productToDelete = product },
                            onAdjustStock = { productToAdjust = product }
                        )
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (productToDelete != null) {
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            title = { Text("Delete Product") },
            text = { Text("Are you sure you want to delete '${productToDelete?.productName}'? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        productToDelete?.let { productViewModel.deleteProduct(it) }
                        productToDelete = null
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { productToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Adjust Stock Dialog
    if (productToAdjust != null) {
        AlertDialog(
            onDismissRequest = { productToAdjust = null },
            title = { Text("Adjust Stock: ${productToAdjust?.productName}") },
            text = {
                Column {
                    Text(
                        text = "Current Stock: ${productToAdjust?.stockQuantity} ${productToAdjust?.unit}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = adjustQtyInput,
                        onValueChange = { adjustQtyInput = it },
                        label = { Text("Quantity Change (e.g. +5 or -3)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = adjustReasonInput,
                        onValueChange = { adjustReasonInput = it },
                        label = { Text("Reason (e.g. Damaged, Found extra)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val change = adjustQtyInput.toDoubleOrNull()
                        if (change != null && adjustReasonInput.isNotBlank()) {
                            productToAdjust?.let {
                                productViewModel.adjustStock(it.productId, change, adjustReasonInput) {
                                    productToAdjust = null
                                    adjustQtyInput = ""
                                    adjustReasonInput = ""
                                }
                            }
                        }
                    }
                ) {
                    Text("Save Adjustment")
                }
            },
            dismissButton = {
                TextButton(onClick = { productToAdjust = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun AddEditProductScreen(
    productId: Long = 0L,
    productViewModel: ProductViewModel,
    onNavigateBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var purchasePrice by remember { mutableStateOf("") }
    var sellingPrice by remember { mutableStateOf("") }
    var stockQuantity by remember { mutableStateOf("") }
    var minStock by remember { mutableStateOf("5") }
    var unit by remember { mutableStateOf("Pcs") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(productId) {
        if (productId > 0L) {
            val prod = productViewModel.getProduct(productId)
            if (prod != null) {
                name = prod.productName
                code = prod.productCode
                category = prod.category
                purchasePrice = prod.purchasePrice.toString()
                sellingPrice = prod.sellingPrice.toString()
                stockQuantity = prod.stockQuantity.toString()
                minStock = prod.minimumStockLevel.toString()
                unit = prod.unit
            }
        } else {
            // Auto generate a code if empty
            code = "P" + (1000..9999).random()
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = if (productId == 0L) "Add New Product" else "Edit Product",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    if (errorMessage != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            Text(
                                text = errorMessage!!,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Product Name *") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("product_name_input")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = code,
                            onValueChange = { code = it },
                            label = { Text("Product Code / SKU *") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("product_code_input")
                        )
                        OutlinedTextField(
                            value = category,
                            onValueChange = { category = it },
                            label = { Text("Category") },
                            placeholder = { Text("e.g. Grocery, Electronics") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("product_category_input")
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = purchasePrice,
                            onValueChange = { purchasePrice = it },
                            label = { Text("Cost Price (Rs.) *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("product_purchase_price_input")
                        )
                        OutlinedTextField(
                            value = sellingPrice,
                            onValueChange = { sellingPrice = it },
                            label = { Text("Selling Price (Rs.) *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("product_selling_price_input")
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = stockQuantity,
                            onValueChange = { stockQuantity = it },
                            label = { Text("Initial Stock *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("product_stock_input")
                        )
                        OutlinedTextField(
                            value = minStock,
                            onValueChange = { minStock = it },
                            label = { Text("Min Stock Alert") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("product_min_stock_input")
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Unit (e.g. Pcs, Kg, Ltr, Box, Packet)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("product_unit_input")
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            val cost = purchasePrice.toDoubleOrNull() ?: -1.0
                            val price = sellingPrice.toDoubleOrNull() ?: -1.0
                            val stock = stockQuantity.toDoubleOrNull() ?: 0.0
                            val min = minStock.toDoubleOrNull() ?: 5.0

                            if (name.isBlank()) {
                                errorMessage = "Product name cannot be empty"
                                return@Button
                            }
                            if (code.isBlank()) {
                                errorMessage = "Product code cannot be empty"
                                return@Button
                            }
                            if (cost < 0 || price < 0) {
                                errorMessage = "Please enter valid positive prices"
                                return@Button
                            }

                            productViewModel.saveProduct(
                                productId = productId,
                                name = name,
                                code = code,
                                category = category,
                                purchasePrice = cost,
                                sellingPrice = price,
                                stock = stock,
                                minStock = min,
                                unit = unit,
                                onSuccess = onNavigateBack
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("save_product_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (productId == 0L) "Save Product" else "Update Product",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
