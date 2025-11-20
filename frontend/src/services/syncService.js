import { apiService } from '././api.jsx';
import { webSocketService } from '././websocketService.js';

class SyncService {
    constructor() {
        this.isSyncing = false;
        this.lastSyncTime = null;
        this.syncInterval = 30000; // 30 seconds
        this.syncTimer = null;
    }

    // Initialize sync after login
    initialize(userId) {
        console.log('Initializing sync service for user:', userId);

        // Connect to WebSocket for real-time updates
        webSocketService.connect(userId);

        // Set up event listeners for real-time updates
        this.setupWebSocketListeners();

        // Perform initial sync
        this.performFullSync();

        // Start periodic sync
        this.startPeriodicSync();
    }

    setupWebSocketListeners() {
        // Listen for real-time updates
        webSocketService.addEventListener('transaction_created', (data) => {
            console.log('Real-time: Transaction created', data);
            this.handleTransactionUpdate(data);
        });

        webSocketService.addEventListener('transaction_updated', (data) => {
            console.log('Real-time: Transaction updated', data);
            this.handleTransactionUpdate(data);
        });

        webSocketService.addEventListener('transaction_deleted', (data) => {
            console.log('Real-time: Transaction deleted', data);
            this.handleTransactionDelete(data);
        });

        webSocketService.addEventListener('wallet_created', (data) => {
            console.log('Real-time: Wallet created', data);
            this.handleWalletUpdate(data);
        });

        webSocketService.addEventListener('wallet_updated', (data) => {
            console.log('Real-time: Wallet updated', data);
            this.handleWalletUpdate(data);
        });

        webSocketService.addEventListener('wallet_deleted', (data) => {
            console.log('Real-time: Wallet deleted', data);
            this.handleWalletDelete(data);
        });

        webSocketService.addEventListener('connected', () => {
            console.log('WebSocket connected - sync service ready');
        });

        webSocketService.addEventListener('disconnected', () => {
            console.log('WebSocket disconnected - falling back to periodic sync');
        });
    }

    async performFullSync() {
        if (this.isSyncing) return;

        this.isSyncing = true;
        console.log('Performing full sync...');

        try {
            // Sync all data
            await Promise.all([
                this.syncWallets(),
                this.syncTransactions(),
                this.syncCategories(),
                this.syncUserData()
            ]);

            this.lastSyncTime = new Date();
            console.log('Full sync completed at:', this.lastSyncTime);

            // Dispatch sync complete event
            this.dispatchSyncEvent('sync_complete', {
                lastSyncTime: this.lastSyncTime
            });

        } catch (error) {
            console.error('Full sync failed:', error);
            this.dispatchSyncEvent('sync_error', { error });
        } finally {
            this.isSyncing = false;
        }
    }

    async syncWallets() {
        try {
            const wallets = await apiService.getWallets();
            this.cacheData('wallets', wallets);
            return wallets;
        } catch (error) {
            console.error('Failed to sync wallets:', error);
            throw error;
        }
    }

    async syncTransactions() {
        try {
            const transactions = await apiService.getTransactions();
            this.cacheData('transactions', transactions);
            return transactions;
        } catch (error) {
            console.error('Failed to sync transactions:', error);
            throw error;
        }
    }

    async syncCategories() {
        try {
            const categories = await apiService.getCategories();
            this.cacheData('categories', categories);
            return categories;
        } catch (error) {
            console.error('Failed to sync categories:', error);
            throw error;
        }
    }

    async syncUserData() {
        try {
            const userData = await apiService.getDetailedUserInfo();
            this.cacheData('user', userData);
            return userData;
        } catch (error) {
            console.error('Failed to sync user data:', error);
            throw error;
        }
    }

    startPeriodicSync() {
        // Clear existing timer
        if (this.syncTimer) {
            clearInterval(this.syncTimer);
        }

        // Start new periodic sync
        this.syncTimer = setInterval(() => {
            if (!this.isSyncing && webSocketService.isConnected()) {
                this.performFullSync();
            }
        }, this.syncInterval);
    }

    stopSync() {
        if (this.syncTimer) {
            clearInterval(this.syncTimer);
            this.syncTimer = null;
        }
        webSocketService.disconnect();
    }

    // Cache data in localStorage with timestamp
    cacheData(key, data) {
        const cacheItem = {
            data: data,
            timestamp: new Date().toISOString(),
            version: '1.0'
        };
        localStorage.setItem(`cache_${key}`, JSON.stringify(cacheItem));
    }

    // Get cached data if not expired
    getCachedData(key, maxAgeMinutes = 5) {
        const cached = localStorage.getItem(`cache_${key}`);
        if (!cached) return null;

        try {
            const cacheItem = JSON.parse(cached);
            const cacheTime = new Date(cacheItem.timestamp);
            const now = new Date();
            const diffMinutes = (now - cacheTime) / (1000 * 60);

            if (diffMinutes < maxAgeMinutes) {
                return cacheItem.data;
            } else {
                // Cache expired, remove it
                localStorage.removeItem(`cache_${key}`);
                return null;
            }
        } catch (error) {
            console.error('Error reading cached data:', error);
            localStorage.removeItem(`cache_${key}`);
            return null;
        }
    }

    // Event dispatching for UI updates
    dispatchSyncEvent(event, data) {
        window.dispatchEvent(new CustomEvent(`sync_${event}`, { detail: data }));
    }

    // Real-time update handlers
    handleTransactionUpdate(data) {
        this.dispatchSyncEvent('transaction_updated', data);
    }

    handleTransactionDelete(data) {
        this.dispatchSyncEvent('transaction_deleted', data);
    }

    handleWalletUpdate(data) {
        this.dispatchSyncEvent('wallet_updated', data);
    }

    handleWalletDelete(data) {
        this.dispatchSyncEvent('wallet_deleted', data);
    }

    // Get sync status
    getSyncStatus() {
        return {
            isSyncing: this.isSyncing,
            lastSyncTime: this.lastSyncTime,
            isWebSocketConnected: webSocketService.isConnected()
        };
    }
}

export const syncService = new SyncService();