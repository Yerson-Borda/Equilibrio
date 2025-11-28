package com.example.moneymate.ui.screens.transaction.addtransaction

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.category.model.Category
import com.example.domain.category.usecase.GetExpenseCategoriesUseCase
import com.example.domain.category.usecase.GetIncomeCategoriesUseCase
import com.example.domain.transaction.usecase.CreateTransactionUseCase
import com.example.domain.transaction.usecase.CreateTransferUseCase
import com.example.domain.wallet.model.Wallet
import com.example.domain.wallet.usecase.GetWalletsUseCase
import com.example.moneymate.utils.AppError
import com.example.moneymate.utils.ErrorHandler
import com.example.moneymate.utils.ScreenState
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

    // Remove individual flows since they're now part of uiState
    // private val _wallets = MutableStateFlow<List<Wallet>>(emptyList())
    // private val _categories = MutableStateFlow<List<Category>>(emptyList())
    // private val _errorState = MutableStateFlow<AppError?>(null)

    fun onAttachmentsSelected(uris: List<String>) {
        _uiState.value = _uiState.value.copy(attachments = uris)
    }

    init {
        loadWallets()
        loadCategories()
    }

     fun loadWallets() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(walletsState = ScreenState.Loading)

            try {
                val result = getWalletsUseCase()
                if (result.isSuccess) {
                    val wallets = result.getOrThrow()
                    _uiState.value = _uiState.value.copy(
                        walletsState = ScreenState.Success(wallets)
                    )
                    // Set the first wallet as default selection if available
                    val firstWallet = wallets.firstOrNull()
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
                    _uiState.value = _uiState.value.copy(
                        walletsState = ScreenState.Error(
                            ErrorHandler.mapExceptionToAppError(exception),
                            retryAction = { loadWallets() }
                        )
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    walletsState = ScreenState.Error(
                        ErrorHandler.mapExceptionToAppError(e),
                        retryAction = { loadWallets() }
                    )
                )
            }
        }
    }

     fun loadCategories() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(categoriesState = ScreenState.Loading)

            try {
                val result = when (_uiState.value.selectedType) {
                    TransactionType.INCOME -> getIncomeCategoriesUseCase()
                    TransactionType.EXPENSE -> getExpenseCategoriesUseCase()
                    TransactionType.TRANSFER -> getExpenseCategoriesUseCase()
                }

                if (result.isSuccess) {
                    val categories = result.getOrThrow()
                    _uiState.value = _uiState.value.copy(
                        categoriesState = ScreenState.Success(categories)
                    )
                    // Set the first category as default selection if available
                    val firstCategory = categories.firstOrNull()
                    if (firstCategory != null) {
                        _uiState.value = _uiState.value.copy(
                            selectedCategoryId = firstCategory.id,
                            selectedCategoryName = firstCategory.name
                        )
                    }
                } else {
                    val exception = result.exceptionOrNull() ?: Exception("Error loading categories")
                    _uiState.value = _uiState.value.copy(
                        categoriesState = ScreenState.Error(
                            ErrorHandler.mapExceptionToAppError(exception),
                            retryAction = { loadCategories() }
                        )
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    categoriesState = ScreenState.Error(
                        ErrorHandler.mapExceptionToAppError(e),
                        retryAction = { loadCategories() }
                    )
                )
            }
        }
    }

    fun onTransactionTypeSelected(type: TransactionType) {
        _uiState.value = _uiState.value.copy(selectedType = type)
        loadCategories() // Reload categories when type changes
    }

    fun onAmountChanged(amount: String) {
        _uiState.value = _uiState.value.copy(amount = amount)
    }

    fun onWalletSelected(walletId: Int, walletName: String) {
        _uiState.value = _uiState.value.copy(
            selectedWalletId = walletId,
            selectedWalletName = walletName
        )
    }

    fun onDestinationWalletSelected(walletId: Int, walletName: String) {
        _uiState.value = _uiState.value.copy(
            destinationWalletId = walletId,
            destinationWalletName = walletName
        )
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
    }

    fun onBackspacePressed() {
        val currentAmount = _uiState.value.amount
        if (currentAmount.isNotEmpty()) {
            val newAmount = currentAmount.dropLast(1)
            _uiState.value = _uiState.value.copy(
                amount = if (newAmount.isEmpty()) "0" else newAmount
            )
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
            _uiState.value = _uiState.value.copy(transactionState = ScreenState.Loading)

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
                _uiState.value = _uiState.value.copy(
                    transactionState = ScreenState.Error(
                        ErrorHandler.mapExceptionToAppError(e),
                        retryAction = { createTransaction() }
                    )
                )
            }
        }
    }

    private fun validateWalletBalance(): Boolean {
        if (_uiState.value.selectedType == TransactionType.EXPENSE) {
            val wallets = when (val state = _uiState.value.walletsState) {
                is ScreenState.Success -> state.data
                else -> emptyList()
            }
            val selectedWallet = wallets.find { it.id == _uiState.value.selectedWalletId }
            val walletBalance = selectedWallet?.balance?.toDoubleOrNull() ?: 0.0
            val expenseAmount = _uiState.value.amount.toDoubleOrNull() ?: 0.0

            if (walletBalance < expenseAmount) {
                _uiState.value = _uiState.value.copy(
                    transactionState = ScreenState.Error(
                        AppError.ValidationError(
                            "Insufficient balance. Your wallet has $${"%.2f".format(walletBalance)} but you're trying to spend $${"%.2f".format(expenseAmount)}"
                        )
                    )
                )
                return false
            }
        }
        return true
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun handleTransferCreation() {
        if (_uiState.value.selectedWalletId == _uiState.value.destinationWalletId) {
            _uiState.value = _uiState.value.copy(
                transactionState = ScreenState.Error(
                    AppError.ValidationError("Cannot transfer to the same wallet")
                )
            )
            return
        }

        val result = createTransferUseCase(
            sourceWalletId = _uiState.value.selectedWalletId,
            destinationWalletId = _uiState.value.destinationWalletId,
            amount = _uiState.value.amount,
            note = _uiState.value.description
        )

        if (result.isSuccess) {
            _uiState.value = _uiState.value.copy(transactionState = ScreenState.Success(Unit))
            _navigationEvent.value = NavigationEvent.NavigateHome
        } else {
            val exception = result.exceptionOrNull() ?: Exception("Failed to create transfer")
            _uiState.value = _uiState.value.copy(
                transactionState = ScreenState.Error(
                    ErrorHandler.mapExceptionToAppError(exception),
                    retryAction = { createTransaction() }
                )
            )
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
            _uiState.value = _uiState.value.copy(transactionState = ScreenState.Success(Unit))
            _navigationEvent.value = NavigationEvent.NavigateHome
        } else {
            val exception = result.exceptionOrNull() ?: Exception("Failed to create transaction")
            _uiState.value = _uiState.value.copy(
                transactionState = ScreenState.Error(
                    ErrorHandler.mapExceptionToAppError(exception),
                    retryAction = { createTransaction() }
                )
            )
        }
    }

    private fun validateInputs(): Boolean {
        val amount = _uiState.value.amount
        if (amount == "0" || amount == "0." || amount.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                transactionState = ScreenState.Error(
                    AppError.ValidationError("Please enter a valid amount")
                )
            )
            return false
        }

        if (_uiState.value.selectedWalletId == 0) {
            _uiState.value = _uiState.value.copy(
                transactionState = ScreenState.Error(
                    AppError.ValidationError("Please select a wallet")
                )
            )
            return false
        }

        // For transfers, check destination wallet
        if (_uiState.value.selectedType == TransactionType.TRANSFER) {
            if (_uiState.value.destinationWalletId == 0) {
                _uiState.value = _uiState.value.copy(
                    transactionState = ScreenState.Error(
                        AppError.ValidationError("Please select a destination wallet")
                    )
                )
                return false
            }
            if (_uiState.value.selectedWalletId == _uiState.value.destinationWalletId) {
                _uiState.value = _uiState.value.copy(
                    transactionState = ScreenState.Error(
                        AppError.ValidationError("Cannot transfer to the same wallet")
                    )
                )
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

    // Helper functions to get data from screen states
    fun getWallets(): List<Wallet> {
        return when (val state = _uiState.value.walletsState) {
            is ScreenState.Success -> state.data
            else -> emptyList()
        }
    }

    fun getCategories(): List<Category> {
        return when (val state = _uiState.value.categoriesState) {
            is ScreenState.Success -> state.data
            else -> emptyList()
        }
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
    val selectedCategoryName: String = "Foods & Drinks",
    val description: String = "",
    val selectedTags: List<String> = emptyList(),
    val attachments: List<String> = emptyList(),

    // Screen states for loading and error handling
    val walletsState: ScreenState<List<Wallet>> = ScreenState.Loading,
    val categoriesState: ScreenState<List<Category>> = ScreenState.Loading,
    val transactionState: ScreenState<Unit> = ScreenState.Empty
)

enum class TransactionType(val displayName: String, val apiValue: String) {
    INCOME("INCOME", "income"),
    EXPENSE("EXPENSE", "expense"),
    TRANSFER("TRANSFER", "transfer")
}