package com.example.data.network.transaction
import com.example.data.network.transaction.model.TransactionCreateRequest
import com.example.data.network.transaction.model.TransactionDto
import com.example.data.network.transaction.model.TransferCreateRequest
import com.example.data.network.transaction.model.TransferDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface TransactionApi {

    @POST("api/transactions/")
    suspend fun createTransaction(@Body request: TransactionCreateRequest): Response<TransactionDto>

    @POST("api/transactions/transfer")
    suspend fun createTransfer(@Body request: TransferCreateRequest): Response<TransferDto>

    @GET("api/transactions/")
    suspend fun getTransactions(): Response<List<TransactionDto>>

    @GET("api/transactions/wallet/{wallet_id}")
    suspend fun getTransactionsByWalletId(@Path("wallet_id") walletId: Int): Response<List<TransactionDto>>

    @DELETE("api/transactions/{transaction_id}")
    suspend fun deleteTransaction(@Path("id") id: Int): Response<Unit>
}