package com.example.moneymate.ui.screens.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.domain.wallet.model.Wallet
import com.example.domain.wallet.model.WalletUpdateRequest
import org.koin.androidx.compose.koinViewModel

@Composable
fun EditWalletScreen(
    walletId: Int,
    onBackClick: () -> Unit,
    viewModel: WalletViewModel = koinViewModel()
) {
    val walletDetail by viewModel.walletDetail.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val walletUpdated by viewModel.walletUpdated.collectAsState()
    val error by viewModel.error.collectAsState()

    // Load wallet data when screen opens
    LaunchedEffect(walletId) {
        viewModel.loadWalletDetail(walletId)
    }

    // Navigate back when wallet is successfully updated
    LaunchedEffect(walletUpdated) {
        if (walletUpdated) {
            viewModel.resetWalletUpdated()
            onBackClick()
        }
    }

    Scaffold(
        topBar = {
            WalletTopBar(
                title = "Edit Wallet",
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
                EditWalletFormContent(
                    walletDetail = walletDetail!!,
                    onUpdateWallet = viewModel::updateWallet,
                    onCancel = onBackClick,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                )
            }
        }
    }
}

@Composable
private fun EditWalletFormContent(
    walletDetail: Wallet,
    onUpdateWallet: (Int, WalletUpdateRequest) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var walletName by remember { mutableStateOf(walletDetail.name) }
    var initialBalance by remember { mutableStateOf(walletDetail.initialBalance) }
    var selectedWalletType by remember { mutableStateOf(walletDetail.walletType) }
    var cardNumber by remember { mutableStateOf(walletDetail.cardNumber ?: "") }
    var selectedCurrency by remember { mutableStateOf(walletDetail.currency) }
    var selectedColor by remember { mutableStateOf(walletDetail.color) }

    WalletForm(
        walletName = walletName,
        onWalletNameChange = { walletName = it },
        initialBalance = initialBalance,
        onInitialBalanceChange = {
            // Allow only numbers and decimal point
            if (it.matches(Regex("^\\d*\\.?\\d*$"))) {
                initialBalance = it
            }
        },
        selectedWalletType = selectedWalletType,
        onWalletTypeSelected = { selectedWalletType = it },
        cardNumber = cardNumber,
        onCardNumberChange = {
            // Allow only numbers and limit to 16 characters
            val cleaned = it.filter { char -> char.isDigit() }.take(16)
            cardNumber = cleaned
        },
        selectedCurrency = selectedCurrency,
        onCurrencySelected = { selectedCurrency = it },
        selectedColor = selectedColor,
        onColorSelected = { selectedColor = it },
        onSubmit = {
            val request = WalletUpdateRequest(
                name = walletName.ifEmpty { "My Wallet" },
                currency = selectedCurrency,
                walletType = selectedWalletType,
                initialBalance = initialBalance.toDoubleOrNull() ?: 0.0,
                cardNumber = cardNumber.ifEmpty { null },
                color = selectedColor
            )
            onUpdateWallet(walletDetail.id, request)
        },
        onCancel = onCancel,
        submitButtonText = "Update Wallet",
        isSubmitEnabled = walletName.isNotEmpty(),
        modifier = modifier
    )
}