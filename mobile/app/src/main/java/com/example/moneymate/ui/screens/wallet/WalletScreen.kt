package com.example.moneymate.ui.screens.wallet

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.domain.transaction.model.TransactionEntity
import com.example.domain.wallet.model.Wallet
import com.example.moneymate.R
import com.example.moneymate.ui.components.TransactionsSection
import com.example.moneymate.ui.components.states.FullScreenError
import com.example.moneymate.ui.components.states.FullScreenLoading
import com.example.moneymate.ui.components.states.SectionStateManager
import com.example.moneymate.ui.navigation.BottomNavigationBar
import com.example.moneymate.ui.screens.home.AddRecordButton
import com.example.moneymate.utils.ScreenState
import org.koin.androidx.compose.koinViewModel

@Composable
fun WalletScreen(
    onNavigateToWalletCreation: () -> Unit,
    onNavigateToWalletDetail: (Int) -> Unit,
    viewModel: WalletViewModel = koinViewModel(),
    currentScreen: String = "home",
    onNavigationItemSelected: (String) -> Unit = {},
    onBackClick: () -> Unit,
    onAddRecord: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Handle operation success states
    LaunchedEffect(uiState.createWalletState) {
        if (uiState.createWalletState is ScreenState.Success) {
            viewModel.resetCreateWalletState()
        }
    }

    // Extract unique tags from all transactions
    val availableTags = remember(uiState.transactionsState) {
        when (val state = uiState.transactionsState) {
            is ScreenState.Success -> {
                state.data.flatMap { it.tags }.distinct().sorted()
            }
            else -> emptyList()
        }
    }

    // Main content with state management
    when {
        // Show full screen loading if wallets are loading
        uiState.walletsState is ScreenState.Loading -> {
            FullScreenLoading(message = "Loading wallets...")
        }
        // Show full screen error if wallets failed to load
        uiState.walletsState is ScreenState.Error -> {
            FullScreenError(
                error = (uiState.walletsState as ScreenState.Error).error,
                onRetry = { viewModel.loadWallets() }
            )
        }
        // Show normal content
        else -> {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF8F9FA))
                        .padding(16.dp)
                        .statusBarsPadding()
                ) {
                    // Top bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_back_arrow),
                                contentDescription = "Wallets",
                                tint = Color.Black,
                                modifier = Modifier.size(21.dp)
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
                    Spacer(modifier = Modifier.height(24.dp))

                    // Wallets Cards Section with state management
                    SectionStateManager(
                        state = uiState.walletsState,
                        onRetry = { viewModel.loadWallets() }
                    ) { wallets ->
                        WalletsCardsSection(
                            wallets = wallets,
                            selectedWallet = uiState.selectedWallet,
                            onWalletSelected = viewModel::selectWallet,
                            onWalletDetail = { wallet ->
                                wallet.id?.let { walletId ->
                                    onNavigateToWalletDetail(walletId)
                                }
                            },
                            onCreateNewWallet = onNavigateToWalletCreation,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Transactions Section with state management
                    SectionStateManager(
                        state = uiState.transactionsState,
                        onRetry = {
                            uiState.selectedWallet?.id?.let { walletId ->
                                viewModel.loadTransactions(walletId)
                            }
                        }
                    ) { transactions ->
                        TransactionsSection(
                            transactions = transactions,
                            availableTags = availableTags,
                            modifier = Modifier.weight(1f)
                        )
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
@Composable
private fun WalletsCardsSection(
    wallets: List<Wallet>,
    selectedWallet: Wallet?,
    onWalletSelected: (Wallet) -> Unit,
    onWalletDetail: (Wallet) -> Unit,
    onCreateNewWallet: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Add Wallet Card (always first)
            item {
                AddWalletCard(onClick = onCreateNewWallet)
            }

            // Wallet Cards
            items(wallets) { wallet ->
                WalletCardItem(
                    wallet = wallet,
                    isSelected = wallet.id == selectedWallet?.id,
                    onSelected = {
                        onWalletSelected(wallet)
                        onWalletDetail(wallet)
                    }
                )
            }
        }
    }
}

@Composable
private fun AddWalletCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .height(100.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Wallet",
                tint = Color(0xFF4D6BFA),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Add Wallet",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF4D6BFA)
            )
        }
    }
}

@Composable
private fun WalletCardItem(
    wallet: Wallet,
    isSelected: Boolean,
    onSelected: () -> Unit
) {
    // Safe color parsing with fallback
    val walletColor = try {
        Color(android.graphics.Color.parseColor(wallet.color))
    } catch (e: Exception) {
        Color(0xFF4D6BFA) // Fallback color
    }

    Card(
        modifier = Modifier
            .width(160.dp)
            .height(100.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 2.dp
        ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = walletColor.copy(alpha = 0.1f)
        ),
        border = if (isSelected) BorderStroke(2.dp, Color(0xFF4D6BFA)) else null,
        onClick = onSelected
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = when (wallet.walletType) {
                        "debit_card" -> "VISA"
                        "credit_card" -> "VISA"
                        else -> wallet.walletType.replace("_", " ").uppercase()
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF666666),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "$${wallet.balance ?: "0.00"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF1A1A1A),
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = formatCardNumber(wallet.cardNumber ?: ""),
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF1A1A1A),
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = wallet.name,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF666666)
                )
                Text(
                    text = "09/25", // You can make this dynamic
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF666666)
                )
            }
        }
    }
}