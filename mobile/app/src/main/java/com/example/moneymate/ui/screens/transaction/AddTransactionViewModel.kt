package com.example.moneymate.ui.screens.transaction

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.category.model.Category
import com.example.domain.category.usecase.GetExpenseCategoriesUseCase
import com.example.domain.category.usecase.GetIncomeCategoriesUseCase
import com.example.domain.transaction.usecase.CreateTransactionUseCase
import com.example.domain.transaction.usecase.CreateTransferUseCase
import com.example.domain.wallet.usecase.GetWalletsUseCase
import com.example.domain.wallet.model.Wallet
import com.example.moneymate.utils.AppError
import com.example.moneymate.utils.ErrorHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class AddTransactionViewModel(
    private val createTransactionUseCase: CreateTransactionUseCase,
    private val createTransferUseCase: CreateTransferUseCase,
    private val getWalletsUseCase: GetWalletsUseCase,
    private val getIncomeCategoriesUseCase: GetIncomeCategoriesUseCase,
    private val getExpenseCategoriesUseCase: GetExpenseCategoriesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddTransactionState())
    val uiState: StateFlow<AddTransactionState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableStateFlow<NavigationEvent?>(null)
    val navigationEvent: StateFlow<NavigationEvent?> = _navigationEvent.asStateFlow()

    private val _wallets = MutableStateFlow<List<Wallet>>(emptyList())
    val wallets: StateFlow<List<Wallet>> = _wallets.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _errorState = MutableStateFlow<AppError?>(null)
    val errorState: StateFlow<AppError?> = _errorState.asStateFlow()

    fun onAttachmentsSelected(uris: List<String>) {
        _uiState.value = _uiState.value.copy(attachments = uris)
    }

    fun clearError() {
        _errorState.value = null
    }

    init {
        loadWallets()
        loadCategories() // Load categories based on initial transaction type
    }

    private fun loadWallets() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                val result = getWalletsUseCase()
                if (result.isSuccess) {
                    _wallets.value = result.getOrThrow()
                    // Set the first wallet as default selection if available
                    val firstWallet = _wallets.value.firstOrNull()
                    if (firstWallet != null) {
                        _uiState.value = _uiState.value.copy(
                            selectedWalletId = firstWallet.id ?: 0,
                            selectedWalletName = firstWallet.name,
                            destinationWalletId = firstWallet.id ?: 0,
                            destinationWalletName = firstWallet.name
                        )
                    }
                } else {
                    val exception = result.exceptionOrNull() ?: Exception("Unknown error loading wallets")
                    _errorState.value = ErrorHandler.mapExceptionToAppError(exception)
                }
            } catch (e: Exception) {
                _errorState.value = ErrorHandler.mapExceptionToAppError(e)
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    // Add this function to load categories based on transaction type
    private fun loadCategories() {
        viewModelScope.launch {
            try {
                val result = when (_uiState.value.selectedType) {
                    TransactionType.INCOME -> getIncomeCategoriesUseCase()
                    TransactionType.EXPENSE -> getExpenseCategoriesUseCase()
                    TransactionType.TRANSFER -> getExpenseCategoriesUseCase() // or handle differently
                }

                if (result.isSuccess) {
                    _categories.value = result.getOrThrow()
                    // Set the first category as default selection if available
                    val firstCategory = _categories.value.firstOrNull()
                    if (firstCategory != null) {
                        _uiState.value = _uiState.value.copy(
                            selectedCategoryId = firstCategory.id,
                            selectedCategoryName = firstCategory.name
                        )
                    }
                }
            } catch (e: Exception) {
                // Handle error silently or log it
                println("Error loading categories: ${e.message}")
            }
        }
    }

    fun onTransactionTypeSelected(type: TransactionType) {
        _uiState.value = _uiState.value.copy(selectedType = type)
        clearError()
        loadCategories() // Reload categories when type changes
    }

    // ... rest of your existing functions remain the same
    fun onAmountChanged(amount: String) {
        _uiState.value = _uiState.value.copy(amount = amount)
        clearError()
    }

    fun onWalletSelected(walletId: Int, walletName: String) {
        _uiState.value = _uiState.value.copy(
            selectedWalletId = walletId,
            selectedWalletName = walletName
        )
        clearError()
    }

    fun onDestinationWalletSelected(walletId: Int, walletName: String) {
        _uiState.value = _uiState.value.copy(
            destinationWalletId = walletId,
            destinationWalletName = walletName
        )
        clearError()
    }

    fun onCategorySelected(categoryId: Int, categoryName: String) {
        _uiState.value = _uiState.value.copy(
            selectedCategoryId = categoryId,
            selectedCategoryName = categoryName
        )
    }

    fun onDescriptionChanged(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }

    fun onTagSelected(tag: String) {
        val currentTags = _uiState.value.selectedTags.toMutableList()
        if (currentTags.contains(tag)) {
            currentTags.remove(tag)
        } else {
            currentTags.add(tag)
        }
        _uiState.value = _uiState.value.copy(selectedTags = currentTags)
    }

    fun onNumberPressed(number: String) {
        val currentAmount = _uiState.value.amount
        val newAmount = if (currentAmount == "0") number else currentAmount + number
        _uiState.value = _uiState.value.copy(amount = newAmount)
        clearError()
    }

    fun onBackspacePressed() {
        val currentAmount = _uiState.value.amount
        if (currentAmount.isNotEmpty()) {
            val newAmount = currentAmount.dropLast(1)
            _uiState.value = _uiState.value.copy(
                amount = if (newAmount.isEmpty()) "0" else newAmount
            )
            clearError()
        }
    }

    fun onDecimalPressed() {
        val currentAmount = _uiState.value.amount
        if (!currentAmount.contains(".")) {
            _uiState.value = _uiState.value.copy(amount = "$currentAmount.")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createTransaction() {
        if (!validateInputs()) {
            return
        }

        if (!validateWalletBalance()) {
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            clearError()

            try {
                when (_uiState.value.selectedType) {
                    TransactionType.TRANSFER -> {
                        handleTransferCreation()
                    }
                    else -> {
                        handleTransactionCreation()
                    }
                }
            } catch (e: Exception) {
                _errorState.value = ErrorHandler.mapExceptionToAppError(e)
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    private fun validateWalletBalance(): Boolean {
        if (_uiState.value.selectedType == TransactionType.EXPENSE) {
            val selectedWallet = _wallets.value.find { it.id == _uiState.value.selectedWalletId }
            val walletBalance = selectedWallet?.balance?.toDoubleOrNull() ?: 0.0
            val expenseAmount = _uiState.value.amount.toDoubleOrNull() ?: 0.0

            if (walletBalance < expenseAmount) {
                _errorState.value = AppError.ValidationError(
                    "Insufficient balance. Your wallet has $${"%.2f".format(walletBalance)} but you're trying to spend $${"%.2f".format(expenseAmount)}"
                )
                return false
            }
        }
        return true
    }

    private suspend fun handleTransferCreation() {
        if (_uiState.value.selectedWalletId == _uiState.value.destinationWalletId) {
            _errorState.value = AppError.ValidationError("Cannot transfer to the same wallet")
            return
        }

        val result = createTransferUseCase(
            sourceWalletId = _uiState.value.selectedWalletId,
            destinationWalletId = _uiState.value.destinationWalletId,
            amount = _uiState.value.amount,
            note = _uiState.value.description
        )

        if (result.isSuccess) {
            _navigationEvent.value = NavigationEvent.NavigateHome
        } else {
            val exception = result.exceptionOrNull() ?: Exception("Failed to create transfer")
            _errorState.value = ErrorHandler.mapExceptionToAppError(exception)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun handleTransactionCreation() {
        val result = createTransactionUseCase(
            amount = _uiState.value.amount,
            description = _uiState.value.description,
            note = _uiState.value.description,
            type = _uiState.value.selectedType.apiValue,
            transactionDate = LocalDate.now().toString(),
            walletId = _uiState.value.selectedWalletId,
            categoryId = _uiState.value.selectedCategoryId
        )

        if (result.isSuccess) {
            _navigationEvent.value = NavigationEvent.NavigateHome
        } else {
            val exception = result.exceptionOrNull() ?: Exception("Failed to create transaction")
            _errorState.value = ErrorHandler.mapExceptionToAppError(exception)
        }
    }

    private fun validateInputs(): Boolean {
        val amount = _uiState.value.amount
        if (amount == "0" || amount == "0." || amount.isEmpty()) {
            _errorState.value = AppError.ValidationError("Please enter a valid amount")
            return false
        }

        if (_uiState.value.selectedWalletId == 0) {
            _errorState.value = AppError.ValidationError("Please select a wallet")
            return false
        }

        // For transfers, check destination wallet
        if (_uiState.value.selectedType == TransactionType.TRANSFER) {
            if (_uiState.value.destinationWalletId == 0) {
                _errorState.value = AppError.ValidationError("Please select a destination wallet")
                return false
            }
            if (_uiState.value.selectedWalletId == _uiState.value.destinationWalletId) {
                _errorState.value = AppError.ValidationError("Cannot transfer to the same wallet")
                return false
            }
        }

        return true
    }

    fun removeAttachment(uri: String) {
        val currentAttachments = _uiState.value.attachments.toMutableList()
        currentAttachments.remove(uri)
        _uiState.value = _uiState.value.copy(attachments = currentAttachments)
    }

    fun clearNavigationEvent() {
        _navigationEvent.value = null
    }
}
sealed class NavigationEvent {
    object NavigateHome : NavigationEvent()
}

data class AddTransactionState(
    val selectedType: TransactionType = TransactionType.EXPENSE,
    val amount: String = "0",
    val selectedWalletId: Int = 0,
    val selectedWalletName: String = "Select Wallet",
    val destinationWalletId: Int = 0,
    val destinationWalletName: String = "Select Wallet",
    val selectedCategoryId: Int = 1,
    val selectedCategoryName: String = "Food",
    val description: String = "",
    val selectedTags: List<String> = emptyList(),
    val attachments: List<String> = emptyList(),
    val isLoading: Boolean = false
)

enum class TransactionType(val displayName: String, val apiValue: String) {
    INCOME("INCOME", "income"),
    EXPENSE("EXPENSE", "expense"),
    TRANSFER("TRANSFER", "transfer")
}