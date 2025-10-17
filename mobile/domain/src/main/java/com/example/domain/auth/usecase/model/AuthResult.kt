package com.example.domain.auth.usecase.model

import com.example.domain.auth.dataSource.model.AccessToken

sealed class AuthResult {
    data class Success(val tokens: AccessToken) : AuthResult()
    data class RegistrationSuccess(val userData: UserData) : AuthResult()
}