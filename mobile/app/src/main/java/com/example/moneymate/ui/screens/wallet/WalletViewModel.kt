package com.example.moneymate.ui.screens.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.transaction.model.TransactionEntity
import com.example.domain.transaction.usecase.GetWalletTransactionsUseCase
import com.example.domain.wallet.model.Wallet
import com.example.domain.wallet.model.WalletCreateRequest
import com.example.domain.wallet.model.WalletUpdateRequest
import com.example.domain.wallet.usecase.CreateWalletUseCase
import com.example.domain.wallet.usecase.DeleteWalletUseCase
import com.example.domain.wallet.usecase.GetWalletDetailUseCase
import com.example.domain.wallet.usecase.GetWalletsUseCase
import com.example.domain.wallet.usecase.UpdateWalletUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WalletViewModel(
    private val getWalletsUseCase: GetWalletsUseCase,
    private val createWalletUseCase: CreateWalletUseCase,
    private val getWalletDetailUseCase: GetWalletDetailUseCase,
    private val deleteWalletUseCase: DeleteWalletUseCase,
    private val updateWalletUseCase: UpdateWalletUseCase,
    private val getWalletTransactionsUseCase: GetWalletTransactionsUseCase // Add this use case
) : ViewModel() {
    private val _wallets = MutableStateFlow<List<Wallet>>(emptyList())
    val wallets: StateFlow<List<Wallet>> = _wallets.asStateFlow()

    private val _transactions = MutableStateFlow<List<TransactionEntity>>(emptyList())
    val transactions: StateFlow<List<TransactionEntity>> = _transactions.asStateFlow()

    private val _selectedWallet = MutableStateFlow<Wallet?>(null)
    val selectedWallet: StateFlow<Wallet?> = _selectedWallet.asStateFlow()

    private val _walletCreated = MutableStateFlow(false)
    val walletCreated: StateFlow<Boolean> = _walletCreated.asStateFlow()

    private val _walletDetail = MutableStateFlow<Wallet?>(null)
    val walletDetail: StateFlow<Wallet?> = _walletDetail.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _showDeleteDialog = MutableStateFlow(false)
    val showDeleteDialog: StateFlow<Boolean> = _showDeleteDialog.asStateFlow()

    private val _walletDeleted = MutableStateFlow(false)
    val walletDeleted: StateFlow<Boolean> = _walletDeleted.asStateFlow()

    private val _walletUpdated = MutableStateFlow(false)
    val walletUpdated: StateFlow<Boolean> = _walletUpdated.asStateFlow()

    init {
        loadWallets()
    }

    fun loadWallets() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = getWalletsUseCase()
            if (result.isSuccess) {
                val walletsList = result.getOrNull() ?: emptyList()
                _wallets.value = walletsList
                _selectedWallet.value = walletsList.firstOrNull()
                // Load transactions for the selected wallet
                _selectedWallet.value?.id?.let { walletId ->
                    loadTransactions(walletId)
                }
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Failed to load wallets"
            }
            _isLoading.value = false
        }
    }

    fun createWallet(walletRequest: WalletCreateRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = createWalletUseCase(walletRequest)
            if (result.isSuccess) {
                _walletCreated.value = true
                loadWallets() // Reload wallets to include the new one
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Failed to create wallet"
                _walletCreated.value = false
            }
            _isLoading.value = false
        }
    }

    fun selectWallet(wallet: Wallet) {
        _selectedWallet.value = wallet
        wallet.id?.let { walletId ->
            loadTransactions(walletId)
        }
    }

    fun loadTransactions(walletId: Int) {
        viewModelScope.launch {
            val result = getWalletTransactionsUseCase(walletId) // Use the new use case
            if (result.isSuccess) {
                _transactions.value = result.getOrNull() ?: emptyList()
            } else {
                // Handle error, maybe show empty transactions
                _transactions.value = emptyList()
            }
        }
    }

    fun loadWalletDetail(walletId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                getWalletDetailUseCase(walletId).collect { wallet ->
                    _walletDetail.value = wallet
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load wallet details"
            }
            _isLoading.value = false
        }
    }

    fun deleteWallet(walletId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = deleteWalletUseCase(walletId)
            if (result.isSuccess) {
                _walletDeleted.value = true
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Failed to delete wallet"
            }
            _isLoading.value = false
            _showDeleteDialog.value = false
        }
    }

    fun updateWallet(walletId: Int, walletRequest: WalletUpdateRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = updateWalletUseCase(walletId, walletRequest)
            if (result.isSuccess) {
                _walletUpdated.value = true
                loadWallets() // Reload wallets to reflect changes
                loadWalletDetail(walletId) // Reload the wallet detail
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Failed to update wallet"
                _walletUpdated.value = false
            }
            _isLoading.value = false
        }
    }

    fun resetWalletUpdated() {
        _walletUpdated.value = false
    }

    fun getWalletIncomeExpense(walletId: Int): Pair<Double, Double> {
        val walletTransactions = _transactions.value.filter { it.walletId == walletId }
        return calculateIncomeExpense(walletTransactions)
    }

    fun showDeleteConfirmation() {
        _showDeleteDialog.value = true
    }

    fun dismissDeleteConfirmation() {
        _showDeleteDialog.value = false
    }

    fun resetWalletDeleted() {
        _walletDeleted.value = false
    }

    fun clearError() {
        _error.value = null
    }

    fun resetWalletCreated() {
        _walletCreated.value = false
    }
}