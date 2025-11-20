// src/components/budget/BudgetProgress.jsx
import React from 'react';

const BudgetProgress = ({ budget = 0, spent = 0, month }) => {
    // Make sure we always work with numbers
    const safeBudget = Number(budget) || 0;
    const safeSpent = Number(spent) || 0;

    const percentage = safeBudget > 0 ? (safeSpent / safeBudget) * 100 : 0;
    const remaining = safeBudget - safeSpent;
    const isOverBudget = safeSpent > safeBudget;

    const getProgressColor = () => {
        if (percentage >= 100) return '#ef4444'; // red
        if (percentage >= 80) return '#f59e0b';  // amber
        return '#10b981';                        // green
    };

    const getRemainingText = () => {
        if (isOverBudget) {
            return `Over budget by $${Math.abs(remaining).toFixed(2)}`;
        }
        return `$${remaining.toFixed(2)} remaining`;
    };

    const formattedMonth = month
        ? new Date(month + '-01').toLocaleString('en-US', { month: 'long', year: 'numeric' })
        : 'This month';

    return (
        <div className="bg-gray-900 rounded-2xl p-6 text-white h-full flex flex-col">
            <div className="flex items-start justify-between mb-4">
                <div>
                    <p className="text-sm text-gray-400">Budget & Limits</p>
                    <h3 className="text-xl font-semibold mt-1">{formattedMonth}</h3>
                </div>
                <span className="text-sm px-3 py-1 rounded-full bg-gray-800">
                    {percentage.toFixed(0)}% used
                </span>
            </div>

            {/* Main amount */}
            <div className="mb-6">
                <p className="text-3xl font-bold">
                    ${safeBudget.toFixed(2)}
                    <span className="text-sm text-gray-400 ml-2">total budget</span>
                </p>
                <p className="text-sm text-gray-400 mt-1">
                    Spent: <span className="text-white font-medium">${safeSpent.toFixed(2)}</span>
                </p>
            </div>

            {/* Progress bar */}
            <div className="mb-4">
                <div className="flex justify-between text-xs mb-2 text-gray-400">
                    <span>0</span>
                    <span>{getRemainingText()}</span>
                    <span>${safeBudget.toFixed(2)}</span>
                </div>
                <div className="w-full h-3 bg-gray-800 rounded-full overflow-hidden">
                    <div
                        className="h-full rounded-full transition-all duration-300"
                        style={{
                            width: `${Math.min(percentage, 100)}%`,
                            backgroundColor: getProgressColor(),
                        }}
                    />
                </div>
            </div>

            {/* Status */}
            <div className="mt-auto pt-4 border-t border-gray-800">
                <p className="text-sm">
                    Status:{' '}
                    <span className={isOverBudget ? 'text-red-400' : 'text-green-400'}>
                        {isOverBudget ? 'Over budget' : 'On track'}
                    </span>
                </p>

                {safeBudget > 0 && !isOverBudget && (
                    <p className="text-xs text-gray-400 mt-2">
                        Daily average: $
                        {(
                            (safeBudget - safeSpent) /
                            Math.max(1, new Date().getDate())
                        ).toFixed(2)}{' '}
                        remaining / day
                    </p>
                )}
            </div>
        </div>
    );
};

export default BudgetProgress;
