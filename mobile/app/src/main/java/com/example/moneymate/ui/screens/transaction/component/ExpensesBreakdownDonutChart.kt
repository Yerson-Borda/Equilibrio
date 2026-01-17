package com.example.moneymate.ui.screens.transaction.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.yml.charts.common.model.PlotType
import co.yml.charts.ui.piechart.charts.DonutPieChart
import co.yml.charts.ui.piechart.models.PieChartConfig
import co.yml.charts.ui.piechart.models.PieChartData
import com.example.domain.transaction.model.AverageSpendingData
import com.example.domain.transaction.model.PeriodFilter
import com.example.domain.transaction.model.TopCategoryData
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ExpensesBreakdownDonutChart(
    topCategories: List<TopCategoryData>,
    averageSpending: List<AverageSpendingData>,
    period: PeriodFilter,
    onPeriodChanged: (PeriodFilter) -> Unit,
    isLoading: Boolean = false, // Add loading state
    modifier: Modifier = Modifier
) {
    var selectedPeriod by remember { mutableStateOf(period) }
    val checkedCategories = remember { mutableStateOf(mutableMapOf<Int, Boolean>()) }

    // Reset checkboxes when data changes
    LaunchedEffect(topCategories) {
        if (topCategories.isNotEmpty() && checkedCategories.value.isEmpty()) {
            topCategories.forEach { category ->
                checkedCategories.value[category.categoryId] = true
            }
        }
    }

    // Filter categories based on checkbox state
    val filteredCategories = topCategories.filter {
        checkedCategories.value[it.categoryId] ?: true
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color.White)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Expenses Breakdown",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            // Period Selector
            PeriodDropdown(
                selectedPeriod = selectedPeriod,
                onPeriodSelected = { newPeriod ->
                    selectedPeriod = newPeriod
                    onPeriodChanged(newPeriod) // This should trigger data refresh in parent
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            // Show loading indicator
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (filteredCategories.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No expense data available",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        } else {
            // Chart and List Side by Side
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            ) {
                // Chart on the left
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(250.dp),
                    contentAlignment = Alignment.Center
                ) {
                    DonutChartSection(
                        categories = filteredCategories,
                        modifier = Modifier.size(180.dp)
                    )
                }

                // Category list on the right (scrollable)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(250.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    filteredCategories.forEachIndexed { index, category ->
                        val averageData = averageSpending.find { it.categoryId == category.categoryId }

                        // Show checkbox for each category
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(getCategoryColor(index))
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                // Category name and amount on same line
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = category.categoryName,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.Black
                                    )

                                    Text(
                                        text = "$${DecimalFormat("#,##0").format(category.totalAmount)}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.Black
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DonutChartSection(
    categories: List<TopCategoryData>,
    modifier: Modifier = Modifier
) {
    // Calculate total
    val total = categories.sumOf { it.totalAmount }

    // Prepare data for the donut chart
    val chartData = remember(categories) {
        PieChartData(
            slices = categories.mapIndexed { index, category ->
                PieChartData.Slice(
                    value = category.totalAmount.toFloat(),
                    color = getCategoryColor(index),
                    label = category.categoryName
                )
            },
            plotType = PlotType.Donut
        )
    }

    val chartConfig = PieChartConfig(
        isAnimationEnable = true,
        showSliceLabels = false,
        sliceLabelTextSize = 8.sp,
        animationDuration = 1000,
        strokeWidth = 40f,
        labelVisible = true,
        labelType = PieChartConfig.LabelType.PERCENTAGE, // Show percentage in center
        labelFontSize = 14.sp,
        labelColor = Color.Black,
        isSumVisible = false,
        sumUnit = "",
        backgroundColor = Color.Transparent,
        chartPadding = 20,
        activeSliceAlpha = 1f,
        inActiveSliceAlpha = 1f
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            DonutPieChart(
                modifier = Modifier.fillMaxSize(),
                pieChartData = chartData,
                pieChartConfig = chartConfig
            )
        }
    }
}

@Composable
fun PeriodDropdown(
    selectedPeriod: PeriodFilter,
    onPeriodSelected: (PeriodFilter) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    // Get period options
    val periodOptions = getPeriodOptions()

    Box {
        Surface(
            modifier = Modifier
                .clip(MaterialTheme.shapes.small)
                .clickable { expanded = true }
                .padding(horizontal = 12.dp, vertical = 8.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = getPeriodDisplayName(selectedPeriod),
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            periodOptions.forEach { (period, label) ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = label,
                            color = if (period == selectedPeriod) MaterialTheme.colorScheme.primary else Color.Black
                        )
                    },
                    onClick = {
                        onPeriodSelected(period)
                        expanded = false
                    }
                )
            }
        }
    }
}

// Helper function to get period options including specific months
private fun getPeriodOptions(): List<Pair<PeriodFilter, String>> {
    val options = mutableListOf<Pair<PeriodFilter, String>>()

    // Get current month and year
    val currentDate = LocalDate.now()
    val monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())

    // Add last 6 months (including current)
    for (i in 0..5) {
        val monthDate = currentDate.minusMonths(i.toLong())
        val monthName = monthDate.format(monthFormatter)
        // For month-specific filtering, we need a way to identify which month
        // We'll use string representation for now
        options.add(PeriodFilter.MONTH to monthName)
    }

    // Add other period filters
    options.addAll(listOf(
        PeriodFilter.DAY to "Today",
        PeriodFilter.DAYS_7 to "Last 7 Days",
        PeriodFilter.DAYS_15 to "Last 15 Days",
        PeriodFilter.DAYS_30 to "Last 30 Days",
        PeriodFilter.DAYS_90 to "Last 90 Days",
        PeriodFilter.MONTH to "This Month", // Current month as "This Month"
        PeriodFilter.YEAR to "This Year"
    ))

    // Remove duplicates by converting to map and back to list
    return options.distinctBy { it.second }
}

// Helper function to get display name for period
private fun getPeriodDisplayName(period: PeriodFilter): String {
    return when (period) {
        PeriodFilter.YEAR -> "This Year"
        PeriodFilter.MONTH -> "This Month"
        PeriodFilter.DAYS_7 -> "Last 7 Days"
        PeriodFilter.DAYS_15 -> "Last 15 Days"
        PeriodFilter.DAYS_30 -> "Last 30 Days"
        PeriodFilter.DAYS_90 -> "Last 90 Days"
        PeriodFilter.DAY -> "Today"
        else -> "This Month"
    }
}

// Helper functions for colors
private fun getCategoryColor(index: Int): Color {
    val colors = listOf(
        Color(0xFF4ECDC4), // Teal
        Color(0xFFFF6B6B), // Red
        Color(0xFFC7F464), // Green
        Color(0xFF4A90E2), // Blue
        Color(0xFFA45EE5), // Purple
        Color(0xFFFFD166), // Yellow
        Color(0xFF06D6A0), // Mint
        Color(0xFFEF476F)  // Pink
    )
    return colors[index % colors.size]
}

private fun getComparisonText(averageData: AverageSpendingData): String {
    return when (averageData.periodType.lowercase()) {
        "month" -> "Monthly avg: $${String.format("%.0f", averageData.totalPeriodSpent)}"
        "year" -> "Yearly avg: $${String.format("%.0f", averageData.totalPeriodSpent)}"
        "day" -> "Daily avg: $${String.format("%.0f", averageData.totalPeriodSpent)}"
        else -> "Avg: $${String.format("%.0f", averageData.totalPeriodSpent)}"
    }
}