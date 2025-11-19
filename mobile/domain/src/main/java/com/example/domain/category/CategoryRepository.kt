package com.example.domain.category

import com.example.domain.category.model.Category
import com.example.domain.category.model.CategoryCreateRequest

interface CategoryRepository {
    suspend fun getCategories(): Result<List<Category>>
    suspend fun createCategory(request: CategoryCreateRequest): Result<Category>
    suspend fun getIncomeCategories(): Result<List<Category>>
    suspend fun getExpenseCategories(): Result<List<Category>>
    suspend fun deleteCategory(categoryId: Int): Result<Boolean>
}