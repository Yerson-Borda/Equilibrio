// data/src/main/java/com/example/data/network/user/model/UpdateUserRequest.kt
package com.example.data.network.user.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateUserRequest(
    @SerialName("email") val email: String? = null,
    @SerialName("full_name") val fullName: String? = null,
    @SerialName("phone_number") val phoneNumber: String? = null,
    @SerialName("date_of_birth") val dateOfBirth: String? = null,
    @SerialName("default_currency") val defaultCurrency: String? = null,
    @SerialName("password") val password: String? = null
)