package com.example.data.network.home

import com.example.data.network.home.model.StatsResponse
import com.example.data.network.home.model.UserDetailedResponse
import com.example.data.network.home.model.UserResponse
import com.example.domain.home.UserRepository
import com.example.domain.home.model.StatsData
import com.example.domain.home.model.UserData
import com.example.domain.home.model.UserDetailedData

class UserRepositoryImpl(
    private val userApi: UserApi
) : UserRepository {

    override suspend fun getUserDetailed(): UserDetailedData {
        val response = userApi.getUserDetailed()
        return response.toDomain()
    }
}

// Extension functions to convert from network to domain models
private fun UserDetailedResponse.toDomain(): UserDetailedData {
    return UserDetailedData(
        user = user.toDomain(),
        stats = stats.toDomain()
    )
}

private fun UserResponse.toDomain(): UserData {
    return UserData(
        id = id.toString(),
        email = email,
        fullName = fullName,
        phoneNumber = phoneNumber,
        dateOfBirth = dateOfBirth,
        avatarUrl = avatarUrl,
        defaultCurrency = defaultCurrency,
        createdAt = createdAt
    )
}

private fun StatsResponse.toDomain(): StatsData {
    return StatsData(
        walletCount = walletCount,
        totalTransactions = totalTransactions,
        expenseCount = expenseCount,
        incomeCount = incomeCount
    )
}