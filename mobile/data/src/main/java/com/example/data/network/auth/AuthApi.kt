package com.example.data.network.auth

import com.example.data.network.auth.model.SignUpRequest
import com.example.data.network.auth.model.TokenResponse
import com.example.data.network.auth.model.UserResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthApi {
    @POST("api/v1/users/register")
    suspend fun signUp(@Body signUpRequest: SignUpRequest): UserResponse

    @POST("api/v1/users/login")
    suspend fun signIn(
        @Query("email") email: String,
        @Query("password") password: String
    ): TokenResponse
}