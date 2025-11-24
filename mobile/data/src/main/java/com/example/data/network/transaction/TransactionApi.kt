package com.example.data.network.transaction
import com.example.data.network.transaction.model.CategorySummaryResponse
import com.example.data.network.transaction.model.MonthlyComparisonResponse
import com.example.data.network.transaction.model.SpendingTrendsResponse
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
import retrofit2.http.Query

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


    @GET("api/analytics/spending-trends")
    suspend fun getSpendingTrends(
        @Query("months") months: Int
    ): Response<SpendingTrendsResponse>

    // ADD NEW ENDPOINTS
    @GET("api/analytics/category-summary")
    suspend fun getCategorySummary(
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String
    ): Response<CategorySummaryResponse>

    @GET("api/analytics/monthly-comparison")
    suspend fun getMonthlyComparison(
        @Query("month") month: String
    ): Response<List<MonthlyComparisonResponse>>
}