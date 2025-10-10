const API_BASE_URL = '/api/v1';

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
        const text = await response.text();
        let data;
        try {
            data = text ? JSON.parse(text) : {};
        } catch {
            data = {};
        }

        if (!response.ok) {
            throw {
                status: response.status,
                message: data?.detail || 'An error occurred',
                errors: data?.detail || [],
            };
        }

        return data;
    }

    // LOGIN (backend expects query params)
    async login(email, password) {
        const url = new URL(`${API_BASE_URL}/users/login`, window.location.origin);
        url.searchParams.append('email', email);
        url.searchParams.append('password', password);

        const response = await fetch(url, {
            method: 'POST',
            headers: this.getAuthHeaders(),
        });

        const data = await this.handleResponse(response);
        if (data.access_token) this.setToken(data.access_token);
        return data;
    }

    // REGISTER (backend expects JSON)
    async register(fullName, email, password) {
        const body = {
            email,
            password,
            full_name: fullName || null,
        };
        const response = await fetch(`${API_BASE_URL}/users/register`, {
            method: 'POST',
            headers: this.getAuthHeaders(),
            body: JSON.stringify(body),
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
