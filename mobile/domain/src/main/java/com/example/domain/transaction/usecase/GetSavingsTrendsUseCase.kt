package com.example.domain.transaction.usecase

import com.example.domain.transaction.TransactionRepository
import com.example.domain.transaction.model.SavingsTrendsData

class GetSavingsTrendsUseCase(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(months: Int = 6): Result<SavingsTrendsData> {
        return transactionRepository.getSavingsTrends(months)
    }
}