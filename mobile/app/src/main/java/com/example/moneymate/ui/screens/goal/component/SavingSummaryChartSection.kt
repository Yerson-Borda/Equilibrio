package com.example.moneymate.ui.screens.goal.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.yml.charts.axis.AxisData
import co.yml.charts.ui.linechart.LineChart
import co.yml.charts.ui.linechart.model.*
import com.example.domain.transaction.model.DailyData
import com.example.domain.transaction.model.MonthlyChartData
import com.example.domain.transaction.model.SavingsMonthlyData
import java.util.Locale

@Composable
fun SavingSummaryChartSection(
    monthlyChartData: MonthlyChartData?,
    selectedMonth: String,
    availableMonths: List<String>,
    selectedPeriod: Int,
    availablePeriods: List<Int>,
    onMonthSelected: (String) -> Unit,
    onPeriodSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var monthExpanded by remember { mutableStateOf(false) }
    var periodExpanded by remember { mutableStateOf(false) }
    val savingsData = monthlyChartData?.savingsData ?: emptyList()

    // Filter data for selected month
    val selectedMonthData = remember(selectedMonth, savingsData) {
        savingsData.find { it.displayName == selectedMonth }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with Title and Period Filter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Savings Trend",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black
                )

                // Period selector
                Box {
                    Text(
                        text = when (selectedPeriod) {
                            3 -> "3M"
                            6 -> "6M"
                            12 -> "1Y"
                            else -> "${selectedPeriod}M"
                        } + " ▾",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier
                            .clickable { periodExpanded = true }
                            .padding(4.dp)
                    )
                    DropdownMenu(
                        expanded = periodExpanded,
                        onDismissRequest = { periodExpanded = false }
                    ) {
                        availablePeriods.forEach { period ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        when (period) {
                                            3 -> "3 Months"
                                            6 -> "6 Months"
                                            12 -> "1 Year"
                                            else -> "$period Months"
                                        }
                                    )
                                },
                                onClick = {
                                    onPeriodSelected(period)
                                    periodExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Month selector and metrics row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Month selector
                Box {
                    Text(
                        text = "$selectedMonth ▾",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier
                            .clickable { monthExpanded = true }
                            .padding(4.dp)
                    )
                    DropdownMenu(
                        expanded = monthExpanded,
                        onDismissRequest = { monthExpanded = false }
                    ) {
                        availableMonths.forEach { month ->
                            DropdownMenuItem(
                                text = { Text(month) },
                                onClick = {
                                    onMonthSelected(month)
                                    monthExpanded = false
                                }
                            )
                        }
                    }
                }

                // Display savings metrics for selected month
                selectedMonthData?.let { monthData ->
                    Row(
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Saved:",
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = String.format(Locale.US, "$%.2f", monthData.savedAmount),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (monthData.savedAmount >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Achievement:",
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = String.format(Locale.US, "%.1f%%", monthData.achievementRate * 100),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (monthData.achievementRate >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
                            )
                        }
                    }
                }
            }

            // Show target if available
            selectedMonthData?.let { monthData ->
                if (monthData.targetAmount > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "Target: $${String.format(Locale.US, "%.2f", monthData.targetAmount)}",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Chart
            if (savingsData.isNotEmpty()) {
                val points = convertSavingsToPoints(savingsData)

                if (points.isNotEmpty()) {
                    // Calculate Y-axis range
                    val maxSaved = savingsData.maxOfOrNull { it.savedAmount } ?: 0.0
                    val minSaved = savingsData.minOfOrNull { it.savedAmount } ?: 0.0

                    // Determine appropriate Y-axis max value
                    val yMax = when {
                        maxSaved == 0.0 -> 100.0
                        maxSaved < 1.0 -> 1.0
                        else -> maxSaved * 1.2
                    }

                    // Determine appropriate steps based on max value
                    val steps = when {
                        yMax <= 10 -> 5
                        yMax <= 100 -> 5
                        else -> 4
                    }

                    // Create X-axis labels - show only some labels to avoid overcrowding
                    val xAxisLabels = savingsData.mapIndexed { index, data ->
                        // Determine which labels to show based on selected period
                        val showLabel = when (selectedPeriod) {
                            3 -> true // Show all labels for 3 months
                            6 -> index % 2 == 0 // Show every other for 6 months
                            12 -> index % 3 == 0 // Show every third for 12 months
                            else -> index % 2 == 0 // Default: show every other
                        }

                        if (showLabel) {
                            when (selectedPeriod) {
                                3 -> data.displayName
                                6 -> {
                                    val parts = data.displayName.split(" ")
                                    if (parts.size >= 2) "${parts[0].take(3)} '${parts[1].takeLast(2)}"
                                    else data.displayName
                                }
                                12 -> {
                                    val parts = data.displayName.split(" ")
                                    if (parts.size >= 2) "${parts[0].take(3)}'${parts[1].takeLast(2)}"
                                    else parts[0].take(3)
                                }
                                else -> data.displayName
                            }
                        } else {
                            "" // Empty string for labels we don't want to show
                        }
                    }

                    val xAxisData = AxisData.Builder()
                        .axisStepSize(40.dp)
                        .steps(points.size - 1)
                        .labelData { i ->
                            if (i < xAxisLabels.size) {
                                xAxisLabels[i]
                            } else {
                                ""
                            }
                        }
                        .axisLabelColor(Color.LightGray)
                        .axisLineColor(Color.Transparent)
                        .backgroundColor(Color.White)
                        .build()

                    val yAxisData = AxisData.Builder()
                        .steps(steps)
                        .labelData { i ->
                            val value = (yMax / steps) * i
                            // Format based on value size
                            when {
                                yMax < 1 -> String.format(Locale.US, "$%.2f", value)
                                yMax < 10 -> String.format(Locale.US, "$%.1f", value)
                                else -> String.format(Locale.US, "$%.0f", value)
                            }
                        }
                        .axisLabelColor(Color.LightGray)
                        .axisLineColor(Color.Transparent)
                        .backgroundColor(Color.White)
                        .build()

                    LineChart(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        lineChartData = LineChartData(
                            linePlotData = LinePlotData(lines = listOf(
                                Line(
                                    dataPoints = points,
                                    lineStyle = LineStyle(
                                        color = Color(0xFF4D73FF),
                                        lineType = LineType.SmoothCurve(false)
                                    ),
                                    intersectionPoint = IntersectionPoint(
                                        color = Color(0xFF4D73FF)
                                    ),
                                    selectionHighlightPoint = SelectionHighlightPoint(),
                                    shadowUnderLine = ShadowUnderLine(
                                        alpha = 0.1f,
                                        color = Color(0xFF4D73FF)
                                    ),
                                    selectionHighlightPopUp = SelectionHighlightPopUp()
                                )
                            )),
                            xAxisData = xAxisData,
                            yAxisData = yAxisData,
                            gridLines = GridLines(color = Color(0xFFF0F0F0)),
                            backgroundColor = Color.White
                        )
                    )

                    // Show period info
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Showing last ${selectedPeriod} months",
                        fontSize = 10.sp,
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    // Show a message if all values are zero
                    if (maxSaved == 0.0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "No savings recorded in this period",
                            fontSize = 10.sp,
                            color = Color.Red,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                } else {
                    NoDataPlaceholder()
                }
            } else if (monthlyChartData?.days?.isNotEmpty() == true) {
                // Fallback to using daily data
                val points = convertDailySavingsToPoints(monthlyChartData.days)

                if (points.isNotEmpty()) {
                    // For daily data, show fewer labels
                    val xAxisLabels = monthlyChartData.days.mapIndexed { index, data ->
                        if (index % 5 == 0) { // Show every 5th day
                            data.dayLabel
                        } else {
                            ""
                        }
                    }

                    val xAxisData = AxisData.Builder()
                        .axisStepSize(40.dp)
                        .steps(points.size - 1)
                        .labelData { i ->
                            if (i < xAxisLabels.size) {
                                xAxisLabels[i]
                            } else {
                                ""
                            }
                        }
                        .axisLabelColor(Color.LightGray)
                        .axisLineColor(Color.Transparent)
                        .backgroundColor(Color.White)
                        .build()

                    val yAxisData = AxisData.Builder()
                        .steps(4)
                        .labelData { i ->
                            val value = (i * 500).toDouble()
                            String.format(Locale.US, "$%.0f", value)
                        }
                        .axisLabelColor(Color.LightGray)
                        .axisLineColor(Color.Transparent)
                        .backgroundColor(Color.White)
                        .build()

                    LineChart(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        lineChartData = LineChartData(
                            linePlotData = LinePlotData(lines = listOf(
                                Line(
                                    dataPoints = points,
                                    lineStyle = LineStyle(
                                        color = Color(0xFF4D73FF),
                                        lineType = LineType.SmoothCurve(false)
                                    ),
                                    intersectionPoint = IntersectionPoint(
                                        color = Color(0xFF4D73FF)
                                    ),
                                    selectionHighlightPoint = SelectionHighlightPoint(),
                                    shadowUnderLine = ShadowUnderLine(
                                        alpha = 0.1f,
                                        color = Color(0xFF4D73FF)
                                    ),
                                    selectionHighlightPopUp = SelectionHighlightPopUp()
                                )
                            )),
                            xAxisData = xAxisData,
                            yAxisData = yAxisData,
                            gridLines = GridLines(color = Color(0xFFF0F0F0)),
                            backgroundColor = Color.White
                        )
                    )
                } else {
                    NoDataPlaceholder()
                }
            } else {
                NoDataPlaceholder()
            }
        }
    }
}

@Composable
private fun NoDataPlaceholder() {
    Box(
        modifier = Modifier
            .height(180.dp)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("No savings data available", color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Set savings goals to track progress",
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
    }
}

private fun convertSavingsToPoints(savingsData: List<SavingsMonthlyData>): List<co.yml.charts.common.model.Point> {
    return savingsData.mapIndexed { index, data ->
        co.yml.charts.common.model.Point(
            x = index.toFloat(),
            y = data.savedAmount.toFloat()
        )
    }
}

private fun convertDailySavingsToPoints(dailyData: List<DailyData>): List<co.yml.charts.common.model.Point> {
    return dailyData.mapIndexed { index, data ->
        co.yml.charts.common.model.Point(
            x = index.toFloat(),
            y = data.savings.toFloat()
        )
    }
}