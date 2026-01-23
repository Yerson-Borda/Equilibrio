package com.example.domain.goal.usecase

import com.example.domain.goal.GoalRepository
import com.example.domain.goal.model.Goal
import java.time.LocalDate

class UpdateGoalUseCase(
    private val repository: GoalRepository
) {
    suspend operator fun invoke(
        goalId: Int,
        title: String? = null,
        description: String? = null,
        image: String? = null,
        deadline: LocalDate? = null,
        goalAmount: Double? = null
    ): Result<Goal> {
        return repository.updateGoal(
            goalId = goalId,
            title = title,
            description = description,
            image = image,
            deadline = deadline,
            goalAmount = goalAmount
        )
    }
}