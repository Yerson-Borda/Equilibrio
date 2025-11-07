package com.example.data.network.home.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@kotlinx.serialization.Serializable
data class HomeResponse(
    @SerialName("total_balance")
    val totalBalance: Double,
    @SerialName("saved_amount")
    val savedAmount: Double,
    @SerialName("spent_amount")
    val spentAmount: Double,
    @SerialName("budget_remaining")
    val budgetRemaining: Double?,
    @SerialName("budget_status")
    val budgetStatus: String?,
    @SerialName("savings_goals")
    val savingsGoals: List<SavingsGoalResponse>,
    @SerialName("recent_transactions")
    val recentTransactions: List<TransactionResponse>,
    @SerialName("has_wallets")
    val hasWallets: Boolean
)

@kotlinx.serialization.Serializable
data class SavingsGoalResponse(
    @SerialName("id")
    val id: String,
    @SerialName("name")
    val name: String,
    @SerialName("target_amount")
    val targetAmount: Double,
    @SerialName("current_amount")
    val currentAmount: Double
)

@Serializable
data class TransactionResponse(
    @SerialName("id")
    val id: String,
    @SerialName("merchant")
    val merchant: String,
    @SerialName("category")
    val category: String,
    @SerialName("amount")
    val amount: Double,
    @SerialName("date")
    val date: String,
    @SerialName("type")
    val type: String // "income" or "expense"
)