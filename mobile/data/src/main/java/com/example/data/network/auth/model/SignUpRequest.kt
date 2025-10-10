package com.example.data.network.auth.model
import kotlinx.serialization.Serializable

@Serializable
data class SignUpRequest(
    val email: String,
    val full_name: String? = null,
    val password: String,
    val default_currency: String = "USD"
)
