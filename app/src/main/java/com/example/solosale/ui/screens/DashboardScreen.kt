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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.solosale.data.local.entity.PaymentMethod
import com.example.solosale.ui.components.AppBottomBar
import com.example.solosale.ui.components.AppTopBar
import com.example.solosale.ui.components.SalesWeeklyBarChart
import com.example.solosale.ui.components.StatCard
import com.example.solosale.ui.theme.GeometricBorder
import com.example.solosale.ui.theme.GeometricBorderLight
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
import com.example.solosale.utils.CurrencyUtils
import com.example.solosale.utils.DateUtils
import com.example.solosale.viewmodel.DashboardViewModel

@Composable
fun DashboardScreen(
    dashboardViewModel: DashboardViewModel,
    onMenuClick: () -> Unit,
    onNavigateToPos: () -> Unit,
    onNavigateToProducts: () -> Unit,
    onNavigateToCustomers: () -> Unit,
    onNavigateToPurchases: () -> Unit,
    onNavigateToSales: () -> Unit,
    onNavigateToSaleDetail: (Long) -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateBottom: (String) -> Unit
) {
    val todaySales by dashboardViewModel.todaySales.collectAsState()
    val totalRevenue by dashboardViewModel.totalRevenue.collectAsState()
    val totalDue by dashboardViewModel.totalDue.collectAsState()
    val totalProducts by dashboardViewModel.totalProducts.collectAsState()
    val lowStockCount by dashboardViewModel.lowStockCount.collectAsState()
    val outOfStockCount by dashboardViewModel.outOfStockCount.collectAsState()
    val totalCustomers by dashboardViewModel.totalCustomers.collectAsState()
    val recentSales by dashboardViewModel.recentSales.collectAsState()
    val topProducts by dashboardViewModel.topSellingProducts.collectAsState()
    val weeklyChartData by dashboardViewModel.weeklyChartData.collectAsState()
    val settings by dashboardViewModel.settings.collectAsState()

    val symbol = settings?.currencySymbol ?: "Rs."

    Scaffold(
        topBar = {
            AppTopBar(
                title = settings?.businessName ?: "SoloSale",
                subtitle = "Dashboard",
                onMenuClick = onMenuClick,
                actions = {
                    IconButton(
                        onClick = onNavigateToNotifications,
                        modifier = Modifier.testTag("dashboard_notifications_btn")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(GeometricPrimaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Alerts",
                                tint = GeometricOnPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                            if (lowStockCount > 0 || outOfStockCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(GeometricDueRed)
                                        .align(Alignment.TopEnd)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(GeometricPrimary)
                            .clickable { onMenuClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (settings?.businessName?.take(2) ?: "SS").uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                }
            )
        },
        bottomBar = {
            AppBottomBar(currentRoute = "dashboard", onNavigate = onNavigateBottom)
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Geometric 2x2 Grid of Stat Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Card 1: Today's Sales (Solid Ice Blue container)
                    StatCard(
                        title = "Today's Sales",
                        value = CurrencyUtils.formatNprShort(todaySales, symbol),
                        icon = Icons.Default.ReceiptLong,
                        iconColor = GeometricOnPrimaryContainer,
                        iconBgColor = GeometricPrimaryContainer.copy(alpha = 0.5f),
                        containerColor = GeometricPrimaryContainer,
                        contentColor = GeometricOnPrimaryContainer,
                        valueColor = GeometricOnPrimaryContainer,
                        border = null,
                        modifier = Modifier.weight(1f),
                        testTag = "stat_today_sales"
                    )

                    // Card 2: Total Revenue (Clean White container with geometric border)
                    StatCard(
                        title = "Total Revenue",
                        value = CurrencyUtils.formatNprShort(totalRevenue, symbol),
                        icon = Icons.Default.AttachMoney,
                        iconColor = GeometricPrimary,
                        iconBgColor = GeometricPrimaryContainer.copy(alpha = 0.4f),
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        valueColor = GeometricPrimary,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier.weight(1f),
                        testTag = "stat_total_revenue"
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Card 3: Low Stock (Clean White container with alert value)
                    val stockItemsText = if (lowStockCount + outOfStockCount < 10) "0${lowStockCount + outOfStockCount} Items" else "${lowStockCount + outOfStockCount} Items"
                    StatCard(
                        title = "Low Stock",
                        value = stockItemsText,
                        subtitle = "$outOfStockCount out of stock",
                        icon = Icons.Default.Inventory2,
                        iconColor = GeometricDueRed,
                        iconBgColor = GeometricDueRedContainer.copy(alpha = 0.6f),
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        valueColor = GeometricDueRed,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToProducts,
                        testTag = "stat_products"
                    )

                    // Card 4: Total Due (Solid Warm Red container)
                    StatCard(
                        title = "Total Due",
                        value = CurrencyUtils.formatNprShort(totalDue, symbol),
                        subtitle = if (totalDue > 0) "Needs collection" else "Zero pending dues",
                        icon = Icons.Default.Warning,
                        iconColor = GeometricDueRedOnContainer,
                        iconBgColor = GeometricDueRedContainer.copy(alpha = 0.5f),
                        containerColor = GeometricDueRedContainer,
                        contentColor = GeometricDueRedOnContainer,
                        valueColor = GeometricDueRedOnContainer,
                        border = null,
                        modifier = Modifier.weight(1f),
                        testTag = "stat_total_due"
                    )
                }
            }

            // Geometric Primary POS Billing Action Button
            item {
                Button(
                    onClick = onNavigateToPos,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("dashboard_start_sale_btn"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GeometricPrimary,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "New Sale / Billing",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Quick Action Horizontal Pills
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickActionButton(
                        icon = Icons.Default.Inventory2,
                        label = "+ Product",
                        onClick = onNavigateToProducts,
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionButton(
                        icon = Icons.Default.Group,
                        label = "+ Customer",
                        onClick = onNavigateToCustomers,
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionButton(
                        icon = Icons.Default.ShoppingBag,
                        label = "+ Stock In",
                        onClick = onNavigateToPurchases,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Recent Sales Geometric Container Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column {
                        // Card Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Recent Sales",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "VIEW ALL",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    letterSpacing = 0.5.sp
                                ),
                                fontWeight = FontWeight.Bold,
                                color = GeometricPrimary,
                                modifier = Modifier.clickable { onNavigateToSales() }
                            )
                        }

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant,
                            thickness = 1.dp
                        )

                        if (recentSales.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No sales yet. Tap 'New Sale / Billing' to begin!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            recentSales.take(5).forEachIndexed { index, sale ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onNavigateToSaleDetail(sale.saleId) }
                                        .padding(horizontal = 20.dp, vertical = 14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = sale.invoiceNumber,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Customer: ${sale.customerName.ifBlank { "Walk-in" }}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = CurrencyUtils.formatNpr(sale.grandTotal, symbol),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Surface(
                                            color = when (sale.paymentMethod) {
                                                PaymentMethod.CASH -> GeometricCashGreenContainer
                                                PaymentMethod.DIGITAL -> GeometricDigitalPurpleContainer
                                                PaymentMethod.CREDIT -> GeometricDueRedContainer
                                            },
                                            shape = RoundedCornerShape(100.dp)
                                        ) {
                                            Text(
                                                text = when (sale.paymentMethod) {
                                                    PaymentMethod.CASH -> "CASH"
                                                    PaymentMethod.DIGITAL -> "E-SEWA"
                                                    PaymentMethod.CREDIT -> "CREDIT"
                                                },
                                                color = when (sale.paymentMethod) {
                                                    PaymentMethod.CASH -> GeometricCashGreen
                                                    PaymentMethod.DIGITAL -> GeometricDigitalPurple
                                                    PaymentMethod.CREDIT -> GeometricDueRedOnContainer
                                                },
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                }

                                if (index < recentSales.take(5).size - 1) {
                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.outlineVariant,
                                        thickness = 1.dp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 7-Day Interactive Sales Chart
            item {
                SalesWeeklyBarChart(
                    dataPoints = weeklyChartData,
                    currencySymbol = symbol
                )
            }

            // Top Selling Products Section
            if (topProducts.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Best Selling Products",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(
                                    imageVector = Icons.Default.Inventory2,
                                    contentDescription = null,
                                    tint = GeometricPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant,
                                thickness = 1.dp
                            )

                            topProducts.forEachIndexed { index, item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.productName,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "Sold: ${item.totalQtySold.toInt()} units",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = CurrencyUtils.formatNpr(item.totalRevenue, symbol),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = GeometricPrimary
                                    )
                                }

                                if (index < topProducts.size - 1) {
                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.outlineVariant,
                                        thickness = 1.dp
                                    )
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

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        contentPadding = PaddingValues(horizontal = 8.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = GeometricPrimary)
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
    }
}
