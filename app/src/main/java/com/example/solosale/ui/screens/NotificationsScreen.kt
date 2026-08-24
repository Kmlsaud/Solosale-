package com.example.solosale.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.solosale.ui.components.AppTopBar
import com.example.solosale.ui.components.EmptyState
import com.example.solosale.ui.components.StockBadge
import com.example.solosale.utils.CurrencyUtils
import com.example.solosale.viewmodel.ProductViewModel
import com.example.solosale.viewmodel.SalesViewModel

@Composable
fun NotificationsScreen(
    productViewModel: ProductViewModel,
    salesViewModel: SalesViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEditProduct: (Long) -> Unit,
    onNavigateToSaleDetail: (Long) -> Unit
) {
    val products by productViewModel.products.collectAsState()
    val allSales by salesViewModel.allSales.collectAsState()

    val lowStockProducts = products.filter { it.stockQuantity <= it.minimumStockLevel }
    val dueSales = allSales.filter { it.dueAmount > 0 }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Alerts & Notifications",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        if (lowStockProducts.isEmpty() && dueSales.isEmpty()) {
            EmptyState(
                title = "All Clear!",
                message = "You have no low stock warnings or urgent overdue payments.",
                icon = Icons.Default.CheckCircle,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (lowStockProducts.isNotEmpty()) {
                    item {
                        Text(
                            text = "Low Stock & Out of Stock Alerts (${lowStockProducts.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    items(lowStockProducts, key = { it.productId }) { prod ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(prod.productName, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "Code: ${prod.productCode} • Threshold: ${prod.minimumStockLevel.toInt()} ${prod.unit}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    StockBadge(quantity = prod.stockQuantity, minStock = prod.minimumStockLevel, unit = prod.unit)
                                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                                    Button(
                                        onClick = { onNavigateToEditProduct(prod.productId) },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Restock")
                                    }
                                }
                            }
                        }
                    }
                }

                if (dueSales.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Pending Due Payments (${dueSales.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEF6C00)
                        )
                    }

                    items(dueSales, key = { it.saleId }) { sale ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(sale.customerName, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "Inv #${sale.invoiceNumber} • Phone: ${sale.customerPhone}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Due: ${CurrencyUtils.formatNpr(sale.dueAmount)}",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    Button(
                                        onClick = { onNavigateToSaleDetail(sale.saleId) },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("View")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
