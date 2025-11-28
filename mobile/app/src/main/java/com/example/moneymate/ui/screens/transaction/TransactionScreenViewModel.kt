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
import com.example.moneymate.utils.ScreenState
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

    init {
        println("DEBUG: ViewModel init - loading data")
        loadData()
    }

    fun refreshData() {
        loadData()
    }

    fun loadData() {
        loadAllChartData()
        loadRecentTransactions()
    }

    fun loadAllChartData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(chartsState = ScreenState.Loading)

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
                        dateRange = defaultDateRange,
                        selectedFilter = ChartFilter.EXPENSES // Set initial filter
                    ),
                    categorySummary = categorySummary,
                    monthlyComparison = monthlyComparison,
                    currentChartType = ChartType.MONTHLY_TRENDS
                )

                _uiState.value = _uiState.value.copy(
                    chartsState = ScreenState.Success(chartsData)
                )
                println("DEBUG: All chart data loaded successfully. Initial filter: ${chartsData.monthlyChart.selectedFilter}")

            } catch (e: Exception) {
                println("DEBUG: Chart data error: ${e.message}")
                // On error, use empty/default data
                val fallbackChartsData = TransactionChartsData(
                    monthlyChart = MonthlyChartData(
                        months = emptyList(),
                        days = emptyList(),
                        dateRange = DateRange(getDefaultStartDate(), getDefaultEndDate()),
                        selectedFilter = ChartFilter.EXPENSES
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
                    chartsState = ScreenState.Error(
                        com.example.moneymate.utils.ErrorHandler.mapExceptionToAppError(e),
                        retryAction = { loadAllChartData() }
                    )
                )
            }
        }
    }

    fun loadRecentTransactions() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(transactionsState = ScreenState.Loading)

            try {
                println("DEBUG: Loading recent transactions...")
                val result = getRecentTransactionsUseCase.execute(limit = 20)

                if (result.isSuccess) {
                    val transactions = result.getOrNull() ?: emptyList()
                    _uiState.value = _uiState.value.copy(
                        transactionsState = ScreenState.Success(transactions)
                    )
                    println("DEBUG: Loaded ${transactions.size} transactions")
                } else {
                    _uiState.value = _uiState.value.copy(
                        transactionsState = ScreenState.Error(
                            com.example.moneymate.utils.ErrorHandler.mapExceptionToAppError(
                                result.exceptionOrNull() ?: Exception("Failed to load transactions")
                            ),
                            retryAction = { loadRecentTransactions() }
                        )
                    )
                }
            } catch (e: Exception) {
                println("DEBUG: Recent transactions error: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    transactionsState = ScreenState.Error(
                        com.example.moneymate.utils.ErrorHandler.mapExceptionToAppError(e),
                        retryAction = { loadRecentTransactions() }
                    )
                )
            }
        }
    }

    // Update chart type (for swipe/tab changes)
    fun updateCurrentChartType(chartType: ChartType) {
        println("DEBUG: updateCurrentChartType called with: $chartType")
        val currentCharts = when (val state = _uiState.value.chartsState) {
            is ScreenState.Success -> {
                println("DEBUG: Current charts state: SUCCESS")
                state.data
            }
            else -> {
                println("DEBUG: Current charts state: ${_uiState.value.chartsState}")
                _uiState.value.chartsData // fallback
            }
        }

        _uiState.value = _uiState.value.copy(
            chartsState = ScreenState.Success(
                currentCharts.copy(currentChartType = chartType)
            )
        )
        println("DEBUG: Chart type updated to: $chartType")
    }

    // Update filters for charts - ADD EXTENSIVE DEBUGGING
    fun updateChartFilter(filter: ChartFilter) {
        println("🚀 DEBUG: updateChartFilter CALLED with: $filter")

        val currentCharts = when (val state = _uiState.value.chartsState) {
            is ScreenState.Success -> {
                println("✅ DEBUG: Charts state is SUCCESS")
                println("📊 DEBUG: Current chart type: ${state.data.currentChartType}")
                println("🎛️ DEBUG: Current monthly filter: ${state.data.monthlyChart.selectedFilter}")
                state.data
            }
            else -> {
                println("❌ DEBUG: Charts state is NOT SUCCESS: ${_uiState.value.chartsState}")
                return
            }
        }

        when (currentCharts.currentChartType) {
            ChartType.MONTHLY_TRENDS -> {
                println("📈 DEBUG: Updating MONTHLY_TRENDS chart filter from ${currentCharts.monthlyChart.selectedFilter} to $filter")

                _uiState.value = _uiState.value.copy(
                    chartsState = ScreenState.Success(
                        currentCharts.copy(
                            monthlyChart = currentCharts.monthlyChart.copy(selectedFilter = filter)
                        )
                    )
                )

                // Verify the update
                when (val newState = _uiState.value.chartsState) {
                    is ScreenState.Success -> {
                        println("✅ DEBUG: SUCCESS - Monthly chart filter updated to: ${newState.data.monthlyChart.selectedFilter}")
                    }
                    else -> {
                        println("❌ DEBUG: FAILED - Charts state after update: $newState")
                    }
                }
            }
            ChartType.CATEGORY_BREAKDOWN -> {
                println("📊 DEBUG: Updating CATEGORY_BREAKDOWN chart filter")
                _uiState.value = _uiState.value.copy(
                    chartsState = ScreenState.Success(
                        currentCharts.copy(
                            categorySummary = currentCharts.categorySummary.copy(selectedFilter = filter)
                        )
                    )
                )
                println("✅ DEBUG: Category summary filter updated to: $filter")
            }
            ChartType.MONTHLY_COMPARISON -> {
                println("📅 DEBUG: MONTHLY_COMPARISON filter change (no action needed)")
            }
        }
    }

    // Update period filter and reload data for charts
    fun updatePeriodFilter(period: PeriodFilter) {
        println("DEBUG: updatePeriodFilter called with: $period")
        viewModelScope.launch {
            try {
                val currentCharts = when (val state = _uiState.value.chartsState) {
                    is ScreenState.Success -> {
                        println("DEBUG: Current charts available for period update")
                        state.data
                    }
                    else -> {
                        println("DEBUG: No charts available for period update")
                        return@launch
                    }
                }

                when (currentCharts.currentChartType) {
                    ChartType.MONTHLY_TRENDS -> {
                        val months = when (period) {
                            PeriodFilter.YEAR -> 12
                            PeriodFilter.MONTH -> 1
                            else -> 12 // Default for other periods in bar chart
                        }
                        val result = getMonthlyChartDataUseCase.execute(months = months)
                        _uiState.value = _uiState.value.copy(
                            chartsState = ScreenState.Success(
                                currentCharts.copy(
                                    monthlyChart = result.copy(
                                        selectedPeriod = period,
                                        selectedFilter = currentCharts.monthlyChart.selectedFilter // Preserve current filter
                                    )
                                )
                            )
                        )
                        println("DEBUG: Monthly chart period updated to: $period")
                    }
                    ChartType.CATEGORY_BREAKDOWN -> {
                        val dateRange = when (period) {
                            PeriodFilter.DAYS_7 -> DateRange(getDateDaysAgo(7), getDefaultEndDate())
                            PeriodFilter.DAYS_15 -> DateRange(getDateDaysAgo(15), getDefaultEndDate())
                            PeriodFilter.DAYS_30 -> DateRange(getDateDaysAgo(30), getDefaultEndDate())
                            PeriodFilter.DAYS_90 -> DateRange(getDateDaysAgo(90), getDefaultEndDate())
                            else -> currentCharts.monthlyChart.dateRange
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
        println("DEBUG: updateDateRange called with: $dateRange")
        viewModelScope.launch {
            try {
                val currentCharts = when (val state = _uiState.value.chartsState) {
                    is ScreenState.Success -> state.data
                    else -> return@launch
                }

                val lineChartData = getMonthlyChartDataUseCase.executeForLineChart(dateRange)
                _uiState.value = _uiState.value.copy(
                    chartsState = ScreenState.Success(
                        currentCharts.copy(
                            monthlyChart = currentCharts.monthlyChart.copy(
                                days = lineChartData.days,
                                dateRange = dateRange,
                                selectedPeriod = getPeriodFilterForDateRange(dateRange)
                            )
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
                val currentCharts = when (val state = _uiState.value.chartsState) {
                    is ScreenState.Success -> state.data
                    else -> return@launch
                }

                val currentDateRange = currentCharts.monthlyChart.dateRange
                val lineChartData = getMonthlyChartDataUseCase.executeForLineChart(currentDateRange)
                _uiState.value = _uiState.value.copy(
                    chartsState = ScreenState.Success(
                        currentCharts.copy(
                            monthlyChart = currentCharts.monthlyChart.copy(
                                days = lineChartData.days
                            )
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
                val currentCharts = when (val state = _uiState.value.chartsState) {
                    is ScreenState.Success -> state.data
                    else -> return@launch
                }

                val categorySummary = getCategorySummaryUseCase.execute(startDate, endDate)
                _uiState.value = _uiState.value.copy(
                    chartsState = ScreenState.Success(
                        currentCharts.copy(
                            categorySummary = categorySummary
                        )
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
                val currentCharts = when (val state = _uiState.value.chartsState) {
                    is ScreenState.Success -> state.data
                    else -> return@launch
                }

                val monthlyComparison = getMonthlyComparisonUseCase.execute(month)
                _uiState.value = _uiState.value.copy(
                    chartsState = ScreenState.Success(
                        currentCharts.copy(
                            monthlyComparison = monthlyComparison
                        )
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
            val currentCharts = when (val state = _uiState.value.chartsState) {
                is ScreenState.Success -> state.data
                else -> return@launch
            }

            when (chartType) {
                ChartType.MONTHLY_TRENDS -> {
                    try {
                        val monthlyChart = getMonthlyChartDataUseCase.execute(months = 12)
                        _uiState.value = _uiState.value.copy(
                            chartsState = ScreenState.Success(
                                currentCharts.copy(
                                    monthlyChart = monthlyChart.copy(
                                        dateRange = currentCharts.monthlyChart.dateRange,
                                        days = currentCharts.monthlyChart.days,
                                        selectedFilter = currentCharts.monthlyChart.selectedFilter // Preserve filter
                                    )
                                )
                            )
                        )
                    } catch (e: Exception) {
                        println("DEBUG: Monthly chart refresh error: ${e.message}")
                    }
                }
                ChartType.CATEGORY_BREAKDOWN -> {
                    try {
                        refreshLineChartData()
                    } catch (e: Exception) {
                        println("DEBUG: Line chart refresh error: ${e.message}")
                    }
                }
                ChartType.MONTHLY_COMPARISON -> {
                    try {
                        val monthlyComparison = getMonthlyComparisonUseCase.execute()
                        _uiState.value = _uiState.value.copy(
                            chartsState = ScreenState.Success(
                                currentCharts.copy(
                                    monthlyComparison = monthlyComparison
                                )
                            )
                        )
                    } catch (e: Exception) {
                        println("DEBUG: Monthly comparison refresh error: ${e.message}")
                    }
                }
            }
        }
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
    val chartsState: ScreenState<TransactionChartsData> = ScreenState.Loading,
    val transactionsState: ScreenState<List<TransactionEntity>> = ScreenState.Loading
) {
    // Helper properties for backward compatibility
    val isLoading: Boolean
        get() = chartsState is ScreenState.Loading && transactionsState is ScreenState.Loading

    val chartsData: TransactionChartsData
        get() = when (chartsState) {
            is ScreenState.Success -> {
                println("🔄 DEBUG: chartsData getter - SUCCESS state, filter: ${chartsState.data.monthlyChart.selectedFilter}")
                chartsState.data
            }
            else -> {
                println("🔄 DEBUG: chartsData getter - FALLBACK state")
                TransactionChartsData(
                    monthlyChart = MonthlyChartData(
                        months = emptyList(),
                        days = emptyList(),
                        dateRange = DateRange(getDefaultStartDate(), getDefaultEndDate()),
                        selectedPeriod = PeriodFilter.DAYS_30,
                        selectedFilter = ChartFilter.EXPENSES
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
            }
        }

    val recentTransactions: List<TransactionEntity>
        get() = when (transactionsState) {
            is ScreenState.Success -> transactionsState.data
            else -> emptyList()
        }
}

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