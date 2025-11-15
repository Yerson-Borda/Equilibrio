package com.example.moneymate.ui.screens.wallet

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.wallet.model.WalletCreateRequest
import org.koin.androidx.compose.koinViewModel

@Composable
fun CreateWalletScreen(
    onBackClick: () -> Unit,
    viewModel: WalletViewModel = koinViewModel()
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val walletCreated by viewModel.walletCreated.collectAsState()
    val error by viewModel.error.collectAsState()

    // Navigate back when wallet is successfully created
    LaunchedEffect(walletCreated) {
        if (walletCreated) {
            viewModel.resetWalletCreated()
            onBackClick()
        }
    }

    Scaffold(
        topBar = {
            WalletTopBar(
                title = "Create Wallet",
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
            } else {
                CreateWalletFormContent(
                    onCreateWallet = viewModel::createWallet,
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
private fun CreateWalletFormContent(
    onCreateWallet: (WalletCreateRequest) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var walletName by remember { mutableStateOf("") }
    var initialBalance by remember { mutableStateOf("") }
    var selectedWalletType by remember { mutableStateOf("debit_card") }
    var cardNumber by remember { mutableStateOf("") }
    var selectedCurrency by remember { mutableStateOf("USD") }
    var selectedColor by remember { mutableStateOf("#4D6BFA") }

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
            val request = WalletCreateRequest(
                name = walletName.ifEmpty { "My Wallet" },
                currency = selectedCurrency,
                walletType = selectedWalletType,
                initialBalance = initialBalance.toDoubleOrNull() ?: 0.0,
                cardNumber = cardNumber,
                color = selectedColor
            )
            onCreateWallet(request)
        },
        onCancel = onCancel,
        submitButtonText = "Add Wallet",
        isSubmitEnabled = walletName.isNotEmpty(),
        modifier = modifier
    )
}