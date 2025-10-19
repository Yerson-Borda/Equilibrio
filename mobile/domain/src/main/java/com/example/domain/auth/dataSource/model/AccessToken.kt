package com.example.domain.auth.dataSource.model

data class AccessToken(
val accessToken: String,
val refreshToken: String,
val userId: String
)
