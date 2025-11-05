import { apiService } from '././api.jsx';
import { localDB } from '././localDB.js';

class SyncService {
    constructor() {
        this.isSyncing = false;
        this.syncInterval = 5 * 60 * 1000; // 5 minutes
        this.syncTimer = null;
        this.offlineQueue = [];
        this.isOnline = navigator.onLine;

        // Listen for online/offline events
        window.addEventListener('online', this.handleOnline.bind(this));
        window.addEventListener('offline', this.handleOffline.bind(this));
    }

    async init() {
        try {
            await localDB.init();
            console.log('Local database initialized');
        } catch (error) {
            console.error('Failed to initialize local database:', error);
        }
    }

    startPeriodicSync() {
        if (this.syncTimer) {
            clearInterval(this.syncTimer);
        }

        this.syncTimer = setInterval(() => {
            if (this.isOnline) {
                this.sync().catch(error => {
                    console.error('Periodic sync failed:', error);
                });
            }
        }, this.syncInterval);

        console.log('Periodic sync started (5 minutes interval)');
    }

    stopPeriodicSync() {
        if (this.syncTimer) {
            clearInterval(this.syncTimer);
            this.syncTimer = null;
        }
        console.log('Periodic sync stopped');
    }

    handleOnline() {
        this.isOnline = true;
        console.log('App is online, triggering background sync...');
        this.sync().catch(error => {
            console.error('Online sync failed:', error);
        });
        this.processOfflineQueue();
    }

    handleOffline() {
        this.isOnline = false;
        console.log('App is offline, queuing operations...');
    }

    // Main sync method
    async sync() {
        if (this.isSyncing || !this.isOnline) {
            return false;
        }

        this.isSyncing = true;

        try {
            console.log('Starting synchronization...');

            // Get last sync metadata
            const metadata = await localDB.getSyncMetadata();
            const lastSyncAt = metadata.lastSyncAt;

            // Step 1: Push local changes to server
            await this.pushChanges();

            // Step 2: Pull changes from server
            await this.pullChanges(lastSyncAt);

            // Update last sync time
            await localDB.setSyncMetadata({
                ...metadata,
                lastSyncAt: new Date().toISOString()
            });

            console.log('Synchronization completed successfully');
            return true;
        } catch (error) {
            console.error('Synchronization failed:', error);
            return false;
        } finally {
            this.isSyncing = false;
        }
    }

    // Push local changes to server
    async pushChanges() {
        try {
            const pendingChanges = await localDB.getPendingChanges();

            if (pendingChanges.length === 0) {
                return;
            }

            console.log(`Pushing ${pendingChanges.length} changes to server...`);

            const changes = {
                wallets: [],
                categories: [],
                transactions: []
            };

            // Group changes by entity type
            for (const change of pendingChanges) {
                if (changes[change.entityType]) {
                    changes[change.entityType].push(change.data);
                }
            }

            // Send changes to server
            const syncRequest = {
                last_sync_at: await this.getLastSyncTime(),
                changes: changes
            };

            const response = await apiService.syncPush(syncRequest);

            // Handle conflicts if any
            if (response.conflicts && response.conflicts.length > 0) {
                await this.resolveConflicts(response.conflicts);
            }

            // Clear pending changes after successful push
            await localDB.clearPendingChanges();

            console.log('Changes pushed successfully');
        } catch (error) {
            console.error('Failed to push changes:', error);
            throw error;
        }
    }

    // Pull changes from server
    async pullChanges(lastSyncAt = null) {
        try {
            console.log('Pulling changes from server...');

            const syncData = await apiService.syncPull(lastSyncAt);

            // Apply server changes to local database
            await this.applyServerChanges(syncData.changes);

            // Update user sync version
            const user = await localDB.getUser();
            if (user) {
                user.sync_version = syncData.user_sync_version;
                user.last_sync_at = syncData.current_server_time;
                await localDB.saveUser(user);
            }

            console.log('Changes pulled successfully');
        } catch (error) {
            console.error('Failed to pull changes:', error);
            throw error;
        }
    }

    // Apply server changes to local database
    async applyServerChanges(changes) {
        for (const [entityType, entities] of Object.entries(changes)) {
            for (const entity of entities) {
                if (entity.is_deleted) {
                    // Mark as deleted locally
                    await localDB.put(entityType, {
                        ...entity,
                        is_deleted: true
                    });
                } else {
                    // Update or create entity
                    await localDB.put(entityType, entity.data);
                }
            }
        }
    }

    // Resolve conflicts (basic implementation - server wins)
    async resolveConflicts(conflicts) {
        console.log(`Resolving ${conflicts.length} conflicts...`);

        for (const conflict of conflicts) {
            // For now, we'll use server version
            // In a real app, you might show a UI to let users choose
            await localDB.put(conflict.entity_type, conflict.server_data);
        }
    }

    // Queue operation for offline mode
    async queueOperation(operation) {
        if (this.isOnline) {
            return await this.executeOperation(operation);
        } else {
            this.offlineQueue.push(operation);
            await localDB.addPendingChange({
                entityType: operation.entityType,
                operation: operation.type,
                data: operation.data
            });
            return operation.data; // Return optimistic response
        }
    }

    // Execute operation (online)
    async executeOperation(operation) {
        switch (operation.type) {
            case 'CREATE_WALLET':
                return await apiService.createWallet(operation.data);
            case 'UPDATE_WALLET':
                return await apiService.updateWallet(operation.data.id, operation.data);
            case 'DELETE_WALLET':
                return await apiService.deleteWallet(operation.data.id);
            case 'CREATE_TRANSACTION':
                return await apiService.createTransaction(operation.data);
            case 'UPDATE_TRANSACTION':
                return await apiService.updateTransaction(operation.data.id, operation.data);
            case 'DELETE_TRANSACTION':
                return await apiService.deleteTransaction(operation.data.id);
            case 'CREATE_CATEGORY':
                return await apiService.createCategory(operation.data);
            case 'UPDATE_CATEGORY':
                return await apiService.updateCategory(operation.data.id, operation.data);
            case 'DELETE_CATEGORY':
                return await apiService.deleteCategory(operation.data.id);
            default:
                throw new Error(`Unknown operation type: ${operation.type}`);
        }
    }

    // Process queued operations when coming online
    async processOfflineQueue() {
        while (this.offlineQueue.length > 0 && this.isOnline) {
            const operation = this.offlineQueue.shift();
            try {
                await this.executeOperation(operation);
            } catch (error) {
                console.error('Failed to process queued operation:', error);
                // Re-queue failed operation
                this.offlineQueue.unshift(operation);
                break;
            }
        }
    }

    // Get last sync time
    async getLastSyncTime() {
        try {
            const metadata = await localDB.getSyncMetadata();
            return metadata.lastSyncAt;
        } catch (error) {
            console.error('Error getting last sync time:', error);
            return null;
        }
    }

    // Check if data is fresh (less than 5 minutes old)
    async isDataFresh() {
        const lastSync = await this.getLastSyncTime();
        if (!lastSync) return false;

        const fiveMinutesAgo = new Date(Date.now() - 5 * 60 * 1000);
        return new Date(lastSync) > fiveMinutesAgo;
    }

    // Get data with fallback to local cache
    async getDataWithFallback(apiCall, localKey, forceRefresh = false) {
        if (!forceRefresh && await this.isDataFresh()) {
            const localData = await localDB.getAll(localKey);
            if (localData.length > 0) {
                console.log('Using cached data for:', localKey);
                return localData;
            }
        }

        // Fetch from API and cache
        try {
            const data = await apiCall();
            await this.cacheData(localKey, data);
            return data;
        } catch (error) {
            console.error('API call failed, using cached data:', error);
            const localData = await localDB.getAll(localKey);
            return localData;
        }
    }

    // Cache data locally
    async cacheData(storeName, data) {
        for (const item of data) {
            await localDB.put(storeName, item);
        }
    }
}

export const syncService = new SyncService();