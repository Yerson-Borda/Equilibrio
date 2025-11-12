package com.example.moneymate.ui.screens.profile.editprofile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.user.model.User
import com.example.domain.user.usecase.DeleteAvatarUseCase
import com.example.domain.user.usecase.GetUserUseCase
import com.example.domain.user.usecase.UpdateUserUseCase
import com.example.domain.user.usecase.UploadAvatarUseCase
import com.example.moneymate.utils.AppError
import com.example.moneymate.utils.ErrorHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EditProfileViewModel(
    private val getUserUseCase: GetUserUseCase,
    private val updateUserUseCase: UpdateUserUseCase,
    private val uploadAvatarUseCase: UploadAvatarUseCase,
    private val deleteAvatarUseCase: DeleteAvatarUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileState())
    val uiState: StateFlow<EditProfileState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableStateFlow<NavigationEvent?>(null)
    val navigationEvent: StateFlow<NavigationEvent?> = _navigationEvent.asStateFlow()

    private val _errorState = MutableStateFlow<AppError?>(null)
    val errorState: StateFlow<AppError?> = _errorState.asStateFlow()

    init {
        loadUser()
    }

    fun loadUser() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val result = getUserUseCase()
                if (result.isSuccess) {
                    val user = result.getOrThrow()
                    _uiState.value = _uiState.value.copy(
                        user = user,
                        fullName = user.fullName,
                        email = user.email,
                        phoneNumber = user.phoneNumber ?: "",
                        birthDate = user.birthDate ?: "",
                        isLoading = false,
                        originalUser = user
                    )
                } else {
                    _errorState.value = ErrorHandler.mapExceptionToAppError(
                        result.exceptionOrNull() ?: Exception("Failed to load user")
                    )
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            } catch (e: Exception) {
                _errorState.value = ErrorHandler.mapExceptionToAppError(e)
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun updateFullName(fullName: String) {
        _uiState.value = _uiState.value.copy(fullName = fullName)
        checkForChanges()
    }

    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(email = email)
        checkForChanges()
    }

    fun updatePhoneNumber(phoneNumber: String) {
        _uiState.value = _uiState.value.copy(phoneNumber = phoneNumber)
        checkForChanges()
    }

    fun updateBirthDate(birthDate: String) {
        _uiState.value = _uiState.value.copy(birthDate = birthDate)
        checkForChanges()
    }

    fun updateNewPassword(newPassword: String) {
        _uiState.value = _uiState.value.copy(newPassword = newPassword)
        checkForChanges()
    }

    fun updateConfirmPassword(confirmPassword: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = confirmPassword)
        checkForChanges()
    }

    fun updateProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            clearError()

            if (!validateInputs()) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                return@launch
            }

            try {
                val currentUser = _uiState.value.user
                if (currentUser != null) {
                    val updatedUser = currentUser.copy(
                        fullName = _uiState.value.fullName,
                        email = _uiState.value.email,
                        phoneNumber = _uiState.value.phoneNumber.ifEmpty { null },
                        birthDate = _uiState.value.birthDate.ifEmpty { null }
                    )

                    val result = updateUserUseCase(updatedUser)
                    if (result.isSuccess) {
                        _navigationEvent.value = NavigationEvent.ProfileUpdated
                        _uiState.value = _uiState.value.copy(
                            user = result.getOrThrow(),
                            isLoading = false,
                            hasChanges = false,
                            originalUser = result.getOrThrow()
                        )
                    } else {
                        _errorState.value = ErrorHandler.mapExceptionToAppError(
                            result.exceptionOrNull() ?: Exception("Failed to update profile")
                        )
                        _uiState.value = _uiState.value.copy(isLoading = false)
                    }
                }
            } catch (e: Exception) {
                _errorState.value = ErrorHandler.mapExceptionToAppError(e)
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun uploadAvatar(avatarUri: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val result = uploadAvatarUseCase(avatarUri)
                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        user = result.getOrThrow(),
                        isLoading = false
                    )
                } else {
                    _errorState.value = ErrorHandler.mapExceptionToAppError(
                        result.exceptionOrNull() ?: Exception("Failed to upload avatar")
                    )
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            } catch (e: Exception) {
                _errorState.value = ErrorHandler.mapExceptionToAppError(e)
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun deleteAvatar() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val result = deleteAvatarUseCase()
                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        user = result.getOrThrow(),
                        isLoading = false
                    )
                } else {
                    _errorState.value = ErrorHandler.mapExceptionToAppError(
                        result.exceptionOrNull() ?: Exception("Failed to delete avatar")
                    )
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            } catch (e: Exception) {
                _errorState.value = ErrorHandler.mapExceptionToAppError(e)
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    private fun validateInputs(): Boolean {
        val state = _uiState.value

        if (state.fullName.isBlank()) {
            _errorState.value = AppError.ValidationError("Full name is required")
            return false
        }

        if (state.email.isBlank()) {
            _errorState.value = AppError.ValidationError("Email is required")
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(state.email).matches()) {
            _errorState.value = AppError.ValidationError("Please enter a valid email address")
            return false
        }

        // Phone number validation (if provided)
        if (state.phoneNumber.isNotBlank() && !isValidPhoneNumber(state.phoneNumber)) {
            _errorState.value = AppError.ValidationError("Please enter a valid phone number")
            return false
        }

        // Password validation (only if user is trying to change password)
        if (state.newPassword.isNotBlank() || state.confirmPassword.isNotBlank()) {
            if (state.newPassword != state.confirmPassword) {
                _errorState.value = AppError.ValidationError("Passwords do not match")
                return false
            }

            if (state.newPassword.length < 6) {
                _errorState.value = AppError.ValidationError("Password must be at least 6 characters")
                return false
            }
        }

        return true
    }

    private fun isValidPhoneNumber(phoneNumber: String): Boolean {
        val phonePattern = "^[+]?[0-9]{10,15}\$".toRegex()
        return phonePattern.matches(phoneNumber.replace("\\s".toRegex(), ""))
    }

    private fun checkForChanges() {
        val state = _uiState.value
        val original = state.originalUser

        val hasChanges = original?.let { originalUser ->
            state.fullName != originalUser.fullName ||
                    state.email != originalUser.email ||
                    state.phoneNumber != (originalUser.phoneNumber ?: "") ||
                    state.birthDate != (originalUser.birthDate ?: "") ||
                    state.newPassword.isNotBlank() ||
                    state.confirmPassword.isNotBlank()
        } ?: true

        _uiState.value = state.copy(hasChanges = hasChanges)
    }

    fun clearError() {
        _errorState.value = null
    }

    fun clearNavigationEvent() {
        _navigationEvent.value = null
    }
}

data class EditProfileState(
    val user: User? = null,
    val originalUser: User? = null, // To track changes
    val fullName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val birthDate: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val hasChanges: Boolean = false // To enable/disable update button
)

sealed class NavigationEvent {
    object ProfileUpdated : NavigationEvent()
}