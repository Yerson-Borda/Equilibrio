package com.example.domain.auth.usecase

import android.util.Log
import com.example.domain.accessToken.AccessTokenRepository
import com.example.domain.auth.AuthRepository
import com.example.domain.auth.usecase.model.SignInInfo

interface SignInUseCase {
    suspend operator fun invoke(signInInfo: SignInInfo)
}

class SignInUseCaseImpl(
    private val authRepository: AuthRepository,
    private val accessTokenRepository: AccessTokenRepository // Add this dependency
) : SignInUseCase {
    override suspend fun invoke(signInInfo: SignInInfo) {
        val accessToken = authRepository.signIn(signInInfo)

        // Store the access token
        accessTokenRepository.setAccessToken(accessToken.accessToken)

        // DEBUG: Verify storage
        val storedToken = accessTokenRepository.getAccessToken()
        Log.d("SignInUseCase", "Token stored: ${!storedToken.isNullOrBlank()}")
        Log.d("SignInUseCase", "Stored token preview: ${storedToken?.take(10)}...")
    }
}