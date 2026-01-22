package com.example.domain.goal.usecase

import com.example.domain.goal.GoalRepository
import com.example.domain.goal.model.Goal
import java.time.LocalDate
    class CreateGoalUseCase(
        private val repository: GoalRepository
    ) {
        suspend operator fun invoke(
            title: String,
            goalAmount: Double,
            currency: String,
            description: String? = null,
            deadline: LocalDate? = null,
            imagePath: String? = null
        ): Result<Goal> {
            return repository.createGoal(
                title = title,
                goalAmount = goalAmount,
                currency = currency,
                description = description,
                deadline = deadline,
                imagePath = imagePath
            )
        }
    }