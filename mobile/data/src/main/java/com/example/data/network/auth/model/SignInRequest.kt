package com.example.data.network.auth.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SignInRequest(
    @SerialName("Email")
    val email: String,

    @SerialName("Password")
    val password: String
)
