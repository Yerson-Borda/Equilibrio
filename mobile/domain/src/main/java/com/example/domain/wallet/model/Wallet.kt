package com.example.domain.wallet.model

data class Wallet(
    val id: Int,
    val name: String,
    val currency: String = "USD",
    val walletType: String, // "debit_card", "credit_card", "cash", "digital"
    val initialBalance: String = "0.00",
    val cardNumber: String? = null,
    val color: String = "#4D6BFA",
    val balance: String? = null,
    val userId: Int? = null,
    val createdAt: String? = null
)