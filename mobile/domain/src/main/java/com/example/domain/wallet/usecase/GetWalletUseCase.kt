package com.example.domain.wallet.usecase

import com.example.domain.wallet.WalletRepository
import com.example.domain.wallet.model.Wallet

class GetWalletUseCase(
    private val repository: WalletRepository
) {
    suspend operator fun invoke(walletId: Int): Result<Wallet> {
        return repository.getWallets().mapCatching { wallets: List<Wallet?> ->
            wallets.find { it?.id == walletId } ?: throw Exception("Wallet not found")
        }
    }
}