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
            CreateWalletTopBar(onBackClick = onBackClick)
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF8F9FA))

        ) {
            Column (

            ){

            }
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFF4D6BFA)
                )
            } else {
                CreateWalletForm(
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
private fun CreateWalletTopBar(onBackClick: () -> Unit) {
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
                tint = Color.Black,
                modifier = Modifier.size(21.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = "Create Wallet",
            color = Color.Black,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun CreateWalletForm(
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

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .verticalScroll(scrollState), // Make the column scrollable
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Total Balance Card
        TotalBalanceCard(
            initialBalance = initialBalance,
            modifier = Modifier.fillMaxWidth()
        )

        // Card Number Preview
        CardNumberPreview(
            cardNumber = cardNumber,
            modifier = Modifier.fillMaxWidth()
        )

        // Form Fields
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Name Field
            OutlinedTextField(
                value = walletName,
                onValueChange = { walletName = it },
                label = { Text("Name") },
                placeholder = { Text("Debit Card") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF4D6BFA),
                    focusedLabelColor = Color(0xFF4D6BFA)
                )
            )

            // Initial Balance Field
            OutlinedTextField(
                value = initialBalance,
                onValueChange = {
                    // Allow only numbers and decimal point
                    if (it.matches(Regex("^\\d*\\.?\\d*$"))) {
                        initialBalance = it
                    }
                },
                label = { Text("Initial balance") },
                placeholder = { Text("0.00") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF4D6BFA),
                    focusedLabelColor = Color(0xFF4D6BFA)
                )
            )

            // Currency Field (Dropdown)
            CurrencyDropdown(
                selectedCurrency = selectedCurrency,
                onCurrencySelected = { selectedCurrency = it },
                modifier = Modifier.fillMaxWidth()
            )

            // Card Number Field
            OutlinedTextField(
                value = cardNumber,
                onValueChange = {
                    // Allow only numbers and limit to 16 characters
                    val cleaned = it.filter { char -> char.isDigit() }.take(16)
                    cardNumber = cleaned
                },
                label = { Text("Card number") },
                placeholder = { Text("5485738137582321") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF4D6BFA),
                    focusedLabelColor = Color(0xFF4D6BFA)
                )
            )

            // Type Field (Dropdown)
            WalletTypeDropdown(
                selectedType = selectedWalletType,
                onTypeSelected = { selectedWalletType = it },
                modifier = Modifier.fillMaxWidth()
            )

            // Color Selection
            ColorSelectionSection(
                selectedColor = selectedColor,
                onColorSelected = { selectedColor = it },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF4D6BFA)
                ),
                border = ButtonDefaults.outlinedButtonBorder
            ) {
                Text("Cancel")
            }

            Button(
                onClick = {
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
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4D6BFA)
                ),
                enabled = walletName.isNotEmpty()
            ) {
                Text("Add Wallet")
            }
        }

        // Add extra space at the bottom for better scrolling
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun TotalBalanceCard(
    initialBalance: String,
    modifier: Modifier = Modifier
) {
    val displayBalance = if (initialBalance.isEmpty()) "0.00" else
        String.format("%.2f", initialBalance.toDoubleOrNull() ?: 0.0)

    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Total Balance",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF666666)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$$displayBalance",
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFF1A1A1A),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun CardNumberPreview(
    cardNumber: String,
    modifier: Modifier = Modifier
) {
    val displayNumber = if (cardNumber.isEmpty()) {
        "5485  7381  3758  2321"
    } else {
        cardNumber.chunked(4).joinToString("  ")
    }

    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF4D6BFA))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = displayNumber,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Card number",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WalletTypeDropdown(
    selectedType: String,
    onTypeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    val walletTypes = listOf(
        "debit_card" to "Debit Card",
        "credit_card" to "Credit Card",
        "cash" to "Cash",
        "bank_account" to "Bank Account"
    )

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = walletTypes.find { it.first == selectedType }?.second ?: "Debit Card",
            onValueChange = { },
            label = { Text("Type") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF4D6BFA),
                focusedLabelColor = Color(0xFF4D6BFA)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            readOnly = true
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            walletTypes.forEach { (type, displayName) ->
                DropdownMenuItem(
                    text = { Text(displayName) },
                    onClick = {
                        onTypeSelected(type)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CurrencyDropdown(
    selectedCurrency: String,
    onCurrencySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    val currencies = listOf(
        "USD" to "USD - US Dollar",
        "EUR" to "EUR - Euro",
        "GBP" to "GBP - British Pound",
        "RUB" to "RUB - Russian Ruble",
        "JPY" to "JPY - Japanese Yen"
    )

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = currencies.find { it.first == selectedCurrency }?.second ?: "USD - US Dollar",
            onValueChange = { },
            label = { Text("Currency") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF4D6BFA),
                focusedLabelColor = Color(0xFF4D6BFA)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            readOnly = true
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            currencies.forEach { (currency, displayName) ->
                DropdownMenuItem(
                    text = { Text(displayName) },
                    onClick = {
                        onCurrencySelected(currency)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ColorSelectionSection(
    selectedColor: String,
    onColorSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Color",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF1A1A1A),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        ColorSelectionGrid(
            selectedColor = selectedColor,
            onColorSelected = onColorSelected
        )
    }
}