package com.example.domain.budget.model

data class Budget(
    val id: Int,
    val month: Int,
    val year: Int,
    val monthlyLimit: Double,
    val dailyLimit: Double,
    val monthlySpent: Double,
    val dailySpent: Double,
    val lastUpdatedDate: String,
    val createdAt: String
) {
    val monthlyRemaining: Double
        get() = monthlyLimit - monthlySpent

    val dailyRemaining: Double
        get() = dailyLimit - dailySpent

    val monthlyProgress: Float
        get() = if (monthlyLimit > 0) (monthlySpent / monthlyLimit).toFloat() else 0f

    val dailyProgress: Float
        get() = if (dailyLimit > 0) (dailySpent / dailyLimit).toFloat() else 0f

    val isMonthlyExceeded: Boolean
        get() = monthlySpent > monthlyLimit

    val isDailyExceeded: Boolean
        get() = dailySpent > dailyLimit
}