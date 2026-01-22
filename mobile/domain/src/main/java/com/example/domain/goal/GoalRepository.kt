package com.example.domain.goal

import com.example.domain.goal.model.Goal
import java.time.LocalDate

interface GoalRepository {
    suspend fun getGoals(): Result<List<Goal>>
    suspend fun getGoal(goalId: Int): Result<Goal>
    suspend fun createGoal(
        title: String,
        goalAmount: Double,
        currency: String,
        description: String? = null,
        deadline: LocalDate? = null,
        imagePath: String? = null
    ): Result<Goal>
    suspend fun updateGoal(
        goalId: Int,
        title: String? = null,
        description: String? = null,
        image: String? = null,
        deadline: LocalDate? = null,
        goalAmount: Double? = null
    ): Result<Goal>
    suspend fun deleteGoal(goalId: Int): Result<Boolean>
}