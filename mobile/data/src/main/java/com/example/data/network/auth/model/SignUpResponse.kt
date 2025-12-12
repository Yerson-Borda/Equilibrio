package com.example.data.network.auth.model

import kotlinx.serialization.Serializable

@Serializable
data class SignUpResponse(
    val id: Int,
    val email: String,
    val full_name: String?,
    val phone_number: String?,
    val date_of_birth: String?,
    val avatar_url: String?,
    val default_currency: String,
    val is_active: Boolean,
    val created_at: String
)