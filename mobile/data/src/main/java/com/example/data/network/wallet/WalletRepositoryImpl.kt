// data/repository/WalletRepositoryImpl.kt
package com.example.data.network.wallet

import com.example.data.network.wallet.model.toDomain
import com.example.data.network.wallet.model.WalletUpdateRequest as DataWalletUpdateRequest
import com.example.domain.wallet.WalletRepository
import com.example.domain.wallet.model.TotalBalance
import com.example.domain.wallet.model.Transaction
import com.example.domain.wallet.model.Wallet
import com.example.domain.wallet.model.WalletCreateRequest
import com.example.domain.wallet.model.WalletUpdateRequest as DomainWalletUpdateRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
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

    override suspend fun getWalletDetail(walletId: Int): Flow<Wallet> = flow {
        try {
            println("🔄 [Repository] Starting getWalletDetail(walletId=$walletId)")
            val response = walletApi.getWalletDetail(walletId)
            println("✅ [Repository] Got wallet detail: id=${response.id}, name=${response.name}")
            emit(response.toDomain())
        } catch (e: Exception) {
            println("❌ [Repository] EXCEPTION in getWalletDetail(): ${e.message}")
            throw e
        }
    }

    override suspend fun deleteWallet(walletId: Int): Result<Boolean> {
        return try {
            println("🔄 [Repository] Starting deleteWallet(walletId=$walletId)")
            val response = walletApi.deleteWallet(walletId)
            println("✅ [Repository] Wallet $walletId deleted successfully")
            Result.success(response.isSuccessful)
        } catch (e: Exception) {
            println("❌ [Repository] EXCEPTION in deleteWallet(): ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun updateWallet(walletId: Int, walletRequest: DomainWalletUpdateRequest): Result<Wallet> {
        return try {
            println("🔄 [Repository] Starting updateWallet(walletId=$walletId)")
            println("🔄 [Repository] Domain request: name=${walletRequest.name}, type=${walletRequest.walletType}, balance=${walletRequest.initialBalance}")

            val dataRequest = DataWalletUpdateRequest.fromDomain(walletRequest)
            println("🔄 [Repository] Data request: name=${dataRequest.name}, wallet_type=${dataRequest.walletType}, initial_balance=${dataRequest.balance}")

            val response = walletApi.updateWallet(walletId, dataRequest)

            if (response.isSuccessful) {
                val walletResponse = response.body()
                if (walletResponse != null) {
                    println("✅ [Repository] Wallet updated successfully: id=${walletResponse.id}, name=${walletResponse.name}")
                    Result.success(walletResponse.toDomain())
                } else {
                    println("❌ [Repository] Empty response body from updateWallet")
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                println("❌ [Repository] API call failed with code: ${response.code()}, message: ${response.message()}")
                Result.failure(Exception("Failed to update wallet: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            println("❌ [Repository] EXCEPTION in updateWallet(): ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }
}