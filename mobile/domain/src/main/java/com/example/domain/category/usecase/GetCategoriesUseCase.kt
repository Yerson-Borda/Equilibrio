package com.example.domain.category.usecase

import com.example.domain.category.CategoryRepository
import com.example.domain.category.model.Category

class GetCategoriesUseCase(
    private val repository: CategoryRepository
) {
    suspend operator fun invoke(): Result<List<Category>> {
        return repository.getCategories()
    }
}