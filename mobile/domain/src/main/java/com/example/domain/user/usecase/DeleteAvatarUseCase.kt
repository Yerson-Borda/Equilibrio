package com.example.domain.user.usecase

import com.example.domain.user.UserRepository
import com.example.domain.user.model.User

class DeleteAvatarUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): Result<User> {
        return userRepository.deleteAvatar()
    }
}