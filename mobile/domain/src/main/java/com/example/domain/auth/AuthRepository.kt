package com.example.domain.auth

import com.example.domain.auth.dataSource.AuthRemoteDataSource
import com.example.domain.auth.dataSource.model.AccessToken
import com.example.domain.auth.dataStore.DataStoreDataSource
import com.example.domain.auth.usecase.model.SignInInfo
import com.example.domain.auth.usecase.model.SignUpInfo
import com.example.domain.auth.usecase.model.UserData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface AuthRepository {
    fun isUserSignedIn(): Flow<Boolean>
    suspend fun signUp(signUpInfo: SignUpInfo): UserData
    suspend fun signIn(signInInfo: SignInInfo): AccessToken
    suspend fun getCurrentUserId(): String?
}

class AuthRepositoryImpl(
    private val dataStoreDataSource: DataStoreDataSource,
    private val remoteDataSource: AuthRemoteDataSource,
) : AuthRepository {
    override fun isUserSignedIn() = dataStoreDataSource.accessToken.map { token->
        token != null
    }

    override suspend fun signUp(signUpInfo: SignUpInfo): UserData {
        return remoteDataSource.signUp(
            fullName = signUpInfo.fullName,
            email = signUpInfo.email,
            password = signUpInfo.password,
            phoneNumber = signUpInfo.phoneNumber,
            dateOfBirth = signUpInfo.dateOfBirth,
            avatarUrl = signUpInfo.avatarUrl,
            defaultCurrency = signUpInfo.defaultCurrency
        )
    }

    override suspend fun signIn(signInInfo: SignInInfo): AccessToken {
        return remoteDataSource.signIn(signInInfo.email, signInInfo.password)
    }

    override suspend fun getCurrentUserId(): String? {
        return dataStoreDataSource.getUserId() ?: throw Exception("User not authenticated")
    }
}