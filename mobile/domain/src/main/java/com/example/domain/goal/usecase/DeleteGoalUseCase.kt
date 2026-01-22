package com.example.domain.goal.usecase

import com.example.domain.goal.GoalRepository

class DeleteGoalUseCase(
    private val repository: GoalRepository
) {
    suspend operator fun invoke(goalId: Int): Result<Boolean> {
        return repository.deleteGoal(goalId)
    }
}