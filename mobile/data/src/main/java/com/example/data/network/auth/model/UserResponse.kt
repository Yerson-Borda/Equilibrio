package com.example.data.network.auth.model

import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    val email: String,
    val full_name: String? = null,
    val default_currency: String,
    val id: Int,
    val is_active: Boolean,
    val created_at: String
)