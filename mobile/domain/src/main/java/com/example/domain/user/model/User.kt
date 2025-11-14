package com.example.domain.user.model

data class User(
    val id: String,
    val email: String,
    val fullName: String?,
    val phoneNumber: String?,
    val dateOfBirth: String?,
    val avatarUrl: String?,
    val defaultCurrency: String,
    val isActive: Boolean,
    val createdAt: String
)
