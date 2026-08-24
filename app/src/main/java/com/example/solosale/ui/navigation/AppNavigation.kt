package com.example.solosale.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.solosale.ui.components.AppDrawer
import com.example.solosale.ui.screens.AddEditProductScreen
import com.example.solosale.ui.screens.AddPurchaseScreen
import com.example.solosale.ui.screens.CustomerDetailsScreen
import com.example.solosale.ui.screens.CustomersScreen
import com.example.solosale.ui.screens.DashboardScreen
import com.example.solosale.ui.screens.InitialAdminScreen
import com.example.solosale.ui.screens.InventoryScreen
import com.example.solosale.ui.screens.InvoiceScreen
import com.example.solosale.ui.screens.LoginScreen
import com.example.solosale.ui.screens.NewSaleScreen
import com.example.solosale.ui.screens.NotificationsScreen
import com.example.solosale.ui.screens.PaymentsScreen
import com.example.solosale.ui.screens.ProductsScreen
import com.example.solosale.ui.screens.PurchasesScreen
import com.example.solosale.ui.screens.ReportsScreen
import com.example.solosale.ui.screens.SalesScreen
import com.example.solosale.ui.screens.SettingsScreen
import com.example.solosale.ui.screens.SplashScreen
import com.example.solosale.ui.screens.SuppliersScreen
import com.example.solosale.ui.screens.UserManagementScreen
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
import kotlinx.coroutines.launch

object AppRoutes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val INITIAL_ADMIN = "initial_admin"
    const val DASHBOARD = "dashboard"
    const val NEW_SALE = "new_sale"
    const val PRODUCTS = "products"
    const val ADD_PRODUCT = "add_product"
    const val EDIT_PRODUCT = "edit_product/{productId}"
    const val SALES = "sales"
    const val INVOICE = "invoice/{saleId}"
    const val CUSTOMERS = "customers"
    const val CUSTOMER_DETAILS = "customer_details/{customerId}"
    const val PAYMENTS = "payments"
    const val PURCHASES = "purchases"
    const val ADD_PURCHASE = "add_purchase"
    const val SUPPLIERS = "suppliers"
    const val INVENTORY = "inventory"
    const val REPORTS = "reports"
    const val NOTIFICATIONS = "notifications"
    const val SETTINGS = "settings"
    const val USERS = "users"
}

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel,
    dashboardViewModel: DashboardViewModel,
    productViewModel: ProductViewModel,
    salesViewModel: SalesViewModel,
    customerViewModel: CustomerViewModel,
    paymentViewModel: PaymentViewModel,
    purchaseViewModel: PurchaseViewModel,
    inventoryViewModel: InventoryViewModel,
    reportViewModel: ReportViewModel,
    settingsViewModel: SettingsViewModel,
    userManagementViewModel: UserManagementViewModel
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val userName by authViewModel.currentUserName.collectAsState()
    val userRole by authViewModel.currentUserRole.collectAsState()

    val openDrawer: () -> Unit = {
        scope.launch { drawerState.open() }
    }
    val closeDrawer: () -> Unit = {
        scope.launch { drawerState.close() }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = currentRoute !in listOf(AppRoutes.SPLASH, AppRoutes.LOGIN, AppRoutes.INITIAL_ADMIN),
        drawerContent = {
            AppDrawer(
                currentRoute = currentRoute,
                userName = userName ?: "Staff",
                userRole = userRole ?: "STAFF",
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(AppRoutes.DASHBOARD) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(AppRoutes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onClose = closeDrawer
            )
        }
    ) {
        NavHost(
            navController = navController,
            startDestination = AppRoutes.SPLASH
        ) {
            composable(AppRoutes.SPLASH) {
                SplashScreen(
                    authViewModel = authViewModel,
                    onNavigateToLogin = {
                        navController.navigate(AppRoutes.LOGIN) { popUpTo(AppRoutes.SPLASH) { inclusive = true } }
                    },
                    onNavigateToFirstAdmin = {
                        navController.navigate(AppRoutes.INITIAL_ADMIN) { popUpTo(AppRoutes.SPLASH) { inclusive = true } }
                    },
                    onNavigateToDashboard = {
                        navController.navigate(AppRoutes.DASHBOARD) { popUpTo(AppRoutes.SPLASH) { inclusive = true } }
                    }
                )
            }

            composable(AppRoutes.LOGIN) {
                LoginScreen(
                    authViewModel = authViewModel,
                    onLoginSuccess = {
                        navController.navigate(AppRoutes.DASHBOARD) {
                            popUpTo(AppRoutes.LOGIN) { inclusive = true }
                        }
                    }
                )
            }

            composable(AppRoutes.INITIAL_ADMIN) {
                InitialAdminScreen(
                    authViewModel = authViewModel,
                    onAdminCreated = {
                        navController.navigate(AppRoutes.DASHBOARD) {
                            popUpTo(AppRoutes.INITIAL_ADMIN) { inclusive = true }
                        }
                    }
                )
            }

            composable(AppRoutes.DASHBOARD) {
                DashboardScreen(
                    dashboardViewModel = dashboardViewModel,
                    onMenuClick = openDrawer,
                    onNavigateToPos = { navController.navigate(AppRoutes.NEW_SALE) },
                    onNavigateToProducts = { navController.navigate(AppRoutes.PRODUCTS) },
                    onNavigateToCustomers = { navController.navigate(AppRoutes.CUSTOMERS) },
                    onNavigateToPurchases = { navController.navigate(AppRoutes.PURCHASES) },
                    onNavigateToSales = { navController.navigate(AppRoutes.SALES) },
                    onNavigateToSaleDetail = { saleId -> navController.navigate("invoice/$saleId") },
                    onNavigateToNotifications = { navController.navigate(AppRoutes.NOTIFICATIONS) },
                    onNavigateBottom = { route -> navController.navigate(route) }
                )
            }

            composable(AppRoutes.NEW_SALE) {
                NewSaleScreen(
                    salesViewModel = salesViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onSaleCompleted = { saleId ->
                        navController.navigate("invoice/$saleId") {
                            popUpTo(AppRoutes.DASHBOARD)
                        }
                    }
                )
            }

            composable(AppRoutes.PRODUCTS) {
                ProductsScreen(
                    productViewModel = productViewModel,
                    onMenuClick = openDrawer,
                    onNavigateToAddProduct = { navController.navigate(AppRoutes.ADD_PRODUCT) },
                    onNavigateToEditProduct = { prodId -> navController.navigate("edit_product/$prodId") },
                    onNavigateBottom = { route -> navController.navigate(route) }
                )
            }

            composable(AppRoutes.ADD_PRODUCT) {
                AddEditProductScreen(
                    productId = 0L,
                    productViewModel = productViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = AppRoutes.EDIT_PRODUCT,
                arguments = listOf(navArgument("productId") { type = NavType.LongType })
            ) { backStackEntry ->
                val prodId = backStackEntry.arguments?.getLong("productId") ?: 0L
                AddEditProductScreen(
                    productId = prodId,
                    productViewModel = productViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(AppRoutes.SALES) {
                SalesScreen(
                    salesViewModel = salesViewModel,
                    onMenuClick = openDrawer,
                    onNavigateToPos = { navController.navigate(AppRoutes.NEW_SALE) },
                    onNavigateToSaleDetail = { saleId -> navController.navigate("invoice/$saleId") },
                    onNavigateBottom = { route -> navController.navigate(route) }
                )
            }

            composable(
                route = AppRoutes.INVOICE,
                arguments = listOf(navArgument("saleId") { type = NavType.LongType })
            ) { backStackEntry ->
                val saleId = backStackEntry.arguments?.getLong("saleId") ?: 0L
                InvoiceScreen(
                    saleId = saleId,
                    salesViewModel = salesViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(AppRoutes.CUSTOMERS) {
                CustomersScreen(
                    customerViewModel = customerViewModel,
                    onMenuClick = openDrawer,
                    onNavigateToCustomerDetails = { custId -> navController.navigate("customer_details/$custId") },
                    onNavigateBottom = { route -> navController.navigate(route) }
                )
            }

            composable(
                route = AppRoutes.CUSTOMER_DETAILS,
                arguments = listOf(navArgument("customerId") { type = NavType.LongType })
            ) { backStackEntry ->
                val custId = backStackEntry.arguments?.getLong("customerId") ?: 0L
                CustomerDetailsScreen(
                    customerId = custId,
                    customerViewModel = customerViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToSaleDetail = { saleId -> navController.navigate("invoice/$saleId") }
                )
            }

            composable(AppRoutes.PAYMENTS) {
                PaymentsScreen(
                    paymentViewModel = paymentViewModel,
                    onMenuClick = openDrawer,
                    onNavigateToSaleDetail = { saleId -> navController.navigate("invoice/$saleId") }
                )
            }

            composable(AppRoutes.PURCHASES) {
                PurchasesScreen(
                    purchaseViewModel = purchaseViewModel,
                    onMenuClick = openDrawer,
                    onNavigateToAddPurchase = { navController.navigate(AppRoutes.ADD_PURCHASE) }
                )
            }

            composable(AppRoutes.ADD_PURCHASE) {
                AddPurchaseScreen(
                    purchaseViewModel = purchaseViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(AppRoutes.SUPPLIERS) {
                SuppliersScreen(
                    purchaseViewModel = purchaseViewModel,
                    onMenuClick = openDrawer
                )
            }

            composable(AppRoutes.INVENTORY) {
                InventoryScreen(
                    inventoryViewModel = inventoryViewModel,
                    productViewModel = productViewModel,
                    onMenuClick = openDrawer,
                    onNavigateToEditProduct = { prodId -> navController.navigate("edit_product/$prodId") }
                )
            }

            composable(AppRoutes.REPORTS) {
                ReportsScreen(
                    reportViewModel = reportViewModel,
                    onMenuClick = openDrawer,
                    onNavigateToSaleDetail = { saleId -> navController.navigate("invoice/$saleId") },
                    onNavigateBottom = { route -> navController.navigate(route) }
                )
            }

            composable(AppRoutes.NOTIFICATIONS) {
                NotificationsScreen(
                    productViewModel = productViewModel,
                    salesViewModel = salesViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEditProduct = { prodId -> navController.navigate("edit_product/$prodId") },
                    onNavigateToSaleDetail = { saleId -> navController.navigate("invoice/$saleId") }
                )
            }

            composable(AppRoutes.SETTINGS) {
                SettingsScreen(
                    settingsViewModel = settingsViewModel,
                    onMenuClick = openDrawer
                )
            }

            composable(AppRoutes.USERS) {
                UserManagementScreen(
                    userViewModel = userManagementViewModel,
                    onMenuClick = openDrawer
                )
            }
        }
    }
}
