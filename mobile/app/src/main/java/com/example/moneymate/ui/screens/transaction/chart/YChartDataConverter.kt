// ui/screens/transaction/chart/YChartDataConverter.kt
package com.example.moneymate.ui.screens.transaction.chart

import androidx.compose.ui.graphics.Color
import co.yml.charts.common.model.Point
import co.yml.charts.ui.barchart.models.BarData
import com.example.domain.transaction.model.ChartFilter
import com.example.domain.transaction.model.MonthlyData

object YChartDataConverter {

    fun convertToBarData(
        months: List<MonthlyData>,
        selectedFilter: ChartFilter,
        incomeColor: Color = Color(0xFF4ECDC4), // Green
        expenseColor: Color = Color(0xFFFF6B6B) // Red
    ): List<BarData> {
        return months.mapIndexed { index, monthlyData ->
            BarData(
                point = Point(
                    x = index.toFloat(), // X position index
                    y = when (selectedFilter) {
                        ChartFilter.INCOME -> monthlyData.income.toFloat()
                        ChartFilter.EXPENSES -> monthlyData.expenses.toFloat()
                    }
                ),
                label = monthlyData.month,
                color = when (selectedFilter) {
                    ChartFilter.INCOME -> incomeColor
                    ChartFilter.EXPENSES -> expenseColor
                }
            )
        }
    }
}