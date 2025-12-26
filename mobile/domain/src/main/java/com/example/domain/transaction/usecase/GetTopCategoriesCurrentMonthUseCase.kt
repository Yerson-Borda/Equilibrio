package com.example.domain.transaction.usecase

import com.example.domain.transaction.TransactionRepository
import com.example.domain.transaction.model.TopCategoryData

class GetTopCategoriesCurrentMonthUseCase(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(): Result<List<TopCategoryData>> {
        return transactionRepository.getTopCategoriesCurrentMonth()
    }
}