package com.example.moneymate.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * Global data sync manager for real-time updates across screens
 */
object DataSyncManager {

    // Event types for different data changes
    sealed class DataChangeEvent {
        object TransactionsUpdated : DataChangeEvent()
        object WalletsUpdated : DataChangeEvent()
        object WalletSpecificUpdated : DataChangeEvent()
        object CategoriesUpdated : DataChangeEvent()
        object TagsUpdated : DataChangeEvent()
        object BudgetUpdated : DataChangeEvent()
        object CategoryLimitsUpdated : DataChangeEvent()
        object UserDataUpdated : DataChangeEvent()

        // Add more event types as needed
        data class SpecificTransactionUpdated(val transactionId: Int) : DataChangeEvent()
    }

    fun notifyCategoryLimitsUpdated() {
        _dataChangeEvents.tryEmit(DataChangeEvent.CategoryLimitsUpdated)
    }
    fun notifyBudgetUpdated() {
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            println("🔄 DEBUG: DataSyncManager - Notifying about budget update")
            _dataChangeEvents.emit(DataChangeEvent.BudgetUpdated)
        }
    }
    // SharedFlow for reactive updates (ViewModel to ViewModel communication)
    private val _dataChangeEvents = MutableSharedFlow<DataChangeEvent>()
    val dataChangeEvents = _dataChangeEvents.asSharedFlow()

    // Function to notify all listeners about a data change
    suspend fun notifyDataChanged(event: DataChangeEvent) {
        _dataChangeEvents.emit(event)
    }

    // Helper to notify from ViewModelScope
    fun notifyDataChangedFromVM(event: DataChangeEvent, onError: (Throwable) -> Unit = {}) {
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            try {
                _dataChangeEvents.emit(event)
            } catch (e: Throwable) {
                onError(e)
            }
        }
    }

    // Simple flag-based approach (alternative for simple cases)
    private val _transactionsDirty = MutableLiveData(true)
    val transactionsDirty: LiveData<Boolean> = _transactionsDirty

    private val _walletsDirty = MutableLiveData(true)
    val walletsDirty: LiveData<Boolean> = _walletsDirty

    fun markTransactionsDirty() {
        _transactionsDirty.postValue(true)
    }

    fun markWalletsDirty() {
        _walletsDirty.postValue(true)
    }

    fun markTransactionsClean() {
        _transactionsDirty.postValue(false)
    }

    fun markWalletsClean() {
        _walletsDirty.postValue(false)
    }
}