package com.example.moneymate.ui.screens.goal

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.moneymate.ui.components.states.EmptyState
import com.example.moneymate.ui.components.states.FullScreenError
import com.example.moneymate.ui.components.states.FullScreenLoading
import com.example.moneymate.ui.navigation.BottomNavigationBar
import com.example.moneymate.ui.screens.goal.component.BalanceCard
import com.example.moneymate.ui.screens.goal.component.BudgetCard
import com.example.moneymate.ui.screens.goal.component.DailyLimitCard
import com.example.moneymate.ui.screens.goal.component.NotificationToggle
import com.example.moneymate.utils.ScreenState
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun GoalScreen(
    viewModel: GoalScreenViewModel = koinViewModel(),
    currentScreen: String = "goals",
    onNavigationItemSelected: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            snackbarHostState.showSnackbar(
                message = error.getUserFriendlyMessage(),
                duration = SnackbarDuration.Short
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = Color(0xFF121212)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState.budgetState) {
                is ScreenState.Loading -> {
                    FullScreenLoading(message = "Loading budget data...")
                }
                is ScreenState.Error -> {
                    FullScreenError(
                        error = state.error,
                        onRetry = { viewModel.loadBudgetData() }
                    )
                }
                is ScreenState.Success -> {
                    val budget = state.data
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // Balance Card
                        BalanceCard(
                            balance = "5,240.21",
                            vsLastMonth = "+2.5% vs Last month",
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        BudgetCard(
                            title = "Budget ${getMonthName(budget.month)}",
                            limit = String.format("$%.0f", budget.monthlyLimit),
                            spent = String.format("$%.0f", budget.monthlySpent),
                            progress = budget.monthlyProgress,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        DailyLimitCard(
                            title = "Daily Limit",
                            limit = String.format("$%.0f", budget.dailyLimit),
                            spent = String.format("$%.0f", budget.dailySpent),
                            progress = budget.dailyProgress,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        NotificationToggle(
                            isEnabled = uiState.isNotificationsEnabled,
                            onToggle = { viewModel.toggleNotifications() },
                            modifier = Modifier.padding(bottom = 24.dp)
                        )

                        TabRow(
                            selectedTabIndex = uiState.currentTabIndex,
                            modifier = Modifier.fillMaxWidth(),
                            containerColor = Color(0xFF1E1E1E),
                            contentColor = Color.White,
                            indicator = { tabPositions ->
                                androidx.compose.material3.TabRowDefaults.Indicator(
                                    modifier = Modifier.tabIndicatorOffset(tabPositions[uiState.currentTabIndex]),
                                    height = 3.dp,
                                    color = Color(0xFF4CAF50)
                                )
                            }
                        ) {
                            Tab(
                                text = {
                                    Text(
                                        "Overview",
                                        color = if (uiState.currentTabIndex == 0) Color.White else Color.White.copy(alpha = 0.6f)
                                    )
                                },
                                selected = uiState.currentTabIndex == 0,
                                onClick = {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(0)
                                    }
                                    viewModel.setCurrentTab(0)
                                }
                            )

                            Tab(
                                text = {
                                    Text(
                                        "Details",
                                        color = if (uiState.currentTabIndex == 1) Color.White else Color.White.copy(alpha = 0.6f)
                                    )
                                },
                                selected = uiState.currentTabIndex == 1,
                                onClick = {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(1)
                                    }
                                    viewModel.setCurrentTab(1)
                                }
                            )
                        }

                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) { page ->
                            when (page) {
                                0 -> {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp)
                                    ) {
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = androidx.compose.material3.CardDefaults.cardColors(
                                                containerColor = Color(0xFF1E1E1E)
                                            )
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(16.dp)
                                            ) {
                                                Text(
                                                    text = "Monthly Overview",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    color = Color.White,
                                                    modifier = Modifier.padding(bottom = 16.dp)
                                                )
                                                BudgetDetailsSection(budget)
                                            }
                                        }
                                    }
                                }
                                1 -> {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp)
                                    ) {
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = androidx.compose.material3.CardDefaults.cardColors(
                                                containerColor = Color(0xFF1E1E1E)
                                            )
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(16.dp)
                                            ) {
                                                Text(
                                                    text = "Detailed Analysis",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    color = Color.White,
                                                    modifier = Modifier.padding(bottom = 16.dp)
                                                )

                                                SpendingAnalysisSection(budget)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                is ScreenState.Empty -> {
                    EmptyState(
                        title = "No Budget Set",
                        message = "You haven't set up a budget yet. Create one to start tracking your spending!",
                        icon = androidx.compose.material.icons.Icons.Default.Payments,
                        action = {
                            androidx.compose.material3.Button(onClick = {
                                // TODO: Navigate to budget setup screen
                            }) {
                                Text("Set Up Budget")
                            }
                        }
                    )
                }
            }

            BottomNavigationBar(
                currentScreen = currentScreen,
                onNavigationItemSelected = onNavigationItemSelected,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
fun BudgetDetailsSection(budget: com.example.domain.budget.model.Budget) {
    Column {
        DetailRow(
            label = "Monthly Limit",
            value = String.format("$%.2f", budget.monthlyLimit),
            textColor = Color.White
        )
        DetailRow(
            label = "Monthly Spent",
            value = String.format("$%.2f", budget.monthlySpent),
            textColor = Color.White
        )
        DetailRow(
            label = "Monthly Remaining",
            value = String.format("$%.2f", budget.monthlyRemaining),
            isPositive = budget.monthlyRemaining >= 0,
            textColor = Color.White
        )
        DetailRow(
            label = "Daily Limit",
            value = String.format("$%.2f", budget.dailyLimit),
            textColor = Color.White
        )
        DetailRow(
            label = "Daily Spent",
            value = String.format("$%.2f", budget.dailySpent),
            textColor = Color.White
        )
        DetailRow(
            label = "Daily Remaining",
            value = String.format("$%.2f", budget.dailyRemaining),
            isPositive = budget.dailyRemaining >= 0,
            textColor = Color.White
        )
        DetailRow(
            label = "Last Updated",
            value = budget.lastUpdatedDate,
            textColor = Color.White
        )
    }
}

@Composable
fun SpendingAnalysisSection(budget: com.example.domain.budget.model.Budget) {
    Column {
        ProgressAnalysisCard(
            title = "Monthly Progress",
            current = budget.monthlySpent,
            limit = budget.monthlyLimit,
            progress = budget.monthlyProgress,
            textColor = Color.White
        )

        Spacer(modifier = Modifier.padding(vertical = 8.dp))

        ProgressAnalysisCard(
            title = "Daily Progress",
            current = budget.dailySpent,
            limit = budget.dailyLimit,
            progress = budget.dailyProgress,
            textColor = Color.White
        )

        if (budget.isMonthlyExceeded) {
            WarningMessage(
                message = "⚠️ Monthly budget exceeded by $${String.format("%.2f", budget.monthlySpent - budget.monthlyLimit)}"
            )
        }

        if (budget.isDailyExceeded) {
            WarningMessage(
                message = "⚠️ Daily limit exceeded by $${String.format("%.2f", budget.dailySpent - budget.dailyLimit)}"
            )
        }
    }
}

@Composable
fun DetailRow(
    label: String,
    value: String,
    isPositive: Boolean? = null,
    textColor: Color = Color.White
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = textColor.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = when {
                isPositive == true -> Color(0xFF4CAF50)
                isPositive == false -> Color(0xFFF44336)
                else -> textColor
            }
        )
    }
}

@Composable
fun ProgressAnalysisCard(
    title: String,
    current: Double,
    limit: Double,
    progress: Float,
    textColor: Color = Color.White
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = textColor,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "Spent: $${String.format("%.2f", current)} of $${String.format("%.2f", limit)}",
            style = MaterialTheme.typography.bodyMedium,
            color = textColor
        )
        Text(
            text = "Progress: ${String.format("%.0f", progress * 100)}%",
            style = MaterialTheme.typography.bodySmall,
            color = when {
                progress > 0.9 -> Color(0xFFF44336)
                progress > 0.7 -> Color(0xFFFF9800)
                else -> textColor.copy(alpha = 0.8f)
            }
        )
    }
}

@Composable
fun WarningMessage(message: String) {
    Text(
        text = message,
        style = MaterialTheme.typography.bodySmall,
        color = Color(0xFFF44336), // Red
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    )
}

fun getMonthName(monthNumber: Int): String {
    return when (monthNumber) {
        1 -> "January"
        2 -> "February"
        3 -> "March"
        4 -> "April"
        5 -> "May"
        6 -> "June"
        7 -> "July"
        8 -> "August"
        9 -> "September"
        10 -> "October"
        11 -> "November"
        12 -> "December"
        else -> "Month $monthNumber"
    }
}