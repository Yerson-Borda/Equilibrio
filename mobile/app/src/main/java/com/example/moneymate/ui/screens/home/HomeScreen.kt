package com.example.moneymate.ui.screens.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.moneymate.R
import com.example.moneymate.ui.navigation.BottomNavigationBar

@Composable
fun HomeScreen(
    isFirstLogin: Boolean = true,
    userName: String = "User",
    profileImage: String? = null,
    onProfileClick: () -> Unit = {},
    onAddRecord: () -> Unit = {},
    onAddWallet: () -> Unit = {},
    onSeeAllBudget: () -> Unit = {},
    onSeeAllTransactions: () -> Unit = {},
    currentScreen: String = "home", // Add this parameter
    onNavigationItemSelected: (String) -> Unit = {} // Add this parameter
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            TopAppBarSection(
                userName = userName,
                profileImage = profileImage,
                onProfileClick = onProfileClick
            )

            Spacer(modifier = Modifier.height(14.dp))
            WalletBalanceCard()
            Spacer(modifier = Modifier.height(16.dp))
            FinancialOverviewCard()
            if (isFirstLogin) {
                FirstLoginContent(
                    onAddRecord = onAddRecord,
                    onAddWallet = onAddWallet
                )
            } else {
                RegularHomeContent(
                    onSeeAllBudget = onSeeAllBudget,
                    onSeeAllTransactions = onSeeAllTransactions
                )
            }

            Spacer(modifier = Modifier.height(80.dp)) // Extra space for bottom nav
        }

        // Add Record Button - positioned above bottom navigation
        AddRecordButton(
            onClick = onAddRecord,
            iconRes = R.drawable.add_outline,
            contentDescription = "Add Record",
            size = 48,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 110.dp) // Position above bottom nav
        )

        // Bottom Navigation Bar - directly in HomeScreen for testing
        BottomNavigationBar(
            currentScreen = currentScreen,
            onNavigationItemSelected = onNavigationItemSelected,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenFirstLoginPreview() {
    MaterialTheme {
        HomeScreen(
            isFirstLogin = true,
            userName = "John Doe",
            currentScreen = "home",
            onNavigationItemSelected = { route ->
                println("Navigating to: $route")
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenRegularPreview() {
    MaterialTheme {
        HomeScreen(
            isFirstLogin = false,
            userName = "Sarah Smith",
            currentScreen = "home",
            onNavigationItemSelected = { route ->
                println("Navigating to: $route")
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenTransactionsPreview() {
    MaterialTheme {
        HomeScreen(
            isFirstLogin = false,
            userName = "Mike Johnson",
            currentScreen = "transactions",
            onNavigationItemSelected = { route ->
                println("Navigating to: $route")
            }
        )
    }
}