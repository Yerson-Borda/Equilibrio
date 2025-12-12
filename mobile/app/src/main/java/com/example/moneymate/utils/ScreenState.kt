package com.example.moneymate.utils

sealed class ScreenState<out T> {
    object Loading : ScreenState<Nothing>()
    data class Success<T>(val data: T) : ScreenState<T>()
    data class Error(val error: AppError, val retryAction: (() -> Unit)? = null) : ScreenState<Nothing>()
    object Empty : ScreenState<Nothing>()

    val isLoading: Boolean get() = this is Loading
    val isError: Boolean get() = this is Error
    val isSuccess: Boolean get() = this is Success
    val isEmpty: Boolean get() = this is Empty
}