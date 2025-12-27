package com.example.data.network.categoryLimit

import com.example.data.network.categoryLimit.model.toDomain
import com.example.data.network.categoryLimit.model.toDto
import com.example.domain.categoryLimit.CategoryLimitRepository
import com.example.domain.categoryLimit.model.CategoryLimit
import com.example.domain.categoryLimit.model.CategoryLimitOverview
import com.example.domain.categoryLimit.model.CategoryLimitUpdate
import java.io.IOException

class CategoryLimitRepositoryImpl(
    private val remoteDataSource: CategoryLimitApi
) : CategoryLimitRepository {


    override suspend fun getCategoryLimits(): Result<List<CategoryLimitOverview>> {
        return try {
            val response = remoteDataSource.getCategoryLimits()
            println("DEBUG: CategoryLimitRepository - GET /limits/ response: ${response.code()}")
            println("DEBUG: Response body: ${response.body()}")
            println("DEBUG: Response error: ${response.errorBody()?.string()}")

            if (response.isSuccessful) {
                val limits = response.body()?.map { it.toDomain() } ?: emptyList()
                Result.success(limits)
            } else {
                val errorBody = response.errorBody()?.string() ?: "No error body"
                val errorMessage = "Failed to fetch category limits: ${response.code()} - $errorBody"
                println("DEBUG: CategoryLimitRepository - Error: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: IOException) {
            println("DEBUG: CategoryLimitRepository - Network error: ${e.message}")
            Result.failure(Exception("Network error: ${e.message}"))
        } catch (e: Exception) {
            println("DEBUG: CategoryLimitRepository - Unexpected error: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun updateCategoryLimit(
        categoryId: Int,
        limitUpdate: CategoryLimitUpdate
    ): Result<CategoryLimit> {
        return try {
            println("DEBUG: CategoryLimitRepository - Updating limit for category $categoryId with $limitUpdate")
            val response = remoteDataSource.updateCategoryLimit(
                categoryId = categoryId,
                limitUpdate = limitUpdate.toDto()
            )

            println("DEBUG: CategoryLimitRepository - PUT /limits/$categoryId response: ${response.code()}")
            println("DEBUG: Response body: ${response.body()}")
            println("DEBUG: Response error: ${response.errorBody()?.string()}")

            if (response.isSuccessful) {
                val categoryLimit = response.body()?.toDomain()
                    ?: return Result.failure(Exception("Empty response body"))
                Result.success(categoryLimit)
            } else {
                val errorBody = response.errorBody()?.string() ?: "No error body"
                val errorMessage = "Failed to update category limit: ${response.code()} - $errorBody"
                println("DEBUG: CategoryLimitRepository - Error: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: IOException) {
            println("DEBUG: CategoryLimitRepository - Network error: ${e.message}")
            Result.failure(Exception("Network error: ${e.message}"))
        } catch (e: Exception) {
            println("DEBUG: CategoryLimitRepository - Unexpected error: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun deleteCategoryLimit(categoryId: Int): Result<Unit> {
        return try {
            println("DEBUG: CategoryLimitRepository - Deleting limit for category $categoryId")
            val response = remoteDataSource.deleteCategoryLimit(categoryId)

            println("DEBUG: CategoryLimitRepository - DELETE /limits/$categoryId response: ${response.code()}")
            println("DEBUG: Response body: ${response.body()}")
            println("DEBUG: Response error: ${response.errorBody()?.string()}")

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorBody = response.errorBody()?.string() ?: "No error body"
                val errorMessage = "Failed to delete category limit: ${response.code()} - $errorBody"
                println("DEBUG: CategoryLimitRepository - Error: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: IOException) {
            println("DEBUG: CategoryLimitRepository - Network error: ${e.message}")
            Result.failure(Exception("Network error: ${e.message}"))
        } catch (e: Exception) {
            println("DEBUG: CategoryLimitRepository - Unexpected error: ${e.message}")
            Result.failure(e)
        }
    }
}