package com.example.data.network.goal

import com.example.data.network.goal.model.GoalResponse
import com.example.data.network.goal.model.GoalUpdateRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface GoalApiService {

    @GET("/api/goals")
    suspend fun getGoals(): Response<List<GoalResponse>>

    @GET("/api/goals/{goal_id}")
    suspend fun getGoal(@Path("goal_id") goalId: Int): Response<GoalResponse>

    @Multipart
    @POST("/api/goals")
    suspend fun createGoal(
        @Part("title") title: RequestBody,
        @Part("goal_amount") goalAmount: RequestBody,
        @Part("currency") currency: RequestBody,
        @Part("description") description: RequestBody? = null,
        @Part("deadline") deadline: RequestBody? = null,
        @Part image: MultipartBody.Part? = null
    ): Response<GoalResponse>

    @PUT("/api/goals/{goal_id}")
    suspend fun updateGoal(
        @Path("goal_id") goalId: Int,
        @Body request: GoalUpdateRequest
    ): Response<GoalResponse>

    @DELETE("/api/goals/{goal_id}")
    suspend fun deleteGoal(@Path("goal_id") goalId: Int): Response<Unit>
}