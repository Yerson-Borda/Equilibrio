package com.example.data.network.category

import com.example.data.network.category.model.CategoryCreateRequest
import com.example.domain.category.CategoryRepository
import com.example.domain.category.model.Category

class CategoryRepositoryImpl(
    private val api: CategoryApi
) : CategoryRepository {

    override suspend fun getCategories(): Result<List<Category>> {
        return try {
            val response = api.getCategories()
            Result.success(response.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getIncomeCategories(): Result<List<Category>> {
        return try {
            val response = api.getIncomeCategories()
            Result.success(response.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getExpenseCategories(): Result<List<Category>> {
        return try {
            val response = api.getExpenseCategories()
            Result.success(response.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createCategory(
        name: String,
        type: String,
        color: String,
        icon: String
    ): Result<Category> {
        return try {
            val request = CategoryCreateRequest(
                name = name,
                type = type,
                color = color,
                icon = icon
            )
            val response = api.createCategory(request)
            Result.success(response.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteCategory(categoryId: Int): Result<Unit> {
        return try {
            val response = api.deleteCategory(categoryId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete category"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}