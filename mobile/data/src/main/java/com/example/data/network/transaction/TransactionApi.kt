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

    @POST("api/v1/transactions/")
    suspend fun createTransaction(@Body request: TransactionCreateRequest): Response<TransactionDto>

    @POST("api/v1/transactions/transfer")
    suspend fun createTransfer(@Body request: TransferCreateRequest): Response<TransferDto>

    @GET("transactions")
    suspend fun getTransactions(): Response<List<TransactionDto>>

    @GET("transactions/{id}")
    suspend fun getTransactionById(@Path("id") id: Int): Response<TransactionDto>

    @DELETE("transactions/{id}")
    suspend fun deleteTransaction(@Path("id") id: Int): Response<Unit>
}