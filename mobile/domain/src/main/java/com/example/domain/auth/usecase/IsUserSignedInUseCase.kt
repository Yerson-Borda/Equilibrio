package com.example.domain.auth.usecase

import com.example.domain.auth.AuthRepository
import kotlinx.coroutines.flow.Flow

interface IsUserSignedInUseCase {
    fun invoke(): Flow<Boolean>
}

class IsUserSignedInUseCaseImpl(
    private val authRepository: AuthRepository
) : IsUserSignedInUseCase {
    override fun invoke() = authRepository.isUserSignedIn()
}
