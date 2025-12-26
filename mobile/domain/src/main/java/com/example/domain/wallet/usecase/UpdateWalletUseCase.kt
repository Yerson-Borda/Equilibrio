package com.example.domain.wallet.usecase

import com.example.domain.wallet.WalletRepository
import com.example.domain.wallet.model.Wallet
import com.example.domain.wallet.model.WalletUpdateRequest

class UpdateWalletUseCase(
    private val walletRepository: WalletRepository
) {
    suspend operator fun invoke(walletId: Int, walletRequest: WalletUpdateRequest): Result<Wallet> {
        return walletRepository.updateWallet(walletId, walletRequest)
    }
}