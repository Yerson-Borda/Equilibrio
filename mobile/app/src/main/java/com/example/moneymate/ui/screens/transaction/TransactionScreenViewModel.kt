package com.example.moneymate.ui.screens.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.transaction.model.CategorySummaryData
import com.example.domain.transaction.model.ChartFilter
import com.example.domain.transaction.model.ChartType
import com.example.domain.transaction.model.DateRange
import com.example.domain.transaction.model.MonthlyChartData
import com.example.domain.transaction.model.MonthlyComparisonData
import com.example.domain.transaction.model.PeriodFilter
import com.example.domain.transaction.model.TransactionChartsData
import com.example.domain.transaction.model.TransactionEntity
import com.example.domain.transaction.usecase.GetCategorySummaryUseCase
import com.example.domain.transaction.usecase.GetMonthlyChartDataUseCase
import com.example.domain.transaction.usecase.GetMonthlyComparisonUseCase
import com.example.domain.transaction.usecase.GetRecentTransactionsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TransactionScreenViewModel(
    private val getMonthlyChartDataUseCase: GetMonthlyChartDataUseCase,
    private val getCategorySummaryUseCase: GetCategorySummaryUseCase,
    private val getMonthlyComparisonUseCase: GetMonthlyComparisonUseCase,
    private val getRecentTransactionsUseCase: GetRecentTransactionsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionScreenState())
    val uiState: StateFlow<TransactionScreenState> = _uiState.asStateFlow()

    fun refreshData() {
        loadData()
    }

    init {
        println("DEBUG: ViewModel init - loading data")
        loadData()
    }

    fun loadData() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        loadAllChartData()
        loadRecentTransactions()
    }

    private fun loadAllChartData() {
        viewModelScope.launch {
            try {
                println("DEBUG: Loading all chart data...")

                // Load chart data sequentially
                val monthlyChart = getMonthlyChartDataUseCase.execute(months = 12)
                val defaultDateRange = DateRange(getDefaultStartDate(), getDefaultEndDate())
                val lineChartData = getMonthlyChartDataUseCase.executeForLineChart(defaultDateRange)
                val categorySummary = getCategorySummaryUseCase.execute()
                val monthlyComparison = getMonthlyComparisonUseCase.execute()

                val chartsData = TransactionChartsData(
                    monthlyChart = monthlyChart.copy(
                        days = lineChartData.days,
                        dateRange = defaultDateRange
                    ),
                    categorySummary = categorySummary,
                    monthlyComparison = monthlyComparison,
                    currentChartType = ChartType.MONTHLY_TRENDS
                )

                _uiState.value = _uiState.value.copy(
                    chartsData = chartsData,
                    isLoading = _uiState.value.recentTransactions.isEmpty()
                )
                println("DEBUG: All chart data loaded successfully")

            } catch (e: Exception) {
                println("DEBUG: Chart data error: ${e.message}")
                // On error, use empty/default data but don't show error for charts
                val fallbackChartsData = TransactionChartsData(
                    monthlyChart = MonthlyChartData(
                        months = emptyList(),
                        days = emptyList(),
                        dateRange = DateRange(getDefaultStartDate(), getDefaultEndDate())
                    ),
                    categorySummary = CategorySummaryData(
                        expenses = emptyList(),
                        incomes = emptyList(),
                        totalExpenses = 0.0,
                        totalIncomes = 0.0,
                        netFlow = 0.0
                    ),
                    monthlyComparison = MonthlyComparisonData(
                        categories = emptyList(),
                        selectedMonth = "Current Month"
                    ),
                    currentChartType = ChartType.MONTHLY_TRENDS
                )

                _uiState.value = _uiState.value.copy(
                    chartsData = fallbackChartsData,
                    isLoading = _uiState.value.recentTransactions.isEmpty(),
                    error = "Some chart data failed to load"
                )
            }
        }
    }

    private fun loadRecentTransactions() {
        viewModelScope.launch {
            try {
                println("DEBUG: Loading recent transactions...")
                val result = getRecentTransactionsUseCase.execute(limit = 20)

                if (result.isSuccess) {
                    val transactions = result.getOrNull() ?: emptyList()
                    _uiState.value = _uiState.value.copy(
                        recentTransactions = transactions,
                        isLoading = false
                    )
                    println("DEBUG: Loaded ${transactions.size} transactions")
                } else {
                    _uiState.value = _uiState.value.copy(
                        recentTransactions = emptyList(),
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                println("DEBUG: Recent transactions error: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    recentTransactions = emptyList(),
                    isLoading = false
                )
            }
        }
    }

    // Update chart type (for swipe/tab changes)
    fun updateCurrentChartType(chartType: ChartType) {
        _uiState.value = _uiState.value.copy(
            chartsData = _uiState.value.chartsData.copy(currentChartType = chartType)
        )
    }

    // Update filters for charts
    fun updateChartFilter(filter: ChartFilter) {
        val currentChartsData = _uiState.value.chartsData

        when (currentChartsData.currentChartType) {
            ChartType.MONTHLY_TRENDS -> {
                _uiState.value = _uiState.value.copy(
                    chartsData = currentChartsData.copy(
                        monthlyChart = currentChartsData.monthlyChart.copy(selectedFilter = filter)
                    )
                )
            }
            ChartType.CATEGORY_BREAKDOWN -> {
                _uiState.value = _uiState.value.copy(
                    chartsData = currentChartsData.copy(
                        categorySummary = currentChartsData.categorySummary.copy(selectedFilter = filter)
                    )
                )
            }
            ChartType.MONTHLY_COMPARISON -> {
                // Handle comparison chart filter if needed
            }
        }
    }

    // Update period filter and reload data for charts
    fun updatePeriodFilter(period: PeriodFilter) {
        viewModelScope.launch {
            try {
                when (_uiState.value.chartsData.currentChartType) {
                    ChartType.MONTHLY_TRENDS -> {
                        val months = when (period) {
                            PeriodFilter.YEAR -> 12
                            PeriodFilter.MONTH -> 1
                            else -> 12 // Default for other periods in bar chart
                        }
                        val result = getMonthlyChartDataUseCase.execute(months = months)
                        val currentChartsData = _uiState.value.chartsData
                        _uiState.value = _uiState.value.copy(
                            chartsData = currentChartsData.copy(
                                monthlyChart = result.copy(selectedPeriod = period)
                            )
                        )
                    }
                    ChartType.CATEGORY_BREAKDOWN -> {
                        // For line chart, we now use date range instead of fixed day counts
                        // But we can still handle period filters for quick selections
                        val dateRange = when (period) {
                            PeriodFilter.DAYS_7 -> DateRange(getDateDaysAgo(7), getDefaultEndDate())
                            PeriodFilter.DAYS_15 -> DateRange(getDateDaysAgo(15), getDefaultEndDate())
                            PeriodFilter.DAYS_30 -> DateRange(getDateDaysAgo(30), getDefaultEndDate())
                            PeriodFilter.DAYS_90 -> DateRange(getDateDaysAgo(90), getDefaultEndDate())
                            else -> _uiState.value.chartsData.monthlyChart.dateRange
                        }
                        updateDateRange(dateRange)
                    }
                    ChartType.MONTHLY_COMPARISON -> {
                        // Handle comparison chart period changes if needed
                    }
                }
            } catch (e: Exception) {
                println("DEBUG: Period filter error: ${e.message}")
            }
        }
    }

    // Update date range for line chart
    fun updateDateRange(dateRange: DateRange) {
        viewModelScope.launch {
            try {
                val lineChartData = getMonthlyChartDataUseCase.executeForLineChart(dateRange)
                val currentChartsData = _uiState.value.chartsData
                _uiState.value = _uiState.value.copy(
                    chartsData = currentChartsData.copy(
                        monthlyChart = currentChartsData.monthlyChart.copy(
                            days = lineChartData.days,
                            dateRange = dateRange,
                            selectedPeriod = getPeriodFilterForDateRange(dateRange)
                        )
                    )
                )
                println("DEBUG: Line chart updated with date range: ${dateRange.startDate} to ${dateRange.endDate}")
            } catch (e: Exception) {
                println("DEBUG: Date range update error: ${e.message}")
            }
        }
    }

    // Refresh line chart data with current date range
    fun refreshLineChartData() {
        viewModelScope.launch {
            try {
                val currentDateRange = _uiState.value.chartsData.monthlyChart.dateRange
                val lineChartData = getMonthlyChartDataUseCase.executeForLineChart(currentDateRange)
                val currentChartsData = _uiState.value.chartsData
                _uiState.value = _uiState.value.copy(
                    chartsData = currentChartsData.copy(
                        monthlyChart = currentChartsData.monthlyChart.copy(
                            days = lineChartData.days
                        )
                    )
                )
                println("DEBUG: Line chart data refreshed")
            } catch (e: Exception) {
                println("DEBUG: Line chart refresh error: ${e.message}")
            }
        }
    }

    // Refresh category summary data with custom date range
    fun refreshCategorySummary(startDate: String, endDate: String) {
        viewModelScope.launch {
            try {
                val categorySummary = getCategorySummaryUseCase.execute(startDate, endDate)
                val currentChartsData = _uiState.value.chartsData
                _uiState.value = _uiState.value.copy(
                    chartsData = currentChartsData.copy(
                        categorySummary = categorySummary
                    )
                )
                println("DEBUG: Category summary refreshed with custom date range")
            } catch (e: Exception) {
                println("DEBUG: Category summary refresh error: ${e.message}")
            }
        }
    }

    // Refresh monthly comparison data
    fun refreshMonthlyComparison(month: String) {
        viewModelScope.launch {
            try {
                val monthlyComparison = getMonthlyComparisonUseCase.execute(month)
                val currentChartsData = _uiState.value.chartsData
                _uiState.value = _uiState.value.copy(
                    chartsData = currentChartsData.copy(
                        monthlyComparison = monthlyComparison
                    )
                )
                println("DEBUG: Monthly comparison refreshed")
            } catch (e: Exception) {
                println("DEBUG: Monthly comparison refresh error: ${e.message}")
            }
        }
    }

    // Refresh specific chart data
    fun refreshChartData(chartType: ChartType) {
        viewModelScope.launch {
            when (chartType) {
                ChartType.MONTHLY_TRENDS -> {
                    try {
                        val monthlyChart = getMonthlyChartDataUseCase.execute(months = 12)
                        val currentChartsData = _uiState.value.chartsData
                        _uiState.value = _uiState.value.copy(
                            chartsData = currentChartsData.copy(
                                monthlyChart = monthlyChart.copy(
                                    dateRange = currentChartsData.monthlyChart.dateRange,
                                    days = currentChartsData.monthlyChart.days
                                )
                            )
                        )
                    } catch (e: Exception) {
                        println("DEBUG: Monthly chart refresh error: ${e.message}")
                    }
                }
                ChartType.CATEGORY_BREAKDOWN -> {
                    try {
                        // Refresh line chart data with current date range
                        refreshLineChartData()
                    } catch (e: Exception) {
                        println("DEBUG: Line chart refresh error: ${e.message}")
                    }
                }
                ChartType.MONTHLY_COMPARISON -> {
                    try {
                        val monthlyComparison = getMonthlyComparisonUseCase.execute()
                        val currentChartsData = _uiState.value.chartsData
                        _uiState.value = _uiState.value.copy(
                            chartsData = currentChartsData.copy(
                                monthlyComparison = monthlyComparison
                            )
                        )
                    } catch (e: Exception) {
                        println("DEBUG: Monthly comparison refresh error: ${e.message}")
                    }
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    // Helper functions for date calculations
    private fun getDefaultStartDate(): String {
        val calendar = java.util.Calendar.getInstance()
        calendar.add(java.util.Calendar.DAY_OF_YEAR, -30) // Default to 30 days ago
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

    private fun getDefaultEndDate(): String {
        val calendar = java.util.Calendar.getInstance()
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

    private fun getDateDaysAgo(days: Int): String {
        val calendar = java.util.Calendar.getInstance()
        calendar.add(java.util.Calendar.DAY_OF_YEAR, -days)
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

    private fun getPeriodFilterForDateRange(dateRange: DateRange): PeriodFilter {
        val start = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).parse(dateRange.startDate)
        val end = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).parse(dateRange.endDate)

        val diff = end.time - start.time
        val days = diff / (1000 * 60 * 60 * 24)

        return when {
            days <= 7 -> PeriodFilter.DAYS_7
            days <= 15 -> PeriodFilter.DAYS_15
            days <= 30 -> PeriodFilter.DAYS_30
            else -> PeriodFilter.DAYS_90
        }
    }
}

data class TransactionScreenState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val chartsData: TransactionChartsData = TransactionChartsData(
        monthlyChart = MonthlyChartData(
            months = emptyList(),
            days = emptyList(),
            dateRange = DateRange(
                startDate = getDefaultStartDate(),
                endDate = getDefaultEndDate()
            ),
            selectedPeriod = PeriodFilter.DAYS_30
        ),
        categorySummary = CategorySummaryData(
            expenses = emptyList(),
            incomes = emptyList(),
            totalExpenses = 0.0,
            totalIncomes = 0.0,
            netFlow = 0.0
        ),
        monthlyComparison = MonthlyComparisonData(
            categories = emptyList(),
            selectedMonth = "Current Month"
        ),
        currentChartType = ChartType.MONTHLY_TRENDS
    ),
    val recentTransactions: List<TransactionEntity> = emptyList()
)

// Helper functions for default dates
private fun getDefaultStartDate(): String {
    val calendar = java.util.Calendar.getInstance()
    calendar.add(java.util.Calendar.DAY_OF_YEAR, -30)
    val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
    return dateFormat.format(calendar.time)
}

private fun getDefaultEndDate(): String {
    val calendar = java.util.Calendar.getInstance()
    val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
    return dateFormat.format(calendar.time)
}