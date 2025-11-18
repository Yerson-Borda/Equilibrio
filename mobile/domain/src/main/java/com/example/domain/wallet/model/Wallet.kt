package com.example.domain.wallet.model

data class Wallet(
    val id: Int,
    val name: String,
    val currency: String,
    val walletType: String,
    val initialBalance: String, // Change from Double to String
    val cardNumber: String?,
    val color: String,
    val balance: String?,
    val userId: Int?,
    val createdAt: String?
)