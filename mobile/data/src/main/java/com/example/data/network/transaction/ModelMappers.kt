// data/network/transaction/model/ModelMappers.kt
package com.example.data.network.transaction

import com.example.data.network.transaction.model.AverageSpendingResponse
import com.example.data.network.transaction.model.CategoryResponse
import com.example.data.network.transaction.model.CategorySummaryResponse
import com.example.data.network.transaction.model.MonthlyComparisonResponse
import com.example.data.network.transaction.model.MonthlySummary
import com.example.data.network.transaction.model.SavingsTrendsResponse
import com.example.data.network.transaction.model.TopCategoryResponse
import com.example.domain.transaction.model.*

fun SavingsTrendsResponse.toDomain(): SavingsTrendsData {
    return SavingsTrendsData(
        monthlyTrends = monthly_trends.map { dto ->
            SavingsMonthlyData(
                year = dto.year,
                month = dto.month,
                displayName = dto.display_name,
                savedAmount = dto.saved_amount,
                targetAmount = dto.target_amount,
                achievementRate = dto.achievement_rate
            )
        },
        monthsAnalyzed = months_analyzed,
        analysisPeriodStart = analysis_period.start_date,
        analysisPeriodEnd = analysis_period.end_date
    )
}

// Also move other extension functions here
fun MonthlySummary.toDomain(): SpendingTrendData {
    return SpendingTrendData(
        year = year,
        month = month,
        totalSpent = total_spent,
        totalIncome = total_income,
        monthName = month_name,
        displayName = display_name
    )
}

fun CategorySummaryResponse.toDomain(): CategorySummaryData {
    return CategorySummaryData(
        expenses = expenses.map { it.toDomain() },
        incomes = incomes.map { it.toDomain() },
        totalExpenses = total_expenses,
        totalIncomes = total_incomes,
        netFlow = net_flow
    )
}

fun CategoryResponse.toDomain(): CategoryData {
    return CategoryData(
        categoryId = category_id,
        categoryName = category_name,
        categoryType = category_type,
        totalAmount = total_amount,
        transactionCount = transaction_count
    )
}

fun MonthlyComparisonResponse.toDomain(): ComparisonCategoryData {
    return ComparisonCategoryData(
        categoryId = category_id,
        categoryName = category_name,
        currentMonthAmount = current_month_amount,
        previousMonthAmount = previous_month_amount,
        difference = difference,
        percentageChange = percentage_change
    )
}

fun TopCategoryResponse.toDomain(): TopCategoryData {
    return TopCategoryData(
        categoryId = category_id,
        categoryName = category_name,
        totalAmount = total_amount
    )
}

fun AverageSpendingResponse.toDomain(): AverageSpendingData {
    return AverageSpendingData(
        categoryId = category_id,
        categoryName = category_name,
        totalPeriodSpent = total_period_spent,
        transactions = transactions,
        periodType = period_type
    )
}