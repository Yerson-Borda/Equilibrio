package com.example.data.network.budget.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BudgetUpdateRequest(
    @SerialName("monthly_limit") val monthlyLimit: String? = null,
    @SerialName("daily_limit") val dailyLimit: String? = null
)