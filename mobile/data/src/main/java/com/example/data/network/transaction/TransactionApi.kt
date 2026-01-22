package com.example.data.network.transaction
import com.example.data.network.transaction.model.AverageSpendingResponse
import com.example.data.network.transaction.model.CategorySummaryResponse
import com.example.data.network.transaction.model.MonthlyComparisonResponse
import com.example.data.network.transaction.model.SpendingTrendsResponse
import com.example.data.network.transaction.model.TopCategoryResponse
import com.example.data.network.transaction.model.TransactionDto
import com.example.data.network.transaction.model.TransferCreateRequest
import com.example.data.network.transaction.model.TransferDto
import com.example.data.network.transaction.model.TransferPreviewResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface TransactionApi {

    @Multipart
    @POST("api/transactions/")
    suspend fun createTransaction(
        @Part("name") name: RequestBody,
        @Part("amount") amount: RequestBody,
        @Part("type") type: RequestBody,
        @Part("transaction_date") transactionDate: RequestBody,
        @Part("wallet_id") walletId: RequestBody,
        @Part("category_id") categoryId: RequestBody,
        @Part("note") note: RequestBody?,
        @Part("tags") tags: RequestBody?,
        @Part receipt: MultipartBody.Part?
    ): Response<TransactionDto>

    @POST("api/transactions/transfer")
    suspend fun createTransfer(@Body request: TransferCreateRequest): Response<TransferDto>

    @GET("api/transactions/transfer/preview")
    suspend fun getTransferPreview(
        @Query("source_wallet_id") sourceWalletId: Int,
        @Query("destination_wallet_id") destinationWalletId: Int,
        @Query("amount") amount: String
    ): Response<TransferPreviewResponse>

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

    @GET("api/analytics/top-categories/current-month")
    suspend fun getTopCategoriesCurrentMonth(): Response<List<TopCategoryResponse>>

    @GET("api/analytics/average-spending")
    suspend fun getAverageSpending(
        @Query("period") period: String // "day", "month", or "year"
    ): Response<List<AverageSpendingResponse>>
}