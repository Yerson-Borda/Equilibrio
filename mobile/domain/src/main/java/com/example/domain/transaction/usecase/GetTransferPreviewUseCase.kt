package com.example.domain.transaction.usecase

import com.example.domain.transaction.TransactionRepository
import com.example.domain.transaction.model.TransferPreview

class GetTransferPreviewUseCase(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(
        sourceWalletId: Int,
        destinationWalletId: Int,
        amount: String
    ): Result<TransferPreview> {
        return repository.getTransferPreview(
            sourceWalletId = sourceWalletId,
            destinationWalletId = destinationWalletId,
            amount = amount
        )
    }
}
