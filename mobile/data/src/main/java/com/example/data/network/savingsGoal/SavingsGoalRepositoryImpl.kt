package com.example.data.network.savingsGoal

import com.example.data.network.savingsGoal.model.SavingsGoalUpdateRequest
import com.example.data.network.savingsGoal.model.toDomain // Import your mapper
import com.example.domain.savingsGoal.SavingsGoalRepository
import com.example.domain.savingsGoal.model.SavingsGoal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SavingsGoalRepositoryImpl(
    private val apiService: SavingsGoalApiService
) : SavingsGoalRepository {

    override suspend fun getCurrentSavingsGoal(): Result<SavingsGoal> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.getCurrentSavingsGoal()
                if (response.isSuccessful) {
                    response.body()?.let {
                        // Map the DTO to Domain model here
                        Result.success(it.toDomain())
                    } ?: Result.failure(Exception("Empty response body"))
                } else {
                    Result.failure(Exception("API Error: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun updateCurrentSavingsGoal(targetAmount: Double): Result<SavingsGoal> =
        withContext(Dispatchers.IO) {
            try {
                // Convert the Double from Domain back to String for the API Request
                val request = SavingsGoalUpdateRequest(target_amount = targetAmount.toString())
                val response = apiService.updateCurrentSavingsGoal(request)

                if (response.isSuccessful) {
                    response.body()?.let {
                        Result.success(it.toDomain())
                    } ?: Result.failure(Exception("Empty response body"))
                } else {
                    Result.failure(Exception("API Error: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}