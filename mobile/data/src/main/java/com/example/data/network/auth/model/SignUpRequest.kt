package com.example.data.network.auth.model
import kotlinx.serialization.Serializable

@Serializable
data class SignUpRequest(
    val email: String,
    val full_name: String? = null,
    val phone_number: String? = null,
    val date_of_birth: String? = null,
    val avatar_url: String? = null,
    val default_currency: String = "USD",
    val password: String
)
