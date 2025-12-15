package com.example.domain.budget.usecase

import com.example.domain.budget.BudgetRepository
import com.example.domain.budget.model.Budget

class UpdateBudgetUseCase(
    private val repository: BudgetRepository
) {
    suspend operator fun invoke(monthlyLimit: Double?, dailyLimit: Double?): Result<Budget> {
        return repository.updateCurrentBudget(monthlyLimit, dailyLimit)
    }
}