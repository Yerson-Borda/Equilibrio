package com.example.domain.category.usecase

import com.example.domain.category.CategoryRepository
import com.example.domain.category.model.Category

class CreateCategoryUseCase(
    private val repository: CategoryRepository
) {
    suspend operator fun invoke(
        name: String,
        type: String,
        color: String,
        icon: String
    ): Result<Category> {
        return repository.createCategory(name, type, color, icon)
    }
}