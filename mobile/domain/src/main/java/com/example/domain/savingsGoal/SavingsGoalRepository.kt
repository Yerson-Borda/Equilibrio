package com.example.domain.savingsGoal

import com.example.domain.savingsGoal.model.SavingsGoal

interface SavingsGoalRepository {
    suspend fun getCurrentSavingsGoal(): Result<SavingsGoal>
    suspend fun updateCurrentSavingsGoal(targetAmount: Double): Result<SavingsGoal>
}