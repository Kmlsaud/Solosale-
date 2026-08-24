package com.example.solosale.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.example.solosale.data.local.entity.CustomerEntity
import com.example.solosale.data.local.entity.PaymentMethod
import com.example.solosale.data.repository.CustomerWithStats
import com.example.solosale.ui.components.AppBottomBar
import com.example.solosale.ui.components.AppSearchBar
import com.example.solosale.ui.components.AppTopBar
import com.example.solosale.ui.components.CustomerCard
import com.example.solosale.ui.components.EmptyState
import com.example.solosale.ui.components.SaleCard
import com.example.solosale.utils.CurrencyUtils
import com.example.solosale.utils.DateUtils
import com.example.solosale.viewmodel.CustomerViewModel

@Composable
fun CustomersScreen(
    customerViewModel: CustomerViewModel,
    onMenuClick: () -> Unit,
    onNavigateToCustomerDetails: (Long) -> Unit,
    onNavigateBottom: (String) -> Unit
) {
    val customersWithStats by customerViewModel.customersWithStats.collectAsState()
    val searchQuery by customerViewModel.searchQuery.collectAsState()
    val statusMessage by customerViewModel.statusMessage.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var nameInput by remember { mutableStateOf("") }
    var phoneInput by remember { mutableStateOf("") }
    var addressInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(statusMessage) {
        statusMessage?.let {
            snackbarHostState.showSnackbar(it)
            customerViewModel.clearStatus()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AppTopBar(
                title = "Customers",
                onMenuClick = onMenuClick
            )
        },
        bottomBar = {
            AppBottomBar(currentRoute = "customers", onNavigate = onNavigateBottom)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_customer_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Customer")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            AppSearchBar(
                query = searchQuery,
                onQueryChange = { customerViewModel.onSearchQueryChange(it) },
                placeholder = "Search customer by name or phone...",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            if (customersWithStats.isEmpty()) {
                EmptyState(
                    title = "No Customers Found",
                    message = "Add customers to keep track of sales and credit dues.",
                    icon = Icons.Default.Group,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(customersWithStats, key = { it.customer.customerId }) { custStats ->
                        CustomerCard(
                            customerWithStats = custStats,
                            onClick = { onNavigateToCustomerDetails(custStats.customer.customerId) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Customer") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Full Name *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = phoneInput,
                        onValueChange = { phoneInput = it },
                        label = { Text("Phone Number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = addressInput,
                        onValueChange = { addressInput = it },
                        label = { Text("Address / City") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("Email (Optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (nameInput.isNotBlank()) {
                        customerViewModel.saveCustomer(
                            name = nameInput,
                            phone = phoneInput,
                            address = addressInput,
                            email = emailInput,
                            onSuccess = {
                                showAddDialog = false
                                nameInput = ""
                                phoneInput = ""
                                addressInput = ""
                                emailInput = ""
                            }
                        )
                    }
                }) {
                    Text("Save Customer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun CustomerDetailsScreen(
    customerId: Long,
    customerViewModel: CustomerViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToSaleDetail: (Long) -> Unit
) {
    val customersWithStats by customerViewModel.customersWithStats.collectAsState()
    val custStats = customersWithStats.find { it.customer.customerId == customerId }
    val sales by customerViewModel.getCustomerSales(customerId).collectAsState()
    val payments by customerViewModel.getCustomerPayments(customerId).collectAsState()

    var showCollectDueDialog by remember { mutableStateOf(false) }
    var dueAmountInput by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf(PaymentMethod.CASH) }
    var notesInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            AppTopBar(
                title = custStats?.customer?.customerName ?: "Customer Details",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        if (custStats == null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("Customer not found")
            }
        } else {
            val customer = custStats.customer

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Info & Dues Summary Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = customer.customerName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            if (customer.phone.isNotBlank()) {
                                Text(text = "Phone: ${customer.phone}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (customer.address.isNotBlank()) {
                                Text(text = "Address: ${customer.address}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Total Purchases", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                                    Text(CurrencyUtils.formatNpr(custStats.totalPurchases), fontWeight = FontWeight.Bold)
                                }
                                Column {
                                    Text("Total Paid", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                                    Text(CurrencyUtils.formatNpr(custStats.totalPaid), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                                Column {
                                    Text("Total Due", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                                    Text(
                                        CurrencyUtils.formatNpr(custStats.totalDue),
                                        fontWeight = FontWeight.Bold,
                                        color = if (custStats.totalDue > 0) MaterialTheme.colorScheme.error else Color(0xFF2E7D32)
                                    )
                                }
                            }

                            if (custStats.totalDue > 0) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = {
                                        dueAmountInput = custStats.totalDue.toString()
                                        showCollectDueDialog = true
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp)
                                        .testTag("collect_due_button"),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Payment, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Collect Due Payment")
                                }
                            }
                        }
                    }
                }

                // Invoices History Header
                item {
                    Text(
                        text = "Sales Invoices (${sales.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (sales.isEmpty()) {
                    item {
                        Text("No sales invoices found for this customer.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    items(sales, key = { it.saleId }) { sale ->
                        SaleCard(sale = sale, onClick = { onNavigateToSaleDetail(sale.saleId) })
                    }
                }

                // Payments History Header
                if (payments.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Payment History (${payments.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    items(payments, key = { it.paymentId }) { payment ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "${payment.paymentMethod} Payment",
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = DateUtils.formatDateTime(payment.paymentDate),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                    if (payment.notes.isNotBlank()) {
                                        Text(
                                            text = payment.notes,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Text(
                                    text = CurrencyUtils.formatNpr(payment.amount),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }

    if (showCollectDueDialog) {
        AlertDialog(
            onDismissRequest = { showCollectDueDialog = false },
            title = { Text("Collect Due Payment") },
            text = {
                Column {
                    Text("Total Outstanding: ${CurrencyUtils.formatNpr(custStats?.totalDue ?: 0.0)}", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = dueAmountInput,
                        onValueChange = { dueAmountInput = it },
                        label = { Text("Amount Received (Rs.) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { paymentMethod = PaymentMethod.CASH },
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = if (paymentMethod == PaymentMethod.CASH) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cash", color = if (paymentMethod == PaymentMethod.CASH) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Button(
                            onClick = { paymentMethod = PaymentMethod.DIGITAL },
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = if (paymentMethod == PaymentMethod.DIGITAL) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Digital", color = if (paymentMethod == PaymentMethod.DIGITAL) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = notesInput,
                        onValueChange = { notesInput = it },
                        label = { Text("Notes / Reference (Optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val amount = dueAmountInput.toDoubleOrNull() ?: 0.0
                    if (amount > 0) {
                        customerViewModel.collectDue(
                            customerId = customerId,
                            amount = amount,
                            method = paymentMethod,
                            notes = notesInput,
                            onDone = { showCollectDueDialog = false }
                        )
                    }
                }) {
                    Text("Confirm Payment")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCollectDueDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
