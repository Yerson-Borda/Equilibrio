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
    override fun isUserSignedIn() = dataStoreDataSource.accessToken.map { token ->
        token != null
    }

    override suspend fun signUp(signUpInfo: SignUpInfo): UserData {
        // Clear any existing authentication data first
        dataStoreDataSource.setAccessToken(null)
        dataStoreDataSource.setRefreshToken(null)
        dataStoreDataSource.setUserId(null)

        // Step 1: Sign up the user
        val userData = remoteDataSource.signUp(
            fullName = signUpInfo.fullName,
            email = signUpInfo.email,
            password = signUpInfo.password,
            phoneNumber = signUpInfo.phoneNumber,
            dateOfBirth = signUpInfo.dateOfBirth,
            avatarUrl = signUpInfo.avatarUrl,
            defaultCurrency = signUpInfo.defaultCurrency
        )

        // Step 2: Automatically sign in after successful sign-up
        val tokens = remoteDataSource.signIn(signUpInfo.email, signUpInfo.password)

        // Step 3: Store the authentication tokens
        dataStoreDataSource.setAccessToken(tokens.accessToken)
        dataStoreDataSource.setRefreshToken(tokens.refreshToken)
        dataStoreDataSource.setUserId(userData.id)

        return userData
    }

    override suspend fun signIn(signInInfo: SignInInfo): AccessToken {
        // Clear any existing authentication data first
        dataStoreDataSource.setAccessToken(null)
        dataStoreDataSource.setRefreshToken(null)
        dataStoreDataSource.setUserId(null)

        val tokens = remoteDataSource.signIn(signInInfo.email, signInInfo.password)

        // Store the authentication tokens
        dataStoreDataSource.setAccessToken(tokens.accessToken)
        dataStoreDataSource.setRefreshToken(tokens.refreshToken)
        // Note: You might want to fetch and store user ID here as well
        // For now, we'll store it when we get user profile data

        return tokens
    }

    override suspend fun getCurrentUserId(): String? {
        return dataStoreDataSource.getUserId() ?: throw Exception("User not authenticated")
    }
}