package com.example.data.network.budget

import com.example.data.network.budget.model.BudgetUpdateRequest
import com.example.domain.budget.BudgetRepository
import com.example.domain.budget.model.Budget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BudgetRepositoryImpl(
    private val budgetApi: BudgetApi
) : BudgetRepository {

    override suspend fun getCurrentBudget(): Result<Budget> {
        return withContext(Dispatchers.IO) {
            try {
                val response = budgetApi.getCurrentBudget()
                if (response.isSuccessful) {
                    val budgetResponse = response.body()
                    if (budgetResponse != null) {
                        Result.success(BudgetMapper.toDomain(budgetResponse))
                    } else {
                        Result.failure(Exception("Budget data is null"))
                    }
                } else {
                    Result.failure(Exception("Failed to fetch budget: ${response.code()} - ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun updateCurrentBudget(monthlyLimit: Double?, dailyLimit: Double?): Result<Budget> {
        return withContext(Dispatchers.IO) {
            try {
                val request = BudgetUpdateRequest(
                    monthlyLimit = monthlyLimit?.toString(),
                    dailyLimit = dailyLimit?.toString()
                )
                val response = budgetApi.updateCurrentBudget(request)
                if (response.isSuccessful) {
                    val budgetResponse = response.body()
                    if (budgetResponse != null) {
                        Result.success(BudgetMapper.toDomain(budgetResponse))
                    } else {
                        Result.failure(Exception("Updated budget data is null"))
                    }
                } else {
                    Result.failure(Exception("Failed to update budget: ${response.code()} - ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}