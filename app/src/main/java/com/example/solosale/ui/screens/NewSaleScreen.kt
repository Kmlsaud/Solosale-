package com.example.solosale.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Discount
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.solosale.data.local.entity.CustomerEntity
import com.example.solosale.data.local.entity.PaymentMethod
import com.example.solosale.data.local.entity.ProductEntity
import com.example.solosale.ui.components.AppSearchBar
import com.example.solosale.ui.components.AppTopBar
import com.example.solosale.ui.components.StockBadge
import com.example.solosale.utils.CurrencyUtils
import com.example.solosale.viewmodel.SalesViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewSaleScreen(
    salesViewModel: SalesViewModel,
    onNavigateBack: () -> Unit,
    onSaleCompleted: (Long) -> Unit
) {
    val products by salesViewModel.availableProducts.collectAsState()
    val cartItems by salesViewModel.cartItems.collectAsState()
    val selectedCustomer by salesViewModel.selectedCustomer.collectAsState()
    val customCustomerName by salesViewModel.customCustomerName.collectAsState()
    val customCustomerPhone by salesViewModel.customCustomerPhone.collectAsState()
    val discountAmount by salesViewModel.discountAmount.collectAsState()
    val isTaxApplied by salesViewModel.isTaxApplied.collectAsState()
    val paymentMethod by salesViewModel.paymentMethod.collectAsState()
    val paidAmountInput by salesViewModel.paidAmountInput.collectAsState()
    val notes by salesViewModel.notes.collectAsState()
    val productSearch by salesViewModel.productSearch.collectAsState()
    val checkoutError by salesViewModel.checkoutError.collectAsState()
    val settings by salesViewModel.settings.collectAsState()
    val allCustomers by salesViewModel.allCustomers.collectAsState()

    val symbol = settings.currencySymbol
    val subtotal = cartItems.sumOf { it.totalPrice }
    val taxPercentage = if (isTaxApplied) settings.taxPercentage else 0.0
    val taxAmount = if (isTaxApplied) ((subtotal - discountAmount) * (taxPercentage / 100.0)).coerceAtLeast(0.0) else 0.0
    val grandTotal = (subtotal - discountAmount + taxAmount).coerceAtLeast(0.0)

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showCartSheet by remember { mutableStateOf(false) }
    var showCustomerDialog by remember { mutableStateOf(false) }
    var showPaymentDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Point of Sale (POS)",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack,
                actions = {
                    if (cartItems.isNotEmpty()) {
                        TextButton(onClick = { salesViewModel.clearCart() }) {
                            Text("Clear", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            )
        },
        bottomBar = {
            // Persistent Bottom Cart Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .clickable { if (cartItems.isNotEmpty()) showCartSheet = true }
                            .testTag("pos_bottom_cart_summary")
                    ) {
                        Text(
                            text = "${cartItems.sumOf { it.quantity }.toInt()} items in cart",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = CurrencyUtils.formatNpr(grandTotal, symbol),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Button(
                        onClick = {
                            if (cartItems.isNotEmpty()) {
                                showPaymentDialog = true
                            }
                        },
                        enabled = cartItems.isNotEmpty(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("pos_checkout_button")
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Review & Pay", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Customer Header Bar
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        val displayName = selectedCustomer?.customerName
                            ?: if (customCustomerName.isNotBlank()) customCustomerName else "Walk-in Customer"
                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    OutlinedButton(
                        onClick = { showCustomerDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text("Change", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            // Search Bar
            AppSearchBar(
                query = productSearch,
                onQueryChange = { salesViewModel.onProductSearchChange(it) },
                placeholder = "Search product to add to cart...",
                modifier = Modifier.padding(16.dp)
            )

            // Product Grid / List for POS
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(products, key = { it.productId }) { product ->
                    PosProductItem(
                        product = product,
                        currencySymbol = symbol,
                        inCartQty = cartItems.find { it.productId == product.productId }?.quantity ?: 0.0,
                        onAddToCart = { salesViewModel.addToCart(product, 1.0) },
                        onMinusFromCart = {
                            val inCart = cartItems.find { it.productId == product.productId }
                            if (inCart != null) {
                                salesViewModel.updateCartItemQuantity(product.productId, inCart.quantity - 1.0)
                            }
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }

    // Modal Cart Sheet
    if (showCartSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCartSheet = false },
            sheetState = sheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Current Cart (${cartItems.size} items)",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { showCartSheet = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                cartItems.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.productName, fontWeight = FontWeight.SemiBold)
                            Text(
                                text = "${CurrencyUtils.formatNpr(item.unitPrice, symbol)} x ${item.quantity.toInt()} = ${CurrencyUtils.formatNpr(item.totalPrice, symbol)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    salesViewModel.updateCartItemQuantity(item.productId, item.quantity - 1.0)
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease")
                            }

                            Text(
                                text = item.quantity.toInt().toString(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )

                            IconButton(
                                onClick = {
                                    salesViewModel.updateCartItemQuantity(item.productId, item.quantity + 1.0)
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Increase")
                            }

                            IconButton(
                                onClick = { salesViewModel.removeFromCart(item.productId) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Remove",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // Discount Input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Discount (Rs.)", style = MaterialTheme.typography.bodyLarge)
                    OutlinedTextField(
                        value = if (discountAmount == 0.0) "" else discountAmount.toString(),
                        onValueChange = { salesViewModel.setDiscount(it.toDoubleOrNull() ?: 0.0) },
                        placeholder = { Text("0") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.width(120.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // VAT Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Apply Tax/VAT (${settings.taxPercentage}%)", style = MaterialTheme.typography.bodyLarge)
                    Switch(
                        checked = isTaxApplied,
                        onCheckedChange = { salesViewModel.toggleTax(it) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Total Breakdown
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Subtotal:")
                            Text(CurrencyUtils.formatNpr(subtotal, symbol))
                        }
                        if (discountAmount > 0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Discount:")
                                Text("- ${CurrencyUtils.formatNpr(discountAmount, symbol)}", color = Color(0xFF2E7D32))
                            }
                        }
                        if (isTaxApplied) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("VAT (${settings.taxPercentage}%):")
                                Text("+ ${CurrencyUtils.formatNpr(taxAmount, symbol)}")
                            }
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Grand Total:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text(CurrencyUtils.formatNpr(grandTotal, symbol), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        showCartSheet = false
                        showPaymentDialog = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Proceed to Checkout", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    // Customer Selection Dialog
    if (showCustomerDialog) {
        var newCustName by remember { mutableStateOf("") }
        var newCustPhone by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCustomerDialog = false },
            title = { Text("Select Customer") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text("Quick Enter Customer:", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newCustName,
                        onValueChange = { newCustName = it },
                        label = { Text("Customer Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newCustPhone,
                        onValueChange = { newCustPhone = it },
                        label = { Text("Phone Number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Or Select Saved Customer:", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    allCustomers.forEach { cust ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    salesViewModel.selectCustomer(cust)
                                    showCustomerDialog = false
                                },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(cust.customerName, fontWeight = FontWeight.SemiBold)
                                    Text(cust.phone, style = MaterialTheme.typography.labelSmall)
                                }
                                if (selectedCustomer?.customerId == cust.customerId) {
                                    Icon(Icons.Default.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (newCustName.isNotBlank()) {
                        salesViewModel.setCustomCustomer(newCustName, newCustPhone)
                    }
                    showCustomerDialog = false
                }) {
                    Text("Done")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    salesViewModel.setCustomCustomer("Walk-in Customer", "")
                    showCustomerDialog = false
                }) {
                    Text("Walk-in")
                }
            }
        )
    }

    // Payment & Final Checkout Dialog
    if (showPaymentDialog) {
        AlertDialog(
            onDismissRequest = { showPaymentDialog = false },
            title = { Text("Checkout & Payment") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    if (checkoutError != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Text(
                                text = checkoutError!!,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    Text(
                        text = "Amount Due: ${CurrencyUtils.formatNpr(grandTotal, symbol)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Payment Method:", fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = paymentMethod == PaymentMethod.CASH,
                            onClick = { salesViewModel.setPaymentMethod(PaymentMethod.CASH) },
                            label = { Text("Cash") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = paymentMethod == PaymentMethod.DIGITAL,
                            onClick = { salesViewModel.setPaymentMethod(PaymentMethod.DIGITAL) },
                            label = { Text("Digital / QR") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = paymentMethod == PaymentMethod.CREDIT,
                            onClick = { salesViewModel.setPaymentMethod(PaymentMethod.CREDIT) },
                            label = { Text("Credit (Due)") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (paymentMethod == PaymentMethod.CREDIT) {
                        OutlinedTextField(
                            value = paidAmountInput,
                            onValueChange = { salesViewModel.setPaidAmountInput(it) },
                            label = { Text("Amount Paid Now (Rs.)") },
                            placeholder = { Text("0") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        val paid = paidAmountInput.toDoubleOrNull() ?: 0.0
                        val remainingDue = (grandTotal - paid).coerceAtLeast(0.0)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Remaining Due: ${CurrencyUtils.formatNpr(remainingDue, symbol)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    } else {
                        OutlinedTextField(
                            value = paidAmountInput,
                            onValueChange = { salesViewModel.setPaidAmountInput(it) },
                            label = { Text("Cash Received (Optional)") },
                            placeholder = { Text(grandTotal.toString()) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        val cashGiven = paidAmountInput.toDoubleOrNull()
                        if (cashGiven != null && cashGiven > grandTotal) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Change to Return: ${CurrencyUtils.formatNpr(cashGiven - grandTotal, symbol)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { salesViewModel.setNotes(it) },
                        label = { Text("Invoice Notes (Optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        salesViewModel.checkout { createdSaleId ->
                            showPaymentDialog = false
                            onSaleCompleted(createdSaleId)
                        }
                    },
                    modifier = Modifier.testTag("confirm_checkout_btn")
                ) {
                    Text("Complete & Print")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    salesViewModel.clearError()
                    showPaymentDialog = false
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun PosProductItem(
    product: ProductEntity,
    currencySymbol: String,
    inCartQty: Double,
    onAddToCart: () -> Unit,
    onMinusFromCart: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("pos_product_${product.productId}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.productName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${CurrencyUtils.formatNpr(product.sellingPrice, currencySymbol)} • Stock: ${product.stockQuantity.toInt()} ${product.unit}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (product.stockQuantity <= 0) {
                StockBadge(quantity = 0.0, minStock = 5.0, unit = product.unit)
            } else if (inCartQty > 0) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onMinusFromCart,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease")
                    }
                    Text(
                        text = inCartQty.toInt().toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    IconButton(
                        onClick = onAddToCart,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Increase")
                    }
                }
            } else {
                Button(
                    onClick = onAddToCart,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(38.dp)
                ) {
                    Text("Add")
                }
            }
        }
    }
}
