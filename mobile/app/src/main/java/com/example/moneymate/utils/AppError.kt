package com.example.moneymate.utils

sealed class AppError {
    data class NetworkError(val message: String) : AppError()
    data class HttpError(val code: Int, val message: String) : AppError()
    data class ValidationError(val message: String) : AppError()
    object EmailExistsError : AppError()
    data class UnknownError(val message: String) : AppError()

    fun getUserFriendlyMessage(): String {
        return when (this) {
            is NetworkError -> message
            is HttpError -> message
            is ValidationError -> message
            is EmailExistsError -> "This email is already registered. Please try logging in."
            is UnknownError -> "An unexpected error occurred. Please try again."
        }
    }
}