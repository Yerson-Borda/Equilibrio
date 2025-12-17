package com.example.domain.category.usecase

import com.example.domain.category.CategoryRepository
import com.example.domain.category.model.Category
import com.example.domain.category.model.CategoryCreateRequest

class CreateCategoryUseCase(
    private val repository: CategoryRepository
) {
    suspend operator fun invoke(request: CategoryCreateRequest): Result<Category> {
        return repository.createCategory(request)
    }
}