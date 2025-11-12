// data/src/main/java/com/example/data/network/user/mapper/UserResponseMapper.kt
package com.example.data.network.user.mapper

import com.example.data.network.user.model.UserResponse
import com.example.domain.user.model.User

object UserResponseMapper {

    fun toDomain(userResponse: UserResponse): User {
        return User(
            id = userResponse.id.toString(),
            fullName = userResponse.fullName ?: "",
            email = userResponse.email,
            phoneNumber = userResponse.phoneNumber,
            birthDate = userResponse.dateOfBirth,
            avatarUrl = userResponse.avatarUrl
        )
    }
}