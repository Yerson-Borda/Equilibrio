package com.example.data.network.auth.model

import kotlinx.serialization.Serializable
@Serializable
data class AuthRequest(
    val fullName: String? = null,
    val email: String,
    val password: String
)
