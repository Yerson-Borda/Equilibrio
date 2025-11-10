package com.example.domain.wallet.model

data class WalletUpdateRequest(
    val name: String,
    val currency: String,
    val walletType: String,
    val initialBalance: Double,
    val cardNumber: String? = null,
    val color: String
)