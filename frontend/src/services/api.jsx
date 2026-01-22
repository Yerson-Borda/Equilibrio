// ==========================================================
// COMPLETE API SERVICE — FULLY MATCHING BACKEND OPENAPI
// ==========================================================

const API_BASE_URL = "/api";

class ApiService {
    constructor() {
        this.token = localStorage.getItem("token");
        this.tagCache = null; // Cache for tags to map names to IDs
        this.tagMap = new Map(); // name -> id mapping
    }

    // ======================================================
    // TOKEN HANDLING
    // ======================================================

    setToken(token) {
        this.token = token;
        if (token) {
            localStorage.setItem("token", token);
        } else {
            localStorage.removeItem("token");
        }
    }

    getAuthHeaders(extraHeaders = {}, withJson = true) {
        const headers = { ...extraHeaders };

        if (withJson) {
            if (!headers["Content-Type"]) headers["Content-Type"] = "application/json";
            if (!headers["Accept"]) headers["Accept"] = "application/json";
        }

        if (this.token) {
            headers["Authorization"] = `Bearer ${this.token}`;
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

        // 204 -> empty body -> returns null (OK)
        return data;
    }

    // ======================================================
    // TAG HELPER METHODS
    // ======================================================

    async refreshTagCache() {
        try {
            this.tagCache = await this.getTags();
            this.tagMap.clear();
            this.tagCache.forEach(tag => {
                this.tagMap.set(tag.name.toLowerCase(), tag.id);
                this.tagMap.set(tag.id.toString(), tag.name); // Reverse mapping
            });
        } catch (error) {
            console.warn("Failed to refresh tag cache:", error);
        }
    }

    async convertTagNamesToIds(tagNames) {
        if (!tagNames || tagNames.length === 0) return [];

        // Ensure cache is populated
        if (!this.tagCache) {
            await this.refreshTagCache();
        }

        const tagIds = [];
        for (const tagName of tagNames) {
            const id = this.tagMap.get(tagName.toLowerCase());
            if (id) {
                tagIds.push(id);
            } else {
                console.warn(`Tag "${tagName}" not found in cache`);
            }
        }
        return tagIds;
    }

    async convertTagIdsToNames(tagIds) {
        if (!tagIds || tagIds.length === 0) return [];

        // Ensure cache is populated
        if (!this.tagCache) {
            await this.refreshTagCache();
        }

        const tagNames = [];
        for (const tagId of tagIds) {
            const name = this.tagMap.get(tagId.toString());
            if (name) {
                tagNames.push(name);
            } else {
                console.warn(`Tag ID "${tagId}" not found in cache`);
            }
        }
        return tagNames;
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
            default_currency: options.defaultCurrency || "USD",
        };

        const response = await fetch(`${API_BASE_URL}/auth/register`, {
            method: "POST",
            headers: this.getAuthHeaders(),
            body: JSON.stringify(body),
        });

        return this.handleResponse(response);
    }

    async login(email, password) {
        const url = new URL(`${API_BASE_URL}/auth/login`, window.location.origin);
        url.searchParams.set("email", email);
        url.searchParams.set("password", password);

        const response = await fetch(url.toString(), {
            method: "POST",
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
            method: "POST",
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
            method: "GET",
            headers: this.getAuthHeaders(),
        });

        return this.handleResponse(response);
    }

    async updateUser(updateData) {
        const response = await fetch(`${API_BASE_URL}/users/me`, {
            method: "PUT",
            headers: this.getAuthHeaders(),
            body: JSON.stringify(updateData),
        });

        return this.handleResponse(response);
    }

    async getDetailedUserInfo() {
        const response = await fetch(`${API_BASE_URL}/users/me/detailed`, {
            method: "GET",
            headers: this.getAuthHeaders(),
        });

        return this.handleResponse(response);
    }

    async uploadAvatar(file) {
        const formData = new FormData();
        formData.append("file", file);

        const response = await fetch(`${API_BASE_URL}/users/me/avatar`, {
            method: "POST",
            headers: this.getAuthHeaders({}, false),
            body: formData,
        });

        return this.handleResponse(response);
    }

    async deleteAvatar() {
        const response = await fetch(`${API_BASE_URL}/users/me/avatar`, {
            method: "DELETE",
            headers: this.getAuthHeaders(),
        });

        return this.handleResponse(response);
    }

    // ======================================================
    // WALLETS
    // ======================================================

    async getUserTotalBalance() {
        const response = await fetch(`${API_BASE_URL}/wallets/user/total`, {
            method: "GET",
            headers: this.getAuthHeaders(),
        });
        return this.handleResponse(response);
    }

    async getWallets() {
        const response = await fetch(`${API_BASE_URL}/wallets/`, {
            method: "GET",
            headers: this.getAuthHeaders(),
        });

        return this.handleResponse(response);
    }

    async getWallet(walletId) {
        const response = await fetch(`${API_BASE_URL}/wallets/${walletId}`, {
            method: "GET",
            headers: this.getAuthHeaders(),
        });

        return this.handleResponse(response);
    }

    async createWallet(walletData) {
        const payload = {
            name: walletData.name,
            currency: walletData.currency || "USD",
            wallet_type: walletData.wallet_type,
            card_number: walletData.card_number || null,
            color: walletData.color || "#3B82F6",
            balance: walletData.balance !== undefined ? walletData.balance : "0.00",
        };

        const response = await fetch(`${API_BASE_URL}/wallets/`, {
            method: "POST",
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
        if (walletData.balance !== undefined) payload.balance = walletData.balance;

        const response = await fetch(`${API_BASE_URL}/wallets/${walletId}`, {
            method: "PUT",
            headers: this.getAuthHeaders(),
            body: JSON.stringify(payload),
        });

        return this.handleResponse(response);
    }

    async deleteWallet(walletId) {
        const response = await fetch(`${API_BASE_URL}/wallets/${walletId}`, {
            method: "DELETE",
            headers: this.getAuthHeaders(),
        });

        return this.handleResponse(response);
    }

    async getWalletBalance(walletId) {
        const response = await fetch(`${API_BASE_URL}/wallets/${walletId}/balance`, {
            method: "GET",
            headers: this.getAuthHeaders(),
        });
        return this.handleResponse(response);
    }

    // ======================================================
    // TRANSACTIONS
    // ======================================================

    // ✅ ADDED: getTransactions method (for backward compatibility)
    async getTransactions(tagId = null) {
        if (tagId) {
            return await this.getTransactionsByTag(tagId);
        }
        return await this.getUserTransactions();
    }

    async getUserTransactions() {
        const response = await fetch(`${API_BASE_URL}/transactions/`, {
            method: "GET",
            headers: this.getAuthHeaders(),
        });

        return this.handleResponse(response);
    }

    async getWalletTransactions(walletId, limit = 10) {
        const url = new URL(
            `${API_BASE_URL}/transactions/wallet/${walletId}`,
            window.location.origin
        );
        if (limit) url.searchParams.set("limit", String(limit));

        const response = await fetch(url.toString(), {
            method: "GET",
            headers: this.getAuthHeaders(),
        });

        return this.handleResponse(response);
    }

    async createTransaction(data) {
        // If data is FormData (with attachments), use it directly
        if (data instanceof FormData) {
            const response = await fetch(`${API_BASE_URL}/transactions/`, {
                method: "POST",
                headers: this.getAuthHeaders({}, false),
                body: data,
            });
            return this.handleResponse(response);
        }

        // Convert object to FormData
        const form = new FormData();

        // Required fields per OpenAPI
        form.append("name", String(data.name));
        form.append("amount", String(data.amount));
        form.append("type", String(data.type));
        form.append("transaction_date", String(data.transaction_date));
        form.append("wallet_id", String(data.wallet_id));
        form.append("category_id", String(data.category_id));

        // Optional fields
        if (data.note) form.append("note", String(data.note));

        // TAGS: Handle different input formats and convert to comma-separated IDs
        if (data.tags !== undefined && data.tags !== null) {
            let tagString = "";

            if (typeof data.tags === "string") {
                // Already a comma-separated string (assume it's IDs)
                tagString = data.tags;
            } else if (Array.isArray(data.tags)) {
                // Array of tag objects, IDs, or names
                const tagIds = [];

                for (const tag of data.tags) {
                    if (typeof tag === "number") {
                        tagIds.push(tag);
                    } else if (typeof tag === "string") {
                        // Check if it's a number string
                        const num = parseInt(tag, 10);
                        if (!Number.isNaN(num)) {
                            tagIds.push(num);
                        } else {
                            // It's a tag name - need to convert to ID
                            // This requires tag cache to be populated
                            console.warn("Tag names need to be converted to IDs first. Use createTransactionWithTags() method instead.");
                        }
                    } else if (tag && typeof tag === "object") {
                        // Tag object with id property
                        if ("id" in tag && tag.id !== undefined) {
                            tagIds.push(Number(tag.id));
                        }
                    }
                }

                if (tagIds.length > 0) {
                    tagString = tagIds.join(",");
                }
            } else if (typeof data.tags === "number") {
                tagString = String(data.tags);
            }

            if (tagString) form.append("tags", tagString);
        }

        // Optional receipt file
        if (data.receipt instanceof File) {
            form.append("receipt", data.receipt);
        }

        const response = await fetch(`${API_BASE_URL}/transactions/`, {
            method: "POST",
            headers: this.getAuthHeaders({}, false),
            body: form,
        });

        return this.handleResponse(response);
    }

    // Enhanced method that handles tag name to ID conversion
    async createTransactionWithTags(transactionData, tagNames = null) {
        const namesToConvert = tagNames || transactionData.tagNames;

        if (namesToConvert && Array.isArray(namesToConvert)) {
            // Convert tag names to IDs
            const tagIds = await this.convertTagNamesToIds(namesToConvert);

            // Create a copy without tagNames
            const { tagNames, ...dataWithoutTagNames } = transactionData;

            // Add tags as comma-separated IDs
            if (tagIds.length > 0) {
                dataWithoutTagNames.tags = tagIds.join(",");
            }

            return await this.createTransaction(dataWithoutTagNames);
        }

        // If no tags need conversion, proceed with regular method
        return await this.createTransaction(transactionData);
    }

    async deleteTransaction(transactionId) {
        const response = await fetch(`${API_BASE_URL}/transactions/${transactionId}`, {
            method: "DELETE",
            headers: this.getAuthHeaders(),
        });

        return this.handleResponse(response);
    }

    async createTransfer(transferData) {
        const payload = {
            source_wallet_id: transferData.source_wallet_id,
            destination_wallet_id: transferData.destination_wallet_id,
            amount: transferData.amount,
            note: transferData.note || null,
        };

        const response = await fetch(`${API_BASE_URL}/transactions/transfer`, {
            method: "POST",
            headers: this.getAuthHeaders(),
            body: JSON.stringify(payload),
        });

        return this.handleResponse(response);
    }

    // ✅ IMPORTANT: Ensure this method exists for transfer preview
    async previewTransfer(source_wallet_id, destination_wallet_id, amount) {
        const url = new URL(
            `${API_BASE_URL}/transactions/transfer/preview`,
            window.location.origin
        );
        url.searchParams.set("source_wallet_id", String(source_wallet_id));
        url.searchParams.set("destination_wallet_id", String(destination_wallet_id));
        url.searchParams.set("amount", String(amount));

        const response = await fetch(url.toString(), {
            method: "GET",
            headers: this.getAuthHeaders(),
        });

        return this.handleResponse(response);
    }

    async getTransactionsByTag(tagId) {
        try {
            const response = await fetch(
                `${API_BASE_URL}/transactions/filter/by-tag/${tagId}`,
                {
                    method: "GET",
                    headers: this.getAuthHeaders(),
                }
            );

            return this.handleResponse(response);
        } catch (error) {
            console.error('API Error (getTransactionsByTag):', error);
            return [];
        }
    }

    // ======================================================
    // CATEGORIES
    // ======================================================

    async getCategories() {
        const response = await fetch(`${API_BASE_URL}/categories/`, {
            method: "GET",
            headers: this.getAuthHeaders(),
        });
        return this.handleResponse(response);
    }

    async getIncomeCategories() {
        const response = await fetch(`${API_BASE_URL}/categories/income`, {
            method: "GET",
            headers: this.getAuthHeaders(),
        });
        return this.handleResponse(response);
    }

    async getExpenseCategories() {
        const response = await fetch(`${API_BASE_URL}/categories/expense`, {
            method: "GET",
            headers: this.getAuthHeaders(),
        });
        return this.handleResponse(response);
    }

    async createCategory(categoryData) {
        const payload = {
            name: categoryData.name,
            type: categoryData.type,
            color: categoryData.color || "#000000",
            icon: categoryData.icon || null,
        };

        const response = await fetch(`${API_BASE_URL}/categories/`, {
            method: "POST",
            headers: this.getAuthHeaders(),
            body: JSON.stringify(payload),
        });
        return this.handleResponse(response);
    }

    async deleteCategory(categoryId) {
        const response = await fetch(`${API_BASE_URL}/categories/${categoryId}`, {
            method: "DELETE",
            headers: this.getAuthHeaders(),
        });
        return this.handleResponse(response);
    }

    // ======================================================
    // BUDGET
    // ======================================================

    async getCurrentBudget() {
        const response = await fetch(`${API_BASE_URL}/budget/current`, {
            method: "GET",
            headers: this.getAuthHeaders(),
        });
        return this.handleResponse(response);
    }

    async updateCurrentBudget(budgetUpdate) {
        const payload = {};

        if (budgetUpdate.monthly_limit !== undefined) {
            payload.monthly_limit = budgetUpdate.monthly_limit;
        }
        if (budgetUpdate.daily_limit !== undefined) {
            payload.daily_limit = budgetUpdate.daily_limit;
        }

        const response = await fetch(`${API_BASE_URL}/budget/current`, {
            method: "PUT",
            headers: this.getAuthHeaders(),
            body: JSON.stringify(payload),
        });
        return this.handleResponse(response);
    }

    // ======================================================
    // CATEGORY LIMITS
    // ======================================================

    async getCategoryLimitsOverview() {
        const response = await fetch(`${API_BASE_URL}/limits/`, {
            method: "GET",
            headers: this.getAuthHeaders(),
        });
        return this.handleResponse(response);
    }

    async setCategoryLimit(categoryId, monthlyLimit) {
        const response = await fetch(`${API_BASE_URL}/limits/${categoryId}`, {
            method: "PUT",
            headers: this.getAuthHeaders(),
            body: JSON.stringify({ monthly_limit: monthlyLimit }),
        });
        return this.handleResponse(response);
    }

    async deleteCategoryLimit(categoryId) {
        const response = await fetch(`${API_BASE_URL}/limits/${categoryId}`, {
            method: "DELETE",
            headers: this.getAuthHeaders(),
        });
        return this.handleResponse(response);
    }

    // ======================================================
    // TAGS
    // ======================================================

    async getTags() {
        try {
            const response = await fetch(`${API_BASE_URL}/tags/`, {
                method: "GET",
                headers: this.getAuthHeaders(),
            });
            return this.handleResponse(response);
        } catch (error) {
            console.error('API Error (getTags):', error);
            return [];
        }
    }

    async createTag(tag) {
        const name = typeof tag === "string" ? tag : tag?.name || "";
        try {
            const response = await fetch(`${API_BASE_URL}/tags/`, {
                method: "POST",
                headers: this.getAuthHeaders(),
                body: JSON.stringify({ name }),
            });
            return this.handleResponse(response);
        } catch (error) {
            console.error('API Error (createTag):', error);
            throw error;
        }
    }

    async deleteTag(tagId) {
        const response = await fetch(`${API_BASE_URL}/tags/${tagId}`, {
            method: "DELETE",
            headers: this.getAuthHeaders(),
        });
        return this.handleResponse(response);
    }

    // Helper to create or get tag ID
    async getOrCreateTag(tagName) {
        // First, ensure cache is populated
        if (!this.tagCache) {
            await this.refreshTagCache();
        }

        // Check if tag exists
        const existingId = this.tagMap.get(tagName.toLowerCase());
        if (existingId) {
            return existingId;
        }

        // Create new tag
        try {
            const newTag = await this.createTag(tagName);
            // Update cache
            this.tagCache.push(newTag);
            this.tagMap.set(newTag.name.toLowerCase(), newTag.id);
            this.tagMap.set(newTag.id.toString(), newTag.name);

            return newTag.id;
        } catch (error) {
            console.error("Failed to create tag:", error);
            throw error;
        }
    }

    // ======================================================
    // WALLET FILTERING BY TAG
    // ======================================================

    async getWalletsByTag(tagId) {
        try {
            // First get all wallets with their transactions
            const wallets = await this.getWallets();

            // If no tag selected, return all wallets
            if (!tagId) return wallets;

            // Filter wallets that have transactions with the given tag
            const filteredWallets = wallets.filter(wallet => {
                // Check if wallet has transactions property
                if (!wallet.transactions) return false;

                // Check if any transaction in this wallet has the selected tag
                return wallet.transactions.some(transaction =>
                    transaction.tags?.some(tag =>
                        tag.id === tagId ||
                        String(tag.id) === String(tagId)
                    )
                );
            });

            return filteredWallets;
        } catch (error) {
            console.error('API Error (getWalletsByTag):', error);
            return [];
        }
    }

    // ======================================================
    // FINANCIAL SUMMARY
    // ======================================================

    async getCurrentSummary() {
        const response = await fetch(`${API_BASE_URL}/financial_summary/current`, {
            method: "GET",
            headers: this.getAuthHeaders(),
        });
        return this.handleResponse(response);
    }

    // ======================================================
    // ANALYTICS - FIXED ENDPOINTS
    // ======================================================

    async getCategorySummary(startDate, endDate) {
        try {
            const url = new URL(`${API_BASE_URL}/analytics/category-summary`, window.location.origin);
            url.searchParams.set("start_date", startDate);
            url.searchParams.set("end_date", endDate);

            const response = await fetch(url.toString(), {
                method: "GET",
                headers: this.getAuthHeaders(),
            });

            return this.handleResponse(response);
        } catch (error) {
            console.error("Error fetching category summary:", error);
            return null;
        }
    }

    async getMonthlyComparison(month) {
        try {
            const url = new URL(`${API_BASE_URL}/analytics/monthly-comparison`, window.location.origin);
            url.searchParams.set("month", month);

            const response = await fetch(url.toString(), {
                method: "GET",
                headers: this.getAuthHeaders(),
            });

            return this.handleResponse(response);
        } catch (error) {
            console.error("Error fetching monthly comparison:", error);
            return [];
        }
    }

    async getSpendingTrends(months = 6) {
        try {
            const url = new URL(`${API_BASE_URL}/analytics/spending-trends`, window.location.origin);
            if (months) url.searchParams.set("months", String(months));

            const response = await fetch(url.toString(), {
                method: "GET",
                headers: this.getAuthHeaders(),
            });

            return this.handleResponse(response);
        } catch (error) {
            console.error("Error fetching spending trends:", error);
            return null;
        }
    }

    async getTopCategoriesCurrentMonth() {
        try {
            const response = await fetch(`${API_BASE_URL}/analytics/top-categories/current-month`, {
                method: "GET",
                headers: this.getAuthHeaders(),
            });
            return this.handleResponse(response);
        } catch (error) {
            console.error("Error fetching top categories:", error);
            return [];
        }
    }

    // FIXED: getAverageSpending method that actually works

    async getAverageSpending(period = "month") {
        try {
            const url = new URL(`${API_BASE_URL}/analytics/average-spending`, window.location.origin);
            url.searchParams.set("period", period);

            const response = await fetch(url.toString(), {
                method: "GET",
                headers: this.getAuthHeaders(),
            });

            const data = await this.handleResponse(response);

            // Debug: Log what we get from backend
            console.log(`Average spending API response for period ${period}:`, data);

            // The backend returns different fields based on period type
            if (data && Array.isArray(data)) {
                return data.map(item => {
                    // Handle different period types and their corresponding fields
                    let amountToUse;

                    switch(period) {
                        case "day":
                            // For day period, use average_daily_spending
                            amountToUse = item.average_daily_spending !== undefined
                                ? item.average_daily_spending
                                : (item.average_amount || item.total_period_spent || 0);
                            break;
                        case "month":
                            // For month period, use average_monthly_spending
                            amountToUse = item.average_monthly_spending !== undefined
                                ? item.average_monthly_spending
                                : (item.average_amount || item.total_period_spent || 0);
                            break;
                        case "year":
                            // For year period, use total_period_spent
                            amountToUse = item.total_period_spent || item.average_amount || 0;
                            break;
                        default:
                            amountToUse = item.total_period_spent || item.average_amount || 0;
                    }

                    return {
                        category_id: item.category_id || item.id,
                        category_name: item.category_name || item.category || item.name || "Unknown",
                        // Store all possible amounts for debugging
                        average_amount: amountToUse,
                        total_period_spent: item.total_period_spent || 0,
                        average_daily_spending: item.average_daily_spending,
                        average_monthly_spending: item.average_monthly_spending,
                        period_type: item.period_type || period,
                        percentage: item.percentage || 0,
                        category_icon: item.category_icon || item.icon,
                        category_color: item.category_color || item.color,
                        transaction_count: item.transactions || item.transaction_count || item.count || 0,
                        // Original data for reference
                        original_data: item
                    };
                });
            }

            console.log("No valid average spending data found, trying category summary...");
            return await this.getCategorySummaryForAverage(period);

        } catch (error) {
            console.error("Error fetching average spending:", error);
            // Fallback to calculating from category-summary
            return await this.getCategorySummaryForAverage(period);
        }
    }

// Update the calculateAverageSpending method to handle different periods:
    async calculateAverageSpending(period = "month") {
        try {
            const transactions = await this.getUserTransactions();
            const categories = await this.getCategories();

            if (!transactions || !Array.isArray(transactions)) {
                return [];
            }

            // Filter only expense transactions
            const expenseTransactions = transactions.filter(t => t.type === 'expense');

            if (expenseTransactions.length === 0) {
                return [];
            }

            // Group by category
            const categoryMap = {};
            const categoryInfo = {};

            // Build category info map
            categories.forEach(cat => {
                if (cat && cat.id) {
                    categoryInfo[cat.id] = {
                        name: cat.name || `Category ${cat.id}`,
                        icon: cat.icon,
                        color: cat.color || '#000000'
                    };
                }
            });

            // Calculate totals per category
            expenseTransactions.forEach(t => {
                const categoryId = t.category_id;
                if (!categoryId) return;

                const amount = Math.abs(Number(t.amount) || 0);

                if (!categoryMap[categoryId]) {
                    categoryMap[categoryId] = {
                        total: 0,
                        count: 0
                    };
                }

                categoryMap[categoryId].total += amount;
                categoryMap[categoryId].count++;
            });

            // Calculate total expenses for percentages
            const totalExpenses = Object.values(categoryMap).reduce((sum, cat) => sum + cat.total, 0);

            // Convert to array format based on period
            const result = [];
            Object.keys(categoryMap).forEach(catId => {
                const catData = categoryMap[catId];
                const catInfo = categoryInfo[catId] || {
                    name: `Category ${catId}`,
                    icon: null,
                    color: '#000000'
                };

                const totalSpent = catData.total;
                const percentage = totalExpenses > 0 ? (catData.total / totalExpenses * 100) : 0;

                // Calculate amounts based on period
                let averageAmount;
                let averageDaily;
                let averageMonthly;

                switch(period) {
                    case "day":
                        averageDaily = totalSpent / 30; // Average per day (assuming 30 days in month)
                        averageMonthly = totalSpent; // Monthly total
                        averageAmount = averageDaily;
                        break;
                    case "month":
                        averageMonthly = totalSpent; // Monthly total
                        averageAmount = averageMonthly;
                        break;
                    case "year":
                        averageAmount = totalSpent; // Yearly total
                        break;
                    default:
                        averageAmount = totalSpent;
                }

                result.push({
                    category_id: parseInt(catId),
                    category_name: catInfo.name,
                    average_amount: averageAmount,
                    total_period_spent: totalSpent,
                    average_daily_spending: averageDaily,
                    average_monthly_spending: averageMonthly,
                    period_type: period,
                    percentage: percentage.toFixed(1),
                    transaction_count: catData.count,
                    category_icon: catInfo.icon,
                    category_color: catInfo.color
                });
            });

            // Sort by average amount descending
            return result.sort((a, b) => b.average_amount - a.average_amount);

        } catch (error) {
            console.error("Error calculating average spending:", error);
            return [];
        }
    }

    // ======================================================
    // GOALS
    // ======================================================

    async getGoals() {
        const response = await fetch(`${API_BASE_URL}/goals`, {
            method: "GET",
            headers: this.getAuthHeaders(),
        });

        return this.handleResponse(response);
    }

    async getGoal(goalId) {
        const response = await fetch(`${API_BASE_URL}/goals/${goalId}`, {
            method: "GET",
            headers: this.getAuthHeaders(),
        });

        return this.handleResponse(response);
    }

    async createGoal(goalData) {
        // If data is FormData (with image), use it directly
        if (goalData instanceof FormData) {
            const response = await fetch(`${API_BASE_URL}/goals`, {
                method: "POST",
                headers: this.getAuthHeaders({}, false),
                body: goalData,
            });
            return this.handleResponse(response);
        }

        // Convert object to FormData for multipart upload
        const form = new FormData();

        // Required fields
        form.append("title", String(goalData.title));
        form.append("goal_amount", String(goalData.goal_amount));

        // Optional fields
        if (goalData.description) form.append("description", String(goalData.description));
        if (goalData.deadline) form.append("deadline", String(goalData.deadline));
        if (goalData.currency) form.append("currency", String(goalData.currency));
        if (goalData.image instanceof File) {
            form.append("image", goalData.image);
        }

        const response = await fetch(`${API_BASE_URL}/goals`, {
            method: "POST",
            headers: this.getAuthHeaders({}, false),
            body: form,
        });

        return this.handleResponse(response);
    }

    async updateGoal(goalId, goalData) {
        const payload = {};

        if (goalData.title !== undefined) payload.title = goalData.title;
        if (goalData.description !== undefined) payload.description = goalData.description;
        if (goalData.image !== undefined) payload.image = goalData.image;
        if (goalData.deadline !== undefined) payload.deadline = goalData.deadline;
        if (goalData.goal_amount !== undefined) payload.goal_amount = goalData.goal_amount;

        const response = await fetch(`${API_BASE_URL}/goals/${goalId}`, {
            method: "PUT",
            headers: this.getAuthHeaders(),
            body: JSON.stringify(payload),
        });

        return this.handleResponse(response);
    }

    async deleteGoal(goalId) {
        const response = await fetch(`${API_BASE_URL}/goals/${goalId}`, {
            method: "DELETE",
            headers: this.getAuthHeaders(),
        });

        return this.handleResponse(response);
    }

    // ======================================================
    // MISC
    // ======================================================

    async healthCheck() {
        const response = await fetch(`/health`, { method: "GET" });
        return this.handleResponse(response);
    }

    async root() {
        const response = await fetch(`/`, { method: "GET" });
        return this.handleResponse(response);
    }
}

export const apiService = new ApiService();