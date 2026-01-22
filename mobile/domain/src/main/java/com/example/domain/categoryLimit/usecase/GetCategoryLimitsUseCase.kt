package com.example.domain.categoryLimit.usecase

import com.example.domain.categoryLimit.CategoryLimitRepository
import com.example.domain.categoryLimit.model.CategoryLimitOverview

class GetCategoryLimitsUseCase (
    private val repository: CategoryLimitRepository
) {
    suspend operator fun invoke(): Result<List<CategoryLimitOverview>> {
        return repository.getCategoryLimits()
    }
}