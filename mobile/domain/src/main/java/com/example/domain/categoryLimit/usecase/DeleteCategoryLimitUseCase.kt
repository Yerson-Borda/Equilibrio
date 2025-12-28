package com.example.domain.categoryLimit.usecase

import com.example.domain.categoryLimit.CategoryLimitRepository

class DeleteCategoryLimitUseCase (
    private val repository: CategoryLimitRepository
) {
    suspend operator fun invoke(categoryId: Int): Result<Unit> {
        return repository.deleteCategoryLimit(categoryId)
    }
}