package com.example.moneymate.ui.screens.transaction.chart

import co.yml.charts.common.model.Point
import com.example.domain.transaction.model.DailyData

object YChartLineDataConverter {

    fun convertToIncomePoints(days: List<DailyData>): List<Point> {
        return days.mapIndexed { index, dayData ->
            Point(
                x = index.toFloat(),
                y = dayData.income.toFloat()
            )
        }
    }

    fun convertToExpensePoints(days: List<DailyData>): List<Point> {
        return days.mapIndexed { index, dayData ->
            Point(
                x = index.toFloat(),
                y = dayData.expenses.toFloat()
            )
        }
    }

    fun getDayLabels(days: List<DailyData>): List<String> {
        return days.map { it.dayLabel }
    }
}