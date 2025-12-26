package com.example.domain.transaction.usecase

import com.example.domain.transaction.TransactionRepository
import com.example.domain.transaction.model.AverageSpendingData
import com.example.domain.transaction.model.PeriodFilter

class GetAverageSpendingUseCase(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(period: PeriodFilter): Result<List<AverageSpendingData>> {
        return transactionRepository.getAverageSpending(period)
    }
}