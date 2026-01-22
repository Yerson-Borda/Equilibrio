package com.example.domain.savingsGoal.usecase

import com.example.domain.savingsGoal.SavingsGoalRepository
import com.example.domain.savingsGoal.model.SavingsGoal

class GetCurrentSavingsGoalUseCase(
    private val repository: SavingsGoalRepository
) {
    suspend operator fun invoke(): Result<SavingsGoal> {
        return repository.getCurrentSavingsGoal()
    }
}