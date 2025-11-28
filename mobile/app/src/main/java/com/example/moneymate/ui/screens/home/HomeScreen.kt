package com.example.moneymate.ui.screens.home

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.moneymate.R
import com.example.moneymate.ui.components.states.FullScreenError
import com.example.moneymate.ui.components.states.FullScreenLoading
import com.example.moneymate.ui.components.states.SectionStateManager
import com.example.moneymate.ui.navigation.BottomNavigationBar
import com.example.moneymate.utils.Config
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel(),
    onProfileClick: () -> Unit = {},
    onAddRecord: () -> Unit = {},
    onAddWallet: () -> Unit = {},
    onSeeAllBudget: () -> Unit = {},
    onSeeAllTransactions: () -> Unit = {},
    currentScreen: String = "home",
    onNavigationItemSelected: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Handle errors via Toast
    LaunchedEffect(uiState.userDataState) {
        if (uiState.userDataState is com.example.moneymate.utils.ScreenState.Error) {
            val error = (uiState.userDataState as com.example.moneymate.utils.ScreenState.Error).error
            Toast.makeText(context, error.getUserFriendlyMessage(), Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(uiState.balanceState) {
        if (uiState.balanceState is com.example.moneymate.utils.ScreenState.Error) {
            val error = (uiState.balanceState as com.example.moneymate.utils.ScreenState.Error).error
            Toast.makeText(context, error.getUserFriendlyMessage(), Toast.LENGTH_LONG).show()
        }
    }

    // Main content with state management
    when {
        // Show full screen loading if both user data and balance are loading
        uiState.userDataState is com.example.moneymate.utils.ScreenState.Loading &&
                uiState.balanceState is com.example.moneymate.utils.ScreenState.Loading -> {
            FullScreenLoading(message = "Loading home data...")
        }
        // Show full screen error if user data failed to load (critical data)
        uiState.userDataState is com.example.moneymate.utils.ScreenState.Error -> {
            FullScreenError(
                error = (uiState.userDataState as com.example.moneymate.utils.ScreenState.Error).error,
                onRetry = { viewModel.loadAllData() }
            )
        }
        // Show normal content
        else -> {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .statusBarsPadding()
                ) {
                    // User data section with state management
                    SectionStateManager(
                        state = uiState.userDataState,
                        onRetry = { viewModel.loadUserData() }
                    ) { userData ->
                        TopAppBarSection(
                            userName = userData.user.fullName ?: "User",
                            profileImage = userData.user.avatarUrl?.let { avatarUrl ->
                                Config.buildImageUrl(avatarUrl)
                            },
                            onProfileClick = onProfileClick
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Balance section with state management
                        SectionStateManager(
                            state = uiState.balanceState,
                            onRetry = { viewModel.loadTotalBalance() }
                        ) { balance ->
                            WalletBalanceCard(
                                totalBalance = balance?.totalBalance,
                                isLoading = uiState.isTotalBalanceLoading
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        FinancialOverviewCard(
                            expenseCount = userData.stats.expenseCount,
                            incomeCount = userData.stats.incomeCount
                        )

                        // Only show FirstLoginContent if there are no wallets AND no transactions
                        if (userData.stats.walletCount == 0 && userData.stats.totalTransactions == 0) {
                            FirstLoginContent(
                                onAddRecord = onAddRecord,
                                onAddWallet = onAddWallet
                            )
                        } else {
                            RegularHomeContent(
                                stats = userData.stats,
                                onSeeAllBudget = onSeeAllBudget,
                                onSeeAllTransactions = onSeeAllTransactions
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(80.dp))
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

@Preview
@Composable
fun HomeScreenPreview() {
    MaterialTheme {
        HomeScreen()
    }
}