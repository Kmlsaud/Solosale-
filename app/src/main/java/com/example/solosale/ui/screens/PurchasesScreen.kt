package com.example.solosale.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.solosale.data.local.entity.ProductEntity
import com.example.solosale.data.local.entity.SupplierEntity
import com.example.solosale.data.repository.PurchaseItemInput
import com.example.solosale.ui.components.AppTopBar
import com.example.solosale.ui.components.EmptyState
import com.example.solosale.utils.CurrencyUtils
import com.example.solosale.utils.DateUtils
import com.example.solosale.viewmodel.PurchaseViewModel

@Composable
fun PurchasesScreen(
    purchaseViewModel: PurchaseViewModel,
    onMenuClick: () -> Unit,
    onNavigateToAddPurchase: () -> Unit
) {
    val purchases by purchaseViewModel.allPurchases.collectAsState()

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Purchases (Stock-In)",
                onMenuClick = onMenuClick
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddPurchase,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_purchase_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Purchase")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (purchases.isEmpty()) {
                EmptyState(
                    title = "No Purchases Recorded",
                    message = "Record incoming stock purchases from suppliers to update inventory.",
                    icon = Icons.Default.ShoppingBag,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(purchases, key = { it.purchaseId }) { purchase ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = if (purchase.billNumber.isNotBlank()) "Bill #${purchase.billNumber}" else "Purchase #${purchase.purchaseId}",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Text(
                                            text = DateUtils.formatDateTime(purchase.purchaseDate),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }

                                    Text(
                                        text = CurrencyUtils.formatNpr(purchase.totalAmount),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Supplier: ${purchase.supplierName}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Paid: ${CurrencyUtils.formatNpr(purchase.paidAmount)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF2E7D32)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddPurchaseScreen(
    purchaseViewModel: PurchaseViewModel,
    onNavigateBack: () -> Unit
) {
    val suppliers by purchaseViewModel.allSuppliers.collectAsState()
    val products by purchaseViewModel.allProducts.collectAsState()
    val statusMessage by purchaseViewModel.statusMessage.collectAsState()

    var selectedSupplier by remember { mutableStateOf<SupplierEntity?>(null) }
    var customSupplierName by remember { mutableStateOf("") }
    var billNumber by remember { mutableStateOf("") }
    var paidAmountInput by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val purchaseItems = remember { mutableStateListOf<PurchaseItemInput>() }

    var showAddItemDialog by remember { mutableStateOf(false) }
    var selectedProductForAdd by remember { mutableStateOf<ProductEntity?>(null) }
    var addQtyInput by remember { mutableStateOf("1") }
    var addCostInput by remember { mutableStateOf("") }

    val totalCost = purchaseItems.sumOf { it.totalPrice }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(statusMessage) {
        statusMessage?.let {
            snackbarHostState.showSnackbar(it)
            purchaseViewModel.clearStatus()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AppTopBar(
                title = "Record Stock-In Purchase",
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Supplier Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = selectedSupplier?.supplierName ?: customSupplierName,
                        onValueChange = {
                            selectedSupplier = null
                            customSupplierName = it
                        },
                        label = { Text("Supplier Name") },
                        placeholder = { Text("e.g. Kathmandu Distributors") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = billNumber,
                        onValueChange = { billNumber = it },
                        label = { Text("Supplier Bill / Invoice Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Products to add
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Items Purchased (${purchaseItems.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Button(
                            onClick = {
                                if (products.isNotEmpty()) {
                                    selectedProductForAdd = products.first()
                                    addCostInput = products.first().purchasePrice.toString()
                                    showAddItemDialog = true
                                }
                            }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Item")
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                    if (purchaseItems.isEmpty()) {
                        Text("No items added yet. Click 'Add Item' to select products.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        purchaseItems.forEachIndexed { index, item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.productName, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        text = "${CurrencyUtils.formatNpr(item.purchasePrice)} x ${item.quantity.toInt()} = ${CurrencyUtils.formatNpr(item.totalPrice)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(onClick = { purchaseItems.removeAt(index) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Amount:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(CurrencyUtils.formatNpr(totalCost), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            // Payment & Notes
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = paidAmountInput,
                        onValueChange = { paidAmountInput = it },
                        label = { Text("Amount Paid (Rs.)") },
                        placeholder = { Text(totalCost.toString()) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes (Optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Button(
                onClick = {
                    val paid = paidAmountInput.toDoubleOrNull() ?: totalCost
                    purchaseViewModel.createPurchase(
                        supplier = selectedSupplier,
                        customSupplierName = customSupplierName,
                        billNumber = billNumber,
                        items = purchaseItems.toList(),
                        paidAmount = paid,
                        notes = notes,
                        onSuccess = onNavigateBack
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("submit_purchase_button"),
                shape = RoundedCornerShape(12.dp),
                enabled = purchaseItems.isNotEmpty()
            ) {
                Text("Save Purchase & Update Stock", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showAddItemDialog && selectedProductForAdd != null) {
        var dropdownExpanded by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAddItemDialog = false },
            title = { Text("Add Purchase Item") },
            text = {
                Column {
                    Text("Select Product:", fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box {
                        OutlinedButton(
                            onClick = { dropdownExpanded = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(selectedProductForAdd?.productName ?: "Select")
                        }
                        DropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false }
                        ) {
                            products.forEach { prod ->
                                DropdownMenuItem(
                                    text = { Text(prod.productName) },
                                    onClick = {
                                        selectedProductForAdd = prod
                                        addCostInput = prod.purchasePrice.toString()
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = addQtyInput,
                        onValueChange = { addQtyInput = it },
                        label = { Text("Quantity (${selectedProductForAdd?.unit})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = addCostInput,
                        onValueChange = { addCostInput = it },
                        label = { Text("Purchase Cost Per Unit (Rs.)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val qty = addQtyInput.toDoubleOrNull() ?: 1.0
                    val cost = addCostInput.toDoubleOrNull() ?: 0.0
                    if (selectedProductForAdd != null && qty > 0 && cost >= 0) {
                        purchaseItems.add(
                            PurchaseItemInput(
                                productId = selectedProductForAdd!!.productId,
                                productName = selectedProductForAdd!!.productName,
                                quantity = qty,
                                purchasePrice = cost
                            )
                        )
                        showAddItemDialog = false
                    }
                }) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddItemDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
