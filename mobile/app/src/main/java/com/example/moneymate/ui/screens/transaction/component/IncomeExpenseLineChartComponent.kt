package com.example.moneymate.ui.screens.transaction.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.transaction.model.DateRange
import com.example.domain.transaction.model.MonthlyChartData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncomeExpenseLineChartComponent(
    monthlyChartData: MonthlyChartData,
    onDateRangeChanged: (DateRange) -> Unit,
    modifier: Modifier = Modifier
) {
    val days = monthlyChartData.days
    val dateRange = monthlyChartData.dateRange

    LaunchedEffect(days) {
        println("=== DEBUG: Line Chart Data ===")
        println("Total days: ${days.size}")
        println("Date range: ${dateRange.startDate} to ${dateRange.endDate}")

        var hasNonZeroData = false
        days.forEachIndexed { index, day ->
            if (day.income > 0 || day.expenses > 0) {
                hasNonZeroData = true
                println("DAY $index: ${day.dayLabel} - Income: $${day.income}, Expense: $${day.expenses}")
            }
        }

        if (!hasNonZeroData) {
            println("WARNING: All data points are zero!")
            println("Sample of all days:")
            days.take(5).forEachIndexed { index, day ->
                println("Sample $index: ${day.dayLabel} - Income: $${day.income}, Expense: $${day.expenses}")
            }
        }
        println("=== END DEBUG ===")
    }

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

        // From and To date pickers
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

            // To Date Picker
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

        // Chart Container with proper scrolling
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .horizontalScroll(rememberScrollState())
        ) {
            LineChart(
                days = days,
                modifier = Modifier
                    .fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Legend for income and expense lines
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

            // Expense legend
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

        // Summary with realistic amounts
        val totalIncome = days.sumOf { it.income }
        val totalExpenses = days.sumOf { it.expenses }
        val netAmount = totalIncome - totalExpenses

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
                    text = "$${String.format("%.2f", totalIncome)}", // Show 2 decimal places
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
                    text = "$${String.format("%.2f", totalExpenses)}", // Show 2 decimal places
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
                    text = "$${String.format("%.2f", netAmount)}", // Show 2 decimal places
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (netAmount >= 0) Color(0xFF4ECDC4) else Color(0xFFFF6B6B)
                )
            }
        }
    }
}

@Composable
private fun LineChart(
    days: List<com.example.domain.transaction.model.DailyData>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (days.isEmpty()) return@Canvas

        // Calculate chart width based on number of days
        val minDayWidth = 60.dp.toPx()
        val chartWidth = maxOf(size.width, days.size * minDayWidth)

        val paddingTop = 40.dp.toPx()
        val paddingBottom = 60.dp.toPx()
        val paddingLeft = 48.dp.toPx()
        val paddingRight = 16.dp.toPx()

        val chartHeight = size.height - paddingTop - paddingBottom
        val availableWidth = chartWidth - paddingLeft - paddingRight

        // Calculate max value for scaling - use REALISTIC ranges for your data
        val maxIncome = days.maxOfOrNull { it.income } ?: 50.0
        val maxExpenses = days.maxOfOrNull { it.expenses } ?: 50.0
        val maxValue = maxOf(maxIncome, maxExpenses, 25.0)

        // Draw Y-axis labels and grid lines
        val yStep = chartHeight / 4
        for (i in 0..4) {
            val yPos = size.height - paddingBottom - (i * yStep)
            val value = (maxValue * i / 4).toInt()

            // Grid line - extend across the entire chart width
            drawLine(
                color = Color(0xFFE5E5E5),
                start = Offset(paddingLeft, yPos),
                end = Offset(chartWidth - paddingRight, yPos),
                strokeWidth = 1.dp.toPx()
            )

            // Y-axis label - show actual dollar amounts without K/M formatting
            drawContext.canvas.nativeCanvas.drawText(
                "$$value",
                paddingLeft - 32.dp.toPx(),
                yPos + 4.dp.toPx(),
                android.graphics.Paint().apply {
                    color = android.graphics.Color.parseColor("#666666")
                    textSize = 10.sp.toPx()
                }
            )
        }

        // Draw X-axis labels (actual day labels from data)
        days.forEachIndexed { index, dayData ->
            val xPos = if (days.size == 1) {
                paddingLeft + availableWidth / 2
            } else {
                paddingLeft + (index * availableWidth / (days.size - 1))
            }
            val yPos = size.height - paddingBottom + 20.dp.toPx()

            drawContext.canvas.nativeCanvas.drawText(
                dayData.dayLabel,
                xPos,
                yPos,
                android.graphics.Paint().apply {
                    color = android.graphics.Color.parseColor("#666666")
                    textSize = 10.sp.toPx()
                    textAlign = android.graphics.Paint.Align.CENTER
                }
            )
        }

        // FIX: Handle single data points for income
        val incomeDataPoints = days.map { it.income }
        val expenseDataPoints = days.map { it.expenses }

        // Draw income line (green) - even with single point
        if (incomeDataPoints.any { it > 0 }) {
            drawLineChart(
                dataPoints = incomeDataPoints,
                maxValue = maxValue,
                color = Color(0xFF4ECDC4),
                paddingLeft = paddingLeft,
                paddingTop = paddingTop,
                paddingBottom = paddingBottom,
                chartWidth = availableWidth,
                chartHeight = chartHeight
            )
        }

        // Draw expense line (red)
        if (expenseDataPoints.any { it > 0 }) {
            drawLineChart(
                dataPoints = expenseDataPoints,
                maxValue = maxValue,
                color = Color(0xFFFF6B6B),
                paddingLeft = paddingLeft,
                paddingTop = paddingTop,
                paddingBottom = paddingBottom,
                chartWidth = availableWidth,
                chartHeight = chartHeight
            )
        }

        // Draw data points for both lines
        days.forEachIndexed { index, dayData ->
            val xPos = if (days.size == 1) {
                paddingLeft + availableWidth / 2
            } else {
                paddingLeft + (index * availableWidth / (days.size - 1))
            }

            // Draw income point (only if there's income)
            if (dayData.income > 0) {
                val incomeYPos = size.height - paddingBottom -
                        (dayData.income / maxValue * chartHeight).toFloat()
                drawCircle(
                    color = Color(0xFF4ECDC4),
                    radius = 4.dp.toPx(), // Slightly larger for visibility
                    center = Offset(xPos, incomeYPos)
                )

                // FIX: Add value label for single income point
                if (incomeDataPoints.count { it > 0 } == 1) {
                    drawContext.canvas.nativeCanvas.drawText(
                        "$${dayData.income.toInt()}",
                        xPos,
                        incomeYPos - 12.dp.toPx(),
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.BLACK
                            textSize = 10.sp.toPx()
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                    )
                }
            }

            // Draw expense point (only if there's expense)
            if (dayData.expenses > 0) {
                val expenseYPos = size.height - paddingBottom -
                        (dayData.expenses / maxValue * chartHeight).toFloat()
                drawCircle(
                    color = Color(0xFFFF6B6B),
                    radius = 4.dp.toPx(),
                    center = Offset(xPos, expenseYPos)
                )
            }
        }
    }
}

private fun DrawScope.drawLineChart(
    dataPoints: List<Double>,
    maxValue: Double,
    color: Color,
    paddingLeft: Float,
    paddingTop: Float,
    paddingBottom: Float,
    chartWidth: Float,
    chartHeight: Float
) {
    // Filter out zero values and get only the points with actual data
    val nonZeroDataPoints = dataPoints.mapIndexedNotNull { index, value ->
        if (value > 0) index to value else null
    }

    // If we have less than 2 non-zero points, don't draw a line
    if (nonZeroDataPoints.size < 2) {
        // For single point, draw a small horizontal line
        if (nonZeroDataPoints.size == 1) {
            val (index, value) = nonZeroDataPoints[0]
            val x = paddingLeft + (index * chartWidth / (dataPoints.size - 1))
            val y = size.height - paddingBottom - (value / maxValue * chartHeight).toFloat()

            drawLine(
                color = color,
                start = Offset(x - 10.dp.toPx(), y),
                end = Offset(x + 10.dp.toPx(), y),
                strokeWidth = 3.dp.toPx() // Thicker for visibility
            )
        }
        return
    }

    // Create points only for non-zero values
    val points = nonZeroDataPoints.map { (index, value) ->
        val x = paddingLeft + (index * chartWidth / (dataPoints.size - 1))
        val y = size.height - paddingBottom - (value / maxValue * chartHeight).toFloat()
        Offset(x, y)
    }

    // Draw connecting lines between non-zero points
    for (i in 0 until points.size - 1) {
        drawLine(
            color = color,
            start = points[i],
            end = points[i + 1],
            strokeWidth = 3.dp.toPx() // Thicker for visibility
        )
    }
}

private fun formatAmountForYAxis(amount: Int): String {
    // For small amounts like yours (0-50 range), just show the dollar amount
    return "$$amount"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDropdown(
    selectedDate: String,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    // Generate date options (last 90 days)
    val dateOptions = generateDateOptions()
    val displayDate = formatDateForDisplay(selectedDate)

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .menuAnchor()
                .background(Color.White)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = displayDate,
                fontSize = 14.sp,
                color = Color.Black
            )
        }

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            dateOptions.forEach { dateOption ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = dateOption.display,
                            fontSize = 14.sp,
                            color = Color.Black
                        )
                    },
                    onClick = {
                        onDateSelected(dateOption.value)
                        expanded = false
                    }
                )
            }
        }
    }
}

private data class DateOption(val value: String, val display: String)

private fun generateDateOptions(): List<DateOption> {
    val options = mutableListOf<DateOption>()
    val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
    val displayFormat = java.text.SimpleDateFormat("dd MMM", java.util.Locale.getDefault())

    val calendar = java.util.Calendar.getInstance()

    // Generate options for last 90 days
    for (i in 0..90) {
        val date = calendar.time
        options.add(
            DateOption(
                value = dateFormat.format(date),
                display = displayFormat.format(date)
            )
        )
        calendar.add(java.util.Calendar.DAY_OF_YEAR, -1)
    }

    return options.reversed() // Oldest to newest
}

private fun formatDateForDisplay(dateString: String): String {
    return try {
        val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val outputFormat = java.text.SimpleDateFormat("dd MMM", java.util.Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date)
    } catch (e: Exception) {
        "Select"
    }
}