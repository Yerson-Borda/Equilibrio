package com.example.data.network.transaction

import com.example.data.network.transaction.model.TransactionCreateRequest
import com.example.data.network.transaction.model.TransferCreateRequest
import com.example.data.network.transaction.model.TransactionDto
import com.example.data.network.transaction.model.TransferDto
import com.example.domain.transaction.TransactionRepository
import com.example.domain.transaction.model.TransactionEntity
import com.example.domain.transaction.model.TransferEntity
import retrofit2.Response

class TransactionRepositoryImpl (
    private val apiService: TransactionApi,
    // Remove preferencesManager if not used, or add import if needed
) : TransactionRepository {

    override suspend fun createTransaction(
        amount: Any,
        description: String?,
        note: String?,
        type: String,
        transactionDate: String,
        walletId: Int,
        categoryId: Int
    ): Result<TransactionEntity> {
        return try {
            val request = TransactionCreateRequest.create(
                amount = amount,
                description = description,
                note = note,
                type = type,
                transactionDate = transactionDate,
                walletId = walletId,
                categoryId = categoryId
            )

            val response = apiService.createTransaction(request)
            handleTransactionResponse(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createTransfer(
        sourceWalletId: Int,
        destinationWalletId: Int,
        amount: Any,
        note: String?
    ): Result<TransferEntity> {
        return try {
            val request = TransferCreateRequest.create(
                sourceWalletId = sourceWalletId,
                destinationWalletId = destinationWalletId,
                amount = amount,
                note = note
            )

            val response = apiService.createTransfer(request)
            handleTransferResponse(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getTransactions(): Result<List<TransactionEntity>> {
        return try {
            val response = apiService.getTransactions()
            if (response.isSuccessful) {
                val transactionDtos = response.body()
                if (transactionDtos != null) {
                    Result.success(transactionDtos.map { it.toEntity() })
                } else {
                    Result.success(emptyList())
                }
            } else {
                Result.failure(Exception("API error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getTransactionById(id: Int): Result<TransactionEntity> {
        return try {
            val response = apiService.getTransactionById(id)
            handleTransactionResponse(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteTransaction(id: Int): Result<Unit> {
        return try {
            val response = apiService.deleteTransaction(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("API error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun handleTransactionResponse(response: Response<TransactionDto>): Result<TransactionEntity> {
        return if (response.isSuccessful) {
            val transactionDto = response.body()
            if (transactionDto != null) {
                Result.success(transactionDto.toEntity())
            } else {
                Result.failure(Exception("Failed to create transaction"))
            }
        } else {
            Result.failure(Exception("API error: ${response.code()} - ${response.errorBody()?.string()}"))
        }
    }

    private fun handleTransferResponse(response: Response<TransferDto>): Result<TransferEntity> {
        return if (response.isSuccessful) {
            val transferDto = response.body()
            if (transferDto != null) {
                Result.success(transferDto.toEntity())
            } else {
                Result.failure(Exception("Failed to create transfer"))
            }
        } else {
            Result.failure(Exception("API error: ${response.code()} - ${response.errorBody()?.string()}"))
        }
    }
}

// Extension functions to convert DTO to Entity
private fun TransactionDto.toEntity(): TransactionEntity {
    return TransactionEntity(
        id = id,
        amount = amount,
        note = note,
        type = type,
        transactionDate = transactionDate,
        walletId = walletId,
        categoryId = categoryId,
        userId = userId,
        createdAt = createdAt
    )
}

private fun TransferDto.toEntity(): TransferEntity {
    return TransferEntity(
        message = message,
        sourceTransaction = sourceTransaction.toEntity(),
        destinationTransaction = destinationTransaction.toEntity(),
        exchangeRate = exchangeRate,
        convertedAmount = convertedAmount
    )
}