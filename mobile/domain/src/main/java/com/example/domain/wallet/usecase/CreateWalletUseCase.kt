package com.example.domain.wallet.usecase

import com.example.domain.wallet.WalletRepository
import com.example.domain.wallet.model.Wallet
import com.example.domain.wallet.model.WalletCreateRequest


class CreateWalletUseCase (
    private val repository: WalletRepository
) {
    suspend operator fun invoke(walletRequest: WalletCreateRequest): Result<Wallet> {
        return repository.createWallet(walletRequest)
    }
}