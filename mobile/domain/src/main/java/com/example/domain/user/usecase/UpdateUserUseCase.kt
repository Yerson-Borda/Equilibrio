package com.example.domain.user.usecase

import com.example.domain.user.UserRepository
import com.example.domain.user.model.User

class UpdateUserUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(user: User): Result<User> {
        return userRepository.updateUser(user)
    }
}