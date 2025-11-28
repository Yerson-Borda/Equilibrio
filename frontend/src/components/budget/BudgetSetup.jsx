import React, { useState, useEffect } from 'react';
import { apiService } from '../../services/api';

const BudgetSetup = ({ onBudgetUpdate }) => {
    const [monthlyLimit, setMonthlyLimit] = useState('');
    const [dailyLimit, setDailyLimit] = useState('');
    const [currentBudget, setCurrentBudget] = useState(null);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState(null);

    const formatMonthLabel = (year, month) => {
        if (!year || !month) return '';
        const date = new Date(year, month - 1, 1);
        return date.toLocaleDateString('en-US', { month: 'long', year: 'numeric' });
    };

    const toMonthString = (year, month) => {
        if (!year || !month) return new Date().toISOString().slice(0, 7);
        return `${year}-${String(month).padStart(2, '0')}`;
    };

    const parseNumber = (value) => {
        if (value === null || value === undefined) return 0;
        const n = typeof value === 'string' ? parseFloat(value) : Number(value);
        return Number.isNaN(n) ? 0 : n;
    };

    const loadCurrentBudget = async () => {
        try {
            setIsLoading(true);
            setError(null);

            const data = await apiService.getCurrentBudget();

            if (data) {
                setCurrentBudget(data);
                setMonthlyLimit(data.monthly_limit ?? '');
                setDailyLimit(data.daily_limit ?? '');

                const monthStr = toMonthString(data.year, data.month);
                const monthlyBudget = parseNumber(data.monthly_limit);
                const monthlySpent = parseNumber(data.monthly_spent);

                if (typeof onBudgetUpdate === 'function') {
                    onBudgetUpdate({
                        budget: monthlyBudget,
                        spent: monthlySpent,
                        month: monthStr,
                    });
                }
            } else {
                // If backend returns null/empty, just reset to 0
                const today = new Date();
                const monthStr = today.toISOString().slice(0, 7);
                setCurrentBudget(null);
                setMonthlyLimit('');
                setDailyLimit('');

                if (typeof onBudgetUpdate === 'function') {
                    onBudgetUpdate({
                        budget: 0,
                        spent: 0,
                        month: monthStr,
                    });
                }
            }
        } catch (err) {
            console.error('Error loading current budget:', err);
            setError('Failed to load current budget');
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        loadCurrentBudget();// eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    const handleSubmit = async (e) => {
        e.preventDefault();

        const monthlyValue = parseNumber(monthlyLimit);
        const dailyValue = dailyLimit ? parseNumber(dailyLimit) : undefined;

        if (monthlyValue <= 0) {
            alert('Please enter a valid monthly budget.');
            return;
        }

        try {
            setIsLoading(true);
            setError(null);

            const updatePayload = {
                monthly_limit: monthlyValue,
            };

            if (dailyValue !== undefined) {
                updatePayload.daily_limit = dailyValue;
            }

            await apiService.updateCurrentBudget(updatePayload);

            // Reload to get updated spent, month/year from backend
            await loadCurrentBudget();

            alert('Budget updated successfully!');
        } catch (err) {
            console.error('Error updating budget:', err);
            setError('Failed to update budget');
            alert(`Failed to update budget: ${err.message || 'Please try again.'}`);
        } finally {
            setIsLoading(false);
        }
    };

    const monthLabel = currentBudget
        ? formatMonthLabel(currentBudget.year, currentBudget.month)
        : 'Current Month';

    return (
        <div className="bg-white rounded-xl shadow-sm border border-strokes p-6">
            <div className="flex justify-between items-center mb-4">
                <div>
                    <h2 className="text-lg font-semibold text-text">Budget Setup</h2>
                    <p className="text-sm text-metallic-gray">
                        Set your monthly and optional daily budget limits.
                    </p>
                </div>
                <span className="text-xs font-medium text-metallic-gray bg-gray-100 px-3 py-1 rounded-full">
                    {monthLabel}
                </span>
            </div>

            {error && (
                <div className="mb-3 text-sm text-red-600 bg-red-50 border border-red-200 rounded-lg px-3 py-2">
                    {error}
                </div>
            )}

            <form onSubmit={handleSubmit} className="space-y-4">
                <div>
                    <label className="block text-sm font-medium text-text mb-2">
                        Monthly Budget
                    </label>
                    <div className="relative">
                        <span className="absolute left-3 top-1/2 -translate-y-1/2 text-metallic-gray">
                            $
                        </span>
                        <input
                            type="number"
                            min="0"
                            step="0.01"
                            value={monthlyLimit}
                            onChange={(e) => setMonthlyLimit(e.target.value)}
                            className="w-full pl-8 p-3 border border-strokes rounded-lg focus:outline-none focus:ring-2 focus:ring-blue focus:border-transparent"
                            placeholder="Enter your monthly limit"
                            required
                        />
                    </div>
                </div>

                <div>
                    <label className="block text-sm font-medium text-text mb-2">
                        Daily Limit (optional)
                    </label>
                    <div className="relative">
                        <span className="absolute left-3 top-1/2 -translate-y-1/2 text-metallic-gray">
                            $
                        </span>
                        <input
                            type="number"
                            min="0"
                            step="0.01"
                            value={dailyLimit}
                            onChange={(e) => setDailyLimit(e.target.value)}
                            className="w-full pl-8 p-3 border border-strokes rounded-lg focus:outline-none focus:ring-2 focus:ring-blue focus:border-transparent"
                            placeholder="Enter your daily limit"
                        />
                    </div>
                    <p className="mt-1 text-xs text-metallic-gray">
                        If not set, daily suggestions will be based on your monthly budget only.
                    </p>
                </div>

                <button
                    type="submit"
                    disabled={isLoading}
                    className="w-full bg-blue text-white py-3 rounded-lg font-semibold hover:bg-[#3450d3] transition disabled:opacity-60 disabled:cursor-not-allowed"
                >
                    {isLoading ? 'Saving...' : 'Save Budget'}
                </button>
            </form>

            {currentBudget && (
                <div className="mt-4 text-xs text-metallic-gray">
                    <p>
                        Monthly spent (from backend):{' '}
                        <span className="font-semibold text-text">
                            ${parseNumber(currentBudget.monthly_spent).toFixed(2)}
                        </span>
                    </p>
                    <p>
                        Daily spent (from backend):{' '}
                        <span className="font-semibold text-text">
                            ${parseNumber(currentBudget.daily_spent).toFixed(2)}
                        </span>
                    </p>
                </div>
            )}
        </div>
    );
};

export default BudgetSetup;
