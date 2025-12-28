package com.example.domain.categoryLimit.usecase

import com.example.domain.categoryLimit.CategoryLimitRepository
import com.example.domain.categoryLimit.model.CategoryLimit
import com.example.domain.categoryLimit.model.CategoryLimitUpdate

class UpdateCategoryLimitUseCase (
    private val repository: CategoryLimitRepository
) {
    suspend operator fun invoke(
        categoryId: Int,
        monthlyLimit: String
    ): Result<CategoryLimit> {
        val limitUpdate = CategoryLimitUpdate(monthlyLimit = monthlyLimit)
        return repository.updateCategoryLimit(categoryId, limitUpdate)
    }

    // Alternative with CategoryLimitUpdate object
    suspend operator fun invoke(
        categoryId: Int,
        limitUpdate: CategoryLimitUpdate
    ): Result<CategoryLimit> {
        return repository.updateCategoryLimit(categoryId, limitUpdate)
    }
}