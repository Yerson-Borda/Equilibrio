package com.example.domain.goal.usecase

import com.example.domain.goal.GoalRepository
import com.example.domain.goal.model.Goal

class GetGoalUseCase(
    private val repository: GoalRepository
) {
    suspend operator fun invoke(goalId: Int): Result<Goal> {
        return repository.getGoal(goalId)
    }
}