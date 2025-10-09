const API_BASE_URL = 'http://localhost:8000/api/v1';

class ApiService {
    constructor() {
        this.token = localStorage.getItem('token');
    }

    setToken(token) {
        this.token = token;
        localStorage.setItem('token', token);
    }

    getAuthHeaders() {
        return {
            'Content-Type': 'application/json',
            ...(this.token && { 'Authorization': `Bearer ${this.token}` }),
        };
    }

    async handleResponse(response) {
        if (!response.ok) {
            const errorData = await response.json().catch(() => null);
            throw {
                status: response.status,
                message: errorData?.detail || 'An error occurred',
                errors: errorData?.detail || [],
            };
        }
        return response.json();
    }

    // Auth endpoints
    async login(email, password) {
        const response = await fetch(`${API_BASE_URL}/users/login`, {
            method: 'POST',
            headers: this.getAuthHeaders(),
            body: JSON.stringify({
                username: email, // Note: API expects 'username' but we're using email
                password: password,
            }),
        });

        const data = await this.handleResponse(response);

        if (data.access_token) {
            this.setToken(data.access_token);
        }

        return data;
    }

    async register(fullName, email, password) {
        const response = await fetch(`${API_BASE_URL}/users/register`, {
            method: 'POST',
            headers: this.getAuthHeaders(),
            body: JSON.stringify({
                full_name: fullName,
                email: email,
                password: password,
            }),
        });

        return this.handleResponse(response);
    }

    async getCurrentUser() {
        const response = await fetch(`${API_BASE_URL}/users/me`, {
            method: 'GET',
            headers: this.getAuthHeaders(),
        });

        return this.handleResponse(response);
    }

    // Wallet endpoints
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
}

export const apiService = new ApiService();