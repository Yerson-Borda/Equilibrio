package com.example.moneymate.ui.screens.transaction.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.yml.charts.axis.AxisData
import co.yml.charts.ui.linechart.LineChart
import co.yml.charts.ui.linechart.model.*
import com.example.domain.transaction.model.DateRange
import com.example.domain.transaction.model.MonthlyChartData
import com.example.moneymate.ui.screens.transaction.chart.YChartLineDataConverter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YChartIncomeExpenseLineChartComponent(
    monthlyChartData: MonthlyChartData,
    onDateRangeChanged: (DateRange) -> Unit,
    modifier: Modifier = Modifier
) {
    val days = monthlyChartData.days
    val dateRange = monthlyChartData.dateRange

    // Calculate totals for summary
    val totalIncome = days.sumOf { it.income }
    val totalExpenses = days.sumOf { it.expenses }
    val netAmount = totalIncome - totalExpenses

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Header with TWO date pickers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Select Date Range",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // From Date Picker
            Column {
                Text(
                    text = "From",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                DatePickerDropdown(
                    selectedDate = dateRange.startDate,
                    onDateSelected = { newStartDate ->
                        onDateRangeChanged(dateRange.copy(startDate = newStartDate))
                    },
                    modifier = Modifier.width(120.dp)
                )
            }
            Column {
                Text(
                    text = "To",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                DatePickerDropdown(
                    selectedDate = dateRange.endDate,
                    onDateSelected = { newEndDate ->
                        onDateRangeChanged(dateRange.copy(endDate = newEndDate))
                    },
                    modifier = Modifier.width(120.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (days.isNotEmpty() && days.any { it.income > 0 || it.expenses > 0 }) {
            DualLineChartWithYCharts(
                days = days,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(Color(0xFFF5F5F5)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No data available for selected date range",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Income legend
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(Color(0xFF4ECDC4))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Income",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.width(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(Color(0xFFFF6B6B))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Expense",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total Income:",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = "$${String.format("%.2f", totalIncome)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF4ECDC4)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total Expense:",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = "$${String.format("%.2f", totalExpenses)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFFF6B6B)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Net Amount:",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = "$${String.format("%.2f", netAmount)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (netAmount >= 0) Color(0xFF4ECDC4) else Color(0xFFFF6B6B)
                )
            }
        }
    }
}

@Composable
fun DualLineChartWithYCharts(
    days: List<com.example.domain.transaction.model.DailyData>,
    modifier: Modifier = Modifier
) {
    val incomePoints = YChartLineDataConverter.convertToIncomePoints(days)
    val expensePoints = YChartLineDataConverter.convertToExpensePoints(days)
    val dayLabels = YChartLineDataConverter.getDayLabels(days)

    val xAxisData = AxisData.Builder()
        .axisStepSize(30.dp)
        .steps(days.size - 1)
        .bottomPadding(50.dp)
        .axisLabelAngle(if (days.size > 10) 45f else 0f)
        .startDrawPadding(60.dp)  // Increased left padding
        .labelData { index ->
            if (index < dayLabels.size) dayLabels[index] else "Day $index"
        }
        .build()

    val yAxisData = AxisData.Builder()
        .steps(5)
        .labelAndAxisLinePadding(25.dp)
        .axisOffset(25.dp)
        .topPadding(60.dp)
        .labelData { index ->
            val value = index * 100
            "$$value"
        }
        .build()

    val incomeLine = Line(
        dataPoints = incomePoints,
        lineStyle = LineStyle(
            color = Color(0xFF4ECDC4)
        ),
        selectionHighlightPopUp = SelectionHighlightPopUp(
            popUpLabel = { xIndex, yValue ->
                val index = kotlin.math.round(xIndex).toInt()
                val actualIncome = if (index < days.size) days[index].income else yValue.toDouble()

                if (actualIncome > 0) "$${actualIncome.toInt()} Income" else ""
            }
        )
    )

    val expenseLine = Line(
        dataPoints = expensePoints,
        lineStyle = LineStyle(
            color = Color(0xFFFF6B6B)
        ),
        selectionHighlightPopUp = SelectionHighlightPopUp(
            popUpLabel = { xIndex, yValue ->
                val index = kotlin.math.round(xIndex).toInt()
                val actualExpense = if (index < days.size) days[index].expenses else yValue.toDouble()
                if (actualExpense > 0) "$${actualExpense.toInt()} Expense" else ""
            }
        )
    )

    val linePlotData = LinePlotData(
        lines = listOf(incomeLine, expenseLine)
    )

    val lineChartData = LineChartData(
        linePlotData = linePlotData,
        xAxisData = xAxisData,
        yAxisData = yAxisData
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
            .padding(horizontal = 8.dp)
    ) {
        LineChart(
            modifier = Modifier.fillMaxSize(),
            lineChartData = lineChartData
        )
    }
}