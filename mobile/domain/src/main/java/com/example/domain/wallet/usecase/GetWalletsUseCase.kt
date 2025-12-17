package com.example.domain.wallet.usecase

import com.example.domain.wallet.WalletRepository
import com.example.domain.wallet.model.Wallet

class GetWalletsUseCase (
    private val repository: WalletRepository
) {
    suspend operator fun invoke(): Result<List<Wallet>> {
        return repository.getWallets()
    }
}