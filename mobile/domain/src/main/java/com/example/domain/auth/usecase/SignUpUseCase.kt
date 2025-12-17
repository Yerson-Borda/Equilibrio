package com.example.domain.auth.usecase

import com.example.domain.auth.AuthRepository
import com.example.domain.auth.usecase.model.SignUpInfo
import com.example.domain.auth.usecase.model.UserData


interface SignUpUseCase {
    suspend operator fun invoke(signUpInfo: SignUpInfo): UserData
}

class SignUpUseCaseImpl(
    private val authRepository: AuthRepository
) : SignUpUseCase {
    override suspend fun invoke(signUpInfo: SignUpInfo): UserData {
        return authRepository.signUp(signUpInfo)
    }
}
