package com.example.data.network.category

import com.example.data.network.category.model.CategoryCreateRequest as DataCategoryCreateRequest
import com.example.domain.category.CategoryRepository
import com.example.domain.category.model.Category
import com.example.domain.category.model.CategoryCreateRequest as DomainCategoryCreateRequest
import com.example.domain.category.model.CategorySummary

class CategoryRepositoryImpl(
    private val categoryApi: CategoryApi
) : CategoryRepository {

    override suspend fun getCategories(): Result<List<Category>> {
        return try {
            val response = categoryApi.getCategories()
            if (response.isSuccessful) {
                val categories = response.body()?.map { it.toDomain() } ?: emptyList()
                Result.success(categories)
            } else {
                Result.failure(Exception("Failed to get categories: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createCategory(request: DomainCategoryCreateRequest): Result<Category> {
        return try {
            val dataRequest = DataCategoryCreateRequest.fromDomain(request)
            val response = categoryApi.createCategory(dataRequest)
            if (response.isSuccessful) {
                val category = response.body()?.toDomain()
                if (category != null) {
                    Result.success(category)
                } else {
                    Result.failure(Exception("Failed to create category: empty response"))
                }
            } else {
                Result.failure(Exception("Failed to create category: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getIncomeCategories(): Result<List<Category>> {
        return try {
            val response = categoryApi.getIncomeCategories()
            if (response.isSuccessful) {
                val categories = response.body()?.map { it.toDomain() } ?: emptyList()
                Result.success(categories)
            } else {
                Result.failure(Exception("Failed to get income categories: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getExpenseCategories(): Result<List<Category>> {
        return try {
            val response = categoryApi.getExpenseCategories()
            if (response.isSuccessful) {
                val categories = response.body()?.map { it.toDomain() } ?: emptyList()
                Result.success(categories)
            } else {
                Result.failure(Exception("Failed to get expense categories: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteCategory(categoryId: Int): Result<Boolean> {
        return try {
            val response = categoryApi.deleteCategory(categoryId)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("Failed to delete category: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}