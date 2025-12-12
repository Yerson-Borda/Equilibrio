package com.example.domain.budget

import com.example.domain.budget.model.Budget

interface BudgetRepository {
    suspend fun getCurrentBudget(): Result<Budget>
    suspend fun updateCurrentBudget(monthlyLimit: Double?, dailyLimit: Double?): Result<Budget>
}