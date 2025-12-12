package com.example.data.network.budget

import com.example.data.network.budget.model.BudgetResponse
import com.example.domain.budget.model.Budget

object BudgetMapper {
    fun toDomain(response: BudgetResponse): Budget {
        return Budget(
            id = response.id,
            month = response.month,
            year = response.year,
            monthlyLimit = response.monthlyLimit.toDoubleOrNull() ?: 0.0,
            dailyLimit = response.dailyLimit.toDoubleOrNull() ?: 0.0,
            monthlySpent = response.monthlySpent.toDoubleOrNull() ?: 0.0,
            dailySpent = response.dailySpent.toDoubleOrNull() ?: 0.0,
            lastUpdatedDate = response.lastUpdatedDate,
            createdAt = response.createdAt
        )
    }
}