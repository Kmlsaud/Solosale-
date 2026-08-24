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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.solosale.data.local.entity.PaymentMethod
import com.example.solosale.data.local.entity.SaleEntity
import com.example.solosale.ui.components.AppBottomBar
import com.example.solosale.ui.components.AppSearchBar
import com.example.solosale.ui.components.AppTopBar
import com.example.solosale.ui.components.EmptyState
import com.example.solosale.ui.components.SaleCard
import com.example.solosale.viewmodel.SalesViewModel

enum class SaleFilterOption(val label: String) {
    ALL("All Sales"),
    CASH("Cash"),
    DIGITAL("Digital"),
    CREDIT_DUE("With Due")
}

@Composable
fun SalesScreen(
    salesViewModel: SalesViewModel,
    onMenuClick: () -> Unit,
    onNavigateToPos: () -> Unit,
    onNavigateToSaleDetail: (Long) -> Unit,
    onNavigateBottom: (String) -> Unit
) {
    val allSales by salesViewModel.allSales.collectAsState()
    val settings by salesViewModel.settings.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var filterOption by remember { mutableStateOf(SaleFilterOption.ALL) }

    val filteredSales = allSales.filter { sale ->
        val matchesQuery = searchQuery.isBlank() ||
                sale.invoiceNumber.contains(searchQuery, ignoreCase = true) ||
                sale.customerName.contains(searchQuery, ignoreCase = true) ||
                sale.customerPhone.contains(searchQuery, ignoreCase = true)

        val matchesFilter = when (filterOption) {
            SaleFilterOption.ALL -> true
            SaleFilterOption.CASH -> sale.paymentMethod == PaymentMethod.CASH
            SaleFilterOption.DIGITAL -> sale.paymentMethod == PaymentMethod.DIGITAL
            SaleFilterOption.CREDIT_DUE -> sale.dueAmount > 0
        }
        matchesQuery && matchesFilter
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Sales & Invoices",
                onMenuClick = onMenuClick
            )
        },
        bottomBar = {
            AppBottomBar(currentRoute = "sales", onNavigate = onNavigateBottom)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToPos,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("sales_new_sale_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Sale")
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
                onQueryChange = { searchQuery = it },
                placeholder = "Search by invoice # or customer name...",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(SaleFilterOption.values()) { option ->
                    FilterChip(
                        selected = filterOption == option,
                        onClick = { filterOption = option },
                        label = { Text(option.label) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredSales.isEmpty()) {
                EmptyState(
                    title = "No Sales Found",
                    message = "No invoices match the selected criteria.",
                    icon = Icons.Default.ReceiptLong,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredSales, key = { it.saleId }) { sale ->
                        SaleCard(
                            sale = sale,
                            onClick = { onNavigateToSaleDetail(sale.saleId) },
                            currencySymbol = settings.currencySymbol
                        )
                    }
                }
            }
        }
    }
}
