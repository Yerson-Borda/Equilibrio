package com.example.moneymate.ui.screens.transaction

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneymate.ui.navigation.BottomNavigationBar
import com.example.moneymate.ui.screens.transaction.component.SwipeableChartContainer
import com.example.moneymate.ui.screens.transaction.component.TransactionListItem
import com.example.moneymate.ui.screens.transaction.component.TransactionTopBar
import org.koin.androidx.compose.koinViewModel

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionScreen(
    onBackClick: () -> Unit,
    onNavigationItemSelected: (String) -> Unit,
    currentScreen: String = "transactions",
    viewModel: TransactionScreenViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Show error snackbar
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TransactionTopBar(
                onBackClick = onBackClick,
                onExportClick = {
                    // TODO: Implement export functionality
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(
                currentScreen = currentScreen,
                onNavigationItemSelected = onNavigationItemSelected
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(Color.White)
        ) {
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // REPLACE: Monthly Chart with Swipeable Container
                        item {
                            SwipeableChartContainer(
                                chartsData = uiState.chartsData,
                                currentChartType = uiState.chartsData.currentChartType,
                                onChartTypeChanged = { chartType ->
                                    viewModel.updateCurrentChartType(chartType)
                                },
                                onPeriodChanged = { period ->
                                    viewModel.updatePeriodFilter(period)
                                },
                                onDateRangeChanged = { dateRange ->
                                    viewModel.updateDateRange(dateRange)
                                },
                                viewModel = viewModel,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Recent Transactions Header
                        item {
                            Text(
                                text = "Recent Transactions",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                modifier = Modifier.padding(
                                    start = 16.dp,
                                    top = 24.dp,
                                    bottom = 16.dp
                                )
                            )
                        }

                        // Transaction List
                        if (uiState.recentTransactions.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No recent transactions",
                                        fontSize = 14.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        } else {
                            items(uiState.recentTransactions) { transaction ->
                                TransactionListItem(transaction = transaction)
                            }
                        }
                    }
                }
            }
        }
    }
}