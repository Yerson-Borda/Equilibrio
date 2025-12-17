package com.example.domain.transaction.usecase

import com.example.domain.transaction.TransactionRepository
import com.example.domain.transaction.model.TransactionEntity

class GetTransactionsUseCase(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(): Result<List<TransactionEntity>> {
        return repository.getTransactions()
    }
}