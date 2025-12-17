package com.example.domain.wallet.usecase

import com.example.domain.wallet.WalletRepository
import com.example.domain.wallet.model.Wallet
import kotlinx.coroutines.flow.Flow

class GetWalletDetailUseCase(
    private val walletRepository: WalletRepository
) {
    suspend operator fun invoke(walletId: Int): Flow<Wallet> {
        return walletRepository.getWalletDetail(walletId)
    }
}