package com.example.solosale.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.solosale.data.local.dao.TopProductSummary
import com.example.solosale.data.local.entity.BusinessSettingsEntity
import com.example.solosale.data.local.entity.SaleEntity
import com.example.solosale.data.repository.CustomerRepository
import com.example.solosale.data.repository.DailySalesDataPoint
import com.example.solosale.data.repository.ProductRepository
import com.example.solosale.data.repository.ReportRepository
import com.example.solosale.data.repository.SalesRepository
import com.example.solosale.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DashboardStats(
    val todaySales: Double = 0.0,
    val totalRevenue: Double = 0.0,
    val totalDue: Double = 0.0,
    val totalProducts: Int = 0,
    val lowStockCount: Int = 0,
    val outOfStockCount: Int = 0,
    val totalCustomers: Int = 0
)

class DashboardViewModel(
    private val salesRepository: SalesRepository,
    private val productRepository: ProductRepository,
    private val customerRepository: CustomerRepository,
    private val reportRepository: ReportRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val settings: StateFlow<BusinessSettingsEntity?> = settingsRepository.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BusinessSettingsEntity()
    )

    val todaySales: StateFlow<Double> = salesRepository.getTodayRevenue().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    val totalRevenue: StateFlow<Double> = salesRepository.totalRevenue.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    val totalDue: StateFlow<Double> = salesRepository.totalDue.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    val totalProducts: StateFlow<Int> = productRepository.totalProductsCount.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val lowStockCount: StateFlow<Int> = productRepository.lowStockCount.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val outOfStockCount: StateFlow<Int> = productRepository.outOfStockCount.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val totalCustomers: StateFlow<Int> = customerRepository.totalCustomersCount.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val recentSales: StateFlow<List<SaleEntity>> = salesRepository.recentSales.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val topSellingProducts: StateFlow<List<TopProductSummary>> = salesRepository.getTopSellingProducts(5).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _weeklyChartData = MutableStateFlow<List<DailySalesDataPoint>>(emptyList())
    val weeklyChartData: StateFlow<List<DailySalesDataPoint>> = _weeklyChartData.asStateFlow()

    init {
        loadWeeklyChart()
    }

    fun loadWeeklyChart() {
        viewModelScope.launch {
            _weeklyChartData.value = reportRepository.getWeeklySalesChartData()
        }
    }
}
