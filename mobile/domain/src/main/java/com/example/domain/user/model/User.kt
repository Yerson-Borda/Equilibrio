package com.example.domain.user.model

data class User(
    val id: String,
    val fullName: String,
    val email: String,
    val phoneNumber: String?,
    val birthDate: String?,
    val avatarUrl: String?
)
