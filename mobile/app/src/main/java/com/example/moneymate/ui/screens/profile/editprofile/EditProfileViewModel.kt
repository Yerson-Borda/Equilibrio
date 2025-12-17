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

    private var originalAvatarUrl: String? = null

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
                    originalAvatarUrl = user.avatarUrl

                    // Convert backend currency code to display format
                    val displayCurrency = convertCurrencyToDisplayFormat(user.defaultCurrency ?: "USD")

                    _uiState.value = _uiState.value.copy(
                        user = user,
                        fullName = user.fullName ?: "",
                        email = user.email,
                        phoneNumber = user.phoneNumber ?: "",
                        birthDate = user.dateOfBirth ?: "",
                        defaultCurrency = displayCurrency, // Set the display format
                        isLoading = false,
                        originalUser = user
                    )
                    checkForChanges()
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
                    // Convert display format back to currency code for backend
                    val currencyCode = extractCurrencyCode(_uiState.value.defaultCurrency)

                    val updatedUser = currentUser.copy(
                        fullName = _uiState.value.fullName.ifEmpty { null },
                        email = _uiState.value.email,
                        phoneNumber = _uiState.value.phoneNumber.ifEmpty { null },
                        dateOfBirth = _uiState.value.birthDate.ifEmpty { null },
                        defaultCurrency = currencyCode // Send only the code to backend
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
                println("DEBUG: Starting avatar upload with URI: $avatarUri")
                val result = uploadAvatarUseCase(avatarUri)
                if (result.isSuccess) {
                    println("DEBUG: Avatar upload successful")
                    val updatedUser = result.getOrThrow()
                    _uiState.value = _uiState.value.copy(
                        user = updatedUser,
                        isLoading = false
                    )
                    // Avatar has changed, so enable the update button
                    _uiState.value = _uiState.value.copy(hasChanges = true)
                } else {
                    val exception = result.exceptionOrNull() ?: Exception("Failed to upload avatar")
                    println("DEBUG: Avatar upload failed: ${exception.message}")
                    _errorState.value = ErrorHandler.mapExceptionToAppError(exception)
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            } catch (e: Exception) {
                println("DEBUG: Exception in uploadAvatar: ${e.message}")
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
                    // Avatar has changed, so enable the update button
                    _uiState.value = _uiState.value.copy(hasChanges = true)
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

    fun updateDefaultCurrency(currency: String) {
        _uiState.value = _uiState.value.copy(defaultCurrency = currency)
        checkForChanges()
    }

    private fun checkForChanges() {
        val state = _uiState.value
        val original = state.originalUser

        val hasChanges = original?.let { originalUser ->
            state.fullName != (originalUser.fullName ?: "") ||
                    state.email != originalUser.email ||
                    state.phoneNumber != (originalUser.phoneNumber ?: "") ||
                    state.birthDate != (originalUser.dateOfBirth ?: "") ||
                    state.defaultCurrency != (originalUser.defaultCurrency ?: "USD - $") ||
                    state.newPassword.isNotBlank() ||
                    state.confirmPassword.isNotBlank() ||
                    state.user?.avatarUrl != originalAvatarUrl
        } ?: true

        _uiState.value = state.copy(hasChanges = hasChanges)
    }

    fun clearError() {
        _errorState.value = null
    }

    fun clearNavigationEvent() {
        _navigationEvent.value = null
    }

    private fun convertCurrencyToDisplayFormat(currencyCode: String): String {
        return when (currencyCode.uppercase()) {
            "USD" -> "USD - $"
            "EUR" -> "EUR - €"
            "GBP" -> "GBP - £"
            "JPY" -> "JPY - ¥"
            "CAD" -> "CAD - C$"
            "AUD" -> "AUD - A$"
            "CHF" -> "CHF - CHF"
            "CNY" -> "CNY - ¥"
            "INR" -> "INR - ₹"
            "RUB" -> "RUB - ₽"
            "BRL" -> "BRL - R$"
            "MXN" -> "MXN - $"
            "KRW" -> "KRW - ₩"
            else -> "USD - $" // Default fallback
        }
    }

    private fun extractCurrencyCode(displayCurrency: String): String {
        return when {
            displayCurrency.contains("USD") -> "USD"
            displayCurrency.contains("EUR") -> "EUR"
            displayCurrency.contains("GBP") -> "GBP"
            displayCurrency.contains("JPY") -> "JPY"
            displayCurrency.contains("CAD") -> "CAD"
            displayCurrency.contains("AUD") -> "AUD"
            displayCurrency.contains("CHF") -> "CHF"
            displayCurrency.contains("CNY") -> "CNY"
            displayCurrency.contains("INR") -> "INR"
            displayCurrency.contains("RUB") -> "RUB"
            displayCurrency.contains("BRL") -> "BRL"
            displayCurrency.contains("MXN") -> "MXN"
            displayCurrency.contains("KRW") -> "KRW"
            else -> "USD" // Default fallback
        }
    }
}

data class EditProfileState(
    val user: User? = null,
    val originalUser: User? = null,
    val fullName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val birthDate: String = "",
    val defaultCurrency: String = "USD - $", // Add this
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val hasChanges: Boolean = false
)

sealed class NavigationEvent {
    object ProfileUpdated : NavigationEvent()
}