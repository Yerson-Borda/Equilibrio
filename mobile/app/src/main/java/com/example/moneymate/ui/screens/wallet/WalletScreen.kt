package com.example.moneymate.ui.screens.wallet

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
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
import com.example.moneymate.utils.CurrencyUtils.getCurrencySymbol
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
    val lifecycleOwner = LocalLifecycleOwner.current

    // Refresh data when screen comes into focus
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    println("🔄 DEBUG: WalletScreen - Screen resumed, refreshing data...")
                    viewModel.refreshOnScreenFocus()
                }
                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        // Clean up when the composable leaves the composition
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

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
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF8F9FA))
                        .statusBarsPadding()
                ) {
                    item {
                        Column(
                            modifier = Modifier.padding(16.dp)
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
                        }
                    }

                    item {
                        // Transactions Section - PASS isInLazyColumn = true
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
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                onSeeAll = { /* Handle see all if needed */ },
                                isInLazyColumn = true // PASS THIS
                            )
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
    Box(
        modifier = Modifier
            .width(36.dp)
            .height(170.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        // Draw the dashed border
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 1.dp.toPx()
            val dashLength = 4.dp.toPx()
            val gapLength = 2.dp.toPx()
            val cornerRadius = 10.dp.toPx()

            // Calculate the rectangle bounds
            val rect = androidx.compose.ui.geometry.Rect(
                left = strokeWidth / 2,
                top = strokeWidth / 2,
                right = size.width - strokeWidth / 2,
                bottom = size.height - strokeWidth / 2
            )

            // Create rounded rectangle path
            val path = androidx.compose.ui.graphics.Path().apply {
                addRoundRect(
                    RoundRect(
                        rect = rect,
                        cornerRadius = CornerRadius(cornerRadius, cornerRadius)
                    )
                )
            }

            // Draw dashed path
            drawPath(
                path = path,
                color = Color.Black,
                style = Stroke(
                    width = strokeWidth,
                    pathEffect = PathEffect.dashPathEffect(
                        floatArrayOf(dashLength, gapLength),
                        0f
                    )
                )
            )
        }

        // Blue plus icon
        Box(
            modifier = Modifier
                .size(18.dp)
                .background(
                    color = Color.Black,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Wallet",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
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
    val walletColor = try {
        Color(android.graphics.Color.parseColor(wallet.color))
    } catch (e: Exception) {
        Color(0xFF4D6BFA)
    }
    val currencySymbol = remember(wallet.currency) {
        getCurrencySymbol(wallet.currency)
    }

    val textColor = remember(walletColor) {
        val brightness = (walletColor.red * 299 + walletColor.green * 587 + walletColor.blue * 114) / 1000
        if (brightness > 0.5) Color.Black else Color.White
    }

    Card(
        modifier = Modifier
            .width(271.dp)
            .height(170.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        ),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        border = if (isSelected) BorderStroke(2.dp, Color(0xFF4D6BFA)) else null,
        onClick = onSelected
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(
                        color = walletColor,
                        shape = RoundedCornerShape(
                            topStart = 10.dp,
                            topEnd = 10.dp
                        )
                    )
                    .padding(12.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Total Balance",
                                style = MaterialTheme.typography.labelSmall,
                                color = textColor
                            )
                            Text(
                                text = "$currencySymbol${wallet.balance ?: "0.00"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = textColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = when (wallet.walletType) {
                                "debit_card" -> "VISA"
                                "credit_card" -> "VISA"
                                else -> wallet.walletType.replace("_", " ").uppercase()
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = textColor,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Text(
                        text = formatCardNumber(wallet.cardNumber ?: ""),
                        style = MaterialTheme.typography.bodySmall,
                        color = textColor,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.sp
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color.Black,
                        shape = RoundedCornerShape(
                            bottomStart = 10.dp,
                            bottomEnd = 10.dp
                        )
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Name",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF666666),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = wallet.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Column {
                        Text(
                            text = "Exp",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF666666),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "09/25",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}