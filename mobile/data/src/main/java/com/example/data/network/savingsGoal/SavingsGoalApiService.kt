package com.example.data.network.savingsGoal

import com.example.data.network.savingsGoal.model.SavingsGoalResponse
import com.example.data.network.savingsGoal.model.SavingsGoalUpdateRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT

interface SavingsGoalApiService {
    
    @GET("/api/savings_goal/current")
    suspend fun getCurrentSavingsGoal(): Response<SavingsGoalResponse>
    
    @PUT("/api/savings_goal/current")
    suspend fun updateCurrentSavingsGoal(@Body request: SavingsGoalUpdateRequest): Response<SavingsGoalResponse>
}