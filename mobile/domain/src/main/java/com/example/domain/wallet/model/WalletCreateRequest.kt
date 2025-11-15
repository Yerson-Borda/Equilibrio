package com.example.domain.wallet.model

data class WalletCreateRequest(
    val name: String,
    val currency: String = "USD",
    val walletType: String,
    val initialBalance: Double = 0.0,
    val cardNumber: String? = null,
    val color: String = "#3B82F6"
)