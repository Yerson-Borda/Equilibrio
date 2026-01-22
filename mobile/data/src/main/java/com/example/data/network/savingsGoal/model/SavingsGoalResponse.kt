package com.example.data.network.savingsGoal.model

import com.example.domain.savingsGoal.model.SavingsGoal
import kotlinx.serialization.Serializable

@Serializable
data class SavingsGoalResponse(
    val id: Int,
    val month: Int,
    val year: Int,
    val target_amount: String,
    val current_saved: String
)

@Serializable
data class SavingsGoalUpdateRequest(
    val target_amount: String
)

fun SavingsGoalResponse.toDomain(): SavingsGoal {
    return SavingsGoal(
        id = id,
        month = month,
        year = year,
        // Converting String from API to Double for Domain
        targetAmount = target_amount.toDoubleOrNull() ?: 0.0,
        currentSaved = current_saved.toDoubleOrNull() ?: 0.0
    )
}