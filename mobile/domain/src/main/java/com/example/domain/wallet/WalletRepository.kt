// domain/repository/WalletRepository.kt
package com.example.domain.wallet

import android.adservices.signals.UpdateSignalsRequest
import com.example.domain.wallet.model.TotalBalance
import com.example.domain.wallet.model.Transaction
import com.example.domain.wallet.model.Wallet
import com.example.domain.wallet.model.WalletCreateRequest
import com.example.domain.wallet.model.WalletUpdateRequest
import com.example.domain.wallet.usecase.UpdateWalletUseCase
import kotlinx.coroutines.flow.Flow

interface WalletRepository {
    suspend fun getWallets(): Result<List<Wallet>>
    suspend fun createWallet(wallet: WalletCreateRequest): Result<Wallet>
    suspend fun getWalletTransactions(walletId: Int): Result<List<Transaction>>
    suspend fun getTotalBalance(): Result<TotalBalance>
    suspend fun getWalletDetail(walletId: Int): Flow<Wallet>
    suspend fun deleteWallet(walletId: Int): Result<Boolean>
    suspend fun updateWallet(walletId: Int, walletRequest: WalletUpdateRequest): Result<Wallet>}
