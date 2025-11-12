package com.example.data.network.user.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateUserRequest(
    @SerialName("fullName") val fullName: String,
    @SerialName("email") val email: String,
    @SerialName("phoneNumber") val phoneNumber: String?,
    @SerialName("birthDate") val birthDate: String?
)