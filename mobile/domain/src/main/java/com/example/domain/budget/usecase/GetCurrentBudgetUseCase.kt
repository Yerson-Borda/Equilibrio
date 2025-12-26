package com.example.domain.budget.usecase

import com.example.domain.budget.BudgetRepository
import com.example.domain.budget.model.Budget

class GetCurrentBudgetUseCase(
    private val repository: BudgetRepository
) {
    suspend operator fun invoke(): Result<Budget> {
        return repository.getCurrentBudget()
    }
}