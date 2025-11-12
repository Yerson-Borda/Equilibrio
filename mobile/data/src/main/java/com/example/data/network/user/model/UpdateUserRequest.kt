// data/src/main/java/com/example/data/network/user/model/UpdateUserRequest.kt
package com.example.data.network.user.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateUserRequest(
    @SerialName("email") val email: String,
    @SerialName("full_name") val fullName: String?,
    @SerialName("phone_number") val phoneNumber: String?,
    @SerialName("date_of_birth") val dateOfBirth: String?
)