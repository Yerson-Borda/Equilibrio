package com.example.domain.wallet.model
data class WalletUpdateRequest(
    val name: String,
    val currency: String,
    val walletType: String,
    val initialBalance: String, // Changed from Double to String
    val cardNumber: String? = null,
    val color: String
)