package com.example.domain.category.usecase

import com.example.domain.category.CategoryRepository

class DeleteCategoryUseCase(
    private val repository: CategoryRepository
) {
    suspend operator fun invoke(categoryId: Int): Result<Boolean> {
        return repository.deleteCategory(categoryId)
    }
}