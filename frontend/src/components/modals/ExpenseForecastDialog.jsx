import React, { useEffect, useState } from 'react';
import { X, TrendingUp, TrendingDown, AlertCircle, CheckCircle, Info } from 'lucide-react';

const ExpenseForecastDialog = ({
                                   isOpen,
                                   onClose,
                                   selectedCategory,
                                   totalExpenses,
                                   allCategories = []
                               }) => {
    const [forecastData, setForecastData] = useState(null);
    const [budgetStatus, setBudgetStatus] = useState('within'); // 'within', 'exceeding', 'under'

    useEffect(() => {
        if (isOpen) {
            // Generate forecast data
            const mockData = generateForecastData(selectedCategory, totalExpenses, allCategories);
            setForecastData(mockData);
            setBudgetStatus(mockData.isWithinBudget ? 'within' : 'exceeding');
        }
    }, [isOpen, selectedCategory, totalExpenses, allCategories]);

    const generateForecastData = (category, total, categories) => {
        const currentDate = new Date();
        const daysInMonth = new Date(currentDate.getFullYear(), currentDate.getMonth() + 1, 0).getDate();
        const daysElapsed = currentDate.getDate();
        const dailyAverage = total / daysElapsed;
        const projectedTotal = dailyAverage * daysInMonth;

        const monthlyBudget = 2450; // Default budget
        const overBudget = projectedTotal - monthlyBudget;
        const isWithinBudget = overBudget <= 50; // Allow small buffer

        let messages = [];
        let suggestions = [];

        if (category) {
            // Category-specific forecast
            const trend = category.trend || 0;
            const isPositiveTrend = trend > 0;

            messages.push(
                isPositiveTrend
                    ? `Spending on ${category.name.toLowerCase()} is above your monthly average.`
                    : `Your ${category.name.toLowerCase()} spending is below your monthly average.`
            );

            if (isPositiveTrend) {
                suggestions.push(
                    `Consider reducing ${category.name.toLowerCase()} expenses by ${Math.abs(trend).toFixed(1)}% to stay on track.`
                );
            } else {
                suggestions.push(
                    `Great job! Your ${category.name.toLowerCase()} spending is lower than usual.`
                );
            }
        } else {
            // Overall forecast
            messages.push(
                `At your current pace, you're on track to spend around $${projectedTotal.toFixed(0)} by the end of the month.`
            );

            if (isWithinBudget) {
                messages.push("You're projected to finish the month within budget if spending continues at this rate.");
                suggestions.push("Keep monitoring your spending patterns to maintain this positive trend.");
            } else {
                messages.push(`Caution: If current trends continue, you may exceed your monthly budget by approximately $${Math.abs(overBudget).toFixed(0)}.`);
                suggestions.push("Review your top spending categories for potential savings.");
            }
        }

        // Add category-specific insights
        if (categories.length > 0) {
            const highestSpending = categories.reduce((max, cat) =>
                cat.amount > max.amount ? cat : max
            );
            const increasingCategory = categories.find(cat => cat.trend > 0);

            if (highestSpending && highestSpending !== category) {
                suggestions.push(
                    `${highestSpending.name} is your largest expense category. Consider optimizing here for maximum impact.`
                );
            }

            if (increasingCategory && increasingCategory !== category) {
                suggestions.push(
                    `${increasingCategory.name} expenses are ${increasingCategory.trend.toFixed(1)}% higher than last month.`
                );
            }
        }

        return {
            projectedTotal: projectedTotal.toFixed(0),
            isWithinBudget,
            overBudgetAmount: Math.abs(overBudget).toFixed(0),
            dailyAverage: dailyAverage.toFixed(2),
            messages,
            suggestions,
            monthlyBudget,
            daysElapsed,
            daysRemaining: daysInMonth - daysElapsed
        };
    };

    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
            <div className="bg-white rounded-2xl w-full max-w-lg shadow-xl max-h-[90vh] overflow-y-auto">
                {/* Header */}
                <div className="sticky top-0 bg-white p-6 border-b border-gray-200 rounded-t-2xl">
                    <div className="flex items-center justify-between">
                        <div>
                            <h3 className="text-lg font-semibold text-gray-900">Spending Forecast</h3>
                            <p className="text-sm text-gray-500 mt-1">
                                {selectedCategory ? `${selectedCategory.name} Analysis` : 'Monthly Spending Analysis'}
                            </p>
                        </div>
                        <button
                            onClick={onClose}
                            className="p-2 hover:bg-gray-100 rounded-lg transition-colors"
                            aria-label="Close"
                        >
                            <X className="w-5 h-5 text-gray-500" />
                        </button>
                    </div>
                </div>

                {/* Content */}
                <div className="p-6">
                    {/* Budget Status */}
                    <div className={`p-4 rounded-xl mb-6 ${
                        budgetStatus === 'within' ? 'bg-green-50 border border-green-100' :
                            budgetStatus === 'exceeding' ? 'bg-yellow-50 border border-yellow-100' :
                                'bg-blue-50 border border-blue-100'
                    }`}>
                        <div className="flex items-start">
                            <div className={`p-2 rounded-lg mr-3 ${
                                budgetStatus === 'within' ? 'bg-green-100' :
                                    budgetStatus === 'exceeding' ? 'bg-yellow-100' :
                                        'bg-blue-100'
                            }`}>
                                {budgetStatus === 'within' ? (
                                    <CheckCircle className="w-5 h-5 text-green-600" />
                                ) : budgetStatus === 'exceeding' ? (
                                    <AlertCircle className="w-5 h-5 text-yellow-600" />
                                ) : (
                                    <Info className="w-5 h-5 text-blue-600" />
                                )}
                            </div>
                            <div>
                                <h4 className="font-semibold text-gray-900 mb-1">
                                    {budgetStatus === 'within' ? 'On Track' :
                                        budgetStatus === 'exceeding' ? 'Attention Needed' :
                                            'Budget Analysis'}
                                </h4>
                                {forecastData?.messages.map((msg, index) => (
                                    <p key={index} className="text-sm text-gray-700 mb-1">
                                        {msg}
                                    </p>
                                ))}
                            </div>
                        </div>
                    </div>

                    {/* Stats Grid */}
                    <div className="grid grid-cols-2 gap-4 mb-6">
                        <div className="bg-gray-50 p-4 rounded-lg">
                            <p className="text-xs text-gray-500 mb-1">Projected Month-End</p>
                            <p className="text-2xl font-bold text-gray-900">
                                ${forecastData?.projectedTotal}
                            </p>
                        </div>
                        <div className="bg-gray-50 p-4 rounded-lg">
                            <p className="text-xs text-gray-500 mb-1">Daily Average</p>
                            <p className="text-2xl font-bold text-gray-900">
                                ${forecastData?.dailyAverage}
                            </p>
                        </div>
                        <div className="bg-gray-50 p-4 rounded-lg">
                            <p className="text-xs text-gray-500 mb-1">Days Remaining</p>
                            <p className="text-2xl font-bold text-gray-900">
                                {forecastData?.daysRemaining}
                            </p>
                        </div>
                        <div className="bg-gray-50 p-4 rounded-lg">
                            <p className="text-xs text-gray-500 mb-1">Monthly Budget</p>
                            <p className="text-2xl font-bold text-gray-900">
                                ${forecastData?.monthlyBudget}
                            </p>
                        </div>
                    </div>

                    {/* Suggestions */}
                    <div className="space-y-4">
                        <h4 className="font-semibold text-gray-900">Suggestions</h4>
                        {forecastData?.suggestions.map((suggestion, index) => (
                            <div key={index} className="flex items-start space-x-3 p-3 bg-blue-50 rounded-lg">
                                <Info className="w-5 h-5 text-blue-600 mt-0.5 shrink-0" />
                                <p className="text-sm text-gray-700">{suggestion}</p>
                            </div>
                        ))}
                    </div>

                    {/* Category Comparison (if applicable) */}
                    {allCategories.length > 0 && !selectedCategory && (
                        <div className="mt-6 pt-6 border-t border-gray-200">
                            <h4 className="font-semibold text-gray-900 mb-3">Top Categories Trend</h4>
                            <div className="space-y-2">
                                {allCategories.map(category => {
                                    const isPositive = category.trend >= 0;
                                    return (
                                        <div key={category.id} className="flex items-center justify-between p-2 hover:bg-gray-50 rounded">
                                            <div className="flex items-center space-x-2">
                                                <div
                                                    className="w-2 h-2 rounded-full"
                                                    style={{ backgroundColor: category.color }}
                                                />
                                                <span className="text-sm text-gray-700">{category.name}</span>
                                            </div>
                                            <div className={`flex items-center text-sm ${isPositive ? 'text-red-500' : 'text-green-600'}`}>
                                                {isPositive ? (
                                                    <TrendingUp className="w-4 h-4 mr-1" />
                                                ) : (
                                                    <TrendingDown className="w-4 h-4 mr-1" />
                                                )}
                                                <span>{Math.abs(category.trend).toFixed(1)}%</span>
                                            </div>
                                        </div>
                                    );
                                })}
                            </div>
                        </div>
                    )}
                </div>

                {/* Footer */}
                <div className="sticky bottom-0 bg-white p-6 border-t border-gray-200 rounded-b-2xl">
                    <div className="flex space-x-3">
                        <button
                            onClick={onClose}
                            className="flex-1 py-3 px-4 border border-gray-300 text-gray-700 font-medium rounded-lg hover:bg-gray-50 transition-colors"
                        >
                            Dismiss
                        </button>
                        <button
                            onClick={onClose}
                            className="flex-1 py-3 px-4 bg-blue-600 text-white font-medium rounded-lg hover:bg-blue-700 transition-colors"
                        >
                            Apply Suggestions
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default ExpenseForecastDialog;