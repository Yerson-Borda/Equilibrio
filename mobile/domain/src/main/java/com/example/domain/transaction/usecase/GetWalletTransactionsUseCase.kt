package com.example.domain.transaction.usecase

import com.example.domain.transaction.TransactionRepository
import com.example.domain.transaction.model.TransactionEntity

class GetWalletTransactionsUseCase(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(walletId: Int): Result<List<TransactionEntity>> {
        return transactionRepository.getTransactionsByWalletId(walletId)
    }
}