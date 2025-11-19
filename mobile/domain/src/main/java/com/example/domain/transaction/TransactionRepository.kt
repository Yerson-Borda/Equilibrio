package com.example.domain.transaction

import com.example.domain.transaction.model.TransactionEntity
import com.example.domain.transaction.model.TransferEntity

interface TransactionRepository {
    suspend fun createTransaction(
        amount: Any,
        note: String?, // Changed from description to note
        type: String, // "income", "expense", "transfer"
        transactionDate: String,
        walletId: Int,
        categoryId: Int
    ): Result<TransactionEntity>

    suspend fun createTransfer(
        sourceWalletId: Int,
        destinationWalletId: Int,
        amount: Any,
        note: String?
    ): Result<TransferEntity>

    suspend fun getTransactions(): Result<List<TransactionEntity>>

    suspend fun deleteTransaction(id: Int): Result<Unit>

    suspend fun getTransactionsByWalletId(walletId: Int): Result<List<TransactionEntity>>
}