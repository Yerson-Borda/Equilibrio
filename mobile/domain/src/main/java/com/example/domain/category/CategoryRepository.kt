package com.example.domain.category

import com.example.domain.category.model.Category

interface CategoryRepository {
    suspend fun getCategories(): Result<List<Category>>
    suspend fun getIncomeCategories(): Result<List<Category>>
    suspend fun getExpenseCategories(): Result<List<Category>>
    suspend fun createCategory(
        name: String,
        type: String,
        color: String,
        icon: String
    ): Result<Category>
    suspend fun deleteCategory(categoryId: Int) :Result<Unit>
}