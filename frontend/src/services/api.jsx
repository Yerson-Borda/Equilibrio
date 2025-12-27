// ==========================================================
// FULL FINAL API SERVICE — MATCHES YOUR EXACT STRUCTURE
// ==========================================================

const API_BASE_URL = '/api';

class ApiService {
    constructor() {
        this.token = localStorage.getItem('token');
    }

    // ======================================================
    // TOKEN HANDLING
    // ======================================================

    setToken(token) {
        this.token = token;
        if (token) {
            localStorage.setItem('token', token);
        } else {
            localStorage.removeItem('token');
        }
    }

    getAuthHeaders(extraHeaders = {}, withJson = true) {
        const headers = { ...extraHeaders };

        if (withJson) {
            if (!headers['Content-Type']) {
                headers['Content-Type'] = 'application/json';
            }
            if (!headers['Accept']) {
                headers['Accept'] = 'application/json';
            }
        }

        if (this.token) {
            headers['Authorization'] = `Bearer ${this.token}`;
        }

        return headers;
    }

    async handleResponse(response) {
        const text = await response.text();
        let data = null;

        try {
            data = text ? JSON.parse(text) : null;
        } catch {
            data = text || null;
        }

        if (!response.ok) {
            const message =
                (data && (data.detail || data.message)) ||
                `Request failed with status ${response.status}`;
            const error = new Error(message);
            error.status = response.status;
            error.data = data;

            if (response.status === 401) {
                this.setToken(null);
            }

            throw error;
        }

        return data;
    }

    // ======================================================
    // AUTH
    // ======================================================

    async register(fullName, email, password, options = {}) {
        const body = {
            email,
            password,
            full_name: fullName || null,
            phone_number: options.phoneNumber || null,
            date_of_birth: options.dateOfBirth || null,
            avatar_url: options.avatarUrl || null,
            default_currency: options.defaultCurrency || 'USD',
        };

        const response = await fetch(`${API_BASE_URL}/auth/register`, {
            method: 'POST',
            headers: this.getAuthHeaders(),
            body: JSON.stringify(body),
        });

        return this.handleResponse(response);
    }

    async login(email, password) {
        const url = new URL(`${API_BASE_URL}/auth/login`, window.location.origin);
        url.searchParams.set('email', email);
        url.searchParams.set('password', password);

        const response = await fetch(url.toString(), {
            method: 'POST',
            headers: this.getAuthHeaders(),
        });

        const data = await this.handleResponse(response);

        if (data && data.access_token) {
            this.setToken(data.access_token);
        }

        return data;
    }

    async logout() {
        const response = await fetch(`${API_BASE_URL}/auth/logout`, {
            method: 'POST',
            headers: this.getAuthHeaders(),
        });

        const data = await this.handleResponse(response);
        this.setToken(null);
        return data;
    }

    // ======================================================
    // USERS
    // ======================================================

    async getCurrentUser() {
        const response = await fetch(`${API_BASE_URL}/users/me`, {
            method: 'GET',
            headers: this.getAuthHeaders(),
        });

        return this.handleResponse(response);
    }

    async updateUser(updateData) {
        const response = await fetch(`${API_BASE_URL}/users/me`, {
            method: 'PUT',
            headers: this.getAuthHeaders(),
            body: JSON.stringify(updateData),
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

    async uploadAvatar(file) {
        const formData = new FormData();
        formData.append('file', file);

        const response = await fetch(`${API_BASE_URL}/users/me/avatar`, {
            method: 'POST',
            headers: this.getAuthHeaders({}, false),
            body: formData,
        });

        return this.handleResponse(response);
    }

    async deleteAvatar() {
        const response = await fetch(`${API_BASE_URL}/users/me/avatar`, {
            method: 'DELETE',
            headers: this.getAuthHeaders(),
        });

        return this.handleResponse(response);
    }

    // ======================================================
    // WALLETS
    // ======================================================

    async getUserTotalBalance() {
        const response = await fetch(`${API_BASE_URL}/wallets/user/total`, {
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

    /**
     * GET /api/wallets/{wallet_id}
     */
    async getWallet(walletId) {
        const response = await fetch(`${API_BASE_URL}/wallets/${walletId}`, {
            method: 'GET',
            headers: this.getAuthHeaders(),
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

    async createWallet(walletData) {
        const payload = {
            name: walletData.name,
            currency: walletData.currency || 'USD',
            wallet_type: walletData.wallet_type,
            card_number: walletData.card_number || null,
            color: walletData.color || '#3B82F6',
            balance:
                walletData.balance !== undefined && walletData.balance !== null
                    ? walletData.balance
                    : walletData.initial_balance !== undefined &&
                    walletData.initial_balance !== null
                        ? walletData.initial_balance
                        : 0,
        };

        const response = await fetch(`${API_BASE_URL}/wallets/`, {
            method: 'POST',
            headers: this.getAuthHeaders(),
            body: JSON.stringify(payload),
        });

        return this.handleResponse(response);
    }

    async updateWallet(walletId, walletData) {
        const payload = {};

        if (walletData.name !== undefined) payload.name = walletData.name;
        if (walletData.currency !== undefined) payload.currency = walletData.currency;
        if (walletData.wallet_type !== undefined) payload.wallet_type = walletData.wallet_type;
        if (walletData.card_number !== undefined) payload.card_number = walletData.card_number;
        if (walletData.color !== undefined) payload.color = walletData.color;

        if (walletData.balance !== undefined) {
            payload.balance = walletData.balance;
        } else if (walletData.initial_balance !== undefined) {
            payload.balance = walletData.initial_balance;
        }

        const response = await fetch(`${API_BASE_URL}/wallets/${walletId}`, {
            method: 'PUT',
            headers: this.getAuthHeaders(),
            body: JSON.stringify(payload),
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

    // ======================================================
    // TRANSACTIONS
    // ======================================================

    async getTransactions() {
        const response = await fetch(`${API_BASE_URL}/transactions/`, {
            method: 'GET',
            headers: this.getAuthHeaders(),
        });
        return this.handleResponse(response);
    }

    async getWalletTransactions(walletId, limit = 10) {
        const url = new URL(`${API_BASE_URL}/transactions/wallet/${walletId}`, window.location.origin);
        if (limit) url.searchParams.set('limit', String(limit));

        const response = await fetch(url.toString(), {
            method: 'GET',
            headers: this.getAuthHeaders(),
        });

        return this.handleResponse(response);
    }

    // ======================================================
    // UPDATED createTransaction() — Option Y (KEEP ALL ENDPOINTS)
    // ======================================================

    async createTransaction(data) {
        const name =
            data.name ??
            data.title ??
            data.description ??
            (data.type === 'income'
                ? 'Income'
                : data.type === 'expense'
                    ? 'Expense'
                    : 'Transaction');

        const amount = Number(data.amount);
        const note = data.note ?? data.description ?? '';
        const transaction_date =
            data.transaction_date ?? new Date().toISOString().split('T')[0];

        const wallet_id =
            data.wallet_id ??
            data.walletId ??
            (data.wallet && data.wallet.id);

        const category_id =
            data.category_id ??
            data.categoryId ??
            (data.category && data.category.id);

        let tags = [];
        if (Array.isArray(data.tags)) {
            tags = data.tags
                .map(tag => {
                    if (typeof tag === 'number') return tag;
                    if (typeof tag === 'string') {
                        const parsed = parseInt(tag);
                        return isNaN(parsed) ? null : parsed;
                    }
                    if (tag && typeof tag === 'object' && 'id' in tag) return Number(tag.id);
                    return null;
                })
                .filter(Boolean);
        }

        const payload = {
            name,
            amount,
            note,
            type: data.type,
            transaction_date,
            wallet_id,
            category_id,
            tags,
        };

        const response = await fetch(`${API_BASE_URL}/transactions/`, {
            method: 'POST',
            headers: this.getAuthHeaders(),
            body: JSON.stringify(payload),
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

    async createTransfer(transferData) {
        const payload = {
            source_wallet_id: transferData.source_wallet_id,
            destination_wallet_id: transferData.destination_wallet_id,
            amount: transferData.amount,
            note: transferData.note || '',
        };

        const response = await fetch(`${API_BASE_URL}/transactions/transfer`, {
            method: 'POST',
            headers: this.getAuthHeaders(),
            body: JSON.stringify(payload),
        });

        return this.handleResponse(response);
    }

    // ======================================================
    // CATEGORIES
    // ======================================================

    async getCategories() {
        const response = await fetch(`${API_BASE_URL}/categories/`, {
            method: 'GET',
            headers: this.getAuthHeaders(),
        });
        return this.handleResponse(response);
    }

    async getIncomeCategories() {
        const response = await fetch(`${API_BASE_URL}/categories/income`, {
            method: 'GET',
            headers: this.getAuthHeaders(),
        });
        return this.handleResponse(response);
    }

    async getExpenseCategories() {
        const response = await fetch(`${API_BASE_URL}/categories/expense`, {
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

    async deleteCategory(categoryId) {
        const response = await fetch(`${API_BASE_URL}/categories/${categoryId}`, {
            method: 'DELETE',
            headers: this.getAuthHeaders(),
        });
        return this.handleResponse(response);
    }

    // ======================================================
    // BUDGET
    // ======================================================

    async getCurrentBudget() {
        const response = await fetch(`${API_BASE_URL}/budget/current`, {
            method: 'GET',
            headers: this.getAuthHeaders(),
        });
        return this.handleResponse(response);
    }

    async updateCurrentBudget(budgetUpdate) {
        const response = await fetch(`${API_BASE_URL}/budget/current`, {
            method: 'PUT',
            headers: this.getAuthHeaders(),
            body: JSON.stringify(budgetUpdate),
        });
        return this.handleResponse(response);
    }

    // ======================================================
    // CATEGORY LIMITS (NEW)
    // ======================================================

    async getCategoryLimitsOverview() {
        const response = await fetch(`${API_BASE_URL}/limits`, {
            method: 'GET',
            headers: this.getAuthHeaders(),
        });
        return this.handleResponse(response);
    }

    async setCategoryLimit(categoryId, monthlyLimit) {
        const response = await fetch(`${API_BASE_URL}/limits/${categoryId}`, {
            method: 'PUT',
            headers: this.getAuthHeaders(),
            body: JSON.stringify({ monthly_limit: monthlyLimit }),
        });
        return this.handleResponse(response);
    }

    async deleteCategoryLimit(categoryId) {
        const response = await fetch(`${API_BASE_URL}/limits/${categoryId}`, {
            method: 'DELETE',
            headers: this.getAuthHeaders(),
        });
        return this.handleResponse(response);
    }

    // ======================================================
    // TAGS — matches backend spec
    // ======================================================

    /** GET /api/tags/ */
    async getTags() {
        const response = await fetch(`${API_BASE_URL}/tags/`, {
            method: 'GET',
            headers: this.getAuthHeaders(),
        });
        // Spec returns an array of TagResponse
        return this.handleResponse(response);
    }

    /** POST /api/tags/  Body: { name: string } */
    async createTag(tag) {
        const name = typeof tag === 'string' ? tag : (tag?.name ?? '');
        const response = await fetch(`${API_BASE_URL}/tags/`, {
            method: 'POST',
            headers: this.getAuthHeaders(),
            body: JSON.stringify({ name }),
        });
        return this.handleResponse(response);
    }

    /** DELETE /api/tags/{tag_id} */
    async deleteTag(tagId) {
        const response = await fetch(`${API_BASE_URL}/tags/${tagId}`, {
            method: 'DELETE',
            headers: this.getAuthHeaders(),
        });
        // 204 No Content → handleResponse will return null
        return this.handleResponse(response);
    }

    // ======================================================
    // TRANSACTIONS by Tag — from spec
    // GET /api/transactions/filter/by-tag/{tag_id}
    // ======================================================
    async getTransactionsByTag(tagId) {
        const response = await fetch(
            `${API_BASE_URL}/transactions/filter/by-tag/${tagId}`,
            {
                method: 'GET',
                headers: this.getAuthHeaders(),
            }
        );
        return this.handleResponse(response);
    }

    // ======================================================
    // FINANCIAL SUMMARY
    // ======================================================

    async getCurrentSummary() {
        const response = await fetch(`${API_BASE_URL}/financial_summary/current`, {
            method: 'GET',
            headers: this.getAuthHeaders(),
        });
        return this.handleResponse(response);
    }

    // ======================================================
    // ANALYTICS
    // ======================================================

    async getCategorySummary(startDate, endDate) {
        const url = new URL(`${API_BASE_URL}/analytics/category-summary`, window.location.origin);
        url.searchParams.set('start_date', startDate);
        url.searchParams.set('end_date', endDate);

        const response = await fetch(url.toString(), {
            method: 'GET',
            headers: this.getAuthHeaders(),
        });

        return this.handleResponse(response);
    }

    async getMonthlyComparison(month) {
        const url = new URL(`${API_BASE_URL}/analytics/monthly-comparison`, window.location.origin);
        url.searchParams.set('month', month);

        const response = await fetch(url.toString(), {
            method: 'GET',
            headers: this.getAuthHeaders(),
        });

        return this.handleResponse(response);
    }

    async getSpendingTrends(months = 6) {
        const url = new URL(`${API_BASE_URL}/analytics/spending-trends`, window.location.origin);
        if (months) url.searchParams.set('months', String(months));

        const response = await fetch(url.toString(), {
            method: 'GET',
            headers: this.getAuthHeaders(),
        });

        return this.handleResponse(response);
    }

    // 🔥 NEW: Top 3 categories
    async getTopCategoriesCurrentMonth() {
        const response = await fetch(
            `${API_BASE_URL}/analytics/top-categories/current-month`,
            {
                method: 'GET',
                headers: this.getAuthHeaders(),
            }
        );
        return this.handleResponse(response);
    }

    // 🔥 NEW: Average spending
    async getAverageSpending(period = 'month') {
        const url = new URL(`${API_BASE_URL}/analytics/average-spending`, window.location.origin);
        url.searchParams.set('period', period);

        const response = await fetch(url, {
            method: 'GET',
            headers: this.getAuthHeaders(),
        });

        return this.handleResponse(response);
    }

    // ======================================================
    // MISC
    // ======================================================

    async healthCheck() {
        const response = await fetch(`/health`, {
            method: 'GET',
        });
        return this.handleResponse(response);
    }

    async root() {
        const response = await fetch(`/`, {
            method: 'GET',
        });
        return this.handleResponse(response);
    }
}

export const apiService = new ApiService();
