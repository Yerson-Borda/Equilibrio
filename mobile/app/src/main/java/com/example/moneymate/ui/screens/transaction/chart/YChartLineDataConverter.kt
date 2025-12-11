// ui/screens/transaction/chart/YChartLineDataConverter.kt - UPDATED
package com.example.moneymate.ui.screens.transaction.chart

import androidx.compose.ui.graphics.Color
import co.yml.charts.common.model.Point
import com.example.domain.transaction.model.DailyData

object YChartLineDataConverter {

    // Convert your DailyData to YCharts Points for Income line
    fun convertToIncomePoints(days: List<DailyData>): List<Point> {
        return days.mapIndexed { index, dayData ->
            Point(
                x = index.toFloat(), // X position index
                y = dayData.income.toFloat() // CORRECT: This should be income
            )
        }
    }

    // Convert your DailyData to YCharts Points for Expense line
    fun convertToExpensePoints(days: List<DailyData>): List<Point> {
        return days.mapIndexed { index, dayData ->
            Point(
                x = index.toFloat(), // X position index
                y = dayData.expenses.toFloat() // CORRECT: This should be expenses
            )
        }
    }

    // Get day labels for X-axis
    fun getDayLabels(days: List<DailyData>): List<String> {
        return days.map { it.dayLabel }
    }

    // Get the maximum Y value for scaling
    fun getMaxYValue(days: List<DailyData>): Float {
        val maxIncome = days.maxOfOrNull { it.income } ?: 0.0
        val maxExpense = days.maxOfOrNull { it.expenses } ?: 0.0
        return maxOf(maxIncome, maxExpense).toFloat()
    }
}