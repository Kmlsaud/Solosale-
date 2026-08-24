package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.solosale.data.local.AppDatabase
import com.example.solosale.data.repository.AuthRepository
import com.example.solosale.data.repository.CustomerRepository
import com.example.solosale.data.repository.InventoryRepository
import com.example.solosale.data.repository.PaymentRepository
import com.example.solosale.data.repository.ProductRepository
import com.example.solosale.data.repository.PurchaseRepository
import com.example.solosale.data.repository.ReportRepository
import com.example.solosale.data.repository.SalesRepository
import com.example.solosale.data.repository.SettingsRepository
import com.example.solosale.data.repository.SupplierRepository
import com.example.solosale.ui.navigation.AppNavigation
import com.example.solosale.ui.theme.SoloSaleTheme
import com.example.solosale.utils.TokenManager
import com.example.solosale.viewmodel.AuthViewModel
import com.example.solosale.viewmodel.CustomerViewModel
import com.example.solosale.viewmodel.DashboardViewModel
import com.example.solosale.viewmodel.InventoryViewModel
import com.example.solosale.viewmodel.PaymentViewModel
import com.example.solosale.viewmodel.ProductViewModel
import com.example.solosale.viewmodel.PurchaseViewModel
import com.example.solosale.viewmodel.ReportViewModel
import com.example.solosale.viewmodel.SalesViewModel
import com.example.solosale.viewmodel.SettingsViewModel
import com.example.solosale.viewmodel.UserManagementViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Singletons
        val database = AppDatabase.getDatabase(applicationContext)
        val tokenManager = TokenManager(applicationContext)

        val authRepository = AuthRepository(
            userDao = database.userDao(),
            sessionDao = database.sessionDao(),
            tokenManager = tokenManager
        )
        val productRepository = ProductRepository(database.productDao())
        val salesRepository = SalesRepository(database)
        val customerRepository = CustomerRepository(database.customerDao(), database.saleDao(), database.paymentDao())
        val purchaseRepository = PurchaseRepository(database)
        val supplierRepository = SupplierRepository(database.supplierDao())
        val paymentRepository = PaymentRepository(database)
        val inventoryRepository = InventoryRepository(database)
        val reportRepository = ReportRepository(database)
        val settingsRepository = SettingsRepository(database)

        // ViewModels
        val authViewModel = AuthViewModel(authRepository, tokenManager)
        val dashboardViewModel = DashboardViewModel(
            salesRepository = salesRepository,
            productRepository = productRepository,
            customerRepository = customerRepository,
            reportRepository = reportRepository,
            settingsRepository = settingsRepository
        )
        val productViewModel = ProductViewModel(productRepository, inventoryRepository)
        val salesViewModel = SalesViewModel(
            salesRepository = salesRepository,
            productRepository = productRepository,
            customerRepository = customerRepository,
            settingsRepository = settingsRepository,
            tokenManager = tokenManager
        )
        val customerViewModel = CustomerViewModel(customerRepository, salesRepository, paymentRepository)
        val paymentViewModel = PaymentViewModel(paymentRepository, salesRepository)
        val purchaseViewModel = PurchaseViewModel(purchaseRepository, supplierRepository, productRepository)
        val inventoryViewModel = InventoryViewModel(inventoryRepository, productRepository)
        val reportViewModel = ReportViewModel(reportRepository)
        val settingsViewModel = SettingsViewModel(settingsRepository)
        val userManagementViewModel = UserManagementViewModel(authRepository)

        setContent {
            SoloSaleTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation(
                        authViewModel = authViewModel,
                        dashboardViewModel = dashboardViewModel,
                        productViewModel = productViewModel,
                        salesViewModel = salesViewModel,
                        customerViewModel = customerViewModel,
                        paymentViewModel = paymentViewModel,
                        purchaseViewModel = purchaseViewModel,
                        inventoryViewModel = inventoryViewModel,
                        reportViewModel = reportViewModel,
                        settingsViewModel = settingsViewModel,
                        userManagementViewModel = userManagementViewModel
                    )
                }
            }
        }
    }
}
