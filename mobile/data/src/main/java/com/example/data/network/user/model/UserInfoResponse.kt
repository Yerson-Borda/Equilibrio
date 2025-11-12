package com.example.data.network.user.model

import com.example.domain.user.model.User
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserInfoResponse(
    @SerialName("id") val id: String,
    @SerialName("fullName") val fullName: String,
    @SerialName("email") val email: String,
    @SerialName("phoneNumber") val phoneNumber: String?,
    @SerialName("birthDate") val birthDate: String?,
    @SerialName("avatarUrl") val avatarUrl: String?
) {
    fun toDomain(): User {
        return User(
            id = id,
            fullName = fullName,
            email = email,
            phoneNumber = phoneNumber,
            birthDate = birthDate,
            avatarUrl = avatarUrl
        )
    }
}