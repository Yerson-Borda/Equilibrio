package com.example.moneymate.ui.screens.profile.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.auth.dataStore.DataStoreDataSource
import com.example.domain.user.usecase.LogoutUseCase
import com.example.moneymate.utils.AppError
import com.example.moneymate.utils.ErrorHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class SettingsNavigationEvent {
    object LogoutSuccess : SettingsNavigationEvent()
}

class SettingsScreenViewModel(
    private val logoutUseCase: LogoutUseCase,
    private val dataStoreDataSource: DataStoreDataSource
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsState())
    val uiState: StateFlow<SettingsState> = _uiState.asStateFlow()

    private val _errorState = MutableStateFlow<AppError?>(null)
    val errorState: StateFlow<AppError?> = _errorState.asStateFlow()

    private val _navigationEvent = MutableStateFlow<SettingsNavigationEvent?>(null)
    val navigationEvent: StateFlow<SettingsNavigationEvent?> = _navigationEvent.asStateFlow()

    fun logout() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                // Call logout API
                val result = logoutUseCase()
                if (result.isSuccess) {
                    // Clear local data
                    clearLocalData()
                    _navigationEvent.value = SettingsNavigationEvent.LogoutSuccess
                } else {
                    _errorState.value = ErrorHandler.mapExceptionToAppError(
                        result.exceptionOrNull() ?: Exception("Failed to logout")
                    )
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            } catch (e: Exception) {
                _errorState.value = ErrorHandler.mapExceptionToAppError(e)
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    private suspend fun clearLocalData() {
        // Clear all authentication data from DataStore
        dataStoreDataSource.setAccessToken(null)
        dataStoreDataSource.setRefreshToken(null)
        dataStoreDataSource.setUserId(null)
    }

    fun updateBiometricSetting(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(biometricEnabled = enabled)
        // Here you can save this setting to DataStore if needed
    }

    fun updateDarkModeSetting(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(darkModeEnabled = enabled)
        // Here you can save this setting to DataStore if needed
    }

    fun clearError() {
        _errorState.value = null
    }

    fun clearNavigationEvent() {
        _navigationEvent.value = null
    }
}

data class SettingsState(
    val isLoading: Boolean = false,
    val biometricEnabled: Boolean = false,
    val darkModeEnabled: Boolean = false
)