package com.example.moneymate.utils

import android.util.Log
import com.example.domain.common.MoneyMateError
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object ErrorHandler {
    fun mapExceptionToAppError(exception: Exception): AppError {
        Log.d("ErrorHandler", "Exception type: ${exception.javaClass.simpleName}")

        return when (exception) {
            is HttpException -> {
                Log.d("ErrorHandler", "HTTP Exception code: ${exception.code()}")
                handleHttpException(exception)
            }
            is MoneyMateError -> { // Add this case
                Log.d("ErrorHandler", "MoneyMateError: ${exception.message}")
                AppError.HttpError(0, exception.message ?: "Unknown error")
            }
            is SocketTimeoutException -> AppError.NetworkError("Request timed out. Please check your connection")
            is UnknownHostException -> AppError.NetworkError("No internet connection")
            is IOException -> AppError.NetworkError("Network error. Please check your connection")
            is IllegalArgumentException -> AppError.ValidationError(exception.message ?: "Invalid input")
            else -> AppError.UnknownError(exception.message ?: "An unexpected error occurred")
        }
    }

    private fun handleHttpException(exception: HttpException): AppError {
        val errorCode = exception.code()
        val errorMessage = parseErrorMessage(exception)

        return when (errorCode) {
            400 -> {
                when {
                    errorMessage.contains("email", ignoreCase = true) -> AppError.EmailExistsError
                    else -> AppError.HttpError(errorCode, "Invalid request. Please check your input")
                }
            }
            401 -> AppError.HttpError(errorCode, "Invalid email or password")
            403 -> AppError.HttpError(errorCode, "Access denied")
            404 -> AppError.HttpError(errorCode, "Service not found")
            409 -> AppError.EmailExistsError // Conflict - email exists
            422 -> AppError.HttpError(errorCode, "Invalid input data")
            500 -> AppError.HttpError(errorCode, "Server is temporarily unavailable. Please try again later")
            in 400..499 -> AppError.HttpError(errorCode, "Invalid request. Please check your input")
            in 500..599 -> AppError.HttpError(errorCode, "Server error. Please try again later")
            else -> AppError.HttpError(errorCode, "Network error occurred")
        }
    }

    private fun parseErrorMessage(exception: HttpException): String {
        return try {
            exception.response()?.errorBody()?.string() ?: ""
        } catch (e: Exception) {
            ""
        }
    }
}