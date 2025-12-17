package com.example.domain.transaction.usecase

import com.example.domain.transaction.TransactionRepository
import com.example.domain.transaction.model.TransactionEntity

class GetRecentTransactionsUseCase (
    private val transactionRepository: TransactionRepository
) {
    suspend fun execute(limit: Int = 10): Result<List<TransactionEntity>> {
        return try {
            val result = transactionRepository.getTransactions()
            if (result.isSuccess) {
                val transactions = result.getOrNull() ?: emptyList()
                // Sort by date descending and take limited number
                val sortedTransactions = transactions.sortedByDescending { it.transactionDate }.take(limit)
                Result.success(sortedTransactions)
            } else {
                result
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}