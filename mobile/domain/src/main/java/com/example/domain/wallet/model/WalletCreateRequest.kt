package com.example.domain.wallet.model

data class WalletCreateRequest(
    val name: String,
    val currency: String,
    val walletType: String,
    val initialBalance: String, // Change from Double to String
    val cardNumber: String?,
    val color: String
)