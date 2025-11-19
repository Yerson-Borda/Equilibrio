package com.example.domain.auth.usecase.model

data class UserData(
    val id: Int,
    val email: String,
    val fullName: String?,
    val phoneNumber: String?,
    val dateOfBirth: String?,
    val avatarUrl: String?,
    val defaultCurrency: String,
    val isActive: Boolean,
    val createdAt: String
)