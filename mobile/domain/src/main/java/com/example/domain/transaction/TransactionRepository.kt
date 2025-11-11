package com.example.domain.transaction

import com.example.domain.transaction.model.TransactionEntity
import com.example.domain.transaction.model.TransferEntity

interface TransactionRepository {
    suspend fun createTransaction(
        amount: Any, // Number or String
        description: String?,
        note: String?,
        type: String,
        transactionDate: String,
        walletId: Int,
        categoryId: Int
    ): Result<TransactionEntity>

    suspend fun createTransfer(
        sourceWalletId: Int,
        destinationWalletId: Int,
        amount: Any, // Number or String
        note: String?
    ): Result<TransferEntity>

    suspend fun getTransactions(): Result<List<TransactionEntity>>
    suspend fun getTransactionById(id: Int): Result<TransactionEntity>
    suspend fun deleteTransaction(id: Int): Result<Unit>
}