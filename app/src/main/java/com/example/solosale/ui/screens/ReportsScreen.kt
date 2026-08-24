package com.example.solosale.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.solosale.data.local.entity.PaymentMethod
import com.example.solosale.data.repository.ReportDateFilter
import com.example.solosale.ui.components.AppBottomBar
import com.example.solosale.ui.components.AppTopBar
import com.example.solosale.ui.components.StatCard
import com.example.solosale.ui.theme.GeometricCashGreen
import com.example.solosale.ui.theme.GeometricCashGreenContainer
import com.example.solosale.ui.theme.GeometricDigitalPurple
import com.example.solosale.ui.theme.GeometricDigitalPurpleContainer
import com.example.solosale.ui.theme.GeometricDueRed
import com.example.solosale.ui.theme.GeometricDueRedContainer
import com.example.solosale.ui.theme.GeometricDueRedOnContainer
import com.example.solosale.ui.theme.GeometricOnPrimaryContainer
import com.example.solosale.ui.theme.GeometricPrimary
import com.example.solosale.ui.theme.GeometricPrimaryContainer
import com.example.solosale.ui.theme.GeometricSecondary
import com.example.solosale.ui.theme.GeometricSecondaryContainer
import com.example.solosale.ui.theme.GeometricWarningAmber
import com.example.solosale.ui.theme.GeometricWarningContainer
import com.example.solosale.utils.CurrencyUtils
import com.example.solosale.utils.DateUtils
import com.example.solosale.viewmodel.ReportViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    reportViewModel: ReportViewModel,
    onMenuClick: () -> Unit,
    onNavigateToSaleDetail: (Long) -> Unit = {},
    onNavigateBottom: (String) -> Unit = {}
) {
    val reportData by reportViewModel.reportData.collectAsState()
    val selectedFilter by reportViewModel.selectedFilter.collectAsState()
    val isLoading by reportViewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Sales & Profit Reports",
                subtitle = "Analytics",
                onMenuClick = onMenuClick
            )
        },
        bottomBar = {
            AppBottomBar(currentRoute = "reports", onNavigate = onNavigateBottom)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Filter Pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    ReportDateFilter.TODAY,
                    ReportDateFilter.THIS_WEEK,
                    ReportDateFilter.THIS_MONTH,
                    ReportDateFilter.ALL_TIME
                ).forEach { filter ->
                    val selected = selectedFilter == filter
                    FilterChip(
                        selected = selected,
                        onClick = { reportViewModel.setDateFilter(filter) },
                        label = {
                            Text(
                                text = filter.label,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        shape = RoundedCornerShape(100.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GeometricPrimaryContainer,
                            selectedLabelColor = GeometricOnPrimaryContainer
                        ),
                        modifier = Modifier.testTag("report_filter_${filter.name}")
                    )
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GeometricPrimary)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Profit & Revenue Hero Card (Geometric Balance Style)
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reports_profit_card"),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = GeometricPrimaryContainer),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = "ESTIMATED NET PROFIT",
                                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = GeometricOnPrimaryContainer
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = CurrencyUtils.formatNpr(reportData.estimatedProfit),
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (reportData.estimatedProfit >= 0) GeometricCashGreen else GeometricDueRed
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Revenue: ${CurrencyUtils.formatNpr(reportData.totalRevenue)}  •  Cost of Goods: ${CurrencyUtils.formatNpr(reportData.totalCostOfGoodsSold)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GeometricOnPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }

                    // 2x2 Grid of Financial Breakdown
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            StatCard(
                                title = "Total Sales",
                                value = "${reportData.totalSalesCount} bills",
                                subtitle = CurrencyUtils.formatNpr(reportData.totalRevenue),
                                icon = Icons.Default.ReceiptLong,
                                iconColor = GeometricPrimary,
                                iconBgColor = GeometricPrimaryContainer,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "Total Discounts",
                                value = CurrencyUtils.formatNpr(reportData.totalDiscount),
                                subtitle = "Given to customers",
                                icon = Icons.Default.AttachMoney,
                                iconColor = GeometricWarningAmber,
                                iconBgColor = GeometricWarningContainer,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            StatCard(
                                title = "Tax / VAT",
                                value = CurrencyUtils.formatNpr(reportData.totalTax),
                                icon = Icons.Default.TrendingUp,
                                iconColor = GeometricSecondary,
                                iconBgColor = GeometricSecondaryContainer,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "Credit Dues",
                                value = CurrencyUtils.formatNpr(reportData.totalDue),
                                subtitle = if (reportData.totalDue > 0) "Unpaid balance" else "Cleared",
                                icon = Icons.Default.PointOfSale,
                                iconColor = if (reportData.totalDue > 0) GeometricDueRedOnContainer else GeometricCashGreen,
                                iconBgColor = if (reportData.totalDue > 0) GeometricDueRedContainer else GeometricCashGreenContainer,
                                containerColor = if (reportData.totalDue > 0) GeometricDueRedContainer else MaterialTheme.colorScheme.surface,
                                contentColor = if (reportData.totalDue > 0) GeometricDueRedOnContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                valueColor = if (reportData.totalDue > 0) GeometricDueRedOnContainer else GeometricCashGreen,
                                border = if (reportData.totalDue > 0) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Payment Method Breakdown
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = "Payment Method Breakdown",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Cash Received", style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        CurrencyUtils.formatNpr(reportData.cashSales),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = GeometricCashGreen
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Digital / QR Payments", style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        CurrencyUtils.formatNpr(reportData.digitalSales),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = GeometricPrimary
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Credit (Due) Sales", style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        CurrencyUtils.formatNpr(reportData.creditSales),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = GeometricDueRed
                                    )
                                }
                            }
                        }
                    }

                    // Sales Transaction List in Period
                    if (reportData.salesList.isNotEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "Sales in this Period (${reportData.salesList.size})",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                                    )

                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                                    reportData.salesList.forEachIndexed { index, sale ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { onNavigateToSaleDetail(sale.saleId) }
                                                .padding(horizontal = 20.dp, vertical = 12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = sale.invoiceNumber,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                Text(
                                                    text = "${sale.customerName.ifBlank { "Walk-in" }} • ${DateUtils.formatDateTime(sale.saleDate)}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }

                                            Text(
                                                text = CurrencyUtils.formatNpr(sale.grandTotal),
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }

                                        if (index < reportData.salesList.size - 1) {
                                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                        }
                                    }
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
    }
}
