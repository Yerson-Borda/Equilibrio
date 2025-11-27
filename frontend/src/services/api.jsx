const API_BASE_URL = '/api';

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
                // Token invalid/expired on backend
                this.setToken(null);
            }

            throw error;
        }

        return data;
    }

    // ======================================================
    // AUTH
    // ======================================================

    /**
     * POST /api/auth/register
     * Body: UserCreate
     */
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

    /**
     * POST /api/auth/login
     * email & password are query parameters in the OpenAPI spec
     */
    async login(email, password) {
        const url = new URL(`${API_BASE_URL}/auth/login`, window.location.origin);
        url.searchParams.set('email', email);
        url.searchParams.set('password', password);

        const response = await fetch(url.toString(), {
            method: 'POST',
            headers: this.getAuthHeaders(),
        });

        const data = await this.handleResponse(response);

        // Token schema: { access_token, token_type }
        if (data && data.access_token) {
            this.setToken(data.access_token);
        }

        return data;
    }

    /**
     * POST /api/auth/logout
     */
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

    /**
     * GET /api/users/me
     * Returns UserResponse
     */
    async getCurrentUser() {
        const response = await fetch(`${API_BASE_URL}/users/me`, {
            method: 'GET',
            headers: this.getAuthHeaders(),
        });

        return this.handleResponse(response);
    }

    /**
     * PUT /api/users/me
     * Body: UserUpdate
     */
    async updateUser(updateData) {
        const response = await fetch(`${API_BASE_URL}/users/me`, {
            method: 'PUT',
            headers: this.getAuthHeaders(),
            body: JSON.stringify(updateData),
        });

        return this.handleResponse(response);
    }

    /**
     * GET /api/users/me/detailed
     * Returns an object with extended info
     */
    async getDetailedUserInfo() {
        const response = await fetch(`${API_BASE_URL}/users/me/detailed`, {
            method: 'GET',
            headers: this.getAuthHeaders(),
        });

        return this.handleResponse(response);
    }

    /**
     * POST /api/users/me/avatar
     * multipart/form-data
     */
    async uploadAvatar(file) {
        const formData = new FormData();
        formData.append('file', file);

        const response = await fetch(`${API_BASE_URL}/users/me/avatar`, {
            method: 'POST',
            // No JSON headers for multipart
            headers: this.getAuthHeaders({}, false),
            body: formData,
        });

        return this.handleResponse(response);
    }

    /**
     * DELETE /api/users/me/avatar
     */
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

    /**
     * GET /api/wallets/user/total
     * Returns total balance for current user.
     * (Shape is backend-defined; often { total_balance: "123.45" })
     */
    async getUserTotalBalance() {
        const response = await fetch(`${API_BASE_URL}/wallets/user/total`, {
            method: 'GET',
            headers: this.getAuthHeaders(),
        });

        return this.handleResponse(response);
    }

    /**
     * GET /api/wallets/
     * Returns WalletResponse[]
     */
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

    /**
     * POST /api/wallets/
     * Body: WalletCreate
     * { name, currency?, wallet_type, card_number?, color?, balance? }
     *
     * NOTE:
     *   frontend might pass `initial_balance`; we map it to backend `balance` here.
     */
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

    /**
     * PUT /api/wallets/{wallet_id}
     * Body: WalletUpdate (all fields optional)
     */
    async updateWallet(walletId, walletData) {
        const payload = {};

        if (walletData.name !== undefined) payload.name = walletData.name;
        if (walletData.currency !== undefined) payload.currency = walletData.currency;
        if (walletData.wallet_type !== undefined)
            payload.wallet_type = walletData.wallet_type;
        if (walletData.card_number !== undefined)
            payload.card_number = walletData.card_number;
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

    /**
     * DELETE /api/wallets/{wallet_id}
     */
    async deleteWallet(walletId) {
        const response = await fetch(`${API_BASE_URL}/wallets/${walletId}`, {
            method: 'DELETE',
            headers: this.getAuthHeaders(),
        });

        // Backend returns 204 with no body
        return this.handleResponse(response);
    }

    /**
     * GET /api/wallets/{wallet_id}/balance
     */
    async getWalletBalance(walletId) {
        const response = await fetch(
            `${API_BASE_URL}/wallets/${walletId}/balance`,
            {
                method: 'GET',
                headers: this.getAuthHeaders(),
            }
        );

        return this.handleResponse(response);
    }

    // ======================================================
    // TRANSACTIONS
    // ======================================================

    /**
     * GET /api/transactions/
     * Returns TransactionResponse[]
     */
    async getTransactions() {
        const response = await fetch(`${API_BASE_URL}/transactions/`, {
            method: 'GET',
            headers: this.getAuthHeaders(),
        });

        return this.handleResponse(response);
    }

    /**
     * GET /api/transactions/wallet/{wallet_id}?limit=10
     */
    async getWalletTransactions(walletId, limit = 10) {
        const url = new URL(
            `${API_BASE_URL}/transactions/wallet/${walletId}`,
            window.location.origin
        );
        if (limit) {
            url.searchParams.set('limit', String(limit));
        }

        const response = await fetch(url.toString(), {
            method: 'GET',
            headers: this.getAuthHeaders(),
        });

        return this.handleResponse(response);
    }

    /**
     * POST /api/transactions/
     * Body: TransactionCreate
     * { amount, note, type, transaction_date, wallet_id, category_id }
     *
     * NOTE:
     *   If frontend passes `description`, we map it to `note` here.
     */
    async createTransaction(transactionData) {
        const payload = {
            amount: transactionData.amount,
            note:
                transactionData.note !== undefined
                    ? transactionData.note
                    : transactionData.description || '',
            type: transactionData.type,
            transaction_date: transactionData.transaction_date,
            wallet_id: transactionData.wallet_id,
            category_id: transactionData.category_id,
        };

        const response = await fetch(`${API_BASE_URL}/transactions/`, {
            method: 'POST',
            headers: this.getAuthHeaders(),
            body: JSON.stringify(payload),
        });

        return this.handleResponse(response);
    }

    /**
     * DELETE /api/transactions/{transaction_id}
     */
    async deleteTransaction(transactionId) {
        const response = await fetch(
            `${API_BASE_URL}/transactions/${transactionId}`,
            {
                method: 'DELETE',
                headers: this.getAuthHeaders(),
            }
        );

        return this.handleResponse(response);
    }

    /**
     * POST /api/transactions/transfer
     * Body: TransferCreate
     * { source_wallet_id, destination_wallet_id, amount, note? }
     */
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

    /**
     * GET /api/categories/
     */
    async getCategories() {
        const response = await fetch(`${API_BASE_URL}/categories/`, {
            method: 'GET',
            headers: this.getAuthHeaders(),
        });

        return this.handleResponse(response);
    }

    /**
     * GET /api/categories/income
     */
    async getIncomeCategories() {
        const response = await fetch(`${API_BASE_URL}/categories/income`, {
            method: 'GET',
            headers: this.getAuthHeaders(),
        });

        return this.handleResponse(response);
    }

    /**
     * GET /api/categories/expense
     */
    async getExpenseCategories() {
        const response = await fetch(`${API_BASE_URL}/categories/expense`, {
            method: 'GET',
            headers: this.getAuthHeaders(),
        });

        return this.handleResponse(response);
    }

    /**
     * POST /api/categories/
     * Body: CategoryCreate
     */
    async createCategory(categoryData) {
        const response = await fetch(`${API_BASE_URL}/categories/`, {
            method: 'POST',
            headers: this.getAuthHeaders(),
            body: JSON.stringify(categoryData),
        });

        return this.handleResponse(response);
    }

    /**
     * DELETE /api/categories/{category_id}
     */
    async deleteCategory(categoryId) {
        const response = await fetch(
            `${API_BASE_URL}/categories/${categoryId}`,
            {
                method: 'DELETE',
                headers: this.getAuthHeaders(),
            }
        );

        return this.handleResponse(response);
    }

    // ======================================================
    // BUDGET
    // ======================================================

    /**
     * GET /api/budget/current
     * Returns BudgetResponse
     */
    async getCurrentBudget() {
        const response = await fetch(`${API_BASE_URL}/budget/current`, {
            method: 'GET',
            headers: this.getAuthHeaders(),
        });

        return this.handleResponse(response);
    }

    /**
     * PUT /api/budget/current
     * Body: BudgetUpdate { monthly_limit?, daily_limit? }
     */
    async updateCurrentBudget(budgetUpdate) {
        const response = await fetch(`${API_BASE_URL}/budget/current`, {
            method: 'PUT',
            headers: this.getAuthHeaders(),
            body: JSON.stringify(budgetUpdate),
        });

        return this.handleResponse(response);
    }

    // ======================================================
    // FINANCIAL SUMMARY
    // ======================================================

    /**
     * GET /api/financial_summary/current
     * Returns FinancialSummaryResponse
     */
    async getCurrentSummary() {
        const response = await fetch(
            `${API_BASE_URL}/financial_summary/current`,
            {
                method: 'GET',
                headers: this.getAuthHeaders(),
            }
        );

        return this.handleResponse(response);
    }

    // ======================================================
    // ANALYTICS
    // ======================================================

    /**
     * GET /api/analytics/category-summary
     * Query: start_date=YYYY-MM-DD, end_date=YYYY-MM-DD
     */
    async getCategorySummary(startDate, endDate) {
        const url = new URL(
            `${API_BASE_URL}/analytics/category-summary`,
            window.location.origin
        );
        url.searchParams.set('start_date', startDate);
        url.searchParams.set('end_date', endDate);

        const response = await fetch(url.toString(), {
            method: 'GET',
            headers: this.getAuthHeaders(),
        });

        return this.handleResponse(response);
    }

    /**
     * GET /api/analytics/monthly-comparison
     * Query: month=YYYY-MM
     */
    async getMonthlyComparison(month) {
        const url = new URL(
            `${API_BASE_URL}/analytics/monthly-comparison`,
            window.location.origin
        );
        url.searchParams.set('month', month);

        const response = await fetch(url.toString(), {
            method: 'GET',
            headers: this.getAuthHeaders(),
        });

        return this.handleResponse(response);
    }

    /**
     * GET /api/analytics/spending-trends
     * Query: months=1..12
     */
    async getSpendingTrends(months = 6) {
        const url = new URL(
            `${API_BASE_URL}/analytics/spending-trends`,
            window.location.origin
        );
        if (months) {
            url.searchParams.set('months', String(months));
        }

        const response = await fetch(url.toString(), {
            method: 'GET',
            headers: this.getAuthHeaders(),
        });

        return this.handleResponse(response);
    }

    // ======================================================
    // MISC (root, health)
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
