package com.example.data.network.auth

import com.example.data.network.auth.model.SignInRequest
import com.example.data.network.auth.model.SignUpRequest
import com.example.domain.auth.dataSource.AuthRemoteDataSource
import com.example.domain.auth.dataSource.model.AccessToken
import com.example.domain.auth.usecase.model.UserData

class AuthRemoteDataSourceImpl(
    private val authApi: AuthApi
): AuthRemoteDataSource {

    override suspend fun signUp(
        fullName: String?,
        email: String,
        password: String
    ): UserData { // Change return type to UserData
        val request = SignUpRequest(
            full_name = fullName ?: "",
            email = email,
            password = password,
            default_currency = "USD" // Add default currency
        )
        val response = authApi.signUp(request)
        return UserData(
            id = response.id,
            email = response.email,
            fullName = response.full_name,
            defaultCurrency = response.default_currency,
            isActive = response.is_active,
            createdAt = response.created_at
        )
    }

    override suspend fun signIn(email: String, password: String): AccessToken {
        // Directly pass email and password as query parameters
        val response = authApi.signIn(email = email, password = password)
        return AccessToken(response.accessToken , response.tokenType)
    }
}