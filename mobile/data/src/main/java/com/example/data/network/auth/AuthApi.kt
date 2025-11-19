package com.example.data.network.auth

import com.example.data.network.auth.model.SignUpRequest
import com.example.data.network.auth.model.SignUpResponse
import com.example.data.network.auth.model.TokenResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthApi {
    @POST("api/auth/register")
    suspend fun signUp(@Body signUpRequest: SignUpRequest): SignUpResponse

    @POST("api/auth/login")
    suspend fun signIn(
        @Query("email") email: String,
        @Query("password") password: String
    ): TokenResponse
}