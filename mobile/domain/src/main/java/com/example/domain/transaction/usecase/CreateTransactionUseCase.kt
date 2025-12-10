package com.example.domain.transaction.usecase

import com.example.domain.transaction.TransactionRepository
import com.example.domain.transaction.model.CreateTransaction
import com.example.domain.transaction.model.TransactionEntity

class CreateTransactionUseCase(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(
        createTransaction: CreateTransaction
    ): Result<TransactionEntity> {
        return repository.createTransaction(createTransaction)
    }
}