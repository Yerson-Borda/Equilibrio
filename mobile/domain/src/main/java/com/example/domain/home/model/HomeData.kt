package com.example.domain.home.model

data class HomeData(
    val totalBalance: Double,
    val savedAmount: Double,
    val spentAmount: Double,
    val budgetRemaining: Double?,
    val budgetStatus: String?,
    val savingsGoals: List<SavingsGoal>,
    val recentTransactions: List<Transaction>,
    val hasWallets: Boolean
)

data class SavingsGoal(
    val id: String,
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double
)

data class Transaction(
    val id: String,
    val merchant: String,
    val category: String,
    val amount: Double,
    val date: String,
    val type: String // "income" or "expense"
)