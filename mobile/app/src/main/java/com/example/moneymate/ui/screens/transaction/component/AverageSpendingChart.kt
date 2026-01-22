// ui/screens/transaction/component/AverageSpendingChart.kt
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.domain.transaction.model.AverageSpendingData
import com.example.domain.transaction.model.PeriodFilter
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun AverageSpendingChart(
    averageSpendingData: List<AverageSpendingData>,
    period: PeriodFilter,
    onPeriodChanged: (PeriodFilter) -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    var selectedPeriod by remember { mutableStateOf(period) }
    val checkedCategories = remember { mutableStateOf(mutableMapOf<Int, Boolean>()) }

    // Initialize all checkboxes to checked by default
    if (checkedCategories.value.isEmpty() && averageSpendingData.isNotEmpty()) {
        averageSpendingData.forEach { data ->
            checkedCategories.value[data.categoryId] = true
        }
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
                text = "Average spending per category",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            // Period Selector
            AverageSpendingPeriodDropdown(
                selectedPeriod = selectedPeriod,
                onPeriodSelected = { newPeriod ->
                    selectedPeriod = newPeriod
                    onPeriodChanged(newPeriod)
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Loading...",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        } else if (averageSpendingData.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No spending data available",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        } else {
            // Calculate total of selected categories
            val selectedTotal = averageSpendingData
                .filter { checkedCategories.value[it.categoryId] ?: true }
                .sumOf { it.totalPeriodSpent }

            // Total spending header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total spending",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                Text(
                    text = "$${DecimalFormat("#,##0").format(selectedTotal)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
            }

            // Categories list
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(averageSpendingData) { spendingData ->
                    val isChecked = checkedCategories.value[spendingData.categoryId] ?: true

                    AverageSpendingListItem(
                        spendingData = spendingData,
                        isChecked = isChecked,
                        onCheckedChange = { checked ->
                            checkedCategories.value = checkedCategories.value.toMutableMap().apply {
                                put(spendingData.categoryId, checked)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AverageSpendingListItem(
    spendingData: AverageSpendingData,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Category name and amount
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = spendingData.categoryName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = Color.Black
            )

            Text(
                text = "$${DecimalFormat("#,##0").format(spendingData.totalPeriodSpent)}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
        }
    }
}

@Composable
fun AverageSpendingPeriodDropdown(
    selectedPeriod: PeriodFilter,
    onPeriodSelected: (PeriodFilter) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    // Get period options
    val periodOptions = getAverageSpendingPeriodOptions()

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
                    text = getAverageSpendingPeriodDisplayName(selectedPeriod),
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

// Helper function to get period options for average spending
private fun getAverageSpendingPeriodOptions(): List<Pair<PeriodFilter, String>> {
    val options = mutableListOf<Pair<PeriodFilter, String>>()

    // Add month options (last 6 months)
    val currentDate = LocalDate.now()
    val monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())

    for (i in 0..5) {
        val monthDate = currentDate.minusMonths(i.toLong())
        val monthName = monthDate.format(monthFormatter)
        options.add(PeriodFilter.MONTH to monthName)
    }

    // Add standard period filters
    options.addAll(listOf(
        PeriodFilter.DAY to "Today",
        PeriodFilter.DAYS_7 to "Last 7 Days",
        PeriodFilter.DAYS_30 to "Last 30 Days",
        PeriodFilter.DAYS_90 to "Last 90 Days",
        PeriodFilter.YEAR to "This Year",
    ))

    return options.distinctBy { it.second }
}

// Helper function to get display name for period
private fun getAverageSpendingPeriodDisplayName(period: PeriodFilter): String {
    return when (period) {
        PeriodFilter.YEAR -> "This Year"
        PeriodFilter.MONTH -> "This Month"
        PeriodFilter.DAYS_7 -> "Last 7 Days"
        PeriodFilter.DAYS_30 -> "Last 30 Days"
        PeriodFilter.DAYS_90 -> "Last 90 Days"
        PeriodFilter.DAY -> "Today"
        else -> "This Month"
    }
}