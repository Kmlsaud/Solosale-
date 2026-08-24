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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.solosale.ui.components.AppTopBar
import com.example.solosale.ui.components.EmptyState
import com.example.solosale.ui.components.SaleCard
import com.example.solosale.ui.components.StatCard
import com.example.solosale.utils.CurrencyUtils
import com.example.solosale.utils.DateUtils
import com.example.solosale.viewmodel.PaymentViewModel

@Composable
fun PaymentsScreen(
    paymentViewModel: PaymentViewModel,
    onMenuClick: () -> Unit,
    onNavigateToSaleDetail: (Long) -> Unit
) {
    val allPayments by paymentViewModel.allPayments.collectAsState()
    val salesWithDue by paymentViewModel.salesWithDue.collectAsState()
    val totalDue by paymentViewModel.totalDue.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Payments & Due Collection",
                onMenuClick = onMenuClick
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Outstanding Stat Card
            StatCard(
                title = "Total Outstanding Credit Due",
                value = CurrencyUtils.formatNpr(totalDue),
                subtitle = "${salesWithDue.size} pending invoices",
                icon = Icons.Default.Warning,
                iconColor = if (totalDue > 0) Color(0xFFC62828) else Color(0xFF2E7D32),
                iconBgColor = if (totalDue > 0) Color(0xFFFFEBEE) else Color(0xFFE8F5E9),
                modifier = Modifier.padding(16.dp),
                testTag = "payments_total_due_card"
            )

            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Pending Dues (${salesWithDue.size})") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Payment Receipts (${allPayments.size})") }
                )
            }

            if (selectedTab == 0) {
                if (salesWithDue.isEmpty()) {
                    EmptyState(
                        title = "No Pending Dues",
                        message = "All customer sales are fully paid up!",
                        icon = Icons.Default.Payment,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(salesWithDue, key = { it.saleId }) { sale ->
                            SaleCard(
                                sale = sale,
                                onClick = { onNavigateToSaleDetail(sale.saleId) }
                            )
                        }
                    }
                }
            } else {
                if (allPayments.isEmpty()) {
                    EmptyState(
                        title = "No Payment Receipts",
                        message = "No payments have been recorded yet.",
                        icon = Icons.Default.AttachMoney,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(allPayments, key = { it.paymentId }) { payment ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Receipt #${payment.paymentId}",
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${payment.paymentMethod} • ${DateUtils.formatDateTime(payment.paymentDate)}",
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
