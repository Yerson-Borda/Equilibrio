package com.example.moneymate.ui.screens.profile.profileoptions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.user.model.User
import com.example.domain.user.usecase.GetUserUseCase
import com.example.domain.user.usecase.LogoutUseCase
import com.example.moneymate.utils.AppError
import com.example.moneymate.utils.ErrorHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileOptionsScreenViewModel(
    private val getUserUseCase: GetUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileOptionsState())
    val uiState: StateFlow<ProfileOptionsState> = _uiState.asStateFlow()

    private val _errorState = MutableStateFlow<AppError?>(null)
    val errorState: StateFlow<AppError?> = _errorState.asStateFlow()

    init {
        loadUserData()
    }

    fun loadUserData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val result = getUserUseCase()
                if (result.isSuccess) {
                    val user = result.getOrThrow()
                    _uiState.value = _uiState.value.copy(
                        user = user,
                        isLoading = false
                    )
                } else {
                    _errorState.value = ErrorHandler.mapExceptionToAppError(
                        result.exceptionOrNull() ?: Exception("Failed to load user data")
                    )
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            } catch (e: Exception) {
                _errorState.value = ErrorHandler.mapExceptionToAppError(e)
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
    fun clearError() {
        _errorState.value = null
    }
}

data class ProfileOptionsState(
    val user: User? = null,
    val isLoading: Boolean = false
)

sealed class NavigationEvent {
    object LogoutSuccess : NavigationEvent()
}