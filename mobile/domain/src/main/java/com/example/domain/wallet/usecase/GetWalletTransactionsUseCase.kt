package com.example.domain.wallet.usecase

import com.example.domain.wallet.WalletRepository
import com.example.domain.wallet.model.Transaction

class GetWalletTransactionsUseCase (
    private val repository: WalletRepository
) {
    suspend operator fun invoke(walletId: Int): Result<List<Transaction>> {
        return repository.getWalletTransactions(walletId)
    }
}