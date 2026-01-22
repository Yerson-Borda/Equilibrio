package com.example.domain.transaction.model
data class SpendingTrendData(
    val year: Int,
    val month: Int,
    val totalSpent: Double,
    val totalIncome: Double,
    val monthName: String,
    val displayName: String
)
data class TopCategoryData(
    val categoryId: Int,
    val categoryName: String,
    val totalAmount: Double
)

data class AverageSpendingData(
    val categoryId: Int,
    val categoryName: String,
    val totalPeriodSpent: Double,
    val transactions: Int,
    val periodType: String
)
data class DailyData(
    val date: String,
    val dayLabel: String,
    val income: Double,
    val expenses: Double
)

data class DateRange(
    val startDate: String,
    val endDate: String
)

data class MonthlyChartData(
    val months: List<MonthlyData>,
    val days: List<DailyData>,
    val selectedFilter: ChartFilter = ChartFilter.EXPENSES,
    val selectedPeriod: PeriodFilter = PeriodFilter.YEAR,
    val dateRange: DateRange = DateRange(getDefaultStartDate(), getDefaultEndDate()) // ADD THIS
)

private fun getDefaultStartDate(): String {
    // 30 days ago as default
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

data class MonthlyData(
    val month: String, // "Jan", "Feb", etc.
    val income: Double,
    val expenses: Double,
    val monthNumber: Int // 1-12 for sorting
)

enum class ChartFilter {
    INCOME, EXPENSES
}

enum class PeriodFilter {
    YEAR, MONTH, DAYS_7, DAYS_15, DAYS_30, DAYS_90, DAY
}


data class CategorySummaryData(
    val expenses: List<CategoryData>,
    val incomes: List<CategoryData>,
    val totalExpenses: Double,
    val totalIncomes: Double,
    val netFlow: Double,
    val selectedFilter: ChartFilter = ChartFilter.EXPENSES
)

data class CategoryData(
    val categoryId: Int,
    val categoryName: String,
    val categoryType: String,
    val totalAmount: Double,
    val transactionCount: Int
)

data class MonthlyComparisonData(
    val categories: List<ComparisonCategoryData>,
    val selectedMonth: String // Format: "March 2024"
)

data class ComparisonCategoryData(
    val categoryId: Int,
    val categoryName: String,
    val currentMonthAmount: Double,
    val previousMonthAmount: Double,
    val difference: Double,
    val percentageChange: Double
)

// Main container for all charts
data class TransactionChartsData(
    val monthlyChart: MonthlyChartData,
    val categorySummary: CategorySummaryData,
    val monthlyComparison: MonthlyComparisonData,
    val topCategories: List<TopCategoryData> = emptyList(),
    val averageSpending: List<AverageSpendingData> = emptyList(),
    val currentChartType: ChartType = ChartType.MONTHLY_TRENDS,
    val currentPeriod: PeriodFilter = PeriodFilter.MONTH
)

// In domain/transaction/model/ChartModels.kt
enum class ChartType {
    MONTHLY_TRENDS,       // Bar chart (first)
    CATEGORY_BREAKDOWN,   // Line chart (second) - renamed to be more general
    MONTHLY_COMPARISON,    // Pie chart (third)
    TOP_CATEGORIES,
    AVERAGE_SPENDING_RADAR,  // Radar chart version
    AVERAGE_SPENDING_LIST    // List-based version (last)
}