// data/repository/WalletRepositoryImpl.kt
package com.example.data.network.wallet

import com.example.data.network.wallet.model.toDomain
import com.example.domain.wallet.WalletRepository
import com.example.domain.wallet.model.TotalBalance
import com.example.domain.wallet.model.Transaction
import com.example.domain.wallet.model.Wallet
import com.example.domain.wallet.model.WalletCreateRequest
import com.example.data.network.wallet.model.WalletCreateRequest as DataWalletCreateRequest

class WalletRepositoryImpl(
    private val walletApi: WalletApi
) : WalletRepository {

    override suspend fun getWallets(): Result<List<Wallet>> {
        return try {
            println("🔄 [Repository] Starting getWallets()")
            println("🔄 [Repository] About to call walletApi.getWallets()")

            val response = walletApi.getWallets()
            println("✅ [Repository] API call successful! Received ${response.size} wallets")

            // Log each wallet received
            response.forEachIndexed { index, walletResponse ->
                println("   📦 Wallet $index: id=${walletResponse.id}, name='${walletResponse.name}', balance=${walletResponse.balance}, type=${walletResponse.wallet_type}")
            }

            println("🔄 [Repository] Mapping response to domain models...")
            val domainWallets = response.map { it.toDomain() }
            println("✅ [Repository] Successfully mapped to ${domainWallets.size} domain wallets")

            // Log domain wallets
            domainWallets.forEachIndexed { index, wallet ->
                println("   💳 Domain Wallet $index: id=${wallet.id}, name='${wallet.name}', balance=${wallet.balance}, type=${wallet.walletType}")
            }

            Result.success(domainWallets)
        } catch (e: Exception) {
            println("❌ [Repository] EXCEPTION in getWallets(): ${e.message}")
            println("❌ [Repository] Exception type: ${e.javaClass.simpleName}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun createWallet(wallet: WalletCreateRequest): Result<Wallet> {
        return try {
            println("🔄 [Repository] Starting createWallet()")
            val request = DataWalletCreateRequest.fromDomain(wallet)
            val response = walletApi.createWallet(request)
            println("✅ [Repository] Wallet created successfully: id=${response.id}, name=${response.name}")
            Result.success(response.toDomain())
        } catch (e: Exception) {
            println("❌ [Repository] EXCEPTION in createWallet(): ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun getWalletTransactions(walletId: Int): Result<List<Transaction>> {
        return try {
            println("🔄 [Repository] Starting getWalletTransactions(walletId=$walletId)")
            val response = walletApi.getWalletTransactions(walletId)
            println("✅ [Repository] Got ${response.size} transactions for wallet $walletId")
            Result.success(response.map { it.toDomain() })
        } catch (e: Exception) {
            println("❌ [Repository] EXCEPTION in getWalletTransactions(): ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun getTotalBalance(): Result<TotalBalance> {
        return try {
            val response = walletApi.getTotalBalance()
            Result.success(response.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}