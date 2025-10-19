package com.example.moneymate.ui.screens.auth.singIn

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.auth.usecase.SignInUseCase
import com.example.domain.auth.usecase.model.SignInInfo
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
                if (email.isBlank() || password.isBlank()) {
                    throw IllegalArgumentException("Please fill all fields")
                }

                if (!isValidEmail(email)) {
                    throw IllegalArgumentException("Please enter a valid email address")
                }

                signInUseCase(
                    SignInInfo(
                        email = email,
                        password = password
                    )
                ).also {
                    _navigateToHome.emit(Unit)
                }
            } catch (e: Exception) {
                _showError.emit(e.message ?: "Login failed")
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}