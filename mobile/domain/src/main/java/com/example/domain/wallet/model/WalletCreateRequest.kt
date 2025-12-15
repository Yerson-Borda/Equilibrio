package com.example.domain.wallet.model

data class WalletCreateRequest(
    val name: String,
    val currency: String = "USD",
    val walletType: String, // Make sure this matches wallet_type in API
    val initialBalance: Double = 0.0, // This will be mapped to "balance" in API
    val cardNumber: String? = null,
    val color: String = "#3B82F6"
)