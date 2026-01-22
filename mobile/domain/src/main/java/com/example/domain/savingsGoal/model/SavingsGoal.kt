package com.example.domain.savingsGoal.model

data class SavingsGoal(
    val id: Int,
    val month: Int,
    val year: Int,
    val targetAmount: Double,
    val currentSaved: Double
) {
    val progress: Float
        get() = if (targetAmount > 0) (currentSaved / targetAmount).toFloat().coerceIn(0f, 1f) else 0f
    
    val remainingAmount: Double
        get() = (targetAmount - currentSaved).coerceAtLeast(0.0)
    
    val isAchieved: Boolean
        get() = currentSaved >= targetAmount
}