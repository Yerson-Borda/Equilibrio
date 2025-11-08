package com.example.moneymate.ui.screens.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.wallet.model.Transaction
import com.example.domain.wallet.model.Wallet
import com.example.domain.wallet.model.WalletCreateRequest
import com.example.domain.wallet.usecase.CreateWalletUseCase
import com.example.domain.wallet.usecase.GetWalletTransactionsUseCase
import com.example.domain.wallet.usecase.GetWalletsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WalletViewModel(
    private val getWalletsUseCase: GetWalletsUseCase,
    private val createWalletUseCase: CreateWalletUseCase,
    private val getWalletTransactionsUseCase: GetWalletTransactionsUseCase
) : ViewModel() {
    private val _wallets = MutableStateFlow<List<Wallet>>(emptyList())
    val wallets: StateFlow<List<Wallet>> = _wallets.asStateFlow()

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    private val _selectedWallet = MutableStateFlow<Wallet?>(null)
    val selectedWallet: StateFlow<Wallet?> = _selectedWallet.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _walletCreated = MutableStateFlow(false)
    val walletCreated: StateFlow<Boolean> = _walletCreated.asStateFlow()

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

    private fun loadTransactions(walletId: Int) {
        viewModelScope.launch {
            val result = getWalletTransactionsUseCase(walletId)
            if (result.isSuccess) {
                _transactions.value = result.getOrNull() ?: emptyList()
            } else {
                // Handle error, maybe show empty transactions
                _transactions.value = emptyList()
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun resetWalletCreated() {
        _walletCreated.value = false
    }
}