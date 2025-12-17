package com.example.domain.user.usecase

import com.example.domain.user.UserRepository
import com.example.domain.user.model.User

class UploadAvatarUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(avatarUri: String): Result<User> {
        return userRepository.uploadAvatar(avatarUri)
    }
}