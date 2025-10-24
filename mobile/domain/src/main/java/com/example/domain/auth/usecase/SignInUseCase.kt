package com.example.domain.auth.usecase

import com.example.domain.auth.AuthRepository
import com.example.domain.auth.usecase.model.SignInInfo

interface SignInUseCase {
    suspend operator fun invoke(signInInfo: SignInInfo)
}

class SignInUseCaseImpl(
    private val authRepository: AuthRepository
) : SignInUseCase {
    override suspend fun invoke(signInInfo: SignInInfo) {
        authRepository.signIn(signInInfo)
    }
}