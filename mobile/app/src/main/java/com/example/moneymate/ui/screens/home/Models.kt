package com.example.moneymate.ui.screens.home

data class SavingsItem(
    val name: String,
    val targetAmount: Double
)

data class Transaction(
    val merchant: String,
    val category: String,
    val amount: Double, val date: String
)