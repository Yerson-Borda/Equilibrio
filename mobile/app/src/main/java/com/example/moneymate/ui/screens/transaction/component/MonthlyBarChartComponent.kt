package com.example.moneymate.ui.screens.transaction.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.transaction.model.ChartFilter
import com.example.domain.transaction.model.MonthlyChartData
import com.example.domain.transaction.model.PeriodFilter
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlyBarChartComponent(
    monthlyChartData: MonthlyChartData,
    onFilterChanged: (ChartFilter) -> Unit,
    onPeriodChanged: (PeriodFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    println("📊 DEBUG: MonthlyBarChartComponent recomposing with filter: ${monthlyChartData.selectedFilter}")
    val months = monthlyChartData.months
    val selectedFilter = monthlyChartData.selectedFilter
    val selectedPeriod = monthlyChartData.selectedPeriod

    // State for hover effect
    var hoveredBarIndex by remember { mutableStateOf<Int?>(null) }

    // Convert Dp to Px
    val density = LocalDensity.current

    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        // Header with title and filters
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

            // Year/Month Filter only
            FilterDropdown(
                items = PeriodFilter.entries.map { it.name },
                selectedItem = selectedPeriod.name,
                onItemSelected = { period ->
                    onPeriodChanged(PeriodFilter.valueOf(period))
                },
                modifier = Modifier.width(120.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Income/Expenses Toggle Switch - FIXED VERSION
        IncomeExpenseToggle(
            selectedFilter = selectedFilter,
            onFilterChanged = onFilterChanged,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Chart Container - Increased height
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .pointerInput(months, selectedFilter) { // Add selectedFilter as key to recompose on filter change
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                val position = event.changes.first().position

                                when (event.type) {
                                    PointerEventType.Move,
                                    PointerEventType.Enter,
                                    PointerEventType.Exit -> {
                                        // Calculate which bar is being hovered
                                        val barWidth = if (months.size == 1) {
                                            size.width - with(density) { 96.dp.toPx() }
                                        } else {
                                            (size.width - with(density) { 48.dp.toPx() }) / months.size - with(density) { 8.dp.toPx() }
                                        }

                                        val hoveredIndex = months.indices.find { index ->
                                            val startX = if (months.size == 1) {
                                                with(density) { 48.dp.toPx() }
                                            } else {
                                                index * (barWidth + with(density) { 8.dp.toPx() }) + with(density) { 48.dp.toPx() }
                                            }

                                            position.x >= startX &&
                                                    position.x <= startX + barWidth &&
                                                    position.y <= size.height - with(density) { 40.dp.toPx() }
                                        }

                                        hoveredBarIndex = hoveredIndex
                                    }
                                    else -> {}
                                }
                            }
                        }
                    }
            ) {
                val barWidth = if (months.size == 1) {
                    size.width - with(density) { 96.dp.toPx() }
                } else {
                    (size.width - with(density) { 48.dp.toPx() }) / months.size - with(density) { 8.dp.toPx() }
                }
                val maxBarHeight = size.height - with(density) { 80.dp.toPx() }

                // Calculate max value for the selected filter
                val maxValue = when (selectedFilter) {
                    ChartFilter.INCOME -> months.maxOfOrNull { it.income } ?: 100000.0
                    ChartFilter.EXPENSES -> months.maxOfOrNull { it.expenses } ?: 100000.0
                }

                // Draw Y-axis labels and grid lines
                val yStep = maxBarHeight / 4
                for (i in 0..4) {
                    val yPos = size.height - with(density) { 40.dp.toPx() } - (i * yStep)
                    val value = (maxValue * i / 4).toInt()

                    // Grid line
                    drawContext.canvas.nativeCanvas.drawLine(
                        with(density) { 40.dp.toPx() }, yPos, size.width, yPos,
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.parseColor("#E5E5E5")
                            strokeWidth = with(density) { 1.dp.toPx() }
                        }
                    )

                    // Y-axis label
                    drawContext.canvas.nativeCanvas.drawText(
                        formatAmountForYAxis(value),
                        with(density) { 8.dp.toPx() },
                        yPos + with(density) { 4.dp.toPx() },
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.parseColor("#666666")
                            textSize = with(density) { 10.sp.toPx() }
                        }
                    )
                }

                // Draw bars for each month
                months.forEachIndexed { index, monthData ->
                    val startX = if (months.size == 1) {
                        with(density) { 48.dp.toPx() }
                    } else {
                        index * (barWidth + with(density) { 8.dp.toPx() }) + with(density) { 48.dp.toPx() }
                    }

                    val value = when (selectedFilter) {
                        ChartFilter.INCOME -> monthData.income
                        ChartFilter.EXPENSES -> monthData.expenses
                    }

                    val barHeight = if (maxValue > 0) {
                        (value.toFloat() / maxValue.toFloat()) * maxBarHeight
                    } else {
                        0f
                    }
                    val barY = size.height - barHeight - with(density) { 40.dp.toPx() }

                    // Determine bar color based on hover state
                    val isHovered = hoveredBarIndex == index
                    val barColor = when {
                        isHovered -> when (selectedFilter) {
                            ChartFilter.INCOME -> Color(0xFF26A69A) // Darker green when hovered
                            ChartFilter.EXPENSES -> Color(0xFFE53935) // Darker red when hovered
                        }
                        else -> when (selectedFilter) {
                            ChartFilter.INCOME -> Color(0xFF4ECDC4) // Green for income
                            ChartFilter.EXPENSES -> Color(0xFFFF6B6B) // Red for expenses
                        }
                    }

                    // Draw bar
                    drawRect(
                        color = barColor,
                        topLeft = Offset(startX, barY),
                        size = Size(barWidth, barHeight)
                    )

                    // Draw value on top of hovered bar
                    if (isHovered && barHeight > with(density) { 20.dp.toPx() }) {
                        drawContext.canvas.nativeCanvas.drawText(
                            formatAmountForBar(value),
                            startX + barWidth / 2,
                            barY - with(density) { 12.dp.toPx() },
                            android.graphics.Paint().apply {
                                color = android.graphics.Color.BLACK
                                textSize = with(density) { 10.sp.toPx() }
                                textAlign = android.graphics.Paint.Align.CENTER
                            }
                        )
                    }

                    // Draw month label
                    drawContext.canvas.nativeCanvas.drawText(
                        monthData.month,
                        startX + barWidth / 2,
                        size.height - with(density) { 20.dp.toPx() },
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.parseColor("#666666")
                            textSize = with(density) { 10.sp.toPx() }
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Summary - FIXED to show correct totals based on selected filter
        val totalIncome = months.sumOf { it.income }
        val totalExpenses = months.sumOf { it.expenses }
        val netAmount = totalIncome - totalExpenses
        val selectedTotal = when (selectedFilter) {
            ChartFilter.INCOME -> totalIncome
            ChartFilter.EXPENSES -> totalExpenses
        }

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
                    text = "$${String.format("%,.0f", abs(netAmount))}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (netAmount >= 0) Color(0xFF4ECDC4) else Color(0xFFFF6B6B)
                )
            }
        }
    }
}

@Composable
fun IncomeExpenseToggle(
    selectedFilter: ChartFilter,
    onFilterChanged: (ChartFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Show:",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            ChartFilter.entries.forEach { filter ->
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(
                        index = filter.ordinal,
                        count = ChartFilter.entries.size
                    ),
                    onClick = {
                        println("🎯 DEBUG: IncomeExpenseToggle - Button clicked: ${filter.name}")
                        println("🎯 DEBUG: Current selected filter: $selectedFilter")
                        println("🎯 DEBUG: New filter to apply: $filter")
                        onFilterChanged(filter)
                    },
                    selected = filter == selectedFilter
                ) {
                    Text(
                        text = filter.name,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDropdown(
    items: List<String>,
    selectedItem: String,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

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
                text = selectedItem,
                fontSize = 14.sp,
                color = Color.Black
            )
        }

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = item,
                            fontSize = 14.sp,
                            color = Color.Black
                        )
                    },
                    onClick = {
                        onItemSelected(item)
                        expanded = false
                    }
                )
            }
        }
    }
}

private fun formatAmountForYAxis(amount: Int): String {
    return if (amount >= 1000) {
        "$${amount / 1000}K"
    } else {
        "$$amount"
    }
}

private fun formatAmountForBar(amount: Double): String {
    return if (amount >= 1000) {
        "$${(amount / 1000).toInt()}K"
    } else {
        "$${amount.toInt()}"
    }
}