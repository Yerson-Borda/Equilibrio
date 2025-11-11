package com.example.moneymate.ui.screens.transaction

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.transaction.usecase.CreateTransactionUseCase
import com.example.domain.transaction.usecase.CreateTransferUseCase
import com.example.domain.wallet.usecase.GetWalletsUseCase
import com.example.domain.wallet.model.Wallet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class AddTransactionViewModel(
    private val createTransactionUseCase: CreateTransactionUseCase,
    private val createTransferUseCase: CreateTransferUseCase,
    private val getWalletsUseCase: GetWalletsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddTransactionState())
    val uiState: StateFlow<AddTransactionState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableStateFlow<NavigationEvent?>(null)
    val navigationEvent: StateFlow<NavigationEvent?> = _navigationEvent.asStateFlow()

    private val _wallets = MutableStateFlow<List<Wallet>>(emptyList())
    val wallets: StateFlow<List<Wallet>> = _wallets.asStateFlow()

    fun onAttachmentsSelected(uris: List<String>) {
        _uiState.value = _uiState.value.copy(attachments = uris)
    }


    init {
        loadWallets()
    }

    private fun loadWallets() {
        viewModelScope.launch {
            try {
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
                }
            } catch (e: Exception) {
                println("Error loading wallets: ${e.message}")
            }
        }
    }

    fun onTransactionTypeSelected(type: TransactionType) {
        _uiState.value = _uiState.value.copy(selectedType = type)
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
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                when (_uiState.value.selectedType) {
                    TransactionType.TRANSFER -> {
                        // Handle transfer
                        if (_uiState.value.selectedWalletId != _uiState.value.destinationWalletId) {
                            val result = createTransferUseCase(
                                sourceWalletId = _uiState.value.selectedWalletId,
                                destinationWalletId = _uiState.value.destinationWalletId,
                                amount = _uiState.value.amount,
                                note = _uiState.value.description
                            )

                            if (result.isSuccess) {
                                _navigationEvent.value = NavigationEvent.NavigateHome
                                println("Transfer created successfully: ${result.getOrNull()}")
                            } else {
                                println("Failed to create transfer: ${result.exceptionOrNull()}")
                            }
                        } else {
                            println("Cannot transfer to the same wallet")
                        }
                    }
                    else -> {
                        // Handle income/expense
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
                            println("Transaction created successfully: ${result.getOrNull()}")
                        } else {
                            println("Failed to create transaction: ${result.exceptionOrNull()}")
                        }
                    }
                }
            } catch (e: Exception) {
                println("Error creating transaction: ${e.message}")
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
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