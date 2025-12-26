package com.example.domain.wallet.usecase

import com.example.domain.wallet.WalletRepository
import com.example.domain.wallet.model.TotalBalance

class GetTotalBalanceUseCase (
    private val repository: WalletRepository
) {
    suspend operator fun invoke(): Result<TotalBalance> {
        return repository.getTotalBalance()
    }
}