package com.example.solosale.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.solosale.data.local.entity.PaymentMethod
import com.example.solosale.data.local.entity.SaleEntity
import com.example.solosale.data.repository.CustomerWithStats
import com.example.solosale.ui.theme.GeometricCashGreen
import com.example.solosale.ui.theme.GeometricCashGreenContainer
import com.example.solosale.ui.theme.GeometricDigitalPurple
import com.example.solosale.ui.theme.GeometricDigitalPurpleContainer
import com.example.solosale.ui.theme.GeometricDueRed
import com.example.solosale.ui.theme.GeometricDueRedContainer
import com.example.solosale.ui.theme.GeometricDueRedOnContainer
import com.example.solosale.utils.CurrencyUtils
import com.example.solosale.utils.DateUtils

@Composable
fun SaleCard(
    sale: SaleEntity,
    onClick: () -> Unit,
    currencySymbol: String = "Rs.",
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("sale_card_${sale.saleId}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = sale.invoiceNumber,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Customer: ${sale.customerName.ifBlank { "Walk-in" }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Geometric Pill Payment Badge
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
                            PaymentMethod.DIGITAL -> "E-SEWA / QR"
                            PaymentMethod.CREDIT -> "CREDIT"
                        },
                        color = when (sale.paymentMethod) {
                            PaymentMethod.CASH -> GeometricCashGreen
                            PaymentMethod.DIGITAL -> GeometricDigitalPurple
                            PaymentMethod.CREDIT -> GeometricDueRedOnContainer
                        },
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = DateUtils.formatDateTime(sale.saleDate),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = CurrencyUtils.formatNpr(sale.grandTotal, currencySymbol),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (sale.dueAmount > 0) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Surface(
                        color = GeometricDueRedContainer,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "Due: ${CurrencyUtils.formatNpr(sale.dueAmount, currencySymbol)}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            fontWeight = FontWeight.Bold,
                            color = GeometricDueRedOnContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CustomerCard(
    customerWithStats: CustomerWithStats,
    onClick: () -> Unit,
    currencySymbol: String = "Rs.",
    modifier: Modifier = Modifier
) {
    val customer = customerWithStats.customer

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("customer_card_${customer.customerId}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = customer.customerName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (customer.phone.isNotBlank()) {
                        Text(
                            text = customer.phone,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (customerWithStats.totalDue > 0) {
                    Surface(
                        color = GeometricDueRedContainer,
                        shape = RoundedCornerShape(100.dp)
                    ) {
                        Text(
                            text = "Due: ${CurrencyUtils.formatNpr(customerWithStats.totalDue, currencySymbol)}",
                            color = GeometricDueRedOnContainer,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                } else {
                    Surface(
                        color = GeometricCashGreenContainer,
                        shape = RoundedCornerShape(100.dp)
                    ) {
                        Text(
                            text = "NO DUE",
                            color = GeometricCashGreen,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total: ${CurrencyUtils.formatNpr(customerWithStats.totalPurchases, currencySymbol)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Paid: ${CurrencyUtils.formatNpr(customerWithStats.totalPaid, currencySymbol)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
