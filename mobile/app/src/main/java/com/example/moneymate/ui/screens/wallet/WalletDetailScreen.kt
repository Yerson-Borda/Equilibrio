package com.example.moneymate.ui.screens.wallet

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.wallet.model.Wallet
import com.example.moneymate.R
import org.koin.androidx.compose.koinViewModel

@Composable
fun WalletDetailScreen(
    walletId: Int,
    onBackClick: () -> Unit,
    onEditWallet: (Int) -> Unit,
    viewModel: WalletViewModel = koinViewModel()
) {
    val walletDetail by viewModel.walletDetail.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val showDeleteDialog by viewModel.showDeleteDialog.collectAsState()
    val walletDeleted by viewModel.walletDeleted.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val (income, expense) = remember(transactions, walletId) {
        calculateIncomeExpense(transactions.filter { it.walletId == walletId })
    }
    // Add debug logging
    LaunchedEffect(walletId) {
        println("DEBUG: Loading wallet detail for ID: $walletId")
        viewModel.loadWalletDetail(walletId)
        viewModel.loadTransactions(walletId)
    }

    LaunchedEffect(walletDetail) {
        println("DEBUG: Wallet detail updated: $walletDetail")
    }

    LaunchedEffect(error) {
        if (error != null) {
            println("DEBUG: Error loading wallet: $error")
        }
    }

    LaunchedEffect(walletDeleted) {
        if (walletDeleted) {
            viewModel.resetWalletDeleted()
            onBackClick() // Navigate back to wallets list
        }
    }

    Scaffold(
        topBar = {
            WalletDetailTopBar(
                onBackClick = onBackClick
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF8F9FA))
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFF4D6BFA)
                )
            } else if (walletDetail != null) {
                WalletDetailContent(
                    walletDetail = walletDetail!!,
                    onEditWallet = onEditWallet,
                    onDeleteWallet = { viewModel.showDeleteConfirmation() },
                    income = income,
                    expense = expense,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                )
            } else if (error != null) {
                // Show error state
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Error loading wallet",
                        color = Color.Red,
                        modifier = Modifier.padding(16.dp)
                    )
                    Button(
                        onClick = { viewModel.loadWalletDetail(walletId) }
                    ) {
                        Text("Retry")
                    }
                }
            } else {
                // Show empty state (no wallet data and no error)
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No wallet data found",
                        color = Color.Gray,
                        modifier = Modifier.padding(16.dp)
                    )
                    Button(
                        onClick = { viewModel.loadWalletDetail(walletId) }
                    ) {
                        Text("Retry")
                    }
                }
            }

            if (showDeleteDialog) {
                DeleteConfirmationDialog(
                    onConfirm = { viewModel.deleteWallet(walletId) },
                    onDismiss = { viewModel.dismissDeleteConfirmation() }
                )
            }
        }
    }
}

@Composable
private fun WalletDetailTopBar(
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .statusBarsPadding(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.Black
            )
        }
        Spacer(modifier = Modifier.width(105.dp))
        Text(
            text = "Wallets",
            color = Color.Black,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun WalletDetailContent(
    walletDetail: Wallet,
    income: Double,
    expense: Double,
    onDeleteWallet: () -> Unit,
    onEditWallet: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Wallet Card
        WalletCard(walletDetail = walletDetail)

        // Wallet Info Card with income/expense
        WalletInfoCard(
            walletDetail = walletDetail,
            income = income,
            expense = expense,
            onEditWallet = onEditWallet
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Delete Button
        Button(
            onClick = onDeleteWallet,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFEF4444)
            )
        ) {
            Text("Delete")
        }

        // Add extra space at the bottom for better scrolling
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun WalletCard(walletDetail: Wallet) {
    // Use balance if available, otherwise use initialBalance
    val displayBalance = walletDetail.balance ?: walletDetail.initialBalance

    // Safe color parsing with fallback
    val backgroundColor = try {
        Color(android.graphics.Color.parseColor(walletDetail.color))
    } catch (e: Exception) {
        // Fallback to default color if parsing fails
        Color(0xFF4D6BFA) // Your app's primary color
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Total Balance",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )
            Text(
                text = "$$displayBalance",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Show card type based on walletType
            Text(
                text = getCardTypeDisplay(walletDetail.walletType),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Show card number if available, otherwise show placeholder
            val cardDisplay = if (!walletDetail.cardNumber.isNullOrEmpty()) {
                formatCardNumber(walletDetail.cardNumber)
            } else {
                "****  ****  ****  ****"
            }

            Text(
                text = cardDisplay,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = walletDetail.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
                // For non-card wallets, you might want to show different info
                Text(
                    text = getExpiryDisplay(walletDetail.walletType),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun WalletInfoCard(
    walletDetail: Wallet,
    income: Double,
    expense: Double,
    onEditWallet: (Int) -> Unit
) {
    val displayBalance = walletDetail.balance ?: walletDetail.initialBalance

    // Safe color parsing with fallback
    val walletColor = try {
        Color(android.graphics.Color.parseColor(walletDetail.color))
    } catch (e: Exception) {
        Color(0xFF4D6BFA) // Fallback color
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = walletColor.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Your Balance",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF666666)
                )
                IconButton(
                    onClick = { walletDetail.id?.let { onEditWallet(it) } },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_edit),
                        contentDescription = "Edit wallet",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Text(
                text = "$$displayBalance",
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFF1A1A1A),
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Income and Expense Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Income
                IncomeExpenseItem(
                    amount = income,
                    isIncome = true,
                    modifier = Modifier.weight(1f)
                )

                // Expense
                IncomeExpenseItem(
                    amount = expense,
                    isIncome = false,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Currency Info
            InfoRow(
                title = "Currency",
                value = "${walletDetail.currency} / ${getCurrencyName(walletDetail.currency)}"
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Type Info
            InfoRow(
                title = "Type",
                value = formatWalletType(walletDetail.walletType)
            )
        }
    }
}

@Composable
private fun IncomeExpenseItem(
    amount: Double,
    isIncome: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(
                    if (isIncome) com.example.domain.R.drawable.up else com.example.domain.R.drawable.down
                ),
                contentDescription = if (isIncome) "Income" else "Expense",
                tint = if (isIncome) Color(0xFF10B981) else Color(0xFFEF4444),
                modifier = Modifier.size(16.dp)
            )
        }
        Text(
            text = "$${String.format("%.2f", amount)}",
            style = MaterialTheme.typography.bodyMedium,
            color = if (isIncome) Color(0xFF10B981) else Color(0xFFEF4444),
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun InfoRow(title: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF666666)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF1A1A1A),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun DeleteConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Wallet") },
        text = { Text("The wallet will be deleted with all the records and related objects") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEF4444)
                )
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6B7280)
                )
            ) {
                Text("Cancel")
            }
        }
    )
}