// data/src/main/java/com/example/data/network/user/model/UserDetailedResponse.kt
package com.example.data.network.user.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDetailedResponse(
    @SerialName("user") val user: UserResponse,
    @SerialName("stats") val stats: StatsResponse
)

@Serializable
data class UserResponse(
    @SerialName("id") val id: Int,
    @SerialName("email") val email: String,
    @SerialName("full_name") val fullName: String?,
    @SerialName("phone_number") val phoneNumber: String?,
    @SerialName("date_of_birth") val dateOfBirth: String?,
    @SerialName("avatar_url") val avatarUrl: String?,
    @SerialName("default_currency") val defaultCurrency: String,
    @SerialName("is_active") val isActive: Boolean? = null,
    @SerialName("created_at") val createdAt: String
)

@Serializable
data class StatsResponse(
    @SerialName("wallet_count") val walletCount: Int,
    @SerialName("total_transactions") val totalTransactions: Int,
    @SerialName("expense_count") val expenseCount: Int,
    @SerialName("income_count") val incomeCount: Int
)