package com.example.data.network.home

import com.example.data.network.home.model.HomeResponse
import com.example.data.network.home.model.SavingsGoalResponse
import com.example.data.network.home.model.TransactionResponse
import com.example.domain.home.HomeRepository
import com.example.domain.home.model.HomeData
import com.example.domain.home.model.SavingsGoal
import com.example.domain.home.model.Transaction

class HomeRepositoryImpl(
    private val homeApi: HomeApi
) : HomeRepository {

    override suspend fun getHomeData(): HomeData {
        val response = homeApi.getHomeData()
        return response.toDomain()
    }
}

// Extension functions to convert from network to domain models
private fun HomeResponse.toDomain(): HomeData {
    return HomeData(
        totalBalance = totalBalance,
        savedAmount = savedAmount,
        spentAmount = spentAmount,
        budgetRemaining = budgetRemaining,
        budgetStatus = budgetStatus,
        savingsGoals = savingsGoals.map { it.toDomain() },
        recentTransactions = recentTransactions.map { it.toDomain() },
        hasWallets = hasWallets
    )
}

private fun SavingsGoalResponse.toDomain(): SavingsGoal {
    return SavingsGoal(
        id = id,
        name = name,
        targetAmount = targetAmount,
        currentAmount = currentAmount
    )
}

private fun TransactionResponse.toDomain(): Transaction {
    return Transaction(
        id = id,
        merchant = merchant,
        category = category,
        amount = amount,
        date = date,
        type = type
    )
}