package com.example.domain.auth.dataSource.model

data class AccessToken(
val accessToken: String,
val tokenType: String,
val refreshToken: String? = null
)
