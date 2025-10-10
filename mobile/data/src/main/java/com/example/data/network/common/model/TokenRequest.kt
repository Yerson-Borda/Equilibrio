package com.example.data.network.common.model

import kotlinx.serialization.Serializable


@Serializable
data class TokenRequest(
    val refreshToken: String
)