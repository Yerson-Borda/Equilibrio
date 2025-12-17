package com.example.domain.auth.dataSource

import com.example.domain.auth.dataSource.model.AccessToken
import com.example.domain.auth.usecase.model.UserData

interface AuthRemoteDataSource {
    suspend fun signUp(
        fullName: String?,
        email: String,
        password: String,
        phoneNumber: String? = null,
        dateOfBirth: String? = null,
        avatarUrl: String? = null,
        defaultCurrency: String = "USD"
    ): UserData

    suspend fun signIn(email: String, password: String): AccessToken
}