package com.example.data.network.budget.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BudgetResponse(
    @SerialName("id") val id: Int,
    @SerialName("month") val month: Int,
    @SerialName("year") val year: Int,
    @SerialName("monthly_limit") val monthlyLimit: String,
    @SerialName("daily_limit") val dailyLimit: String,
    @SerialName("monthly_spent") val monthlySpent: String,
    @SerialName("daily_spent") val dailySpent: String,
    @SerialName("last_updated_date") val lastUpdatedDate: String,
    @SerialName("created_at") val createdAt: String
)