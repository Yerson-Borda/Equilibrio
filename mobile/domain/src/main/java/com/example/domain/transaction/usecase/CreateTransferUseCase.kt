package com.example.domain.transaction.usecase

import com.example.domain.transaction.TransactionRepository
import com.example.domain.transaction.model.TransferEntity

class CreateTransferUseCase(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(
        sourceWalletId: Int,
        destinationWalletId: Int,
        amount: Any,
        note: String? = null
    ): Result<TransferEntity> {
        return repository.createTransfer(
            sourceWalletId = sourceWalletId,
            destinationWalletId = destinationWalletId,
            amount = amount,
            note = note
        )
    }
}