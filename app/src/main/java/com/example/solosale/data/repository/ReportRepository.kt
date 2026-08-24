package com.example.solosale.data.repository

import com.example.solosale.data.local.AppDatabase
import com.example.solosale.data.local.dao.TopProductSummary
import com.example.solosale.data.local.entity.BusinessSettingsEntity
import com.example.solosale.data.local.entity.PaymentMethod
import com.example.solosale.data.local.entity.SaleEntity
import com.example.solosale.utils.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

enum class ReportDateFilter(val label: String) {
    TODAY("Today"),
    YESTERDAY("Yesterday"),
    THIS_WEEK("This Week"),
    THIS_MONTH("This Month"),
    ALL_TIME("All Time"),
    CUSTOM("Custom")
}

data class FinancialReport(
    val totalSalesCount: Int = 0,
    val totalRevenue: Double = 0.0,
    val totalDiscount: Double = 0.0,
    val totalTax: Double = 0.0,
    val totalCostOfGoodsSold: Double = 0.0,
    val estimatedProfit: Double = 0.0,
    val totalPaid: Double = 0.0,
    val totalDue: Double = 0.0,
    val cashSales: Double = 0.0,
    val digitalSales: Double = 0.0,
    val creditSales: Double = 0.0,
    val topProducts: List<TopProductSummary> = emptyList(),
    val salesList: List<SaleEntity> = emptyList()
)

data class DailySalesDataPoint(
    val label: String, // "Mon", "Tue", etc. or date string
    val amount: Double,
    val timestamp: Long
)

class ReportRepository(private val database: AppDatabase) {
    private val saleDao = database.saleDao()
    private val saleItemDao = database.saleItemDao()

    suspend fun getFinancialReport(startDate: Long, endDate: Long): FinancialReport {
        val sales = saleDao.getSalesBetweenDatesDirect(startDate, endDate)

        var totalRevenue = 0.0
        var totalDiscount = 0.0
        var totalTax = 0.0
        var totalPaid = 0.0
        var totalDue = 0.0
        var cashSales = 0.0
        var digitalSales = 0.0
        var creditSales = 0.0

        var totalCogs = 0.0

        for (sale in sales) {
            totalRevenue += sale.grandTotal
            totalDiscount += sale.discount
            totalTax += sale.tax
            totalPaid += sale.paidAmount
            totalDue += sale.dueAmount

            when (sale.paymentMethod) {
                PaymentMethod.CASH -> cashSales += sale.grandTotal
                PaymentMethod.DIGITAL -> digitalSales += sale.grandTotal
                PaymentMethod.CREDIT -> creditSales += sale.grandTotal
            }

            // Calculate cost of goods sold from line items
            val items = saleItemDao.getItemsForSaleDirect(sale.saleId)
            for (item in items) {
                totalCogs += (item.purchasePrice * item.quantity)
            }
        }

        val estimatedProfit = totalRevenue - totalCogs

        return FinancialReport(
            totalSalesCount = sales.size,
            totalRevenue = totalRevenue,
            totalDiscount = totalDiscount,
            totalTax = totalTax,
            totalCostOfGoodsSold = totalCogs,
            estimatedProfit = estimatedProfit,
            totalPaid = totalPaid,
            totalDue = totalDue,
            cashSales = cashSales,
            digitalSales = digitalSales,
            creditSales = creditSales,
            salesList = sales
        )
    }

    suspend fun getWeeklySalesChartData(): List<DailySalesDataPoint> {
        val list = mutableListOf<DailySalesDataPoint>()
        val cal = java.util.Calendar.getInstance()
        val days = arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

        // Last 7 days
        for (i in 6 downTo 0) {
            val c = java.util.Calendar.getInstance()
            c.add(java.util.Calendar.DAY_OF_YEAR, -i)
            val start = DateUtils.getStartOfDay(c.timeInMillis)
            val end = DateUtils.getEndOfDay(c.timeInMillis)
            val sales = saleDao.getSalesBetweenDatesDirect(start, end)
            val total = sales.sumOf { it.grandTotal }
            val dayName = days[c.get(java.util.Calendar.DAY_OF_WEEK) - 1]
            list.add(DailySalesDataPoint(dayName, total, start))
        }
        return list
    }
}

class SettingsRepository(private val database: AppDatabase) {
    private val settingsDao = database.settingsDao()

    val settingsFlow: Flow<BusinessSettingsEntity> = kotlinx.coroutines.flow.flow {
        settingsDao.getSettingsFlow().collect { entity ->
            emit(entity ?: BusinessSettingsEntity())
        }
    }

    suspend fun getSettings(): BusinessSettingsEntity {
        return settingsDao.getSettings() ?: BusinessSettingsEntity()
    }

    suspend fun updateSettings(settings: BusinessSettingsEntity) {
        settingsDao.insertOrUpdate(settings)
    }
}
