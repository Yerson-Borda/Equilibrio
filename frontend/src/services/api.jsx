const API_BASE_URL = '/api/v1';

class ApiService {
    constructor() {
        this.token = localStorage.getItem('token');
    }

    setToken(token) {
        this.token = token;
        if (token) {
            localStorage.setItem('token', token);
        } else {
            localStorage.removeItem('token');
        }
    }

    getAuthHeaders() {
        const headers = {
            'Content-Type': 'application/json',
        };

        if (this.token) {
            headers['Authorization'] = `Bearer ${this.token}`;
        }

        return headers;
    }

    async handleResponse(response) {
        const text = await response.text();
        let data;
        try {
            data = text ? JSON.parse(text) : {};
        } catch {
            data = {};
        }

        if (!response.ok) {
            const error = {
                status: response.status,
                message: data?.detail || 'An error occurred',
                errors: data?.detail || [],
            };

            if (response.status === 401) {
                this.setToken(null);
            }

            throw error;
        }

        return data;
    }

    // AUTH ENDPOINTS
    async login(email, password) {
        const url = new URL(`${API_BASE_URL}/users/login`, window.location.origin);
        url.searchParams.append('email', email);
        url.searchParams.append('password', password);

        const response = await fetch(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
        });

        const data = await this.handleResponse(response);
        if (data.access_token) {
            this.setToken(data.access_token);
        }
        return data;
    }

    async register(fullName, email, password) {
        const body = {
            email: email,
            password: password,
            full_name: fullName || null,
        };

        const response = await fetch(`${API_BASE_URL}/users/register`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(body),
        });

        const data = await this.handleResponse(response);
        return data;
    }

    async logout() {
        const response = await fetch(`${API_BASE_URL}/users/logout`, {
            method: 'POST',
            headers: this.getAuthHeaders(),
        });
        this.setToken(null);
        return this.handleResponse(response);
    }

    // USER ENDPOINTS
    async getCurrentUser() {
        const response = await fetch(`${API_BASE_URL}/users/me`, {
            method: 'GET',
            headers: this.getAuthHeaders(),
        });
        return this.handleResponse(response);
    }

    async updateUser(userData) {
        const response = await fetch(`${API_BASE_URL}/users/me`, {
            method: 'PUT',
            headers: this.getAuthHeaders(),
            body: JSON.stringify(userData),
        });
        return this.handleResponse(response);
    }

    async uploadAvatar(file) {
        const formData = new FormData();
        formData.append('file', file);

        const response = await fetch(`${API_BASE_URL}/users/me/avatar`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${this.token}`,
            },
            body: formData,
        });
        return this.handleResponse(response);
    }

    async getDetailedUserInfo() {
        const response = await fetch(`${API_BASE_URL}/users/me/detailed`, {
            method: 'GET',
            headers: this.getAuthHeaders(),
        });
        return this.handleResponse(response);
    }

    // WALLET ENDPOINTS
    async getWallets() {
        const response = await fetch(`${API_BASE_URL}/wallets/`, {
            method: 'GET',
            headers: this.getAuthHeaders(),
        });
        return this.handleResponse(response);
    }

    async createWallet(walletData) {
        const response = await fetch(`${API_BASE_URL}/wallets/`, {
            method: 'POST',
            headers: this.getAuthHeaders(),
            body: JSON.stringify(walletData),
        });
        return this.handleResponse(response);
    }

    async getWallet(walletId) {
        const response = await fetch(`${API_BASE_URL}/wallets/${walletId}`, {
            method: 'GET',
            headers: this.getAuthHeaders(),
        });
        return this.handleResponse(response);
    }

    async updateWallet(walletId, walletData) {
        const response = await fetch(`${API_BASE_URL}/wallets/${walletId}`, {
            method: 'PUT',
            headers: this.getAuthHeaders(),
            body: JSON.stringify(walletData),
        });
        return this.handleResponse(response);
    }

    async deleteWallet(walletId) {
        const response = await fetch(`${API_BASE_URL}/wallets/${walletId}`, {
            method: 'DELETE',
            headers: this.getAuthHeaders(),
        });
        return this.handleResponse(response);
    }

    async getWalletBalance(walletId) {
        const response = await fetch(`${API_BASE_URL}/wallets/${walletId}/balance`, {
            method: 'GET',
            headers: this.getAuthHeaders(),
        });
        return this.handleResponse(response);
    }

    async getUserTotalBalance() {
        const response = await fetch(`${API_BASE_URL}/wallets/user/total`, {
            method: 'GET',
            headers: this.getAuthHeaders(),
        });
        return this.handleResponse(response);
    }

    // TRANSACTION ENDPOINTS
    async getTransactions() {
        const response = await fetch(`${API_BASE_URL}/transactions/`, {
            method: 'GET',
            headers: this.getAuthHeaders(),
        });
        return this.handleResponse(response);
    }

    async createTransaction(transactionData) {
        const response = await fetch(`${API_BASE_URL}/transactions/`, {
            method: 'POST',
            headers: this.getAuthHeaders(),
            body: JSON.stringify(transactionData),
        });
        return this.handleResponse(response);
    }

    async updateTransaction(transactionId, transactionData) {
        const response = await fetch(`${API_BASE_URL}/transactions/${transactionId}`, {
            method: 'PUT',
            headers: this.getAuthHeaders(),
            body: JSON.stringify(transactionData),
        });
        return this.handleResponse(response);
    }

    async deleteTransaction(transactionId) {
        const response = await fetch(`${API_BASE_URL}/transactions/${transactionId}`, {
            method: 'DELETE',
            headers: this.getAuthHeaders(),
        });
        return this.handleResponse(response);
    }

    async getWalletTransactions(walletId) {
        const response = await fetch(`${API_BASE_URL}/transactions/wallet/${walletId}`, {
            method: 'GET',
            headers: this.getAuthHeaders(),
        });
        return this.handleResponse(response);
    }

    async transferFunds(transferData) {
        const response = await fetch(`${API_BASE_URL}/transactions/transfer`, {
            method: 'POST',
            headers: this.getAuthHeaders(),
            body: JSON.stringify(transferData),
        });
        return this.handleResponse(response);
    }

    // CATEGORY ENDPOINTS
    async getCategories() {
        const response = await fetch(`${API_BASE_URL}/categories/`, {
            method: 'GET',
            headers: this.getAuthHeaders(),
        });
        return this.handleResponse(response);
    }

    async createCategory(categoryData) {
        const response = await fetch(`${API_BASE_URL}/categories/`, {
            method: 'POST',
            headers: this.getAuthHeaders(),
            body: JSON.stringify(categoryData),
        });
        return this.handleResponse(response);
    }

    async updateCategory(categoryId, categoryData) {
        const response = await fetch(`${API_BASE_URL}/categories/${categoryId}`, {
            method: 'PUT',
            headers: this.getAuthHeaders(),
            body: JSON.stringify(categoryData),
        });
        return this.handleResponse(response);
    }

    async deleteCategory(categoryId) {
        const response = await fetch(`${API_BASE_URL}/categories/${categoryId}`, {
            method: 'DELETE',
            headers: this.getAuthHeaders(),
        });
        return this.handleResponse(response);
    }

    // SYNC ENDPOINTS
    async syncPull(lastSyncAt = null) {
        const body = lastSyncAt ? { last_sync_at: lastSyncAt } : {};

        const response = await fetch(`${API_BASE_URL}/sync/pull`, {
            method: 'POST',
            headers: this.getAuthHeaders(),
            body: JSON.stringify(body),
        });
        return this.handleResponse(response);
    }

    async syncPush(changes) {
        const response = await fetch(`${API_BASE_URL}/sync/push`, {
            method: 'POST',
            headers: this.getAuthHeaders(),
            body: JSON.stringify(changes),
        });
        return this.handleResponse(response);
    }

    async resolveConflicts(resolutions) {
        const response = await fetch(`${API_BASE_URL}/sync/resolve-conflicts`, {
            method: 'POST',
            headers: this.getAuthHeaders(),
            body: JSON.stringify(resolutions),
        });
        return this.handleResponse(response);
    }

    async getSyncStatus() {
        const response = await fetch(`${API_BASE_URL}/sync/status`, {
            method: 'GET',
            headers: this.getAuthHeaders(),
        });
        return this.handleResponse(response);
    }
}

export const apiService = new ApiService();