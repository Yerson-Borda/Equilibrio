package com.example.data.network.budget

import com.example.data.network.budget.model.BudgetResponse
import com.example.data.network.budget.model.BudgetUpdateRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT

interface BudgetApi {
    @GET("api/budget/current")
    suspend fun getCurrentBudget(): Response<BudgetResponse>

    @PUT("api/budget/current")
    suspend fun updateCurrentBudget(@Body request: BudgetUpdateRequest): Response<BudgetResponse>
}