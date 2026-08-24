package com.example.solosale.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.solosale.data.local.entity.StockAdjustmentEntity
import com.example.solosale.data.repository.FinancialReport
import com.example.solosale.data.repository.InventoryRepository
import com.example.solosale.data.repository.ProductRepository
import com.example.solosale.data.repository.ReportDateFilter
import com.example.solosale.data.repository.ReportRepository
import com.example.solosale.utils.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ReportViewModel(
    private val reportRepository: ReportRepository
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow(ReportDateFilter.THIS_MONTH)
    val selectedFilter = _selectedFilter.asStateFlow()

    private val _customStartDate = MutableStateFlow(DateUtils.getStartOfMonth())
    val customStartDate = _customStartDate.asStateFlow()

    private val _customEndDate = MutableStateFlow(DateUtils.getEndOfDay())
    val customEndDate = _customEndDate.asStateFlow()

    private val _reportData = MutableStateFlow(FinancialReport())
    val reportData = _reportData.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    init {
        loadReport()
    }

    fun setDateFilter(filter: ReportDateFilter) {
        _selectedFilter.value = filter
        loadReport()
    }

    fun setCustomRange(start: Long, end: Long) {
        _customStartDate.value = start
        _customEndDate.value = end
        _selectedFilter.value = ReportDateFilter.CUSTOM
        loadReport()
    }

    fun loadReport() {
        viewModelScope.launch {
            _isLoading.value = true
            val (start, end) = when (_selectedFilter.value) {
                ReportDateFilter.TODAY -> Pair(DateUtils.getStartOfDay(), DateUtils.getEndOfDay())
                ReportDateFilter.YESTERDAY -> Pair(DateUtils.getStartOfYesterday(), DateUtils.getEndOfYesterday())
                ReportDateFilter.THIS_WEEK -> Pair(DateUtils.getStartOfWeek(), DateUtils.getEndOfDay())
                ReportDateFilter.THIS_MONTH -> Pair(DateUtils.getStartOfMonth(), DateUtils.getEndOfDay())
                ReportDateFilter.ALL_TIME -> Pair(0L, System.currentTimeMillis() + 86400000)
                ReportDateFilter.CUSTOM -> Pair(_customStartDate.value, _customEndDate.value)
            }

            _reportData.value = reportRepository.getFinancialReport(start, end)
            _isLoading.value = false
        }
    }
}

class InventoryViewModel(
    private val inventoryRepository: InventoryRepository,
    private val productRepository: ProductRepository
) : ViewModel() {

    val allAdjustments: StateFlow<List<StockAdjustmentEntity>> = inventoryRepository.allAdjustments.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
}
