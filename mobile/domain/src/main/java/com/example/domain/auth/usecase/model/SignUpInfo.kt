package com.example.domain.auth.usecase.model

data class SignUpInfo(
    val fullName: String,
    val email: String,
    val password: String,
    val phoneNumber: String? = null,
    val dateOfBirth: String? = null,
    val avatarUrl: String? = null,
    val defaultCurrency: String = "USD"
)