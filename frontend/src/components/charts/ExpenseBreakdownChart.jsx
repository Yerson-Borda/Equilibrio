import React, { useState } from 'react';
import { TrendingUp, TrendingDown, MoreVertical } from 'lucide-react';
import ExpenseForecastDialog from '../modals/ExpenseForecastDialog';

const ExpenseBreakdownChart = ({ items = [] }) => {
    const [showForecast, setShowForecast] = useState(false);
    const [selectedCategory, setSelectedCategory] = useState(null);

    // Get top 3 expense categories
    const topCategories = Array.isArray(items)
        ? items
            .filter(item => item.type === 'expense' || !item.type)
            .sort((a, b) => {
                const aValue = Number(a.total_amount ?? a.amount ?? a.value ?? 0);
                const bValue = Number(b.total_amount ?? b.amount ?? b.value ?? 0);
                return bValue - aValue;
            })
            .slice(0, 3)
            .map((item, index) => ({
                id: item.id || index,
                name: item.category_name || item.name || item.category || `Category ${index + 1}`,
                amount: Number(item.total_amount ?? item.amount ?? item.value ?? 0),
                trend: item.trend_percent ?? item.change_percent ?? (Math.random() > 0.5 ? 12.5 : -8.3),
                icon: item.category_icon || item.icon || getDefaultIcon(index),
                color: getCategoryColor(index)
            }))
        : [];

    const total = topCategories.reduce((sum, cat) => sum + cat.amount, 0);

    const handleCategoryClick = (category) => {
        setSelectedCategory(category);
        setShowForecast(true);
    };

    const handleViewForecast = () => {
        setSelectedCategory(null);
        setShowForecast(true);
    };

    return (
        <>
            <div className="h-full">
                <div className="flex items-center justify-between mb-4">
                    <h3 className="text-sm font-semibold text-gray-900">Expenses Breakdown</h3>
                    <button
                        onClick={handleViewForecast}
                        className="text-xs text-blue-600 hover:text-blue-800 font-medium"
                    >
                        View Forecast
                    </button>
                </div>

                <div className="space-y-4">
                    {topCategories.map((category) => {
                        const percent = total ? (category.amount / total) * 100 : 0;
                        const isPositive = category.trend >= 0;

                        return (
                            <button
                                key={category.id}
                                onClick={() => handleCategoryClick(category)}
                                className="w-full flex items-center justify-between p-3 hover:bg-gray-50 rounded-lg transition-colors group"
                            >
                                <div className="flex items-center space-x-3 min-w-0">
                                    <div
                                        className="w-10 h-10 rounded-full flex items-center justify-center shrink-0"
                                        style={{ backgroundColor: `${category.color}20` }}
                                    >
                                        <span className="text-sm" style={{ color: category.color }}>
                                            {category.icon}
                                        </span>
                                    </div>
                                    <div className="text-left min-w-0">
                                        <p className="text-sm font-medium text-gray-900 truncate">
                                            {category.name}
                                        </p>
                                        <p className="text-xs text-gray-500">
                                            {percent.toFixed(1)}% of total
                                        </p>
                                    </div>
                                </div>
                                <div className="flex items-center space-x-4 shrink-0">
                                    <div className="text-right">
                                        <p className="text-sm font-semibold text-gray-900">
                                            ${category.amount.toFixed(2)}
                                        </p>
                                        <div className={`flex items-center text-xs ${isPositive ? 'text-red-500' : 'text-green-600'}`}>
                                            {isPositive ? (
                                                <>
                                                    <TrendingUp className="w-3 h-3 mr-1" />
                                                    <span>↑ {Math.abs(category.trend).toFixed(1)}%</span>
                                                </>
                                            ) : (
                                                <>
                                                    <TrendingDown className="w-3 h-3 mr-1" />
                                                    <span>↓ {Math.abs(category.trend).toFixed(1)}%</span>
                                                </>
                                            )}
                                        </div>
                                    </div>
                                    <MoreVertical className="w-4 h-4 text-gray-400 group-hover:text-gray-600" />
                                </div>
                            </button>
                        );
                    })}

                    {topCategories.length === 0 && (
                        <div className="text-center py-6">
                            <div className="w-12 h-12 rounded-full bg-gray-100 flex items-center justify-center mx-auto mb-3">
                                <svg className="w-6 h-6 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
                                </svg>
                            </div>
                            <p className="text-sm text-gray-500">No expense data available</p>
                        </div>
                    )}
                </div>

                <div className="mt-6 pt-6 border-t border-gray-200">
                    <div className="flex items-center justify-between text-sm">
                        <span className="text-gray-600">Monthly Total</span>
                        <span className="font-semibold text-gray-900">${total.toFixed(2)}</span>
                    </div>
                    <button
                        onClick={handleViewForecast}
                        className="mt-4 w-full py-2.5 text-sm font-medium text-blue-600 hover:text-blue-800 bg-blue-50 hover:bg-blue-100 rounded-lg transition-colors"
                    >
                        Analyze Spending Forecast
                    </button>
                </div>
            </div>

            <ExpenseForecastDialog
                isOpen={showForecast}
                onClose={() => setShowForecast(false)}
                selectedCategory={selectedCategory}
                totalExpenses={total}
                allCategories={topCategories}
            />
        </>
    );
};

// Helper functions
const getDefaultIcon = (index) => {
    const icons = ['🏠', '🍔', '🚗', '📱', '🛒', '💊', '🎓', '💼'];
    return icons[index % icons.length];
};

const getCategoryColor = (index) => {
    const colors = [
        '#6366F1', // Purple
        '#F97316', // Orange
        '#10B981', // Green
        '#EF4444', // Red
        '#8B5CF6', // Violet
        '#EC4899', // Pink
        '#14B8A6', // Teal
        '#F59E0B', // Yellow
    ];
    return colors[index % colors.length];
};

export default ExpenseBreakdownChart;