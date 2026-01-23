package com.example.moneymate.ui.screens.transaction.component

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.domain.transaction.model.ChartFilter
import com.example.domain.transaction.model.ChartType
import com.example.domain.transaction.model.DateRange
import com.example.domain.transaction.model.PeriodFilter
import com.example.domain.transaction.model.TransactionChartsData
import com.example.moneymate.ui.screens.transaction.TransactionScreenViewModel
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SwipeableChartContainer(
    chartsData: TransactionChartsData,
    currentChartType: ChartType,
    onChartTypeChanged: (ChartType) -> Unit,
    onFilterChanged: (ChartFilter) -> Unit = {},
    onPeriodChanged: (PeriodFilter) -> Unit = {},
    onDateRangeChanged: (DateRange) -> Unit = {},
    viewModel: TransactionScreenViewModel,
    modifier: Modifier = Modifier
) {
    val chartTypes = ChartType.values()
    val pagerState = rememberPagerState(initialPage = chartTypes.indexOf(currentChartType)) { chartTypes.size }
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = modifier) {
        // Swipeable Chart Area
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(380.dp)
        ) { page ->
            val chartType = chartTypes[page]

            when (chartType) {
                ChartType.MONTHLY_TRENDS -> {
                    YChartMonthlyBarChartComponent(
                        monthlyChartData = chartsData.monthlyChart,
                        onFilterChanged = onFilterChanged,
                        onPeriodChanged = onPeriodChanged,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                // Add new chart type for comparison line chart
                ChartType.CATEGORY_BREAKDOWN -> {
                    YChartIncomeExpenseLineChartComponent(
                        monthlyChartData = chartsData.monthlyChart,
                        onDateRangeChanged = onDateRangeChanged,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                ChartType.MONTHLY_COMPARISON -> {
                    CategoryPieChartComponent(
                        categorySummaryData = chartsData.categorySummary,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                ChartType.TOP_CATEGORIES -> {
                    // This is now the new category breakdown donut chart
                    ExpensesBreakdownDonutChart(
                        topCategories = chartsData.topCategories,
                        averageSpending = chartsData.averageSpending,
                        period = chartsData.currentPeriod,
                        onPeriodChanged = { newPeriod ->
                            viewModel.loadAverageSpending(newPeriod)
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                ChartType.AVERAGE_SPENDING_RADAR -> {
                    RadarAverageSpendingChart(
                        averageSpendingData = chartsData.averageSpending,
                        period = chartsData.currentPeriod,
                        onPeriodChanged = { newPeriod ->
                            viewModel.updatePeriodFilter(newPeriod)
                        },
                        isLoading = false,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                ChartType.AVERAGE_SPENDING_LIST -> {
                    AverageSpendingChart(
                        averageSpendingData = chartsData.averageSpending,
                        period = chartsData.currentPeriod,
                        onPeriodChanged = { newPeriod ->
                            viewModel.updatePeriodFilter(newPeriod)
                        },
                        isLoading = false,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        ChartSwipeIndicator(
            currentPage = pagerState.currentPage,
            totalPages = chartTypes.size,
            onPageSelected = { page ->
                coroutineScope.launch {
                    pagerState.animateScrollToPage(page)
                }
                onChartTypeChanged(chartTypes[page])
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        )
    }
}
@Composable
private fun ChartSwipeIndicator(
    currentPage: Int,
    totalPages: Int,
    onPageSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalPages) { page ->
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(8.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(
                        if (page == currentPage) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            Color.LightGray
                        }
                    )
                    .clickable { onPageSelected(page) }
            )
        }
    }
}