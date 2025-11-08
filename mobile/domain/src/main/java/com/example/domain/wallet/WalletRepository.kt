// domain/repository/WalletRepository.kt
package com.example.domain.wallet

import com.example.domain.wallet.model.TotalBalance
import com.example.domain.wallet.model.Transaction
import com.example.domain.wallet.model.Wallet
import com.example.domain.wallet.model.WalletCreateRequest

interface WalletRepository {
    suspend fun getWallets(): Result<List<Wallet>>
    suspend fun createWallet(wallet: WalletCreateRequest): Result<Wallet>
    suspend fun getWalletTransactions(walletId: Int): Result<List<Transaction>>
    suspend fun getTotalBalance(): Result<TotalBalance>
}