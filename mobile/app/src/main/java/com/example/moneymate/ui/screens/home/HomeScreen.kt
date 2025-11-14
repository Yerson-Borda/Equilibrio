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
import androidx.compose.material3.CircularProgressIndicator
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
    val userData by viewModel.userData.collectAsState()
    val totalBalance by viewModel.totalBalance.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isTotalBalanceLoading by viewModel.isTotalBalanceLoading.collectAsState()
    val scrollState = rememberScrollState()

    // Handle errors
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.error.collect { error ->
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
        }
    }

    if (isLoading && userData == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .statusBarsPadding()
        ) {
            TopAppBarSection(
                userName = userData?.user?.fullName ?: "User",
                profileImage = userData?.user?.avatarUrl?.let { avatarUrl ->
                    // Build the full URL using your Config
                    Config.buildImageUrl(avatarUrl)
                },
                onProfileClick = onProfileClick
            )

            Spacer(modifier = Modifier.height(14.dp))

            userData?.let { data ->
                // ALWAYS show these cards, even when walletCount is 0
                WalletBalanceCard(
                    totalBalance = totalBalance?.totalBalance,
                    isLoading = isTotalBalanceLoading
                )
                Spacer(modifier = Modifier.height(16.dp))
                FinancialOverviewCard(
                    expenseCount = data.stats.expenseCount,
                    incomeCount = data.stats.incomeCount
                )

                // Only show FirstLoginContent if there are no wallets AND no transactions
                if (data.stats.walletCount == 0 && data.stats.totalTransactions == 0) {
                    FirstLoginContent(
                        onAddRecord = onAddRecord,
                        onAddWallet = onAddWallet
                    )
                } else {
                    RegularHomeContent(
                        stats = data.stats,
                        onSeeAllBudget = onSeeAllBudget,
                        onSeeAllTransactions = onSeeAllTransactions
                    )
                }
            } ?: run {
                // Show empty state when no data at all
                FirstLoginContent(
                    onAddRecord = onAddRecord,
                    onAddWallet = onAddWallet
                )
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

//@Preview(showBackground = true)
//@Composable
//fun HomeScreenFirstLoginPreview() {
//    MaterialTheme {
//        HomeScreen(
//            isFirstLogin = true,
//            userName = "John Doe",
//            currentScreen = "home",
//            onNavigationItemSelected = { route ->
//                println("Navigating to: $route")
//            }
//        )
//    }
//}
//
//@Preview(showBackground = true)
//@Composable
//fun HomeScreenRegularPreview() {
//    MaterialTheme {
//        HomeScreen(
//            isFirstLogin = false,
//            userName = "Sarah Smith",
//            currentScreen = "home",
//            onNavigationItemSelected = { route ->
//                println("Navigating to: $route")
//            }
//        )
//    }
//}
//
//@Preview(showBackground = true)
//@Composable
//fun HomeScreenTransactionsPreview() {
//    MaterialTheme {
//        HomeScreen(
//            isFirstLogin = false,
//            userName = "Mike Johnson",
//            currentScreen = "transactions",
//            onNavigationItemSelected = { route ->
//                println("Navigating to: $route")
//            }
//        )
//    }
//}