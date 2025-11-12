// data/src/main/java/com/example/data/network/user/mapper/UserInfoResponseMapper.kt
package com.example.data.network.user.mapper

import com.example.data.network.user.model.UserInfoResponse
import com.example.domain.user.model.User

object UserInfoResponseMapper {

    fun toDomain(userInfoResponse: UserInfoResponse): User {
        return User(
            id = userInfoResponse.id.toString(),
            fullName = userInfoResponse.fullName ?: "",
            email = userInfoResponse.email,
            phoneNumber = userInfoResponse.phoneNumber,
            birthDate = userInfoResponse.dateOfBirth,
            avatarUrl = userInfoResponse.avatarUrl
        )
    }
}