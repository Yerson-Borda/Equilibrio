package com.example.domain.goal.usecase

import com.example.domain.goal.GoalRepository
import com.example.domain.goal.model.Goal
import java.time.LocalDate

class GetGoalsUseCase(
    private val repository: GoalRepository
) {
    suspend operator fun invoke(): Result<List<Goal>> {
        return repository.getGoals()
    }
}