package com.example.domain.categoryLimit

import com.example.domain.categoryLimit.model.CategoryLimit
import com.example.domain.categoryLimit.model.CategoryLimitOverview
import com.example.domain.categoryLimit.model.CategoryLimitUpdate

interface CategoryLimitRepository {
    suspend fun getCategoryLimits(): Result<List<CategoryLimitOverview>>
    suspend fun updateCategoryLimit(
        categoryId: Int,
        limitUpdate: CategoryLimitUpdate
    ): Result<CategoryLimit>

    suspend fun deleteCategoryLimit(categoryId: Int): Result<Unit>
}