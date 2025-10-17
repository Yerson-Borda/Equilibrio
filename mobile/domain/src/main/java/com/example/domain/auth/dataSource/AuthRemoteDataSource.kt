package com.example.domain.auth.dataSource

import com.example.domain.auth.dataSource.model.AccessToken
import com.example.domain.auth.usecase.model.UserData

interface AuthRemoteDataSource {
    suspend fun signUp(
        fullName: String?,
        email: String,
        password: String
    ): UserData

    suspend fun signIn(email: String, password: String): AccessToken
}

