package com.example.moneymate.ui.screens.home

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
    val homeData by viewModel.homeData.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val scrollState = rememberScrollState()

    // Handle errors
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.error.collect { error ->
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
        }
    }

    if (isLoading && homeData == null) {
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
        ) {
            TopAppBarSection(
                userName = "User", // You might want to get this from user profile
                profileImage = null,
                onProfileClick = onProfileClick
            )

            Spacer(modifier = Modifier.height(14.dp))

            homeData?.let { data ->
                WalletBalanceCard(totalBalance = data.totalBalance)
                Spacer(modifier = Modifier.height(16.dp))
                FinancialOverviewCard(
                    savedAmount = data.savedAmount,
                    spentAmount = data.spentAmount
                )

                if (!data.hasWallets) {
                    FirstLoginContent(
                        onAddRecord = onAddRecord,
                        onAddWallet = onAddWallet
                    )
                } else {
                    RegularHomeContent(
                        homeData = data,
                        onSeeAllBudget = onSeeAllBudget,
                        onSeeAllTransactions = onSeeAllTransactions
                    )
                }
            } ?: run {
                // Show empty state or error
                EmptyHomeState(
                    onRetry = { viewModel.refreshHomeData() }
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