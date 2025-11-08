package com.example.domain.home.usecase

import com.example.domain.home.UserRepository
import com.example.domain.home.model.UserDetailedData

class GetUserDetailedUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): UserDetailedData {
        return userRepository.getUserDetailed()
    }
}