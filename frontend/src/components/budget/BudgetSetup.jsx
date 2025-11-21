import React, { useState, useEffect } from 'react';
import { apiService } from '../../services/api';
import Input from '../ui/Input';
import Button from '../ui/Button';

const BudgetSetup = ({ onBudgetUpdate }) => {
    const [budget, setBudget] = useState('');
    const [month, setMonth] = useState('');
    const [isLoading, setIsLoading] = useState(false);
    const [currentBudget, setCurrentBudget] = useState(null);

    useEffect(() => {
        const today = new Date();
        const currentMonth = today.toISOString().slice(0, 7); // YYYY-MM
        setMonth(currentMonth);
        loadCurrentBudget(currentMonth);
    }, []);

    const loadCurrentBudget = async (monthValue) => {
        try {
            const saved = localStorage.getItem(`budget_${monthValue}`);
            if (saved) {
                const parsed = JSON.parse(saved);
                setCurrentBudget(parsed);
                setBudget(String(parsed.amount || ''));
            } else {
                setCurrentBudget(null);
                setBudget('');
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
                month,
                currency: 'USD', // could be dynamic based on user
            };

            // Save locally (placeholder for real API)
            localStorage.setItem(`budget_${month}`, JSON.stringify(budgetData));
            setCurrentBudget(budgetData);

            // Calculate spending using analytics endpoint
            const today = new Date();
            const startDate = new Date(
                today.getFullYear(),
                today.getMonth(),
                1
            )
                .toISOString()
                .split('T')[0];
            const endDate = today.toISOString().split('T')[0];

            const analytics = await apiService.getCategorySummary(
                startDate,
                endDate
            );
            const totalSpent = analytics.total_expenses || 0;

            if (onBudgetUpdate) {
                onBudgetUpdate({
                    budget: budgetData.amount,
                    spent: totalSpent,
                    month,
                });
            }

            alert('Budget set successfully!');
        } catch (error) {
            console.error('Error setting budget:', error);
            alert('Failed to set budget. Please try again.');
        } finally {
            setIsLoading(false);
        }
    };

    const formattedMonth =
        month &&
        new Date(`${month}-01`).toLocaleDateString('en-US', {
            month: 'long',
            year: 'numeric',
        });

    return (
        <div className="space-y-4">
            {currentBudget && (
                <div className="bg-soft-gray rounded-lg px-4 py-3 text-sm text-text flex items-center justify-between">
                    <div>
                        <p className="font-medium">
                            Current budget for {formattedMonth}:
                        </p>
                        <p className="text-metallic-gray">
                            ${currentBudget.amount.toFixed(2)} in USD
                        </p>
                    </div>
                </div>
            )}

            <form onSubmit={handleSubmit} className="space-y-4">
                <Input
                    label="Month"
                    type="month"
                    value={month}
                    onChange={(e) => {
                        const val = e.target.value;
                        setMonth(val);
                        loadCurrentBudget(val);
                    }}
                    required
                />

                <Input
                    label="Budget Amount ($)"
                    type="number"
                    value={budget}
                    onChange={(e) => setBudget(e.target.value)}
                    placeholder="Enter your monthly budget"
                    min="0"
                    step="0.01"
                    required
                />

                <Button
                    type="submit"
                    variant="primary"
                    className="w-full py-3"
                    disabled={isLoading}
                >
                    {isLoading ? 'Setting Budget...' : 'Set Budget'}
                </Button>
            </form>
        </div>
    );
};

export default BudgetSetup;
