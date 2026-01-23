// data/network/transaction/model/ChartResponses.kt
package com.example.data.network.transaction.model

import com.example.domain.transaction.model.AverageSpendingData
import com.example.domain.transaction.model.TopCategoryData
import kotlinx.serialization.Serializable

// KEEP EXISTING MODELS
@Serializable
data class SpendingTrendsResponse(
    val monthly_summary: List<MonthlySummary>,
    val summary: Summary,
    val analysis_period: AnalysisPeriod
)

@Serializable
data class MonthlySummary(
    val year: Int,
    val month: Int,
    val total_spent: Double,
    val total_income: Double,
    val month_name: String,
    val display_name: String
)

@Serializable
data class Summary(
    val total_spent: Double,
    val total_income: Double,
    val net_flow: Double,
    val average_monthly_spent: Double,
    val months_analyzed: Int
)

@Serializable
data class AnalysisPeriod(
    val start_date: String,
    val end_date: String,
    val months_analyzed: Int
)

// ADD NEW RESPONSE MODELS
@Serializable
data class CategorySummaryResponse(
    val expenses: List<CategoryResponse>,
    val incomes: List<CategoryResponse>,
    val total_expenses: Double,
    val total_incomes: Double,
    val net_flow: Double
)

@Serializable
data class CategoryResponse(
    val category_id: Int,
    val category_name: String,
    val category_type: String,
    val total_amount: Double,
    val transaction_count: Int
)

@Serializable
data class MonthlyComparisonResponse(
    val category_id: Int,
    val category_name: String,
    val current_month_amount: Double,
    val previous_month_amount: Double,
    val difference: Double,
    val percentage_change: Double
)

@Serializable
data class TopCategoryResponse(
    val category_id: Int,
    val category_name: String,
    val total_amount: Double
)

@Serializable
data class AverageSpendingResponse(
    val category_id: Int,
    val category_name: String,
    val total_period_spent: Double,
    val transactions: Int,
    val period_type: String
)

// SAVINGS TRENDS RESPONSE MODELS
@Serializable
data class SavingsTrendsResponse(
    val monthly_trends: List<SavingsMonthlyTrendDto>,
    val months_analyzed: Int,
    val analysis_period: AnalysisPeriodDto
)

@Serializable
data class SavingsMonthlyTrendDto(
    val year: Int,
    val month: Int,
    val display_name: String,
    val saved_amount: Double,
    val target_amount: Double,
    val achievement_rate: Double
)

@Serializable
data class AnalysisPeriodDto(
    val start_date: String,
    val end_date: String
)