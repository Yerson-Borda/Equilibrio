package com.example.domain.transaction.usecase

import com.example.domain.transaction.TransactionRepository
import com.example.domain.transaction.model.TransactionEntity

class CreateTransactionUseCase(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(
        amount: Any,
        description: String? = null,
        note: String? = null,
        type: String,
        transactionDate: String,
        walletId: Int,
        categoryId: Int
    ): Result<TransactionEntity> {
        return repository.createTransaction(
            amount = amount,
            description = description,
            note = note,
            type = type,
            transactionDate = transactionDate,
            walletId = walletId,
            categoryId = categoryId
        )
    }
}