// data/network/transaction/model/ChartResponses.kt
package com.example.data.network.transaction.model

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