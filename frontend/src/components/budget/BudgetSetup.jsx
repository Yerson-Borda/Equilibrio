import React, { useState, useEffect } from 'react';
import { apiService } from '../../services/api';

const BudgetSetup = ({ onBudgetUpdate }) => {
    const [budget, setBudget] = useState('');
    const [month, setMonth] = useState('');
    const [isLoading, setIsLoading] = useState(false);
    const [currentBudget, setCurrentBudget] = useState(null);

    useEffect(() => {
        // Set default month to current month
        const today = new Date();
        const currentMonth = today.toISOString().slice(0, 7); // YYYY-MM format
        setMonth(currentMonth);
        loadCurrentBudget(currentMonth);
    }, []);

    const loadCurrentBudget = async (month) => {
        try {
            // In a real app, you would fetch the current budget from your backend
            // For now, we'll use localStorage
            const savedBudget = localStorage.getItem(`budget_${month}`);
            if (savedBudget) {
                setCurrentBudget(JSON.parse(savedBudget));
                setBudget(JSON.parse(savedBudget).amount);
            }
        } catch (error) {
            console.error('Error loading budget:', error);
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!budget || !month) return;

        setIsLoading(true);
        try {
            const budgetData = {
                amount: parseFloat(budget),
                month: month,
                currency: 'USD' // You can make this dynamic based on user's default currency
            };

            // Save to localStorage (replace with API call to your backend)
            localStorage.setItem(`budget_${month}`, JSON.stringify(budgetData));
            setCurrentBudget(budgetData);

            // Call analytics to get current spending for progress calculation
            const today = new Date();
            const startDate = new Date(today.getFullYear(), today.getMonth(), 1).toISOString().split('T')[0];
            const endDate = new Date(today.getFullYear(), today.getMonth() + 1, 0).toISOString().split('T')[0];

            const analytics = await apiService.getCategorySummary(startDate, endDate);
            const totalSpent = analytics.total_expenses || 0;

            onBudgetUpdate({
                budget: budgetData.amount,
                spent: totalSpent,
                month: month
            });

            alert('Budget set successfully!');

        } catch (error) {
            console.error('Error setting budget:', error);
            alert('Failed to set budget. Please try again.');
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="bg-white rounded-xl shadow-sm p-6 border border-strokes">
            <h3 className="text-lg font-semibold text-text mb-4">Set Monthly Budget</h3>

            <form onSubmit={handleSubmit} className="space-y-4">
                <div>
                    <label className="block text-sm font-medium text-text mb-2">
                        Month
                    </label>
                    <input
                        type="month"
                        value={month}
                        onChange={(e) => {
                            setMonth(e.target.value);
                            loadCurrentBudget(e.target.value);
                        }}
                        className="w-full p-3 border border-strokes rounded-lg focus:outline-none focus:ring-2 focus:ring-blue focus:border-transparent"
                        required
                    />
                </div>

                <div>
                    <label className="block text-sm font-medium text-text mb-2">
                        Budget Amount ($)
                    </label>
                    <input
                        type="number"
                        value={budget}
                        onChange={(e) => setBudget(e.target.value)}
                        placeholder="Enter your monthly budget"
                        step="0.01"
                        min="0"
                        className="w-full p-3 border border-strokes rounded-lg focus:outline-none focus:ring-2 focus:ring-blue focus:border-transparent"
                        required
                    />
                </div>

                {currentBudget && (
                    <div className="bg-blue-50 p-3 rounded-lg">
                        <p className="text-sm text-blue-800">
                            Current budget for {new Date(month + '-01').toLocaleDateString('en-US', { month: 'long', year: 'numeric' })}:
                            <span className="font-semibold"> ${currentBudget.amount}</span>
                        </p>
                    </div>
                )}

                <button
                    type="submit"
                    disabled={isLoading}
                    className="w-full bg-blue text-white py-3 px-4 rounded-lg font-semibold hover:bg-blue-600 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                    style={{ backgroundColor: '#4361ee' }}
                >
                    {isLoading ? 'Setting Budget...' : 'Set Budget'}
                </button>
            </form>
        </div>
    );
};

export default BudgetSetup;