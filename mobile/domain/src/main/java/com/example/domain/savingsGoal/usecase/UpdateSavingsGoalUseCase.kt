package com.example.domain.savingsGoal.usecase

import com.example.domain.savingsGoal.SavingsGoalRepository
import com.example.domain.savingsGoal.model.SavingsGoal

class UpdateSavingsGoalUseCase(
    private val repository: SavingsGoalRepository
) {
    suspend operator fun invoke(targetAmount: Double): Result<SavingsGoal> {
        return repository.updateCurrentSavingsGoal(targetAmount)
    }
}