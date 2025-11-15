package com.example.domain.user.model

data class UserDetailedData(
    val user: User,
    val stats: StatsData
)
data class StatsData(
    val walletCount: Int,
    val totalTransactions: Int,
    val expenseCount: Int,
    val incomeCount: Int
)