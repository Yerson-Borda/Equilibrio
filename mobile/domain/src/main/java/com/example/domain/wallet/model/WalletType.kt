package com.example.domain.wallet.model

enum class WalletType(val displayName: String) {
    DEBIT_CARD("Debit Card"),
    CREDIT_CARD("Credit Card"),
    CASH("Cash"),
    BANK_ACCOUNT("Bank Account")
}