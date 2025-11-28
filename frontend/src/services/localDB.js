// Local IndexedDB service for offline storage and caching
class LocalDB {
    constructor() {
        this.dbName = 'FinanceTrackerDB';
        this.version = 1;
        this.db = null;
    }

    async init() {
        return new Promise((resolve, reject) => {
            const request = indexedDB.open(this.dbName, this.version);

            request.onerror = () => reject(request.error);
            request.onsuccess = () => {
                this.db = request.result;
                resolve(this.db);
            };

            request.onupgradeneeded = (event) => {
                const db = event.target.result;

                // Create object stores for each entity type
                if (!db.objectStoreNames.contains('users')) {
                    const userStore = db.createObjectStore('users', { keyPath: 'id' });
                    userStore.createIndex('email', 'email', { unique: true });
                    userStore.createIndex('lastSync', 'last_sync_at');
                }

                if (!db.objectStoreNames.contains('wallets')) {
                    const walletStore = db.createObjectStore('wallets', { keyPath: 'id' });
                    walletStore.createIndex('userId', 'user_id');
                    walletStore.createIndex('lastSync', 'updated_at');
                    walletStore.createIndex('isDeleted', 'is_deleted');
                }

                if (!db.objectStoreNames.contains('categories')) {
                    const categoryStore = db.createObjectStore('categories', { keyPath: 'id' });
                    categoryStore.createIndex('userId', 'user_id');
                    categoryStore.createIndex('lastSync', 'updated_at');
                    categoryStore.createIndex('isDeleted', 'is_deleted');
                }

                if (!db.objectStoreNames.contains('transactions')) {
                    const transactionStore = db.createObjectStore('transactions', { keyPath: 'id' });
                    transactionStore.createIndex('userId', 'user_id');
                    transactionStore.createIndex('walletId', 'wallet_id');
                    transactionStore.createIndex('lastSync', 'updated_at');
                    transactionStore.createIndex('isDeleted', 'is_deleted');
                }

                if (!db.objectStoreNames.contains('syncMetadata')) {
                    const syncStore = db.createObjectStore('syncMetadata', { keyPath: 'key' });
                }
            };
        });
    }

    // Generic CRUD operations
    async add(storeName, data) {
        const transaction = this.db.transaction([storeName], 'readwrite');
        const store = transaction.objectStore(storeName);
        return new Promise((resolve, reject) => {
            const request = store.add(data);
            request.onsuccess = () => resolve(request.result);
            request.onerror = () => reject(request.error);
        });
    }

    async put(storeName, data) {
        const transaction = this.db.transaction([storeName], 'readwrite');
        const store = transaction.objectStore(storeName);
        return new Promise((resolve, reject) => {
            const request = store.put(data);
            request.onsuccess = () => resolve(request.result);
            request.onerror = () => reject(request.error);
        });
    }

    async get(storeName, key) {
        const transaction = this.db.transaction([storeName], 'readonly');
        const store = transaction.objectStore(storeName);
        return new Promise((resolve, reject) => {
            const request = store.get(key);
            request.onsuccess = () => resolve(request.result);
            request.onerror = () => reject(request.error);
        });
    }

    async getAll(storeName, indexName = null, query = null) {
        const transaction = this.db.transaction([storeName], 'readonly');
        const store = transaction.objectStore(storeName);
        const target = indexName ? store.index(indexName) : store;

        return new Promise((resolve, reject) => {
            const request = query ? target.getAll(query) : target.getAll();
            request.onsuccess = () => resolve(request.result);
            request.onerror = () => reject(request.error);
        });
    }

    async delete(storeName, key) {
        const transaction = this.db.transaction([storeName], 'readwrite');
        const store = transaction.objectStore(storeName);
        return new Promise((resolve, reject) => {
            const request = store.delete(key);
            request.onsuccess = () => resolve(request.result);
            request.onerror = () => reject(request.error);
        });
    }

    // Sync metadata operations - FIXED VERSION
    async getSyncMetadata() {
        const metadata = await this.get('syncMetadata', 'lastSync');
        // Return default metadata if none exists
        return metadata || {
            lastSyncAt: null,
            userSyncVersion: 0,
            pendingChanges: []
        };
    }

    async setSyncMetadata(metadata) {
        return this.put('syncMetadata', {
            key: 'lastSync',
            ...metadata
        });
    }

    // Entity-specific methods
    async getUser() {
        const users = await this.getAll('users');
        return users.length > 0 ? users[0] : null;
    }

    async saveUser(user) {
        return this.put('users', user);
    }

    async getWallets() {
        return this.getAll('wallets', 'isDeleted', false);
    }

    async saveWallet(wallet) {
        return this.put('wallets', wallet);
    }

    async getCategories() {
        return this.getAll('categories', 'isDeleted', false);
    }

    async saveCategory(category) {
        return this.put('categories', category);
    }

    async getTransactions() {
        return this.getAll('transactions', 'isDeleted', false);
    }

    async saveTransaction(transaction) {
        return this.put('transactions', transaction);
    }

    // Get pending changes for sync
    async getPendingChanges() {
        const metadata = await this.getSyncMetadata();
        return metadata.pendingChanges || [];
    }

    // Add pending change
    async addPendingChange(change) {
        const metadata = await this.getSyncMetadata();
        metadata.pendingChanges = metadata.pendingChanges || [];
        metadata.pendingChanges.push({
            ...change,
            id: Date.now(), // Unique ID for the change
            timestamp: new Date().toISOString()
        });
        await this.setSyncMetadata(metadata);
    }

    // Clear pending changes after successful sync
    async clearPendingChanges() {
        const metadata = await this.getSyncMetadata();
        metadata.pendingChanges = [];
        await this.setSyncMetadata(metadata);
    }
}

export const localDB = new LocalDB();