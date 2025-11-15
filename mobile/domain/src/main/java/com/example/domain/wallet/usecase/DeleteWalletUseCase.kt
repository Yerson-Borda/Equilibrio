package com.example.domain.wallet.usecase

import com.example.domain.wallet.WalletRepository

class DeleteWalletUseCase(
    private val walletRepository: WalletRepository
) {
    suspend operator fun invoke(walletId: Int): Result<Boolean> {
        return walletRepository.deleteWallet(walletId)
    }
}