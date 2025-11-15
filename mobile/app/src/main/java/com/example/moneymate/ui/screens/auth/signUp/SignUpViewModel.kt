package com.example.moneymate.ui.screens.auth.signUp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.auth.usecase.SignUpUseCase
import com.example.domain.auth.usecase.model.SignUpInfo
import com.example.moneymate.utils.AppError
import com.example.moneymate.utils.ErrorHandler
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SignUpViewModel(
    private val signUpUseCase: SignUpUseCase
) : ViewModel() {
    private val _navigateToProfile = MutableSharedFlow<Unit>()
    val navigateToProfile = _navigateToProfile.asSharedFlow()

    private val _showError = MutableSharedFlow<String>()
    val showError = _showError.asSharedFlow()

    private val _showEmailExistsDialog = MutableSharedFlow<Unit>()
    val showEmailExistsDialog = _showEmailExistsDialog.asSharedFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun signUp(fullName: String, email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                validateInput(fullName, email, password)

                signUpUseCase(
                    SignUpInfo(
                        fullName = fullName,
                        email = email,
                        password = password
                    )
                ).also {
                    _navigateToProfile.emit(Unit)
                }
            } catch (e: Exception) {
                handleError(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun validateInput(fullName: String, email: String, password: String) {
        if (fullName.isBlank() || email.isBlank() || password.isBlank()) {
            throw IllegalArgumentException("Please fill in all fields")
        }

        if (!isValidEmail(email)) {
            throw IllegalArgumentException("Please enter a valid email address")
        }

        if (password.length < 6) {
            throw IllegalArgumentException("Password should be at least 6 characters")
        }
    }

    private suspend fun handleError(exception: Exception) {
        val appError = ErrorHandler.mapExceptionToAppError(exception)

        when (appError) {
            is AppError.EmailExistsError -> _showEmailExistsDialog.emit(Unit)
            else -> _showError.emit(appError.getUserFriendlyMessage())
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}