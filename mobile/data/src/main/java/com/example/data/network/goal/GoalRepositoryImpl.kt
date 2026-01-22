package com.example.data.network.goal

import com.example.data.network.goal.model.GoalUpdateRequest
import com.example.data.network.goal.model.toDomain
import com.example.domain.goal.GoalRepository
import com.example.domain.goal.model.Goal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.time.LocalDate

class GoalRepositoryImpl(
    private val apiService: GoalApiService
) : GoalRepository {

    override suspend fun getGoals(): Result<List<Goal>> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.getGoals()
                if (response.isSuccessful) {
                    response.body()?.let { goalResponses ->
                        Result.success(goalResponses.map { it.toDomain() })
                    } ?: Result.failure(Exception("Empty response body"))
                } else {
                    Result.failure(Exception("API Error: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun getGoal(goalId: Int): Result<Goal> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.getGoal(goalId)
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

    override suspend fun createGoal(
        title: String,
        goalAmount: Double,
        currency: String,
        description: String?,
        deadline: LocalDate?,
        imagePath: String?
    ): Result<Goal> = withContext(Dispatchers.IO) {
        try {
            val titleBody = title.toRequestBody(MultipartBody.FORM)
            val goalAmountBody = goalAmount.toString().toRequestBody(MultipartBody.FORM)
            val currencyBody = currency.toRequestBody(MultipartBody.FORM)
            val descriptionBody = description?.toRequestBody(MultipartBody.FORM)
            val deadlineBody = deadline?.toString()?.toRequestBody(MultipartBody.FORM)

            val imagePart = imagePath?.let { path ->
                val file = File(path)
                val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData(
                    "image",
                    file.name,
                    requestBody
                )
            }

            val response = apiService.createGoal(
                title = titleBody,
                goalAmount = goalAmountBody,
                currency = currencyBody,
                description = descriptionBody,
                deadline = deadlineBody,
                image = imagePart
            )

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

    override suspend fun updateGoal(
        goalId: Int,
        title: String?,
        description: String?,
        image: String?,
        deadline: LocalDate?,
        goalAmount: Double?
    ): Result<Goal> = withContext(Dispatchers.IO) {
        try {
            val request = GoalUpdateRequest(
                title = title,
                description = description,
                image = image,
                deadline = deadline?.toString(),
                goal_amount = goalAmount?.toString()
            )

            val response = apiService.updateGoal(goalId, request)
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

    override suspend fun deleteGoal(goalId: Int): Result<Boolean> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.deleteGoal(goalId)
                if (response.isSuccessful) {
                    Result.success(true)
                } else {
                    Result.failure(Exception("API Error: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}