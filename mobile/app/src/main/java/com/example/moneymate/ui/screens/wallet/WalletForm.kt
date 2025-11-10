package com.example.moneymate.ui.screens.wallet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.moneymate.ui.components.wallet.CardNumberPreview
import com.example.moneymate.ui.components.wallet.ColorSelectionSection
import com.example.moneymate.ui.components.wallet.CurrencyDropdown
import com.example.moneymate.ui.components.wallet.TotalBalanceCard
import com.example.moneymate.ui.components.wallet.WalletTypeDropdown


@Composable
fun WalletForm(
    walletName: String,
    onWalletNameChange: (String) -> Unit,
    initialBalance: String,
    onInitialBalanceChange: (String) -> Unit,
    selectedWalletType: String,
    onWalletTypeSelected: (String) -> Unit,
    cardNumber: String,
    onCardNumberChange: (String) -> Unit,
    selectedCurrency: String,
    onCurrencySelected: (String) -> Unit,
    selectedColor: String,
    onColorSelected: (String) -> Unit,
    onSubmit: () -> Unit,
    onCancel: () -> Unit,
    submitButtonText: String,
    isSubmitEnabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .verticalScroll(scrollState),
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
                onValueChange = onWalletNameChange,
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
                onValueChange = onInitialBalanceChange,
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
                onCurrencySelected = onCurrencySelected,
                modifier = Modifier.fillMaxWidth()
            )

            // Card Number Field
            OutlinedTextField(
                value = cardNumber,
                onValueChange = onCardNumberChange,
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
                onTypeSelected = onWalletTypeSelected,
                modifier = Modifier.fillMaxWidth()
            )

            // Color Selection
            ColorSelectionSection(
                selectedColor = selectedColor,
                onColorSelected = onColorSelected,
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
                onClick = onSubmit,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4D6BFA)
                ),
                enabled = isSubmitEnabled
            ) {
                Text(submitButtonText)
            }
        }

        // Add extra space at the bottom for better scrolling
        Spacer(modifier = Modifier.height(16.dp))
    }
}