package com.example.data.network.wallet


import com.example.data.network.wallet.model.TotalBalanceResponse
import com.example.data.network.wallet.model.WalletBalanceResponse
import com.example.data.network.wallet.model.WalletCreateRequest
import com.example.data.network.wallet.model.WalletResponse
import com.example.data.network.wallet.model.WalletUpdateRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface WalletApi {
    @GET("api/wallets/")
    suspend fun getWallets(): List<WalletResponse>

    @POST("api/wallets/")
    suspend fun createWallet(@Body request: WalletCreateRequest): WalletResponse

    @GET("api/wallets/user/total")
    suspend fun getTotalBalance(): TotalBalanceResponse

    @GET("api/wallets/{wallet_id}")
    suspend fun getWalletDetail(@Path("wallet_id") walletId: Int): WalletResponse

    @DELETE("api/wallets/{wallet_id}")
    suspend fun deleteWallet(@Path("wallet_id") walletId: Int): Response<Unit>

    @PUT("api/wallets/{wallet_id}") suspend fun updateWallet(
        @Path("wallet_id") walletId: Int,
        @Body walletRequest: WalletUpdateRequest
    ): Response<WalletResponse>

    @GET("api/wallets/{wallet_id}/balance")
    suspend fun getWalletBalance(@Path("wallet_id") walletId: Int): WalletBalanceResponse
}