package com.example.moneymate.utils

sealed class AppError {
    data class NetworkError(val message: String) : AppError()
    data class HttpError(val code: Int, val message: String) : AppError()
    data class ValidationError(val message: String) : AppError()
    data class InsufficientBalanceError(val currentBalance: Double, val requestedAmount: Double) : AppError()
    object EmailExistsError : AppError()
    data class UnknownError(val message: String) : AppError()

    fun getUserFriendlyMessage(): String {
        return when (this) {
            is NetworkError -> message
            is HttpError -> message
            is ValidationError -> message
            is InsufficientBalanceError ->
                "Insufficient balance. Your wallet has $${"%.2f".format(currentBalance)} but you're trying to spend $${"%.2f".format(requestedAmount)}"
            is EmailExistsError -> "This email is already registered. Please try logging in."
            is UnknownError -> "An unexpected error occurred. Please try again."
        }
    }
}