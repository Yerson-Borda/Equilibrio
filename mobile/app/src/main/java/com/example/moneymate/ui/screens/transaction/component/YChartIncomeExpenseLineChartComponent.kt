package com.example.moneymate.ui.screens.transaction.component

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.yml.charts.axis.AxisData
import co.yml.charts.common.model.Point
import co.yml.charts.ui.linechart.LineChart
import co.yml.charts.ui.linechart.model.*
import com.example.domain.transaction.model.DateRange
import com.example.domain.transaction.model.MonthlyChartData
import com.example.moneymate.ui.screens.transaction.chart.YChartLineDataConverter
import com.example.moneymate.utils.ChartUtils
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YChartIncomeExpenseLineChartComponent(
    monthlyChartData: MonthlyChartData,
    onDateRangeChanged: (DateRange) -> Unit,
    modifier: Modifier = Modifier
) {
    val days = monthlyChartData.days
    val dateRange = monthlyChartData.dateRange

    // Calculate totals for summary - filter by actual selected date range
    val filteredDays = remember(days, dateRange) {
        days.filter { dailyData ->
            try {
                val dayDate = LocalDate.parse(dailyData.date)
                val startDate = LocalDate.parse(dateRange.startDate)
                val endDate = LocalDate.parse(dateRange.endDate)
                !dayDate.isBefore(startDate) && !dayDate.isAfter(endDate)
            } catch (e: Exception) {
                true // If parsing fails, include all days
            }
        }
    }

    // Group days intelligently for the chart display
    val chartDays = remember(filteredDays) {
        ChartUtils.smartGroupDaysForChart(filteredDays)
    }

    val totalIncome = filteredDays.sumOf { it.income }
    val totalExpenses = filteredDays.sumOf { it.expenses }
    val netAmount = totalIncome - totalExpenses

    // Show grouping info to user
    val showGroupingInfo = filteredDays.size > chartDays.size
    val daysCountText = if (showGroupingInfo) {
        "Showing ${chartDays.size} of ${filteredDays.size} days (grouped for better visibility)"
    } else {
        "Showing ${filteredDays.size} days"
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp) // CHANGED: Only horizontal padding
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
            // From Date Picker - show full date
            Column {
                Text(
                    text = "From",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                SimpleMonthPickerButton(
                    selectedDateString = dateRange.startDate,
                    onDateSelected = { newStartDateString ->
                        // Don't adjust the date - use exactly what user selected
                        onDateRangeChanged(dateRange.copy(startDate = newStartDateString))
                    },
                    modifier = Modifier.width(140.dp),
                    displayFormat = "dd MMM yyyy"
                )
            }

            // To Date Picker - show full date
            Column {
                Text(
                    text = "To",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                SimpleMonthPickerButton(
                    selectedDateString = dateRange.endDate,
                    onDateSelected = { newEndDateString ->
                        // Don't adjust the date - use exactly what user selected
                        onDateRangeChanged(dateRange.copy(endDate = newEndDateString))
                    },
                    modifier = Modifier.width(140.dp),
                    displayFormat = "dd MMM yyyy"
                )
            }
        }

        // Validation message if dates are invalid
        val isValidRange = remember(dateRange) {
            try {
                val start = LocalDate.parse(dateRange.startDate)
                val end = LocalDate.parse(dateRange.endDate)
                !start.isAfter(end)
            } catch (e: Exception) {
                false
            }
        }

        if (!isValidRange) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "⚠️ Start date must be before end date",
                fontSize = 12.sp,
                color = Color.Red,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        } else if (showGroupingInfo) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "📊 $daysCountText",
                fontSize = 11.sp,
                color = Color(0xFF666666),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Use grouped days for the chart
        if (filteredDays.isNotEmpty() && filteredDays.any { it.income > 0 || it.expenses > 0 }) {
            DualLineChartWithYCharts(
                days = chartDays,
                originalDaysCount = filteredDays.size,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp) // CHANGED: Slightly taller for better spacing
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp) // CHANGED: Match height with chart
                    .background(Color(0xFFF5F5F5)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No data available for selected date range",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "${dateRange.startDate} to ${dateRange.endDate}",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
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
    originalDaysCount: Int = days.size,
    modifier: Modifier = Modifier
) {
    val displayDays = days.takeIf { it.isNotEmpty() } ?: emptyList()

    // Points are already at correct positions (0, 1, 2, ...) from converter
    val incomePoints = YChartLineDataConverter.convertToIncomePoints(displayDays)
    val expensePoints = YChartLineDataConverter.convertToExpensePoints(displayDays)
    val dayLabels = YChartLineDataConverter.getDayLabels(displayDays)

    val isGrouped = originalDaysCount > days.size

    // Adjust spacing
    val axisStepSize = when {
        displayDays.size <= 7 -> 40.dp
        displayDays.size <= 15 -> 30.dp
        displayDays.size <= 30 -> 20.dp
        else -> 15.dp
    }

    val steps = maxOf(0, displayDays.size - 1)

    // Use minimal but balanced padding
    val xAxisData = AxisData.Builder()
        .axisStepSize(axisStepSize)
        .steps(steps)
        .bottomPadding(45.dp)
        .axisLabelAngle(if (displayDays.size > 10) 45f else 0f)
        .startDrawPadding(4.dp) // CHANGED: Minimal padding
        .labelData { index ->
            if (index < dayLabels.size) dayLabels[index] else ""
        }
        .build()

    val maxValue = remember(displayDays) {
        if (displayDays.isEmpty()) 100.0 else {
            val maxIncome = displayDays.maxOfOrNull { it.income } ?: 0.0
            val maxExpense = displayDays.maxOfOrNull { it.expenses } ?: 0.0
            maxOf(maxIncome, maxExpense) * 1.15
        }
    }

    val yAxisSteps = 5
    val yStepValue = if (maxValue > 0) {
        val step = (maxValue / yAxisSteps).toInt()
        if (step == 0) 1 else step
    } else 1

    val yAxisData = AxisData.Builder()
        .steps(yAxisSteps)
        .labelAndAxisLinePadding(25.dp)
        .axisOffset(25.dp)
        .topPadding(60.dp)
        .labelData { index ->
            val value = index * yStepValue
            "$$value"
        }
        .build()

    val incomeLine = Line(
        dataPoints = incomePoints,
        lineStyle = LineStyle(
            color = Color(0xFF4ECDC4),
            lineType = LineType.SmoothCurve(isDotted = false)
        ),
        intersectionPoint = IntersectionPoint(
            color = Color(0xFF4ECDC4)
        ),
        selectionHighlightPoint = SelectionHighlightPoint(
            color = Color(0xFF4ECDC4),
            radius = 8.dp
        ),
        selectionHighlightPopUp = SelectionHighlightPopUp(
            popUpLabel = { xIndex, yValue ->
                val index = kotlin.math.round(xIndex).toInt()
                val actualIncome = if (index < displayDays.size) displayDays[index].income else yValue.toDouble()
                val dateLabel = if (index < dayLabels.size) dayLabels[index] else "Day $index"
                val groupedInfo = if (isGrouped && actualIncome > 0) "\n(Average per period)" else ""
                if (actualIncome > 0) "$${String.format("%.1f", actualIncome)}\nIncome ($dateLabel)$groupedInfo" else ""
            }
        )
    )

    val expenseLine = Line(
        dataPoints = expensePoints,
        lineStyle = LineStyle(
            color = Color(0xFFFF6B6B),
            lineType = LineType.SmoothCurve(isDotted = false)
        ),
        intersectionPoint = IntersectionPoint(
            color = Color(0xFFFF6B6B)
        ),
        selectionHighlightPoint = SelectionHighlightPoint(
            color = Color(0xFFFF6B6B),
            radius = 8.dp
        ),
        selectionHighlightPopUp = SelectionHighlightPopUp(
            popUpLabel = { xIndex, yValue ->
                val index = kotlin.math.round(xIndex).toInt()
                val actualExpense = if (index < displayDays.size) displayDays[index].expenses else yValue.toDouble()
                val dateLabel = if (index < dayLabels.size) dayLabels[index] else "Day $index"
                val groupedInfo = if (isGrouped && actualExpense > 0) "\n(Average per period)" else ""
                if (actualExpense > 0) "$${String.format("%.1f", actualExpense)}\nExpense ($dateLabel)$groupedInfo" else ""
            }
        )
    )
    val linePlotData = LinePlotData(
        lines = listOf(incomeLine, expenseLine)
    )

    val lineChartData = LineChartData(
        linePlotData = linePlotData,
        xAxisData = xAxisData,
        yAxisData = yAxisData,
        backgroundColor = Color.Transparent
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(320.dp)
    ) {
        LineChart(
            modifier = Modifier.fillMaxSize(),
            lineChartData = lineChartData
        )
    }
}