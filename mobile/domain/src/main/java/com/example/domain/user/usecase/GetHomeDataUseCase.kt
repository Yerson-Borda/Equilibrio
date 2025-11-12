package com.example.domain.user.usecase

import com.example.domain.user.UserRepository
import com.example.domain.user.model.UserDetailedData

class GetUserDetailedUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): UserDetailedData {
        return userRepository.getUserDetailed()
    }
}