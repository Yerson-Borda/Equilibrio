package com.example.moneymate.ui.screens.auth.singIn

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.auth.usecase.SignInUseCase
import com.example.domain.auth.usecase.model.SignInInfo
import com.example.moneymate.utils.ErrorHandler
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SignInViewModel(
    private val signInUseCase: SignInUseCase
) : ViewModel() {
    private val _navigateToHome = MutableSharedFlow<Unit>()
    val navigateToHome = _navigateToHome.asSharedFlow()

    private val _showError = MutableSharedFlow<String>()
    val showError = _showError.asSharedFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                validateInput(email, password)

                signInUseCase(
                    SignInInfo(
                        email = email,
                        password = password
                    )
                ).also {
                    _navigateToHome.emit(Unit)
                }
            } catch (e: Exception) {
                handleError(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun validateInput(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            throw IllegalArgumentException("Please fill in all fields")
        }

        if (!isValidEmail(email)) {
            throw IllegalArgumentException("Please enter a valid email address")
        }
    }

    private suspend fun handleError(exception: Exception) {
        val appError = ErrorHandler.mapExceptionToAppError(exception)
        _showError.emit(appError.getUserFriendlyMessage())
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}