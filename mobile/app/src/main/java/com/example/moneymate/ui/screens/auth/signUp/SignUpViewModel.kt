package com.example.moneymate.ui.screens.auth.signUp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.auth.usecase.SignUpUseCase
import com.example.domain.auth.usecase.model.SignUpInfo
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
                if (fullName.isBlank() || email.isBlank() || password.isBlank()) {
                    throw IllegalArgumentException("Please fill all fields")
                }

                if (!isValidEmail(email)) {
                    throw IllegalArgumentException("Please enter a valid email address")
                }

                if (password.length < 6) {
                    throw IllegalArgumentException("Password should be at least 6 characters")
                }

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
                if (isEmailExistsError(e)) {
                    _showEmailExistsDialog.emit(Unit)
                } else {
                    _showError.emit(e.message ?: "Registration failed")
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun isEmailExistsError(e: Exception): Boolean {
        return when {
            e.message?.contains("email", ignoreCase = true) == true -> true
            e.message?.contains("exists", ignoreCase = true) == true -> true
            e.message?.contains("already", ignoreCase = true) == true -> true
            e.message?.contains("duplicate", ignoreCase = true) == true -> true
            e.message?.contains("500", ignoreCase = true) == true -> true
            e.message?.contains("internal server error", ignoreCase = true) == true -> true
            // For now, assume ALL 500 errors are email exists errors (temporary solution)
            e.message?.contains("registration", ignoreCase = true) == true -> true
            else -> false
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}