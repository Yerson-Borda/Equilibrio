import { apiService } from './api.jsx';
import { webSocketService } from './websocketService.js';

class SyncService {
    constructor() {
        this.isSyncing = false;
        this.lastSyncTime = null;
        this.syncInterval = 30000; // 30 seconds
        this.syncTimer = null;

        // Simple event system: eventName -> Set(listeners)
        this.listeners = new Map();
    }

    // ==============================
    // Public API used by the app
    // ==============================

    // Called after login
    initialize(userId) {
        console.log('Initializing sync service for user:', userId);

        // WebSocket connect is handled by websocketService elsewhere,
        // but we can still log status here.
        if (userId) {
            if (!webSocketService.isConnected?.()) {
                webSocketService.connect(userId);
            }
        }

        this.startSync();
    }

    // Start periodic background sync
    startSync() {
        if (this.syncTimer) return;

        // Do one immediate sync, then schedule
        this.syncNow();

        this.syncTimer = setInterval(() => {
            this.syncNow();
        }, this.syncInterval);
    }

    // Stop periodic sync (called on logout)
    stopSync() {
        if (this.syncTimer) {
            clearInterval(this.syncTimer);
            this.syncTimer = null;
        }
        this.isSyncing = false;
        console.log('Sync service stopped');
    }

    // Manual one-shot sync
    async syncNow() {
        if (this.isSyncing) {
            console.log('Sync already in progress, skipping');
            return;
        }

        this.isSyncing = true;
        console.log('🔄 Sync started');

        try {
            const [wallets, transactions, categories] = await Promise.all([
                apiService.getWallets(),
                apiService.getTransactions(),
                apiService.getCategories(),
            ]);

            this.cacheData('wallets', wallets);
            this.cacheData('transactions', transactions);
            this.cacheData('categories', categories);

            this.lastSyncTime = new Date().toISOString();

            // Notify listeners that fresh data is available
            this.dispatchSyncEvent('wallet_synced', wallets);
            this.dispatchSyncEvent('transaction_synced', transactions);
            this.dispatchSyncEvent('category_synced', categories);

            console.log('✅ Sync finished at', this.lastSyncTime);
        } catch (error) {
            console.error('❌ Sync failed:', error);
        } finally {
            this.isSyncing = false;
        }
    }

    // Store data in localStorage as a simple cache
    cacheData(key, data) {
        try {
            localStorage.setItem(
                `equilibrio_${key}`,
                JSON.stringify({
                    data,
                    timestamp: new Date().toISOString(),
                })
            );
        } catch (error) {
            console.error('Failed to cache data for', key, error);
        }
    }

    // Retrieve cached data
    getCachedData(key, defaultValue = null) {
        try {
            const raw = localStorage.getItem(`equilibrio_${key}`);
            if (!raw) return defaultValue;

            const parsed = JSON.parse(raw);
            return parsed.data ?? defaultValue;
        } catch (error) {
            console.error('Failed to read cached data for', key, error);
            return defaultValue;
        }
    }

    // ==============================
    // Event system
    // ==============================

    addListener(eventName, callback) {
        if (!eventName || typeof callback !== 'function') return;

        if (!this.listeners.has(eventName)) {
            this.listeners.set(eventName, new Set());
        }

        this.listeners.get(eventName).add(callback);
    }

    removeListener(eventName, callback) {
        if (!this.listeners.has(eventName)) return;

        if (!callback) {
            // Remove all listeners for this event
            this.listeners.delete(eventName);
            return;
        }

        const set = this.listeners.get(eventName);
        set.delete(callback);
        if (set.size === 0) {
            this.listeners.delete(eventName);
        }
    }

    dispatchSyncEvent(eventName, payload) {
        const listeners = this.listeners.get(eventName);
        if (!listeners || listeners.size === 0) return;

        listeners.forEach((cb) => {
            try {
                cb(payload);
            } catch (error) {
                console.error(`Error in syncService listener for "${eventName}":`, error);
            }
        });
    }

    // ==============================
    // Hooks that can be called from WebSocket events
    // (optional, but keeps compatibility with older code)
    // ==============================

    handleWalletCreate(data) {
        this.cacheData('wallets', data);
        this.dispatchSyncEvent('wallet_synced', data);
    }

    handleWalletUpdate(data) {
        this.dispatchSyncEvent('wallet_synced', data);
    }

    handleWalletDelete(data) {
        this.dispatchSyncEvent('wallet_deleted', data);
    }

    // Get overall sync status (useful for Settings / debug UI)
    getSyncStatus() {
        return {
            isSyncing: this.isSyncing,
            lastSyncTime: this.lastSyncTime,
            isWebSocketConnected: webSocketService.isConnected
                ? webSocketService.isConnected()
                : false,
        };
    }
}

export const syncService = new SyncService();
