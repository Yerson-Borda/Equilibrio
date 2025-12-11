// ui/screens/transaction/component/YChartMonthlyBarChartComponent.kt
package com.example.moneymate.ui.screens.transaction.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.yml.charts.axis.AxisData
import co.yml.charts.ui.barchart.BarChart
import co.yml.charts.ui.barchart.models.BarChartData
import co.yml.charts.ui.barchart.models.BarData
import co.yml.charts.ui.barchart.models.BarStyle
import co.yml.charts.ui.barchart.models.SelectionHighlightData
import com.example.domain.transaction.model.ChartFilter
import com.example.domain.transaction.model.MonthlyChartData
import com.example.domain.transaction.model.MonthlyData
import com.example.domain.transaction.model.PeriodFilter
import com.example.moneymate.ui.screens.transaction.chart.YChartDataConverter
import kotlin.math.ceil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YChartMonthlyBarChartComponent(
    monthlyChartData: MonthlyChartData,
    onFilterChanged: (ChartFilter) -> Unit,
    onPeriodChanged: (PeriodFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    val months = monthlyChartData.months
    val selectedFilter = monthlyChartData.selectedFilter

    // Calculate totals for summary
    val totalIncome = remember(months) { months.sumOf { it.income } }
    val totalExpenses = remember(months) { months.sumOf { it.expenses } }
    val selectedTotal = when (selectedFilter) {
        ChartFilter.INCOME -> totalIncome
        ChartFilter.EXPENSES -> totalExpenses
    }
    val netAmount = totalIncome - totalExpenses

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Header with title and filters (unchanged)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Monthly Trends",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            FilterDropdown(
                items = PeriodFilter.entries.map { it.name },
                selectedItem = monthlyChartData.selectedPeriod.name,
                onItemSelected = { period ->
                    onPeriodChanged(PeriodFilter.valueOf(period))
                },
                modifier = Modifier.width(120.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Income/Expenses Toggle Switch (unchanged)
        IncomeExpenseToggle(
            selectedFilter = selectedFilter,
            onFilterChanged = onFilterChanged,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        // YCharts Bar Chart
        if (months.isNotEmpty()) {
            BarChartWithYCharts(
                months = months,
                selectedFilter = selectedFilter,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No data available",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Summary section (unchanged)
        Column {
            Text(
                text = "Total ${selectedFilter.name.lowercase().replaceFirstChar { it.uppercase() }}: $${String.format("%,.0f", selectedTotal)}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )

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
                    text = "$${String.format("%,.0f", netAmount)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (netAmount >= 0) Color(0xFF4ECDC4) else Color(0xFFFF6B6B)
                )
            }
        }
    }
}

@Composable
fun BarChartWithYCharts(
    months: List<MonthlyData>,
    selectedFilter: ChartFilter,
    modifier: Modifier = Modifier
) {
    // Convert your data to YCharts BarData format
    val barData = YChartDataConverter.convertToBarData(months, selectedFilter)

    // Calculate max value for Y-axis scaling
    val maxValue = barData.maxOfOrNull { it.point.y } ?: 100000f

    // Configure Y-axis steps (rounded up to nearest nice number)
    val yMax = calculateNiceMaxValue(maxValue)
    val yStepSize = calculateStepCount(yMax)

    // Build X-axis data
    val xAxisData = AxisData.Builder()
        .axisStepSize(30.dp)
        .steps(barData.size - 1)
        .bottomPadding(40.dp)
        .axisLabelAngle(if (barData.size > 6) 45f else 0f) // Angle labels if many bars
        .startDrawPadding(48.dp)
        .labelData { index -> barData[index].label }
        .build()

    // Build Y-axis data
    val yAxisData = AxisData.Builder()
        .steps(yStepSize)
        .labelAndAxisLinePadding(20.dp)
        .topPadding(40.dp)
        .axisOffset(20.dp)
        .labelData { index ->
            val value = (index * (yMax / yStepSize)).toInt()
            formatAmountForYAxis(value)
        }
        .build()

    // Configure bar chart data
    val barChartData = BarChartData(
        chartData = barData,
        xAxisData = xAxisData,
        yAxisData = yAxisData,
        barStyle = BarStyle(
            paddingBetweenBars = 12.dp,
            barWidth = if (months.size <= 3) 40.dp else 25.dp,
            selectionHighlightData = SelectionHighlightData(
                highlightBarColor = when (selectedFilter) {
                    ChartFilter.INCOME -> Color(0xFF26A69A)
                    ChartFilter.EXPENSES -> Color(0xFFE53935)
                },
                highlightTextBackgroundColor = Color.White,
                popUpLabel = { x, y ->
                    val value = y.toDouble()
                    if (value >= 1000) {
                        "$${(value / 1000).toInt()}K"
                    } else {
                        "$${value.toInt()}"
                    }
                }
            )
        ),
        showYAxis = true,
        showXAxis = true,
        horizontalExtraSpace = 10.dp
    )

    // Render the chart
    BarChart(
        modifier = modifier,
        barChartData = barChartData
    )
}

private fun calculateNiceMaxValue(maxValue: Float): Float {
    if (maxValue <= 0) return 100f

    // Round up to nearest 100, 1000, 10000 etc based on magnitude
    return when {
        maxValue < 100 -> ceil(maxValue / 10f) * 10f
        maxValue < 1000 -> ceil(maxValue / 100f) * 100f
        maxValue < 10000 -> ceil(maxValue / 1000f) * 1000f
        maxValue < 100000 -> ceil(maxValue / 10000f) * 10000f
        else -> ceil(maxValue / 100000f) * 100000f
    }
}

// Helper function to calculate step count
private fun calculateStepCount(maxValue: Float): Int {
    return when {
        maxValue <= 1000 -> 5
        maxValue <= 5000 -> 5
        maxValue <= 10000 -> 5
        maxValue <= 50000 -> 5
        else -> 5
    }
}

private fun formatAmountForYAxis(amount: Int): String {
    return if (amount >= 1000) {
        "$${amount / 1000}K"
    } else {
        "$$amount"
    }
}