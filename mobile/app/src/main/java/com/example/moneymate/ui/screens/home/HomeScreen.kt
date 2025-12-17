package com.example.moneymate.ui.screens.home

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.domain.user.model.UserDetailedData
import com.example.moneymate.R
import com.example.moneymate.ui.components.states.FullScreenError
import com.example.moneymate.ui.components.states.FullScreenLoading
import com.example.moneymate.ui.components.states.SectionStateManager
import com.example.moneymate.ui.navigation.BottomNavigationBar
import com.example.moneymate.utils.Config
import com.example.moneymate.utils.ScreenState
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel(),
    onProfileClick: () -> Unit = {},
    onAddRecord: () -> Unit = {},
    onSeeAllBudget: () -> Unit = {},
    onSeeAllTransactions: () -> Unit = {},
    currentScreen: String = "home",
    onNavigationItemSelected: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    println("🔄 DEBUG: HomeScreen - Screen resumed, refreshing data...")
                    viewModel.refreshOnScreenFocus()
                }
                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val currencySymbol = remember(uiState.userDataState) {
        when (uiState.userDataState) {
            is ScreenState.Success -> {
                extractCurrencySymbol((uiState.userDataState as ScreenState.Success<UserDetailedData>).data.user.defaultCurrency)
            }
            else -> "$"
        }
    }

    LaunchedEffect(uiState.userDataState) {
        if (uiState.userDataState is ScreenState.Error) {
            val error = (uiState.userDataState as ScreenState.Error).error
            Toast.makeText(context, error.getUserFriendlyMessage(), Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(uiState.balanceState) {
        if (uiState.balanceState is ScreenState.Error) {
            val error = (uiState.balanceState as ScreenState.Error).error
            Toast.makeText(context, error.getUserFriendlyMessage(), Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(uiState.budgetState) {
        if (uiState.budgetState is ScreenState.Error) {
            val error = (uiState.budgetState as ScreenState.Error).error
            Toast.makeText(context, error.getUserFriendlyMessage(), Toast.LENGTH_LONG).show()
        }
    }

    when {
        uiState.userDataState is ScreenState.Loading -> {
            FullScreenLoading(message = "Loading home data...")
        }
        uiState.userDataState is ScreenState.Error -> {
            FullScreenError(
                error = (uiState.userDataState as ScreenState.Error).error,
                onRetry = { viewModel.loadAllData() }
            )
        }
        else -> {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                        .statusBarsPadding()
                ) {
                    item {
                        SectionStateManager(
                            state = uiState.userDataState,
                            onRetry = { viewModel.loadUserData() }
                        ) { userData ->
                            Column {
                                TopAppBarSection(
                                    userName = userData.user.fullName ?: "User",
                                    profileImage = userData.user.avatarUrl?.let { avatarUrl ->
                                        Config.buildImageUrl(avatarUrl)
                                    },
                                    onProfileClick = onProfileClick
                                )

                                Spacer(modifier = Modifier.height(14.dp))
                                SectionStateManager(
                                    state = uiState.balanceState,
                                    onRetry = { viewModel.loadTotalBalance() }
                                ) { balance ->
                                    WalletBalanceCard(
                                        totalBalance = balance?.totalBalance,
                                        currencySymbol = currencySymbol,
                                        isLoading = uiState.isTotalBalanceLoading
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Financial Overview section with state management
                                SectionStateManager(
                                    state = uiState.financialOverviewState,
                                    onRetry = { viewModel.loadFinancialOverview() }
                                ) { financialData ->
                                    FinancialOverviewCard(
                                        totalIncome = financialData.totalIncome,
                                        totalExpense = financialData.totalExpense,
                                        currencySymbol = currencySymbol
                                    )
                                }

                                if (userData.stats.walletCount == 0 && userData.stats.totalTransactions == 0) {
                                    FirstLoginContent()
                                } else {
                                    SectionStateManager(
                                        state = uiState.budgetState,
                                        onRetry = { viewModel.loadBudgetData() }
                                    ) { budgetData ->
                                        RegularHomeContent(
                                            recentTransactions = uiState.recentTransactions,
                                            budgetData = budgetData,
                                            currencySymbol = currencySymbol,
                                            onSeeAllBudget = onSeeAllBudget,
                                            onSeeAllTransactions = onSeeAllTransactions,
                                            isInLazyColumn = true
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }

                AddRecordButton(
                    onClick = onAddRecord,
                    iconRes = R.drawable.add_outline,
                    contentDescription = "Add Record",
                    size = 48,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 110.dp)
                )

                BottomNavigationBar(
                    currentScreen = currentScreen,
                    onNavigationItemSelected = onNavigationItemSelected,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}

private fun extractCurrencySymbol(currencyString: String?): String {
    if (currencyString?.contains(" - ") == true) {
        return currencyString.split(" - ").last().trim()
    }

    return when (currencyString?.uppercase()) {
        "USD" -> "$"
        "EUR" -> "€"
        "GBP" -> "£"
        "JPY" -> "¥"
        "CAD" -> "C$"
        "AUD" -> "A$"
        "CHF" -> "CHF"
        "CNY" -> "¥"
        "INR" -> "₹"
        "RUB" -> "₽"
        "BRL" -> "R$"
        "MXN" -> "$"
        "KRW" -> "₩"
        else -> "$"
    }
}

@Preview
@Composable
fun HomeScreenPreview() {
    MaterialTheme {
        HomeScreen()
    }
}