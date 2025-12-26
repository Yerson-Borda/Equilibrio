package com.example.data.network.auth

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
        password: String,
        phoneNumber: String?,
        dateOfBirth: String?,
        avatarUrl: String?,
        defaultCurrency: String
    ): UserData {
        val request = SignUpRequest(
            email = email,
            full_name = fullName,
            phone_number = phoneNumber,
            date_of_birth = dateOfBirth,
            avatar_url = avatarUrl,
            default_currency = defaultCurrency,
            password = password
        )
        val response = authApi.signUp(request)
        return UserData(
            id = response.id,
            email = response.email,
            fullName = response.full_name,
            phoneNumber = response.phone_number,
            dateOfBirth = response.date_of_birth,
            avatarUrl = response.avatar_url,
            defaultCurrency = response.default_currency,
            isActive = response.is_active,
            createdAt = response.created_at
        )
    }

    override suspend fun signIn(email: String, password: String): AccessToken {
        // Directly pass email and password as query parameters
        val response = authApi.signIn(email = email, password = password)
        return AccessToken(response.accessToken, response.tokenType)
    }
}