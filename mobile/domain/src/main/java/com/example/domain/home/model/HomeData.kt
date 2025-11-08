package com.example.domain.home.model

data class UserDetailedData(
    val user: UserData,
    val stats: StatsData
)

data class UserData(
    val id: String,
    val email: String,
    val fullName: String,
    val phoneNumber: String?,
    val dateOfBirth: String?,
    val avatarUrl: String?,
    val defaultCurrency: String,
    val createdAt: String
)

data class StatsData(
    val walletCount: Int,
    val totalTransactions: Int,
    val expenseCount: Int,
    val incomeCount: Int
)