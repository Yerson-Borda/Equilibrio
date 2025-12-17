package com.example.domain.auth.usecase

import com.example.domain.auth.AuthRepository

interface GetCurrentUserIdUseCase {
    suspend operator fun invoke(): String
}

class GetCurrentUserIdUseCaseImpl(
    private val authRepository: AuthRepository
) : GetCurrentUserIdUseCase {
    override suspend fun invoke(): String {
        return authRepository.getCurrentUserId()
            ?: throw IllegalStateException("User not logged in")
    }
}